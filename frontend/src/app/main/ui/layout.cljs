(ns app.main.ui.layout
  (:require
   [app.main.api :as api]
   [app.main.ui.character-list :refer [character-list*]]
   [app.main.ui.character-card :refer [character-card*]]
   [app.main.ui.planet-card :refer [planet-card*]]
   [rumext.v2 :as mf]))

(mf/defc layout*
  []
  (let [selected-character-id (mf/use-state nil)
        character             (mf/use-state nil)
        selected-planet-id    (mf/use-state nil)
        planet                (mf/use-state nil)
        loading?              (mf/use-state false)
        error                 (mf/use-state nil)
        loading-planet?       (mf/use-state false)
        error-planet          (mf/use-state nil)]

    (mf/with-effect [selected-character-id]
      (when @selected-character-id
        (reset! loading? true)
        (reset! error nil)
        (-> (api/get-character @selected-character-id)
            (.then (fn [^js data]
                     (reset! loading? false)
                     (if (.-ok data)
                       (do
                           (reset! character (.-character data))
                           (reset! selected-planet-id (.-id (.-originPlanet (.-character data))) ))
                       (reset! error (.-error data)))))
            (.catch (fn [err]
                      (reset! loading? false)
                      (reset! error (str err)))))))

    (mf/with-effect [selected-planet-id]
      (when @selected-planet-id
        (reset! loading-planet? true)
        (reset! error-planet nil)
        (-> (api/get-planet @selected-planet-id)
            (.then (fn [^js data]
                     (reset! loading-planet? false)
                     (if (.-ok data)
                       (reset! planet (.-planet data))
                       (reset! error-planet (.-error data)))))
            (.catch (fn [err]
                      (reset! loading-planet? false)
                      (reset! error-planet (str err)))))))


    [:div.app-shell
     [:aside.sidebar
      [:section.sidebar-section.sidebar-section--list
       [:h2 {:style {:font-size "0.9rem"
                     :color "var(--color-fg-secondary)"}}
        "Characters"]
       [:> character-list* {:selected-character-id @selected-character-id
                            :on-select #(reset! selected-character-id %)}]]

      [:footer.status-bar
       "Ready"]]

      [:main.main-panel
        [:div.character-wrapper
          [:> character-card* {:character @character}]
        ]
        [:div.planet-wrapper
          [:> planet-card* {:planet @planet}]
        ]
      ]
     ]))