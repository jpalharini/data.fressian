(ns simple-stress-test-converter
  (:require [aux :as a]
            [clojure.edn :as edn]
            [metrics :as m])
  (:import [org.fressian.handlers ConvertList ILookup]))

(def custom-read-handlers
  (reify ILookup
    (valAt [_ k]
      (get {"fressian/list"
            (reify ConvertList
              (convertList [_ items]
                (vec items)))}
           k))))

(defn -main [& args]
  (let [parsed-args (map edn/read-string args)
        argmap      (zipmap [:struct-type :struct-size :convert-list?] parsed-args)]
    (m/initialize-and-return-metrics)
    (a/stress-test argmap custom-read-handlers)
    (System/exit 0)))