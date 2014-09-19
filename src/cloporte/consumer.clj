(ns cloporte.consumer
  (:require [cloporte.helpers :as helpers]
            [taoensso.carmine :as redis]
            [taoensso.carmine.message-queue :as redis-mq]))

(defn perform-job
  "Runs the function and args in the job."
  [job]
  ;; TODO check the namespace is already required?
  (require (symbol (:ns job)))
  (apply (resolve (symbol (:ns job) (:fn job)))
         (into () (:args job))))

(defn start-worker
  "Starts a multi-threaded worker to consume jobs from agiven queue."
  ([] (start-worker :default 1))
  ([nthreads] (start-worker :default nthreads))
  ([queue nthreads]
   (helpers/redis-worker {:handler  (fn  [{:keys  [message attempt]}]
                                      (perform-job message)
                                      {:status :success})})))
