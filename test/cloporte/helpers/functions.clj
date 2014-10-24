(ns cloporte.helpers.functions)

(defn foo
  ([arg] (str "arg: " arg))
  ([arg n] (apply str (repeat n arg))))

(defn foo-printer [arg]  (print (foo arg) "\n"))
