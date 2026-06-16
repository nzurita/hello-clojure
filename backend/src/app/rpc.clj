(ns app.rpc
  (:require
   [app.common.schema :as schema]))

;; Patrón RPC de Penpot: un defmulti despacha por NOMBRE de comando.
;; Cada defmethod = un "endpoint" (~ una acción de controller / message handler).
;; El cliente llama "saludar-persona" y el runtime enruta aquí.

(defmulti handle (fn [method _params] method))

(defmethod handle :default
  [method _params]
  {:ok false :error (str "comando desconocido: " (name method))})

(defmethod handle :saludar-persona
  [_ params]
  (if (schema/valida? params)
    {:ok true
     :mensaje (str "Hola, " (:nombre params) " (" (:edad params) " años)")}
    {:ok false :errores (schema/errores params)}))
