package com.br.org.icol.icolbackend.controller;

import com.br.org.icol.icolbackend.dto.DocenteRequestDTO;
import com.br.org.icol.icolbackend.dto.DocenteResponseDTO;
import com.br.org.icol.icolbackend.service.DocentesIcolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/docentes")
@PreAuthorize("hasRole('COORDENADORA')")
public class DocentesIcolController {

    private final DocentesIcolService servicoDocentes;

    public DocentesIcolController(DocentesIcolService service) {
        this.servicoDocentes = service;
    }

    @GetMapping
    public ResponseEntity<List<DocenteResponseDTO>> listar() {
        return ResponseEntity.ok(servicoDocentes.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocenteResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(servicoDocentes.buscar(id));
    }

    @PostMapping
    public ResponseEntity<DocenteResponseDTO> criar(@Valid @RequestBody DocenteRequestDTO dto) {
        DocenteResponseDTO novo = servicoDocentes.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocenteResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody DocenteRequestDTO dto) {
        DocenteResponseDTO atualizado = servicoDocentes.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servicoDocentes.inativar(id);
        return ResponseEntity.noContent().build();
    }
}