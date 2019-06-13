(ns model-driver.runtime.api
  (:require
   [facts-db.api :as db]
   [facts-db.ddapi :as ddapi]
   [model-driver.model.api :as model]))


(defn create-projections [module]
  (reduce
   (fn [projections-map {:keys [ident singleton?]}]
     (if singleton?
       (let [module-ident (:ident module)
             api-key (keyword (str (name (:ident module))
                                   ".projection."
                                   (name ident)))]
         (assoc-in projections-map
                   [ident :singleton]
                   (ddapi/new-db api-key {})))
       (assoc-in projections-map [ident] {})))
   {}
   (:projections module)))



(defn create-module-runtime [module-db]
  (let [module (db/tree module-db :module {:projections {}})]
    {:ident (:ident module)
     :projections (create-projections module)}))


(defn create-modules-runtimes [model]
  (reduce
   (fn [runtime [module-name module-db]]
     (assoc runtime module-name (create-module-runtime module-db)))
   {}
   (:modules model)))


(defn initialize [db config]
  (let [modules-events (:domain-model/modules-events config)
        model (model/load-from-events modules-events)]
    (-> db
        (assoc :domain-model/model model) ;; TODO remove this
        (assoc :model-driver/model model)

        (assoc-in [:model-driver/runtime :modules]
                  (create-modules-runtimes model)))))


;; (defn project-events [projection projection-model events])


;; (defn project-events-in-module [module-runtime module-model events]
;;   (let [projections-models
;;         (-> module-model
;;             (db/tree :module
;;                      {:projections {:event-handlers {:event {}}}})
;;             :projections)]
;;     (tap> [::!!!!!!projections projections-models])
;;     (reduce
;;      (fn [module-runtime projection-model]
;;        (update-in module-runtime [:projections projection-ident]))
;;      module-runtime
;;      projections-models)))
  ;;(update module-runtime :projections #(map project-events)))


(defn- projection-dispatch-event [projection projection-model [event-ident event-args]]
  (if-let [handler-model (first
                          (filter #(= event-ident (get-in % [:event :ident]))
                                  (:event-handlers projection-model)))]
    (ddapi/events> projection [[event-ident event-args]])
    projection))


(defn- projection-dispatch-events [projection projection-model events]
  (reduce
   (fn [projection event]
     (projection-dispatch-event projection projection-model event))
   projection
   events))


(defn- projections-dispatch-events [projections-map projection-model events]
  (if (:singleton? projection-model)
    (update projections-map
            :singleton
            projection-dispatch-events
            projection-model
            events)
    (throw (ex-info "projections-dispatch-events not implemented for non-singleton" {}))))


(defn- module-dispatch-events [module-runtime module-model events]
  (let [projections-models
        (-> module-model
            (db/tree :module
                     {:projections {:event-handlers {:event {}}}})
            :projections)]
    (reduce
     (fn [module-runtime projection-model]
       (update-in module-runtime
                 [:projections (:ident projection-model)]
                 projections-dispatch-events
                 projection-model
                 events
         module-runtime))
     module-runtime
     projections-models)))


(defn dispatch-event [db [event-key event-args]]
  (let [module-ident (keyword (namespace event-key))
        event-ident (keyword (name event-key))
        module-model (get-in db [:model-driver/model :modules module-ident])]
    (update-in db [:model-driver/runtime :modules module-ident]
               module-dispatch-events
               module-model
               [[event-ident event-args]])))
