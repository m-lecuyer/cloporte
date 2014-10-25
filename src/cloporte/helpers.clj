(ns cloporte.helpers
  (:require [cloporte.state.redis :as redis-state]
            [taoensso.carmine :as redis]
            [taoensso.carmine.message-queue :as redis-mq]))

(defn set-redis-config!
  "Sets up redis configs."
  [opts]
  (redis-state/set-config! opts))

(defmacro redis-query
  "Redis query with config state."
  [& body]
  `(redis/wcar (redis-state/config) ~@body))

(defn redis-enqueue
  "Enqueues message in queues, in configured Redis."
  [options message]
  (let [queue (or (:queue options) :default)]
    (redis-query (redis-mq/enqueue (name queue) message))))

(defmacro redis-worker
  "Starts a multi-threaded worker to consume jobs from agiven queue."
  [queue opts]
  `(redis-mq/worker (redis-state/config) ~queue ~opts))
