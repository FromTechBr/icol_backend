package com.br.org.icol.icolbackend.service;

import com.br.org.icol.icolbackend.exception.RequisicaoNaoEncontrada;
import com.br.org.icol.icolbackend.exception.RequisicaoInvalida;
import com.br.org.icol.icolbackend.model.FrequenciaIcol;
import com.br.org.icol.icolbackend.model.MatriculaIcol;
import com.br.org.icol.icolbackend.repository.FrequenciaIcolRepositorio;
import com.br.org.icol.icolbackend.repository.MatriculaIcolRepositorio;
import com.br.org.icol.icolbackend.service.AcessoUsuario;
import com.br.org.icol.icolbackend.dto.FrequenciaRequestDTO;
import com.br.org.icol.icolbackend.dto.FrequenciaResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FrequenciaIcolService {
    
    private final FrequenciaIcolRepositorio repoFrequencia;
    private final MatriculaIcolRepositorio repoMatricula;
    private final AcessoUsuario acessoUsuario;

    public FrequenciaIcolService(FrequenciaIcolRepositorio repoFrequencia, 
                                MatriculaIcolRepositorio repoMatricula,
                                AcessoUsuario acessoUsuario) {
        this.repoFrequencia = repoFrequencia;
        this.repoMatricula = repoMatricula;
        this.acessoUsuario = acessoUsuario;
    }

    // Método auxiliar para converter Entidade -> DTO
    private FrequenciaResponseDTO toDTO(FrequenciaIcol entidade) {
        FrequenciaResponseDTO dto = new FrequenciaResponseDTO();
        dto.setId(entidade.getId());
        dto.setDataAula(entidade.getDataAula());
        dto.setStatusPresenca(entidade.getStatusPresenca());
        dto.setJustificativa(entidade.getJustificativa());
        if (entidade.getMatriculaId() != null) {
            dto.setMatriculaId(entidade.getMatriculaId().getId());
        }
        return dto;
    }

    // Método auxiliar para buscar a entidade internamente
    public FrequenciaIcol buscarEntidade(Long id) {
        return repoFrequencia.findById(id)
                .orElseThrow(() -> new RequisicaoNaoEncontrada("Frequência não encontrada com ID: " + id));
    }

    public List<FrequenciaResponseDTO> listar() {
        return repoFrequencia.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FrequenciaResponseDTO> listarVisiveis() {
        if (acessoUsuario.ehCoordenadora()) {
            return listar();
        }
        String email = acessoUsuario.emailAtual();
        List<FrequenciaIcol> frequencias;
        if (acessoUsuario.possuiPapel("ALUNO")) {
            frequencias = repoFrequencia.findDoAluno(email);
        } else if (acessoUsuario.possuiPapel("PROFESSOR")) {
            frequencias = repoFrequencia.findDasTurmasDoProfessor(email);
        } else {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Perfil sem acesso a frequências."
            );
        }
        return frequencias.stream().map(this::toDTO).toList();
    }

    public FrequenciaResponseDTO buscar(Long id) {
        return toDTO(buscarEntidade(id));
    }

    public FrequenciaResponseDTO buscarVisivel(Long id) {
        if (acessoUsuario.ehCoordenadora()) {
            return buscar(id);
        }
        String email = acessoUsuario.emailAtual();
        FrequenciaIcol frequencia = buscarEntidade(id);
        if (acessoUsuario.possuiPapel("ALUNO")
                && frequencia.getMatriculaId().getAlunoMatrId().getUsuario()
                .getEmailUsuario().equalsIgnoreCase(email)) {
            return toDTO(frequencia);
        }
        if (acessoUsuario.possuiPapel("PROFESSOR")
                && repoFrequencia.existsDaTurmaDoProfessor(id, email)) {
            return toDTO(frequencia);
        }
        throw new RequisicaoNaoEncontrada("Frequência não encontrada.");
    }

    public FrequenciaResponseDTO registrar(FrequenciaRequestDTO dto) {
        MatriculaIcol matriculaExistente = repoMatricula.findById(dto.getMatriculaId())
                .orElseThrow(() -> new RequisicaoNaoEncontrada("Matrícula não encontrada."));

        // Regra 5: Prevenir Frequência Duplicada
        if(repoFrequencia.existsByMatriculaIdAndDataAula(matriculaExistente, dto.getDataAula())){
            throw new RequisicaoInvalida("Já existe um registro de frequência para este aluno nesta data.");
        }

        FrequenciaIcol frequenciaRegistrar = new FrequenciaIcol();
        frequenciaRegistrar.setMatriculaId(matriculaExistente);
        frequenciaRegistrar.setDataAula(dto.getDataAula());
        frequenciaRegistrar.setStatusPresenca(dto.getStatusPresenca());
        frequenciaRegistrar.setJustificativa(dto.getJustificativa());

        FrequenciaIcol salvo = repoFrequencia.save(frequenciaRegistrar);
        return toDTO(salvo);
    }

    public FrequenciaResponseDTO registrarPorProfessorOuCoordenadora(FrequenciaRequestDTO dto) {
        if (!acessoUsuario.ehCoordenadora()) {
            String email = acessoUsuario.emailAtual();
            if (!acessoUsuario.possuiPapel("PROFESSOR")
                    || !repoFrequencia.matriculaPertenceAoProfessor(dto.getMatriculaId(), email)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Só o professor da turma pode registrar a frequência."
                );
            }
        }
        return registrar(dto);
    }

    public FrequenciaResponseDTO atualizar(Long id, FrequenciaRequestDTO dto) {
        FrequenciaIcol existente = buscarEntidade(id);

        existente.setDataAula(dto.getDataAula());
        existente.setStatusPresenca(dto.getStatusPresenca());
        existente.setJustificativa(dto.getJustificativa());

        FrequenciaIcol salvo = repoFrequencia.save(existente);
        return toDTO(salvo);
    }

    public FrequenciaResponseDTO atualizarPorProfessorOuCoordenadora(
            Long id,
            FrequenciaRequestDTO dto
    ) {
        if (!acessoUsuario.ehCoordenadora()) {
            String email = acessoUsuario.emailAtual();
            if (!acessoUsuario.possuiPapel("PROFESSOR")
                    || !repoFrequencia.existsDaTurmaDoProfessor(id, email)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Só o professor da turma pode alterar a frequência."
                );
            }
        }
        return atualizar(id, dto);
    }
}
