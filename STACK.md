## Stack tecnológico de Penpot (resumen)

| Módulo          | Lenguaje(s)            | Tecnología / Framework clave                                   |
| --------------- | ---------------------- | -------------------------------------------------------------- |
| `frontend/`     | ClojureScript + SCSS   | React 19 vía **Rumext**, **Potok** (estado), **RxJS/beicon**, **okulary**, shadow-cljs |
| `backend/`      | Clojure (JVM)          | **Integrant**, HTTP/RPC con **reitit** + **yetti** (Jetty), **next.jdbc**, **HikariCP** |
| `common/`       | CLJC (JVM + JS)        | Tipos/geometría/schemas compartidos, **Malli**, Transit         |
| `render-wasm/`  | **Rust** → WebAssembly | **Skia** (`skia-safe`), Emscripten, OpenGL                      |
| `exporter/`     | ClojureScript + Node   | **Playwright** headless (export SVG/PDF/PNG)                     |
| `mcp/`          | **TypeScript** (Node)  | MCP SDK (`@modelcontextprotocol/sdk`)                           |
| `plugins/`      | **TypeScript**         | Runtime de plugins (SES), tipos de la Plugin API                |
| `library/`      | ClojureScript          | `@penpot/library`: builder de archivos `.penpot`                |

Penpot trae además un sistema de memorias en `penpot/.serena/memories/`
(con `penpot/AGENTS.md` como índice) que documenta cada módulo en detalle.
Consúltalo si necesitas entender un patrón concreto, pero **no lo edites**.

## Análisis del stack tecnológico
penpot/ es el código fuente de Penpot, una plataforma open source de diseño/prototipado (alternativa a Figma), licencia MPL-2.0. Es un monorepo con módulos independientes, cada uno con su propio stack:

### Lenguajes principales

Clojure / ClojureScript / CLJC → el núcleo de la aplicación (frontend, backend y código compartido).
Rust → motor de renderizado compilado a WebAssembly.
TypeScript → integración MCP y sistema de plugins.
SCSS → estilos del frontend.
Frameworks y tecnologías por módulo

frontend/ (ClojureScript + SCSS): React 19 vía Rumext, Potok para el estado, RxJS/beicon, okulary para refs reactivas, compilado con shadow-cljs.
backend/ (Clojure JVM): Integrant (system map), HTTP/RPC con reitit + yetti (Jetty), next.jdbc + HikariCP sobre PostgreSQL.
common/ (CLJC, corre en JVM y JS): tipos, geometría, schemas (Malli), lógica de archivos compartida.
render-wasm/ (Rust → WASM): Skia (skia-safe), Emscripten, OpenGL.
exporter/ (ClojureScript + Node): export headless SVG/PDF/PNG con Playwright.
mcp/ (TypeScript/Node): servidor Model Context Protocol (@modelcontextprotocol/sdk).
plugins/ (TypeScript): runtime de plugins con sandbox SES y tipos de la Plugin API.
library/ (ClojureScript): builder de archivos .penpot.
Tooling e infraestructura: pnpm 10 (workspaces), Node v22.22.0, Clojure 1.12 con deps.edn, Cargo, y despliegue con Docker Compose sobre PostgreSQL + Redis/Valkey + almacenamiento fs/S3 + SMTP.

Un detalle importante que encontré: el repo ya trae un sistema de memorias propio en .serena/memories/ (con AGENTS.md como guía), que es la fuente primaria de documentación técnica orientada a agentes. Lo he aprovechado y referenciado en el CLAUDE.md.
