(ns hello.core
  (:require
   [clojure.string :as str]
   [ring.adapter.jetty :as jetty]
   ;; ¡Código que vive en el módulo common/ y que también usará el frontend!
   [hello.common.schema :as schema]))

;; Dos personas de ejemplo: la primera cumple el schema, la segunda no.
(def ejemplos
  [{:nombre "Norberto" :email "norberto@example.com" :edad 40}
   {:nombre ""         :email "no-es-un-email"        :edad -3}])

(defn- linea
  "Texto con el resultado de validar una persona contra el schema."
  [persona]
  (if (schema/valida? persona)
    (str persona "  =>  VÁLIDA")
    (str persona "  =>  INVÁLIDA: " (schema/errores persona))))

(defn handler
  "Handler Ring: recibe la petición (un mapa) y devuelve la respuesta (otro mapa)."
  [_request]
  {:status 200
   :headers {"Content-Type" "text/plain; charset=utf-8"}
   :body (str "Validación con el schema de common/ (Malli):\n\n"
              (str/join "\n" (map linea ejemplos)))})

(defn -main
  [& _args]
  (println "Servidor escuchando en http://0.0.0.0:9090")
  (jetty/run-jetty handler {:port 9090 :join? true}))
