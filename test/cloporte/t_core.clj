(ns cloporte.t-core
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [cloporte.core :refer :all]))

(facts "`foo` is bar"
      (+ 1 1) => 2)
