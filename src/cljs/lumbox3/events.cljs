(ns lumbox3.events
  (:require [lumbox3.db :as db]
            [re-frame.core :refer [dispatch reg-event-db reg-sub]]))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  ::set-main-view
  (fn [db [_ main-view]]
    (assoc db :main-view main-view)))

(reg-sub
  ::main-view
  (fn [db _]
    (:main-view db)))
