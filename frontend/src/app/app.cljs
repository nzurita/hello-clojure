(ns app.app
  (:require
   [app.main.ui :as ui]
   [rumext.v2 :as mf]))

(defonce app-root
  (mf/create-root (.getElementById js/document "app")))

(defn init
  []
  (mf/render! app-root (mf/element ui/app*)))

(defn ^:dev/after-load reload
  []
  (init))
