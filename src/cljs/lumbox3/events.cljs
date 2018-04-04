(ns lumbox3.events
  (:require [lumbox3.db :as db]
            [re-frame.core :as rf :refer [dispatch reg-event-db reg-sub reg-sub-raw subscribe]]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [camel-snake-kebab.extras :as csk-extras]
            [camel-snake-kebab.core :as csk])
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

(rf/reg-event-fx
  :register-user
  (fn [{db :db} [_ cache-key input]]
    {:http-xhrio {:method          :post
                  :uri             "/api"
                  :params          {:query     "mutation RegisterUser($register_user_input: RegisterUserInput!) {
                  register_user(input: $register_user_input) { user { email id } } }"
                                    :variables {:register_user_input (csk-extras/transform-keys csk/->snake_case input)}}
                  :format          (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success      [:register-user-succeeded cache-key]
                  :on-failure      [:http-xhrio-graphql-failed cache-key]}}))

(rf/reg-event-db
  :register-user-succeeded
  (fn [db [e cache-key result]]
    (-> db
        (assoc :status e)
        (assoc :result result)
        (update :cache dissoc cache-key)
        (assoc :main-view :login))))

(rf/reg-event-fx
  :login
  (fn [{db :db} [_ cache-key input]]
    {:http-xhrio {:method          :post
                  :uri             "/api"
                  :params          {:query     "mutation Login($login_input: LoginInput!) {
                  login(input: $login_input) { user { email } } }"
                                    :variables {:login_input (csk-extras/transform-keys csk/->snake_case input)}}
                  :format          (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success      [:login-succeeded cache-key]
                  :on-failure      [:http-xhrio-graphql-failed cache-key]}}))

(rf/reg-event-db
  :login-succeeded
  (fn [db [e cache-key result]]
    (-> db
        (assoc :status e)
        (assoc :result result)
        (assoc :identity (->> result :data :login :user (csk-extras/transform-keys csk/->kebab-case)
                             #_(update :roles #(->> % (map gql-enum-to-clj) set))))
        (update :cache dissoc cache-key)
        (assoc :main-view :home))))

(rf/reg-event-fx
  :logout
  (fn [{db :db} [_ cache-key input]]
    {:http-xhrio {:method          :post
                  :uri             "/api"
                  :params          {:query "mutation { logout { user { email } } }"}
                  :format          (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success      [:logout-succeeded cache-key]
                  :on-failure      [:http-xhrio-graphql-failed cache-key]}}))

(rf/reg-event-db
  :logout-succeeded
  (fn [db [e cache-key result]]
    (-> db
        (assoc :status e)
        (assoc :result result)
        (dissoc :identity)
        (update :cache dissoc cache-key)
        (assoc :main-view :logout))))