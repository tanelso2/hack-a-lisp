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
        (let [[tags & rs] tl]
          (make-block hd tags rs))
        :else (throw (Exception. (str "Don't know how to handle list with head of " hd)))))
    (map? term)
    (make-map term)
    (vector? term)
    (make-list term)
    (seq? term)
    (str/join "\n\n" (map evaluate term))
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
  (if (empty? m)
    "{}"
    (->>
      (for [[k v] m]
        (let [ek (eval-indent k)
              ev (eval-indent v)]
          (str indent ek " = " ev)))
      (str/join "\n")
      (#(str "{\n" % "\n}")))))

(defn make-vars-map
  [m]
  (->>
    (for [[k v] m]
      (let [ek (eval-indent k)
            ev (eval-indent v)]
        (str ek " = " ev)))
    (str/join "\n\n")))

(defn make-list
  [l]
  (if (empty? l)
    "[]"
    (let [evs (for [v l]
                (evaluate v))]
      (str "[ " (str/join ", " evs) " ]"))))

(defn make-multiline-list
  [l]
  (let [evs (for [v l]
              (eval-indent v))]
    (str "[\n" indent (str/join ",\n" evs) "\n]")))

(defn make-block-body
  [attrs]
  (letfn
   [(f [attrs]
      (if (empty? attrs)
        nil
        (let [[hd & tl] attrs]
          (if (list? hd)
            (let [eh (evaluate hd)
                  et (f tl)]
              (cons eh et))
            (let [eh (evaluate hd)
                  [v & rs] tl
                  ev (evaluate v)
                  ers (f rs)]
              (cons (str eh " = " ev)
                    ers))))))]
   (str/join "\n" (f attrs))))

(defn make-block
  [hd tags tl]
  (assert (keyword? hd) "Blocks need to start with a keyword")
  (assert (vector? tags) "Tags should be a vector")
  (assert (every? string? tags) "All tags should be strings")
  (let [name (evaluate hd)
        ets (map evaluate tags)
        body (make-block-body tl)
        tags-str (if (empty? ets)
                   ""
                   (str (str/join " " ets) \space))
        body-str (if (empty? body)
                    "{}"
                    (str "{\n" indent (indent-lines body) "\n}"))]
      (str name " " tags-str body-str)))

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

(defn vars-evaluate
  [term]
  (cond
    (map? term) (make-vars-map term)
    (seq? term) (do
                  (assert (= 1 (count term)) "tfvlsp files should only consist of a single map")
                  (assert (map? (first term)) "tfvlsp files should only consist of a single map")
                  (-> term
                      first
                      make-vars-map
                      tf-fmt-obj))
    :else (throw (Exception. "tfvlsp files should only consist of a single map"))))

(deflisp tf :evaluate fmt-evaluate)
