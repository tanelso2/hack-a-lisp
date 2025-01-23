(ns hack-a-lisp.util
  (:require
    [clj-toolbox.prelude :refer [into-map]]))

(defn remove-empty-map-entries
  [m]
  (->> m
       (filter (fn [[_ v]]
                 (cond
                   (nil? v) false
                   (coll? v) (not (empty? v))
                   :else true)))
       into-map))
