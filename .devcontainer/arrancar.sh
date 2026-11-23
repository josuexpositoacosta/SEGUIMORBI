#!/usr/bin/env bash
# Arranca la API en segundo plano al iniciar el codespace. Registro: /tmp/seguimorbi.log
set -u
cd "$(dirname "$0")/.."

if [ -z "${JWT_SECRET:-}" ] || [ -z "${ADMIN_PASSWORD:-}" ]; then
  echo "Faltan los secretos JWT_SECRET y/o ADMIN_PASSWORD de Codespaces. Ver docs/codespaces.md" >&2
  exit 0
fi

if pgrep -f 'spring-boot:run' >/dev/null; then
  echo "La API ya está en marcha."
  exit 0
fi

# Espera a que MySQL acepte conexiones (máx. 60 s).
for _ in $(seq 1 60); do
  (exec 3<>/dev/tcp/localhost/3306) 2>/dev/null && break
  sleep 1
done

setsid nohup ./mvnw -q spring-boot:run > /tmp/seguimorbi.log 2>&1 < /dev/null &
echo "API arrancando en el puerto 8080. Registro: tail -f /tmp/seguimorbi.log"
