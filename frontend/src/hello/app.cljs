(ns hello.app)

(defn init
  "Punto de entrada del frontend: shadow-cljs la llama al cargar main.js."
  []
  (let [el (.getElementById js/document "app")]
    (set! (.-textContent el) "¡Hola, mundo desde ClojureScript!")))
