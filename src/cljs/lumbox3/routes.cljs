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

;; TODO: routes: path: why does match-by-name! not throw exception
(defn path
  [route-name & path-params]
  (:path (apply r/match-by-name! router route-name path-params)))

(defn href
  "Appends # to path for href in link."
  [route-name & path-params]
  (str "#"(apply path route-name path-params)))

;; TODO: handle 404
;; TODO: handle query params
(defn dispatch-path
  [path-with-query-string]
  (let [uri (goog.Uri. path-with-query-string)]
    (console.log "dispatch-path:" path-with-query-string (.getPath uri) (.getQuery uri))
    (if-let [match (r/match-by-path router (.getPath uri))]
      (rf/dispatch [:set-route match])
      (rf/dispatch [:set-route nil]))))

;; https://lispcast.com/mastering-client-side-routing-with-secretary-and-goog-history/
(defn history-did-navigate [e]
  (js/console.log (str "history-did-navigate: " (.-token e)))

  ;; isNavigation indicates user action as opposed to programmatic.
  (when-not (.-isNavigation e)
    (js/console.log "history-did-navigate: Token set programmatically")
    ;; scroll to the top to simulate a navigation
    #_(js/window.scrollTo 0 0))

  ;; When goog history is enabled, it will dispatch an initial token,
  ;; which may be empty.
  (if (empty? (.-token e))
    (.setToken (.-target e) "/")
    (dispatch-path (.-token e))))

(defonce history
         (doto (History.)
           (goog.events/listen HistoryEventType/NAVIGATE
                               ;; wrap in a fn to allow live reloading
                               #(history-did-navigate %))
           #_(.setEnabled true)))

;; https://google.github.io/closure-library/api/goog.History.html
(defn navigate [token]
  (.setToken history token))

(defn enable-history!
  "Enable goog history.
   History will dispatch an initial navigate event so may want to
   enable after app db and events set up."
  []
  (.setEnabled history true))
