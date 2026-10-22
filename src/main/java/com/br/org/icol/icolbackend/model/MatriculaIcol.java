package com.br.org.icol.icolbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.*;
import java.util.List;
import com.br.org.icol.icolbackend.enums.StatusMatricula;
import com.br.org.icol.icolbackend.enums.SituacaoFinalMatricula;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name="matricula_icol")
@ToString(exclude="frequencias")
@EqualsAndHashCode(exclude="frequencias")
public class MatriculaIcol {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private LocalDate dataMatricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private StatusMatricula statusMatricula;

    @Enumerated(EnumType.STRING)
    @Column(name="situacao_final")
    private SituacaoFinalMatricula situacaoFinal;

    @Column(nullable=false)
    private Boolean ativo = true;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="aluno_id", nullable=false)
    private AlunosIcol alunoMatrId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="turma_id", nullable=false)
    private TurmasIcol turmaMatrId;

    @JsonIgnore
    @OneToMany(mappedBy="matriculaId", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<FrequenciaIcol> frequencias;
}
