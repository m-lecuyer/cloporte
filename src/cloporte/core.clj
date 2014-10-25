(ns cloporte.core
  (:require [cloporte.serializer :as s]
            [cloporte.helpers :as helpers] :reload))

;; TODO error checking and handle/return errors
(defmacro perform-async
  "Enqueues the job on cloporte's redis queue."
  [function-call & options]
  ;; save function symbal, eval args, serialize and queue
  `(helpers/redis-enqueue ~options
                  (s/serialize (quote ~(first function-call)) ~@(rest function-call))))
