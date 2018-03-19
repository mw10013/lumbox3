(ns lumbox3.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [lumbox3.layout :refer [error-page]]
            [lumbox3.routes.home :refer [home-routes]]
            [lumbox3.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [lumbox3.env :refer [defaults]]
            [mount.core :as mount]
            [lumbox3.middleware :as middleware]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(mount/defstate app
  :start
  (middleware/wrap-base
    (routes
      (-> #'home-routes
          (wrap-routes middleware/wrap-csrf)
          (wrap-routes middleware/wrap-formats))
          #'service-routes
      (route/not-found
        (:body
          (error-page {:status 404
                       :title "page not found"}))))))
