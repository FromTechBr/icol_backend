package com.br.org.icol.icolbackend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.org.icol.icolbackend.exception.RequisicaoNaoEncontrada;
import com.br.org.icol.icolbackend.model.CursosIcol;
import com.br.org.icol.icolbackend.model.DocentesIcol;
import com.br.org.icol.icolbackend.model.TurmasIcol;
import com.br.org.icol.icolbackend.repository.CursosIcolRepositorio;
import com.br.org.icol.icolbackend.repository.DocentesIcolRepositorio;
import com.br.org.icol.icolbackend.repository.TurmasIcolRepositorio;
import com.br.org.icol.icolbackend.dto.TurmaRequestDTO;
import com.br.org.icol.icolbackend.dto.TurmaResponseDTO;
import java.util.stream.Collectors;

@Service
@Transactional
public class TurmasIcolService {
    private final TurmasIcolRepositorio repoTurmas;
    private final DocentesIcolRepositorio repoDocentes;
    private final CursosIcolRepositorio repoCursos;

    public TurmasIcolService(TurmasIcolRepositorio repoTurmas, DocentesIcolRepositorio repoDocentes, CursosIcolRepositorio repoCursos){
        this.repoDocentes=repoDocentes;
        this.repoCursos=repoCursos;
        this.repoTurmas=repoTurmas;
    }

    // Método auxiliar para converter Entidade -> DTO
    private TurmaResponseDTO toDTO(TurmasIcol entidade) {
        TurmaResponseDTO dto = new TurmaResponseDTO();
        dto.setId(entidade.getId());
        dto.setStatusTurma(entidade.getStatusTurma());
        dto.setNumMaxAlunos(entidade.getNumMaxAlunos());
        dto.setCronogramaHorarios(entidade.getCronogramaHorarios());
        dto.setAtivo(entidade.getAtivo());
        
        if (entidade.getCurso() != null) {
            dto.setCursoId(entidade.getCurso().getId());
            dto.setNomeCurso(entidade.getCurso().getNomeCurso());
        }
        
        if (entidade.getDocente() != null) {
            dto.setDocenteId(entidade.getDocente().getId());
            dto.setNomeDocente(entidade.getDocente().getNomeCompleto());
        }
        
        return dto;
    }

    // Método auxiliar para buscar a entidade internamente
    public TurmasIcol buscarEntidade(Long id){
        return repoTurmas.findByIdAndAtivo(id).orElseThrow(()-> new RequisicaoNaoEncontrada("Não foi possivel encontrar turma com ID: "+id));
    }

    public List<TurmaResponseDTO> listar(){
        return repoTurmas.findAllAtivos().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TurmaResponseDTO buscar(Long id){
        return toDTO(buscarEntidade(id));
    }

    public List<TurmaResponseDTO> listarAbertas() {
        return repoTurmas.findAllAtivasAbertas().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TurmaResponseDTO buscarAberta(Long id) {
        return repoTurmas.findByIdAtivaAberta(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RequisicaoNaoEncontrada("Turma aberta não encontrada."));
    }
    
    public TurmaResponseDTO criar(TurmaRequestDTO dto){
        CursosIcol cursoExistente = repoCursos.findById(dto.getCursoId())
                .orElseThrow(()-> new RequisicaoNaoEncontrada("Curso não encontrado."));
        
        DocentesIcol docenteExistente = repoDocentes.findById(dto.getDocenteId())
                .orElseThrow(()-> new RequisicaoNaoEncontrada("Docente não encontrado."));
        
        TurmasIcol turmaCadastrar = new TurmasIcol();
        turmaCadastrar.setStatusTurma(dto.getStatusTurma());
        turmaCadastrar.setNumMaxAlunos(dto.getNumMaxAlunos());
        turmaCadastrar.setCronogramaHorarios(dto.getCronogramaHorarios());
        turmaCadastrar.setCurso(cursoExistente);
        turmaCadastrar.setDocente(docenteExistente);
        turmaCadastrar.setAtivo(true);
        
        TurmasIcol salvo = repoTurmas.save(turmaCadastrar);
        return toDTO(salvo);
    }

    public TurmaResponseDTO atualizar(Long id, TurmaRequestDTO dto){
        TurmasIcol existente = buscarEntidade(id);
        
        existente.setStatusTurma(dto.getStatusTurma());
        existente.setNumMaxAlunos(dto.getNumMaxAlunos());
        existente.setCronogramaHorarios(dto.getCronogramaHorarios());
        
        TurmasIcol salvo = repoTurmas.save(existente);
        return toDTO(salvo);
    }

    public void inativar(Long id){
        TurmasIcol turmaInativar = buscarEntidade(id);
        turmaInativar.setAtivo(false);
        repoTurmas.save(turmaInativar);
    }
}
