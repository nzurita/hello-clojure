;; Arranque de DESARROLLO (lo lanza la ventana "backend" de tmux):
;;   1. Levanta un servidor nREPL en 0.0.0.0:6064 -> conectas tu editor aquí.
;;   2. Arranca el servidor web (user/start) -> /api/rpc/command/... responde de inmediato.
;;   3. Bloquea el proceso para que tmux mantenga la ventana viva.
;; El fichero se llama dev_main.clj porque en Clojure el guion del namespace
;; (dev-main) se convierte en guion bajo en la ruta del fichero.
(ns dev-main
  (:require
   [clojure.main :as main]
   [nrepl.server :as nrepl]
   [cider.nrepl :refer [cider-nrepl-handler]]
   [rebel-readline.core :as rebel]
   [rebel-readline.clojure.line-reader :as rebel-reader]
   [rebel-readline.clojure.service.local :as rebel-service]
   [rebel-readline.clojure.main :as rebel-main]
   [user :as u]))

(defn -main [& _args]
  (nrepl/start-server :bind "0.0.0.0" :port 6064 :handler cider-nrepl-handler)
  (println "nREPL escuchando en 0.0.0.0:6064 (conecta tu editor aquí)")
  (u/start)
  (println "Backend listo. En este REPL: (reload) recarga el código · (restart) reinicia el servidor.")
  ;; REPL interactivo con rebel-readline (historial, flechas, autocierre...).
  ;; Arranca en el namespace 'user, donde están (reload)/(start)/(stop)/(restart).
  (rebel/with-line-reader
    (rebel-reader/create (rebel-service/create))
    (main/repl
     :init #(in-ns 'user)
     :prompt (fn [])
     :read (rebel-main/create-repl-read))))
