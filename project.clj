(defproject lumbox3 "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[clj-time "0.14.2"]
                 [cljs-ajax "0.7.3"]
                 [com.walmartlabs/lacinia "0.25.0"]
                 [compojure "1.6.0"]
                 [conman "0.7.6"]
                 [cprop "0.1.11"]
                 [funcool/struct "1.2.0"]
                 [luminus-immutant "0.2.4"]
                 [luminus-migrations "0.5.0"]
                 [luminus-nrepl "0.1.4"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [markdown-clj "1.0.2"]
                 [metosin/compojure-api "1.1.11"]
                 [metosin/muuntaja "0.5.0"]
                 [metosin/ring-http-response "0.9.0"]
                 [metosin/reitit "0.1.0"]
                 [mount "0.1.12"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946" :scope "provided"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.clojure/tools.reader "1.2.2"]
                 [org.postgresql/postgresql "42.2.1"]
                 #_[org.webjars.bower/tether "1.4.3"]
                 #_[org.webjars/bootstrap "4.0.0"]
                 #_[org.webjars/font-awesome "5.0.6"]
                 [cljsjs/react "16.2.0-3"]
                 [cljsjs/react-dom "16.2.0-3"]
                 [cljsjs/react-dom-server "16.2.0-3"]
                 [re-frame "0.10.5"]
                 [reagent "0.7.0"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-defaults "0.3.1"]
                 #_[secretary "1.2.3"]
                 [selmer "1.11.7"]
                 [camel-snake-kebab "0.4.0"]
                 [com.cognitect/anomalies "0.1.12"]
                 [buddy "2.0.0"]
                 #_[cljsjs/semantic-ui-react "0.78.2-0" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [day8.re-frame/http-fx "0.1.4"]
                 [cljsjs/antd "3.3.0-0"]
                 [cljsjs/moment "2.17.1-1"]]

  :min-lein-version "2.0.0"
  
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main ^:skip-aot lumbox3.core
  :migratus {:store :database :db ~(get (System/getenv) "DATABASE_URL")}

  :plugins [[migratus-lein "0.5.4"]
            [lein-cljsbuild "1.1.5"]
            [lein-immutant "2.1.0"]]
  :clean-targets ^{:protect false}
  [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]
  :figwheel
  {:http-server-root "public"
   :nrepl-port 7002
   :css-dirs ["resources/public/css"]
   :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  

  :profiles
  {:uberjar {:omit-source true
             :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
             :cljsbuild
             {:builds
              {:min
               {:source-paths ["src/cljc" "src/cljs" "env/prod/cljs"]
                :compiler
                {:output-dir "target/cljsbuild/public/js"
                 :output-to "target/cljsbuild/public/js/app.js"
                 :source-map "target/cljsbuild/public/js/app.js.map"
                 :optimizations :advanced
                 :pretty-print false
                 :closure-warnings
                 {:externs-validation :off :non-standard-jsdoc :off}
                 :externs ["react/externs/react.js"]}}}}
             
             
             :aot :all
             :uberjar-name "lumbox3.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-server" "-Dconf=dev-config.edn"]
                  :dependencies [[binaryage/devtools "0.9.9"]
                                 [com.cemerick/piggieback "0.2.2"]
                                 [day8.re-frame/re-frame-10x "0.2.0"]
                                 [doo "0.1.8"]
                                 [figwheel-sidecar "0.5.15"]
                                 [pjstadig/humane-test-output "0.8.3"]
                                 [prone "1.5.0"]
                                 [ring/ring-devel "1.6.3"]
                                 [ring/ring-mock "0.3.2"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.19.0"]
                                 [lein-doo "0.1.8"]
                                 [lein-figwheel "0.5.15"]
                                 [org.clojure/clojurescript "1.9.946"]]
                  :cljsbuild
                  {:builds
                   {:app
                    {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                     :figwheel {:on-jsload "lumbox3.core/mount-components"}
                     :compiler
                     {:main "lumbox3.app"
                      :asset-path "/js/out"
                      :output-to "target/cljsbuild/public/js/app.js"
                      :output-dir "target/cljsbuild/public/js/out"
                      :source-map true
                      :optimizations :none
                      :pretty-print true
                      :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true}
                      :preloads [day8.re-frame-10x.preload]}}}}
                  
                  
                  
                  :doo {:build "test"}
                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-server" "-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]
                  :cljsbuild
                  {:builds
                   {:test
                    {:source-paths ["src/cljc" "src/cljs" "test/cljs"]
                     :compiler
                     {:output-to "target/test.js"
                      :main "lumbox3.doo-runner"
                      :optimizations :whitespace
                      :pretty-print true}}}}
                  
                  }
   :profiles/dev {}
   :profiles/test {}})
