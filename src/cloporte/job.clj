(ns cloporte.job)

;; TODO support functions as arguments?
;; TODO support func not defined
;; TODO verify args are serializable in json?
;; TODO verify we have a symbol that is fn?
(defn serialize
  "Serializes a function call into a hash-map."
  [function-call & args]
  (let [func-meta (-> function-call resolve meta)]
    {:ns   (-> func-meta :ns str)    ;; namespave of func
     :fn   (-> func-meta :name str)  ;; func fully qulified name
     :args (into [] args)}))         ;; arguments

(defn perform-job
  "Runs the function and args in the job."
  [job]
  (require (symbol (:ns job)))
  (apply (resolve (symbol (:ns job) (:fn job)))
         (reverse (into () (:args job)))))

;; TODO error checking and handle/return errors
(defmacro perform-async
  "Enqueues the job on cloporte's redis queue."
  [qname function-call failure-callback]
  ;; save function symbol, eval args, serialize and queue
  `(queue/redis-enqueue
    (or ~qname :default)
    (job/serialize (quote ~(first function-call)) ~@(rest function-call))))
