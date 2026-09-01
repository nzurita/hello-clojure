(ns app.main.ui.splash-screen
  (:require
   [rumext.v2 :as mf]))

(def app-info {:author "by Norberto Zurita" :name "Hello World!" :description "Hola mundo! Hablando en Clojure! Este es un pequeño proyecto de presentación y práctica de Clojure."})

(mf/defc splash-screen*
  {::mf/props :obj}
  [{:keys [splash-active on-close-splash]}]
  [:div.splash-overlay {:style {:display (when (not splash-active) "none")}}
   [:div.splash-box
    [:h1.splash-title (:name app-info)]
    [:h2.splash-author (:author app-info)]
    [:p.splash-description
     (:description app-info)]
    [:a.splash-enter {:href "#" :on-click on-close-splash} "Entrar"]]]
  )