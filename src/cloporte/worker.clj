(ns cloporte.worker
  (:require [taoensso.carmine.message-queue :as redis-mq]
            [cloporte.serializer :as s]))

;; remember it to close it
;; TODO keep it somewhere to close all ?
(defn start-worker
  "Starts a worker with n threads, on a given queue."
  ([nthreads] (start-worker nthreads :default))
  ([nthreads queue]
   (redis-mq/worker nil (str (or queue :default))
                    {:handler (fn [{:keys [message attempt]}]
                                  (s/perform-job message)
                                  {:status :success})
                     :nthreads (Integer. nthreads)})))

(defn stop-worker [worker] (redis-mq/stop worker))

(defn queue-status [qname] (redis-mq/queue-status nil qname))

(defn queue-empty? [qname]
  (empty? (:messages (queue-status qname))))

(defn queued-messages [qname]
  (count (:messages (queue-status qname))))

