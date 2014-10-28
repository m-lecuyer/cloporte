(ns cloporte.t-core
  (:use midje.sweet)
  (:require [cloporte.core :as core]
            [cloporte.job :as job]
            [cloporte.queue :as queue]
            [cloporte.helpers.functions :as fns]
            [clojure.data.json :as json] :reload))

(defn foo ([] "default") ([arg] arg))

(facts "about the `serialize` function"
       (fact "it returns right function, namespace and arguments"
             (job/serialize 'foo "bar")
             => {:ns   "cloporte.t-core"
                 :fn   "foo"
                 :args ["bar"]}
             (job/serialize 'cloporte.helpers.functions/foo)
             => {:ns   "cloporte.helpers.functions"
                 :fn   "foo"
                 :args []}
             (job/serialize 'fns/foo "bar" "baz" 3)
             => {:ns   "cloporte.helpers.functions"
                 :fn   "foo"
                 :args ["bar" "baz" 3]}))

       ; (fact "it fails gracefully"  ;; TODO
             ; (core/marshal '(bar "foo")) => nil))

(facts "about the `perform-job` function"
       (fact "it executes the job properly with namespace and args"
             (job/perform-job {:ns   "cloporte.t-core"
                             :fn   "foo"
                             :args ["bar"]}) => "bar"
             (job/perform-job {:ns   "cloporte.helpers.functions"
                             :fn   "foo"
                             :args ["bar" 4]}) => "barbarbarbar"))

(facts "about performing a job through serialization"
       (with-redefs  ;; /!\ careful, not parallelizable
         [queue/redis-enqueue (fn [opts json] json)]  ;; to json instead of enqueue
         (fact "it works end to end with namespaces and arguments"
               (job/perform-job (core/perform-async (foo))) => "default"
               (job/perform-job (core/perform-async (cloporte.t-core/foo "bar"))) => "bar"
               (job/perform-job (core/perform-async (fns/foo "bar" 3))) => "barbarbar"
               (job/perform-job (core/perform-async (cloporte.helpers.functions/foo "bar")))
               => "arg: bar")))

(def jobs {"a" 2 "b" 3 "c" 1})

(def result (atom {}))

(defn reset-result! []
  (swap! result (fn [atm] {})))

(defn increment [job-arg]
  (swap! result
         (fn [result-map]
           (assoc result-map job-arg (inc (get result-map job-arg 0))))))

(defn do-async [work]
  (doseq [k (keys work)]
    (dotimes [n (get work k)]
      (core/perform-async (increment k)))))

(facts "about performing a job through serialization and a redis queue"
       (fact "it enqueues the jobs"
             (do (reset-result!)
                 (do-async jobs)  ;; populate queue
                 (queue/queued-messages "default")) => (reduce + (vals jobs)))
       (fact "it consumes all the jobs once"
             (let [worker (queue/start-worker 4)]
               (while (not (queue/queue-empty? "default")) (Thread/sleep 200))
               (queue/stop-worker worker)
               @result) => jobs))
