#!/usr/bin/env bash
# Actualiza un servicio desde GitHub Actions por SSH (forced command en authorized_keys).
# El workflow manda "back" o "front" como comando; cualquier otra cosa se rechaza.
set -euo pipefail

s="${SSH_ORIGINAL_COMMAND:-${1:-}}"
case "$s" in
  back|front) ;;
  *) echo "uso: back|front" >&2; exit 1 ;;
esac

cd "$(dirname "$0")" 2>/dev/null || cd /root/chispas

# un deploy a la vez (back y front pueden llegar juntos)
exec 9>.deploy.lock
flock 9

docker compose pull "$s"
docker compose up -d "$s"
docker image prune -f
echo OK
