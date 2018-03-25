(ns lumbox3.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            cljsjs.semantic-ui-react
            [cljs.reader :as reader]
            [lumbox3.events :as events]))

(def sui js/semanticUIReact)

(defn header []
  [:> sui.Menu {:inverted true}
   [:> sui.Container
    [:> sui.Menu.Item {:as "a" :header true :link true :href "/#"} "Lumbox 3"]
    [:> sui.Menu.Item {:as "a" :link true :href "/#"} "Home"]
    [:> sui.Dropdown {:item true :simple true :text "Dropdown"}
     [:> sui.Dropdown.Menu
      [:> sui.Dropdown.Item {:value (pr-str [::events/set-active-panel :simple-sui-panel])
                             :onClick #(-> %2 (aget "value") reader/read-string rf/dispatch)} "Simple"]
      [:> sui.Dropdown.Item {:value (pr-str [::events/set-active-panel :simple-sui-react-panel])
                             :onClick #(-> %2 (aget "value") reader/read-string rf/dispatch)} "Simple React"]
      [:> sui.Dropdown.Divider]
      [:> sui.Dropdown.Item
       [:i.dropdown.icon]
       [:span.text "Submenu"]
       [:> sui.Dropdown.Menu
        [:> sui.Dropdown.Item {:value   (pr-str [::events/set-active-panel :simple-sui-panel])
                               :onClick #(-> %2 (aget "value") reader/read-string rf/dispatch)} "Simple"]
        [:> sui.Dropdown.Item {:value   (pr-str [::events/set-active-panel :simple-sui-react-panel])
                               :onClick #(-> %2 (aget "value") reader/read-string rf/dispatch)} "Simple React"]]]]]
    [:> sui.MenuItem {:position :right}
     [:> sui.Button {:as :a :inverted true} "Log in"]
     [:> sui.Button {:as :a :inverted true :style {:marginLeft "0.5em"}} "Register"]]]])

(defn home-page []
  [:div
   [header]
   [:> sui.Container {:text true :style {:marginTop "2em"}}
    [:> sui.Header {:as :h1 :content "SUI Header"}]]])