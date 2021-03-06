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

(defn identity-of-user [db] (:identity db))
(rf/reg-sub :identity (fn [db _] (identity-of-user db)))
(defn identity-groups [db] (:groups (identity-of-user db)))
(rf/reg-sub :identity-groups (fn [db _] (identity-groups db)))

#_(rf/reg-event-db :set-route (fn [db [_ route]] (assoc db :route route)))
(rf/reg-sub :route (fn [db _] (:route db)))

(rf/reg-sub
  :route-name
  :<- [:route]
  (fn [route query-v] (get-in route [:data :name])))

(rf/reg-sub
  :route-path
  :<- [:route]
  (fn [route query-v] (:path route)))

(rf/reg-event-fx
  :dispatch-route
  (fn [{db :db} [_ route]]
    (let [groups (get-in route [:data :groups])
          start-event (get-in route [:data :start])
          identity-groups (identity-groups db)]
      (if (and (not-empty groups) (empty? (clojure.set/intersection groups identity-groups)))
        {:dispatch [:set-route {:data {:name :restricted ::comment "Synthetic route"} :path (:path route)}]}
        {:dispatch-n [[:set-route route]
                      (when start-event (conj start-event route))]}))))

(rf/reg-event-db :toggle-sider (fn [db] (update db :sider-collapsed not)))
(rf/reg-sub :sider-collapsed (fn [db] (:sider-collapsed db)))

(rf/reg-sub :status (fn [db _] (:status db)))
(rf/reg-sub :result (fn [db _] (:result db)))

(rf/reg-sub
  :cache
  (fn [db [_ cache-key]] (get-in db [:cache cache-key])))

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

;; TODO: Remove :error-message if it is no longer needed.
(rf/reg-sub
  :error-message
  (fn [[_ cache-key]] (rf/subscribe [:cache cache-key]))
  (fn [cache] (:error-message cache)))

(rf/reg-event-db
  :set-input
  (fn [db [_ cache-key input]]
    (assoc-in db [:cache cache-key :input] input)))

(rf/reg-event-db
  :set-input-kv
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

(rf/reg-sub
  :alert
  (fn [[_ cache-key] _] (rf/subscribe [:cache cache-key]))
  (fn [cache] (:alert cache)))

(rf/reg-event-db
  :set-alert
  (fn [db [_ cache-key alert]]
    (assoc-in db [:cache cache-key :alert] alert)))

(rf/reg-event-fx
  :http-xhrio-graphql-failed
  (fn [{db :db} [_ cache-key result]]
    (let [error (some-> result (get-in [:response :errors]) first)]
      {:db (-> db
               (assoc :status "http xhrio graphql failed")
               (assoc :result result)
               (update-in [:cache cache-key] assoc :input-errors (:input-errors error) :error-message (:message error)))
       :dispatch-n [[:set-input-errors cache-key (:input-errors error)]
                    [:set-error-message cache-key (:message error)]
                    [:set-alert cache-key {:type :error #_:closable #_true :message (:message error)}]]})))

(rf/reg-event-db
  :setup-register
  (fn [db [_ route]]
    (update db :cache dissoc (get-in route [:data :name]))))

(rf/reg-event-fx
  :register
  (fn [{db :db} [_ cache-key input]]
    {:http-xhrio {:method          :post
                  :uri             "/api"
                  :params          {:query     "mutation RegisterUser($register_user_input: RegisterUserInput!) {
                  register_user(input: $register_user_input) { user { email id } } }"
                                    :variables {:register_user_input (csk-extras/transform-keys csk/->snake_case input)}}
                  :format          (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success      [:register-succeeded cache-key]
                  :on-failure      [:http-xhrio-graphql-failed cache-key]}}))

(rf/reg-event-fx
  :register-succeeded
  (fn [{db :db} [e cache-key result]]
    {:db       (-> db
                   (assoc :status e)
                   (assoc :result result)
                   (update :cache dissoc cache-key))
     :navigate :login}))

(defn unmarshal-user
  [m]
  (when m
    (as-> m $
          (csk-extras/transform-keys csk/->kebab-case $)
          (if-let [groups (:groups $)]
            (update $ :groups set)
            $)
          (if-let [locked-at (:locked-at $)]
            (assoc $ :locked-at (js/Date. locked-at))
            $)
          (if-let [created-at (:created-at $)]
            (assoc $ :created-at (js/Date. created-at))
            $))))

(rf/reg-event-db
  :setup-login
  (fn [db [_ route]]
    (update db :cache dissoc (get-in route [:data :name]))))

(rf/reg-event-fx
  :login
  (fn [{db :db} [_ cache-key input]]
    {:http-xhrio {:method          :post
                  :uri             "/api"
                  :params          {:query     "mutation Login($login_input: LoginInput!) {
                  login(input: $login_input) { user { id email groups } } }"
                                    :variables {:login_input (csk-extras/transform-keys csk/->snake_case input)}}
                  :format          (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success      [:login-succeeded cache-key]
                  :on-failure      [:http-xhrio-graphql-failed cache-key]}}))

(rf/reg-event-fx
  :login-succeeded
  (fn [{db :db} [e cache-key result]]
    {:db       (-> db
                   (assoc :status e)
                   (assoc :result result)
                   (assoc :identity (->> result :data :login :user unmarshal-user))
                   (update :cache dissoc cache-key))
     :navigate :home}))

(rf/reg-event-fx
  :logout
  (fn [{db :db} [_ cache-key input]]
    {:db         (dissoc db :identity)
     :http-xhrio {:method          :post
                  :uri             "/api"
                  :params          {:query "mutation { logout { user { email } } }"}
                  :format          (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success      [:logout-succeeded cache-key]
                  :on-failure      [:http-xhrio-graphql-failed cache-key]}}))

(rf/reg-event-fx
  :logout-succeeded
  (fn [{db :db} [e cache-key result]]
    {:db (-> db                                             ; :logout event dissoc's identity
             (assoc :status e)
             (assoc :result result)
             (update :cache dissoc cache-key))}))

(rf/reg-event-fx
  :get-users
  (fn [{db :db}]
    {:db         (-> db
                     (update :admin dissoc :users))
     :http-xhrio {:method          :post
                  :uri             "/api"
                  :params          {:query "{ users { id email locked_at created_at groups } }"}
                  :format          (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success      [:get-users-succeeded]
                  :on-failure      [:http-xhrio-graphql-failed :admin]}}))

(rf/reg-event-fx
  :get-users-succeeded
  (fn [{db :db} [e result]]
    {:db (-> db
             (assoc-in [:admin :users] (->> result :data :users (map unmarshal-user)))
             (assoc :status e)
             (assoc :result result))}))

(rf/reg-sub :admin (fn [db _] (:admin db)))

(rf/reg-sub
  :users
  (fn [_ _] (rf/subscribe [:admin]))
  (fn [admin] (:users admin)))

(rf/reg-event-fx
  :edit-user
  (fn [{db :db} [_ route]]
    (let [cache-key (get-in route [:data :name])]
      {:db         (-> db
                       (update :cache dissoc cache-key))
       :http-xhrio {:method          :post
                    :uri             "/api"
                    :params          {:query     "query User($id: ID!) {
                  user(id: $id) { id email locked_at created_at groups note } }"
                                      :variables {:id (get-in route [:parameters :path :id])}}
                    :format          (ajax/transit-request-format)
                    :response-format (ajax/transit-response-format)
                    :on-success      [:edit-user-query-succeeded cache-key]
                    :on-failure      [:http-xhrio-graphql-failed cache-key]}})))

(rf/reg-event-fx
  :edit-user-query-succeeded
  (fn [{db :db} [e cache-key result]]
    (let [user (->> result :data :user unmarshal-user)]
      {:db (-> db
               (update-in [:cache cache-key] assoc :user user :input (update user :groups vec))
               (assoc :status e :result result))})))

(rf/reg-event-fx
  :edit-user-save
  (fn [{db :db} [_ cache-key input]]
    (console.log "edit-user-save: input:" input)
    {:http-xhrio {:method          :post
                  :uri             "/api"
                  :params          {:query     "mutation Update($update_input: UpdateUserInput!) {
                  update_user(input: $update_input) { user { id email locked_at created_at groups note } } }"
                                    :variables {:update_input (csk-extras/transform-keys csk/->snake_case input)}}
                  :format          (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success      [:edit-user-save-succeeded cache-key]
                  :on-failure      [:http-xhrio-graphql-failed cache-key]}}))

(rf/reg-event-fx
  :edit-user-save-succeeded
  (fn [{db :db} [e cache-key result]]
    (let [user (->> result :data :update_user :user unmarshal-user)]
      {:db       (-> db
                     (update-in [:cache cache-key] assoc
                                :user user
                                :input (update user :groups vec))
                     (assoc :status e :result result))
       :dispatch [:set-alert cache-key {:type :success #_:closable #_true :message "User saved."}]})))