package com.br.org.icol.icolbackend.controller;

import com.br.org.icol.icolbackend.dto.LoginRequestDTO;
import com.br.org.icol.icolbackend.dto.LoginResponseDTO;
import com.br.org.icol.icolbackend.dto.UsuarioAutenticadoDTO;
import com.br.org.icol.icolbackend.service.AutenticacaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(
                autenticacaoService.login(request, servletRequest.getRemoteAddr())
        );
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioAutenticadoDTO> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(autenticacaoService.usuarioAutenticado(jwt.getSubject()));
    }
}
