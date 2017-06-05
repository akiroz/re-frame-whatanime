(defproject akiroz.re-frame/whatanime "0.1.0-SNAPSHOT"
  :description "re-frame fx for the whatanime.ga API"
  :url "https://github.com/akiroz/re-frame-whatanime"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/core.async "0.3.443"]
                 [com.rpl/specter "1.0.1"]
                 [funcool/promesa "1.8.1"]
                 [com.cemerick/url "0.1.1"]
                 [cljs-http "0.1.43"]]

  :clean-targets ^{:protect false} ["target" "out"]

  :profiles {:dev {:plugins [[lein-cljsbuild "1.1.6"]
                             [lein-doo "0.1.7"]]
                   :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                                  [org.clojure/clojurescript "1.9.562"]
                                  [re-frame "0.9.4"]
                                  [smidje "0.2.0"]]
                   :doo {:build "test"}
                   :cljsbuild {:builds [{:id "test"
                                         :source-paths ["src" "test"]
                                         :compiler {:output-to "out/testable.js"
                                                    :optimizations :none
                                                    :parallel-build true
                                                    :main akiroz.re-frame.whatanime.runner}}]}}}

  )
