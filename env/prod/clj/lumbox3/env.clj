(ns lumbox3.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[lumbox3 started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[lumbox3 has shut down successfully]=-"))
   :middleware identity})
