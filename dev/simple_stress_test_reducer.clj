(ns simple-stress-test-reducer
  (:require [aux :as a]
            [clojure.edn :as edn])
  (:import [org.fressian.handlers ILookup IReduceList])
  (:gen-class))

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
    (a/stress-test argmap custom-read-handlers)
    (System/exit 0)))