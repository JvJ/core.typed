(ns clojure.core.typed.test.munge-record-field
  (:require [clojure.core.typed :as t :refer [ann check-ns ann-form print-env cf ann-record ann-datatype]])
  (:import (clojure.lang IPersistentMap IPersistentVector)))

(ann-datatype FooDT [normal :- Number
                     other-keys? :- Number])
(deftype FooDT [normal other-keys?])

(ann-record FooRec [other-keys? :- Number])
(defrecord FooRec [other-keys?])

(ann-form (.normal ^FooDT (->FooDT 2 1)) Number)
(ann-form (.other-keys? ^FooDT (->FooDT 2 1)) Number)

(ann-form (:other-keys? (->FooRec 1)) Number)
(ann-form (.other-keys? ^FooRec (->FooRec 1)) Number)
