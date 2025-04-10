(ns simple-stress-test
  (:require [aux :as a]
            [clojure.core.async :as async]
            [clojure.edn :as edn]
            [metrics :as m])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]
           [org.fressian FressianReader FressianWriter]
           [org.fressian.handlers ConvertList ILookup]))

(def custom-read-handlers
  (reify ILookup
    (valAt [_ k]
      (get {"fressian/list"
            (reify ConvertList
              (convertList [_ items]
                (vec items)))}
           k))))

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

(defn stress-test [{:keys [struct-type struct-size convert-list?] :as argmap}]
  (let [gen-struct-fn (case (str struct-type)
                        "map" gen-map
                        "vec" gen-vec)
        fr-handlers   (when convert-list? custom-read-handlers)]
    (a/info (merge {:event :starting-stress-test} argmap))
    (let [work-chan    (async/chan 1)
          timeout-chan (async/timeout (* 1000 60 20))]
      (loop [runs      0
             proc-time 0]
        (let [structs (repeatedly 1000 #(gen-struct-fn struct-size))
              ; put generated structs fressianed into the work channel as a single coll
              _       (async/put! work-chan (into [] (for [st structs] (fressian st))))
              [val port] (async/alts!! [work-chan timeout-chan])]
          (cond
            (= port timeout-chan)
            (a/info (merge {:event :stress-test-complete :runs runs :processing-time proc-time} argmap))

            (= port work-chan)
            ; measure time to read all structs in work chan
            (let [dur (measure-duration (doseq [fs val]
                                          (defressian fs fr-handlers)))]
              (recur (inc runs) (+ proc-time dur)))))))))

(defn -main [& args]
  (let [parsed-args (map edn/read-string args)
        argmap      (zipmap [:struct-type :struct-size :convert-list?] parsed-args)]
    (m/initialize-and-return-metrics)
    (stress-test argmap)
    (System/exit 0)))