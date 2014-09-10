(ns cloporte.core)

(defn serialize
  ""
  [& args]
  (map nil nil))

(defmacro perform-async
  "Enqueues the job on cloporte's redis queue."
  [function & args]
  nil)
