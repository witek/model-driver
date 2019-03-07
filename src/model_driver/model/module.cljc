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
      :events #{}}]))


(def-event ::entity-created
  (fn [model {:keys [id container-id ident]}]
    (let [ident (or ident :some/entity)
          id (or id (db/new-uuid))]
      [{:db/id id
        :container container-id
        :ident ident
        :components #{}
        :facts #{}}
       {:db/id :module
        [:db/add-1 :entities] id}
       {:db/id container-id
        [:db/add-1 :components] id}])))


(def-event ::event-created
  (fn [model {:keys [id ident]}]
    (let [ident (or ident :some/event)
          id (or id (db/new-uuid))]
      [{:db/id id
        :ident ident
        :facts #{}}
       {:db/id :module
        [:db/add-1 :events] id}])))


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


