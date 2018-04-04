(ns lumbox3.fx
  (:require [re-frame.core :as rf]
            [lumbox3.routes :as routes]))

(rf/reg-fx
  :navigate
  (fn [token]
    (routes/navigate token)))
