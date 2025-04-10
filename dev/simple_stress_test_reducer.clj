(ns simple-stress-test-reducer
  (:require [aux :as a]
            [clojure.core.async :as async]
            [clojure.edn :as edn]
            [metrics :as m])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]
           [org.fressian FressianReader FressianWriter]
           [org.fressian.handlers ILookup IReduceList]))

(def custom-read-handlers
  (reify ILookup
    (valAt [_ k]
      (get {"fressian/list" (reify IReduceList
                              (init [_ len]
                                (transient []))

                              (step [_ acc o]
                                (conj! acc o))

                              (complete [_ acc]
                                (persistent! acc)))}
           k))))

(defn -main [& args]
  (let [parsed-args (map edn/read-string args)
        argmap      (zipmap [:struct-type :struct-size :convert-list?] parsed-args)]
    (m/initialize-and-return-metrics)
    (a/stress-test argmap custom-read-handlers)
    (System/exit 0)))