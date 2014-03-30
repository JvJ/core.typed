; untyped, clojure.core.typed depends on this namespace
(ns clojure.core.typed.rt.jvm.current-impl
  (:require [clojure.core.typed.chk.common.profiling :as p]
            [clojure.set :as set]))

(defonce var-env (atom {}))
(defonce alias-env (atom {}))
(defonce protocol-env (atom {}))
(defonce rclass-env (atom {}))
(defonce datatype-env (atom {}))

(defn v [vsym]
  {:pre [(symbol? vsym)
         (namespace vsym)]}
  (let [ns (find-ns (symbol (namespace vsym)))
        _ (assert ns (str "Cannot find namespace: " (namespace vsym)))
        var (ns-resolve ns (symbol (name vsym)))]
    (assert (var? var) (str "Cannot find var: " vsym))
    @var))

(defn the-var [vsym]
  {:pre [(symbol? vsym)
         (namespace vsym)]
   :post [(var? %)]}
  (let [ns (find-ns (symbol (namespace vsym)))
        _ (assert ns (str "Cannot find namespace: " (namespace vsym)))
        var (ns-resolve ns (symbol (name vsym)))]
    (assert (var? var) (str "Cannot find var: " vsym))
    var))

(def clojure ::clojure)
(def clojurescript ::clojurescript)

(def any-impl ::any-impl)

(derive clojure any-impl)
(derive clojurescript any-impl)

(defonce ^:dynamic *current-impl* nil)
(set-validator! #'*current-impl* (some-fn nil? keyword?))

(defmacro with-impl [impl & body]
  `(do (assert ((some-fn #{~impl} nil?) *current-impl*) 
               (str "Cannot overlay different core.typed implementations: " (pr-str *current-impl*)
                    ", expected " (pr-str ~impl)))
     (binding [*current-impl* ~impl]
       ~@body)))

(defmacro with-clojure-impl [& body]
  `(with-impl clojure
     (clojure.core.typed.chk.common.profiling/p :current-impl/push-thread-bindings
     (push-thread-bindings {(the-var '~'clojure.core.typed.chk.common.name-env/*current-name-env*)
                             (v '~'clojure.core.typed.chk.common.name-env/CLJ-TYPE-NAME-ENV)
                            (the-var '~'clojure.core.typed.chk.common.protocol-env/*current-protocol-env*)
                             (v '~'clojure.core.typed.chk.common.protocol-env/CLJ-PROTOCOL-ENV)
                            (the-var '~'clojure.core.typed.chk.common.ns-deps/*current-deps*)
                             (v '~'clojure.core.typed.chk.common.ns-deps/CLJ-TYPED-DEPS)
                            ; var env
                            (the-var '~'clojure.core.typed.chk.common.var-env/*current-var-annotations*)
                             (v '~'clojure.core.typed.chk.common.var-env/CLJ-VAR-ANNOTATIONS)
                            (the-var '~'clojure.core.typed.chk.common.var-env/*current-nocheck-var?*)
                             (v '~'clojure.core.typed.chk.common.var-env/CLJ-NOCHECK-VAR?)
                            (the-var '~'clojure.core.typed.chk.common.var-env/*current-used-vars*)
                             (v '~'clojure.core.typed.chk.common.var-env/CLJ-USED-VARS)
                            (the-var '~'clojure.core.typed.chk.common.var-env/*current-checked-var-defs*)
                             (v '~'clojure.core.typed.chk.common.var-env/CLJ-CHECKED-VAR-DEFS) 

                            (the-var '~'clojure.core.typed.chk.common.declared-kind-env/*current-declared-kinds*)
                             (v '~'clojure.core.typed.chk.common.declared-kind-env/CLJ-DECLARED-KIND-ENV) 
                            (the-var '~'clojure.core.typed.chk.common.datatype-env/*current-datatype-env*)
                             (v '~'clojure.core.typed.chk.common.datatype-env/CLJ-DATATYPE-ENV) 
                            (the-var '~'clojure.core.typed.chk.common.datatype-ancestor-env/*current-dt-ancestors*)
                             (v '~'clojure.core.typed.chk.common.datatype-ancestor-env/CLJ-DT-ANCESTOR-ENV) 
                            }))
     (try 
       ~@body
       (finally (clojure.core.typed.chk.common.profiling/p :current-impl/pop-thread-bindings 
                     (pop-thread-bindings))))))

(defmacro with-cljs-impl [& body]
  `(with-impl clojurescript
     (push-thread-bindings {(the-var '~'clojure.core.typed.chk.common.name-env/*current-name-env*)
                             (v '~'clojure.core.typed.chk.common.name-env/CLJS-TYPE-NAME-ENV)
                            (the-var '~'clojure.core.typed.chk.common.protocol-env/*current-protocol-env*)
                             (v '~'clojure.core.typed.chk.common.protocol-env/CLJS-PROTOCOL-ENV)
                            (the-var '~'clojure.core.typed.chk.common.ns-deps/*current-deps*)
                             (v '~'clojure.core.typed.chk.common.ns-deps/CLJS-TYPED-DEPS)
                            ; var env
                            (the-var '~'clojure.core.typed.chk.common.var-env/*current-var-annotations*)
                             (v '~'clojure.core.typed.chk.common.var-env/CLJS-VAR-ANNOTATIONS)
                            (the-var '~'clojure.core.typed.chk.common.var-env/*current-nocheck-var?*)
                             (v '~'clojure.core.typed.chk.common.var-env/CLJS-NOCHECK-VAR?)
                            (the-var '~'clojure.core.typed.chk.common.var-env/*current-used-vars*)
                             (v '~'clojure.core.typed.chk.common.var-env/CLJS-USED-VARS)
                            (the-var '~'clojure.core.typed.chk.common.var-env/*current-checked-var-defs*)
                             (v '~'clojure.core.typed.chk.common.var-env/CLJS-CHECKED-VAR-DEFS) 

                            (the-var '~'clojure.core.typed.chk.common.declared-kind-env/*current-declared-kinds*)
                             (v '~'clojure.core.typed.chk.common.declared-kind-env/CLJS-DECLARED-KIND-ENV) 
                            (the-var '~'clojure.core.typed.chk.common.datatype-env/*current-datatype-env*)
                             (v '~'clojure.core.typed.chk.common.datatype-env/CLJS-DATATYPE-ENV) 
                            })
     (try 
       ~@body
       (finally (pop-thread-bindings)))))

(defn implementation-specified? []
  (boolean *current-impl*))

(defn ensure-impl-specified []
  (assert (implementation-specified?) "No implementation specified"))

(defn current-impl []
  (ensure-impl-specified)
  *current-impl*)

(defn checking-clojure? []
  (ensure-impl-specified)
  (= clojure *current-impl*))

(defn checking-clojurescript? []
  (ensure-impl-specified)
  (= clojurescript *current-impl*))

(defn assert-clojure 
  ([] (assert-clojure nil))
  ([msg] (assert (= clojure *current-impl*) (str "Clojure implementation only"
                                                 (when (seq msg)
                                                   (str ": " msg))))))

(defn assert-cljs []
  (assert (= clojurescript *current-impl*) "Clojurescript implementation only"))

(defmacro impl-case [& {:keys [clojure cljs] :as opts}]
  (assert (= #{:clojure :cljs} (set (keys opts)))
          "Incorrect cases to impl-case")
  `(condp = (current-impl)
     clojure ~clojure
     clojurescript ~cljs
     (assert nil "No case matched for impl-case")))

(defn var->symbol [^clojure.lang.Var var]
  {:pre [(var? var)]
   :post [(symbol? %)
          (namespace %)]}
  (symbol (str (ns-name (.ns var)))
          (str (.sym var))))

(defn Class->symbol [^Class cls]
  {:pre [(class? cls)]
   :post [(symbol? %)]}
  (symbol (.getName cls)))

; for type-contract
(defn hmap-c? [& {:keys [mandatory optional absent-keys complete?]}]
  (every-pred map?
              #(cond
                 complete? (set/subset? (set (keys %))
                                        (set (mapcat keys [mandatory optional])))
                 :else
                 (let [actual-ks (set (keys %))]
                   (and 
                     ;required keys is a subset of actual keys
                     (set/subset? 
                       (set (keys mandatory))
                       actual-ks)
                     ;no absent-keys are present
                     (empty?
                       (set/intersection
                         absent-keys
                         actual-ks)))))
              #(every? identity 
                       (for [[k vc] mandatory]
                         (and (contains? % k)
                              (vc (get % k)))))
              #(every? identity 
                       (for [[k vc] optional]
                         (or (not (contains? % k))
                             (vc (get % k)))))))

(def init-aliases
  '[
  ^{:doc "A type that returns true for clojure.core/integer?"
    :forms [AnyInteger]}
clojure.core.typed/AnyInteger (U Integer Long clojure.lang.BigInt BigInteger Short Byte)

    ^{:doc "A type that returns true for clojure.core/integer?"
      :forms [Int]}
clojure.core.typed/Int (U Integer Long clojure.lang.BigInt BigInteger Short Byte)
      ^{:doc "A type that returns true for clojure.core/number?"
        :forms [Num]}
clojure.core.typed/Num Number
      ^{:doc "A keyword"
        :forms [Keyword]}
clojure.core.typed/Keyword clojure.lang.Keyword
      ^{:doc "A symbol"
        :forms [Symbol]}
clojure.core.typed/Symbol clojure.lang.Symbol

      ^{:doc "A namespace"
        :forms [Namespace]}
clojure.core.typed/Namespace clojure.lang.Namespace

    ^{:doc "An atom that can read and write type x."
      :forms [(Atom1 t)]}
clojure.core.typed/Atom1 (TFn [[x :variance :invariant]] 
                              (clojure.lang.Atom x x))
    ^{:doc "An atom that can write type w and read type r."
      :forms [(Atom2 t)]}
clojure.core.typed/Atom2 (TFn [[w :variance :contravariant]
                               [r :variance :covariant]] 
                              (clojure.lang.Atom w r))
    ^{:doc "An var that can read and write type x."
      :forms [(Var1 t)]}
clojure.core.typed/Var1 
    (TFn [[x :variance :invariant]] 
         (clojure.lang.Var x x))
    ^{:doc "An var that can write type w and read type r."
      :forms [(Var2 w r)]}
clojure.core.typed/Var2 
    (TFn [[w :variance :contravariant]
          [r :variance :covariant]] 
         (clojure.lang.Var w r))
    ^{:doc "A ref that can read and write type x."
      :forms [(Ref1 t)]}
clojure.core.typed/Ref1 (TFn [[x :variance :invariant]] (clojure.lang.Ref x x))
    ^{:doc "A ref that can write type w and read type r."
      :forms [(Ref2 w r)]}
clojure.core.typed/Ref2 (TFn [[w :variance :contravariant]
                              [r :variance :covariant]] 
                             (clojure.lang.Ref w r))
    ^{:doc "An agent that can read and write type x."
      :forms [(Agent1 t)]}
clojure.core.typed/Agent1 (TFn [[x :variance :invariant]] 
                               (clojure.lang.Agent x x))
    ^{:doc "An agent that can write type w and read type r."
      :forms [(Agent2 t t)]}
clojure.core.typed/Agent2 (TFn [[w :variance :contravariant]
                                [r :variance :covariant]] 
                               (clojure.lang.Agent w r))

    ^{:doc "A union of x and nil."
      :forms [(Option t)]}
clojure.core.typed/Option (TFn [[x :variance :covariant]] (U nil x))

    ^{:doc "A union of x and nil."
      :forms [(Nilable t)]}
clojure.core.typed/Nilable (TFn [[x :variance :covariant]] (U nil x))

      ^{:doc "The identity function at the type level."
        :forms [Id]}
clojure.core.typed/Id (TFn [[x :variance :covariant]] x)

      ^{:doc "A persistent collection with member type x."
        :forms [(Coll t)]}
clojure.core.typed/Coll (TFn [[x :variance :covariant]]
                             (clojure.lang.IPersistentCollection x))
    ^{:doc "A persistent collection with member type x and count greater than 0."
      :forms [(NonEmptyColl t)]}
clojure.core.typed/NonEmptyColl (TFn [[x :variance :covariant]]
                                      (I (clojure.lang.IPersistentCollection x) (CountRange 1)))
    ^{:doc "A persistent vector with member type x."
      :forms [(Vec t)]}
clojure.core.typed/Vec (TFn [[x :variance :covariant]]
                            (clojure.lang.IPersistentVector x))
    ^{:doc "A persistent vector with member type x and count greater than 0."
      :forms [(NonEmptyVec t)]}
clojure.core.typed/NonEmptyVec (TFn [[x :variance :covariant]]
                                     (I (clojure.lang.IPersistentVector x) (CountRange 1)))
    ^{:doc "A non-empty lazy sequence of type t"
      :forms [(NonEmptyLazySeq t)]}
clojure.core.typed/NonEmptyLazySeq (TFn [[t :variance :covariant]]
                                        (I (clojure.lang.LazySeq t) (CountRange 1)))
    ^{:doc "A persistent map with keys k and vals v."
      :forms [(Map t t)]}
clojure.core.typed/Map (TFn [[k :variance :covariant]
                             [v :variance :covariant]]
                            (clojure.lang.IPersistentMap k v))
    ^{:doc "A persistent set with member type x"
      :forms [(Set t)]}
clojure.core.typed/Set (TFn [[x :variance :covariant]]
                            (clojure.lang.IPersistentSet x))
    ^{:doc "A sorted persistent set with member type x"
      :forms [(SortedSet t)]}
clojure.core.typed/SortedSet (TFn [[x :variance :covariant]]
                               (Extends [(clojure.lang.IPersistentSet x) clojure.lang.Sorted]))
    ^{:doc "A type that can be used to create a sequence of member type x."
      :forms [(Seqable t)]}
clojure.core.typed/Seqable (TFn [[x :variance :covariant]]
                                (clojure.lang.Seqable x))
    ^{:doc "A type that can be used to create a sequence of member type x
with count greater than 0."
      :forms [(NonEmptySeqable t)]}

clojure.core.typed/NonEmptySeqable (TFn [[x :variance :covariant]]
                                         (I (clojure.lang.Seqable x) (CountRange 1)))
    ^{:doc "A type that can be used to create a sequence of member type x
with count 0."
      :forms [(EmptySeqable t)]}
clojure.core.typed/EmptySeqable (TFn [[x :variance :covariant]]
                                  (I (clojure.lang.Seqable x) (ExactCount 0)))
      ^{:doc "A persistent sequence of member type x."
        :forms [(Seq t)]}
clojure.core.typed/Seq (TFn [[x :variance :covariant]]
                            (clojure.lang.ISeq x))

    ^{:doc "A persistent sequence of member type x with count greater than 0."
      :forms [(NonEmptySeq t)]}
clojure.core.typed/NonEmptySeq (TFn [[x :variance :covariant]]
                                     (I (clojure.lang.ISeq x) (CountRange 1)))

    ^{:doc "A persistent sequence of member type x with count greater than 0, or nil."
      :forms [(NilableNonEmptySeq t)]}
clojure.core.typed/NilableNonEmptySeq (TFn [[x :variance :covariant]]
                                         (U nil (I (clojure.lang.ISeq x) (CountRange 1))))

    ^{:doc "The type of all things with count 0. Use as part of an intersection.
eg. See EmptySeqable."
      :forms [EmptyCount]}

clojure.core.typed/EmptyCount (ExactCount 0)
    ^{:doc "The type of all things with count greater than 0. Use as part of an intersection.
eg. See NonEmptySeq"
      :forms [NonEmptyCount]}
clojure.core.typed/NonEmptyCount (CountRange 1)

    ^{:doc "A hierarchy for use with derive, isa? etc."
      :forms [Hierarchy]}
clojure.core.typed/Hierarchy '{:parents (clojure.lang.IPersistentMap Any Any)
                               :ancestors (clojure.lang.IPersistentMap Any Any)
                               :descendants (clojure.lang.IPersistentMap Any Any)}

    ^{:doc "A Clojure future (see clojure.core/{future-call,future})."
      :forms [(Future t)]}
clojure.core.typed/Future 
                      (TFn [[x :variance :covariant]]
                       (Extends [(clojure.lang.IDeref x)
                                 (clojure.lang.IBlockingDeref x)
                                 clojure.lang.IPending
                                 java.util.concurrent.Future]))

    ^{:doc "A Clojure promise (see clojure.core/{promise,deliver})."
      :forms [(Promise t)]}
clojure.core.typed/Promise 
              (TFn [[x :variance :invariant]]
               (Rec [p]
                (I (Extends [(clojure.lang.IDeref x)
                             (clojure.lang.IBlockingDeref x)
                             clojure.lang.IPending])
                   [x -> (U nil p)])))

    ^{:doc "A Clojure delay (see clojure.core/{delay,force})."
      :forms [(Delay t)]}
clojure.core.typed/Delay
              (TFn [[x :variance :covariant]]
                   (clojure.lang.Delay x))

    ^{:doc "A Clojure derefable (see clojure.core/deref)."
      :forms [(Deref t)]}
clojure.core.typed/Deref
              (TFn [[x :variance :covariant]]
                   (clojure.lang.IDeref x))

    ^{:doc "A Clojure blocking derefable (see clojure.core/deref)."
      :forms [(BlockingDeref t)]}
clojure.core.typed/BlockingDeref
              (TFn [[x :variance :covariant]]
                   (clojure.lang.IBlockingDeref x))

    ^{:doc "A Clojure persistent list."
      :forms [(List t)]}
clojure.core.typed/List
              (TFn [[x :variance :covariant]]
                   (clojure.lang.IPersistentList x))

    ^{:doc "A Clojure custom exception type."
      :forms [ExInfo]}
clojure.core.typed/ExInfo
              (I clojure.lang.IExceptionInfo
                 RuntimeException)

    ^{:doc "A Clojure proxy."
      :forms [Proxy]}
clojure.core.typed/Proxy
              clojure.lang.IProxy

; Should c.l.Sorted be parameterised? Is it immutable?
;    ^{:doc "A sorted Clojure collection."
;      :forms [Sorted]}
;clojure.core.typed/Sorted
;              clojure.lang.Sorted

    ^{:doc "A Clojure stack."
      :forms [(Stack t)]}
clojure.core.typed/Stack
              (TFn [[x :variance :covariant]]
                   (clojure.lang.IPersistentStack x))

    ^{:doc "A Clojure reversible collection."
      :forms [(Reversible t)]}
clojure.core.typed/Reversible
              (TFn [[x :variance :covariant]]
                   (clojure.lang.Reversible x))

; TODO should this be parameterised?
;    ^{:doc "A Clojure sequential collection."
;      :forms [(Sequential t)]}
;clojure.core.typed/Sequential
;             clojure.lang.Sequential


    ^{:doc "A Clojure multimethod."
      :forms [Multi]}
clojure.core.typed/Multi
              clojure.lang.MultiFn
])

(assert (even? (count init-aliases)))
(assert (apply distinct? (map first (partition 2 init-aliases))))
