package com.br.org.icol.icolbackend.controller;

import com.br.org.icol.icolbackend.dto.TurmaRequestDTO;
import com.br.org.icol.icolbackend.dto.TurmaResponseDTO;
import com.br.org.icol.icolbackend.service.TurmasIcolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/turmas")
public class TurmasIcolController {

    private final TurmasIcolService servicoTurmas;

    public TurmasIcolController(TurmasIcolService service) {
        this.servicoTurmas = service;
    }

    @GetMapping
    public ResponseEntity<List<TurmaResponseDTO>> listar() {
        return ResponseEntity.ok(servicoTurmas.listarAbertas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurmaResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(servicoTurmas.buscarAberta(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('COORDENADORA')")
    public ResponseEntity<TurmaResponseDTO> criar(@Valid @RequestBody TurmaRequestDTO dto) {
        TurmaResponseDTO novo = servicoTurmas.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COORDENADORA')")
    public ResponseEntity<TurmaResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody TurmaRequestDTO dto) {
        TurmaResponseDTO atualizado = servicoTurmas.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COORDENADORA')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servicoTurmas.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
