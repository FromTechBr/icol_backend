package com.br.org.icol.icolbackend.controller;

import com.br.org.icol.icolbackend.dto.AvisoMuralRequestDTO;
import com.br.org.icol.icolbackend.dto.AvisoMuralResponseDTO;
import com.br.org.icol.icolbackend.service.AvisosMuralIcolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/avisos")
public class AvisosMuralIcolController {

    private final AvisosMuralIcolService servicoAvisos;

    public AvisosMuralIcolController(AvisosMuralIcolService service) {
        this.servicoAvisos = service;
    }

    @GetMapping
    public ResponseEntity<List<AvisoMuralResponseDTO>> listar() {
        return ResponseEntity.ok(servicoAvisos.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvisoMuralResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(servicoAvisos.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADORA')")
    public ResponseEntity<AvisoMuralResponseDTO> criar(@Valid @RequestBody AvisoMuralRequestDTO dto) {
        AvisoMuralResponseDTO novo = servicoAvisos.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADORA')")
    public ResponseEntity<AvisoMuralResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody AvisoMuralRequestDTO dto) {
        AvisoMuralResponseDTO atualizado = servicoAvisos.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADORA')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servicoAvisos.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
