(ns app.main.ui.character-card
  (:require
   [app.main.api :as api]
   [rumext.v2 :as mf]))

(mf/defc character-card*
  "Componente con carta de personaje"
  {::mf/props :obj}
  [{:keys [character]}]
  (let []

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
		         		(if-let [planet (.-originPlanet c)]
							           [:div.card-planet 
							           	[:img.card-planet-small-picture  {:src (.-image planet) :alt (.-name planet)}]
							           	[:p.card-planet-name (.-name planet)]
							           ])
		          ]
		          [:div.card-bottom
		          	[:p.card-force (.-ki c)]
		          	[:p.card-info (.-race c)]
		          ]
		        ]
		      ])

      :else nil)))