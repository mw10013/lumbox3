(ns lumbox3.routes
  (:require [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [lumbox3.events])
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

(secretary/defroute user-path "/users/:id" [id]
                    (js/console.log (str "User " id "'s path")))

;; https://lispcast.com/mastering-client-side-routing-with-secretary-and-goog-history/
(defn history-did-navigate [e]
  (js/console.log (str "history-did-navigate: " (.-token e)))
  #_(js/console.log e)

  ;; we are checking if this event is due to user action,
  ;; such as click a link, a back button, etc.
  ;; as opposed to programmatically setting the URL with the API
  (when-not (.-isNavigation e)
    ;; in this case, we're setting it
    (js/console.log "history-did-navigate: Token set programmatically")
    ;; let's scroll to the top to simulate a navigation
    #_(js/window.scrollTo 0 0))

  (secretary/dispatch! (.-token e)))

(defonce history
         (doto (History.)
           (goog.events/listen HistoryEventType/NAVIGATE
                               ;; wrap in a fn to allow live reloading
                               #(history-did-navigate %))
           (.setEnabled true)))

;; https://google.github.io/closure-library/api/goog.History.html
(defn navigate [token]
  (.setToken history token))

