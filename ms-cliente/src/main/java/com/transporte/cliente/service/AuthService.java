package com.transporte.cliente.service;

import com.transporte.cliente.dto.LoginRequest;
import com.transporte.cliente.dto.RegisterRequest;
import com.transporte.cliente.entity.Role;
import com.transporte.cliente.entity.User;
import com.transporte.cliente.repository.AuditLogRepository;
import com.transporte.cliente.repository.RoleRepository;
import com.transporte.cliente.repository.UserRepository;
import com.transporte.cliente.entity.AuditLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogRepository auditLogRepository;

    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            auditLog("SYSTEM", "REGISTER", "FAILED", 
                    "Usuario ya existe: " + request.getUsername());
            throw new RuntimeException("Usuario ya existe");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            auditLog("SYSTEM", "REGISTER", "FAILED", 
                    "Email ya existe: " + request.getEmail());
            throw new RuntimeException("Email ya existe");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .active(true)
                .build();

        // Asignar rol CLIENTE por defecto
        Role clientRole = roleRepository.findByName("CLIENTE")
                .orElseThrow(() -> new RuntimeException("Rol CLIENTE no existe"));
        
        Set<Role> roles = new HashSet<>();
        roles.add(clientRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        auditLog(savedUser.getUsername(), "REGISTER", "SUCCESS", 
                "Usuario registrado correctamente");
        return savedUser;
    }

    public User authenticate(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null || !user.getActive()) {
            auditLog(request.getUsername(), "LOGIN", "FAILED", 
                    "Usuario no existe o está inactivo");
            throw new RuntimeException("Credenciales inválidas");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            auditLog(user.getUsername(), "LOGIN", "FAILED", 
                    "Password incorrecta");
            throw new RuntimeException("Credenciales inválidas");
        }

        auditLog(user.getUsername(), "LOGIN", "SUCCESS", 
                "Login exitoso");
        return user;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private void auditLog(String username, String action, String result, String details) {
        try {
            String ipAddress = getClientIpAddress();
            
            AuditLog log = AuditLog.builder()
                    .usuario(username)
                    .accion(action)
                    .resultado(result)
                    .detalles(details)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(log);
        } catch (Exception e) {
            log.error("Error al guardar audit log: {}", e.getMessage());
        }
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0];
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("No request context available");
        }
        return "N/A";
    }
}
