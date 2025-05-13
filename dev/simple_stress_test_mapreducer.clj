(ns simple-stress-test-mapreducer
  (:require [aux :as a]
            [clojure.edn :as edn])
  (:gen-class))

(defn -main [& args]
  (let [parsed-args (map edn/read-string args)
        argmap      (zipmap [:struct-type :struct-size :convert-list?] parsed-args)]
    (a/stress-test argmap nil)
    (System/exit 0)))