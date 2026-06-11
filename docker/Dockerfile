# Contenedor de desarrollo "todo en uno" para el proyecto de aprendizaje Clojure.
# Filosofía equivalente a tu LAMP wdev-*: una sola imagen Debian/Ubuntu con todo
# el toolchain dentro (JDK, Clojure CLI, Node/pnpm, PostgreSQL, nginx).
#
# Base: imagen oficial de Eclipse Temurin (Ubuntu) que ya trae el JDK 21 listo.
# (En tu mundo PHP, el JDK es el "runtime" como lo es el binario de PHP.)
FROM eclipse-temurin:21-jdk

ENV DEBIAN_FRONTEND=noninteractive \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    TZ=Europe/Madrid \
    NODE_MAJOR=22 \
    PG_MAJOR=15 \
    PNPM_VERSION=10.33.0

# --- Utilidades base ---------------------------------------------------------
RUN apt-get update && apt-get install -y --no-install-recommends \
      ca-certificates curl wget gnupg lsb-release \
      git vim less procps sudo rlwrap unzip zip tree \
      dumb-init \
    && rm -rf /var/lib/apt/lists/*

# --- Node.js + pnpm ----------------------------------------------------------
# Necesario para el tooling del frontend (shadow-cljs compila ClojureScript a JS).
# Equivale al Node/yarn que ya usas para compilar assets en Symfony.
RUN curl -fsSL https://deb.nodesource.com/setup_${NODE_MAJOR}.x | bash - \
    && apt-get install -y nodejs \
    && corepack enable \
    && corepack prepare pnpm@${PNPM_VERSION} --activate \
    && rm -rf /var/lib/apt/lists/*

# --- Clojure CLI (tools.deps) ------------------------------------------------
# Resuelve dependencias de deps.edn y ejecuta el proyecto.
# Es el equivalente a tener "php + composer" en uno.
RUN curl -fsSL -o /tmp/linux-install.sh \
      https://github.com/clojure/brew-install/releases/latest/download/linux-install.sh \
    && bash /tmp/linux-install.sh \
    && rm /tmp/linux-install.sh \
    && clojure -e '(println "Clojure" (clojure-version))'

# --- PostgreSQL 15 (repositorio oficial PGDG) --------------------------------
# Incluido aunque todavía no se use (igual que tu MariaDB horneada en la imagen).
RUN install -d /usr/share/postgresql-common/pgdg \
    && curl -fsSL -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc \
         https://www.postgresql.org/media/keys/ACCC4CF8.asc \
    && echo "deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc] https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" \
         > /etc/apt/sources.list.d/pgdg.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends postgresql-${PG_MAJOR} \
    && rm -rf /var/lib/apt/lists/*

# --- nginx -------------------------------------------------------------------
# Sirve el SPA (estáticos) y hace de proxy /api -> backend. Igual rol que el
# Apache/Nginx de tu LAMP.
RUN apt-get update && apt-get install -y --no-install-recommends nginx \
    && rm -rf /var/lib/apt/lists/*
COPY nginx/default.conf /etc/nginx/sites-available/default

# --- Configuración de PostgreSQL para desarrollo -----------------------------
# Escucha en todas las interfaces y permite conexiones (solo entorno local).
RUN echo "listen_addresses = '*'" >> /etc/postgresql/${PG_MAJOR}/main/postgresql.conf \
    && echo "host all all 0.0.0.0/0 md5" >> /etc/postgresql/${PG_MAJOR}/main/pg_hba.conf

# Copia "prístina" del directorio de datos para sembrar un volumen vacío.
# (Mismo truco que usas con /var/lib/mysql_default en tu LAMP.)
RUN cp -aR /var/lib/postgresql /var/lib/postgresql_default

# --- Script de arranque ------------------------------------------------------
COPY cmd.sh /usr/local/bin/cmd.sh
RUN chmod +x /usr/local/bin/cmd.sh

WORKDIR /workspace

# Puertos: 8080 nginx (web) | 9090 backend | 9630 shadow-cljs | 5432 PostgreSQL
EXPOSE 8080 9090 9630 5432

CMD ["dumb-init", "--", "/usr/local/bin/cmd.sh"]
