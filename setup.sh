#!/bin/bash

# Script de Setup - Sistema de Transporte
# Este script configura y lanza toda la arquitectura de seguridad

echo "================================="
echo "Sistema de Transporte - Setup"
echo "================================="
echo ""

# Verificar Docker
echo "1. Verificando Docker..."
if command -v docker &> /dev/null; then
    echo "   ✓ Docker instalado"
else
    echo "   ✗ Docker no está instalado"
    exit 1
fi

# Verificar Maven
echo "2. Verificando Maven..."
if command -v mvn &> /dev/null; then
    echo "   ✓ Maven instalado"
else
    echo "   ✗ Maven no está instalado"
    exit 1
fi

# Build Auth Service
echo "3. Compilando ms-auth-service..."
cd ms-auth-service
if mvn clean package -DskipTests; then
    echo "   ✓ Auth Service compilado"
else
    echo "   ✗ Error compilando Auth Service"
    exit 1
fi
cd ..

# Build API Gateway
echo "4. Compilando ms-lib-api-gateway..."
cd ms-lib-api-gateway
if mvn clean package -DskipTests; then
    echo "   ✓ API Gateway compilado"
else
    echo "   ✗ Error compilando API Gateway"
    exit 1
fi
cd ..

# Detener servicios previos
echo "5. Deteniendo servicios previos..."
docker-compose down 2>/dev/null
echo "   ✓ Servicios previos detenidos"

# Iniciar Docker Compose
echo "6. Iniciando servicios con Docker Compose..."
if docker-compose up -d; then
    echo "   ✓ Servicios iniciados"
else
    echo "   ✗ Error iniciando servicios"
    exit 1
fi

# Esperar que los servicios estén listos
echo "7. Esperando que los servicios estén listos..."
sleep 10

# Verificar servicios
echo "8. Verificando estado de servicios..."
services=("auth-postgres" "auth-service" "transporte-redis" "api-gateway")
for service in "${services[@]}"; do
    if docker-compose ps $service | grep -q "Up"; then
        echo "   ✓ $service está activo"
    else
        echo "   ✗ $service no está activo"
    fi
done

echo ""
echo "================================="
echo "¡Setup completado!"
echo "================================="
echo ""
echo "URLs de acceso:"
echo "  API Gateway:       http://localhost:8080"
echo "  Auth Service:      http://localhost:8081"
echo "  Swagger UI:        http://localhost:8081/swagger-ui.html"
echo "  PostgreSQL:        localhost:5432"
echo "  Redis:             localhost:6379"
echo ""
echo "Credenciales por defecto:"
echo "  Username: admin"
echo "  Password: 123456"
echo ""
echo "Comandos útiles:"
echo "  Ver logs:          docker-compose logs -f"
echo "  Detener:           docker-compose down"
echo "  Reiniciar:         docker-compose restart"
echo ""
echo "Próximos pasos:"
echo "  1. Importar postman_collection.json en Postman"
echo "  2. Ejecutar request 'Login - Obtener Token'"
echo "  3. Usar el token para acceder a otros endpoints"
echo ""
