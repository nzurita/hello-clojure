(ns app.rpc
  (:require
   [clj-http.client :as http]))

;; Patrón RPC de Penpot: un defmulti despacha por NOMBRE de comando.
;; Cada defmethod = un "endpoint" (~ una acción de controller / message handler).
;; El cliente llama "api/fetch-characters" y el runtime enruta aquí.

(defmulti handle (fn [method _params] method))

(defmethod handle :default
  [method _params]
  {:ok false :error (str "comando desconocido: " (name method))})

(def empty-character {:id nil
                       :name nil
                       :image nil
                       :video nil
                       :cv nil
                       :description nil
                       :ki nil
                       :race nil
                       :affiliation nil
                       :originPlanet {:id nil
                                      :name nil
                                      :isDestroyed nil
                                      :description nil
                                      :image nil
                                      :deletedAt nil
                                                          }
                       :cv-path nil})

(def empty-planet {:id nil :name nil :image nil})

(def main-character {:id "0"
                       :name "Norberto"
                       :image "data/img/main-character-4.png"
                       :video "data/main-character-presentation.mp4"
                       :cv "data/main-character-cv.pdf"
                       :description "Ingeniero de software con 25 años de experiencia, los últimos años ha trabajado por cuenta propia principalmente con PHP Symfony y Microsoft .NET. En empresas anteriores también ha trabajado con Java, Oracle Forms, PL/SQL, ASP clásico, C y C++."
                       :ki "1.000"
                       :programmingKi "50.000.000"
                       :race "Human"
                       :affiliation "Clojure Team"
                       :originPlanet {:id "2"
                                      :name "Tierra"
                                      :isDestroyed false
                                      :description "La Tierra también llamado Mundo del Dragón (Dragon World), es el planeta principal donde se desarrolla la serie de Dragon Ball. Se encuentra en el Sistema Solar de la Vía Láctea de las Galaxias del Norte del Universo 7, lugar que supervisa el Kaio del Norte, y tiene su equivalente en el Universo 6. El hogar de los terrícolas y los Guerreros Z. Ha sido atacado en varias ocasiones por enemigos poderosos."
                                      :image "https://dragonball-api.com/planetas/Tierra_Dragon_Ball_Z.webp"
                                      :deletedAt nil
                                                          }
                       :cv-path "data/cv/norberto-cv.pdf"}
                     )

(def dragonball-api-base "https://dragonball-api.com/api/")

(defn- first-sentence [s]
  (if (string? s)
    (or (re-find #"^(?:Dr\.|[^.])*\." s) s)
    s))

(defn fetch-characters
  "Pide la lista de personajes a la API externa. Devuelve el mapa de datos
   ya parseado (con :items y :meta), tal cual lo entrega la API."
  [page]
  (let [resp (http/get (str dragonball-api-base "characters?page=" page) {:as :json})]
    (:body resp)))

(defmethod handle :get-characters
  [_ params]
  (let [page (parse-long (get params "page"))
        resp (fetch-characters page)]
  		{:ok true
     :characters (map (fn [character]
                         {:id (:id character)
                          :name (:name character)
                          :image (:image character)})
                       (if (= page 1)
                       		(cons main-character (:items resp))
                       		(:items resp)))
     :meta {:totalItems (:totalItems (:meta resp))
             :itemCount (:itemCount (:meta resp))
             :itemsPerPage (:itemsPerPage (:meta resp))
             :totalPages (:totalPages (:meta resp))
             :currentPage (:currentPage (:meta resp))}}))


(defn fetch-character
  "Pide un personaje por id a la API externa. Devuelve el mapa de datos
   si existe, o nil si la API responde 404 (no encontrado)."
  [id]
  (let [resp (http/get (str dragonball-api-base "characters/" id)
                        {:as :json :throw-exceptions false})]
    (when (= 200 (:status resp))
      (:body resp))))

(defn- prepare-character
  "Normaliza un personaje de la API antes de devolverlo al frontend."
  [{:keys [race] :as character}]
  (let [android? (= "Android" race)]
    (-> character
        (update :description first-sentence)
        (update :description str (if android?
                                   " Grandes dotes en desarrollo de software aunque tiene antecedentes violentos en trabajos anteriores."
                                   " No sabe programar."))
        (assoc :programmingKi (if android? "60.000.000" "0")))))

(defmethod handle :get-character
  [_ params]
  (let [id        (parse-long (get params "id"))
        character (if (not= 0 id)
                    (fetch-character id)
                    main-character)
        character (if (and character (not= 0 id))
                    (prepare-character character)
                    character)]
  (if character
    {:ok true :character character}
    {:ok false :error "El personaje solicitado no existe" :character empty-character})))

(defn fetch-planet
  "Pide un planeta por id a la API externa. Devuelve el mapa de datos
   si existe, o nil si la API responde 404 (no encontrado)."
  [id]
  (let [resp (http/get (str dragonball-api-base "planets/" id)
                        {:as :json :throw-exceptions false})]
    (when (= 200 (:status resp))
      (:body resp))))

(defmethod handle :get-planet
  [_ params]
  (let [id      (parse-long (get params "id"))
        planet  (fetch-planet id)]
    (if planet
      {:ok true
       :planet planet}
      {:ok false
       :error "El planeta solicitado no existe"
       :planet empty-planet})))