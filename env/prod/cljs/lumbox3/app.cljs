(ns lumbox3.app
  (:require [lumbox3.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
