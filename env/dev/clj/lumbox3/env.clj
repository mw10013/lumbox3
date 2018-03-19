(ns lumbox3.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [lumbox3.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[lumbox3 started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[lumbox3 has shut down successfully]=-"))
   :middleware wrap-dev})
