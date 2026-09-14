# Hello World!

Clojure + React training project. Dragon Ball characters and planets using [Dragon Ball API](https://web.dragonball-api.com/) with some added customization. Aimed to train some of [Penpot](https://github.com/penpot/penpot) tech stack components.

By [Norberto Zurita](https://izuca.es)

# Development environment

`docker` folder includes development environment configuration: Nginx, JDK, Node JS and PostgreSQL among others.

## Create environment

```
docker compose up -d --build
docker compose exec wdev-clojure-jdk-21 bash

# -P  prepares classpath and download jars
cd /workspace/backend
clojure -P -M:dev
cd /workspace/common
clojure -P
cd /workspace/frontend
pnpm install
```

### Run environment

`docker compose up -d`

After installing deps, restart backend and frontend in tmux:

Run tmux:
```
docker compose exec wdev-clojure-jdk-21 tmux attach -t dev
```

In each window (backend and frontend): **Ctrl+C** to stop, then:

```
Backend: clojure -M:dev
Frontend: pnpm run watch
```

Exit without stopping: Ctrl+b, then d (detach).

### Tmux: development processes

In Clojure development processes are interactive and require hot reload:

- PostgreSQL + nginx: background services.
- Backend (clojure -M -m app.core): manual process, active REPL for hot reload.
- Frontend (shadow-cljs watch app): manual process, recompiles and authomaticly hot reloads .cljs in browser.

**Run REPL:**

`docker compose exec wdev-clojure-jdk-21 tmux attach -t dev`

Inside, you move between windows with Ctrl+b and then the number (0, 1, …) or Ctrl+b n/p (next/previous).

**Load and refer function docs:**

`(require '[clojure.repl :refer :all])`

```
(doc +)
(apropos "+")
(find-doc "trim")
(dir clojure.repl)
(source dir)
```

> Ctrl+C → kill JVM.
> Command `clojure -M:dev` → returns clean user=>.

### End and exit:

`docker compose down`

# Production environment

```
cd hello-clojure/frontend
pnpm install
pnpm run release

cd hello-clojure
sudo chown -R user:www-data frontend/resources/public
sudo chmod -R g+rX frontend/resources/public

# manually check listen
cd hello-clojure/backend
clojure -P # descarga dependencias
clojure -M -m app.core
```

