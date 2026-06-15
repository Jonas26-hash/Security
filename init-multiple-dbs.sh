#!/bin/bash

# Script para crear múltiples bases de datos en PostgreSQL
# Se ejecuta automáticamente al iniciar el contenedor

set -e
set -u

function create_user_and_database() {
    local database=$1
    echo "Creando usuario y base de datos: '$database'"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        CREATE USER $database WITH PASSWORD '$database';
        CREATE DATABASE $database;
        GRANT ALL PRIVILEGES ON DATABASE $database TO $database;
EOSQL
}

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
    echo "Creación de múltiples bases de datos solicitada: $POSTGRES_MULTIPLE_DATABASES"
    for db in $(echo $POSTGRES_MULTIPLE_DATABASES | tr ',' ' '); do
        create_user_and_database $db
    done
    echo "Múltiples bases de datos creadas exitosamente"
fi
