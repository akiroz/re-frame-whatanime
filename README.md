# re-frame-whatanime

[![clojars shield][]][clojars]
[![license shield][]][license]

A re-frame fx for interacting with the [whatanime.ga API][].

Depends on `re-frame >= 0.8.0`. 

## Usage

```clojure
(ns my-app
  (:require [akiroz.re-frame.whatanime :as whatanime]))

;; register the re-frame fx
(whatanime/reg-fx!
  {:token "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"})

;; use the fx
(reg-event-fx
  :search-image
  (fn search-image [_ [_ image]]
    ;; NOTE: fully qualified keyword used!
    ;; If you don't import the ns, you must use :akiroz.re-frame.whatanime/search
    {::whatanime/search {:image image
                         :filter "2017-*"
                         :success-dispatch [:search-image-success]
                         :failure-dispatch [:search-image-failure]}}))

```

## API Reference

For every API call:

If the server responds with status code 200, we will dispatch the vector provided by `:success-dispatch`,
otherwise we will dispatch the vector provided by `:failure-dispatch`. Both are optional.

The dispatch vectors will have a result map added to the end with the status code along with data from the server.

For example (failure dispatch vector):

```clojure
[:search-image-failure {:status 429
                        :message "Search quota exceeded. Please wait 87 seconds."}]
``` 

-----------------------------------------------------------

* `:akiroz.re-frame.whatanime/me` Get API token info from server.

**Success Result:**
```
{:status 200
 :user-id 1001
 :email "soruly@gmail.com"}
```

-----------------------------------------------------------

* `:akiroz.re-frame.whatanime/list` List seasons and anime.

This information is **used by the server internally** and you may use it to narrow the
image search result (see `filter` argument in the `search` fx).

**Success Result:**
```clojure
{:status 200
 :result {"1990-1999" ["Berserk"
                       "COWBOY BEBOP"
                       "GTO"
                       "Sailor Moon"]
          "2002-04"   [ ... ]
          "2005-10"   [ ... ]
          "Movie"     [ ... ]
          "OVA"       [ ... ]
          }}
```


-----------------------------------------------------------

* `:akiroz.re-frame.whatanime/search` Image reverse search.

**Arguments:**
```clojure
{:image image
 :filter "*Gundam*"}
```

* `image`: `image/jpeg` MIME; either in data URL (string) or Blob (object) format.
* `filter`: [Optional] filter search results based on a `<season>/<anime>` string path with
  the `*` wild card character (see `list` fx for possible seasons and anime).

**Success Result:**
```clojure
{:status 200
 :rate-limit {:remaining-quota  4                                           ;; request quota remaining for current time frame
              :reset-timeout    286}                                        ;; seconds until quota resets
 :trials [{:frames-compared 28103                                           ;; number of frames compared for this trial
           :search-time     134                                             ;; seconds used for searching in this trial
           :rank-time       179}                                            ;; seconds used for ranking in this trial
          { ... }
          { ... }]
 :results [{:match {:similarity 0.96267949                                  ;; image similarity (0-1 float)
                    :season     "2014-07"                                   ;; server internal season folder
                    :anime      "ALDNOAH.ZERO"                              ;; server internal anime folder
                    :file       "[KTXP][Aldnoah.Zero][03][BIG5][720p].mp4"  ;; server internal file name
                    :at         708.667}                                    ;; seconds from start of file
            :title {:japanese "アルドノア・ゼロ"
                    :romaji   "ALDNOAH.ZERO"
                    :english  "ALDNOAH.ZERO"
                    :chinese  "ALDNOAH.ZERO"
                    :synonyms {:english []
                               :chinese ["ALDNOAH ZERO"]}}
            :thumbnail  "https://..."                                       ;; thumbnail image of match
            :preview    "https://..."                                       ;; preview image of match
            :anilist-id 20632                                               ;; AniList database ID
            }
           { ... }
           { ... }]}
```

[whatanime.ga API]: https://soruly.github.io/whatanime.ga
[clojars]: https://clojars.org/akiroz.re-frame/whatanime
[clojars shield]: https://img.shields.io/clojars/v/akiroz.re-frame/whatanime.svg
[license]: https://raw.githubusercontent.com/akiroz/re-frame-storage/master/LICENSE
[license shield]: https://img.shields.io/badge/license-MIT-blue.svg
