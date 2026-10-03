package com.joaovitor.estacionamento_api;

import com.joaovitor.estacionamento_api.web.dto.ClienteCreateDto;
import com.joaovitor.estacionamento_api.web.dto.ClienteResponseDto;
import com.joaovitor.estacionamento_api.web.exception.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Sql(scripts = "/sql/clientes/clientes-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clientes/clientes-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)

public class ClienteIT {

    @Autowired
    RestTestClient testClient;

    @Test
    public void criarCliente_ComDadosValidos_RetornarClienteComStatus201() {
        ClienteResponseDto responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "toby@email.com", "123456"))
                .body(new ClienteCreateDto("Tobias Ferreira", "91191064085"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ClienteResponseDto.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getId()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getNome()).isEqualTo("Tobias Ferreira");
        org.assertj.core.api.Assertions.assertThat(responseBody.getCpf()).isEqualTo("91191064085");
    }

    @Test
    public void criarClienteComCpfJaCadastrado_RetornarErrorMessageStatus409() {
        ErrorMessage responseBody = testClient
                .post()
                        .uri("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(JwtAuthentication.getHeaderAuthorization(testClient, "toby@email.com", "123456"))
                        .body(new ClienteCreateDto("Tobias Ferreira", "55352517047"))
                    .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test
    public void criarClienteComDadosInvalidos_RetornarErrorMessageStatus422() {
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "toby@email.com", "123456"))
                .body(new ClienteCreateDto("", ""))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();

        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "toby@email.com", "123456"))
        .body(new ClienteCreateDto("Bobb", "00000000000"))
        .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "toby@email.com", "123456"))
    .body(new ClienteCreateDto("Bobb", "911.916.640-85"))
    .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void criarClienteComUsuarioNaoPermitido_RetornarErrorMessageStatus403() {
        ErrorMessage responseBody = testClient
                .post()
                        .uri("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new ClienteCreateDto("Tobias Ferreira", "91191064085"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }

    @Test
    public void buscarClienteComIdExistentePeloAdmin_RetornarClienteComStatus200() {
        ClienteResponseDto responseBody = testClient
                .get()
                .uri("/api/v1/clientes/10")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClienteResponseDto.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getId()).isEqualTo(10);
    }

    @Test
    public void buscarCliente_ComIdInexistentePeloAdmin_RetornarClienteComStatus404() {
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/clientes/0")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void buscarClienteComIdExistentePeloCliente_RetornarErrorMessageComStatus403() {
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/clientes/0")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }

    @Test
    public void buscarClientesComPaginacaoPeloAdmin_RetornarClientesComStatus200() {
        testClient
                .get()
                .uri("/api/v1/clientes")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_embedded.clientes.length()").isEqualTo(2)
                .jsonPath("page.number").isEqualTo(0)
                .jsonPath("page.totalPages").isEqualTo(1)
                .jsonPath("_embedded.clientes[0]._links.self").exists();

        testClient
                .get()
                .uri("/api/v1/clientes?size=1&page=1")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_embedded.clientes.length()").isEqualTo(1)
                .jsonPath("page.number").isEqualTo(1)
                .jsonPath("page.totalPages").isEqualTo(2)
                .jsonPath("_links.prev").exists()
                .jsonPath("_links.next").doesNotExist();
    }

    @Test
    public void buscarCliente_ComIdExistentePeloAdmin_RetornarLinksDoAdmin() {
        testClient
                .get()
                .uri("/api/v1/clientes/{id}", 20)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_links.self.href").value(String.class, href -> assertThat(href).endsWith("/api/v1/clientes/20"))
                .jsonPath("_links.update").exists()
                .jsonPath("_links.delete").exists()
                .jsonPath("_links.clientes").exists()
                .jsonPath("_links.estacionamentos.href").value(String.class,
                        href -> assertThat(href).contains("/api/v1/estacionamentos/cpf/55352517047"));
    }

    @Test
    public void buscarDetalhes_PeloCliente_RetornarSomenteLinksPermitidosAoCliente() {
        testClient
                .get()
                .uri("/api/v1/clientes/detalhes")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_links.self.href").value(String.class, href -> assertThat(href).endsWith("/api/v1/clientes/detalhes"))
                .jsonPath("_links.estacionamentos.href").value(String.class, href -> assertThat(href).contains("/api/v1/estacionamentos"))
                .jsonPath("_links.update").doesNotExist()
                .jsonPath("_links.delete").doesNotExist()
                .jsonPath("_links.clientes").doesNotExist();
    }

    @Test
    public void buscarClientes_ComPaginacaoPeloCliente_RetornarErrorMessageComStatus403() {
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/clientes")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }

    @Test
    public void buscarCliente_ComDadosDoTokenDeCliente_RetornarClienteComStatus200() {
        ClienteResponseDto responseBody = testClient
                .get()
                .uri("/api/v1/clientes/detalhes")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClienteResponseDto.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getCpf()).isEqualTo("79074426050");
        org.assertj.core.api.Assertions.assertThat(responseBody.getNome()).isEqualTo("Bianca Silva");
        org.assertj.core.api.Assertions.assertThat(responseBody.getId()).isEqualTo(10);
    }

    @Test
    public void buscarCliente_ComDadosDoTokenDeAdministrador_RetornarErrorMessageComStatus403() {
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/clientes/detalhes")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }

    @Test
    public void buscarCliente_ComCpfExistentePeloAdmin_RetornarClienteComStatus200() {
        testClient
                .get()
                .uri("/api/v1/clientes/cpf/{cpf}", "79074426050")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("id").isEqualTo(10)
                .jsonPath("nome").isEqualTo("Bianca Silva");
    }

    @Test
    public void buscarCliente_ComCpfInexistentePeloAdmin_RetornarErrorMessageComStatus404() {
        testClient
                .get()
                .uri("/api/v1/clientes/cpf/{cpf}", "09191773016")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("status").isEqualTo(404);
    }

    @Test
    public void buscarCliente_PorCpfPeloCliente_RetornarErrorMessageComStatus403() {
        testClient
                .get()
                .uri("/api/v1/clientes/cpf/{cpf}", "79074426050")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void atualizarCliente_ComDadosValidosPeloAdmin_RetornarClienteAtualizadoComStatus200() {
        testClient
                .put()
                .uri("/api/v1/clientes/{id}", 10)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new ClienteCreateDto("Bianca Souza Silva", "09191773016"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("id").isEqualTo(10)
                .jsonPath("nome").isEqualTo("Bianca Souza Silva")
                .jsonPath("cpf").isEqualTo("09191773016");
    }

    @Test
    public void atualizarCliente_ComCpfDeOutroCliente_RetornarErrorMessageComStatus409() {
        testClient
                .put()
                .uri("/api/v1/clientes/{id}", 10)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new ClienteCreateDto("Bianca Silva", "55352517047"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("status").isEqualTo(409)
                .jsonPath("method").isEqualTo("PUT");
    }

    @Test
    public void atualizarCliente_ComCpfInvalido_RetornarErrorMessageComStatus422() {
        testClient
                .put()
                .uri("/api/v1/clientes/{id}", 10)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new ClienteCreateDto("Bianca Silva", "00000000000"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("status").isEqualTo(422);
    }

    @Test
    public void atualizarCliente_ComIdInexistente_RetornarErrorMessageComStatus404() {
        testClient
                .put()
                .uri("/api/v1/clientes/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .body(new ClienteCreateDto("Fulano de Tal", "09191773016"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void atualizarCliente_PeloCliente_RetornarErrorMessageComStatus403() {
        testClient
                .put()
                .uri("/api/v1/clientes/{id}", 10)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .body(new ClienteCreateDto("Bianca Silva", "79074426050"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void excluirCliente_SemHistorico_RetornarStatus204() {
        testClient
                .delete()
                .uri("/api/v1/clientes/{id}", 10)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isNoContent();

        testClient
                .get()
                .uri("/api/v1/clientes/{id}", 10)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void excluirCliente_ComHistoricoDeEstacionamento_RetornarErrorMessageComStatus409() {
        testClient
                .delete()
                .uri("/api/v1/clientes/{id}", 20)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("status").isEqualTo(409)
                .jsonPath("method").isEqualTo("DELETE");
    }

    @Test
    public void excluirCliente_PeloCliente_RetornarErrorMessageComStatus403() {
        testClient
                .delete()
                .uri("/api/v1/clientes/{id}", 10)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }
}
