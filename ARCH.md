# Arquitectura Penpot

```
                    NAVEGADOR (cliente)
                          │
                          │  HTTP / WebSocket
                          ▼
        ┌─────────────────────────────────────┐
        │  Contenedor FRONTEND (nginx)         │
        │  - Sirve el SPA (JS/CSS compilados)  │
        │  - Hace de reverse proxy:            │
        │       /api  → backend                │
        │       /ws   → backend (websockets)   │
        │       export/mcp → otros servicios   │
        └─────────────────────────────────────┘
                 │            │
        /api,/ws │            │ export
                 ▼            ▼
        ┌──────────────┐  ┌──────────────┐
        │   BACKEND    │  │  EXPORTER    │
        │ Clojure JVM  │  │ CLJS/Node    │
        │ Integrant    │  │ Playwright   │
        │ reitit + RPC │  └──────────────┘
        └──────────────┘
           │     │     │
           ▼     ▼     ▼
      Postgres  Redis  Object Storage
                (Valkey) (fs / S3)

```
## Las piezas y su equivalente "mental" en Symfony

- **Frontend (frontend/)**: un SPA escrito en ClojureScript, compilado a JavaScript con shadow-cljs (≈ Webpack). En producción son archivos estáticos servidos por nginx. Equivale al "front" de una SPA cualquiera (Vue/React) que consume una API.
- **Backend (backend/)**: app Clojure sobre la JVM. Sus conceptos clave:
- **Integrant** = el container de servicios y el kernel de Symfony juntos: declara qué componentes existen (servidor HTTP, pool de BD, etc.) y los arranca/para en orden.
- **reitit** = el componente Routing (mapea URLs a handlers).
- **RPC (/api/rpc/command/<nombre>)**: en vez de muchos endpoints REST, expone "comandos". Cada comando ≈ un controlador/acción o un message handler de Symfony Messenger.
- **next.jdbc** = capa de acceso a BD, parecido a Doctrine DBAL (sin ORM).
- **common (common/)**: código compartido entre front y back (.cljc corre en JVM y en navegador). Ahí viven validaciones con Malli (≈ Symfony Validator) y tipos comunes. Esto es algo que en PHP normalmente no puedes hacer: compartir el mismo código de validación en cliente y servidor.
- **Servicios de datos**: PostgreSQL (datos), Redis/Valkey (pub/sub para colaboración en tiempo real + caché) y almacenamiento de objetos (ficheros).
- **exporter y render-wasm**: servicios muy específicos de Penpot (exportar a PDF/SVG, render con Skia). Para tu entrenamiento los ignoramos.

## Cómo se comunican front y back
El navegador carga el SPA → el SPA hace peticiones HTTP a /api/... → nginx las reenvía al backend → el backend responde datos (Penpot usa Transit, un formato tipo JSON). Para colaboración usa WebSockets. nginx es la pieza que "une" todo bajo un mismo origen.

# Arquitectura test
```
        NAVEGADOR
            │ HTTP
            ▼
   ┌───────────────────┐
   │ FRONTEND (nginx)  │  sirve el SPA + proxy /api → backend
   └───────────────────┘
            │ /api
            ▼
   ┌───────────────────┐
   │ BACKEND (Clojure) │  responde JSON
   └───────────────────┘
```