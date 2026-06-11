(ns hello.core
  (:require [ring.adapter.jetty :as jetty]))

(defn handler
  "Handler Ring: recibe la petición (un mapa) y devuelve la respuesta (otro mapa)."
  [_request]
  {:status 200
   :headers {"Content-Type" "text/plain; charset=utf-8"}
   :body "¡Hola, mundo!"})

(defn -main
  [& _args]
  (println "Servidor escuchando en http://0.0.0.0:9090")
  (jetty/run-jetty handler {:port 9090 :join? true}))
