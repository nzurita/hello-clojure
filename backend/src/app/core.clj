(ns app.core
  (:require
   [app.http :as http]
   [ring.adapter.jetty :as jetty]))

(defn handler
  "Handler Ring principal: delega en el router de reitit (app.http)."
  [request]
  (http/handler request))

(defn -main
  [& _args]
  (println "Servidor escuchando en http://0.0.0.0:9090")
  (jetty/run-jetty handler {:port 9090 :join? true}))
