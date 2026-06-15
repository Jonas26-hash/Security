package com.transporte.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequest {
    @NotBlank(message = "Nombres es requerido")
    private String nombres;

    @NotBlank(message = "Apellidos es requerido")
    private String apellidos;

    @NotBlank(message = "DNI es requerido")
    private String dni;

    @Email(message = "Email debe ser válido")
    @NotBlank(message = "Email es requerido")
    private String email;

    private String telefono;
    private String direccion;
}
