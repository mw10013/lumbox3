(ns lumbox3.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [lumbox3.ajax :refer [load-interceptors!]]
            [lumbox3.events]
            [lumbox3.views :as views])
  (:import goog.History))

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
                    (rf/dispatch [:lumbox3.events/set-main-view :home]))
(secretary/defroute "/about" []
                    (rf/dispatch [:lumbox3.events/set-main-view :about]))
(secretary/defroute "/register" []
                    (rf/dispatch [:lumbox3.events/set-main-view :register]))
(secretary/defroute "/login" []
                    (rf/dispatch [:lumbox3.events/set-main-view :login]))
(secretary/defroute "/logout" []
                    (rf/dispatch [:lumbox3.events/set-main-view :logout]))


;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
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
