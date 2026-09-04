;; Namespace "user": Clojure lo carga automáticamente al iniciar el REPL si
;; está en el classpath (aquí, vía :extra-paths ["dev"] del alias :dev).
;; Es el sitio idiomático para utilidades de desarrollo (start/stop/reload).
(ns user
  (:require
   [ring.adapter.jetty :as jetty]
   [app.core :as core]))

;; Guardamos la instancia del servidor Jetty para poder pararla luego.
;; defonce = "define solo si no existe", para no perder el valor al recargar.
(defonce ^:private server (atom nil))

(defn stop
  "Para el servidor web si está arrancado."
  []
  (when-let [s @server]
    (.stop s)
    (reset! server nil)
    (println "Backend detenido.")))

(defn start
  "Arranca el servidor web en el puerto 9090.
   Usa #'core/handler (la VAR, no el valor): así, si rediseñas el handler en
   el REPL, el servidor usa la nueva versión sin reiniciar (recarga en caliente)."
  []
  (stop)
  (reset! server (jetty/run-jetty #'core/handler {:port 9090 :join? false}))
  (println "Backend escuchando en http://0.0.0.0:9090"))

(defn restart
  "Para y vuelve a arrancar el servidor."
  []
  (stop)
  (start))

(defn reload
  "Recarga app.core desde disco (lo recompila). Como el servidor usa
   #'core/handler (la VAR), el cambio se aplica al instante SIN reiniciar.
   Es el equivalente backend al hot-reload del frontend, pero disparado por ti."
  []
  (require 'app.common.schema-character
           'app.rpc
           'app.http
           'app.core
           :reload)
  (println "app.common.schema-character, app.rpc, app.http y app.core recargados."))
