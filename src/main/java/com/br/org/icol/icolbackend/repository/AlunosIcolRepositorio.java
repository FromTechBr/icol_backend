package com.br.org.icol.icolbackend.repository;

import com.br.org.icol.icolbackend.model.AlunosIcol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface  AlunosIcolRepositorio extends JpaRepository<AlunosIcol, Long>{
    Optional<AlunosIcol> findByCpf(String cpf);
    
    // Busca por CPF apenas entre alunos ativos
    @Query("SELECT a FROM AlunosIcol a WHERE a.cpf = :cpf AND a.ativo = true")
    Optional<AlunosIcol> findByCpfAndAtivo(@Param("cpf") String cpf);
    
    // Lista todos os alunos ativos
    @Query("SELECT a FROM AlunosIcol a WHERE a.ativo = true ORDER BY a.nomeCompleto ASC")
    List<AlunosIcol> findAllAtivos();
    
    // Busca aluno por ID apenas se ativo
    @Query("SELECT a FROM AlunosIcol a WHERE a.id = :id AND a.ativo = true")
    Optional<AlunosIcol> findByIdAndAtivo(@Param("id") Long id);

    boolean existsByUsuario_Id(Long usuarioId);

    @Query("""
            SELECT a FROM AlunosIcol a
            WHERE a.ativo = true AND LOWER(a.usuario.emailUsuario) = LOWER(:email)
            ORDER BY a.nomeCompleto ASC
            """)
    List<AlunosIcol> findAtivosDoUsuario(@Param("email") String email);

    @Query("""
            SELECT a FROM AlunosIcol a
            JOIN a.matriculas m
            JOIN m.turmaMatrId t
            WHERE a.ativo = true
              AND t.ativo = true
              AND LOWER(t.docente.usuario.emailUsuario) = LOWER(:email)
            ORDER BY a.nomeCompleto ASC
            """)
    List<AlunosIcol> findAtivosNasTurmasDoProfessor(@Param("email") String email);

    @Query("""
            SELECT a FROM AlunosIcol a
            WHERE a.id = :id AND a.ativo = true
              AND LOWER(a.usuario.emailUsuario) = LOWER(:email)
            """)
    Optional<AlunosIcol> findAtivoDoUsuario(@Param("id") Long id, @Param("email") String email);

    @Query("""
            SELECT a FROM AlunosIcol a
            JOIN a.matriculas m
            JOIN m.turmaMatrId t
            WHERE a.id = :id AND a.ativo = true AND t.ativo = true
              AND LOWER(t.docente.usuario.emailUsuario) = LOWER(:email)
            """)
    Optional<AlunosIcol> findAtivoNasTurmasDoProfessor(
            @Param("id") Long id,
            @Param("email") String email
    );
}
