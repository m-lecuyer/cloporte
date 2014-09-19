(ns cloporte.producer
  (:require [cloporte.helpers :as helpers]
            [taoensso.carmine :as redis]
            [taoensso.carmine.message-queue :as redis-mq]))

;; TODO support functions as arguments?
;; TODO support func not defined
;; TODO very args a serializable in json?
(defn serialize
  "Serializes a function call into a hash-map."
  [function-call-list]
  (let [func-meta (-> function-call-list first resolve meta)]
    {:ns   (-> func-meta :ns str)                  ;; namespave of func
     :fn   (-> func-meta :name str)                ;; func fully qulified name
     :args (into [] (rest function-call-list))}))  ;; arguments

;; TODO error checking and handle/return errors
(defmacro perform-async
  "Enqueues the job on cloporte's redis queue."
  [function-call & queue]
  `(->> (quote ~function-call)
        serialize
        (helpers/enqueue (or ~queue :default))))
