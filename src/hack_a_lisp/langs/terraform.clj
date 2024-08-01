(ns hack-a-lisp.langs.terraform
  (:require
    [clojure.string :as str]
    [clojure.java.shell :refer [sh]]
    [hack-a-lisp.langs.common :refer [deflisp 
                                      indent-lines 
                                      two-space-indent]]))


(def ^:private indent two-space-indent)
(def ^:private nl-indent (str "\n" indent))

(declare
  make-map
  make-list
  make-block
  make-ternary
  make-binop
  make-func-call)

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
        pr-str)
    (list? term)
    (let [[hd & tl] term]
      (cond
        (symbol? hd)
        (condp = hd
          'if (apply make-ternary tl)
          'not= (make-binop "!=" tl)
          (make-func-call hd tl))
          ;; (throw (Exception. (pr-str "ERROR - unrecognized function call " hd))))
        (keyword? hd)
        (make-block hd tl)
        :else (throw (Exception. (str "Don't know how to handle list with head of " hd)))))
    (map? term)
    (make-map term)
    (vector? term)
    (make-list term)
    :else
    (throw (Exception. (str "ERROR - unrecognized term " term)))))

(defn- make-func-call
  [f args]
  (let [ea (map evaluate args)
        args-str (str/join ", " ea)]
    (str f "(" args-str ")")))

(defn- make-ternary
  [pred t f]
  (let [ep (evaluate pred)
        et (evaluate t)
        ef (evaluate f)]
    (str ep " ? " et " : " ef)))

(defn- make-binop
  [op args]
  (assert (= 2 (count args)) (str op "can only be applied to 2 args"))
  (let [el (evaluate (first args))
        er (evaluate (second args))]
    (str el " " op " " er)))

(defn- eval-indent
  [term]
  (-> term
      evaluate
      indent-lines))

(defn make-map
  [m]
  (->>
    (for [[k v] m]
      (let [ek (eval-indent k)
            ev (eval-indent v)]
        (str indent ek " = " ev)))
    (str/join "\n")
    (#(str "{\n" % "\n}"))))

(defn make-list
  [l]
  (let [evs (for [v l]
              (eval-indent v))]
    (str "[ " indent (str/join ", " evs) " ]")))

(defn make-multiline-list
  [l]
  (let [evs (for [v l]
              (eval-indent v))]
    (str "[\n" indent (str/join ",\n" evs) "\n]")))

(defn make-block
  [hd tl]
  (let [attrs (last tl)
        tags (drop-last tl)]
    (assert (keyword? hd) "Blocks need to start with a keyword")
    (assert (every? string? tags)
            "Everything but the last element should be a string")
    (assert (map? attrs) "Last element should be a map")
    (let [m (evaluate attrs)
          name (evaluate hd)
          ts (map evaluate tags)]
      (str name " " (str/join " "  ts) " " m))))

(defn- tf-fmt
  [s]
  (sh "terraform" "fmt" s))

(defn tf-fmt-obj
  [o]
  ; TODO: Use an actual temp file
  (let [f "/tmp/obj.tf"]
    (spit f o)
    (tf-fmt f)
    (slurp f)))

(defn fmt-evaluate
  [term]
  (tf-fmt-obj (evaluate term)))

(deflisp tf :evaluate fmt-evaluate)
