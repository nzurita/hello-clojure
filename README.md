# Entorno de desarrollo

### Levantar el entorno

`docker compose up -d`

### Tmux: Procesos de desarrollo

A diferencia de LAMP (donde cmd.sh arranca servicios "fijos" como Apache/MariaDB), en Clojure los procesos de desarrollo son interactivos y con recarga en caliente:

- PostgreSQL + nginx: servicios de fondo → estos sí tiene sentido que los arranque cmd.sh solos (ya lo hacen).
- Backend (clojure -M -m app.core): proceso que quieres controlar tú; idealmente con un REPL conectado para recarga en caliente.
- Frontend (shadow-cljs watch app): proceso que vigila los .cljs, recompila y hace hot-reload en el navegador.

Engancharte para ver/controlar (cuando quieras mirar logs o tocar algo):

`docker compose exec wdev-clojure-jdk-21 tmux attach -t dev`

Dentro, te mueves entre ventanas con Ctrl+b y luego el número (0, 1, …) o Ctrl+b n/p (siguiente/anterior). En cada ventana ves los logs de ese proceso, puedes hacer Ctrl+C para pararlo y relanzarlo, etc.

> Ctrl+C → mata la JVM y te deja en el shell #.
> escribes clojure -M:dev → vuelve el user=> limpio.

### Programar
Editas tus .cljs/.clj en el host (con tu editor de siempre). El watch del frontend detecta el cambio, recompila y recarga el navegador solo. (El backend, según cómo lo configuremos, se recarga vía REPL o reiniciando su ventana.)

**Frontend**: editas frontend/src/app/app.cljs → shadow recompila solo → recargas el navegador.
**Backend**: conectas tu editor (Calva → Connect a localhost:6064) y evalúas; con #'core/handler los cambios se ven sin reiniciar. Si prefieres, en el REPL: (restart).

### Salir sin parar nada
Pulsas Ctrl+b y luego d (detach). Te desengancha de tmux pero los procesos siguen vivos dentro del contenedor.

### Apagar al terminar el día:

`docker compose down`
