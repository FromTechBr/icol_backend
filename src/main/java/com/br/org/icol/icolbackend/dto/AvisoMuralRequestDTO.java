package com.br.org.icol.icolbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AvisoMuralRequestDTO {
    private Long autorId;

    @NotBlank(message = "O título é obrigatório.")
    private String titulo;

    @NotBlank(message = "O conteúdo é obrigatório.")
    private String conteudo;
}
