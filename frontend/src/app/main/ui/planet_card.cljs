(ns app.main.ui.planet-card
  (:require
   [rumext.v2 :as mf]))

(mf/defc planet-card*
  "Componente con carta de planeta"
  {::mf/props :obj}
  [{:keys [planet]}]
  (let [^js p planet]
    (when p
         [:div.dragonball-card.planet-card
             [:div.card-frame
               [:div.picture-frame
                 [:img.card-picture {:src (.-image p) :alt (.-name p)}]
               ]
               [:h4.card-title (.-name p)]
               [:div.card-description
                 [:p (.-description p)]
               ]
               [:div.card-bottom
                 "-"
               ]
             ]
           ]
    )
  ))