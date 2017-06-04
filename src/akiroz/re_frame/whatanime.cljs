(ns akiroz.re-frame.whatanime
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<!]]
            [promesa.core :as p]
            [com.rpl.specter :refer [transform MAP-VALS ALL]]
            [cljs-http.client :as http]
            [cemerick.url :refer [url]]
            [re-frame.core :refer [reg-fx dispatch]]
            ))

(def api-host     "https://whatanime.ga")
(def api-endpoint (str api-host "/api"))
(def api-token    (atom nil))

(defn as-promise [request-fn & args]
  (p/promise
    (fn [rsov rjct]
      (go
        (let [{:as response
               :keys [status body]} (<! (apply request-fn args))]
          (if (>= status 400)
            (rjct {:status status :message body})
            (rsov response)))))))

(defn -me []
  (->> (as-promise http/get
                   (str (url api-endpoint "me"))
                   {:query-params {:token @api-token}})
       (p/map (fn [:keys [status body]]
                {:status  status
                 :user-id (get body "user_id")
                 :email   (get body "email")}))))

(defn -list []
  (->> (as-promise http/get
                   (str (url api-endpoint "list"))
                   {:query-params {:token @api-token}})
       (p/map (fn [:keys [status body]]
                {:status status
                 :results (->> body
                               (mapv (let [[_ season anime] (re-find #"(.+)/(.+)" %)]
                                       {:season season :anime anime}))
                               (group-by :season)
                               (transform [MAP-VALS ALL] :anime))}))))


(defn -search [{:keys [image] :as args}]
  (->> (p/promise (if (string? image)
                    image
                    (fn [rsov]
                      (doto (new js/FileReader)
                        (.readAsDataURL image)
                        (.onload #(-> % .-target .-result rsov))))))
       (p/map (fn [image-url]
                (as-promise http/post
                            (str (url api-endpoint "search"))
                            {:query-params  {:token @api-token}
                             :form-params   {:image image-url
                                             :filter (get args :filter "*")}})))
       (p/map (fn [:keys [status body]]
                {:status      status
                 :rate-limit  {:remaining-quota  (get body "quota")
                               :reset-timeout    (get body "expire")}
                 :trials      (mapv (fn [frames search rank]
                                      {:frames-compared frames
                                       :search-time search
                                       :rank-time rank})
                                    (get body "RawDocsCount")
                                    (get body "RawDocsSearchTime")
                                    (get body "ReRankSearchTime"))
                 :results     (->> (get body "docs")
                                   (mapv
                                     (fn [result]
                                       (let [thumb-token (get result "tokenthumb")
                                             match {:season (get result "season")
                                                    :anime  (get result "anime")
                                                    :file   (get result "filename")
                                                    :at     (get result "at")}]
                                         {:match (assoc match :similarity (get result "similarity"))
                                          :title {:japanese (get result "title")
                                                  :romaji   (get result "title_romaji")
                                                  :english  (get result "title_english")
                                                  :chinese  (get result "title_chinese")
                                                  :synonyms {:english (get result "synonyms")
                                                             :chinese (get result "synonyms_chinese")}}
                                          :thumbnail  (str (assoc
                                                             (url api-host "thumbnail.php")
                                                             :query (assoc match :token thumb-token)))
                                          :preview    (str (assoc
                                                             (url api-host "preview.php")
                                                             :query (assoc match :token thumb-token)))
                                          :ailist-id  (get result "anilist_id")}))))}))))

(defn -wrap-dispatch [f]
  (fn [:keys [success-dispatch
              failure-dispatch]
       :as arg]
    (->> (f arg)
         (p/map
           #(when success-dispatch
              (dispatch (conj success-dispatch %))))
         (p/error
           #(when failure-dispatch
              (dispatch (conj failure-dispatch %)))))))


(defn reg-fx! [{:keys [token]}]
  (reset! api-token token)
  (reg-fx ::me      (-wrap-dispatch -me))
  (reg-fx ::list    (-wrap-dispatch -list))
  (reg-fx ::search  (-wrap-dispatch -search)))


