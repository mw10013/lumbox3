(ns lumbox3.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as gevents]
            [goog.history.EventType :as HistoryEventType]
            [lumbox3.ajax :refer [load-interceptors!]]
            [lumbox3.events :as events]
            [lumbox3.views :as views])
  (:import goog.History))

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [::events/set-main-view :home]))

(secretary/defroute "/about" []
  (rf/dispatch [::events/set-main-view :about]))

;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (gevents/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn mount-components []
  (rf/clear-subscription-cache!)
  #_(r/render [#'page] (.getElementById js/document "app"))
  (r/render [views/root-view] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (hook-browser-navigation!)
  (mount-components))
