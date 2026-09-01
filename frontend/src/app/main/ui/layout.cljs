(ns app.main.ui.layout
  (:require
   [app.main.api               :as api]
   [app.main.ui.splash-screen  :refer [splash-screen*]]
   [app.main.ui.app-status     :refer [push-status! app-status*]]
   [app.main.ui.character-list :refer [character-list*]]
   [app.main.ui.character-card :refer [character-card*]]
   [app.main.ui.planet-card    :refer [planet-card*]]
   [rumext.v2                  :as mf]))

(mf/defc layout*
  []
  (let [splash-screen-active? (mf/use-state true)
        selected-character-id (mf/use-state nil)
        character             (mf/use-state nil)
        selected-planet-id    (mf/use-state nil)
        planet                (mf/use-state nil)
        loading?              (mf/use-state false)
        loading-planet?       (mf/use-state false)
        app-status            (mf/use-state (list {:type :info :msg "Ready"}))]

    (mf/with-effect [selected-character-id]
      (when @selected-character-id
        (reset! loading? true)
        (push-status! app-status {:type :info :msg "Cargando"})

        (-> (api/get-character @selected-character-id)
            (.then (fn [^js data]
                     (reset! loading? false)
                     (if (.-ok data)
                       (do
                           (reset! character (.-character data))
                              (push-status! app-status {:type :info :msg (str "Cargado personaje " (.-id (.-character data)))})

                           (reset! selected-planet-id (.-id (.-originPlanet (.-character data))) ))
                       (push-status! app-status {:type :error :msg (.-error data)}) )))
            (.catch (fn [err]
                      (reset! loading? false)
                       (push-status! app-status {:type :error :msg (str err)}) )))))

    (mf/with-effect [selected-planet-id]
      (when @selected-planet-id
        (reset! loading-planet? true)
        (push-status! app-status {:type :info :msg "Cargando"})
        (-> (api/get-planet @selected-planet-id)
            (.then (fn [^js data]
                     (reset! loading-planet? false)
                     (if (.-ok data)
                       (do
                           (reset! planet (.-planet data))
                           (push-status! app-status {:type :info :msg (str "Cargado planeta " (.-id (.-planet data)))}))
                       (push-status! app-status {:type :error :msg (.-error data)}) )))
            (.catch (fn [err]
                      (reset! loading-planet? false)
                       (push-status! app-status {:type :error :msg (str err)}) )))))


    [:div
      [:> splash-screen* {:splash-active @splash-screen-active? :on-close-splash #(reset! splash-screen-active? false)}]
      [:div.app-shell
       [:aside.sidebar
        [:section.sidebar-section.sidebar-section--list
         [:h2 {:style {:font-size "0.9rem"
                       :color "var(--color-fg-secondary)"}}
          "Characters"]
         [:> character-list* {:selected-character-id @selected-character-id
                              :on-select #(reset! selected-character-id %)}]]
         [:> app-status* {:app-status @app-status}]
       ]

       [:main.main-panel
          [:button.btn-about {:on-click #(reset! splash-screen-active? true)} "?"]
          [:div.character-wrapper
            [:> character-card* {:character @character}]
          ]
          [:div.planet-wrapper
            [:> planet-card* {:planet @planet}]
          ]
       ]
      ]
    ]))