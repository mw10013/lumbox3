(ns lumbox3.routes.services
  (:require [com.walmartlabs.lacinia.util :refer [attach-resolvers]]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia :as lacinia]
            [clojure.data.json :as json]
            [clojure.edn :as edn]
            [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]))



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

(def compiled-schema
  (-> "resources/graphql/schema.edn"
      slurp
      edn/read-string
      (attach-resolvers {:get-hero get-hero
                         :get-droid (constantly {})})
      schema/compile))

(defn format-params [query]
   (let [parsed (json/read-str query)] ;;-> placeholder - need to ensure query meets graphql syntax
     (str "query { hero(id: \"1000\") { name appears_in }}")))

(defn execute-request [query]
    (let [vars nil
          context nil]
    (-> (lacinia/execute compiled-schema query vars context)
        (json/write-str))))

(defapi service-routes
  (POST "/api" [:as {body :body}]
      (ok (execute-request (slurp body)))))
