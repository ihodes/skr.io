(defproject skrio "0.1.0"
  :description "Simple text storage API"
  :url "http://www.skr.io"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring-json-response "0.2.0"]
                 [com.novemberain/monger "1.7.0-beta1"]
                 [ring.middleware.logger "0.4.0"]
                 [environ "0.4.0"]
                 [ring/ring-jetty-adapter "1.1.6"]]
  :plugins [[lein-ring "0.8.8"]
            [lein-environ "0.4.0"]]
  :uberjar-name "skrio-standalone.jar"
  :ring {:handler skrio.core/app}
  :env {:mongodb-url "mongodb://localhost/skrio"}
  :min-lein-version "2.3.3")
