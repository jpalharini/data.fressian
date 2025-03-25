(ns stress-test
  (:require [aux :as a]
            [clojure.core.async :as async]
            [clojure.data.fressian :as fr]
            [clojure.edn :as edn]
            [metrics :as m])
  (:import [io.prometheus.metrics.core.datapoints TimerApi]
           [org.fressian.handlers ConvertList]))

(def read-handlers
  {"fressian/list"
   (reify ConvertList
     (convertList [_ items]
       (vec items)))})

(defn gen-vec [size]
  (into [] (for [_ (range size)]
             (random-uuid))))

(defn gen-map [size]
  (into {} (for [_ (range size)]
             {(random-uuid) (random-uuid)})))

(defmacro with-duration [metric & body]
  `(let [t# (.startTimer ~metric)
         st# (System/currentTimeMillis)]
     (try
       ~@body
       (- (System/currentTimeMillis) st#)
       (finally
         (.close t#)))))

(defn stress-test [{:keys [struct-type struct-size convert-list?] :as argmap}]
  (let [gen-struct-fn (case struct-type
                        'map gen-map
                        'vec gen-vec)
        metric        ^TimerApi (-> (:test-metric (m/initialize-and-return-metrics))
                                    (.labelValues (into-array String (map str [struct-type struct-size
                                                                               (if convert-list?
                                                                                 "enabled" "disabled")]))))
        ; pass custom ConvertList or fallback to defualt read handlers in data.fressian
        fr-handlers   (if convert-list?
                        (-> fr/clojure-read-handlers (merge read-handlers) (fr/associative-lookup))
                        (fr/associative-lookup fr/clojure-read-handlers))]
    ; We need to wait for Prometheus to pick up monitoring
    (Thread/sleep ^long (* 1000 60 1))
    (a/info (merge {:event :starting-stress-test} argmap))
    (let [work-chan    (async/chan 1)
          timeout-chan (async/timeout (* 1000 60 15))]
      (loop [runs      0
             proc-time 0]
        (let [structs (repeatedly 1000 #(gen-struct-fn struct-size))
              ; put generated structs fressianed into the work channel as a single coll
              _       (async/put! work-chan (for [st structs] (fr/write st)))
              [val port] (async/alts!! [work-chan timeout-chan])]
          (cond
            (= port timeout-chan)
            (a/info (merge {:event :stress-test-complete :runs runs :processing-time proc-time} argmap))

            (= port work-chan)
            ; measure time to read all structs in work chan
            (let [dur (with-duration metric (doseq [fs val]
                                              (fr/read fs :handlers fr-handlers)))]
              (recur (inc runs) (+ proc-time dur)))))))))

(defn -main [& args]
  (let [parsed-args (map edn/read-string args)
        argmap      (zipmap [:struct-type :struct-size :convert-list?] parsed-args)]
    (stress-test argmap)
    (System/exit 0)))