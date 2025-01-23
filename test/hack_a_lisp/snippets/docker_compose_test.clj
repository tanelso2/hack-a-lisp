(ns hack-a-lisp.snippets.docker-compose-test
  (:require
    [clojure.test :refer :all]
    [clojure.pprint :refer [pprint]]
    [clj-toolbox.test-utils :refer :all]
    [hack-a-lisp.snippets.docker-compose :as dc :refer :all]))
  
(defntest service
  [:image "alpine:latest"] 
  {:image "alpine:latest"}

  [:ports [(mount 8080)] :cap_add [:net_admin]] 
  {:image "nginx:latest"
   :ports ["8080:8080"]
   :cap_add [:net_admin]})
  
(defntest mount
  ["/home/foo" "/root"] "/home/foo:/root"
  ["/root"] "/root:/root"
  [9000] "9000:9000"
  [nil "/root"] "/root:/root"
  [nil nil] nil) 
