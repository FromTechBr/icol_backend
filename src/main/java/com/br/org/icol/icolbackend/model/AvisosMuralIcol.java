package com.br.org.icol.icolbackend.model;

import java.time.*;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="avisos_mural")
public class AvisosMuralIcol {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="usuario_id", nullable=false)
    private UsuariosIcol autor;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="turma_id")
    private TurmasIcol turma;

    @Column(nullable=false)
    private String titulo;
    @Column(nullable=false)
    private String conteudo;
    @Column(nullable=false)
    private LocalDateTime dataPostagem;

    @Column(nullable=false)
    private Boolean ativo = true;
}
