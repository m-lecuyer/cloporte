(ns cloporte.t-core
  (:use midje.sweet)
  (:require [cloporte.core :as core]
            [cloporte.helpers.functions :as fns]
            [clojure.data.json :as json]))

(defn foo [arg] arg)

(facts "about the `serialize` function"
       (fact "it returns right function, namespace and arguments"
             (core/serialize '(foo "bar"))
             => {:ns   "cloporte.t-core"
                 :fn   "foo"
                 :args ["bar"]}
             (core/serialize '(cloporte.helpers.functions/foo "bar"))
             => {:ns   "cloporte.helpers.functions"
                 :fn   "foo"
                 :args ["bar"]}))
       ; (fact "it fails gracefully"  ;; TODO
             ; (core/marshal '(bar "foo")) => nil))

(facts "about the `perform-job` function"
       (fact "it executes the job properly from the right namespace"
             (core/perform-job {:ns   "cloporte.t-core"
                                :fn   "foo"
                                :args ["bar"]})
             => "bar"
             (core/perform-job {:ns   "cloporte.helpers.functions"
                                :fn   "foo"
                                :args ["bar"]})
             => "arg: bar"))

(facts "about performing a job through serialization"
       (fact "it works end to end with right namespace going through serialize"
             (core/perform-job (core/serialize '(foo "bar")))
             => "bar"
             (core/perform-job (core/serialize '(cloporte.t-core/foo "bar")))
             => "bar"
             (core/perform-job (core/serialize '(fns/foo "bar")))
             => "arg: bar"
             (core/perform-job (core/serialize '(cloporte.helpers.functions/foo "bar")))
             => "arg: bar")
       (with-redefs  ;; careful, not parallelizable
         [core/enqueue-job (fn [opts json] json)]
         (fact "it works end to end with right namespace going through
                the perform-async macro"
               (core/perform-job (core/unmarshal (core/perform-async (foo "bar"))))
               => "bar"
               (-> (core/perform-async  (cloporte.t-core/foo "bar"))
                   core/unmarshal
                   core/perform-job)
               => "bar"
               (core/perform-job (core/unmarshal (core/perform-async (fns/foo "bar"))))
               => "arg: bar"
               (-> (core/perform-async  (cloporte.helpers.functions/foo "bar"))
                   core/unmarshal
                   core/perform-job)
               => "arg: bar")))
