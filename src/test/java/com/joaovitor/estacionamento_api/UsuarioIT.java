package com.joaovitor.estacionamento_api;

import com.joaovitor.estacionamento_api.web.dto.UsuarioCreateDto;
import com.joaovitor.estacionamento_api.web.dto.UsuarioResponseDto;
import com.joaovitor.estacionamento_api.web.dto.UsuarioSenhaDto;
import com.joaovitor.estacionamento_api.web.exception.ErrorMessage;
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
@Sql(scripts = "/sql/usuarios/usuarios-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/usuarios/usuarios-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)

public class UsuarioIT {
    @Autowired
    RestTestClient testClient;

    @Test
    public void createUsuario_ComUsernameEPasswordValidos_RetornarUsuarioCriadoComStatus201() {
        UsuarioResponseDto responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioCreateDto("tody@email.com", "123456"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UsuarioResponseDto.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getId()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getUsername()).isEqualTo("tody@email.com");
        org.assertj.core.api.Assertions.assertThat(responseBody.getRole()).isEqualTo("CLIENTE");
    }

    @Test
    public void createUsuario_ComUsernameInvalido_RetornarErrorMessageStatus422() {

        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioCreateDto("", "123456"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioCreateDto("tody@", "123456"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioCreateDto("tody@email", "123456"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

    }

    @Test
    public void createUsuario_ComPasswordInvalido_RetornarErrorMessageStatus422() {

        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioCreateDto("tody@email.com", ""))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioCreateDto("tody@email.com", "12345"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioCreateDto("tody@email.com", "1234567"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

    }

    @Test
    public void createUsuario_ComUsernameRepetido_RetornarErroMessageComStatus409() {
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioCreateDto("ana@email.com", "123456"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test
    public void buscarUsuario_ComIdExistente_RetornarUsuarioComStatus200() {
        UsuarioResponseDto responseBody = testClient
                .get()
                .uri("/api/v1/usuarios/100")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UsuarioResponseDto.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getId()).isEqualTo(100);
        org.assertj.core.api.Assertions.assertThat(responseBody.getUsername()).isEqualTo("ana@email.com");
        org.assertj.core.api.Assertions.assertThat(responseBody.getRole()).isEqualTo("ADMIN");
    }

    @Test
    public void buscarUsuario_ComIdInexistente_RetornarErrorMessageComStatus404() {
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/usuarios/0")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void buscarUsuario_ComUsuarioClienteBuscandoOutroCliente_RetornarErrorMessageComStatus403() {
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/usuarios/102")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }

    @Test
    public void editarSenha_ComDadosValidos_RetornarStatus204() {
        testClient
                .patch()
                .uri("/api/v1/usuarios/100")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioSenhaDto(
                        "123456",
                        "123456",
                        "123456"))
                .exchange()
                .expectStatus().isNoContent();
        testClient
                .patch()
                .uri("/api/v1/usuarios/101")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioSenhaDto(
                        "123456",
                        "123456",
                        "123456"))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void editarSenha_ComCamposInvalidos_RetornarErrorMessageComStatus422() {
        ErrorMessage responseBody = testClient
                .patch()
                .uri("/api/v1/usuarios/100")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioSenhaDto("", "", ""))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .patch()
                .uri("/api/v1/usuarios/100")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioSenhaDto("12345", "12345", "12345"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .patch()
                .uri("/api/v1/usuarios/100")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioSenhaDto("12345678", "12345678", "12345678"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void editarSenha_ComSenhaInvalidas_RetornarErrorMessageComStatus400() {
        ErrorMessage responseBody = testClient
                .patch()
                .uri("/api/v1/usuarios/100")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioSenhaDto("123456", "123456", "000000"))
                .exchange()
                .expectStatus().isEqualTo(400)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(400);

        responseBody = testClient
                .patch()
                .uri("/api/v1/usuarios/100")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioSenhaDto("000000", "123456", "123456"))
                .exchange()
                .expectStatus().isEqualTo(400)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(400);
    }

    @Test
    public void listarUsuarios_ComPaginacaoPeloAdmin_RetornarUsuariosComStatus200() {
        testClient
                .get()
                .uri("/api/v1/usuarios?size=2&page=0&sort=id,asc")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_embedded.usuarios.length()").isEqualTo(2)
                .jsonPath("page.totalElements").isEqualTo(3)
                .jsonPath("_embedded.usuarios[0].username").isEqualTo("ana@email.com")
                .jsonPath("_embedded.usuarios[0].role").isEqualTo("ADMIN")
                .jsonPath("_links.next").exists()
                .jsonPath("_embedded.usuarios[0]._links['update-password']").exists()
                .jsonPath("_embedded.usuarios[0]._links.delete").doesNotExist()
                .jsonPath("_embedded.usuarios[1]._links.delete").exists()
                .jsonPath("_embedded.usuarios[1]._links['update-password']").doesNotExist();
    }

    @Test
    public void criarUsuario_ComDadosValidos_RetornarLocationELinkSelf() {
        testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UsuarioCreateDto("tody@email.com", "123456"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody()
                .jsonPath("_links.self.href").value(String.class, href -> assertThat(href).contains("/api/v1/usuarios/"))
                .jsonPath("_links.delete").doesNotExist()
                .jsonPath("_links['update-password']").doesNotExist();
    }

    @Test
    public void buscarUsuario_OProprioCliente_RetornarSomenteLinksDoProprio() {
        testClient
                .get()
                .uri("/api/v1/usuarios/{id}", 101)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_links.self.href").value(String.class, href -> assertThat(href).endsWith("/api/v1/usuarios/101"))
                .jsonPath("_links['update-password'].href").value(String.class, href -> assertThat(href).endsWith("/api/v1/usuarios/101"))
                .jsonPath("_links.delete").doesNotExist()
                .jsonPath("_links.usuarios").doesNotExist();
    }

    @Test
    public void listarUsuarios_PeloCliente_RetornarErrorMessageComStatus403() {
        testClient
                .get()
                .uri("/api/v1/usuarios")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void listarUsuariosPorRole_ComRoleCliente_RetornarSomenteClientesComStatus200() {
        testClient
                .get()
                .uri("/api/v1/usuarios/role/{role}", "cliente")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("page.totalElements").isEqualTo(2)
                .jsonPath("_embedded.usuarios[0].role").isEqualTo("CLIENTE")
                .jsonPath("_embedded.usuarios[1].role").isEqualTo("CLIENTE");
    }

    @Test
    public void listarUsuariosPorRole_ComRoleInexistente_RetornarErrorMessageComStatus400() {
        testClient
                .get()
                .uri("/api/v1/usuarios/role/{role}", "GERENTE")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("status").isEqualTo(400);
    }

    @Test
    public void excluirUsuario_SemCadastroDeCliente_RetornarStatus204() {
        testClient
                .delete()
                .uri("/api/v1/usuarios/{id}", 102)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isNoContent();

        testClient
                .get()
                .uri("/api/v1/usuarios/{id}", 102)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void excluirUsuario_ComCadastroDeCliente_RetornarErrorMessageComStatus409() {
        testClient
                .delete()
                .uri("/api/v1/usuarios/{id}", 101)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("status").isEqualTo(409)
                .jsonPath("method").isEqualTo("DELETE");
    }

    @Test
    public void excluirUsuario_OProprioAdmin_RetornarErrorMessageComStatus409() {
        testClient
                .delete()
                .uri("/api/v1/usuarios/{id}", 100)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    public void excluirUsuario_ComIdInexistente_RetornarErrorMessageComStatus404() {
        testClient
                .delete()
                .uri("/api/v1/usuarios/{id}", 0)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com", "123456"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void excluirUsuario_PeloCliente_RetornarErrorMessageComStatus403() {
        testClient
                .delete()
                .uri("/api/v1/usuarios/{id}", 102)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }
}
