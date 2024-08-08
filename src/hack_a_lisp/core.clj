(ns hack-a-lisp.core
  (:require
    [clojure.java.io :as io]
    [babashka.cli :as cli]
    [clj-toolbox.prelude :refer [into-map]]
    [clj-toolbox.files :as files :refer [dir-exists? file-exists?]]
    [hack-a-lisp.langs.nix :as nix])
  (:gen-class))

(defn show-help
  [spec]
  (let [order (-> spec
                  :spec
                  keys
                  vec)
        opts (cli/format-opts (merge spec {:order order}))]
    (println opts)))

; Global flags and options would go here
(def global-spec {})

(def lang-configs
  {:nix {:outext "nix"
         :repl nix/repl
         :evaluate nix/evaluate
         :ext "nixlsp"}})

(def lang-exts
  (into-map (comp keyword :ext)
            (vals lang-configs)))

(defn repl
  [lang]
  (if (contains? lang-configs lang)
    (let [r (get-in lang-configs [lang :repl])]
      (r))))

(defn convert
  [f]
  (assert (file-exists? f) (str f " does not exist or is not a file"))
  (let [ext (files/path->ext f)
        ext-key (keyword ext)]
    (if (contains? lang-exts ext-key)
      (let [{:keys [outext repl evaluate]} (get lang-exts ext-key)
            new-filename (str (files/strip-ext f) \. outext)
            result (evaluate (read-string (slurp f)))]
        ;; TODO: Check if file exists
        (println "INFO - Converting" f)
        (println "INFO - Writing to" new-filename)
        (spit new-filename result)))))
      ;; (println "DEBUG - Can't handle file extension" ext))))

(defn convert-tree
  [dir]
  ;; (println "DEBUG - Visiting " dir)
  (cond
    (file-exists? dir) (convert dir)
    (dir-exists? dir) (doseq [x (.list (io/file dir))]
                             (convert-tree (files/path-join dir x)))
    :else (println "Don't know how to handle" dir)))

(defn repl-wrapper
  [{:keys [opts]}]
  (when (not (contains? opts :lang))
    (println "Usage: <> repl <lang>")
    (System/exit 1))
  (repl (keyword (:lang opts))))

(defn convert-wrapper
  [{:keys [opts]}]
  (when (not (contains? opts :file))
    (println "Usage: <> convert <file>")
    (System/exit 1))
  (convert (:file opts)))

(defn convert-tree-wrapper
  [{:keys [opts]}]
  (when (not (contains? opts :dir))
    (println "Usage: <> convert-tree <dir>")
    (System/exit 1))
  (convert-tree (:dir opts)))

(defn help
  [opts]
  (println "Commands:
  repl <lang>          opens a repl for the specified language
  convert <file>       converts a file based on the file's extension
  convert-tree <dir>   walks all the files and directories under dir, converting all eligible files
  ")
  (show-help {:spec global-spec}))

(def table
  [{:cmds [] :spec global-spec}
   {:cmds ["repl"] :fn repl-wrapper :args->opts [:lang]}
   {:cmds ["convert"] :fn convert-wrapper :args->opts [:file]}
   {:cmds ["convert-tree"] :fn convert-tree-wrapper :args->opts [:dir]}
   {:cmds [] :fn help}])

(defn -main
  [& args]
  (cli/dispatch table args))
