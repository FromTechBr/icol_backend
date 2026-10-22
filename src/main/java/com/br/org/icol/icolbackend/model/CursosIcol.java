package com.br.org.icol.icolbackend.model;
import com.br.org.icol.icolbackend.enums.TiposCursos;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.util.List;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name="cursos_icol")
@ToString(exclude="turmas")
@EqualsAndHashCode(exclude="turmas")
public class CursosIcol {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String nomeCurso;
    @Column(nullable=false)
    private String descricao;
    @Column(nullable=false)
    private String duracao;
    @Column(nullable=false)
    private Boolean ativo=true;

    @Column(nullable=false)
    private Boolean matriculaAutomatica = false;

    @Column(precision=5, scale=2)
    private BigDecimal limiteFaltasPercentual;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TiposCursos categoria;

    @JsonIgnore
    @OneToMany(mappedBy="curso", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<TurmasIcol> turmas;
}
