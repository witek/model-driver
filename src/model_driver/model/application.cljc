(ns model-driver.model.application
  (:require
   [bindscript.api :refer [def-bindscript]]
   [facts-db.api :as db]
   [facts-db.ddapi :as ddapi :refer [def-event def-query def-api events> <query new-db]]
   [conform.api :as conform]))


;; (def-api ::domain-model-application
;;   :db-constructor
;;   (fn [{:keys [ident]}]
;;     [{:db/id :application
;;       :ident ident}]))


;; (def-event ::entity-created
;;   (fn [model {:keys [id container-id ident]}]
;;     (let [ident (or ident :some/entity)
;;           id (or id (db/new-uuid))]
;;       [{:db/id id
;;         :db/type :entity
;;         :module :module
;;         :container container-id
;;         :ident ident
;;         :components #{}
;;         :facts #{}
;;         :projection-event-handlers #{}
;;         :event-attributes #{}}
;;        {:db/id :module
;;         [:db/add-1 :entities] id}
;;        {:db/id container-id
;;         [:db/add-1 :components] id}])))
