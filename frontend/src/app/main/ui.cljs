; ui.cljs
(ns app.main.ui
  (:require
   [app.main.ui.layout :refer [layout*]]
   [rumext.v2 :as mf]))

(mf/defc app*
  []
  [:> layout*])
