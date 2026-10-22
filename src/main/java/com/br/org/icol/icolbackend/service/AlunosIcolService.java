package com.br.org.icol.icolbackend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.br.org.icol.icolbackend.model.AlunosIcol;
import com.br.org.icol.icolbackend.model.UsuariosIcol;
import com.br.org.icol.icolbackend.repository.AlunosIcolRepositorio;
import com.br.org.icol.icolbackend.repository.UsuariosIcolRepositorio;
import com.br.org.icol.icolbackend.repository.DocentesIcolRepositorio;
import com.br.org.icol.icolbackend.exception.RequisicaoNaoEncontrada;
import com.br.org.icol.icolbackend.exception.RequisicaoInvalida;
import com.br.org.icol.icolbackend.dto.AlunoRequestDTO;
import com.br.org.icol.icolbackend.dto.AlunoResponseDTO;
import com.br.org.icol.icolbackend.dto.AlunoResumoDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlunosIcolService {

    private final AlunosIcolRepositorio repoAlunos;
    private final UsuariosIcolRepositorio repoUsuarios;
    private final DocentesIcolRepositorio repoDocentes;
    private final AcessoUsuario acessoUsuario;

    // Injeção de dependência dos repositórios via construtor
    public AlunosIcolService(
            AlunosIcolRepositorio repoAlunos,
            UsuariosIcolRepositorio repoUsuarios,
            DocentesIcolRepositorio repoDocentes,
            AcessoUsuario acessoUsuario
    ) {
        this.repoAlunos = repoAlunos;
        this.repoUsuarios = repoUsuarios;
        this.repoDocentes = repoDocentes;
        this.acessoUsuario = acessoUsuario;
    }

    // Método auxiliar para converter Entidade -> DTO
    private AlunoResponseDTO toDTO(AlunosIcol entidade) {
        AlunoResponseDTO dto = new AlunoResponseDTO();
        dto.setId(entidade.getId());
        dto.setNomeCompleto(entidade.getNomeCompleto());
        dto.setIdade(entidade.getIdade());
        dto.setCpf(entidade.getCpf());
        dto.setTelefone(entidade.getTelefone());
        dto.setDeclaSocie(entidade.getDeclaSocie());
        dto.setEndereco(entidade.getEndereco());
        dto.setAtivo(entidade.getAtivo());
        if (entidade.getUsuario() != null) {
            dto.setUsuarioId(entidade.getUsuario().getId());
            dto.setEmailUsuario(entidade.getUsuario().getEmailUsuario());
        }
        return dto;
    }

    // Método auxiliar para buscar a entidade internamente (apenas ativos)
    public AlunosIcol buscarEntidade(Long id) {
        return repoAlunos.findByIdAndAtivo(id)
                .orElseThrow(() -> new RequisicaoNaoEncontrada("Aluno não encontrado com ID: " + id));
    }

    // 1. Listar todos os alunos ativos
    public List<AlunoResponseDTO> listar() {
        return repoAlunos.findAllAtivos().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // 2. Buscar aluno por ID
    public AlunoResponseDTO buscar(Long id) {
        return toDTO(buscarEntidade(id));
    }

    public List<?> listarVisiveis() {
        String email = acessoUsuario.emailAtual();
        if (acessoUsuario.ehCoordenadora()) {
            return listar();
        }
        if (acessoUsuario.possuiPapel("ALUNO")) {
            return repoAlunos.findAtivosDoUsuario(email).stream()
                    .map(this::toDTO)
                    .toList();
        }
        if (acessoUsuario.possuiPapel("PROFESSOR")) {
            return repoAlunos.findAtivosNasTurmasDoProfessor(email).stream()
                    .map(aluno -> new AlunoResumoDTO(aluno.getId(), aluno.getNomeCompleto()))
                    .toList();
        }
        throw new org.springframework.security.access.AccessDeniedException("Perfil sem acesso a alunos.");
    }

    public Object buscarVisivel(Long id) {
        if (acessoUsuario.ehCoordenadora()) {
            return buscar(id);
        }
        String email = acessoUsuario.emailAtual();
        if (acessoUsuario.possuiPapel("ALUNO")) {
            return repoAlunos.findAtivoDoUsuario(id, email)
                    .map(this::toDTO)
                    .orElseThrow(() -> new RequisicaoNaoEncontrada("Aluno não encontrado."));
        }
        if (acessoUsuario.possuiPapel("PROFESSOR")) {
            return repoAlunos.findAtivoNasTurmasDoProfessor(id, email)
                    .map(aluno -> new AlunoResumoDTO(aluno.getId(), aluno.getNomeCompleto()))
                    .orElseThrow(() -> new RequisicaoNaoEncontrada("Aluno não encontrado."));
        }
        throw new org.springframework.security.access.AccessDeniedException("Perfil sem acesso a alunos.");
    }

    public AlunoResponseDTO atualizarProprioOuAdministrativo(Long id, AlunoRequestDTO dto) {
        if (!acessoUsuario.ehCoordenadora()) {
            String email = acessoUsuario.emailAtual();
            if (!acessoUsuario.possuiPapel("ALUNO")
                    || repoAlunos.findAtivoDoUsuario(id, email).isEmpty()) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Você só pode atualizar seu próprio cadastro."
                );
            }
        }
        return atualizar(id, dto);
    }

    // 3. Criar Aluno (Com regras de negócio)
    public AlunoResponseDTO criar(AlunoRequestDTO dto) {
        // RN: CPF Único (apenas entre ativos) - Verifica se já existe
        if (repoAlunos.findByCpfAndAtivo(dto.getCpf()).isPresent()) {
            throw new RequisicaoInvalida("Já existe um aluno cadastrado com este CPF.");
        }

        // Busca o usuário real no banco para garantir que ele existe antes de vincular
        UsuariosIcol usuarioExistente = repoUsuarios.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RequisicaoNaoEncontrada("Usuário de acesso não encontrado."));

        // Regra 1: Vínculo Único e Perfil
        if (usuarioExistente.getTipoUsuario() != com.br.org.icol.icolbackend.enums.TiposUsuario.ALUNO) {
            throw new RequisicaoInvalida("Este usuário não possui permissão de ALUNO.");
        }
        if (repoDocentes.existsByUsuario_Id(dto.getUsuarioId())) {
            throw new RequisicaoInvalida("Este usuário já possui um vínculo de DOCENTE ativo.");
        }

        AlunosIcol alunoCadastrar = new AlunosIcol();
        alunoCadastrar.setNomeCompleto(dto.getNomeCompleto());
        alunoCadastrar.setIdade(dto.getIdade());
        alunoCadastrar.setCpf(dto.getCpf());
        alunoCadastrar.setTelefone(dto.getTelefone());
        alunoCadastrar.setDeclaSocie(dto.getDeclaSocie());
        alunoCadastrar.setEndereco(dto.getEndereco());
        alunoCadastrar.setUsuario(usuarioExistente);
        alunoCadastrar.setAtivo(true);

        AlunosIcol salvo = repoAlunos.save(alunoCadastrar);
        return toDTO(salvo);
    }

    // 4. Atualizar Aluno
    public AlunoResponseDTO atualizar(Long id, AlunoRequestDTO dto) {
        AlunosIcol existente = buscarEntidade(id);

        // RN: Validação de CPF na atualização (Não pode usar CPF de outro aluno ativo)
        Optional<AlunosIcol> alunoComEsseCpf = repoAlunos.findByCpfAndAtivo(dto.getCpf());
        if (alunoComEsseCpf.isPresent() && !alunoComEsseCpf.get().getId().equals(id)) {
            throw new RequisicaoInvalida("Este CPF já está sendo usado por outro aluno.");
        }

        // Atualiza os dados cadastrais
        existente.setNomeCompleto(dto.getNomeCompleto());
        existente.setCpf(dto.getCpf());
        existente.setTelefone(dto.getTelefone());
        existente.setEndereco(dto.getEndereco());
        existente.setIdade(dto.getIdade());
        existente.setDeclaSocie(dto.getDeclaSocie());

        // Nota: Não atualizamos o 'Usuario' (Login) aqui para manter a segurança do vínculo.
        
        AlunosIcol salvo = repoAlunos.save(existente);
        return toDTO(salvo);
    }

    // 5. Inativar Aluno (Soft Delete)
    public void inativar(Long id) {
        AlunosIcol alunoInativar = buscarEntidade(id);
        alunoInativar.setAtivo(false);
        repoAlunos.save(alunoInativar);
    }
}