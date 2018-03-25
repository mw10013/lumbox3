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
      [:> sui.Dropdown.Item {:value (pr-str [::events/set-main-view :about])
                             :onClick #(-> %2 (aget "value") reader/read-string rf/dispatch)} "About"]
      [:> sui.Dropdown.Item {:value (pr-str [::events/set-main-view :home])
                             :onClick #(-> %2 (aget "value") reader/read-string rf/dispatch)} "Home"]
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

(defn home-view []
  [:div
   [:> sui.Header {:as :h1 :content "Home"}]])

(defn about-view []
  [:div
   [:> sui.Header {:as :h1 :content "About"}]])

(def main-views
  {:home home-view
   :about about-view})

(defn root-view []
  [:div
   [header]
   [:> sui.Container {:text true :style {:marginTop "2em"}}
    [(main-views @(rf/subscribe [::events/main-view]))]]])