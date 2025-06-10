(ns aux
  (:require [clojure.core.async :as async]
            [clojure.string :as str])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]
           [org.fressian FressianReader FressianWriter]
           [org.slf4j LoggerFactory]))

(defn get-fressian-version []
  (let [libs (-> (System/getProperty "java.class.path")
                 (str/split #":"))
        jar  (->> libs
                  (filter #(str/includes? % "org/fressian"))
                  (first))]
    (->> jar
         (re-find #".*fressian-(.*)\.jar")
         (second))))

(defn- log [level msg]
  `(let [logger# (LoggerFactory/getLogger "fressian")]
     (. logger# ~level (str ~msg))))

(defmacro debug [msg]
  (log 'debug msg))

(defmacro info [msg]
  (log 'info msg))

(defmacro warn [msg]
  (log 'warn msg))

(defmacro error [msg]
  (log 'error msg))

(defn gen-vec [size]
  (into [] (for [_ (range size)]
             (str (random-uuid)))))

(defn gen-map [size]
  (into {} (for [_ (range size)]
             {(str (random-uuid)) (str (random-uuid))})))

(defn fressian [data]
  (with-open [baos (ByteArrayOutputStream.)
              fw   (FressianWriter. baos)]
    (.writeObject fw data)
    (.toByteArray baos)))

(defn defressian [data & [handlers]]
  (with-open [bais (ByteArrayInputStream. data)
              fr   (FressianReader. bais handlers)]
    (.readObject fr)))

(defmacro measure-duration [& body]
  `(let [st# (System/nanoTime)]
     ~@body
     (- (System/nanoTime) st#)))

(defn stress-test
  "Benchmarks Fressian reading for 20 minutes, repeatedly reading a collection of structs.
   Generates a log message at the end with total runs and processing time.

   Receives argmap and a collection of Fressian read handlers
   - struct-type can be map or vec
   - struct-size is an arbitrary positive integer - if less than 9, repeated reading will use a collection of 1,000
     structs, else it will use a collection of 100,000 to avoid sub-millisecond measurement as the JVM doesn't promise
     sub-millisecond resolution
   - convert-list? determines whether reading will use custom read handlers or defaults"
  [{:keys [struct-type struct-size convert-list?] :as argmap}
   read-handlers]
  (let [gen-struct-fn (case (str struct-type)
                        "map" gen-map
                        "vec" gen-vec)
        fr-handlers   (when convert-list? read-handlers)]
    (info (merge {:event :starting-stress-test} argmap))
    (let [work-chan    (async/chan 1)
          timeout-chan (async/timeout (* 1000 60 20))
          struct-count (* 1000 (if (< struct-size 9)
                                 100 1))]
      (loop [runs      0
             proc-time 0]
        (let [structs (repeatedly struct-count #(gen-struct-fn struct-size))
              ; put generated structs fressianed into the work channel as a single, eager vector
              _       (async/put! work-chan (into [] (for [st structs] (fressian st))))
              [val port] (async/alts!! [work-chan timeout-chan])]
          (cond
            (= port timeout-chan)
            (info (merge {:event :stress-test-complete :runs runs :processing-time proc-time} argmap))

            (= port work-chan)
            ; measure time to read all structs in work chan
            (let [dur (measure-duration (doseq [fs val]
                                          (defressian fs fr-handlers)))]
              (recur (inc runs) (+ proc-time dur)))))))))