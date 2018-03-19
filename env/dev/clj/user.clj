(ns user
  (:require [luminus-migrations.core :as migrations]
            [lumbox3.config :refer [env]]
            [mount.core :as mount]
            [lumbox3.figwheel :refer [start-fw stop-fw cljs]]
            [lumbox3.core :refer [start-app]]))

(defn start []
  (mount/start-without #'lumbox3.core/repl-server))

(defn stop []
  (mount/stop-except #'lumbox3.core/repl-server))

(defn restart []
  (stop)
  (start))

(defn reset-db []
  (migrations/migrate ["reset"] {:database-url (:config-database-url env)}))

(defn migrate []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration [name]
  (migrations/create name (select-keys env [:database-url])))


