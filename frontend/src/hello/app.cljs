(ns hello.app)

(defn mostrar
  "Escribe un texto en el div #app del DOM."
  [texto]
  (set! (.-textContent (.getElementById js/document "app")) texto))

(defn init
  "Punto de entrada: pide /api/hello al backend y muestra la respuesta."
  []
  (-> (js/fetch "/api/hello")
      (.then (fn [resp] (.text resp)))
      (.then (fn [texto] (mostrar (str "El backend dice: " texto))))
      (.catch (fn [err] (mostrar (str "Error al contactar el backend: " err))))))
