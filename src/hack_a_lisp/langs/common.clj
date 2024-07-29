(ns hack-a-lisp.langs.common)

(defn mk-repl
  [& {:keys [name evaluate]
      :or {name "lisp"}}]
  (letfn [(repl []
              (print (str "(" name ")? "))
              (flush)
              (let [input (read)
                    res (evaluate input)]
                (println "=>" res)
                (println)
                (recur)))]
    repl))

(defn mk-eval-file
  [& {:keys [name evaluate]
      :or {name "lisp"}}]
  (fn [f]
    (-> f
        slurp
        read-string
        evaluate)))

(defmacro deflisp
  "Declares some common functions for a language
   Current functions are:
     repl - runs a repl
     eval-file - reads a file and returns the evaluated form"
  [name & {:keys [evaluate]}]
  `(do
     (def ~'repl (mk-repl :name ~(str name) :evaluate ~evaluate))
     (def ~'eval-file (mk-eval-file :name ~(str name) :evaluate ~evaluate))))
