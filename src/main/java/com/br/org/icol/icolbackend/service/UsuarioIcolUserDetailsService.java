package com.br.org.icol.icolbackend.service;

import com.br.org.icol.icolbackend.model.UsuariosIcol;
import com.br.org.icol.icolbackend.repository.UsuariosIcolRepositorio;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioIcolUserDetailsService implements UserDetailsService {

    private final UsuariosIcolRepositorio usuariosRepositorio;

    public UsuarioIcolUserDetailsService(UsuariosIcolRepositorio usuariosRepositorio) {
        this.usuariosRepositorio = usuariosRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsuariosIcol usuario = usuariosRepositorio.findByEmailUsuarioIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new UsernameNotFoundException("Credenciais inválidas.");
        }

        return User.withUsername(usuario.getEmailUsuario())
                .password(usuario.getSenha())
                .roles(usuario.getTipoUsuario().name())
                .build();
    }
}
