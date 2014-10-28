(ns cloporte.core
  (:require [cloporte.job :as job]
            [cloporte.queue :as queue] :reload))

;; TODO error checking and handle/return errors
(defmacro perform-async
  "Enqueues the job on cloporte's redis queue."
  [function-call & options]
  ;; save function symbol, eval args, serialize and queue
  `(queue/redis-enqueue ~options
                  (job/serialize (quote ~(first function-call)) ~@(rest function-call))))

(defn start-worker
  "Starts a multi-threaded worker"
  [& args] (apply queue/start-worker args))
