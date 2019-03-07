(ns model-driver.model.api
  (:require
   [facts-db.ddapi :as ddapi]
   [model-driver.model.module :as module]))


(defn load-module [module-ident events]
  (-> (ddapi/new-db :domain-model-module {:ident module-ident})
      (ddapi/events> events)))


(defn load-from-events [module-ident->module-events]
  {:modules (reduce
             (fn [modules-map [module-ident module-events]]
               (assoc modules-map module-ident (load-module module-ident module-events)))
             {}
             module-ident->module-events)})
