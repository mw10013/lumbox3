(ns lumbox3.events
  (:require [lumbox3.db :as db]
            [re-frame.core :as rf]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [camel-snake-kebab.extras :as csk-extras]
            [camel-snake-kebab.core :as csk])
  #_(:require-macros [reagent.ratom :refer [reaction]]))

;; https://github.com/Day8/re-frame/blob/master/docs/SubscriptionFlow.md
;; https://github.com/Day8/re-frame/blob/master/examples/todomvc/src/todomvc/subs.cljs

(rf/reg-event-db :initialize-db (fn [_ _] db/default-db))

(rf/reg-event-db :set-route (fn [db [_ route]] (assoc db :route route)))
(rf/reg-sub :route (fn [db _] (:route db)))

(rf/reg-sub
  :route-name
  :<- [:route]
  (fn [route query-v] (get-in route [:data :name])))

(rf/reg-sub :identity (fn [db _] (:identity db)))

(rf/reg-sub :status (fn [db _] (:status db)))
(rf/reg-sub :result (fn [db _] (:result db)))

(rf/reg-event-db
  ::set-main-view
  (fn [db [_ main-view]]
    (assoc db :main-view main-view)))

(rf/reg-sub
  ::main-view
  (fn [db _]
    (:main-view db)))

(rf/reg-sub
  :cache
  (fn [db [_ k]] (get-in db [:cache k])))

#_(reg-sub-raw
  :input
  (fn [_ [_ cache-key]]
    (reaction
      (:input @(rf/subscribe [:cache cache-key])))))

(rf/reg-sub
  :input
  (fn [[_ cache-key] _] (rf/subscribe [:cache cache-key]))
  (fn [cache] (:input cache)))

(rf/reg-sub
  :input-errors
  (fn [[_ cache-key]] (rf/subscribe [:cache cache-key]))
  (fn [cache] (:input-errors cache)))

(rf/reg-sub
  :error-message
  (fn [[_ cache-key]] (rf/subscribe [:cache cache-key]))
  (fn [cache] (:error-message cache)))

(rf/reg-event-db
  :set-input
  (fn [db [_ cache-key k v]]
    (assoc-in db [:cache cache-key :input k] v)))

(rf/reg-event-db
  :set-input-errors
  (fn [db [_ cache-key input-errors]]
    (assoc-in db [:cache cache-key :input-errors] input-errors)))

(rf/reg-event-db
  :set-error-message
  (fn [db [_ cache-key error-message]]
    (assoc-in db [:cache cache-key :error-message] error-message)))

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

(rf/reg-event-fx
  :register-user-succeeded
  (fn [{db :db} [e cache-key result]]
    {:db (-> db
             (assoc :status e)
             (assoc :result result)
             (update :cache dissoc cache-key))
     :navigate "/login"}))

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

(rf/reg-event-fx
  :login-succeeded
  (fn [{db :db} [e cache-key result]]
    {:db (-> db
             (assoc :status e)
             (assoc :result result)
             (assoc :identity (->> result :data :login :user (csk-extras/transform-keys csk/->kebab-case)
                                   #_(update :roles #(->> % (map gql-enum-to-clj) set))))
             (update :cache dissoc cache-key))
     :navigate "/"}))

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

(rf/reg-event-fx
  :logout-succeeded
  (fn [{db :db} [e cache-key result]]
    {:db (-> db
             (assoc :status e)
             (assoc :result result)
             (dissoc :identity)
             (update :cache dissoc cache-key))
     :navigate "/logout"}))