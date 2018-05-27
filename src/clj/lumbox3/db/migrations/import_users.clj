(ns lumbox3.db.migrations.import-users
  (:require [lumbox3.db.core :as db]
            [lumbox3.routes.services :as services]
            [mount.core :as mount]))

(def admin-email "admin@admin.com")
(def password "letmein")
(def users (for [domain ["aaa" "bbb" "ccc" "ddd" "eee"]
                 n (range 1 6)]
             {:email (str "user" n "@" domain ".com") :password password}))
(def users (for [n (range 1 3)
                 offset (range 26)]
             {:email (str "user@" (->> \a int (+ offset) char (repeat n) (apply str)) ".com")
              :password password}))

(defn migrate-up [config]
  (mount/start #'db/*db*)
  (doseq [user users]
    (services/register-user nil {:input user} nil))
  (let [payload (services/register-user nil {:input {:email admin-email :password password}} nil)
        user-id (some-> payload :user :id bigint)]
    (db/add-user-to-group! {:user-id user-id :group-id "admins"})))

(defn migrate-down [config]
  (mount/start #'db/*db*)
  (doseq [user users]
    (db/delete-user-by-email! user))
  (db/delete-user-by-email! {:email admin-email}))

(comment
  (mount.core/start #'lumbox3.routes.services/schema)
  "lein run migrate 20180525213300"
  "lein run rollback 20180525213300"
  "lein run reset"
  )
