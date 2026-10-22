package com.br.org.icol.icolbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.br.org.icol.icolbackend.model.TurmasIcol;

@Repository
public interface TurmasIcolRepositorio extends JpaRepository<TurmasIcol, Long>{
    @org.springframework.data.jpa.repository.Query("SELECT t FROM TurmasIcol t WHERE t.ativo = true")
    java.util.List<TurmasIcol> findAllAtivos();

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TurmasIcol t WHERE t.id = :id AND t.ativo = true")
    java.util.Optional<TurmasIcol> findByIdAndAtivo(@org.springframework.data.repository.query.Param("id") Long id);

    @org.springframework.data.jpa.repository.Query(
            "SELECT t FROM TurmasIcol t WHERE t.ativo = true AND t.statusTurma = "
                    + "com.br.org.icol.icolbackend.enums.StatusTurma.ABERTA"
    )
    java.util.List<TurmasIcol> findAllAtivasAbertas();

    @org.springframework.data.jpa.repository.Query(
            "SELECT t FROM TurmasIcol t WHERE t.id = :id AND t.ativo = true AND t.statusTurma = "
                    + "com.br.org.icol.icolbackend.enums.StatusTurma.ABERTA"
    )
    java.util.Optional<TurmasIcol> findByIdAtivaAberta(
            @org.springframework.data.repository.query.Param("id") Long id
    );
}
