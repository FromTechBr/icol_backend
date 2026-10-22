package com.br.org.icol.icolbackend.service;

import com.br.org.icol.icolbackend.exception.RequisicaoNaoEncontrada;
import com.br.org.icol.icolbackend.model.AvisosMuralIcol;
import com.br.org.icol.icolbackend.model.UsuariosIcol;
import com.br.org.icol.icolbackend.repository.AvisosMuralIcolRepositorio;
import com.br.org.icol.icolbackend.repository.UsuariosIcolRepositorio;
import com.br.org.icol.icolbackend.service.AcessoUsuario;
import com.br.org.icol.icolbackend.dto.AvisoMuralRequestDTO;
import com.br.org.icol.icolbackend.dto.AvisoMuralResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AvisosMuralIcolService {
    
    private final AvisosMuralIcolRepositorio repoAvisos;
    private final UsuariosIcolRepositorio repoUsuarios;
    private final AcessoUsuario acessoUsuario;

    public AvisosMuralIcolService(AvisosMuralIcolRepositorio repoAvisos, 
                                 UsuariosIcolRepositorio repoUsuarios,
                                 AcessoUsuario acessoUsuario) {
        this.repoAvisos = repoAvisos;
        this.repoUsuarios = repoUsuarios;
        this.acessoUsuario = acessoUsuario;
    }

    private AvisoMuralResponseDTO toDTO(AvisosMuralIcol entidade) {
        AvisoMuralResponseDTO dto = new AvisoMuralResponseDTO();
        dto.setId(entidade.getId());
        dto.setTitulo(entidade.getTitulo());
        dto.setConteudo(entidade.getConteudo());
        dto.setDataPostagem(entidade.getDataPostagem());
        if (entidade.getAutor() != null) {
            dto.setAutorId(entidade.getAutor().getId());
            dto.setEmailAutor(entidade.getAutor().getEmailUsuario());
        }
        return dto;
    }

    public AvisosMuralIcol buscarEntidade(Long id) {
        return repoAvisos.findById(id)
                .orElseThrow(() -> new RequisicaoNaoEncontrada("Aviso não encontrado com ID: " + id));
    }

    public List<AvisoMuralResponseDTO> listar() {
        return repoAvisos.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AvisoMuralResponseDTO buscar(Long id) {
        return toDTO(buscarEntidade(id));
    }

    public AvisoMuralResponseDTO criar(AvisoMuralRequestDTO dto) {
        UsuariosIcol autorExistente = repoUsuarios.findByEmailUsuarioIgnoreCase(
                        acessoUsuario.emailAtual())
                .orElseThrow(() -> new RequisicaoNaoEncontrada("Autor (Usuário) não encontrado."));

        AvisosMuralIcol avisoCadastrar = new AvisosMuralIcol();
        avisoCadastrar.setAutor(autorExistente);
        avisoCadastrar.setTitulo(dto.getTitulo());
        avisoCadastrar.setConteudo(dto.getConteudo());
        avisoCadastrar.setDataPostagem(LocalDateTime.now());
        
        AvisosMuralIcol salvo = repoAvisos.save(avisoCadastrar);
        return toDTO(salvo);
    }

    public AvisoMuralResponseDTO atualizar(Long id, AvisoMuralRequestDTO dto) {
        garantirAutorOuCoordenadora(id);
        AvisosMuralIcol existente = buscarEntidade(id);
        
        existente.setTitulo(dto.getTitulo());
        existente.setConteudo(dto.getConteudo());
        
        AvisosMuralIcol salvo = repoAvisos.save(existente);
        return toDTO(salvo);
    }

    public void deletar(Long id) {
        garantirAutorOuCoordenadora(id);
        if (!repoAvisos.existsById(id)) {
            throw new RequisicaoNaoEncontrada("Aviso não encontrado para exclusão.");
        }
        repoAvisos.deleteById(id);
    }

    private void garantirAutorOuCoordenadora(Long id) {
        if (!acessoUsuario.ehCoordenadora()
                && (!acessoUsuario.possuiPapel("PROFESSOR")
                || !repoAvisos.pertenceAoAutor(id, acessoUsuario.emailAtual()))) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Só o autor pode alterar ou remover este aviso."
            );
        }
    }
}
