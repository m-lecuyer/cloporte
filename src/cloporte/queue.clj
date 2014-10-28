(ns cloporte.queue
  (:require [cloporte.state.redis :as redis-state]
            [taoensso.carmine :as redis]
            [taoensso.carmine.message-queue :as redis-mq]
            [cloporte.job :as job]))

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
  [qname message]
    (redis-query (redis-mq/enqueue (name qname) message)))

(defmacro redis-worker
  "Starts a multi-threaded worker to consume jobs from agiven queue."
  [queue opts]
  `(redis-mq/worker (redis-state/config) ~queue ~opts))

;; remember it to close it
;; TODO keep it somewhere to close all ?
(defn start-worker
  "Starts a worker with n threads, on a given queue."
  ([nthreads] (start-worker nthreads :default))
  ([nthreads qname]
   (redis-mq/worker nil (name (or qname :default))
                    {:handler (fn [{:keys [message attempt]}]
                                  (job/perform-job message)
                                  {:status :success})
                     :nthreads (Integer. nthreads)})))

(defn stop-worker [worker] (redis-mq/stop worker))

(defn queue-status [qname] (redis-mq/queue-status nil qname))

(defn queue-empty? [qname]
  (empty? (:messages (queue-status qname))))

(defn queued-messages [qname]
  (count (:messages (queue-status qname))))
