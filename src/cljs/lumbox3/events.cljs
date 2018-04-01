(ns lumbox3.events
  (:require [lumbox3.db :as db]
            [re-frame.core :as rf :refer [dispatch reg-event-db reg-sub reg-sub-raw subscribe]]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx])
  (:require-macros [reagent.ratom :refer [reaction]]))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-sub
  :identity
  (fn [db _] (:identity db)))

(reg-sub
  :status
  (fn [db _] (:status db)))

(reg-sub
  :result
  (fn [db _] (:result db)))

(reg-event-db
  ::set-main-view
  (fn [db [_ main-view]]
    (assoc db :main-view main-view)))

(reg-sub
  ::main-view
  (fn [db _]
    (:main-view db)))

(reg-sub
  :cache
  (fn [db [_ k]] (get-in db [:cache k])))

(reg-sub-raw
  :input
  (fn [_ [_ cache-key]]
    (reaction
      (:input @(rf/subscribe [:cache cache-key])))))

(reg-sub-raw
  :input-errors
  (fn [_ [_ cache-key]]
    (reaction
      (:input-errors @(rf/subscribe [:cache cache-key])))))

(reg-sub-raw
  :error-message
  (fn [_ [_ cache-key]]
    (reaction
      (:error-message @(rf/subscribe [:cache cache-key])))))

(reg-event-db
  :set-input
  (fn [db [_ cache-key k v]]
    (assoc-in db [:cache cache-key :input k] v)))

(reg-event-db
  :set-input-errors
  (fn [db [_ cache-key input-errors]]
    (assoc-in db [:cache cache-key :input-errors] input-errors)))

(rf/reg-event-db
  :http-xhrio-graphql-failed
  (fn [db [_ k result]]
    (let [error (some-> result (get-in [:response :errors]) first)]
      (-> db
          (assoc :status "http xhrio graphql failed")
          (assoc :result result)
          (update-in [:cache k] assoc :input-errors (:input-errors error) :error-message (:message error))))))