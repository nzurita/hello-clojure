(ns app.http
  (:require
   [cheshire.core :as json]
   [clojure.string :as str]
   [app.common.schema :as schema]
   [app.rpc :as rpc]
   [reitit.ring :as ring]))

;; --- Utilidades JSON ---------------------------------------------------------

(defn- json-response
  [status body]
  {:status status
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/generate-string body)})

(defn- read-json-body
  "Lee el cuerpo de la petición Ring como mapa (Jetty devuelve HttpInput)."
  [request]
  (when-let [body (:body request)]
    (cond
      (map? body) body
      (string? body) (if (empty? body) {} (json/parse-string body true))
      :else (let [text (slurp body)]
              (if (empty? text) {} (json/parse-string text true))))))

;; --- Handlers ----------------------------------------------------------------

(defn- app-handler
  "Ruta legacy de las fases 6–7: demo de validación en texto plano."
  [_request]
  (let [ejemplos [{:nombre "Norberto" :email "norberto@example.com" :edad 40}
                  {:nombre ""         :email "no-es-un-email"        :edad -3}]
        linea (fn [p]
                (if (schema/valida? p)
                  (str p "  =>  VÁLIDA")
                  (str p "  =>  INVÁLIDA: " (schema/errores p))))]
    {:status 200
     :headers {"Content-Type" "text/plain; charset=utf-8"}
     :body (str "Validación con el schema de common/ (Malli):\n\n"
                (str/join "\n" (map linea ejemplos)))}))

(defn- rpc-handler
  "Entrada RPC (~ Penpot /api/rpc/command/:method-name).
   POST con JSON en el body; el nombre del comando va en la URL."
  [request]
  (let [method-name (get-in request [:path-params :method-name])
        method-kw (keyword method-name)]
    (try
      (let [params (or (read-json-body request) {})
            result (rpc/handle method-kw params)]
        (if (contains? (methods rpc/handle) method-kw)
          (json-response 200 result)
          (json-response 404 result)))
      (catch Exception e
        (json-response 400 {:ok false :error (.getMessage e)})))))

;; --- Router ------------------------------------------------------------------

(def routes
  ["/api"
   ["/app" {:get app-handler}]
   ["/rpc/command/:method-name" {:post rpc-handler}]])

(def router
  (ring/router routes))

(def handler
  (ring/ring-handler
   router
   (ring/routes
    (ring/create-resource-handler {:path "/"})
    (ring/create-default-handler))))
