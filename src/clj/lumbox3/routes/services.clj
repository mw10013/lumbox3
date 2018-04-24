(ns lumbox3.routes.services
  (:require [com.walmartlabs.lacinia.util :as lacinia-util]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia :as l]
            [com.walmartlabs.lacinia.resolve :refer [resolve-as]]
            [clojure.edn :as edn]
            [compojure.api.sweet :refer :all]
            [mount.core :as mount]
            [ring.util.http-response :as response]
            [lumbox3.db.core :as db]
            [camel-snake-kebab.extras :as csk-extras]
            [camel-snake-kebab.core :as csk]
            [lumbox3.validation :as v]
            [buddy.hashers :as hashers]
            [cognitect.anomalies :as anom]
            [clojure.tools.logging :as log]
            [clj-time.coerce :as tc]))

(def ^:private snake-case-keys (partial csk-extras/transform-keys csk/->snake_case))

(defn ^:private marshal-user
  "Transform user map m for graphql."
  [{:keys [user-id] :as m}]
  (-> m
      (assoc :id (str user-id))
      (dissoc :user-id)
      snake-case-keys))

;; TODO: services: users: check perms.
(defn users
  [_ _ _]
  (map marshal-user (db/users)))

(defn register-user
  [_ {:keys [input]} _]
  (let [[errors {:keys [email password]}] (v/validate-register-user-input input)
        _ (println "register-user: :input" input)]
    (if errors
      (resolve-as nil {:message "Invalid input." :anomaly {:category :incorrect} :input-errors errors})
      (try
        (let [encrypted-password (hashers/encrypt password)]
          (->> {:email email :encrypted-password encrypted-password}
               db/create-user!
               marshal-user
               (hash-map :user)))
        (catch Throwable t
          (let [tm (Throwable->map t)
                dupe? (-> tm :cause (.startsWith "ERROR: duplicate key value violates unique constraint \"users_email_key\""))]
            (if dupe?
              (resolve-as nil {:message "Email already used." :anomaly {:category :conflict} :input-errors {:email "Email already used."}})
              (resolve-as nil {:message (:cause tm) :anomaly {:category :fault}}))))))))

(defn login
  "Login by checking password.
   Successful login will create side effect to add an identity to the session containing :user/email."
  [context {:keys [input]} _]
  (if (get-in context [:session :identity])
    (resolve-as nil {:message "Already logged in." :anomaly {:category :fault}})
    (let [[errors {:keys [email password]}] (v/validate-login-input input)]
      (if errors
        (resolve-as nil {:message "Invalid input." :anomaly {:category :incorrect} :input-errors errors})
        (if-let [user (db/user-by-email {:email email})]
          (if (hashers/check password (:encrypted-password user))
            (let [session (or (-> context :side-effects deref :session) (:session context {}))
                  session (assoc session :identity user)]
              (swap! (:side-effects context) assoc :session session)
              {:user (marshal-user user)})
            (resolve-as nil {:message "Invalid login credentials" :anomaly {:category :forbidden}}))
          (resolve-as nil {:message "Invalid login credentials." :anomaly {:category :forbidden}}))))))

(defn logout
  "Logout. Always clears session as a side effect."
  [context _ _]
  (let [email (get-in context [:session :identity :email])]
    (swap! (:side-effects context) assoc :session nil)      ; always clear session.
    (if email
      {:user (-> {:email email} db/user-by-email marshal-user)}
      (resolve-as nil {:message "Not logged in." :anomaly {:category :fault}}))))

;; TODO: schema: error handling for scalar transformers
(mount/defstate schema
                :start (-> "resources/graphql/schema.edn"
                           slurp
                           edn/read-string
                           (lacinia-util/attach-resolvers
                             {:query/users            users
                              :mutation/register-user register-user
                              :mutation/login login
                              :mutation/logout logout})
                           (lacinia-util/attach-scalar-transformers
                             {:date-time-utc-parser     (schema/as-conformer tc/from-string)
                              :date-time-utc-serializer (schema/as-conformer tc/to-string)})
                           schema/compile))

(defn graphql
  "Handles graphql request.
   Adds :session and :side-effects kyes to the lacinia context map.
   Session comes from the request and side-effects is an atom map.
   To change the session on the way out, add a session key to side effects.
   A nil value will clear the session."
  [req]
  (let [{{:keys [query variables operation-name operationName]} :params} req
        query (or query (-> req :body slurp))
        _ (log/debugf "graphql: query: %s" query)
        options {:operation-name (or operation-name operationName)}
        side-effects (atom {})
        context {:session (:session req) :side-effects side-effects}
        result (l/execute schema query variables context options)
        _ (log/debugf "graphql: result: %s" result)
        response (if (:errors result) (response/bad-request result) (response/ok result))
        session (:session @side-effects ::not-found)]       ; nil will clear the session.
    (if (= session ::not-found) response (assoc response :session session))))

(defapi service-routes
  (POST "/api" req (graphql req))
  (GET "/api" req (graphql req)))

(comment
  (mount.core/start #'lumbox3.routes.services/schema)
  (db/users)
  (db/user-by-email {:email "bee@sting.com"})
  (db/user-by-email {:email "bad email"})
  (users nil nil nil)
  (l/execute schema "{ users { id email locked_at created_at } }" nil nil)

  (register-user nil {:input {:email "bee@sting.com" :password "letmein"}} nil)
  (l/execute schema
             "mutation RegisterUser($register_user_input: RegisterUserInput!) {
                register_user(input: $register_user_input) {
                  user { id email } } }"
             {:register_user_input {:email "bee@sting.com" :password "letmein"}} nil)
  (db/delete-user! {:user-id 20})

  (require 'ring.mock.request)
  (ring.mock.request/request :post "/api" {:query "{ users { id email } }"})
  (service-routes (ring.mock.request/request :post "/api" {:query "{ users { id email } }"}))
  (-> (ring.mock.request/request :post "/api" {:query "{ users { id email } }"})
      service-routes :body slurp)

  (hugsql.core/def-db-fns "sql/queries.sql")
  (create-user! db/*db* {:user_email "bee@sting.com" :encrypted_password "letmein"})
  )
