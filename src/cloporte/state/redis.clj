(ns cloporte.state.redis)

(def ^:private config*  (ref {}))

(defn config [] @config*)

(defn set-config! [new-config]
  {:pre [(map? new-config)]}
  (dosync
   (ref-set config* new-config)))
