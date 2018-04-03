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

(defn debug-cache [cache-key]
  [:div [:hr]
   [:div "cache-key: " cache-key ": " (pr-str @(rf/subscribe [:cache cache-key]))]
   [:div "identity: " (pr-str @(rf/subscribe [:identity]))]
   [:div "status: " @(rf/subscribe [:status])]
   [:div "result: " @(rf/subscribe [:result])]])

(defn controlled-input []
  (let [state (r/atom {})]
    (fn []
      [:div
       [:input {:value (:html-value @state)
                :on-change #(swap! state assoc :html-value (-> % .-target .-value))}]
       [:> antd.Input {:value (:antd-value @state) :formNoValidate true
                       :on-change #(do
                                     (swap! state assoc :antd-value (-> % .-target .-value))
                                     (r/flush))}]
       [:hr]
       [:div "state:" (pr-str @state)]])))

(defn register-view- []
  [controlled-input])

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
        error-message @(rf/subscribe [:error-message cache-key])
        form-item-layout {:labelCol   {:xs {:span 24}
                                       :sm {:span 6}}
                          :wrapperCol {:xs {:span 24}
                                       :sm {:span 12}}}]
    [:> antd.Form {:style     {:max-width "350px"}
                   :on-submit (fn [e]
                                (.preventDefault e)
                                (.stopPropagation e)
                                (let [[input-errors input] (v/validate-register-user-input @(rf/subscribe [:input cache-key]))]
                                  (rf/dispatch [:set-input-errors cache-key input-errors])
                                  #_(when-not input-errors
                                      (rf/dispatch [:register-user cache-key input]))))}
     [:> antd.Form.Item (merge form-item-layout {:label "E-mail" :required true}
                               (when-let [errors (:email input-errors)] {:validateStatus :error :hasFeedback true
                                                                         :help errors}))
      [:> antd.Input {:placeholder "E-mail address"
                      :value       (:email input)
                      :on-change    (partial dispatch-sync-flush [:set-input cache-key :email])}]]
     [:> antd.Form.Item (merge form-item-layout {:label "Password" :required true}
                               (when-let [errors (:password input-errors)] {:validateStatus :error :hasFeecback true
                                                                            :help           errors}))
      [:> antd.Input {:id        :password :type :password :placeholder "Password"
                      :value     (:password input)
                      :on-change (partial dispatch-sync-flush [:set-input cache-key :password])}]]
     [:> antd.Button {:type :primary :htmlType :submit} "Register"]
     [debug-cache cache-key]])
  #_(let [cache-key :register
          input @(rf/subscribe [:input cache-key])
          input-errors @(rf/subscribe [:input-errors cache-key])
          error-message @(rf/subscribe [:error-message cache-key])]
      [:> sui.Grid {:textAlign "center" #_:style #_{:height "100%"} :verticalAlign "middle"}
       [:> sui.Grid.Column {:style {:maxWidth 450}}
        [:> sui.Header {:as :h2 :color "teal" :textAlign "center"} "Register for an account"]
        [:> sui.Form {:size      :large
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

(defn home-view []
  [:div
   [:h1 "Home"]])

(defn about-view []
  [:div
   [:h1 "About"]])

(def main-views
  {:home     home-view
   :about    about-view
   :register register-view
   :login    login-view
   :logout   logout-view})

(defn main-view []
  (let [k @(rf/subscribe [::events/main-view])]
    [(get main-views k #(vector :div (str "main-view not found: " k)))]))

(defn header []
  (let [identity @(rf/subscribe [:identity])]
    [:> antd.Layout.Header
     [:> antd.Menu {:theme :dark :mode :horizontal :selectable false
                    :style {:line-height "64px"}}
      [:> antd.Menu.Item
       [:a {:href "#/"}] "Lumbox 3"]
      [:> antd.Menu.Item
       [:a {:href "#/about"}] "About"]
      [:> antd.Menu.SubMenu {:title "Admin"}
       [:> antd.Menu.Item "Users"]
       [:> antd.Menu.Item "Groups"]]
      ; float right appears in opposite order ie. Login Register
      (when-not identity [:> antd.Menu.Item {:style {:float :right}}
                          [:a {:href "#/register"} "Register"]])
      (if identity
        [:> antd.Menu.Item {:style {:float :right}}
         [:a {:href "#/logout"} "Logout"]]
        [:> antd.Menu.Item {:style {:float :right}}
         [:a {:href "#/login"} "Login"]])]]))

(defn footer []
  [:> antd.Layout.Footer {:style {:text-align :center}} "Footer"])

(defn root-view []
  (let [identity @(rf/subscribe [:identity])]
    [:> antd.Layout
     [header]
     [:> antd.Layout.Content {:style {:margin 0 :padding "2em" :border "0px solid black"}}
      [main-view]]
     [footer]]))
