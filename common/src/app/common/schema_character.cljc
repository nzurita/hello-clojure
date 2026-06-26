(ns app.common.schema-character
  (:require
   [malli.core :as m]
   [malli.error :as me]))

;; Un schema de Malli es simplemente un DATO (un vector), no una clase con
;; anotaciones. Esto describe la "forma" válida de una persona.
(def TaleCharacter
  [:map
   [:name [:string {:min 1}]]
   [:avatar :string]
   [:intro-text :string]
   [:cv-path :string]])

(defn valid?
  "true si `character` cumple el schema, false si no."
  [character]
  (m/validate TaleCharacter character))

(defn errors
  "Devuelve un mapa {campo [mensajes]} con errores LEGIBLES, o nil si es válida.
   (m/explain da el detalle técnico; me/humanize lo traduce a mensajes.)"
  [character]
  (-> (m/explain TaleCharacter character)
      (me/humanize)))
