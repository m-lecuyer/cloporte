(ns cloporte.job-queue
  (:require [clojure.data.json :as json]
            [taoensso.carmine :as car :refer (wcar)]))

(defn enqueue-job
  "Queues a job in Redis for it to be consumed by a worker."
  [options job]
  (let [queue (or (:queue options) :default)
        job-json (json/write-str job)]  ;; TODO other options: {:after time}...
                                               ;; TODO centralize defaults
    ;; here we put the job in a redis queue
    (println "queue: " queue)
    (println "job: " job-json)
    (println "options: " options)))

;; TODO don't transform stings to keywords in args?
(defn unmarshal
  "Desirializes the job-json."
  [job-json]
  (json/read-str job-json :key-fn keyword))
