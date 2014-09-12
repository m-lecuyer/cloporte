(ns cloporte.t-core
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [cloporte.core :as core]))

(defn foo [arg] nil)

(facts "about `serialize`"
       (fact "returns right function and arguments"
             (core/serialize '(foo "bar")) => {:ns   "cloporte.t-core"
                                               :fn   "#'cloporte.t-core/foo"
                                               :args ["bar"]})
       (fact "... when the function is not defined ???"  ;; TODO
             (core/serialize '(bar "foo")) => nil))
