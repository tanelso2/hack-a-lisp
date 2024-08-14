(defproject hack-a-lisp "0.2.0-SNAPSHOT"
  :description "Hack some lisps together, exploiting Clojure's read function to great effect."
  :url "http://github.com/tanelso2/hack-a-lisp"
  :license {:name "The Unlicense"
            :url "https://unlicense.org"}
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [org.babashka/cli "0.8.60"]
                 [org.clojars.tanelso2/clj-toolbox "0.7.2"]
                 [com.taoensso/telemere "1.0.0-beta16"]]
  :plugins [[lein-cloverage "1.0.13"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]]
  :main ^:skip-aot hack-a-lisp.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :aliases {"update-readme-version" ["shell" "sed" "-i" "s/\\\\[hack-a-lisp \"[0-9.]*\"\\\\]/[hack-a-lisp \"${:version}\"]/" "README.md"]}
  :release-tasks [["shell" "git" "diff" "--exit-code"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["changelog" "release"]
                  ["update-readme-version"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ;;["deploy"]
                  ["change" "version" "leiningen.release/bump-version" "patch"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
