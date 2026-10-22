package com.br.org.icol.icolbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@Import(PostgresTestConfiguration.class)
class ActuatorIsolationIntegrationTests {

    @LocalServerPort
    private int applicationPort;

    @LocalManagementPort
    private int managementPort;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void expõeHealthEPrometheusSemAutenticacaoNaPortaDeGestao() throws Exception {
        assertThat(managementPort).isNotEqualTo(applicationPort);
        assertStatus(managementPort, "/actuator/health", 200);
        assertStatus(managementPort, "/actuator/prometheus", 200);
    }

    @Test
    void mantemActuatorERecursosDeNegocioForaDaPortaIncorreta() throws Exception {
        assertStatus(applicationPort, "/api/actuator/health", 404);
        assertStatus(applicationPort, "/api/actuator/prometheus", 404);
        assertStatus(managementPort, "/api/alunos", 404);
    }

    @Test
    void disponibilizaOpenApiNoPerfilDeTeste() throws Exception {
        assertStatus(applicationPort, "/api/swagger-ui/index.html", 200);
        assertStatus(applicationPort, "/api/v3/api-docs", 200);
    }

    private void assertStatus(int port, String path, int expectedStatus) throws Exception {
        HttpResponse<String> response = getResponse(port, path);
        assertThat(response.statusCode())
                .withFailMessage(
                        "Expected HTTP %s for %s on port %s but got %s: %s",
                        expectedStatus,
                        path,
                        port,
                        response.statusCode(),
                        response.body()
                )
                .isEqualTo(expectedStatus);
    }

    private HttpResponse<String> getResponse(int port, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }
}
