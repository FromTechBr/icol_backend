package com.br.org.icol.icolbackend.dto;

import com.br.org.icol.icolbackend.enums.TiposUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioRequestDTO {
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    private String email;

    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
    private String senha;

    @NotNull(message = "O tipo de usuário é obrigatório.")
    private TiposUsuario tipoUsuario;
}
