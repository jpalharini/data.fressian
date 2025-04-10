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

(defn stress-test [{:keys [struct-type struct-size convert-list?] :as argmap}
                   read-handlers]
  (let [gen-struct-fn (case (str struct-type)
                        "map" gen-map
                        "vec" gen-vec)
        fr-handlers   (when convert-list? read-handlers)]
    (info (merge {:event :starting-stress-test} argmap))
    (let [work-chan    (async/chan 1)
          timeout-chan (async/timeout (* 1000 60 20))
          ; avoid submillisecond measurement
          struct-count (* 1000 (if (< struct-size 9)
                                 100 1))]
      (loop [runs      0
             proc-time 0]
        (let [structs (repeatedly struct-count #(gen-struct-fn struct-size))
              ; put generated structs fressianed into the work channel as a single coll
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