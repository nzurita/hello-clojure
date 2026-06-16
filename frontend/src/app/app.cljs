(ns app.app
  (:require
   [clojure.string :as str]
   ;; ¡El MISMO namespace que usa el backend! Vive en common/ (.cljc).
   [app.common.schema :as schema]))

;; Las MISMAS personas de ejemplo que valida el backend.
(def ejemplos
  [{:nombre "Norberto" :email "norberto@example.com" :edad 40}
   {:nombre ""         :email "no-es-un-email"        :edad -3}])

(defn- linea
  "Resultado de validar una persona, usando el schema compartido."
  [p]
  (if (schema/valida? p)
    (str (pr-str p) "  =>  VÁLIDA")
    (str (pr-str p) "  =>  INVÁLIDA: " (pr-str (schema/errores p)))))

(defn- validacion-cliente []
  (str/join "\n" (map linea ejemplos)))

(defn- pinta [html]
  (set! (.-innerHTML (.getElementById js/document "app")) html))

(defn- render
  "Pinta dos bloques: validación hecha en el navegador y la del backend."
  [respuesta-backend]
  (pinta
   (str "<h2>Validación en el NAVEGADOR (schema de common/)</h2>"
        "<pre>" (validacion-cliente) "</pre>"
        "<h2>Validación en el BACKEND (mismo schema)</h2>"
        "<pre>" respuesta-backend "</pre>")))

(defn init
  "Valida en el cliente al instante y, además, pide al backend su validación
   para comprobar que ambos lados dan EXACTAMENTE el mismo resultado."
  []
  (render "(cargando…)")
  (-> (js/fetch "/api/app")
      (.then (fn [resp] (.text resp)))
      (.then (fn [texto] (render texto)))
      (.catch (fn [err] (render (str "Error al contactar el backend: " err))))))

(defn ^:dev/after-load reload []
  (init))
