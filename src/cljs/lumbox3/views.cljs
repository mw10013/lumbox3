(ns lumbox3.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            cljsjs.antd
            cljsjs.moment
            [cljs.reader :as reader]
            [lumbox3.events :as events]
            [lumbox3.validation :as v]))

(def antd js/antd)
(def m js/moment)

(defn header []
  [:div "header"]
  #_[:> sui.Menu {:inverted true}
   [:> sui.Container
    #_[:> sui.Menu.Item {:as "a" :header true :link true :href "/#"} "Lumbox 3"]
    [:> sui.Menu.Item {:as "a" :header true :on-click #(rf/dispatch [::events/set-main-view :home])} "Lumbox 3"]
    [:> sui.Menu.Item {:as "a" :on-click #(rf/dispatch [::events/set-main-view :home])} "Home"]
    [:> sui.Dropdown {:item true :simple true :text "Dropdown"}
     [:> sui.Dropdown.Menu
      #_[:> sui.Dropdown.Item {:value   (pr-str [::events/set-main-view :about])
                               :onClick #(-> %2 (aget "value") reader/read-string rf/dispatch)} "About"]
      [:> sui.Dropdown.Item {:on-click #(rf/dispatch [::events/set-main-view :about])} "About"]
      [:> sui.Dropdown.Item {:onClick #(rf/dispatch [::events/set-main-view :home])} "Home"]
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
     [:> sui.Button {:as :a :inverted true :on-click #(rf/dispatch [::events/set-main-view :login])} "Log in"]
     [:> sui.Button {:as       :a :inverted true :style {:marginLeft "0.5em"}
                     :on-click #(rf/dispatch [::events/set-main-view :register])} "Register"]]]])

(defn footer []
  [:div "footer"]
  #_[:> sui.Segment {:inverted true :vertical true :style {:padding "5em 0em"}}
   [:> sui.Container
    [:> sui.Grid {:divided true :inverted true :stackable true}
     [:> sui.Grid.Row
      [:> sui.Grid.Column {:width 3}
       [:> sui.Header {:as :h4 :inverted true :content "About"}]
       [:> sui.List {:link true :inverted true}
        [:> sui.List.Item {:as :a} "Sitemap"]
        [:> sui.List.Item {:as :a} "Contact"]]]
      [:> sui.Grid.Column {:width 3}
       [:> sui.Header {:as :h4 :inverted true} "Services"]
       [:> sui.List {:link true :inverted true}
        [:> sui.List.Item {:as :a} "Pre-Order"]
        [:> sui.List.Item {:as :a} "FAQ"]]]
      [:> sui.Grid.Column {:width 7}
       [:> sui.Header {:as :h4 :inverted true} "Footer Header"]
       [:p "Extra space for a call to action inside the footer that could help re-engage users."]]]]]]
  )

(defn home-view []
  [:div
   [:h1 "Home"]])

(defn about-view []
  [:div
   [:h1 "About"]])

(defn debug-cache [cache-key]
  [:div [:hr]
   [:div "cache-key: " cache-key ": " (pr-str @(rf/subscribe [:cache cache-key]))]
   [:div "identity: " (pr-str @(rf/subscribe [:identity]))]
   [:div "status: " @(rf/subscribe [:status])]
   [:div "result: " @(rf/subscribe [:result])]])

(defn register-view []
  [:div "register-view"]
  #_(let [cache-key :register
        input @(rf/subscribe [:input cache-key])
        input-errors @(rf/subscribe [:input-errors cache-key])
        error-message @(rf/subscribe [:error-message cache-key])]
    [:> sui.Grid {:textAlign "center" #_:style #_{:height "100%"} :verticalAlign "middle"}
     [:> sui.Grid.Column {:style {:maxWidth 450}}
      [:> sui.Header {:as :h2 :color "teal" :textAlign "center"} "Register for an account"]
      [:> sui.Form {:size :large
                    :on-submit (fn [e]
                            (.preventDefault e)
                            (.stopPropagation e)
                            (let [[input-errors input] (v/validate-register-user-input @(rf/subscribe [:input cache-key]))]
                              (rf/dispatch [:set-input-errors cache-key input-errors])
                              #_(when-not input-errors
                                (rf/dispatch [:register-user cache-key input]))))}
       [:> sui.Segment {:stacked true}
        [:> sui.Form.Input {:fluid     true :icon :user :iconPosition :left :placeholder "E-mail address"
                            :value     (:email input) :label "E-mail" :required true
                            :on-change #(rf/dispatch [:set-input cache-key :email (-> % .-target .-value)])}]
        [:> sui.Form.Input {:fluid     true :icon :lock :iconPosition :left :placeholder "Password" :type :password
                            :value     (:password input)
                            :on-change #(rf/dispatch [:set-input cache-key :password (-> % .-target .-value)])}]
        [:> sui.Button {:color :teal :fluid true :size :large} "Register"]
        [debug-cache cache-key]]]]]))

(defn login-view []
  [:div "login-view"]
  #_[:> sui.Grid {:textAlign "center" :style {:height "100%"} :verticalAlign "middle"}
   [:> sui.Grid.Column {:style {:maxWidth 450}}
    [:> sui.Header {:as :h2 :color "teal" :textAlign "center"} "Log in to your account"]
    [:> sui.Form {:size :large}
     [:> sui.Segment {:stacked true}
      [:> sui.Form.Input {:fluid true :icon :user :iconPosition :left :placeholder "E-mail address"}]
      [:> sui.Form.Input {:fluid true :icon :lock :iconPosition :left :placeholder "Password" :type :password}]
      [:> sui.Button {:color :teal :fluid true :size :large} "Login"]]]]])

(defn logout-view []
  [:div
   [:h1 "Log out"]])


(def main-views
  {:home     home-view
   :about    about-view
   :register register-view
   :login    login-view
   :logout   logout-view})

(defn main-view []
  (let [main-view @(rf/subscribe [::events/main-view])]
    [(case main-view
       :home home-view
       :about about-view
       :register register-view
       :login login-view
       :logout logout-view)]))

(defn root-view []
  [:> antd.Layout
   [:> antd.Layout.Header
    [:div {:style {:float :left :padding-right 20}}
     [:a {:href "#/" :style {:text-decoration :none}} "Lumbox3"]]
    [:> antd.Menu {:theme :dark :mode :horizontal :selectable false
                   :style {:line-height "64px"}}
     [:> antd.Menu.Item {:key :1}
      [:a {:href "#/about"}] "About"]
     [:> antd.Menu.Item {:key :2}
      [:> antd.Icon {:type :mail}]
      "Nav 2"]
     [:> antd.Menu.SubMenu {:title "Admin"}
      [:> antd.Menu.Item "Users"]
      [:> antd.Menu.Item "Groups"]]

     [:> antd.Menu.Item {:key :3} "Nav 3"]
     ; float right appears in opposite order ie. Login Register
     [:> antd.Menu.Item {:style {:float :right}}
      [:a {:href "#/register"} "Register"]]
     [:> antd.Menu.Item {:style {:float :right}}
      [:a {:href "#/login"} "Login"]]]]
   [:> antd.Layout.Content {:style {:margin 0 :padding 12 :border "1px solid black"}}
    [main-view]
    #_[:div {:style {:background "#fff" :padding 12 :min-height 280 :border "1px solid black"}} "Content"]]
   [:> antd.Layout.Footer {:style {:text-align :center}} "Footer"]])

#_(defn root-view []
  [:div
   "root view"
   #_[header]
   #_[:> sui.Container {:text true :style {:marginTop "2em"}}
    [(main-views @(rf/subscribe [::events/main-view]))]]
   #_[footer]])