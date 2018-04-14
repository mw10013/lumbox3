(ns lumbox3.routes
  (:require [re-frame.core :as rf]
            [reitit.core :as r]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [lumbox3.events])
  (:import goog.History goog.Uri))

(def router
  (r/router
    ["/"
     ["" :home]
     ["about" :about]
     ["register" :register]
     ["login" :login]
     ["logout" :logout]
     ["error" :error]]))

(defn dispatch-path
  "TODO: handle 404."
  [path-with-query-string]
  (let [uri (goog.Uri. path-with-query-string)]
    (console.log "dispatch-path:" path-with-query-string (.getPath uri) (.getQuery uri))
    (if-let [match (r/match-by-path router (.getPath uri))]
      (rf/dispatch [:lumbox3.events/set-main-view (get-in match [:data :name])])
      (rf/dispatch [:lumbox3.events/set-main-view :invalid-path]))))

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

  (dispatch-path (.-token e)))

(defonce history
         (doto (History.)
           (goog.events/listen HistoryEventType/NAVIGATE
                               ;; wrap in a fn to allow live reloading
                               #(history-did-navigate %))
           (.setEnabled true)))

;; https://google.github.io/closure-library/api/goog.History.html
(defn navigate [token]
  (.setToken history token))

