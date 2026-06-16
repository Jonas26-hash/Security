#!/bin/bash
set -e
set -u

# Crea múltiples bases de datos si se define POSTGRES_MULTIPLE_DATABASES
# Ejemplo: POSTGRES_MULTIPLE_DATABASES=cliente_db,reserva_db

if [ -n "${POSTGRES_MULTIPLE_DATABASES:-}" ]; then
    echo ">>> Creando bases de datos: $POSTGRES_MULTIPLE_DATABASES"
    IFS=',' read -ra DBS <<< "$POSTGRES_MULTIPLE_DATABASES"
    for db in "${DBS[@]}"; do
        db=$(echo "$db" | xargs)  # trim
        echo ">>> Creando base de datos: $db"
        psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
            CREATE DATABASE "$db";
            GRANT ALL PRIVILEGES ON DATABASE "$db" TO "$POSTGRES_USER";
EOSQL
    done
    echo ">>> Bases de datos creadas exitosamente"
fi
