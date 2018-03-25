(ns lumbox3.events
  (:require [lumbox3.db :as db]
            [re-frame.core :refer [dispatch reg-event-db reg-sub]]))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :active-page page)))

(reg-sub
  :active-page
  (fn [db _]
    (:active-page db)))

(reg-event-db
  ::set-active-panel
  (fn [db [_ active-panel]]
    (assoc db :active-panel active-panel)))

(reg-sub
  ::active-panel
  (fn [db _]
    (:active-panel db)))
