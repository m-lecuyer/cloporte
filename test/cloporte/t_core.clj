(ns cloporte.t-core
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [cloporte.core :as core]
            [cloporte.helpers.functions :as fns]))

(defn foo [arg] arg)

(facts "about the `marshal` function"
       (fact "it returns right function, namespace and arguments"
             (core/marshal '(foo "bar"))
             => {:ns   "cloporte.t-core"
                 :fn   "foo"
                 :args ["bar"]}
             (core/marshal '(cloporte.helpers.functions/foo "bar"))
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
       (fact "it works end to end with right namespace"
             (core/perform-job (core/marshal '(foo "bar")))
             => "bar"
             (core/perform-job (core/marshal '(cloporte.t-core/foo "bar")))
             => "bar"
             (core/perform-job (core/marshal '(fns/foo "bar")))
             => "arg: bar"
             (core/perform-job (core/marshal '(cloporte.helpers.functions/foo "bar")))
             => "arg: bar"))
