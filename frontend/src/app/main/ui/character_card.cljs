(ns app.main.ui.character-card
  (:require
   [app.main.api :as api]
   [rumext.v2 :as mf]))

(mf/defc character-card*
  "Componente con carta de personaje"
  {::mf/props :obj}
  [{:keys [character]}]
  (let [video-open? (mf/use-state false)]
    (cond
      (nil? character)
      [:p {:style {:color "var(--color-fg-secondary)"}}
       "Selecciona un personaje de la lista."]

      ; (cond (not (nil? (.-originPlanet c))) {:background-image (str "url(\"" (.-image (.-originPlanet c)) "\")")} :else {} )
      character
      (let [^js c character]
        [:div.dragonball-card.character-card
          [:div.card-frame
           [:div.picture-frame
            {:style (if-let [planet-image (some-> (.-originPlanet c) .-image)]
                  #js {:backgroundImage (str "url(\"" planet-image "\")")}
                  #js {})}
             [:img.card-picture {:src (.-image c) :alt (.-name c)}]
            ]
            [:h4.card-title (.-name c)]
            [:div.card-description
             [:p (.-description c)]
            ]
            (if-let [video (.-video c)]
               [:div.card-secondary-info
                 [:button.video-play
                    {:on-click #(reset! video-open? true)
                     :title "Ver vídeo presentación"} "▶"]
                 (when @video-open?
                    [:div.video-overlay
                     [:div.video-modal
                      [:button.btn-video-close
                         {:on-click #(reset! video-open? false)} "✕"]
                      [:video {:src video
                               :controls true
                               :autoPlay true
                               :playsInline true}]]])
                 (when-let [cv (.-cv c)]
                    [:a.btn.cv-download
                     {:href cv
                      :title "Ver currículum"
                      :target "_blank"
                      :rel "noopener noreferrer"}
                     "CV"])
               ]
             )
            [:div.card-secondary-info
              (if-let [planet (.-originPlanet c)]
                  [:div.card-planet 
                   [:img.card-planet-small-picture  {:src (.-image planet) :alt (.-name planet)}]
                   [:p.card-planet-name (.-name planet)]
                  ])
              [:p.card-race (.-race c)]
            ]
            [:div.card-bottom
             [:div.card-force
               [:p.card-force-label "lucha"]
               [:p (.-ki c)]
             ]
             [:div.card-force
               [:p.card-force-label "programación"]
               [:p (.-programmingKi c)]
             ]
            ]
          ]
        ])

      :else nil)))