package com.transporte.reserva.service;

import com.transporte.reserva.entity.Reserva;
import com.transporte.reserva.repository.ReservaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    public Reserva crear(Reserva reserva) {
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setEstado("CONFIRMADA");
        return reservaRepository.save(reserva);
    }

    public Optional<Reserva> obtenerPorId(Long id) {
        return reservaRepository.findById(id);
    }

    public List<Reserva> obtenerTodas() {
        return reservaRepository.findAll();
    }

    public List<Reserva> obtenerPorCliente(Long clienteId) {
        return reservaRepository.findByClienteId(clienteId);
    }

    public Reserva actualizar(Long id, Reserva reserva) {
        Reserva existente = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        existente.setEstado(reserva.getEstado());
        existente.setAsientoId(reserva.getAsientoId());
        return reservaRepository.save(existente);
    }

    public void cancelar(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        reserva.setEstado("CANCELADA");
        reservaRepository.save(reserva);
    }
}
