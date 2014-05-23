(ns clojure.core.typed.tc-equiv
  (:require [clojure.core.typed.type-rep :as r]
            [clojure.core.typed.type-ctors :as c]
            [clojure.core.typed.filter-rep :as fl]
            [clojure.core.typed.filter-ops :as fo]
            [clojure.math.combinatorics :as comb]))

;[Any TCResult * -> TCResult]
(defn tc-equiv [comparator & vs]
  {:pre [(every? r/TCResult? vs)]
   :post [(r/TCResult? %)]}
  (assert (#{:=} comparator))
  (assert (seq vs))
  (let [; TODO sequence behaviour is subtle
        ; conservative for now
        equiv-able (fn [t]
                     (boolean
                       (when (r/Value? t)
                         ((some-fn number? symbol? keyword? nil? true? false? class?) (:val t)))))
        vs-combinations (comb/combinations vs 2)
        ;_ (prn vs-combinations)
        then-filter (apply fo/-and (apply concat
                                          (for [[{t1 :t fl1 :fl o1 :o}
                                                 {t2 :t fl2 :fl o2 :o}] vs-combinations]
                                            (concat
                                              (when (equiv-able t1) 
                                                [(fo/-filter-at t1 o2)])
                                              (when (equiv-able t2) 
                                                [(fo/-filter-at t2 o1)])))))
        ;_ (prn then-filter)
        else-filter (apply fo/-or 
                           (if-let [fs (seq (apply concat
                                                   (for [[{t1 :t fl1 :fl o1 :o}
                                                          {t2 :t fl2 :fl o2 :o}] vs-combinations]
                                                     (concat
                                                       (when (equiv-able t1) 
                                                         [(fo/-not-filter-at t1 o2)])
                                                       (when (equiv-able t2) 
                                                         [(fo/-not-filter-at t2 o1)])))))]
                             fs
                             ; ensure we don't simplify to ff if we have more than one
                             ; argument to = (1 arg is always a true value)
                             (when (< 1 (count vs))
                               [fl/-top])))
        ;_ (prn else-filter)
        ]
    (r/ret (c/Un r/-false r/-true)
           (fo/-FS then-filter else-filter))))
