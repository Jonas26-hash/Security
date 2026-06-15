# 📚 Sistema de Transporte - Módulo de Seguridad

## 🎯 Visión General

Implementación completa de una arquitectura de microservicios con Spring Boot que incluye:
- Autenticación centralizada (JWT, Spring Security)
- Service Registry (Eureka) y Config Server
- API Gateway con validación JWT y rate limiting
- Microservicios: ms-cliente (con auth) y ms-reserva
- Base de datos PostgreSQL con múltiples schemas
- Logging y auditoría completos

## 🚀 Inicio Rápido

### 1. Requisitos Previos
- Docker & Docker Compose
- Java 17+
- Maven 3.6+

### 2. Ejecutar Setup
```bash
# Windows
.\setup.ps1

# Linux/Mac
chmod +x setup.sh && ./setup.sh
```

### 3. Verificar Servicios
```bash
docker-compose ps
```

### 4. URLs de Acceso
```
API Gateway:     http://localhost:8080
Config Server:   http://localhost:8888
Registry:        http://localhost:8761
ms-Cliente:       http://localhost:8082
ms-Reserva:       http://localhost:8083
PostgreSQL:      localhost:5432
Redis:           localhost:6379
```

### 5. Credenciales por Defecto
```
Usuario Admin:   admin / 123456
Config Server:   config / config123
Registry:        eureka / eureka123
```

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    Cliente / Postman / App                  │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼ HTTP/HTTPS
        ┌──────────────────────────────────────┐
        │   API Gateway (8080)                 │
        │  ├─ JWT Validation Global Filter     │
        │  ├─ Rate Limiting Filter (Redis)     │
        │  ├─ IP Whitelisting                  │
        │  └─ Request Logging                  │
        └──────────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        ▼                 ▼                 ▼
    ┌──────────┐   ┌──────────┐    ┌───────────┐
    │ Auth Svc │   │ Clientes │    │ Reservas  │
    │ (8082)   │   │ (8082)   │    │ (8083)    │
    └────┬─────┘   └────┬─────┘    └────┬──────┘
         │              │              │
         └──────────────┼──────────────┘
                        │
                        ▼
                   ┌────────────┐
                   │ PostgreSQL │
                   │ (5432)     │
                   └────────────┘
                        │
        ┌──────────────┴──────────────┐
        ▼                             ▼
    ┌─────────────┐          ┌────────────┐
    │ Eureka      │          │ Config    │
    │ Registry    │          │ Server    │
    │ (8761)      │          │ (8888)    │
    └─────────────┘          └────────────┘
```

---

## 🔐 Autenticación y Seguridad

### Flujo de Autenticación

```
Cliente
  │
  ├─→ POST /auth/login (username, password)
  │
  ├─→ ms-cliente valida credenciales con BCrypt
  │
  ├─→ ms-cliente genera token JWT (HS256)
  │
  └─→ Respuesta: { token, expiresIn, username, email, role }

Token JWT:
{
  "sub": "admin",
  "roles": ["ROLE_ADMIN"],
  "iat": 1710000000,
  "exp": 1710003600
}
```

### Validación en API Gateway

```
Solicitud a Microservicio
  │
  ├─→ API Gateway intercepta
  │
  ├─→ ¿Ruta pública? (auth/*, swagger/*, v3/api-docs/*)
  │   ├─→ SÍ: Permitir paso
  │   └─→ NO: Continuar
  │
  ├─→ ¿Token en Authorization header?
  │   ├─→ NO: 401 Unauthorized
  │   └─→ SÍ: Validar
  │
  ├─→ JwtValidationService valida firma y expiración
  │   ├─→ Inválido: 401 Unauthorized
  │   └─→ Válido: Continuar
  │
  ├─→ ¿Ruta administrativa? (/admin/*)
  │   ├─→ SÍ: Validar IP en whitelist
  │   │   ├─→ No autorizada: 403 Forbidden
  │   │   └─→ Autorizada: Continuar
  │   └─→ NO: Continuar
  │
  ├─→ Rate Limiting (Redis)
  │   ├─→ Límite excedido: 429 Too Many Requests
  │   └─→ OK: Continuar
  │
  └─→ Enrutar a microservicio
```

### Roles y Permisos

| Recurso | ADMIN | OPERADOR | CLIENTE |
|---------|-------|----------|---------|
| Clientes | CRUD | Lectura | Propio |
| Reservas | CRUD | CRUD | Crear/Consultar |

### Rate Limiting

- **Límite:** 100 solicitudes por minuto por usuario
- **Storage:** Redis
- **Ventana:** 60 segundos deslizantes
- **Respuesta:** 429 Too Many Requests

### IP Whitelisting

**Aplicado a:** Rutas administrativas (/admin/*)

**IPs permitidas:**
- 192.168.1.10 (Admin 1)
- 192.168.1.11 (Admin 2)
- 127.0.0.1 (Localhost)

---

## 📊 Microservicios

### 1. ms-lib-config-server (8888)
**Spring Cloud Config Server**
- Almacena configuración para todos los microservicios
- Soporta múltiples perfiles (dev, test, prod)
- Conecta a Eureka para discovery

### 2. ms-lib-registry-server (8761)
**Eureka Service Registry**
- Registro centralizado de servicios
- Health checks para todos los microservicios
- Balanceo de carga y failover

### 3. ms-lib-api-gateway (8080)
**API Gateway Central**
- Validación JWT
- Rate limiting
- IP whitelisting
- Enrutamiento a microservicios
- Logging de todas las solicitudes

### 4. ms-cliente (8082)
**Gestión de Clientes + Autenticación**
- CRUD de clientes
- **Autenticación JWT integrada** (login, register)
- Roles y permisos
- Auditoría de operaciones
- Base de datos: cliente_db

### 5. ms-reserva (8083)
**Gestión de Reservas**
- CRUD de reservas
- Autenticación via JWT
- Base de datos: reserva_db

---

## 🔌 APIs Principales

### Autenticación (ms-cliente)

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| POST | /auth/login | Login de usuario | No |
| POST | /auth/register | Registro de usuario | No |
| GET | /auth/validate | Validar token | No |
| GET | /cliente/all | Listar clientes | Sí (JWT) |

### Reservas (ms-reserva)

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| POST | /reserva | Crear reserva | Sí (JWT) |
| GET | /reserva/{id} | Obtener reserva | Sí (JWT) |
| GET | /reserva | Listar todas | Sí (JWT) |
| PUT | /reserva/{id} | Actualizar reserva | Sí (JWT) |
| DELETE | /reserva/{id} | Cancelar reserva | Sí (JWT) |

### Clientes (ms-cliente)

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| POST | /cliente | Crear cliente | Sí (ADMIN) |
| GET | /cliente/{id} | Obtener cliente | Sí (ADMIN) |
| GET | /cliente/dni/{dni} | Obtener por DNI | Sí (ADMIN) |
| GET | /cliente | Listar clientes | Sí (ADMIN) |
| PUT | /cliente/{id} | Actualizar cliente | Sí (ADMIN) |
| DELETE | /cliente/{id} | Eliminar cliente | Sí (ADMIN) |

---

## 📚 Testing

### Casos de Prueba (5 Requeridos)

#### ✅ Caso 1: Login Correcto
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# Esperado: 200 OK + token JWT
```

#### ✅ Caso 2: Password Incorrecta
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrong"}'

# Esperado: 401 Unauthorized
```

#### ✅ Caso 3: Token Expirado
```bash
curl http://localhost:8080/auth/validate?token=expired_token

# Esperado: false
```

#### ✅ Caso 4: Sin Permisos
```bash
# Cliente CLIENTE intenta acceder a admin
curl -H "Authorization: Bearer $CLIENT_TOKEN" \
  http://localhost:8080/cliente/admin-only

# Esperado: 403 Forbidden
```

#### ✅ Caso 5: Rate Limiting
```bash
# Enviar >100 solicitudes/minuto
for i in {1..105}; do
  curl -H "Authorization: Bearer $TOKEN" \
    http://localhost:8080/cliente/all &
done

# Solicitud 101+: 429 Too Many Requests
```

### Postman Collection

Importar `postman_collection.json` en Postman:

1. Abrir Postman
2. File → Import → Seleccionar archivo
3. Hacer clic en "Import"
4. Variables de entorno se cargan automáticamente

**Variables disponibles:**
- `{{jwt_token}}` - Token admin
- `{{client_token}}` - Token cliente
- `{{token_type}}` - Tipo de token

### Pruebas Unitarias

```bash
cd ms-cliente
mvn test
```

### Pruebas de Integración

```bash
cd ms-cliente
mvn verify
```

---

## 🗄️ Base de Datos

### Conectar a PostgreSQL

```bash
# Desde la terminal
docker-compose exec postgres psql -U postgres -d cliente_db

# O usando DBeaver/pgAdmin
# Datos: localhost:5432 / postgres / postgres
```

### Tablas Principales

```sql
-- Ver usuarios
SELECT id, username, email, activo, fecha_creacion FROM usuarios;

-- Ver roles de usuario específico
SELECT u.username, r.name FROM usuario_rol ur
JOIN usuarios u ON ur.usuario_id = u.id
JOIN roles r ON ur.rol_id = r.id;

-- Ver auditoría
SELECT * FROM auditoria ORDER BY fecha_hora DESC;

-- Ver clientes
SELECT * FROM clientes;

-- Ver reservas
SELECT * FROM reservas;
```

---

## 🐳 Docker

### Comandos
```bash
# Iniciar servicios
docker-compose up -d

# Detener servicios
docker-compose down

# Ver logs en tiempo real
docker-compose logs -f

# Ver estado
docker-compose ps

# Reiniciar
docker-compose restart

# Rebuildar imágenes
docker-compose up -d --build
```

### Comandos Útiles
```bash
# Ver logs específicos
docker-compose logs -f api-gateway
docker-compose logs -f ms-cliente
docker-compose logs -f ms-reserva

# Conectar a PostgreSQL
docker-compose exec postgres psql -U postgres -d cliente_db

# Conectar a Redis
docker-compose exec redis redis-cli

# Conectar a Eureka
docker-compose exec registry-server curl http://localhost:8761/

# Conectar a Config
docker-compose exec config-server curl http://localhost:8888/actuator/env
```

---

## 📋 Variables de Entorno

```yaml
# .env (opcional)
POSTGRES_MULTIPLE_DATABASES=cliente_db,reserva_db

JWT_SECRET=your-super-secret-key-256-bits

REDIS_HOST=redis
REDIS_PORT=6379

IP_WHITELIST=192.168.1.10,192.168.1.11,127.0.0.1
```

---

## 🔧 Configuración

### application.yml (ms-cliente)
```yaml
spring:
  application:
    name: ms-cliente
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  datasource:
    url: jdbc:postgresql://postgres:5432/cliente_db
    username: postgres
    password: postgres
  cloud:
    config:
      uri: http://config-server:8888
    discovery:
      enabled: true

jwt:
  secret: your-super-secret-key-256-bits
  expiration: 3600000

server:
  port: 8082
```

### application.yml (ms-reserva)
```yaml
spring:
  application:
    name: ms-reserva
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  datasource:
    url: jdbc:postgresql://postgres:5432/reserva_db
    username: postgres
    password: postgres
  cloud:
    config:
      uri: http://config-server:8888
    discovery:
      enabled: true

server:
  port: 8083
```

---

## 📚 Documentación Adicional

### Spring Cloud Config
- URL: http://localhost:8888
- Actuator: http://localhost:8888/actuator
- Perfiles: application.yml, application-dev.yml, application-prod.yml

### Eureka
- URL: http://localhost:8761
- Dashboard: http://localhost:8761/

### Swagger
- ms-cliente: http://localhost:8082/swagger-ui.html
- ms-reserva: http://localhost:8083/swagger-ui.html

---

**Versión:** 1.0
**Fecha:** Junio 2026
**Autor:** Sistema de Transporte - Equipo de Seguridad
