package com.br.org.icol.icolbackend;

import com.br.org.icol.icolbackend.enums.TiposUsuario;
import com.br.org.icol.icolbackend.model.UsuariosIcol;
import com.br.org.icol.icolbackend.repository.UsuariosIcolRepositorio;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(PostgresTestConfiguration.class)
class SegurancaIntegrationTests {

    @Autowired
    private UsuariosIcolRepositorio usuariosRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.web.server.LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void loginArgon2EmiteJwtEPermiteConsultarProprioPerfil() throws Exception {
        String email = UUID.randomUUID() + "@icol.test";
        String senha = "Senha-forte-123";
        UsuariosIcol usuario = criarUsuario(email, senha, TiposUsuario.ALUNO);
        assertThat(usuario.getSenha()).startsWith("{argon2}$argon2id$");

        HttpResponse<String> loginResponse = post(
                "/api/auth/login",
                "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"
        );

        assertThat(loginResponse.statusCode()).isEqualTo(200);
        JsonNode login = objectMapper.readTree(loginResponse.body());
        assertThat(login.path("tokenType").asText()).isEqualTo("Bearer");
        assertThat(login.path("expiresIn").asLong()).isEqualTo(3600);
        assertThat(login.path("tipoUsuario").asText()).isEqualTo("ALUNO");
        String token = login.path("accessToken").asText();
        assertThat(token.split("\\.")).hasSize(3);

        HttpResponse<String> meResponse = get("/api/auth/me", token);
        assertThat(meResponse.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(meResponse.body()).path("email").asText()).isEqualTo(email);
    }

    @Test
    void exigeTokenEAplicaPermissaoPorPerfil() throws Exception {
        assertThat(get("/api/auth/me", null).statusCode()).isEqualTo(401);
        assertThat(get("/api/usuarios", null).statusCode()).isEqualTo(401);

        String email = UUID.randomUUID() + "@icol.test";
        String senha = "Senha-forte-456";
        criarUsuario(email, senha, TiposUsuario.ALUNO);
        JsonNode login = objectMapper.readTree(post(
                "/api/auth/login",
                "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"
        ).body());

        assertThat(get("/api/usuarios", login.path("accessToken").asText()).statusCode())
                .isEqualTo(403);
        assertThat(get("/api/cursos", null).statusCode()).isEqualTo(200);
    }

    @Test
    void senhaIgualGeraHashesArgon2Diferentes() {
        String primeira = passwordEncoder.encode("mesma-senha");
        String segunda = passwordEncoder.encode("mesma-senha");

        assertThat(primeira).startsWith("{argon2}$argon2id$").isNotEqualTo(segunda);
        assertThat(passwordEncoder.matches("mesma-senha", primeira)).isTrue();
    }

    private UsuariosIcol criarUsuario(String email, String senha, TiposUsuario tipo) {
        UsuariosIcol usuario = new UsuariosIcol();
        usuario.setEmailUsuario(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setAtivo(true);
        usuario.setTipoUsuario(tipo);
        return usuariosRepositorio.save(usuario);
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET();
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
