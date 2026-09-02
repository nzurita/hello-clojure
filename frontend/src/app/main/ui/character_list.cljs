(ns app.main.ui.character-list
  (:require
   [app.main.api :as api]
   [rumext.v2 :as mf]))

(mf/defc character-list*
  {::mf/props :obj}
  [{:keys [characters list-meta-info on-page-click selected-character-id on-select]}]

    [:div
       [:ul.character-list
        (for [^js c characters]
          [:li
           {:key (.-id c)
            :class (str "character-item" (when (= selected-character-id (.-id c)) " selected"))
            :on-click #(on-select (.-id c))}
           [:img.profile-picture.profile-picture-thumbnail
            {:src (.-image c) :alt (.-name c)}]
           [:span.character-name (.-name c)]])
       ]
       [:div.list-pagination
         [:ul.list-pages
        (for [p (range 1 (inc (:totalPages list-meta-info)))]
                [:li
                    {:key p
                      :class (when (= p (:currentPage list-meta-info)) " selected")
                      :on-click #(on-page-click p)} p])
         ]
        ]
      ])