(ns ^:skip-wiki clojure.core.typed.lex-env
  (:require [clojure.core.typed.utils :as u]
            [clojure.core.typed.contract-utils :as con]
            [clojure.core.typed.type-rep :as r]
            [clojure.core.typed.filter-rep :as fr]))

(alter-meta! *ns* assoc :skip-wiki true)

;(ann (predicate (APersistentMap Symbol Any)))
(def lex-env? (con/hash-c? (every-pred symbol? (complement namespace)) r/Type?))

(u/defrecord PropEnv [l props]
  "A lexical environment l, props is a list of known propositions"
  [(lex-env? l)
   (set? props)
   (every? fr/Filter? props)])

(defn -PropEnv [l props]
  (->PropEnv l (if (set? props)
                 props
                 (into #{} props))))

(defonce ^:dynamic *lexical-env* (-PropEnv {} #{}))
(set-validator! #'*lexical-env* (fn [a]
                                  (or (PropEnv? a)
                                      ;work around for recompilation issues with AOT
                                      (= "clojure.core.typed.lex_env.PropEnv"
                                         (.getName (class a))))))

(defn lookup-local [sym]
  (-> *lexical-env* :l sym))

(defn merge-locals [env new]
  (-> env
    (update-in [:l] #(merge % new))))

(defmacro with-locals [locals & body]
  `(binding [*lexical-env* (merge-locals *lexical-env* ~locals)]
     ~@body))
