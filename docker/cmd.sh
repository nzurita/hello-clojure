#!/usr/bin/env bash
# Script de arranque del contenedor (PID 1 vía dumb-init).
# Arranca los servicios y mantiene el contenedor vivo, igual que tu cmd.sh de LAMP.
set -e

PG_MAJOR=15

# Siembra el directorio de datos de PostgreSQL si el volumen está vacío
# (equivalente a tu copia de /var/lib/mysql_default).
if [ ! "$(ls -A /var/lib/postgresql 2>/dev/null)" ]; then
  echo "Sembrando el directorio de datos de PostgreSQL..."
  cp -aR /var/lib/postgresql_default/* /var/lib/postgresql/
fi
chown -R postgres:postgres /var/lib/postgresql

echo "Arrancando PostgreSQL ${PG_MAJOR}..."
pg_ctlcluster "${PG_MAJOR}" main start

# Crea un rol y una base de datos de desarrollo en el primer arranque.
su - postgres -c "psql -tc \"SELECT 1 FROM pg_roles WHERE rolname='dev'\"" | grep -q 1 \
  || su - postgres -c "psql -c \"CREATE ROLE dev LOGIN PASSWORD 'dev' SUPERUSER;\""
su - postgres -c "psql -tc \"SELECT 1 FROM pg_database WHERE datname='hello'\"" | grep -q 1 \
  || su - postgres -c "psql -c \"CREATE DATABASE hello OWNER dev;\""

echo "Arrancando nginx..."
service nginx start

echo "----------------------------------------------------------------"
echo " Contenedor de desarrollo listo."
echo "   JDK     : $(java -version 2>&1 | head -n1)"
echo "   Clojure : $(clojure --version 2>/dev/null || echo 'n/a')"
echo "   Node    : $(node -v)   pnpm: $(pnpm -v)"
echo "   Postgres: db 'hello' / rol 'dev' / pass 'dev' (puerto 5432)"
echo "   Web     : http://localhost:8080  (nginx)"
echo "----------------------------------------------------------------"

# Mantiene el contenedor vivo mostrando los logs de nginx.
tail -F /var/log/nginx/access.log /var/log/nginx/error.log
