package com.br.org.icol.icolbackend.service;

import com.br.org.icol.icolbackend.exception.RequisicaoNaoEncontrada;
import com.br.org.icol.icolbackend.exception.RequisicaoInvalida;
import com.br.org.icol.icolbackend.model.AlunosIcol;
import com.br.org.icol.icolbackend.model.MatriculaIcol;
import com.br.org.icol.icolbackend.model.TurmasIcol;
import com.br.org.icol.icolbackend.repository.AlunosIcolRepositorio;
import com.br.org.icol.icolbackend.repository.MatriculaIcolRepositorio;
import com.br.org.icol.icolbackend.repository.TurmasIcolRepositorio;
import com.br.org.icol.icolbackend.service.AcessoUsuario;
import com.br.org.icol.icolbackend.enums.StatusMatricula;
import com.br.org.icol.icolbackend.dto.MatriculaRequestDTO;
import com.br.org.icol.icolbackend.dto.MatriculaResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MatriculaIcolService {
    
    private final MatriculaIcolRepositorio repoMatricula;
    private final AlunosIcolRepositorio repoAlunos;
    private final TurmasIcolRepositorio repoTurmas;
    private final AcessoUsuario acessoUsuario;

    public MatriculaIcolService(MatriculaIcolRepositorio repoMatricula, 
                               AlunosIcolRepositorio repoAlunos,
                               TurmasIcolRepositorio repoTurmas,
                               AcessoUsuario acessoUsuario) {
        this.repoMatricula = repoMatricula;
        this.repoAlunos = repoAlunos;
        this.repoTurmas = repoTurmas;
        this.acessoUsuario = acessoUsuario;
    }

    // Método auxiliar para converter Entidade -> DTO
    private MatriculaResponseDTO toDTO(MatriculaIcol entidade) {
        MatriculaResponseDTO dto = new MatriculaResponseDTO();
        dto.setId(entidade.getId());
        dto.setDataMatricula(entidade.getDataMatricula());
        dto.setStatusMatricula(entidade.getStatusMatricula());
        
        if (entidade.getAlunoMatrId() != null) {
            dto.setAlunoId(entidade.getAlunoMatrId().getId());
            dto.setNomeAluno(entidade.getAlunoMatrId().getNomeCompleto());
        }
        
        if (entidade.getTurmaMatrId() != null) {
            dto.setTurmaId(entidade.getTurmaMatrId().getId());
        }
        
        return dto;
    }

    // Método auxiliar para buscar a entidade internamente
    public MatriculaIcol buscarEntidade(Long id){
        return repoMatricula.findById(id).orElseThrow(()-> new RequisicaoNaoEncontrada("Não foi possível encontrar a matrícula com ID: "+id));
    }

    public List<MatriculaResponseDTO> listar(){
        return repoMatricula.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<MatriculaResponseDTO> listarVisiveis() {
        if (acessoUsuario.ehCoordenadora()) {
            return listar();
        }
        String email = acessoUsuario.emailAtual();
        List<MatriculaIcol> matriculas;
        if (acessoUsuario.possuiPapel("ALUNO")) {
            matriculas = repoMatricula.findAtivasDoAluno(email);
        } else if (acessoUsuario.possuiPapel("PROFESSOR")) {
            matriculas = repoMatricula.findAtivasDasTurmasDoProfessor(email);
        } else {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Perfil sem acesso a matrículas."
            );
        }
        return matriculas.stream().map(this::toDTO).toList();
    }

    public MatriculaResponseDTO buscar(Long id){
        return toDTO(buscarEntidade(id));
    }

    public MatriculaResponseDTO buscarVisivel(Long id) {
        if (acessoUsuario.ehCoordenadora()) {
            return buscar(id);
        }
        String email = acessoUsuario.emailAtual();
        if (acessoUsuario.possuiPapel("ALUNO")) {
            return repoMatricula.findAtivaDoAluno(id, email)
                    .map(this::toDTO)
                    .orElseThrow(() -> new RequisicaoNaoEncontrada("Matrícula não encontrada."));
        }
        if (acessoUsuario.possuiPapel("PROFESSOR")) {
            return repoMatricula.findAtivaDaTurmaDoProfessor(id, email)
                    .map(this::toDTO)
                    .orElseThrow(() -> new RequisicaoNaoEncontrada("Matrícula não encontrada."));
        }
        throw new org.springframework.security.access.AccessDeniedException(
                "Perfil sem acesso a matrículas."
        );
    }
    
    public MatriculaResponseDTO criar(MatriculaRequestDTO dto){
        AlunosIcol alunoExistente = repoAlunos.findById(dto.getAlunoId())
                .orElseThrow(()-> new RequisicaoNaoEncontrada("Aluno não encontrado."));

        if (!acessoUsuario.ehCoordenadora()
                && (!acessoUsuario.possuiPapel("ALUNO")
                || !alunoExistente.getUsuario().getEmailUsuario()
                .equalsIgnoreCase(acessoUsuario.emailAtual()))) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Você só pode solicitar matrícula para seu próprio cadastro."
            );
        }
        
        TurmasIcol turmaExistente = repoTurmas.findById(dto.getTurmaId())
                .orElseThrow(()-> new RequisicaoNaoEncontrada("Turma não encontrada."));
        
        // Regra 3: Evitar Matriculas Duplicadas
        if(repoMatricula.existsByAlunoMatrIdAndTurmaMatrId(alunoExistente, turmaExistente)){
            throw new RequisicaoInvalida("Este aluno já possui matrícula nesta turma.");
        }

        // Regra 2: Capacidade da Turma
        long matriculasAtivas = repoMatricula.countByTurmaMatrIdAndStatusMatricula(turmaExistente, com.br.org.icol.icolbackend.enums.StatusMatricula.APROVADA);
        if(matriculasAtivas >= turmaExistente.getNumMaxAlunos()){
            throw new RequisicaoInvalida("A turma selecionada já atingiu a capacidade máxima de alunos.");
        }
        
        MatriculaIcol matriculaCadastrar = new MatriculaIcol();
        matriculaCadastrar.setStatusMatricula(StatusMatricula.PENDENTE);
        matriculaCadastrar.setDataMatricula(LocalDate.now());
        matriculaCadastrar.setAlunoMatrId(alunoExistente);
        matriculaCadastrar.setTurmaMatrId(turmaExistente);
        
        MatriculaIcol salvo = repoMatricula.save(matriculaCadastrar);
        return toDTO(salvo);
    }

    public MatriculaResponseDTO atualizar(Long id, MatriculaRequestDTO dto){
        MatriculaIcol existente = buscarEntidade(id);
        existente.setStatusMatricula(dto.getStatusMatricula());
        
        MatriculaIcol salvo = repoMatricula.save(existente);
        return toDTO(salvo);
    }

    public void deletar(Long id) {
        if (!repoMatricula.existsById(id)) {
            throw new RequisicaoNaoEncontrada("Matrícula não encontrada para exclusão.");
        }
        repoMatricula.deleteById(id);
    }
}
