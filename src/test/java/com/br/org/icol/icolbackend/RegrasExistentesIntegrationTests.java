package com.br.org.icol.icolbackend;

import com.br.org.icol.icolbackend.dto.AlunoRequestDTO;
import com.br.org.icol.icolbackend.dto.FrequenciaRequestDTO;
import com.br.org.icol.icolbackend.dto.MatriculaRequestDTO;
import com.br.org.icol.icolbackend.config.CoordenadoraInicialSeeder;
import com.br.org.icol.icolbackend.enums.StatusMatricula;
import com.br.org.icol.icolbackend.enums.StatusPresenca;
import com.br.org.icol.icolbackend.enums.StatusTurma;
import com.br.org.icol.icolbackend.enums.TiposCursos;
import com.br.org.icol.icolbackend.enums.TiposUsuario;
import com.br.org.icol.icolbackend.exception.RequisicaoInvalida;
import com.br.org.icol.icolbackend.model.AlunosIcol;
import com.br.org.icol.icolbackend.model.CursosIcol;
import com.br.org.icol.icolbackend.model.DocentesIcol;
import com.br.org.icol.icolbackend.model.TurmasIcol;
import com.br.org.icol.icolbackend.model.UsuariosIcol;
import com.br.org.icol.icolbackend.repository.AlunosIcolRepositorio;
import com.br.org.icol.icolbackend.repository.CursosIcolRepositorio;
import com.br.org.icol.icolbackend.repository.DocentesIcolRepositorio;
import com.br.org.icol.icolbackend.repository.TurmasIcolRepositorio;
import com.br.org.icol.icolbackend.repository.UsuariosIcolRepositorio;
import com.br.org.icol.icolbackend.service.AlunosIcolService;
import com.br.org.icol.icolbackend.service.FrequenciaIcolService;
import com.br.org.icol.icolbackend.service.MatriculaIcolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgresTestConfiguration.class)
@Transactional
class RegrasExistentesIntegrationTests {

    @Autowired
    private UsuariosIcolRepositorio usuariosRepositorio;

    @Autowired
    private AlunosIcolRepositorio alunosRepositorio;

    @Autowired
    private CursosIcolRepositorio cursosRepositorio;

    @Autowired
    private DocentesIcolRepositorio docentesRepositorio;

    @Autowired
    private TurmasIcolRepositorio turmasRepositorio;

    @Autowired
    private AlunosIcolService alunosService;

    @Autowired
    private MatriculaIcolService matriculaService;

    @Autowired
    private FrequenciaIcolService frequenciaService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AlunosIcol aluno;
    private TurmasIcol turma;
    private UsuariosIcol coordenadora;

    @BeforeEach
    void setUp() {
        coordenadora = criarUsuario(TiposUsuario.COORDENADORA);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        coordenadora.getEmailUsuario(),
                        "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_COORDENADORA"))
                )
        );
        aluno = criarAluno("111.222.333-44");
        turma = criarTurma(1);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejeitaCpfJaCadastrado() {
        AlunoRequestDTO dto = new AlunoRequestDTO();
        dto.setUsuarioId(criarUsuario(TiposUsuario.ALUNO).getId());
        dto.setNomeCompleto("Aluno repetido");
        dto.setIdade(20);
        dto.setCpf(aluno.getCpf());
        dto.setTelefone("11999990000");
        dto.setDeclaSocie("Informação socioeconômica");
        dto.setEndereco("Rua de teste, 1");

        assertThatThrownBy(() -> alunosService.criar(dto))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("CPF");
    }

    @Test
    void rejeitaMatriculaDuplicadaNaMesmaTurma() {
        matricular(aluno, StatusMatricula.PENDENTE);

        assertThatThrownBy(() -> matricular(aluno, StatusMatricula.PENDENTE))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("matrícula");
    }

    @Test
    void rejeitaMatriculaQuandoTurmaAtingiuCapacidade() {
        matricular(aluno, StatusMatricula.APROVADA);
        AlunosIcol segundoAluno = criarAluno("222.333.444-55");

        assertThatThrownBy(() -> matricular(segundoAluno, StatusMatricula.APROVADA))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("capacidade");
    }

    @Test
    void rejeitaFrequenciaDuplicadaNaMesmaData() {
        Long matriculaId = matricular(aluno, StatusMatricula.APROVADA);
        FrequenciaRequestDTO dto = new FrequenciaRequestDTO();
        dto.setMatriculaId(matriculaId);
        dto.setDataAula(LocalDate.of(2026, 10, 20));
        dto.setStatusPresenca(StatusPresenca.PRESENTE);
        frequenciaService.registrar(dto);

        assertThatThrownBy(() -> frequenciaService.registrar(dto))
                .isInstanceOf(RequisicaoInvalida.class)
                .hasMessageContaining("frequência");
    }

    @Test
    void criaCoordenadoraInicialComCredenciaisConfiguradas() {
        String email = UUID.randomUUID() + "@icol.test";
        String senha = "senha-segura-de-teste";
        usuariosRepositorio.delete(coordenadora);
        CoordenadoraInicialSeeder seeder = new CoordenadoraInicialSeeder(
                usuariosRepositorio,
                passwordEncoder,
                email,
                senha
        );

        seeder.run(new DefaultApplicationArguments(new String[0]));

        UsuariosIcol coordenadora = usuariosRepositorio.findByEmailUsuario(email).orElseThrow();
        assertThat(coordenadora.getTipoUsuario()).isEqualTo(TiposUsuario.COORDENADORA);
        assertThat(passwordEncoder.matches(senha, coordenadora.getSenha())).isTrue();
    }

    private AlunosIcol criarAluno(String cpf) {
        AlunosIcol novoAluno = new AlunosIcol();
        novoAluno.setUsuario(criarUsuario(TiposUsuario.ALUNO));
        novoAluno.setNomeCompleto("Aluno de teste");
        novoAluno.setIdade(20);
        novoAluno.setCpf(cpf);
        novoAluno.setTelefone("11999990000");
        novoAluno.setDeclaSocie("Informação socioeconômica");
        novoAluno.setEndereco("Rua de teste, 1");
        novoAluno.setAtivo(true);
        return alunosRepositorio.save(novoAluno);
    }

    private UsuariosIcol criarUsuario(TiposUsuario tipo) {
        UsuariosIcol usuario = new UsuariosIcol();
        usuario.setEmailUsuario(UUID.randomUUID() + "@icol.test");
        usuario.setSenha("senha-de-teste");
        usuario.setAtivo(true);
        usuario.setTipoUsuario(tipo);
        return usuariosRepositorio.save(usuario);
    }

    private TurmasIcol criarTurma(int capacidade) {
        CursosIcol curso = new CursosIcol();
        curso.setNomeCurso("Curso de teste");
        curso.setDescricao("Descrição");
        curso.setDuracao("12 semanas");
        curso.setAtivo(true);
        curso.setCategoria(TiposCursos.ALEPH);
        curso = cursosRepositorio.save(curso);

        DocentesIcol docente = new DocentesIcol();
        docente.setUsuario(criarUsuario(TiposUsuario.PROFESSOR));
        docente.setNomeCompleto("Docente de teste");
        docente.setEspecializacao("Educação");
        docente.setAtivo(true);
        docente = docentesRepositorio.save(docente);

        TurmasIcol novaTurma = new TurmasIcol();
        novaTurma.setStatusTurma(StatusTurma.ABERTA);
        novaTurma.setCurso(curso);
        novaTurma.setDocente(docente);
        novaTurma.setNumMaxAlunos(capacidade);
        novaTurma.setCronogramaHorarios("Segunda-feira");
        novaTurma.setAtivo(true);
        return turmasRepositorio.save(novaTurma);
    }

    private Long matricular(AlunosIcol aluno, StatusMatricula status) {
        MatriculaRequestDTO dto = new MatriculaRequestDTO();
        dto.setAlunoId(aluno.getId());
        dto.setTurmaId(turma.getId());
        dto.setStatusMatricula(StatusMatricula.PENDENTE);
        Long matriculaId = matriculaService.criar(dto).getId();
        if (status != StatusMatricula.PENDENTE) {
            dto.setStatusMatricula(status);
            matriculaService.atualizar(matriculaId, dto);
        }
        return matriculaId;
    }
}
