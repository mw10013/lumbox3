(ns lumbox3.routes.services
  (:require [com.walmartlabs.lacinia.util :as lacinia-util]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia :as l]
            [clojure.edn :as edn]
            [compojure.api.sweet :refer :all]
            [mount.core :as mount]
            [ring.util.http-response :as response]))

(defn get-hero [context args value]
  (let [data  [{:id 1000
               :name "Luke"
               :home_planet "Tatooine"
               :appears_in ["NEWHOPE" "EMPIRE" "JEDI"]}
              {:id 2000
               :name "Lando Calrissian"
               :home_planet "Socorro"
               :appears_in ["EMPIRE" "JEDI"]}]]
           (first data)))

(mount/defstate schema
          :start (-> "resources/graphql/schema.edn"
                     slurp
                     edn/read-string
                     (lacinia-util/attach-resolvers
                       {:get-hero get-hero
                        :get-droid (constantly {})})
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
  (l/execute schema "{ hero(id: \"id\") { id name appears_in } }" nil nil)

  (require 'ring.mock.request)
  (ring.mock.request/request :post "/api" {:query "{ hero(id: \"id\") { id name appears_in } }"})
  (service-routes (ring.mock.request/request :post "/api" {:query "{ hero(id: \"id\") { id name appears_in } }"}))
  (-> (ring.mock.request/request :post "/api" {:query "{ hero(id: \"id\") { id name appears_in } }"})
      service-routes :body slurp)
  )
