# Entorno de desarrollo

## Crear el entorno

```
docker compose up -d --build
docker compose exec wdev-clojure-jdk-21 bash

# -P  prepara classpath y descarga jars
cd /workspace/backend
clojure -P -M:dev
cd /workspace/common
clojure -P
cd /workspace/frontend
pnpm install
```

### Levantar el entorno

`docker compose up -d`

Tras instalar deps, conviene reiniciar backend y frontend en tmux:

Enganchar a tmux:
```
docker compose exec wdev-clojure-jdk-21 tmux attach -t dev
```

En cada ventana (backend y frontend): **Ctrl+C para parar**, luego:

```
Backend: clojure -M:dev
Frontend: pnpm run watch
```

Salir sin parar nada: Ctrl+b, luego d (detach).

Atajos tmux: Ctrl+b n/p cambia de ventana.

### Tmux: Procesos de desarrollo

A diferencia de LAMP (donde cmd.sh arranca servicios "fijos" como Apache/MariaDB), en Clojure los procesos de desarrollo son interactivos y con recarga en caliente:

- PostgreSQL + nginx: servicios de fondo.
- Backend (clojure -M -m app.core): proceso manual; idealmente con un REPL conectado para recarga en caliente.
- Frontend (shadow-cljs watch app): proceso que vigila los .cljs, recompila y hace hot-reload en el navegador.

Enganchar al REPL para ver/controlar:

`docker compose exec wdev-clojure-jdk-21 tmux attach -t dev`

Dentro, te mueves entre ventanas con Ctrl+b y luego el número (0, 1, …) o Ctrl+b n/p (siguiente/anterior). En cada ventana ves los logs de ese proceso, puedes hacer Ctrl+C para pararlo y relanzarlo, etc.

**Para cargar las referencias de funciones:**

`(require '[clojure.repl :refer :all])`

```
(doc +)
(apropos "+")
(find-doc "trim")
(dir clojure.repl)
(source dir)
```

> Ctrl+C → mata la JVM y te deja en el shell #.
> Comando `clojure -M:dev` → vuelve el user=> limpio.

### Programar
Editar .cljs/.clj en el host. El watch del frontend detecta el cambio, recompila y recarga el navegador solo. (El backend, según cómo lo configuremos, se recarga vía REPL o reiniciando su ventana.)

**Frontend**: editas `frontend/src/app/app.cljs` → shadow recompila solo → recargar el navegador.
**Backend**: conectar editor (Calva → Connect a localhost:6064) y evaluar; con #'core/handler los cambios se ven sin reiniciar. Si prefieres, en el REPL: `(restart)` o `(restart)`.

### Salir sin parar nada
Pulsar Ctrl+b y luego d (detach). Te desengancha de tmux pero los procesos siguen vivos dentro del contenedor.

### Apagar al terminar:

`docker compose down`
