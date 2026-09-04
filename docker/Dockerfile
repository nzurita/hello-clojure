# All-in-one development container for the Clojure learning project.
# Same philosophy as a LAMP wdev-* box: one Debian/Ubuntu image with the full
# toolchain inside (JDK, Clojure CLI, Node/pnpm, PostgreSQL, nginx).
#
# Base: official Eclipse Temurin (Ubuntu) image with JDK 21 ready to use.
FROM eclipse-temurin:21-jdk

ENV DEBIAN_FRONTEND=noninteractive \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    TZ=Europe/Madrid \
    NODE_MAJOR=22 \
    PG_MAJOR=15 \
    PNPM_VERSION=10.33.0

# --- Base utilities ----------------------------------------------------------
RUN apt-get update && apt-get install -y --no-install-recommends \
      ca-certificates curl wget gnupg lsb-release \
      git vim less procps sudo rlwrap unzip zip tree \
      dumb-init \
    && rm -rf /var/lib/apt/lists/*

# --- Node.js + pnpm ----------------------------------------------------------
# Required for frontend tooling (shadow-cljs compiles ClojureScript to JS).
RUN curl -fsSL https://deb.nodesource.com/setup_${NODE_MAJOR}.x | bash - \
    && apt-get install -y nodejs \
    && corepack enable \
    && corepack prepare pnpm@${PNPM_VERSION} --activate \
    && rm -rf /var/lib/apt/lists/*

# --- Clojure CLI (tools.deps) ------------------------------------------------
# Resolves deps.edn dependencies and runs the project.
RUN curl -fsSL -o /tmp/linux-install.sh \
      https://github.com/clojure/brew-install/releases/latest/download/linux-install.sh \
    && bash /tmp/linux-install.sh \
    && rm /tmp/linux-install.sh \
    && clojure -e '(println "Clojure" (clojure-version))'

# --- PostgreSQL 15 (official PGDG repository) --------------------------------
# Included even though it is not used yet (same as baking MariaDB into the image).
RUN install -d /usr/share/postgresql-common/pgdg \
    && curl -fsSL -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc \
         https://www.postgresql.org/media/keys/ACCC4CF8.asc \
    && echo "deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc] https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" \
         > /etc/apt/sources.list.d/pgdg.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends postgresql-${PG_MAJOR} \
    && rm -rf /var/lib/apt/lists/*

# --- nginx -------------------------------------------------------------------
# Serves the SPA (static assets) and proxies /api -> backend.
RUN apt-get update && apt-get install -y --no-install-recommends nginx \
    && rm -rf /var/lib/apt/lists/*
COPY nginx/default.conf /etc/nginx/sites-available/default

# --- PostgreSQL configuration for development ------------------------------
# Listen on all interfaces and allow connections (local environment only).
RUN echo "listen_addresses = '*'" >> /etc/postgresql/${PG_MAJOR}/main/postgresql.conf \
    && echo "host all all 0.0.0.0/0 md5" >> /etc/postgresql/${PG_MAJOR}/main/pg_hba.conf

# Pristine copy of the data directory to seed an empty volume.
RUN cp -aR /var/lib/postgresql /var/lib/postgresql_default

# --- tmux (terminal multiplexer for development sessions) --------------------
RUN apt-get update && apt-get install -y --no-install-recommends tmux \
    && rm -rf /var/lib/apt/lists/*

# --- Startup script ----------------------------------------------------------
COPY cmd.sh /usr/local/bin/cmd.sh
RUN chmod +x /usr/local/bin/cmd.sh

WORKDIR /workspace

# Ports: 8080 nginx (web) | 9090 backend | 9630 shadow-cljs | 5432 PostgreSQL
EXPOSE 8080 9090 9630 5432

CMD ["dumb-init", "--", "/usr/local/bin/cmd.sh"]
