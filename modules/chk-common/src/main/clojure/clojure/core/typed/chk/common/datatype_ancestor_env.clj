(ns ^:skip-wiki clojure.core.typed.chk.common.datatype-ancestor-env
  (:require [clojure.core.typed.chk.common.utils :as u]
            [clojure.core.typed.chk.common.type-rep :as r]
            [clojure.core.typed.chk.common.type-ctors :as c]
            [clojure.core.typed.chk.common.subst :as subst]
            [clojure.core.typed :as t :refer [when-let-fail inst]]
            [clojure.set :as set])
  (:import (clojure.lang Symbol)
           (clojure.core.typed.chk.common.type_rep DataType)))

(t/tc-ignore
(alter-meta! *ns* assoc :skip-wiki true)
  )

(t/typed-deps clojure.core.typed.chk.common.type-ctors
              clojure.core.typed.chk.common.subst)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Type Aliases

(t/def-alias DTAncestorEnv
  "Environment mapping datatype names to sets of ancestor types."
  (t/Map Symbol (t/Set r/ScopedType)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Predicates

(def ^:no-check ^{:ann '[Any -> Any]}
  dt-ancestor-env? (u/hash-c? symbol? (u/set-c? (some-fn r/Scope? r/Type?))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Implementation specific global state

(t/ann CLJ-DT-ANCESTOR-ENV (t/Atom1 DTAncestorEnv))
(defonce ^:private CLJ-DT-ANCESTOR-ENV ((inst atom DTAncestorEnv) {} :validator dt-ancestor-env?))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Implementation agnostic state

(t/ann ^:no-check *current-dt-ancestors* (U nil (t/Atom1 DTAncestorEnv)))
(defonce ^:dynamic 
  *current-dt-ancestors* nil)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers

(defn ^:private ^{:ann '[-> Any]}
  assert-dt-ancestors []
  (assert *current-dt-ancestors* "No datatype ancestor environment bound"))

(defn ^:private ^{:ann '[DataType (U nil (t/Seqable r/Type)) -> (t/Set r/Type)]}
  inst-ancestors
  "Given a datatype, return its instantiated ancestors"
  [{poly :poly? :as dt} anctrs]
  {:pre [(r/DataType? dt)]
   :post [((u/set-c? r/Type?) %)]}
  (set (t/for> :- r/Type
         [u :- r/Type, anctrs]
         (c/inst-and-subst u poly))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Interface

(defn ^:no-check ^{:ann '[DataType -> (t/Set r/Type)]}
  get-datatype-ancestors 
  "Returns the set of overriden ancestors of the given DataType."
  [{:keys [poly? the-class] :as dt}]
  {:pre [(r/DataType? dt)]}
  (assert-dt-ancestors)
  (when-let-fail [a *current-dt-ancestors*]
    (inst-ancestors dt (@a the-class))))

(defn ^:no-check ^{:ann '[Symbol (t/Set r/Type) -> nil]}
  add-datatype-ancestors 
  "Add a set of ancestor overrides for the datatype named sym."
  [sym tset]
  (assert-dt-ancestors)
  (when-let-fail [a *current-dt-ancestors*]
    (swap! a update-in [sym] #(set/union (or % #{}) tset)))
  nil)

(defn ^:no-check ^{:ann '[DTAncestorEnv -> nil]}
  reset-datatype-ancestors! 
  "Reset the current ancestor map."
  [aenv]
  (assert-dt-ancestors)
  (when-let-fail [a *current-dt-ancestors*]
    (reset! a aenv))
  nil)
