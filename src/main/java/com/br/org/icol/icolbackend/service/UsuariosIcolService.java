package com.br.org.icol.icolbackend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.br.org.icol.icolbackend.exception.RequisicaoNaoEncontrada;
import com.br.org.icol.icolbackend.exception.RequisicaoInvalida;
import com.br.org.icol.icolbackend.model.UsuariosIcol;
import com.br.org.icol.icolbackend.repository.UsuariosIcolRepositorio;
import com.br.org.icol.icolbackend.dto.UsuarioRequestDTO;
import com.br.org.icol.icolbackend.dto.UsuarioResponseDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuariosIcolService {
    private final UsuariosIcolRepositorio repoUsuarios; //declaro que a variavel repo é do tipo repositorio e que ele quem vai receber as informações para od
    private final PasswordEncoder passwordEncoder;

    public UsuariosIcolService(UsuariosIcolRepositorio repoUsuarios, PasswordEncoder passwordEncoder){
        this.repoUsuarios = repoUsuarios;
        this.passwordEncoder=passwordEncoder;
    }
    
    // Método auxiliar para converter Entidade -> DTO
    private UsuarioResponseDTO toDTO(UsuariosIcol entidade) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(entidade.getId());
        dto.setEmail(entidade.getEmailUsuario());
        dto.setTipoUsuario(entidade.getTipoUsuario());
        dto.setAtivo(entidade.getAtivo());
        return dto;
    }

    // Método auxiliar para buscar a entidade internamente
    public UsuariosIcol buscarEntidade(Long id){
        return repoUsuarios.findById(id).orElseThrow(()-> new RequisicaoNaoEncontrada("Não foi possivel encontrar usuario com ID: "+id));
    }

    public List<UsuarioResponseDTO> listar(){
        return repoUsuarios.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public UsuarioResponseDTO buscar(Long id){
        return toDTO(buscarEntidade(id));
    }

    public UsuarioResponseDTO criar(UsuarioRequestDTO dto){
        if (dto.getSenha() == null || dto.getSenha().isBlank()) {
            throw new RequisicaoInvalida("A senha é obrigatória.");
        }
        if(repoUsuarios.findByEmailUsuario(dto.getEmail()).isPresent()){
            throw new RequisicaoInvalida("Este email já está cadastrado!");
        }
        
        UsuariosIcol usuarioCadastrar = new UsuariosIcol();
        usuarioCadastrar.setEmailUsuario(dto.getEmail());
        usuarioCadastrar.setAtivo(true);
        usuarioCadastrar.setTipoUsuario(dto.getTipoUsuario());
        
        String senhaCodificada = passwordEncoder.encode(dto.getSenha());
        usuarioCadastrar.setSenha(senhaCodificada);
        
        UsuariosIcol salvo = repoUsuarios.save(usuarioCadastrar);
        return toDTO(salvo);
    }

    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto){
        UsuariosIcol existente = buscarEntidade(id);
        Optional<UsuariosIcol> usuarioComEsseEmail = repoUsuarios.findByEmailUsuario(dto.getEmail());

        if(usuarioComEsseEmail.isPresent() && !usuarioComEsseEmail.get().getId().equals(id)){
            throw new RequisicaoInvalida("Este email já está sendo usado por outro usuário!");
        }
        
        existente.setEmailUsuario(dto.getEmail());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        existente.setTipoUsuario(dto.getTipoUsuario());
        
        UsuariosIcol salvo = repoUsuarios.save(existente);
        return toDTO(salvo);
    }

    public void inativar(Long id){
        UsuariosIcol usuarioAdeletar = buscarEntidade(id);
        usuarioAdeletar.setAtivo(false);
        repoUsuarios.save(usuarioAdeletar);
    }

}
