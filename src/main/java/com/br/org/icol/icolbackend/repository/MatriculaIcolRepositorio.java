package com.br.org.icol.icolbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.br.org.icol.icolbackend.model.MatriculaIcol;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatriculaIcolRepositorio extends JpaRepository<MatriculaIcol, Long>{
    boolean existsByAlunoMatrIdAndTurmaMatrId(com.br.org.icol.icolbackend.model.AlunosIcol aluno, com.br.org.icol.icolbackend.model.TurmasIcol turma);
    
    long countByTurmaMatrIdAndStatusMatricula(com.br.org.icol.icolbackend.model.TurmasIcol turma, com.br.org.icol.icolbackend.enums.StatusMatricula status);

    @Query("""
            SELECT m FROM MatriculaIcol m
            WHERE m.ativo = true AND LOWER(m.alunoMatrId.usuario.emailUsuario) = LOWER(:email)
            """)
    List<MatriculaIcol> findAtivasDoAluno(@Param("email") String email);

    @Query("""
            SELECT m FROM MatriculaIcol m
            WHERE m.ativo = true
              AND LOWER(m.turmaMatrId.docente.usuario.emailUsuario) = LOWER(:email)
            """)
    List<MatriculaIcol> findAtivasDasTurmasDoProfessor(@Param("email") String email);

    @Query("""
            SELECT m FROM MatriculaIcol m
            WHERE m.id = :id AND m.ativo = true
              AND LOWER(m.alunoMatrId.usuario.emailUsuario) = LOWER(:email)
            """)
    Optional<MatriculaIcol> findAtivaDoAluno(
            @Param("id") Long id,
            @Param("email") String email
    );

    @Query("""
            SELECT m FROM MatriculaIcol m
            WHERE m.id = :id AND m.ativo = true
              AND LOWER(m.turmaMatrId.docente.usuario.emailUsuario) = LOWER(:email)
            """)
    Optional<MatriculaIcol> findAtivaDaTurmaDoProfessor(
            @Param("id") Long id,
            @Param("email") String email
    );
}
