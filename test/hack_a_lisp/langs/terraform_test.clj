(ns hack-a-lisp.langs.terraform-test
  (:require
    [hack-a-lisp.langs.terraform :refer :all]
    [clojure.test :refer :all]
    [clj-toolbox.test-utils :refer :all])
  (:import
    [java.lang AssertionError]))

(defntest-1 evaluate
  ; Numbers
  1 1
  ; Strings
  "hello" "\"hello\""
  ; Lists
  [] "[]"
  [1 2 3] "[ 1, 2, 3 ]"
  ; Maps
  {} "{}"
  '{a 1 b "3"} "{\n  a = 1\n  b = \"3\"\n}"
  ; Blocks
  '(:variable ["foo"] default 7 type int) "variable \"foo\" {\n  default = 7\n  type = int\n}"
  '(:terraform [] (:provider ["aws"] version "1.0.x")) "terraform {\n  provider \"aws\" {\n    version = \"1.0.x\"\n  }\n}"
  '(:foo ["bar"]) "foo \"bar\" {}"
  '(:require default 7) (thrown-with-msg? AssertionError #"should be a vector")
  ; Ternary
  '(if (not= var.x null) var.x null) "var.x != null ? var.x : null"
  ; Function call syntax
  '(map int) "map(int)"
  '(optional bool false) "optional(bool, false)"
  '(1 2 3) (thrown? Exception)
  ; Sequence
  (seq ['(:resource ["foo" "bar1"])
        '(:resource ["foo" "bar2"])]) "resource \"foo\" \"bar1\" {}\n\nresource \"foo\" \"bar2\" {}"
  ; Nonsense inputs
  (Exception. "Hello") (thrown? Exception))

(defntest-1 vars-evaluate
  '{ecr_repos ["foo" "bar"]} "ecr_repos = [ \"foo\", \"bar\" ]"
  '{foo 1 bar 2} "foo = 1\n\nbar = 2"
  (seq ['{foo 1}]) "foo = 1"
  (seq [1]) (thrown? AssertionError)
  1 (thrown-with-msg? Exception #"should only consist of a single map"))
