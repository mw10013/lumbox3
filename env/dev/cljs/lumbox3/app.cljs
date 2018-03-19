(ns ^:figwheel-no-load lumbox3.app
  (:require [lumbox3.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
