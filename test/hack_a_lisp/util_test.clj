(ns hack-a-lisp.util-test
  (:require
    [clojure.test :refer :all]
    [clj-toolbox.test-utils :refer :all]
    [hack-a-lisp.util :refer :all]))

(defntest-1 remove-empty-map-entries
  {:a 1 :b 2} {:a 1 :b 2}
  {:a 1 :b nil} {:a 1}
  {:a 1 :b []} {:a 1}
  {:a 1 :b {}} {:a 1}
  {:a 1 :b [1 2 3]} {:a 1 :b [1 2 3]}
  {:a [] :b [1 2 3]} {:b [1 2 3]})
