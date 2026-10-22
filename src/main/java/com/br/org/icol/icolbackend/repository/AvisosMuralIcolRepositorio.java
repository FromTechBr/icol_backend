package com.br.org.icol.icolbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.br.org.icol.icolbackend.model.AvisosMuralIcol;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AvisosMuralIcolRepositorio extends JpaRepository<AvisosMuralIcol, Long>{
    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM AvisosMuralIcol a
            WHERE a.id = :id
              AND LOWER(a.autor.emailUsuario) = LOWER(:email)
            """)
    boolean pertenceAoAutor(@Param("id") Long id, @Param("email") String email);
}
