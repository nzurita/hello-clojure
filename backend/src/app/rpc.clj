(ns app.rpc
  (:require
   [app.common.schema :as schema]
   [app.common.schema-character :as schema-character]))

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

(def empty-character {:name "-"
                       :avatar "-"
                       :intro-text "-"
                       :cv-path "-"})

(def demo-characters [{:name "Norberto"
                       :avatar "data/img/norberto-avatar.png"
                       :intro-text "Hola, me llamo Norberto"
                       :cv-path "data/cv/norberto-cv.pdf"}
                     {:name "Pedro"
                      :avatar "data/img/pedro-avatar.png"
                      :intro-text "Hola, me llamo Pedro"
                      :cv-path "data/cv/pedro-cv.pdf"}
                     {:name "Marta"
                      :avatar "data/img/marta-avatar.png"
                      :intro-text "Hola, me llamo Marta"
                      :cv-path "data/cv/marta-cv.pdf"}])

   ; (let [id (:id params)
   ;      character (get demo-characters id)]
   ;  (if character
   ;    (if (schema-character/valid? character)
   ;      {:ok true :character character}
   ;      {:ok false :error "invalid character data"})
   ;    {:ok false :error "character not found"})))
  ; (get demo-characters 0))
(defmethod handle :get-character-0
  [_ params]
  {:ok true
   :character (get demo-characters 0)})

(defmethod handle :get-character
  [_ params]
  (let [id (parse-long (get params "id"))]
      (if (and (>= id 0) (< id (count demo-characters)))
          {:ok true
           :character (get demo-characters id)}
          {:ok false
           :error "El personaje solicitado no existe"
           :character empty-character}))
 )

(defmethod handle :get-characters
  [_ params]
  (map-indexed (fn [idx character]
               {:id idx :name (:name character) :avatar (:avatar character)})
             demo-characters)
  )


