package com.br.org.icol.icolbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.br.org.icol.icolbackend.model.FrequenciaIcol;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface FrequenciaIcolRepositorio extends JpaRepository<FrequenciaIcol, Long>{
    boolean existsByMatriculaIdAndDataAula(com.br.org.icol.icolbackend.model.MatriculaIcol matricula, java.time.LocalDate dataAula);

    @Query("""
            SELECT f FROM FrequenciaIcol f
            WHERE LOWER(f.matriculaId.alunoMatrId.usuario.emailUsuario) = LOWER(:email)
            """)
    List<FrequenciaIcol> findDoAluno(@Param("email") String email);

    @Query("""
            SELECT f FROM FrequenciaIcol f
            WHERE LOWER(f.matriculaId.turmaMatrId.docente.usuario.emailUsuario) = LOWER(:email)
            """)
    List<FrequenciaIcol> findDasTurmasDoProfessor(@Param("email") String email);

    @Query("""
            SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
            FROM FrequenciaIcol f
            WHERE f.id = :frequenciaId
              AND LOWER(f.matriculaId.turmaMatrId.docente.usuario.emailUsuario) = LOWER(:email)
            """)
    boolean existsDaTurmaDoProfessor(
            @Param("frequenciaId") Long frequenciaId,
            @Param("email") String email
    );

    @Query("""
            SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
            FROM FrequenciaIcol f
            WHERE f.matriculaId.id = :matriculaId
              AND LOWER(f.matriculaId.turmaMatrId.docente.usuario.emailUsuario) = LOWER(:email)
            """)
    boolean matriculaPertenceAoProfessor(
            @Param("matriculaId") Long matriculaId,
            @Param("email") String email
    );
}
