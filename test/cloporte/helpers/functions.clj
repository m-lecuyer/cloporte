(ns cloporte.helpers.functions)

(defn foo [arg] (str "arg: " arg))

(defn foo-printer [arg]  (print (foo arg) "\n"))
