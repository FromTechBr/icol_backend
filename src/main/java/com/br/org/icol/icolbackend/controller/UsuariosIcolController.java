package com.br.org.icol.icolbackend.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.br.org.icol.icolbackend.service.UsuariosIcolService;
import com.br.org.icol.icolbackend.dto.UsuarioRequestDTO;
import com.br.org.icol.icolbackend.dto.UsuarioResponseDTO;

@RestController
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('COORDENADORA')")
public class UsuariosIcolController {
    private final UsuariosIcolService servicoUsuarios;
    
    public UsuariosIcolController(UsuariosIcolService service){
        this.servicoUsuarios=service;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar(){
        return ResponseEntity.ok(servicoUsuarios.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscar(@PathVariable Long id){
        return ResponseEntity.ok(servicoUsuarios.buscar(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO dto){
        UsuarioResponseDTO novo = servicoUsuarios.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequestDTO dto){
        UsuarioResponseDTO atualizado = servicoUsuarios.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id){
        servicoUsuarios.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
