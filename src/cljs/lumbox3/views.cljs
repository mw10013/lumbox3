(ns lumbox3.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            cljsjs.antd
            cljsjs.moment
            [cljs.reader :as reader]
            [lumbox3.events :as events]
            [lumbox3.routes :as routes]
            [lumbox3.validation :as v]))

(def antd js/antd)
(def m js/moment)

(defn debug-cache [cache-key]
  [:div [:hr]
   [:div "cache-key: " cache-key ": " (pr-str @(rf/subscribe [:cache cache-key]))]
   [:div "identity: " (pr-str @(rf/subscribe [:identity]))]
   [:div "status: " @(rf/subscribe [:status])]
   [:div "result: " @(rf/subscribe [:result])]])

(def form-item-layout {:labelCol   {:xs {:span 24}
                                    :sm {:span 7}
                                    :lg {:span 10}}
                       :wrapperCol {:xs {:span 24}
                                    :sm {:span 10}
                                    :lg {:span 6}}})

(def tail-form-item-layout {:wrapperCol {:xs {:span 24 :offset 0}
                                         :sm {:span 10 :offset 7}
                                         :lg {:span 6 :offset 10}}})

(defn dispatch-sync-flush
  "Dispatch-sync reframe event and then flush reagent.
   Re-frame event must be a vector and the target value of react-event will
   be conj'd onto it.

   Antd input components seem to need the DOM to be refreshed synchronously"
  [reframe-event react-event]
  (rf/dispatch-sync (conj reframe-event (-> react-event .-target .-value)))
  (r/flush))

(defn register-view []
  (let [cache-key :register
        input @(rf/subscribe [:input cache-key])
        input-errors @(rf/subscribe [:input-errors cache-key])
        error-message @(rf/subscribe [:error-message cache-key])]
    [:> antd.Row {:type :flex :justify :center}
     [:> antd.Form {:style     {:max-width "300px"}
                    :on-submit (fn [e]
                                 (.preventDefault e)
                                 (.stopPropagation e)
                                 (let [[input-errors input] (v/validate-register-user-input @(rf/subscribe [:input cache-key]))]
                                   (rf/dispatch [:set-input-errors cache-key input-errors])
                                   (rf/dispatch [:set-error-message cache-key nil])
                                   (when-not input-errors
                                     (rf/dispatch [:register-user cache-key input]))))}
      (when error-message
        [:p [:> antd.Alert {:type :error :message error-message}]])
      [:> antd.Form.Item (when-let [errors (:email input-errors)] {:validateStatus :error :hasFeedback true
                                                                   :help           errors})
       [:> antd.Input {:placeholder "E-mail address"
                       :prefix      (r/as-element [:> antd.Icon {:type :user :style {:color "rgba(0,0,0,.25)"}}])
                       :value       (:email input)
                       :on-change   (partial dispatch-sync-flush [:set-input cache-key :email])}]]
      [:> antd.Form.Item (when-let [errors (:password input-errors)] {:validateStatus :error :hasFeedback true
                                                                      :help           errors})
       [:> antd.Input {:type      :password :placeholder "Password"
                       :prefix    (r/as-element [:> antd.Icon {:type :lock :style {:color "rgba(0,0,0,.25)"}}])
                       :value     (:password input)
                       :on-change (partial dispatch-sync-flush [:set-input cache-key :password])}]]
      [:> antd.Button {:type :primary :htmlType :submit :style {:width "100%"}} "Register"]
      [debug-cache cache-key]]]))

(defn login-view []
  (let [cache-key :login
        input @(rf/subscribe [:input cache-key])
        input-errors @(rf/subscribe [:input-errors cache-key])
        error-message @(rf/subscribe [:error-message cache-key])]
    [:> antd.Row {:type :flex :justify :center}
     [:> antd.Form {:style     {:max-width "300px"}
                    :on-submit (fn [e]
                                 (.preventDefault e)
                                 (.stopPropagation e)
                                 (let [[input-errors input] (v/validate-login-input @(rf/subscribe [:input cache-key]))]
                                   (rf/dispatch [:set-input-errors cache-key input-errors])
                                   (rf/dispatch [:set-error-message cache-key nil])
                                   (when-not input-errors
                                     (rf/dispatch [:login cache-key input]))))}
      (when error-message
        [:p [:> antd.Alert {:type :error :message error-message}]])
      [:> antd.Form.Item (when-let [errors (:email input-errors)] {:validateStatus :error :hasFeedback true
                                                                   :help           errors})
       [:> antd.Input {:placeholder "E-mail address"
                       :prefix      (r/as-element [:> antd.Icon {:type :user :style {:color "rgba(0,0,0,.25)"}}])
                       :value       (:email input)
                       :on-change   (partial dispatch-sync-flush [:set-input cache-key :email])}]]
      [:> antd.Form.Item (when-let [errors (:password input-errors)] {:validateStatus :error :hasFeedback true
                                                                      :help           errors})
       [:> antd.Input {:type      :password :placeholder "Password"
                       :prefix    (r/as-element [:> antd.Icon {:type :lock :style {:color "rgba(0,0,0,.25)"}}])
                       :value     (:password input)
                       :on-change (partial dispatch-sync-flush [:set-input cache-key :password])}]]
      [:> antd.Button {:type :primary :htmlType :submit :style {:width "100%"}} "Login"]
      [debug-cache cache-key]]]))

(defn logout-view []
  (let [cache-key :logout
        error-message @(rf/subscribe [:error-message cache-key])]
    [:div
     [:h3 "Logout"]
     (when error-message [:div.alert.alert-danger error-message])
     [debug-cache cache-key]]))

(defn home-view []
  [:div
   [:h1 "Home"]
   [debug-cache :login]])

(defn about-view []
  [:> antd.Row {:type :flex :justify :center}
   [:h1 "About"]])

(defn error-view []
  [:div
   [:> antd.Alert {:type :error :message "Error" :description "Error description." :show-icon true}]])

(defn path-not-found-view []
  [:div
   [:> antd.Alert {:type :error :message "Page not found." :description "Invalid path." :show-icon true}]])

(def main-views
  {:home           home-view
   :about          about-view
   :register       register-view
   :login          login-view
   :logout         logout-view
   :error          error-view
   :path-not-found path-not-found-view})

(defn main-view []
  (let [k @(rf/subscribe [:route-name])]
    [(get main-views k #(vector :div (str "main-view not found: " k)))]))

(defn header []
  (let [identity @(rf/subscribe [:identity])]
    [:> antd.Layout.Header
     [:> antd.Menu {:theme :dark :mode :horizontal :selectable false
                    :style {:line-height "64px"}}

      [:> antd.Menu.Item
       [:a {:href (routes/href :home)}] "Lumbox 3"]
      [:> antd.Menu.Item
       [:> antd.Icon {:type     (if @(rf/subscribe [:sider-collapsed]) :menu-unfold :menu-fold)
                      #_:style    #_{:color "#1890ff"}
                      :on-click #(rf/dispatch [:toggle-sider])}]]

      ; float right appears in opposite order ie. Login Register
      (when-not identity [:> antd.Menu.Item {:style {:float :right}}
                          [:a {:href (routes/href :register)} "Register"]])
      (if identity
        [:> antd.Menu.Item {:style {:float :right}}
         [:> antd.Dropdown {:overlay (r/as-element [:> antd.Menu {:mode :inline :theme :dark}
                                                    [:> antd.Menu.Item {:key :logout}
                                                     #_[:> antd.Icon {:type :logout}]
                                                     [:a {:href (routes/href :logout)} "Logout"]
                                                     #_[:span [:a {:href (routes/href :logout)} "Logout"]]
                                                     ]])}
          [:span (:email identity)]]]
        #_[:> antd.Menu.Item {:style {:float :right}}
           [:a {:on-click #(rf/dispatch [:logout :logout])} "Logout"]]
        [:> antd.Menu.Item {:style {:float :right}}
         [:a {:href (routes/href :login)} "Login"]])]]))

;; for responsive sider use {:breakpoint :lg :collapsed-width 0}
(defn sider []
  [:> antd.Layout.Sider {:trigger   nil :collapsible true :collapsed-width 0
                         :collapsed @(rf/subscribe [:sider-collapsed])}
   [:> antd.Menu {:theme :dark :mode :inline :selectable false}
    #_[:> antd.Menu.Item
     [:a {:href (routes/href :home)}] "Lumbox 3"]
    [:> antd.Menu.Item
     [:a {:href (routes/href :about)}] "About"]
    [:> antd.Menu.SubMenu {:title "Admin"}
     [:> antd.Menu.Item "Users"]
     [:> antd.Menu.Item "Groups"]]]])

(defn footer []
  [:> antd.Layout.Footer {:style {:text-align :center}} "Footer"])

(defn root-view []
  [:> antd.Layout
   [header]
   [:> antd.Layout
    [sider]
    [:> antd.Layout.Content {:style {:margin 0 :padding "2em" :border "0px solid black"}}
     [main-view]
     [footer]]]])
