(ns app.main.ui.layout
  (:require
   [rumext.v2                  :as mf]
   [app.main.api               :as api]
   [app.main.ui.splash-screen  :refer [splash-screen*]]
   [app.main.ui.app-status     :refer [push-status! app-status*]]
   [app.main.ui.character-list :refer [character-list*]]
   [app.main.ui.character-card :refer [character-card*]]
   [app.main.ui.planet-card    :refer [planet-card*]]))

(def splash-seen-key "hello-clojure-splash-seen")

(def default-list-meta-info {:totalItems 0
                             :itemCount 0
                             :itemsPerPage 10
                             :totalPages 0
                             :currentPage 1})

(mf/defc layout*
  []
  (let [splash-screen-active? (mf/use-state (nil? (.getItem js/localStorage splash-seen-key)))
        current-page          (mf/use-state "1")
        list-meta-info        (mf/use-state default-list-meta-info)
        characters            (mf/use-state [])
        selected-character-id (mf/use-state nil)
        character             (mf/use-state nil)
        selected-planet-id    (mf/use-state nil)
        planet                (mf/use-state nil)
        app-status            (mf/use-state (list {:type :info :msg "Ready"}))]

    (mf/with-effect [current-page]
      (-> (api/get-characters @current-page)
          (.then (fn [^js data]
        (push-status! app-status {:type :tmp :msg (str "Cargando personajes página " @current-page)})
                   (if (.-ok data)
                     (do (reset! characters (.-characters data))
                         (reset! list-meta-info (js->clj (.-meta data) :keywordize-keys true)) 
                         (push-status! app-status {:type :info :msg (str "Cargados personajes página " @current-page)})
                     )
                     (push-status! app-status {:type :error :msg "No se pudieron cargar los personajes."}) )))
          (.catch (fn [err]
                     (push-status! app-status {:type :error :msg (str err)}) ))))

    (mf/with-effect [selected-character-id]
      (when @selected-character-id
        (push-status! app-status {:type :tmp :msg "Cargando personaje"})

        (-> (api/get-character @selected-character-id)
            (.then (fn [^js data]
                     (if (.-ok data)
                       (do
                           (reset! character (.-character data))
                              (push-status! app-status {:type :info :msg (str "Cargado personaje " (.-id (.-character data)))})

                           (reset! selected-planet-id (.-id (.-originPlanet (.-character data))) ))
                       (push-status! app-status {:type :error :msg (.-error data)}) )))
            (.catch (fn [err]
                      (push-status! app-status {:type :error :msg (str err)}) )))))

    (mf/with-effect [selected-planet-id]
      (when @selected-planet-id
        (push-status! app-status {:type :tmp :msg "Cargando planeta"})
        (-> (api/get-planet @selected-planet-id)
            (.then (fn [^js data]
                     (if (.-ok data)
                       (do
                           (reset! planet (.-planet data))
                           (push-status! app-status {:type :info :msg (str "Cargado planeta " (.-id (.-planet data)))}))
                       (push-status! app-status {:type :error :msg (.-error data)}) )))
            (.catch (fn [err]
                      (push-status! app-status {:type :error :msg (str err)}) )))))

    [:div.app-wrapper
      [:> splash-screen* {:splash-active @splash-screen-active? :on-close-splash #(do
                                                                                    (.setItem js/localStorage splash-seen-key "1")
                                                                                    (reset! splash-screen-active? false))}]
      [:div.app-shell
       [:aside.sidebar
        [:section.sidebar-section.sidebar-section-list
         [:h2 {:style {:font-size "0.9rem"
                       :color "var(--color-fg-secondary)"}}
          "Characters"]
         [:> character-list* {:characters @characters
                              :list-meta-info @list-meta-info
                              :selected-character-id @selected-character-id
                              :on-select #(reset! selected-character-id %)
                              :on-page-click #(reset! current-page %)}]]
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