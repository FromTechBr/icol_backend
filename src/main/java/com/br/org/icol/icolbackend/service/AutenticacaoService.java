package com.br.org.icol.icolbackend.service;

import com.br.org.icol.icolbackend.dto.LoginRequestDTO;
import com.br.org.icol.icolbackend.dto.LoginResponseDTO;
import com.br.org.icol.icolbackend.dto.UsuarioAutenticadoDTO;
import com.br.org.icol.icolbackend.exception.CredenciaisInvalidasException;
import com.br.org.icol.icolbackend.exception.LimiteLoginExcedidoException;
import com.br.org.icol.icolbackend.model.UsuariosIcol;
import com.br.org.icol.icolbackend.repository.UsuariosIcolRepositorio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class AutenticacaoService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UsuariosIcolRepositorio usuariosRepositorio;
    private final LoginRateLimiter loginRateLimiter;
    private final Duration tokenDuration;

    public AutenticacaoService(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            UsuariosIcolRepositorio usuariosRepositorio,
            LoginRateLimiter loginRateLimiter,
            @Value("${jwt.expiration}") long expirationMillis
    ) {
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("JWT_EXPIRATION deve ser positivo.");
        }
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.usuariosRepositorio = usuariosRepositorio;
        this.loginRateLimiter = loginRateLimiter;
        this.tokenDuration = Duration.ofMillis(expirationMillis);
    }

    public LoginResponseDTO login(LoginRequestDTO request, String remoteAddress) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!loginRateLimiter.tryAcquire(email, remoteAddress)) {
            throw new LimiteLoginExcedidoException();
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getSenha())
            );
        } catch (AuthenticationException exception) {
            throw new CredenciaisInvalidasException();
        }

        UsuariosIcol usuario = usuariosRepositorio.findByEmailUsuarioIgnoreCase(email)
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .orElseThrow(CredenciaisInvalidasException::new);

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(tokenDuration);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("icol-api")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(usuario.getEmailUsuario())
                .claim("roles", List.of(usuario.getTipoUsuario().name()))
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(),
                        claims
                )
        ).getTokenValue();

        return new LoginResponseDTO(
                token,
                "Bearer",
                tokenDuration.toSeconds(),
                usuario.getEmailUsuario(),
                usuario.getTipoUsuario()
        );
    }

    @Transactional(readOnly = true)
    public UsuarioAutenticadoDTO usuarioAutenticado(String email) {
        UsuariosIcol usuario = usuariosRepositorio.findByEmailUsuarioIgnoreCase(email)
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .orElseThrow(CredenciaisInvalidasException::new);
        return new UsuarioAutenticadoDTO(
                usuario.getId(),
                usuario.getEmailUsuario(),
                usuario.getTipoUsuario(),
                Boolean.TRUE.equals(usuario.getAtivo())
        );
    }
}
