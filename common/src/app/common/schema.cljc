;; Fichero .cljc => se compila para AMBAS plataformas:
;;   - JVM  (lo usará el backend, .clj)
;;   - JS   (lo usará el frontend, .cljs)  <- esto lo aprovecharemos en la Fase 7
;;
;; Namespace bajo app.common.* igual que Penpot usa app.common.*
(ns app.common.schema
  (:require
   [malli.core :as m]
   [malli.error :as me]))

;; Un schema de Malli es simplemente un DATO (un vector), no una clase con
;; anotaciones. Esto describe la "forma" válida de una persona.
(def Persona
  [:map
   [:nombre [:string {:min 1}]]
   [:email  [:re #"^[^@\s]+@[^@\s]+\.[^@\s]+$"]]
   [:edad   [:int {:min 0 :max 150}]]])

(defn valida?
  "true si `persona` cumple el schema, false si no."
  [persona]
  (m/validate Persona persona))

(defn errores
  "Devuelve un mapa {campo [mensajes]} con errores LEGIBLES, o nil si es válida.
   (m/explain da el detalle técnico; me/humanize lo traduce a mensajes.)"
  [persona]
  (-> (m/explain Persona persona)
      (me/humanize)))
