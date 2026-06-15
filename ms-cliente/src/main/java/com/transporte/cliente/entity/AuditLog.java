package com.transporte.cliente.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String usuario;

    @Column(nullable = false)
    private String accion;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String detalles;

    @Column(nullable = false)
    private String resultado;

    @Column(name = "fecha_hora")
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
