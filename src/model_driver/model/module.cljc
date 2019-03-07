(ns model-driver.model.module
  (:require
   [bindscript.api :refer [def-bindscript]]
   [facts-db.api :as db]
   [facts-db.ddapi :as ddapi :refer [def-event def-query def-api events> <query new-db]]
   [conform.api :as conform]))


(def-api ::domain-model-module
  :db-constructor
  (fn [{:keys [ident]}]
    [{:db/id :module
      :ident ident
      :entities #{}
      :events #{}
      :projections #{}
      :types #{}
      :commands #{}}]))


(def-event ::entity-created
  (fn [model {:keys [id container-id ident]}]
    (let [ident (or ident :some/entity)
          id (or id (db/new-uuid))]
      [{:db/id id
        :db/type :entity
        :module :module
        :container container-id
        :ident ident
        :components #{}
        :facts #{}}
       {:db/id :module
        [:db/add-1 :entities] id}
       {:db/id container-id
        [:db/add-1 :components] id}])))


(defn element-created-facts
  [type id facts]
  (let [module-fact (keyword (str (name type) "s"))
        id (or id (db/new-uuid))]
    [(merge
      facts
      {:db/id id
       :db/type type
       :module :module})
     {:db/id :module
      [:db/add-1 module-fact] id}]))


(def-event ::event-created
  (fn [model {:keys [id ident]}]
    (element-created-facts :event
                           id
                           {:ident ident
                            :projections #{}})))


(def-event ::projection-created
  (fn [model {:keys [id ident]}]
    (element-created-facts :projection
                           id
                           {:ident ident
                            :event-handlers #{}})))


(def-event ::projection-event-handler-created
  (fn [model {:keys [id ident projection-id event-id]}]
    (let [id (or id (db/new-uuid))]
      (into
       (element-created-facts :projection-event-handler
                              id
                              {:projection projection-id
                               :event event-id})
       [{:db/id projection-id
         [:db/add-1 :event-handlers] id}
        {:db/id event-id
         [:db/add-1 :projections] id}]))))


(def-event ::type-created
  (fn [model {:keys [id ident]}]
    (element-created-facts :type
                           id
                           {:ident ident})))


(def-event ::command-created
  (fn [model {:keys [id ident]}]
    (element-created-facts :command
                           id
                           {:ident ident
                            :events #{}})))


(def-event ::element-fact-updated
  (fn [model {:keys [element-id fact value]}]
    {:db/id element-id
     fact value}))


(def-query ::module-details
  (fn [model _]
    (db/tree model :module {:entities {}
                            :types {}})))


(def-query ::entity
  (fn [model {:keys [id]}]
    (db/tree model id {})))


(def-query ::entities
  (fn [model {:keys [ids]}]
    (db/trees model ids {})))


