(ns lumbox3.routes
  (:require [re-frame.core :as rf]
            [reitit.core :as r]
            [reitit.coercion.spec]
            [reitit.coercion :as coercion]
            [clojure.test.check.generators]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [lumbox3.events])
  (:import goog.History goog.Uri))

(def router
  (r/router
    ["/"
     ["" {:name :home :breadcrumb-name "Home"}]
     ["about" :about]
     ["admin"
      ["" {:name :admin-dashboard :breadcrumb-name "Admin"}]
      ["/users"
       ["" {:name :admin-users :breadcrumb-name "Users" :start [:get-users]}]
       ["/:id" {:name :admin-user :breadcrumb-name "Edit User"
                :coercion reitit.coercion.spec/coercion
                :parameters {:path {:id string?}}
                :start [:edit-user]}]]
      ["/groups" {:name :admin-groups :breadcrumb-name "Groups"}]]
     ["register" {:name :register :start [:setup-register]}]
     ["login" {:name :login :start [:setup-login]}]
     ["logout" {:name :logout :start [:logout]}]
     ["error" :error]]
    {:compile coercion/compile-request-coercers}))

;; TODO: routes: path: why does match-by-name! not throw exception
;; TODO: routes: path: handle query params
(defn path
  ([route-name]
    (:path (r/match-by-name! router route-name)))
  ([route-name path-params]
    (:path (r/match-by-name! router route-name path-params))))

(defn href
  "Appends # to path for href in link."
  [route-name & path-params]
  (str "#"(apply path route-name path-params)))

(defn breadcrumbs
  "Return breadcrumbs for path as a vector of reitit matches.
   The :data map in each map has keys for :breadcrumb-name
   and breaddrumb-href."
  [path]
  ;; "/admin/users" => "" "admin" "users" => "/" "/admin" "/admin/users"
  (let [split (clojure.string/split path "/")
        paths (reduce (fn [coll x]
                        (conj coll (str (peek coll)
                                        (when-not (= 1 (count coll)) "/")
                                        x)))
                      [] split)]
    (keep (fn [path]
            (when-let [match (r/match-by-path router path)]
              (assoc-in match [:data :breadcrumb-href] (str "#" path)))) paths)))

;; TODO: routes: dispatch-path: handle query params
;; TODO: routes: dispatch-path: handle coercion exception
;; https://google.github.io/closure-library/api/goog.Uri.html
;; https://google.github.io/closure-library/api/goog.Uri.QueryData.html
;; https://github.com/juxt/bidi/issues/51
(defn dispatch-path
  [path-with-query-string]
  (let [uri (goog.Uri. path-with-query-string)]
    (console.log "dispatch-path:" path-with-query-string (.getPath uri) (.getQuery uri))
    (if-let [match (r/match-by-path router (.getPath uri))]
      (let [match (assoc match :parameters (coercion/coerce! match))]
        (rf/dispatch [:set-route match])
        (when-let [event (get-in match [:data :start])]
          (rf/dispatch (conj event match))))
      (rf/dispatch [:set-route {:data {:name :path-not-found ::comment "Synthetic route"} :path (.getPath uri)}]))))

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

;; TODO: switch to html5 history without token
(defonce history
         (doto (History.)
           (goog.events/listen HistoryEventType/NAVIGATE
                               ;; wrap in a fn to allow live reloading
                               #(history-did-navigate %))
           #_(.setEnabled true)))

;; https://google.github.io/closure-library/api/goog.History.html
(defn navigate [route-name]
  (.setToken history (path route-name)))

(defn enable-history!
  "Enable goog history.
   History will dispatch an initial navigate event so may want to
   enable after app db and events set up."
  []
  (.setEnabled history true))
