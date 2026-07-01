; layout.cljs
(ns app.main.ui.layout
  (:require
   [rumext.v2 :as mf]))

(mf/defc layout*
  []
  [:div.app-shell
   [:aside.sidebar
    [:section.sidebar-section.sidebar-section--list
     [:h2 {:style {:font-size "0.9rem"
                   :color "var(--color-fg-secondary)"}}
      "Characters"]
     [:p "List coming soon…"]]

    [:section.sidebar-section
     [:h2 {:style {:font-size "0.9rem"
                   :color "var(--color-fg-secondary)"}}
      "Validate person (POST)"]
     [:p "Form coming soon…"]]

    [:footer.status-bar
     "Ready"]]

   [:main.main-panel
    [:h1 {:style {:font-size "1.25rem" :margin-top 0}}
     "Select a character"]
    [:p {:style {:color "var(--color-fg-secondary)"}}
     "Main panel — detail and RPC messages will appear here."]]])