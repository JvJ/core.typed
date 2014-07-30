(ns ^:skip-wiki clojure.core.typed.object-rep
  (:refer-clojure :exclude [defrecord])
  (:require [clojure.core.typed.impl-protocols :as p]
            [clojure.core.typed.type-rep :as r]
            [clojure.core.typed.path-rep :as pr]
            [clojure.core.typed.filter-rep :as fr]
            [clojure.core.typed.utils :as u]
            [clojure.core.typed.indirect-utils :as ind-u]
            [clojure.core.typed.indirect-ops :as ind]
            [clojure.core.typed :as t])
  (:import (clojure.lang Seqable)))

(t/tc-ignore
(alter-meta! *ns* assoc :skip-wiki true)
  )

(t/defalias RObject
  "An object with a path."
  p/IRObject)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Objects

(t/ann ^:no-check RObject? (t/Pred p/IRObject))
(defn RObject? [a]
  (instance? clojure.core.typed.impl_protocols.IRObject a))

(t/ann-record EmptyObject [])
(u/def-object EmptyObject []
  "No interesting information about this path/object"
  []
  :methods
  [p/IRObject])

(t/ann -empty EmptyObject)
(def -empty (EmptyObject-maker))

(defn -empty-fn []
  -empty)

(ind-u/add-indirection ind/-empty-fn -empty-fn)

(t/ann-record Path [path :- (Seqable p/IRObject)
                    id :- fr/NameRef])
(u/def-object Path [path id]
  "A path to a variable. Paths grow to the right, with leftmost
  pathelem being applied first (think of -> threading operator)."
  [(or (and (seq path)
            (sequential? path))
       (nil? path))
   (every? pr/PathElem? path)
   (fr/name-ref? id)]
  :methods
  [p/IRObject])

(defn -path [path id]
  (Path-maker path id))

(t/ann-record NoObject [])
(u/def-object NoObject []
  "Represents no info about the object of this expression
  should only be used for parsing type annotations and expected types"
  []
  :methods
  [p/IRObject])
