(ns lumbox3.routes.services
  (:require [com.walmartlabs.lacinia.util :as lacinia-util]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia :as l]
            [clojure.data.json :as json]
            [clojure.edn :as edn]
            [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [mount.core :as mount]))

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

(defn format-params [query]
   (let [parsed (json/read-str query)] ;;-> placeholder - need to ensure query meets graphql syntax
     (str "query { hero(id: \"1000\") { name appears_in }}")))

(defn execute-request [query]
    (let [vars nil
          context nil]
    (-> (l/execute compiled-schema query vars context)
        (json/write-str))))

(defapi service-routes
  (POST "/api" [:as {body :body}]
      (ok (execute-request (slurp body)))))

(comment
  (mount.core/start #'lumbox3.routes.services/graphql-schema)
  (l/execute schema "{ hero(id: \"id\") { id name appears_in } }" nil nil)
  )
