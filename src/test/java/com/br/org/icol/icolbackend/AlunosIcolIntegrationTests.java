package com.br.org.icol.icolbackend;

import com.br.org.icol.icolbackend.dto.AlunoRequestDTO;
import com.br.org.icol.icolbackend.dto.AlunoResponseDTO;
import com.br.org.icol.icolbackend.enums.TiposUsuario;
import com.br.org.icol.icolbackend.model.UsuariosIcol;
import com.br.org.icol.icolbackend.repository.AlunosIcolRepositorio;
import com.br.org.icol.icolbackend.repository.UsuariosIcolRepositorio;
import com.br.org.icol.icolbackend.service.AlunosIcolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgresTestConfiguration.class)
@Transactional
class AlunosIcolIntegrationTests {

    @Autowired
    private AlunosIcolService alunosService;

    @Autowired
    private UsuariosIcolRepositorio usuariosRepo;

    @Autowired
    private AlunosIcolRepositorio alunosRepo;

    private UsuariosIcol usuarioAluno;

    @BeforeEach
    void setUp() {
        UsuariosIcol usuario = new UsuariosIcol();
        usuario.setEmailUsuario("aluno.teste." + System.nanoTime() + "@icol.org.br");
        usuario.setSenha("senhaForte123");
        usuario.setTipoUsuario(TiposUsuario.ALUNO);
        usuario.setAtivo(true);
        usuarioAluno = usuariosRepo.save(usuario);
    }

    @Test
    void deveSalvarAlunoComUsuarioExistente() {
        AlunoRequestDTO dto = new AlunoRequestDTO();
        dto.setUsuarioId(usuarioAluno.getId());
        dto.setNomeCompleto("Teste Integração");
        dto.setIdade(22);
        dto.setCpf("999.888.777-66");
        dto.setTelefone("11999998877");
        dto.setDeclaSocie("Renda familiar até 2 salários mínimos");
        dto.setEndereco("Rua de Teste, 10");

        AlunoResponseDTO salvo = alunosService.criar(dto);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getCpf()).isEqualTo("999.888.777-66");
        assertThat(alunosRepo.existsById(salvo.getId())).isTrue();
    }

    @Test
    void deveBuscarAlunoPorIdAposSalvar() {
        AlunoRequestDTO dto = new AlunoRequestDTO();
        dto.setUsuarioId(usuarioAluno.getId());
        dto.setNomeCompleto("Consulta Aluno");
        dto.setIdade(24);
        dto.setCpf("888.777.666-55");
        dto.setTelefone("11888887766");
        dto.setDeclaSocie("Renda familiar até 2 salários mínimos");
        dto.setEndereco("Av Consulta, 100");

        AlunoResponseDTO salvo = alunosService.criar(dto);
        AlunoResponseDTO encontrado = alunosService.buscar(salvo.getId());

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getNomeCompleto()).isEqualTo("Consulta Aluno");
        assertThat(encontrado.getUsuarioId()).isEqualTo(usuarioAluno.getId());
    }

    @Test
    void deveDeletarAlunoAposCriar() {
        AlunoRequestDTO dto = new AlunoRequestDTO();
        dto.setUsuarioId(usuarioAluno.getId());
        dto.setNomeCompleto("Aluno Deletar");
        dto.setIdade(21);
        dto.setCpf("777.666.555-44");
        dto.setTelefone("11777776655");
        dto.setDeclaSocie("Renda familiar até 2 salários mínimos");
        dto.setEndereco("Rua Delete, 50");

        AlunoResponseDTO salvo = alunosService.criar(dto);
        alunosService.inativar(salvo.getId());

        // Soft delete: aluno ainda existe no BD mas inativo
        assertThat(alunosRepo.existsById(salvo.getId())).isTrue();
        // Verificar que ele não aparece mais na listagem de ativos
        assertThat(alunosService.listar()).noneMatch(a -> a.getId().equals(salvo.getId()));
    }
}
