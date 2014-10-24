(ns cloporte.serializer)

;; TODO support functions as arguments?
;; TODO support func not defined
;; TODO verify args are serializable in json?
(defn serialize
  "Serializes a function call into a hash-map."
  [function-call & args]
  (let [func-meta (-> function-call resolve meta)]
    {:ns   (-> func-meta :ns str)                  ;; namespave of func
     :fn   (-> func-meta :name str)                ;; func fully qulified name
     :args (into [] args)}))  ;; arguments

(defn perform-job
  "Runs the function and args in the job."
  [job]
  ;; TODO check the namespace is already required?
  (require (symbol (:ns job)))
  (apply (resolve (symbol (:ns job) (:fn job)))
         (into () (:args job))))
