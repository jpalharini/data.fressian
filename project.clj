(defproject org.clojure/data.fressian "0.2.1-SNAPSHOT"
  :description "Read/write Fressian from Clojure."
  :url "https://github.com/clojure/data.fressian"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/main/clojure"]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.fressian/fressian "0.6.8"]
                 [org.clojure/test.generative "1.1.0" :scope "test"]]
  :profiles {:fressian-dev {:source-paths      ["dev"]
                            :java-source-paths ["../fressian/src"]
                            :dependencies      [#_[org.fressian/fressian "0.6.9-SNAPSHOT"]
                                                [org.clojure/clojure "1.12.0"]
                                                [com.clojure-goes-fast/clj-memory-meter "0.3.0"]]
                            :repl-options      {:port 5555}
                            :jvm-opts          ["-Djdk.attach.allowAttachSelf"]}
             :bench        {:source-paths ["dev"]
                            :dependencies [[org.clojure/clojure "1.12.0"]
                                           [org.clojure/core.async "1.6.681"]
                                           [ch.qos.logback/logback-classic "1.5.16"]
                                           [io.prometheus/prometheus-metrics-core "1.3.5"]
                                           [io.prometheus/prometheus-metrics-instrumentation-jvm "1.3.5"]
                                           [io.prometheus/prometheus-metrics-exporter-httpserver "1.3.5"]]
                            :jvm-opts     ["-server"
                                           "-Xms8g" "-Xmx8g"
                                           "-XX:+UseZGC"]}
             :fressian-new {:dependencies [[org.fressian/fressian "0.6.9-SNAPSHOT"]]
                            :main         simple-stress-test}
             :fressian-old {:dependencies [[org.fressian/fressian "0.6.8"]]
                            :main         simple-stress-test}}

  :jvm-opts ["-Xmx2g" "-server"])