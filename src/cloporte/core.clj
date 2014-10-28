(ns cloporte.core
  (:require [cloporte.job :as job]
            [cloporte.queue :as queue] :reload))

(defn set-redis-config!
  "a config hash-map for the connection, from Carmine."
  [opts]
  (queue/set-redis-config! opts))

;; TODO error checking and handle/return errors
(defmacro perform-async
  "Enqueues the job on cloporte's redis queue."
  ([function-call]
   `(perform-async :default ~function-call nil))
  ([function-call failure-callback]
   `(perform-async :default ~function-call ~failure-callback))
  ([qname function-call failure-callback]
   `(job/perform-async ~qname ~function-call ~failure-callback)))

(defn start-worker
  "Starts a multi-threaded worker"
  [& args] (apply queue/start-worker args))
