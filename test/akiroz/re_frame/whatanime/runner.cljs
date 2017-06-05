(ns akiroz.re-frame.whatanime.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [akiroz.re-frame.whatanime.test]))

(doo-tests 'akiroz.re-frame.whatanime.test)
