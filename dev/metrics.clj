(ns metrics
  (:require [aux :as a]
            [clojure.core.async :as async])
  (:import [io.prometheus.metrics.core.metrics Summary]
           [io.prometheus.metrics.exporter.httpserver HTTPServer]
           [io.prometheus.metrics.instrumentation.jvm JvmMetrics]
           [io.prometheus.metrics.model.snapshots Labels Unit]))

(defn initialize-and-return-metrics []
  (.register (JvmMetrics/builder))
  (let [fressian-version nil #_(a/get-fressian-version)
        metric-map       nil #_{:test-metric (-> (Summary/builder)
                                                 (.name "defressian_1000structs_seconds")
                                                 (.unit Unit/SECONDS)
                                                 (.quantile 0.5 0.01)
                                                 (.quantile 0.9 0.01)
                                                 (.constLabels (Labels/of (into-array ["fressian_version" fressian-version])))
                                                 (.labelNames (into-array ["struct_type" "struct_size" "list_conversion"]))
                                                 (.register))}]
    (async/thread (-> (HTTPServer/builder)
                      (.port 9000)
                      (.buildAndStart)))
    (a/info {:event :initialized-metrics})
    #_metric-map))