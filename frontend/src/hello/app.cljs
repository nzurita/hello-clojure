(ns hello.app)

(defn mostrar
  "Escribe un texto en el div #app del DOM."
  [texto]
  (set! (.-textContent (.getElementById js/document "app")) texto))

(defn init
  "Punto de entrada: shadow lo llama UNA vez al cargar (vía :init-fn)."
  []
  (-> (js/fetch "/api/hello")
      (.then (fn [response] (.text response)))
      (.then (fn [texto] (mostrar (str "El backend dice: " texto))))
      (.catch (fn [err] (mostrar (str "Error al contactar el backend: " err))))))

(defn ^:dev/after-load reload
  "shadow-cljs llama a esta función tras CADA recompilación en caliente.
   Sin ella, shadow redefine las funciones pero el DOM no se vuelve a pintar,
   porque a `init` solo se le llama una vez al arrancar."
  []
  (init))
