(ns hack-a-lisp.langs.nix-test
  (:require
    [hack-a-lisp.langs.nix :refer :all]
    [clojure.test :refer :all]
    [clj-toolbox.test-utils :refer :all]))

(defntest-1 evaluate
  ; Numbers
  1 1
  ; Strings
  "abc" "\"abc\""
  ; Vectors
  [] "[]"
  [1 2 3] "[\n  1\n  2\n  3\n]"
  ; Attr sets
  '{a 1 b "2"} "{\n  a = 1;\n  b = \"2\";\n}"
  '{b 2 a 1} "{\n  a = 1;\n  b = 2;\n}" ; should sort keys
  ; Special forms
  '(1 2 3) (thrown? Exception)
  '(foo 1 2) (thrown? Exception)
  '(with pkgs [git vim]) "with pkgs; [\n  git\n  vim\n]"
  ; Functions
  '(fn [#{pkgs ...}] {globalPackages (with pkgs [git vim])})
  "{ pkgs, ... }:\n{\n  globalPackages = with pkgs; [\n    git\n    vim\n  ];\n}"
  ; Sequences
  (seq ['{r 2 d 2}]) "{\n  d = 2;\n  r = 2;\n}")

