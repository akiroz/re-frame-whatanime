(ns akiroz.re-frame.storage
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [re-frame.core :refer [reg-fx dispatch]]
            ))

(def api-token (atom nil))

(defn -me []
  )

(defn -list []
  )

(defn -search []
  )

(defn -wrap-dispatch [f]
  (fn [:keys [success-dispatch
              failure-dispatch]
       :as arg]
    (go
      (let [[success? result] (<! (f arg))]
        (if success?
          (when success-dispatch
            (dispatch (conj success-dispatch result)))
          (when failure-dispatch
            (dispatch (conj failure-dispatch result))))))))

(defn reg-fx! [{:keys [token]}]
  (reset! api-token token)
  (reg-fx ::me -me)
  (reg-fx ::list -list)
  (reg-fx ::search -search))
