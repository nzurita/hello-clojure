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

## Arquitectura Clojure test
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

## Backend Penpot: La organización del backend es por dos criterios:
### Preocupación técnica / infraestructura (nivel superior de app/):

|---------------------------------------|-------------------------------|------------------------------|
|               Namespace               |            Qué es             |        Símil Symfony         |
|---------------------------------------|-------------------------------|------------------------------|
| app/db.clj, db/                       | acceso a base de datos        | Doctrine DBAL / repositorios |
| app/http.clj, http/                   | servidor y capa HTTP          | HttpKernel                   |
| app/rpc.clj, rpc/                     | la API (despacho de comandos) | Controllers + routing        |
| app/worker.clj, tasks/                | tareas en segundo plano       | Messenger / cron             |
| app/redis.clj, storage.clj, email.clj | servicios de infra            | servicios del container      |
| app/main.clj                          | arranca el sistema            | Kernel / bin/console         |
|---------------------------------------|-------------------------------|------------------------------|
Estos son los "componentes" que Integrant (el container de DI) levanta al arrancar.

### Área de dominio (dentro de rpc/commands/):

Cada fichero agrupa los endpoints de una entidad del dominio:

```
|------------------------------------------------------------|
| rpc/commands/projects.clj   -> todo lo de "proyectos"      |
| rpc/commands/files.clj      -> todo lo de "archivos"       |
| rpc/commands/teams.clj      -> todo lo de "equipos"        |
| rpc/commands/profile.clj    -> todo lo de "perfil/usuario" |
|------------------------------------------------------------|
```

Así que un fichero ≈ un grupo de casos de uso de una entidad (como un Controller "ProjectController" con varias acciones).

###  Patrón RPC
Hay una sola entrada que, según el "nombre del comando", despacha a la función correcta. Cada endpoint es un sv/defmethod dentro del fichero de su entidad:

```
(def ^:private schema:create-project
  [:map {:title "create-project"}
   [:team-id ::sm/uuid]
   [:name [:string {:max 250 :min 1}]]
   [:id {:optional true} ::sm/uuid]])

(sv/defmethod ::create-project
  {::doc/added "1.18"
   ::webhooks/event? true
   ::sm/params schema:create-project}
  [cfg {:keys [::rpc/profile-id team-id] :as params}]

  (teams/check-edition-permissions! cfg profile-id team-id)
  (quotes/check! cfg {::quotes/id ::quotes/projects-per-team
                      ::quotes/profile-id profile-id
                      ::quotes/team-id team-id})

  (let [params (assoc params :profile-id profile-id)]
    (db/tx-run! cfg create-project params)))
```

|-------------------------------------|--------------------------------------------------------------------------|
|          Pieza Clojure              |                         Equivalente Symfony                              |
|-------------------------------------|--------------------------------------------------------------------------|
| (sv/defmethod ::create-project ...) | una acción de controller o un message handler                            |
| ::create-project (el nombre)        | la ruta/nombre de la operación (no una URL por recurso, sino un comando) |
| ::sm/params schema:create-project   | el DTO + validación (Symfony Validator)                                  |
| [cfg {:keys [...] :as params}]      | inyección de dependencias + el Request ya parseado                       |
| check-edition-permissions!          | Voters/Security                                                          |
| db/tx-run!                          | el cuerpo de la lógica dentro de una transacción                         |
|-------------------------------------|--------------------------------------------------------------------------|

El cliente llama por HTTP a algo como POST /api/rpc/command/create-project con un JSON, y el RPC lo enruta a ese defmethod. Por eso no ves una carpeta por endpoint: el "router" es el propio mecanismo de defmethod.

## Frontend Penpot: La organización del backend es por dos criterios:

```
|----------------------------------------------------------------------------------|
| frontend/src/app/                                                                |
|   main/                                                                          |
|     ui/        <- AQUÍ están las pantallas/áreas (componentes React vía Rumext)  |
|     data/      <- eventos y estado (Potok): la "lógica" de cada área             |
|     store.cljs, router.cljs, refs.cljs, repo.cljs   <- infraestructura del front |
|   worker/      <- web worker (cálculo pesado fuera del hilo de UI)               |
|----------------------------------------------------------------------------------|
```
## Resumen
- Backend organizado por infraestructura (db, http, rpc, worker...) + dominio (rpc/commands/<entidad>); un endpoint = un defmethod.
- Frontend organizado por áreas de UI (main/ui/*) + su estado (main/data/*).
- No hay objetos: mapas (datos) + schemas (datos) + funciones, todo separado.

# Clojure test vs Penpot
## Lo que YA tienes (y a qué equivale en Penpot)

|------------------------|-----------------------------------------------|-----------------------------------|
|        Pieza           |           Clojure test (this)                 |               Penpot              |
|------------------------|-----------------------------------------------|-----------------------------------|
| Estructura monorepo    | backend/ + frontend/ + common/                | igual (+ más módulos)             |
| Código compartido CLJC | common/ con Malli                             | common/ con Malli, Transit, tipos |
| Backend JVM            | Clojure + Ring + Jetty                        | Clojure + yetti (Jetty)           |
| Validación             | Malli compartido                              | Malli compartido                  |
| Frontend               | ClojureScript + shadow-cljs                   | ClojureScript + shadow-cljs       |
| Dev en Docker          | contenedor único, tmux, nREPL, rebel-readline | devenv con tmux + nREPL + rebel   |
| Recarga                | hot-reload front + (reload) back              | igual                             |
|------------------------|-----------------------------------------------|-----------------------------------|

## Lo que te FALTA para parecerte de verdad a Penpot

|------------------------|-------------------------------------------------------------|------------------------------------------------|
|          Área          |                     Penpot usa                              |                 Tú aún no                      |
|------------------------|-------------------------------------------------------------|------------------------------------------------|
| Sistema/arranque (DI)  | Integrant (~ container de Symfony)                          | levantas Jetty "a mano"                        |
| Rutas + API            | reitit + patrón RPC (defmethod)                             | un único handler Ring                          |
| Base de datos          | next.jdbc + HikariCP + migraciones                          | PostgreSQL está en el contenedor pero sin usar |
| Frontend UI            | React vía Rumext                                            | DOM "a pelo" (innerHTML)                       |
| Estado frontend        | Potok (Flux/Redux) + RxJS/beicon + okulary                  | sin gestión de estado                          |
| Estilos                | SCSS modules                                                | sin estilos                                    |
| Otros módulos          | render-wasm (Rust), exporter (Playwright), mcp/plugins (TS) | fuera de alcance por ahora                     |
|------------------------|-------------------------------------------------------------|------------------------------------------------|

