(ns cloporte.serializer)

;; TODO support functions as arguments?
;; TODO support func not defined
;; TODO very args a serializable in json?
(defn serialize
  "Serializes a function call into a hash-map."
  [function-call-list]
  (let [func-meta (-> function-call-list first resolve meta)]
    {:ns   (-> func-meta :ns str)                  ;; namespave of func
     :fn   (-> func-meta :name str)                ;; func fully qulified name
     :args (into [] (rest function-call-list))}))  ;; arguments

(defn perform-job
  "Runs the function and args in the job."
  [job]
  ;; TODO check the namespace is already required?
  (require (symbol (:ns job)))
  (apply (resolve (symbol (:ns job) (:fn job)))
         (into () (:args job))))
