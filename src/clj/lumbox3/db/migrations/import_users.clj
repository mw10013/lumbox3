(ns lumbox3.db.migrations.import-users
  (:require [lumbox3.db.core :as db]
            [lumbox3.routes.services :as services]
            [mount.core :as mount]))

(def users (for [domain ["aaa" "bbb" "ccc" "ddd" "eee"]
                 n (range 1 6)]
             {:email (str "user" n "@" domain ".com") :password "letmein"}))

(defn migrate-up [config]
  (mount/start #'db/*db*)
  (doseq [user users]
    (services/register-user nil {:input user} nil)))

(defn migrate-down [config]
  (mount/start #'db/*db*)
  (doseq [user users]
    (db/delete-user-by-email! user)))

(comment
  (mount.core/start #'lumbox3.routes.services/schema)
  "lein run migrate 20180525213300"
  "lein run rollback 20180525213300"
  "lein run reset"
  )
