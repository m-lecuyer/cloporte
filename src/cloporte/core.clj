(ns cloporte.core
  (:require [cloporte.serializer :as s]
            [cloporte.job-queue :as q]))

;; TODO error checking and handle/return errors
(defmacro perform-async
  "Enqueues the job on cloporte's redis queue."
  [function-call & options]
  `(->> (quote ~function-call)
        s/serialize
        (q/enqueue-job ~options)))
