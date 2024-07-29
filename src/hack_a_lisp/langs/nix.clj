(ns hack-a-lisp.langs.nix
  (:require
    [clojure.string :as str]
    [hack-a-lisp.langs.common :refer :all]))

(def ^:private indent "  ")
(def ^:private nl-indent (str "\n" indent));)

(declare make-dict make-function make-list make-params make-set make-with)

(defn evaluate
  [term]
  (cond
    (or (symbol? term)
        (number? term)
        (boolean? term))
    term
    (keyword? term)
    (-> term
        str
        (str/replace-first ":" ""))
    (string? term)
    (-> term
        (str/trim)
        pr-str) ; TODO: Use nix's multiline string format if calls for it
    (list? term)
    (let [[hd & tl] term]
      (cond
        (symbol? hd)
        (condp = hd
          'fn (make-function tl)
          'with (make-with tl)
          (throw (Exception. (pr-str "ERROR - unrecognized special form " hd))))
        :else (throw (Exception. "Only know how to accept symbols at the head"))))
    (map? term)
    (make-dict term)
    (set? term)
    (make-set term)
    (vector? term)
    (make-list term)
    :else
    (throw (Exception. (pr-str "ERROR - unrecognized term " term)))))

(defn- indent-lines
  [s]
  (str/replace s "\n" nl-indent))

(defn- eval-indent
  [term]
  (-> term
      evaluate
      indent-lines))

(defn- make-list
  [l]
  (let [evs (for [v l]
              (eval-indent v))]
    (str "[\n" indent (str/join nl-indent evs) "\n]")))

(defn- make-function
  [[params body]]
  (let [params-str (make-params params)
        body-str (evaluate body)]
    (str params-str ":\n" body-str)))

(defn- make-with
  [[p expr]]
  (str "with " (evaluate p) "; " (evaluate expr)))

; TODO: What are these called in Nix?
(defn- make-dict
  [m]
  (->>
    (for [[k v] m]
      (let [ek (eval-indent k)
            ev (eval-indent v)]
        (str indent ek " = " ev ";")))
    (sort)
    (str/join "\n")
    (#(str "{\n" % "\n}"))))

(defn- make-set
  [s]
  (let [evs (for [v s]
              (eval-indent v))]
    (str
      "{ "
      (str/join ", " evs)
      " }")))

(defn- make-params
  [params]
  (cond
    (vector? params)
    (if (= 1 (count params))
      (evaluate (first params))
      (throw (Exception. "Only handle 1-arity functions")))
    :else (throw (Exception. (str "Don't know how to handle " params)))))

(deflisp nix :evaluate evaluate)
