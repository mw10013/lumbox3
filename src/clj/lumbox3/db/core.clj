(ns lumbox3.db.core
  (:require
    [cheshire.core :refer [generate-string parse-string]]
    [clj-time.jdbc]
    [clojure.java.jdbc :as jdbc]
    [conman.core :as conman]
    [lumbox3.config :refer [env]]
    [mount.core :refer [defstate]]
    [camel-snake-kebab.extras :as csk-extras]
    [camel-snake-kebab.core :as csk])
  (:import org.postgresql.util.PGobject
           java.sql.Array
           clojure.lang.IPersistentMap
           clojure.lang.IPersistentVector
           [java.sql
            BatchUpdateException
            PreparedStatement]))

(defstate ^:dynamic *db*
  :start (conman/connect! {:jdbc-url (env :database-url)})
  :stop (conman/disconnect! *db*))

(defn wrap-sql-fns-
  "Wrap sql fns to snake case the parameter map.
   The keys of m must correspond to the names of the fns."
  ; https://pupeno.com/2015/10/23/automatically-converting-case-between-sql-and-clojure/
  ; https://github.com/pupeno/ninjatools/blob/master/src/clj/ninjatools/db/core.clj
  [m]
  (doall (for [k (keys m)]
           (alter-var-root (find-var (symbol (-> *ns* ns-name name) (name k)))
                           (fn [f]
                             (fn [& args]
                               ; Transform parameter map keys to snake case.
                               ; Parameter map is the first arg if only one arg otherwise the second.
                               (let [args (condp = (count args)
                                            0 args
                                            1 (->> args first (csk-extras/transform-keys csk/->snake_case) vector)
                                            (concat [(first args)
                                                     (->> args second (csk-extras/transform-keys csk/->snake_case))]
                                                    (nnext args)))]
                                 (apply f args))))))))

(defn wrap-sql-fns
  "For names in namespace ns, wrap the corresponding var with a fn
   that transforms the parameter map keys to snake case before calling
   the underlying sql fn."
  [ns names]
  (doall
    (for [n names]
      (alter-var-root
        (find-var (symbol (-> ns ns-name name) n))
        (fn [f]
          (fn with-snake-case-params
            ([] (f))
            ([params] (f (csk-extras/transform-keys csk/->snake_case params)))
            ([conn params & args] (apply f conn (csk-extras/transform-keys csk/->snake_case params) args))))))))

#_(conman/bind-connection *db* "sql/queries.sql")
(->> (conman/bind-connection *db* "sql/queries.sql") :fns keys (map name) (wrap-sql-fns *ns*))
#_(hugsql.core/def-db-fns "sql/queries.sql")
#_(hugsql.core/def-sqlvec-fns "sql/queries.sql")

(extend-protocol jdbc/IResultSetReadColumn
  Array
  (result-set-read-column [v _ _] (vec (.getArray v)))

  PGobject
  (result-set-read-column [pgobj _metadata _index]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (parse-string value true)
        "jsonb" (parse-string value true)
        "citext" (str value)
        value))))

(defn to-pg-json [value]
      (doto (PGobject.)
            (.setType "jsonb")
            (.setValue (generate-string value))))

(extend-type clojure.lang.IPersistentVector
  jdbc/ISQLParameter
  (set-parameter [v ^java.sql.PreparedStatement stmt ^long idx]
    (let [conn      (.getConnection stmt)
          meta      (.getParameterMetaData stmt)
          type-name (.getParameterTypeName meta idx)]
      (if-let [elem-type (when (= (first type-name) \_) (apply str (rest type-name)))]
        (.setObject stmt idx (.createArrayOf conn elem-type (to-array v)))
        (.setObject stmt idx (to-pg-json v))))))

(extend-protocol jdbc/ISQLValue
  IPersistentMap
  (sql-value [value] (to-pg-json value))
  IPersistentVector
  (sql-value [value] (to-pg-json value)))

(defn result-one-snake->kebab
  [this result options]
  (->> (hugsql.adapter/result-one this result options)
       (csk-extras/transform-keys csk/->kebab-case-keyword)))

(defn result-many-snake->kebab
  [this result options]
  (->> (hugsql.adapter/result-many this result options)
       (map #(csk-extras/transform-keys csk/->kebab-case-keyword %))))

(defmethod hugsql.core/hugsql-result-fn :1 [sym]
  'lumbox3.db.core/result-one-snake->kebab)

(defmethod hugsql.core/hugsql-result-fn :one [sym]
  'lumbox3.db.core/result-one-snake->kebab)

(defmethod hugsql.core/hugsql-result-fn :* [sym]
  'lumbox3.db.core/result-many-snake->kebab)

(defmethod hugsql.core/hugsql-result-fn :many [sym]
  'lumbox3.db.core/result-many-snake->kebab)

(defmethod hugsql.core/hugsql-result-fn :raw [sym]
  'lumbox3.db.core/result-many-snake->kebab)

(defmethod hugsql.core/hugsql-result-fn :default [sym]
  'lumbox3.db.core/result-many-snake->kebab)

(comment
  (users)
  (user-by-email {:user-email "bar@bar.com"})
  (create-user! {:user-email "bar@bar.com" :encrypted-password "letmein"})
  (delete-user! {:user-id 3})
  )
