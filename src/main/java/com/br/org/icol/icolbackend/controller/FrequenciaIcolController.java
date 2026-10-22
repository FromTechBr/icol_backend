package com.br.org.icol.icolbackend.controller;

import com.br.org.icol.icolbackend.service.FrequenciaIcolService;
import com.br.org.icol.icolbackend.dto.FrequenciaRequestDTO;
import com.br.org.icol.icolbackend.dto.FrequenciaResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/frequencias")
public class FrequenciaIcolController {

    private final FrequenciaIcolService servicoFrequencia;

    public FrequenciaIcolController(FrequenciaIcolService service) {
        this.servicoFrequencia = service;
    }

    @GetMapping
    public ResponseEntity<List<FrequenciaResponseDTO>> listar() {
        return ResponseEntity.ok(servicoFrequencia.listarVisiveis());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FrequenciaResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(servicoFrequencia.buscarVisivel(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADORA')")
    public ResponseEntity<FrequenciaResponseDTO> registrar(@Valid @RequestBody FrequenciaRequestDTO dto) {
        FrequenciaResponseDTO novo = servicoFrequencia.registrarPorProfessorOuCoordenadora(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADORA')")
    public ResponseEntity<FrequenciaResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody FrequenciaRequestDTO dto) {
        FrequenciaResponseDTO atualizado =
                servicoFrequencia.atualizarPorProfessorOuCoordenadora(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    // DELETE endpoint removido para proteger o histórico (Regra 6)
}
