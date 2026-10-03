package com.joaovitor.estacionamento_api;

import com.joaovitor.estacionamento_api.web.dto.ServicoCreateDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Sql(scripts = "/sql/servicos/servicos-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/servicos/servicos-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ServicoIT {

    @Autowired
    RestTestClient testClient;

    @Test
    public void criarServico_ComDadosValidos_RetornarServicoComLocationStatus201() {
        testClient
                .post()
                .uri("/api/v1/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new ServicoCreateDto("Calibragem", "Calibra os 4 pneus", new BigDecimal("10.00"), "CALIBRAGEM"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody()
                .jsonPath("id").isNotEmpty()
                .jsonPath("nome").isEqualTo("Calibragem")
                .jsonPath("tipo").isEqualTo("CALIBRAGEM");
    }

    @Test
    public void criarServico_ComNomeJaExistente_RetornarErrorMessageComStatus409() {
        testClient
                .post()
                .uri("/api/v1/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new ServicoCreateDto("Manobrista", null, new BigDecimal("20.00"), "MANOBRISTA"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("status").isEqualTo(409);
    }

    @Test
    public void criarServico_ComDadosInvalidos_RetornarErrorMessageComStatus422() {
        testClient
                .post()
                .uri("/api/v1/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new ServicoCreateDto("", null, new BigDecimal("-5"), "PINTURA"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("status").isEqualTo(422);
    }

    @Test
    public void criarServico_ComUsuarioCliente_RetornarErrorMessageComStatus403() {
        testClient
                .post()
                .uri("/api/v1/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .body(new ServicoCreateDto("Calibragem", null, new BigDecimal("10.00"), "CALIBRAGEM"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void buscarServico_ComIdExistente_RetornarServicoComStatus200() {
        testClient
                .get()
                .uri("/api/v1/servicos/10")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("id").isEqualTo(10)
                .jsonPath("nome").isEqualTo("Lavagem simples");
    }

    @Test
    public void buscarServico_ComIdInexistente_RetornarErrorMessageComStatus404() {
        testClient
                .get()
                .uri("/api/v1/servicos/0")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("status").isEqualTo(404);
    }

    @Test
    public void listarServicos_ComPaginacao_RetornarServicosComStatus200() {
        testClient
                .get()
                .uri("/api/v1/servicos?size=2&page=0")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_embedded.servicos.length()").isEqualTo(2)
                .jsonPath("page.totalElements").isEqualTo(3)
                .jsonPath("page.totalPages").isEqualTo(2)
                .jsonPath("_links.next").exists()
                .jsonPath("_embedded.servicos[0]._links.self").exists();
    }

    @Test
    public void buscarServico_ComIdExistente_RetornarServicoComLinksHateoasStatus200() {
        testClient
                .get()
                .uri("/api/v1/servicos/{id}", 10)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("id").isEqualTo(10)
                .jsonPath("_links.self.href").value(String.class, href -> assertThat(href).endsWith("/api/v1/servicos/10"))
                .jsonPath("_links.update.href").value(String.class, href -> assertThat(href).endsWith("/api/v1/servicos/10"))
                .jsonPath("_links.delete.href").value(String.class, href -> assertThat(href).endsWith("/api/v1/servicos/10"))
                .jsonPath("_links.servicos.href").value(String.class, href -> assertThat(href).contains("/api/v1/servicos"));
    }

    @Test
    public void listarServicosPorTipo_ComTipoLavagem_RetornarSoLavagensComStatus200() {
        testClient
                .get()
                .uri("/api/v1/servicos/tipo/LAVAGEM")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("page.totalElements").isEqualTo(2)
                .jsonPath("_embedded.servicos[0].tipo").isEqualTo("LAVAGEM");
    }

    @Test
    public void atualizarServico_ComDadosValidos_RetornarServicoAtualizadoComStatus200() {
        testClient
                .put()
                .uri("/api/v1/servicos/10")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new ServicoCreateDto("Lavagem express", "Lavagem rápida", new BigDecimal("25.00"), "LAVAGEM"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("nome").isEqualTo("Lavagem express")
                .jsonPath("preco").isEqualTo(25.00);
    }

    @Test
    public void atualizarServico_ComNomeDeOutroServico_RetornarErrorMessageComStatus409() {
        testClient
                .put()
                .uri("/api/v1/servicos/10")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new ServicoCreateDto("Manobrista", null, new BigDecimal("25.00"), "LAVAGEM"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    public void excluirServico_SemUso_RetornarStatus204() {
        testClient
                .delete()
                .uri("/api/v1/servicos/20")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void excluirServico_UsadoEmEstacionamento_RetornarErrorMessageComStatus409() {
        testClient
                .delete()
                .uri("/api/v1/servicos/30")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("status").isEqualTo(409);
    }
}
