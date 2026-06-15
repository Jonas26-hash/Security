package com.transporte.cliente.controller;

import com.transporte.cliente.dto.ClienteRequest;
import com.transporte.cliente.dto.ClienteResponse;
import com.transporte.cliente.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cliente")
@Tag(name = "Clientes", description = "Gestión de clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Crear cliente", description = "Registra un nuevo cliente")
    public ResponseEntity<ClienteResponse> create(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Retorna un cliente por su ID")
    public ResponseEntity<ClienteResponse> getById(@PathVariable Long id) {
        ClienteResponse response = clienteService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dni/{dni}")
    @Operation(summary = "Obtener cliente por DNI", description = "Retorna un cliente por su DNI")
    public ResponseEntity<ClienteResponse> getByDni(@PathVariable String dni) {
        ClienteResponse response = clienteService.getByDni(dni);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Retorna lista paginada de clientes")
    public ResponseEntity<Page<ClienteResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ClienteResponse> response = clienteService.getAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos los clientes", description = "Retorna todos los clientes sin paginación")
    public ResponseEntity<List<ClienteResponse>> getAllList() {
        List<ClienteResponse> response = clienteService.getAllList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza un cliente existente")
    public ResponseEntity<ClienteResponse> update(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente", description = "Elimina un cliente por ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/dni/{dni}")
    @Operation(summary = "Eliminar cliente por DNI", description = "Elimina un cliente por su DNI")
    public ResponseEntity<Void> deleteByDni(@PathVariable String dni) {
        clienteService.deleteByDni(dni);
        return ResponseEntity.noContent().build();
    }
}
