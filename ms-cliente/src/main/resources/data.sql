-- Insertar roles
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'Administrador del sistema'),
('OPERADOR', 'Operador de transporte'),
('CLIENTE', 'Cliente del sistema')
ON CONFLICT DO NOTHING;

-- Insertar permisos
INSERT INTO permisos (name, description) VALUES
('CREAR_USUARIO', 'Crear nuevos usuarios'),
('EDITAR_USUARIO', 'Editar usuarios'),
('ELIMINAR_USUARIO', 'Eliminar usuarios'),
('VER_USUARIO', 'Ver información de usuarios'),
('CREAR_RESERVA', 'Crear reservas'),
('EDITAR_RESERVA', 'Editar reservas'),
('ELIMINAR_RESERVA', 'Eliminar reservas'),
('VER_RESERVA', 'Ver reservas'),
('CREAR_PROGRAMACION', 'Crear programación'),
('EDITAR_PROGRAMACION', 'Editar programación'),
('ELIMINAR_PROGRAMACION', 'Eliminar programación'),
('VER_PROGRAMACION', 'Ver programación'),
('CREAR_BUS', 'Crear buses'),
('EDITAR_BUS', 'Editar buses'),
('ELIMINAR_BUS', 'Eliminar buses'),
('VER_BUS', 'Ver buses'),
('CREAR_ASIENTO', 'Crear asientos'),
('EDITAR_ASIENTO', 'Editar asientos'),
('ELIMINAR_ASIENTO', 'Eliminar asientos'),
('VER_ASIENTO', 'Ver asientos'),
('CREAR_DESTINO', 'Crear destinos'),
('EDITAR_DESTINO', 'Editar destinos'),
('ELIMINAR_DESTINO', 'Eliminar destinos'),
('VER_DESTINO', 'Ver destinos'),
('VER_AUDITORIA', 'Ver logs de auditoría')
ON CONFLICT DO NOTHING;

-- Asignar permisos a ADMIN (todos)
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r, permisos p
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Asignar permisos a OPERADOR
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r, permisos p
WHERE r.name = 'OPERADOR' AND p.name IN (
  'CREAR_PROGRAMACION', 'EDITAR_PROGRAMACION', 'ELIMINAR_PROGRAMACION',
  'CREAR_BUS', 'EDITAR_BUS', 'ELIMINAR_BUS',
  'CREAR_ASIENTO', 'EDITAR_ASIENTO', 'ELIMINAR_ASIENTO',
  'CREAR_RESERVA', 'EDITAR_RESERVA', 'ELIMINAR_RESERVA',
  'VER_DESTINO'
)
ON CONFLICT DO NOTHING;

-- Asignar permisos a CLIENTE
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r, permisos p
WHERE r.name = 'CLIENTE' AND p.name IN (
  'CREAR_RESERVA', 'VER_RESERVA',
  'VER_PROGRAMACION', 'VER_ASIENTO', 'VER_DESTINO'
)
ON CONFLICT DO NOTHING;

-- Crear usuario ADMIN de prueba
INSERT INTO usuarios (username, password, email, nombre_completo, activo, fecha_creacion, fecha_actualizacion)
VALUES ('admin', '$2a$10$5X2OYrJLBj2i9X1Q5RXQn.WGzG1lWXB8J.I4J5K5K5K5K5K5K5K', 'admin@transporte.com', 'Administrador', true, NOW(), NOW())
ON CONFLICT (username) DO NOTHING;

-- Asignar rol ADMIN al usuario admin
INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id FROM usuarios u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;
