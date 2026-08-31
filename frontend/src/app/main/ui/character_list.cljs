(ns app.main.ui.character-list
  (:require
   [app.main.api :as api]
   [rumext.v2 :as mf]))

(mf/defc character-list*
  {::mf/props :obj}
  [{:keys [selected-id on-select]}]
  (let [characters (mf/use-state [])
        loading?   (mf/use-state true)
        error      (mf/use-state nil)]

    (mf/with-effect []
      (-> (api/get-characters)
          (.then (fn [^js data]
                   (reset! loading? false)
                   (if (.-ok data)
                     (reset! characters (.-characters data))
                     (reset! error "No se pudieron cargar los personajes"))))
          (.catch (fn [err]
                    (reset! loading? false)
                    (reset! error (str err))))))

    [:div
     (cond
       @loading? [:p "Cargando…"]
       @error    [:p @error]
       :else
       [:ul.character-list
        (for [^js c @characters]
          [:li
           {:key (.-id c)
            :class (str "character-item" (when (= selected-id (.-id c)) " selected"))
            :on-click #(on-select (.-id c))}
           [:img.profile-picture.profile-picture-thumbnail
            {:src (.-image c) :alt (.-name c)}]
           [:span.character-name (.-name c)]])])]))