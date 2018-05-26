(ns lumbox3.db.migrations.import-users
  (:require [lumbox3.db.core :as db]
            [lumbox3.routes.services :as services]
            [mount.core :as mount]))

(defn migrate-up [config]
  (mount/start #'db/*db*)
  (services/register-user nil {:input {:email "user@sting.com" :password "letmein"}} nil))

(comment
  (mount.core/start #'lumbox3.routes.services/schema)
  "lein run migrate 20180525213300"
  "lein run rollback 20180525213300"
  )
