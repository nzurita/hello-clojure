(ns app.common.schema-character
  (:require
   [malli.core :as m]
   [malli.error :as me]))

;; Schema testimonial: describe la forma de un personaje de carta (CardCharacter).
;; El nombre evita "Character" por colisión con java.lang.Character en la JVM.

(def OriginPlanet
  [:map
   [:id [:or :string :int]]
   [:name :string]
   [:isDestroyed :boolean]
   [:description :string]
   [:image :string]
   [:deletedAt {:optional true} [:maybe :string]]])

;; Formato alineado con app.rpc/main-character y las respuestas de :get-character.
(def CardCharacter
  [:map
   [:id [:or :string :int]]
   [:name [:string {:min 1}]]
   [:image :string]
   [:video {:optional true} :string]
   [:cv {:optional true} :string]
   [:description {:optional true} :string]
   [:ki {:optional true} :string]
   [:programmingKi {:optional true} :string]
   [:race {:optional true} :string]
   [:affiliation {:optional true} :string]
   [:originPlanet {:optional true} OriginPlanet]])

(defn valid?
  "true si `character` cumple el schema, false si no."
  [character]
  (m/validate CardCharacter character))

(defn errors
  "Devuelve un mapa {campo [mensajes]} con errores legibles, o nil si es válido."
  [character]
  (-> (m/explain CardCharacter character)
      (me/humanize)))
