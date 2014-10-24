(ns cloporte.t-core
  (:use midje.sweet)
  (:require [cloporte.core :as core]
            [cloporte.worker :as worker]
            [cloporte.serializer :as s]
            [cloporte.job-queue :as q]
            [cloporte.helpers.functions :as fns]
            [clojure.data.json :as json] :reload))

(defn foo [arg] arg)

(facts "about the `serialize` function"
       (fact "it returns right function, namespace and arguments"
             (s/serialize '(foo "bar"))
             => {:ns   "cloporte.t-core"
                 :fn   "foo"
                 :args ["bar"]}
             (s/serialize '(fns/foo "bar"))
             => {:ns   "cloporte.helpers.functions"
                 :fn   "foo"
                 :args ["bar"]}))
       ; (fact "it fails gracefully"  ;; TODO
             ; (core/marshal '(bar "foo")) => nil))

(facts "about the `perform-job` function"
       (fact "it executes the job properly from the right namespace"
             (s/perform-job {:ns   "cloporte.t-core"
                             :fn   "foo"
                             :args ["bar"]}) => "bar"
             (s/perform-job {:ns   "cloporte.helpers.functions"
                             :fn   "foo"
                             :args ["bar"]}) => "arg: bar"))

(facts "about performing a job through serialization"
       (fact "it works end to end with right namespace going through serialize"
             (s/perform-job (s/serialize '(foo "bar"))) => "bar"
             (s/perform-job (s/serialize '(cloporte.t-core/foo "bar"))) => "bar"
             (s/perform-job (s/serialize '(fns/foo "bar"))) => "arg: bar"
             (s/perform-job (s/serialize '(cloporte.helpers.functions/foo "bar")))
             => "arg: bar")
       (with-redefs  ;; /!\ careful, not parallelizable
         [q/enqueue-job (fn [opts json] json)]
         (fact "it works end to end with right namespace going through
                the perform-async macro"
               (s/perform-job (core/perform-async (foo "bar"))) => "bar"
               (s/perform-job (core/perform-async (cloporte.t-core/foo "bar"))) => "bar"
               (s/perform-job (core/perform-async (fns/foo "bar"))) => "arg: bar"
               (s/perform-job (core/perform-async (cloporte.helpers.functions/foo "bar")))
               => "arg: bar")))

(def jobs {"a" 1 "b" 2 "c" 3})

(def result (atom {}))

(defn increment [job-arg]
  (swap! result
         (fn [result-map]
           (assoc result-map job-arg (inc (get result-map job-arg 0))))))

(defn do-async [work]
  (doseq [k (keys work)]
    (dotimes [n (get work k)]
      (core/perform-async (increment k)))))

(let [worker (worker/start-worker 4)]
  (println worker/queue-status)
  (facts "about performing a job through serialization and a redis queue"
         (fact ""
               (do (do-async jobs)  ;; populate queue
                   (while (not (worker/queue-empty? ":default")) (Thread/sleep 200))
                   @result) => jobs))
  (worker/stop-worker worker))
