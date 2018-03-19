(ns lumbox3.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [lumbox3.core-test]))

(doo-tests 'lumbox3.core-test)

