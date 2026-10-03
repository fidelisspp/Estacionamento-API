package com.joaovitor.estacionamento_api;

import com.joaovitor.estacionamento_api.web.dto.VagaCreateDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Sql(scripts = "/sql/vagas/vagas-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/vagas/vagas-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class VagaIT {

    @Autowired
    RestTestClient testClient;

    @Test
    public void criarVaga_ComDadosValidos_RetornarLocationStatus201() {
        testClient
                .post()
                .uri("/api/v1/vagas")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new VagaCreateDto("A-05", "LIVRE"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION);
    }

    @Test
    public void criarVaga_ComCodigoJaExistente_RetornarErrorMessageComStatus409() {
        testClient
                .post()
                .uri("/api/v1/vagas")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
        .body(new VagaCreateDto("A-01", "LIVRE"))
        .exchange()
                .expectStatus().isEqualTo(409)
        .expectBody()
                .jsonPath("status").isEqualTo(409)
        .jsonPath("method").isEqualTo("POST")
        .jsonPath("path").isEqualTo("/api/v1/vagas");
    }

    @Test
    public void criarVaga_ComDadosInvalidos_RetornarErrorMessageComStatus422() {
        testClient
                .post()
                .uri("/api/v1/vagas")
        .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
        .body(new VagaCreateDto("", ""))
        .exchange()
                .expectStatus().isEqualTo( 422)
        .expectBody()
                .jsonPath("status").isEqualTo(422)
        .jsonPath("method").isEqualTo("POST")
        .jsonPath("path").isEqualTo("/api/v1/vagas");

    }

    @Test
    public void buscarVaga_ComCodigoInexistente_RetornarErrorMessageComStatus404() {
        testClient
                .get()
                .uri("/api/v1/vagas/{codigo}", "A-10")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("status").isEqualTo(404)
                .jsonPath("method").isEqualTo("GET")
                .jsonPath("path").isEqualTo("/api/v1/vagas/A-10");
    }

    @Test
    public void listarVagas_ComPaginacao_RetornarVagasComStatus200() {
        testClient
                .get()
                .uri("/api/v1/vagas?size=2&page=0")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_embedded.vagas.length()").isEqualTo(2)
                .jsonPath("page.totalElements").isEqualTo(4)
                .jsonPath("page.totalPages").isEqualTo(2)
                .jsonPath("page.number").isEqualTo(0)
                .jsonPath("_links.self").exists()
                .jsonPath("_links.next").exists()
                .jsonPath("_links.prev").doesNotExist()
                .jsonPath("_embedded.vagas[0]._links.self.href").value(String.class, href -> assertThat(href).endsWith("/api/v1/vagas/A-01"));
    }

    @Test
    public void buscarVaga_ComCodigoExistente_RetornarVagaComLinksHateoasStatus200() {
        testClient
                .get()
                .uri("/api/v1/vagas/{codigo}", "A-01")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("codigo").isEqualTo("A-01")
                .jsonPath("status").isEqualTo("LIVRE")
                .jsonPath("_links.self.href").value(String.class, href -> assertThat(href).endsWith("/api/v1/vagas/A-01"))
                .jsonPath("_links.update.href").value(String.class, href -> assertThat(href).endsWith("/api/v1/vagas/A-01"))
                .jsonPath("_links.delete.href").value(String.class, href -> assertThat(href).endsWith("/api/v1/vagas/A-01"))
                .jsonPath("_links.vagas.href").value(String.class, href -> assertThat(href).contains("/api/v1/vagas"));
    }

    @Test
    public void listarVagas_ComUsuarioSemPermissao_RetornarErrorMessageComStatus403() {
        testClient
                .get()
                .uri("/api/v1/vagas")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("status").isEqualTo(403);
    }

    @Test
    public void listarVagasPorStatus_ComStatusOcupada_RetornarSomenteOcupadasComStatus200() {
        testClient
                .get()
                .uri("/api/v1/vagas/status/{status}", "OCUPADA")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("page.totalElements").isEqualTo(1)
                .jsonPath("_embedded.vagas[0].codigo").isEqualTo("A-03")
                .jsonPath("_embedded.vagas[0].status").isEqualTo("OCUPADA");
    }

    @Test
    public void listarVagasPorStatus_ComStatusInexistente_RetornarErrorMessageComStatus400() {
        testClient
                .get()
                .uri("/api/v1/vagas/status/{status}", "QUEBRADA")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("status").isEqualTo(400)
                .jsonPath("path").isEqualTo("/api/v1/vagas/status/QUEBRADA");
    }

    @Test
    public void atualizarVaga_ComDadosValidos_RetornarVagaAtualizadaComStatus200() {
        testClient
                .put()
                .uri("/api/v1/vagas/{codigo}", "A-01")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new VagaCreateDto("B-01", "OCUPADA"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("id").isEqualTo(10)
                .jsonPath("codigo").isEqualTo("B-01")
                .jsonPath("status").isEqualTo("OCUPADA");
    }

    @Test
    public void atualizarVaga_ComCodigoJaExistente_RetornarErrorMessageComStatus409() {
        testClient
                .put()
                .uri("/api/v1/vagas/{codigo}", "A-01")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new VagaCreateDto("A-02", "LIVRE"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("status").isEqualTo(409)
                .jsonPath("method").isEqualTo("PUT");
    }

    @Test
    public void atualizarVaga_ComVeiculoEstacionado_RetornarErrorMessageComStatus409() {
        testClient
                .put()
                .uri("/api/v1/vagas/{codigo}", "A-03")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new VagaCreateDto("A-03", "LIVRE"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("status").isEqualTo(409);
    }

    @Test
    public void atualizarVaga_ComDadosInvalidos_RetornarErrorMessageComStatus422() {
        testClient
                .put()
                .uri("/api/v1/vagas/{codigo}", "A-01")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new VagaCreateDto("A-0001", "VAZIA"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("status").isEqualTo(422);
    }

    @Test
    public void atualizarVaga_ComCodigoInexistente_RetornarErrorMessageComStatus404() {
        testClient
                .put()
                .uri("/api/v1/vagas/{codigo}", "Z-99")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new VagaCreateDto("Z-98", "LIVRE"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void excluirVaga_NuncaUsada_RetornarStatus204() {
        testClient
                .delete()
                .uri("/api/v1/vagas/{codigo}", "A-02")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isNoContent();

        testClient
                .get()
                .uri("/api/v1/vagas/{codigo}", "A-02")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void excluirVaga_ComHistoricoDeEstacionamento_RetornarErrorMessageComStatus409() {
        testClient
                .delete()
                .uri("/api/v1/vagas/{codigo}", "A-04")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("status").isEqualTo(409)
                .jsonPath("method").isEqualTo("DELETE");
    }

    @Test
    public void excluirVaga_ComUsuarioSemPermissao_RetornarErrorMessageComStatus403() {
        testClient
                .delete()
                .uri("/api/v1/vagas/{codigo}", "A-01")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }
}
