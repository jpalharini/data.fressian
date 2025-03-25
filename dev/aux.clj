(ns aux
  (:require [clojure.string :as str])
  (:import [org.slf4j LoggerFactory]))

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