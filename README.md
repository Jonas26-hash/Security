# Sistema de Transporte - Módulo de Seguridad

Arquitectura de microservicios con autenticación JWT centralizada, Spring Security, auditoría y rate limiting.

## Stack

| Componente | Tecnología |
|---|---|
| Runtime | Java 17 + Spring Boot 3.2.3 |
| Auth | JWT (HS256, 60 min), BCrypt, Spring Security |
| Gateway | Spring Cloud Gateway + Redis (rate limiting) |
| Registry | Eureka Service Registry |
| Config | Spring Cloud Config Server |
| DB | PostgreSQL 15 (bases separadas por servicio) |
| DevOps | Docker Compose (multi-stage Maven builds) |

## Arquitectura

```
                      Cliente / Postman
                            |
                      API Gateway :8080
                     (JWT + Rate Limit)
                    /        |        \
                   /         |         \
          /auth/**    /cliente/**    /reserva/**
              |            |              |
         ms-cliente    ms-cliente    ms-reserva
          :8082         :8082          :8083
         (auth+JWT)   (JWT filter)   (Resource Server)
              |            |              |
              +—— PostgreSQL ————————————+
                 cliente_db    reserva_db

    Registry :8761        Config :8888        Redis :6379
```

## Inicio rápido

```bash
git clone https://github.com/Jonas26-hash/Security.git
cd Security
docker-compose up -d --build
```

### URLs

| Servicio | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| Config Server | http://localhost:8888 |
| Swagger ms-cliente | http://localhost:8082/swagger-ui.html |
| Swagger ms-reserva | http://localhost:8083/swagger-ui.html |
| PostgreSQL | localhost:5432 (postgres/postgres) |

### Credenciales por defecto

**admin / 123456** (rol ADMIN con todos los permisos)

## Microservicios (5)

1. **ms-lib-registry-server** (:8761) — Eureka Service Registry
2. **ms-lib-config-server** (:8888) — Spring Cloud Config (native profile)
3. **ms-lib-api-gateway** (:8080) — Gateway con JWT filter + rate limiting (Redis) + IP whitelist
4. **ms-cliente** (:8082) — CRUD clientes + **autenticación JWT integrada** (login/register) + auditoría
5. **ms-reserva** (:8083) — CRUD reservas con JWT Resource Server

## Seguridad

### Flujo de autenticación

```
1. POST /auth/login {username, password}
2. ms-cliente valida con BCrypt
3. ms-cliente genera JWT (HS256) con roles: ["ROLE_ADMIN"]
4. Cliente usa token en header: Authorization: Bearer <token>
5. API Gateway intercepta, valida JWT, aplica rate limiting
6. Si es válido → enruta a ms-cliente o ms-reserva
7. Cada servicio valida el JWT nuevamente (defensa en profundidad)
```

### Roles y permisos (25 permisos)

| Rol | Permisos |
|---|---|
| ADMIN | Todos (CRUD en usuarios, clientes, reservas, buses, asientos, destinos, programación, auditoría) |
| OPERADOR | CRUD reservas, buses, asientos, programación |
| CLIENTE | Crear/ver reservas, ver programación, asientos, destinos |

### Rate limiting

- 100 solicitudes/min por usuario (hash del token)
- Redis sliding window, respuesta 429 si excede

### IP whitelisting

- Rutas /admin/* solo desde IPs autorizadas
- Configurable en `ip.whitelist`

## APIs

### Autenticación (Gateway :8080)

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | /auth/login | No | Login → JWT token |
| POST | /auth/register | No | Registrar nuevo usuario (rol CLIENTE) |
| GET | /auth/validate?token= | No | Validar token |

### Clientes (Gateway :8080/cliente)

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | /cliente | JWT | Crear cliente |
| GET | /cliente | JWT | Listar (paginado) |
| GET | /cliente/{id} | JWT | Por ID |
| GET | /cliente/dni/{dni} | JWT | Por DNI |
| PUT | /cliente/{id} | JWT | Actualizar |
| DELETE | /cliente/{id} | JWT | Eliminar |

### Reservas (Gateway :8080/reserva)

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | /reserva | JWT | Crear reserva |
| GET | /reserva | JWT | Listar todas |
| GET | /reserva/{id} | JWT | Por ID |
| GET | /reserva/cliente/{id} | JWT | Por cliente |
| PUT | /reserva/{id} | JWT | Actualizar |
| DELETE | /reserva/{id} | JWT | Cancelar |

## Postman — 5 casos de prueba

1. **Login correcto** → `POST /auth/login` admin/123456 → 200 + JWT
2. **Password incorrecta** → `POST /auth/login` admin/wrong → 401
3. **Token expirado** → `GET /auth/validate?token=xxx` → false
4. **Sin permisos** → Cliente CLIENTE intenta acceder a ruta ADMIN → 403
5. **Rate limiting** → 100+ requests/min → 429

Importar `postman_collection.json` en Postman.

## Auditoría

Cada login/register se registra automáticamente en tabla `auditoria`:
```
usuario | accion  | resultado | ip_address | fecha_hora
admin   | LOGIN   | SUCCESS   | 172.x.x.x  | 2026-06-15 09:00
```

## Docker Compose

```bash
# Iniciar (compila todo dentro del contenedor)
docker-compose up -d --build

# Logs
docker-compose logs -f api-gateway
docker-compose logs -f ms-cliente

# Detener (conserva datos)
docker-compose down

# Reset total (borra BD)
docker-compose down -v && docker-compose up -d --build
```

Servicios: postgres, redis, registry-server, config-server, api-gateway, ms-cliente, ms-reserva.

## Estructura del proyecto

```
Security/
├── docker-compose.yml
├── init-multiple-dbs.sh        # Crea cliente_db y reserva_db
├── postman_collection.json
├── ms-lib-registry-server/      # Eureka
├── ms-lib-config-server/        # Config Server
├── ms-lib-api-gateway/          # Gateway + JWT + Rate Limit
├── ms-cliente/                  # Auth + Clientes + Auditoría
└── ms-reserva/                  # Reservas
```

Cada microservicio tiene Dockerfile con multi-stage build (Maven dentro del contenedor).
