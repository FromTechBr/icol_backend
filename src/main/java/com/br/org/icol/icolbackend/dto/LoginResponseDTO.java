package com.br.org.icol.icolbackend.dto;

import com.br.org.icol.icolbackend.enums.TiposUsuario;

public record LoginResponseDTO(
        String accessToken,
        String tokenType,
        long expiresIn,
        String email,
        TiposUsuario tipoUsuario
) {
}
