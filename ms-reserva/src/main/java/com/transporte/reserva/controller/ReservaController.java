package com.transporte.reserva.controller;

import com.transporte.reserva.entity.Reserva;
import com.transporte.reserva.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reserva")
@Tag(name = "Reservas", description = "Gestión de reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @PostMapping
    @Operation(summary = "Crear reserva", description = "Crea una nueva reserva")
    public ResponseEntity<Reserva> crear(@RequestBody Reserva reserva) {
        Reserva creada = reservaService.crear(reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reserva por ID", description = "Retorna una reserva por su ID")
    public ResponseEntity<Reserva> obtenerPorId(@PathVariable Long id) {
        return reservaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar todas las reservas", description = "Retorna todas las reservas")
    public ResponseEntity<List<Reserva>> obtenerTodas() {
        return ResponseEntity.ok(reservaService.obtenerTodas());
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Obtener reservas por cliente", description = "Retorna reservas de un cliente")
    public ResponseEntity<List<Reserva>> obtenerPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(reservaService.obtenerPorCliente(clienteId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar reserva", description = "Actualiza una reserva existente")
    public ResponseEntity<Reserva> actualizar(@PathVariable Long id, @RequestBody Reserva reserva) {
        Reserva actualizada = reservaService.actualizar(id, reserva);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar reserva", description = "Cancela una reserva")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        reservaService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
