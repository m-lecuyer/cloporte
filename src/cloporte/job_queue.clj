(ns cloporte.job-queue
  (:require [clojure.data.json :as json]
            [taoensso.carmine :as redis]
            [taoensso.carmine.message-queue :as redis-mq]))

(defmacro wcar* [& body] `(redis/wcar nil ~@body))

(defn enqueue-job
  "Queues a job in Redis for it to be consumed by a worker."
  [options job]
  ;; TODO other options: {:after time}...
  ;; TODO centralize defaults
  (let [queue (or (:queue options) :default)]
    ;; here we put the job in a redis queue
    (wcar* (redis-mq/enqueue (str queue) job))))
