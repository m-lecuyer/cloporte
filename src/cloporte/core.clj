(ns cloporte.core
  (:require [clojure.data.json :as json]))

(defn enqueue-job
  "Queues a job in Redis for it to be consumed by a worker."
  [options job-json]
  (let [queue (or (:queue options) :default)]  ;; TODO other options: after...
    ;; here we put the job in a redis queue
    (println "queue: " queue)
    (println "job: " job-json)
    (println "options: " options)))

;; TODO support functions as arguments?
(defn serialize
  "Serializes a function call into a hash-map."
  [function-call-list]
  (let [fully-qualified-name (-> function-call-list first resolve)]
    {:ns   (-> fully-qualified-name meta :ns str)  ;; namespave of func
     :fn   (str fully-qualified-name)              ;; func fully qulified name
     :args (into [] (rest function-call-list))}))  ;; arguments

;; TODO error checking and handle/return errors
(defmacro perform-async
  "Enqueues the job on cloporte's redis queue."
  [function-call & options]
  `(->> (quote ~function-call)
        serialize
        json/write-str
        (enqueue-job ~options)))
