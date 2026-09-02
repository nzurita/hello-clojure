(ns app.main.ui.app-status
  (:require
   [rumext.v2 :as mf]))

(def app-status-size 10)

(defn push-status!
  [app-status entry]
  (swap! app-status
         (fn [xs]
           (let [cleaned (if (= :tmp (:type (first xs)))
                           (rest xs)
                           xs)]
             (take app-status-size (conj cleaned entry))))))

(mf/defc app-status*
  {::mf/props :obj}
  [{:keys [app-status]}]
  (let [app-status-expanded (mf/use-state false)]
    [:footer.status-bar
              {:class (when @app-status-expanded "expanded")}
            [:button {:class "btn-expand-collapse" :on-click #(swap! app-status-expanded not)}]
           [:ul.status-list
                (for [[idx msg] (map-indexed vector app-status)]
                  (let [type (name (:type msg))]
                  [:li {:key idx :class type}
                   [:span (:msg msg)]]))
                ]
         ]
  ))