#!/usr/bin/env pwsh

# Script de Setup - Sistema de Transporte
# Este script configura y lanza toda la arquitectura de seguridad

Write-Host "=================================" -ForegroundColor Cyan
Write-Host "Sistema de Transporte - Setup" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

# Verificar Docker
Write-Host "1. Verificando Docker..." -ForegroundColor Yellow
try {
    docker --version | Out-Null
    Write-Host "   ✓ Docker instalado" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Docker no está instalado" -ForegroundColor Red
    exit 1
}

# Verificar Maven
Write-Host "2. Verificando Maven..." -ForegroundColor Yellow
try {
    mvn --version | Out-Null
    Write-Host "   ✓ Maven instalado" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Maven no está instalado" -ForegroundColor Red
    exit 1
}

# Build Auth Service
Write-Host "3. Compilando ms-auth-service..." -ForegroundColor Yellow
Set-Location -Path "ms-auth-service"
try {
    mvn clean package -DskipTests
    Write-Host "   ✓ Auth Service compilado" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Error compilando Auth Service" -ForegroundColor Red
    exit 1
}
Set-Location -Path ".."

# Build API Gateway
Write-Host "4. Compilando ms-lib-api-gateway..." -ForegroundColor Yellow
Set-Location -Path "ms-lib-api-gateway"
try {
    mvn clean package -DskipTests
    Write-Host "   ✓ API Gateway compilado" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Error compilando API Gateway" -ForegroundColor Red
    exit 1
}
Set-Location -Path ".."

# Detener servicios previos
Write-Host "5. Deteniendo servicios previos..." -ForegroundColor Yellow
try {
    docker-compose down 2>$null
    Write-Host "   ✓ Servicios previos detenidos" -ForegroundColor Green
} catch {
    Write-Host "   ⚠ No había servicios previos" -ForegroundColor Yellow
}

# Iniciar Docker Compose
Write-Host "6. Iniciando servicios con Docker Compose..." -ForegroundColor Yellow
try {
    docker-compose up -d
    Write-Host "   ✓ Servicios iniciados" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Error iniciando servicios" -ForegroundColor Red
    exit 1
}

# Esperar que los servicios estén listos
Write-Host "7. Esperando que los servicios estén listos..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Verificar servicios
Write-Host "8. Verificando estado de servicios..." -ForegroundColor Yellow
$services = @("auth-postgres", "auth-service", "transporte-redis", "api-gateway")
foreach ($service in $services) {
    $status = docker-compose ps -q $service 2>$null
    if ($status) {
        Write-Host "   ✓ $service está activo" -ForegroundColor Green
    } else {
        Write-Host "   ✗ $service no está activo" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=================================" -ForegroundColor Cyan
Write-Host "¡Setup completado!" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "URLs de acceso:" -ForegroundColor Cyan
Write-Host "  API Gateway:       http://localhost:8080" -ForegroundColor White
Write-Host "  Auth Service:      http://localhost:8081" -ForegroundColor White
Write-Host "  Swagger UI:        http://localhost:8081/swagger-ui.html" -ForegroundColor White
Write-Host "  PostgreSQL:        localhost:5432" -ForegroundColor White
Write-Host "  Redis:             localhost:6379" -ForegroundColor White
Write-Host ""
Write-Host "Credenciales por defecto:" -ForegroundColor Cyan
Write-Host "  Username: admin" -ForegroundColor White
Write-Host "  Password: 123456" -ForegroundColor White
Write-Host ""
Write-Host "Comandos útiles:" -ForegroundColor Cyan
Write-Host "  Ver logs:          docker-compose logs -f" -ForegroundColor White
Write-Host "  Detener:           docker-compose down" -ForegroundColor White
Write-Host "  Reiniciar:         docker-compose restart" -ForegroundColor White
Write-Host ""
Write-Host "Próximos pasos:" -ForegroundColor Cyan
Write-Host "  1. Importar postman_collection.json en Postman" -ForegroundColor White
Write-Host "  2. Ejecutar request 'Login - Obtener Token'" -ForegroundColor White
Write-Host "  3. Usar el token para acceder a otros endpoints" -ForegroundColor White
Write-Host ""
