package com.transporte.cliente.service;

import com.transporte.cliente.dto.ClienteRequest;
import com.transporte.cliente.dto.ClienteResponse;
import com.transporte.cliente.entity.Cliente;
import com.transporte.cliente.repository.ClienteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public ClienteResponse create(ClienteRequest request) {
        if (clienteRepository.findByDni(request.getDni()).isPresent()) {
            throw new RuntimeException("Ya existe un cliente con DNI: " + request.getDni());
        }
        if (clienteRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Ya existe un cliente con email: " + request.getEmail());
        }

        Cliente cliente = Cliente.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .dni(request.getDni())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .activo(true)
                .build();

        Cliente saved = clienteRepository.save(cliente);
        log.info("Cliente creado: {}", saved.getId());
        return toResponse(saved);
    }

    public ClienteResponse getById(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        return toResponse(cliente);
    }

    public ClienteResponse getByDni(String dni) {
        Cliente cliente = clienteRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con DNI: " + dni));
        return toResponse(cliente);
    }

    public Page<ClienteResponse> getAll(Pageable pageable) {
        return clienteRepository.findAll(pageable).map(this::toResponse);
    }

    public List<ClienteResponse> getAllList() {
        return clienteRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ClienteResponse update(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        if (!cliente.getDni().equals(request.getDni()) && 
            clienteRepository.findByDni(request.getDni()).isPresent()) {
            throw new RuntimeException("Ya existe un cliente con DNI: " + request.getDni());
        }
        if (!cliente.getEmail().equals(request.getEmail()) && 
            clienteRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Ya existe un cliente con email: " + request.getEmail());
        }

        cliente.setNombres(request.getNombres());
        cliente.setApellidos(request.getApellidos());
        cliente.setDni(request.getDni());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());

        Cliente updated = clienteRepository.save(cliente);
        log.info("Cliente actualizado: {}", updated.getId());
        return toResponse(updated);
    }

    public void delete(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        clienteRepository.delete(cliente);
        log.info("Cliente eliminado: {}", id);
    }

    public void deleteByDni(String dni) {
        Cliente cliente = clienteRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con DNI: " + dni));
        clienteRepository.delete(cliente);
        log.info("Cliente eliminado por DNI: {}", dni);
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return ClienteResponse.builder()
                .id(cliente.getId())
                .nombres(cliente.getNombres())
                .apellidos(cliente.getApellidos())
                .dni(cliente.getDni())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .direccion(cliente.getDireccion())
                .activo(cliente.getActivo())
                .fechaCreacion(cliente.getCreatedAt())
                .fechaActualizacion(cliente.getUpdatedAt())
                .build();
    }
}
