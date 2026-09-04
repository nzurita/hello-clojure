(ns app.http
  (:require
   [cheshire.core :as json]
   [app.rpc :as rpc]
   [reitit.ring :as ring]
   [reitit.ring.middleware.parameters :as parameters]))

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

(defn- rpc-handler
  "Entrada RPC: /api/rpc/command/:method-name.
   GET sin body o POST con JSON; el nombre del comando va en la URL."
  [request]
  (let [method-name (get-in request [:path-params :method-name])
        method-kw (keyword method-name)]
    (try
      (let [params (if (= (:request-method request) :get)
                     (:query-params request)
                     (or (read-json-body request) {}))
            result (rpc/handle method-kw params)]
        (if (contains? (methods rpc/handle) method-kw)
          (json-response 200 result)
          (json-response 404 result)))
      (catch Exception e
        (json-response 400 {:ok false :error (str method-name " " (.getMessage e))})))))

;; --- Router ------------------------------------------------------------------

(def routes
  ["/api"
   ["/rpc/command/:method-name" {:get rpc-handler :post rpc-handler}]])

(def router
  (ring/router routes))

(def handler
  (ring/ring-handler
   router
   (ring/routes
    (ring/create-resource-handler {:path "/"})
    (ring/create-default-handler))
   {:middleware [parameters/parameters-middleware]}))
