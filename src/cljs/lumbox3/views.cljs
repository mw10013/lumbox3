(ns lumbox3.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            cljsjs.semantic-ui-react
            [cljs.reader :as reader]
            [lumbox3.events]))

(def sui js/semanticUIReact)

(defn home-page []
  [:div
   [:> sui.Container {:text true :style {:marginTop "2em"}}
    [:> sui.Header {:as :h1 :content "SUI Header"}]]])