(ns lumbox3.fx
  (:require [re-frame.core :as rf]
            [lumbox3.routes :as routes]))

;; TODO: navigate fx: handle path params and query params
(rf/reg-fx
  :navigate
  (fn [route-name]
    (routes/navigate route-name)))
