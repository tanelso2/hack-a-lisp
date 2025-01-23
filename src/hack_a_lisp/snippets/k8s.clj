(ns hack-a-lisp.snippets.k8s
  (:require
    [clj-toolbox.prelude :refer :all]
    [hack-a-lisp.util :refer [remove-empty-map-entries]]))

(defn pod
  [& {:keys [name namespace containers]}]
  (remove-empty-map-entries
    {:apiVersion :v1
     :kind :Pod
     :metadata {:name name :namespace namespace}
     :spec {:containers containers}}))

(defn container
  [& {:keys [name image command args]}]
  (remove-empty-map-entries
    {:name name
     :image image
     :command command
     :args args}))

(defn long-running-pod
  [name namespace image]
  (pod
    :name name
    :namespace namespace
    :containers [(container :name name
                            :image image
                            :command ["/bin/sh"]
                            :args ["-c" "while true; do sleep 10; done"])]))
