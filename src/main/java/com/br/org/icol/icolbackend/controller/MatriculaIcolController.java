package com.br.org.icol.icolbackend.controller;

import com.br.org.icol.icolbackend.dto.MatriculaRequestDTO;
import com.br.org.icol.icolbackend.dto.MatriculaResponseDTO;
import com.br.org.icol.icolbackend.service.MatriculaIcolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/matriculas")
public class MatriculaIcolController {

    private final MatriculaIcolService servicoMatricula;

    public MatriculaIcolController(MatriculaIcolService service) {
        this.servicoMatricula = service;
    }

    @GetMapping
    public ResponseEntity<List<MatriculaResponseDTO>> listar() {
        return ResponseEntity.ok(servicoMatricula.listarVisiveis());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatriculaResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(servicoMatricula.buscarVisivel(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ALUNO', 'COORDENADORA')")
    public ResponseEntity<MatriculaResponseDTO> criar(@Valid @RequestBody MatriculaRequestDTO dto) {
        MatriculaResponseDTO novo = servicoMatricula.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COORDENADORA')")
    public ResponseEntity<MatriculaResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody MatriculaRequestDTO dto) {
        MatriculaResponseDTO atualizado = servicoMatricula.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COORDENADORA')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servicoMatricula.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
