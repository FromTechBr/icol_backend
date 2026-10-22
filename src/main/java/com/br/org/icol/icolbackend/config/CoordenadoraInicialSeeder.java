package com.br.org.icol.icolbackend.config;

import com.br.org.icol.icolbackend.enums.TiposUsuario;
import com.br.org.icol.icolbackend.model.UsuariosIcol;
import com.br.org.icol.icolbackend.repository.UsuariosIcolRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CoordenadoraInicialSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(CoordenadoraInicialSeeder.class);

    private final UsuariosIcolRepositorio usuariosRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;

    public CoordenadoraInicialSeeder(
            UsuariosIcolRepositorio usuariosRepositorio,
            PasswordEncoder passwordEncoder,
            @Value("${ICOL_ADMIN_EMAIL:}") String email,
            @Value("${ICOL_ADMIN_PASSWORD:}") String senha
    ) {
        this.usuariosRepositorio = usuariosRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuariosRepositorio.existsByTipoUsuario(TiposUsuario.COORDENADORA)) {
            return;
        }

        if (email.isBlank() || senha.isBlank()) {
            logger.warn(
                    "Nenhuma coordenadora existe e ICOL_ADMIN_EMAIL/ICOL_ADMIN_PASSWORD "
                            + "não foram configuradas; seed inicial ignorado."
            );
            return;
        }

        UsuariosIcol coordenadora = new UsuariosIcol();
        coordenadora.setEmailUsuario(email.trim());
        coordenadora.setSenha(passwordEncoder.encode(senha));
        coordenadora.setAtivo(true);
        coordenadora.setTipoUsuario(TiposUsuario.COORDENADORA);
        usuariosRepositorio.save(coordenadora);

        logger.info("Coordenadora inicial criada para o e-mail configurado.");
    }
}
