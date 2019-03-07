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
        :facts #{}
        :projections #{}}
       {:db/id :module
        [:db/add-1 :entities] id}
       {:db/id container-id
        [:db/add-1 :components] id}])))


(defn element-created-facts
  [type id ident facts]
  (let [module-fact (keyword (str (name type) "s"))
        id (or id (db/new-uuid))
        ident (or ident id)]
    [(merge
      facts
      {:db/id id
       :db/type type
       :module :module
       :ident ident})
     {:db/id :module
      [:db/add-1 module-fact] id}]))


(def-event ::event-created
  (fn [model {:keys [id ident]}]
    (element-created-facts :event id ident {:projections #{}
                                            :commands #{}})))


(def-event ::projection-created
  (fn [model {:keys [id ident]}]
    (element-created-facts :projection id ident {:events #{}
                                                 :entities #{}})))


(def-event ::type-created
  (fn [model {:keys [id ident]}]
    (element-created-facts :type id ident {})))


(def-event ::command-created
  (fn [model {:keys [id ident]}]
    (element-created-facts :command id ident {:events #{}})))


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


