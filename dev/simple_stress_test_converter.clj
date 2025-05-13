(ns simple-stress-test-converter
  (:require [aux :as a]
            [clojure.edn :as edn])
  (:import [clojure.lang LazilyPersistentVector]
           [org.fressian.handlers ConvertList ILookup])
  (:gen-class))

(def list-converter (reify ConvertList
                      (convertList [_ items]
                        (LazilyPersistentVector/createOwning items))))

(def custom-read-handlers
  (reify ILookup
    (valAt [_ k]
      (get {"fressian/list" list-converter} k))))

(defn -main [& args]
  (let [parsed-args (map edn/read-string args)
        argmap      (zipmap [:struct-type :struct-size :convert-list?] parsed-args)]
    (a/stress-test argmap custom-read-handlers)
    (System/exit 0)))