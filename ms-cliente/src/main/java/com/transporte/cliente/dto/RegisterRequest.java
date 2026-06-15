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
public class RegisterRequest {
    @NotBlank(message = "Username es requerido")
    private String username;

    @NotBlank(message = "Password es requerido")
    private String password;

    @Email(message = "Email debe ser válido")
    @NotBlank(message = "Email es requerido")
    private String email;

    private String fullName;
}
