package com.br.org.icol.icolbackend.dto;

import com.br.org.icol.icolbackend.enums.TiposUsuario;

public record UsuarioAutenticadoDTO(
        Long id,
        String email,
        TiposUsuario tipoUsuario,
        boolean ativo
) {
}
