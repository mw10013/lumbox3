(ns lumbox3.routes.services
  (:require [com.walmartlabs.lacinia.util :as lacinia-util]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia :as l]
            [clojure.edn :as edn]
            [compojure.api.sweet :refer :all]
            [mount.core :as mount]
            [ring.util.http-response :as response]
            [lumbox3.db.core :as db]
            [camel-snake-kebab.extras :as csk-extras]
            [camel-snake-kebab.core :as csk]))

(def ^:private snake-case-keys (partial csk-extras/transform-keys csk/->snake_case))

(defn ^:private marshal-user
  "Transform user map m for graphql."
  [{:keys [user-id user-email] :as m}]
  (-> m
      (assoc :id (str user-id) :email user-email)
      (dissoc :user-id :user-email)
      snake-case-keys))

(defn users
  "TODO: check perms."
  [_ _ _]
  (map marshal-user (db/users)))

(mount/defstate schema
          :start (-> "resources/graphql/schema.edn"
                     slurp
                     edn/read-string
                     (lacinia-util/attach-resolvers
                       {:query/users users})
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
        _ (println "graphql: query:" query)
        options {:operation-name (or operation-name operationName)}
        side-effects (atom {})
        context {:session (:session req) :side-effects side-effects}
        result (l/execute schema query variables context options)
        _ (println "graphql: result:" result)
        response (if (:errors result) (response/bad-request result) (response/ok result))
        session (:session @side-effects ::not-found)]       ; nil will clear the session.
    (if (= session ::not-found) response (assoc response :session session))))

(defapi service-routes
  (POST "/api" req (graphql req))
  (GET "/api" req (graphql req)))

(comment
  (mount.core/start #'lumbox3.routes.services/graphql-schema)
  (db/users)
  (users nil nil nil)
  (l/execute schema "{ users { id email encrypted_password } }" nil nil)

  (require 'ring.mock.request)
  (ring.mock.request/request :post "/api" {:query "{ users { id email encrypted_password } }"})
  (service-routes (ring.mock.request/request :post "/api" {:query "{ users { id email encrypted_password } }"}))
  (-> (ring.mock.request/request :post "/api" {:query "{ users { id email encrypted_password } }"})
      service-routes :body slurp)
  )
