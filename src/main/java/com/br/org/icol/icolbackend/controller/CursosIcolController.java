package com.br.org.icol.icolbackend.controller;

import com.br.org.icol.icolbackend.dto.CursoRequestDTO;
import com.br.org.icol.icolbackend.dto.CursoResponseDTO;
import com.br.org.icol.icolbackend.service.CursosIcolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/cursos")
public class CursosIcolController {

    private final CursosIcolService servicoCursos;

    public CursosIcolController(CursosIcolService service) {
        this.servicoCursos = service;
    }

    @GetMapping
    public ResponseEntity<List<CursoResponseDTO>> listar() {
        return ResponseEntity.ok(servicoCursos.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(servicoCursos.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('COORDENADORA')")
    public ResponseEntity<CursoResponseDTO> criar(@Valid @RequestBody CursoRequestDTO dto) {
        CursoResponseDTO novo = servicoCursos.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COORDENADORA')")
    public ResponseEntity<CursoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody CursoRequestDTO dto) {
        CursoResponseDTO atualizado = servicoCursos.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COORDENADORA')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servicoCursos.inativar(id);
        return ResponseEntity.noContent().build();
    }
}