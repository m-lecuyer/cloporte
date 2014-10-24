(ns cloporte.helpers.state)

;;
;; TODO use atom to update the work map
;;

(def ^:private work*  (ref {}))

(defn work [] @work*)

(defn set-work! [work]
  {:pre [(map? work)]}
  (dosync
   (ref-set work* work)))
