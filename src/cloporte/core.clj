(ns cloporte.core
  (:require [clojure.data.json :as json]))

(defn enqueue-job
  "Queues a job in Redis for it to be consumed by a worker."
  [options job-json]
  (let [queue (or (:queue options) :default)]  ;; TODO other options: {:after time}...
                                               ;; TODO centralize defaults
    ;; here we put the job in a redis queue
    (println "queue: " queue)
    (println "job: " job-json)
    (println "options: " options)))

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

;; serializes and json encodes.
(def marshal (comp json/write-str serialize))

;; TODO error checking and handle/return errors
(defmacro perform-async
  "Enqueues the job on cloporte's redis queue."
  [function-call & options]
  `(->> (quote ~function-call)
        marshal
        (enqueue-job ~options)))

;; TODO don't transform stings to keywords in args?
(defn unmarshal
  "Desirializes the job-json."
  [job-json]
  (json/read-str job-json :key-fn keyword))

(defn perform-job
  "Runs the function and args in the job."
  [job]
  ;; TODO check the namespace is already required?
  (require (symbol (:ns job)))
  (apply (resolve (symbol (:ns job) (:fn job)))
         (into () (:args job))))
