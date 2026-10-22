package com.br.org.icol.icolbackend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.br.org.icol.icolbackend.enums.TiposUsuario;
import com.br.org.icol.icolbackend.model.UsuariosIcol;


@Repository
public interface UsuariosIcolRepositorio extends JpaRepository<UsuariosIcol, Long>{
    Optional<UsuariosIcol> findByEmailUsuario(String emailUsuario);
    Optional<UsuariosIcol> findByEmailUsuarioIgnoreCase(String emailUsuario);
    boolean existsByTipoUsuario(TiposUsuario tipoUsuario);
}
