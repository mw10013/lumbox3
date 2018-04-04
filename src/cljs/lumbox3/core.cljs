(ns lumbox3.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [lumbox3.ajax :refer [load-interceptors!]]
            lumbox3.routes
            lumbox3.fx
            lumbox3.events
            [lumbox3.views :as views]))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [views/root-view] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (mount-components))
