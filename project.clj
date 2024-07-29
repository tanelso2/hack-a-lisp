(defproject hack-a-lisp "0.1.0"
  :description "Hack some lisps together, exploiting Clojure's read function to great effect."
  :url "http://github.com/tanelso2/hack-a-lisp"
  :license {:name "The Unlicense"
            :url "https://unlicense.org"}
  :dependencies [[org.clojure/clojure "1.11.3"]]
  :main ^:skip-aot hack-a-lisp.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
