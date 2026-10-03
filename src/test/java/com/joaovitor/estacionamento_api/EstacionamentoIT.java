package com.joaovitor.estacionamento_api;

import com.joaovitor.estacionamento_api.web.dto.EstacionamentoCreateDto;
import com.joaovitor.estacionamento_api.web.dto.EstacionamentoResponseDto;
import com.joaovitor.estacionamento_api.web.dto.ServicoResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Sql(scripts = "/sql/estacionamentos/estacionamentos-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/estacionamentos/estacionamentos-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class EstacionamentoIT {

    @Autowired
    RestTestClient testClient;

    @Test
    public void criarCheckin_ComDadosValidos_RetornarCreatedAndLocation() {
        EstacionamentoCreateDto createDto = EstacionamentoCreateDto.builder()
                .placa("WER-1111").marca("FIAT").modelo("PALIO 1.0")
                .cor("AZUL").clienteCpf("09191773016")
                .build();

        testClient.post().uri("/api/v1/estacionamentos/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .body(createDto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody()
                .jsonPath("placa").isEqualTo("WER-1111")
                .jsonPath("marca").isEqualTo("FIAT")
                .jsonPath("modelo").isEqualTo("PALIO 1.0")
                .jsonPath("cor").isEqualTo("AZUL")
                .jsonPath("clienteCpf").isEqualTo("09191773016")
                .jsonPath("recibo").exists()
                .jsonPath("dataEntrada").exists()
                .jsonPath("vagaCodigo").exists();
    }

    @Test
    public void criarCheckin_ComServicos_RetornarCreatedComServicosEBuscaPorReciboTambem() {
        EstacionamentoCreateDto createDto = EstacionamentoCreateDto.builder()
                .placa("WER-1111").marca("FIAT").modelo("PALIO 1.0")
                .cor("AZUL").clienteCpf("09191773016")
                .servicosIds(List.of(10L, 30L))
                .build();

        EstacionamentoResponseDto responseBody = testClient.post().uri("/api/v1/estacionamentos/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .body(createDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EstacionamentoResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody.getServicos())
                .extracting(ServicoResponseDto::getId)
                .containsExactlyInAnyOrder(10L, 30L);

        testClient.get().uri("/api/v1/estacionamentos/check-in/{recibo}", responseBody.getRecibo())
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("servicos.length()").isEqualTo(2);
    }

    @Test
    public void criarCheckin_ComServicoInexistente_RetornarErrorStatus404() {
        EstacionamentoCreateDto createDto = EstacionamentoCreateDto.builder()
                .placa("WER-1111").marca("FIAT").modelo("PALIO 1.0")
                .cor("AZUL").clienteCpf("09191773016")
                .servicosIds(List.of(10L, 999L))
                .build();

        testClient.post().uri("/api/v1/estacionamentos/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .body(createDto)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("status").isEqualTo(404)
                .jsonPath("path").isEqualTo("/api/v1/estacionamentos/check-in");
    }

    @Test
    public void criarCheckin_ComRoleCliente_RetornarErrorStatus403() {
        EstacionamentoCreateDto createDto = EstacionamentoCreateDto.builder()
                .placa("WER-1111").marca("FIAT").modelo("PALIO 1.0")
                .cor("AZUL").clienteCpf("09191773016")
                .build();

        testClient.post().uri("/api/v1/estacionamentos/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com.br", "123456"))
                .body(createDto)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("status").isEqualTo("403")
                .jsonPath("path").isEqualTo("/api/v1/estacionamentos/check-in")
                .jsonPath("method").isEqualTo("POST");
    }

    @Test
    public void criarCheckin_ComDadosInvalidos_RetornarErrorStatus422() {
        EstacionamentoCreateDto createDto = EstacionamentoCreateDto.builder()
                .placa("").marca("").modelo("")
                .cor("").clienteCpf("")
                .build();

        testClient.post().uri("/api/v1/estacionamentos/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com.br", "123456"))
                .body(createDto)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("status").isEqualTo("422")
                .jsonPath("path").isEqualTo("/api/v1/estacionamentos/check-in")
                .jsonPath("method").isEqualTo("POST");
    }

    @Test
    public void criarCheckin_ComCpfInexistente_RetornarErrorStatus404() {
        EstacionamentoCreateDto createDto = EstacionamentoCreateDto.builder()
                .placa("WER-1111").marca("FIAT").modelo("PALIO 1.0")
                .cor("AZUL").clienteCpf("33838667000")
                .build();

        testClient.post().uri("/api/v1/estacionamentos/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .body(createDto)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("status").isEqualTo("404")
                .jsonPath("path").isEqualTo("/api/v1/estacionamentos/check-in")
                .jsonPath("method").isEqualTo("POST");
    }

    @Sql(scripts = "/sql/estacionamentos/estacionamentos-insert-vagas-ocupadas.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/estacionamentos/estacionamento-delete-vagas-ocupadas.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    public void criarCheckin_ComVagasOcupadas_RetornarErrorStatus404() {
        EstacionamentoCreateDto createDto = EstacionamentoCreateDto.builder()
                .placa("WER-1111").marca("FIAT").modelo("PALIO 1.0")
                .cor("AZUL").clienteCpf("09191773016")
                .build();

        testClient.post().uri("/api/v1/estacionamentos/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .body(createDto)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("status").isEqualTo("404")
                .jsonPath("path").isEqualTo("/api/v1/estacionamentos/check-in")
                .jsonPath("method").isEqualTo("POST");
    }

    @Test
    public void buscarCheckin_ComPerfilAdmin_RetornarDadosStatus200() {

        testClient.get()
                .uri("/api/v1/estacionamentos/check-in/{recibo}", "20230313-101300")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("placa").isEqualTo("FIT-1020")
                .jsonPath("marca").isEqualTo("FIAT")
                .jsonPath("modelo").isEqualTo("PALIO")
                .jsonPath("cor").isEqualTo("VERDE")
                .jsonPath("clienteCpf").isEqualTo("98401203015")
                .jsonPath("recibo").isEqualTo("20230313-101300")
                .jsonPath("dataEntrada").isEqualTo("2023-03-13 10:15:00")
                .jsonPath("vagaCodigo").isEqualTo("A-01");
    }

    @Test
    public void buscarCheckin_ComPerfilCliente_RetornarDadosStatus200() {

        testClient.get()
                .uri("/api/v1/estacionamentos/check-in/{recibo}", "20230313-101300")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bob@email.com.br", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("placa").isEqualTo("FIT-1020")
                .jsonPath("marca").isEqualTo("FIAT")
                .jsonPath("modelo").isEqualTo("PALIO")
                .jsonPath("cor").isEqualTo("VERDE")
                .jsonPath("clienteCpf").isEqualTo("98401203015")
                .jsonPath("recibo").isEqualTo("20230313-101300")
                .jsonPath("dataEntrada").isEqualTo("2023-03-13 10:15:00")
                .jsonPath("vagaCodigo").isEqualTo("A-01");
    }

    @Test
    public void buscarCheckin_ComReciboInexistente_RetornarErrorStatus404() {

        testClient.get()
                .uri("/api/v1/estacionamentos/check-in/{recibo}", "20230313-999999")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bob@email.com.br", "123456"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("status").isEqualTo("404")
                .jsonPath("path").isEqualTo("/api/v1/estacionamentos/check-in/20230313-999999")
                .jsonPath("method").isEqualTo("GET");
    }

    @Test
    public void buscarCheckin_ComReciboDeOutroCliente_RetornarErrorStatus403() {

        testClient.get()
                .uri("/api/v1/estacionamentos/check-in/{recibo}", "20230313-101300")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com.br", "123456"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("status").isEqualTo("403")
                .jsonPath("path").isEqualTo("/api/v1/estacionamentos/check-in/20230313-101300")
                .jsonPath("method").isEqualTo("GET")
                .jsonPath("message").isEqualTo("Acesso negado. Este recibo não pertence a você.");
    }

    @Test
    public void buscarCheckin_SemToken_RetornarErrorMessageStatus401() {

        testClient.get()
                .uri("/api/v1/estacionamentos/check-in/{recibo}", "20230313-101300")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("status").isEqualTo("401")
                .jsonPath("path").isEqualTo("/api/v1/estacionamentos/check-in/20230313-101300")
                .jsonPath("method").isEqualTo("GET")
                .jsonPath("message").isEqualTo("Autenticação necessária");
    }

    @Test
    public void criarCheckOut_ComReciboExistente_RetornarSucesso() {

        testClient.put()
                .uri("/api/v1/estacionamentos/check-out/{recibo}", "20230313-101300")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("placa").isEqualTo("FIT-1020")
                .jsonPath("marca").isEqualTo("FIAT")
                .jsonPath("modelo").isEqualTo("PALIO")
                .jsonPath("cor").isEqualTo("VERDE")
                .jsonPath("dataEntrada").isEqualTo("2023-03-13 10:15:00")
                .jsonPath("clienteCpf").isEqualTo("98401203015")
                .jsonPath("vagaCodigo").isEqualTo("A-01")
                .jsonPath("recibo").isEqualTo("20230313-101300")
                .jsonPath("dataSaida").exists()
                .jsonPath("valor").exists()
                .jsonPath("desconto").exists();
    }

    @Test
    @Sql(scripts = {"/sql/estacionamentos/estacionamentos-insert.sql",
            "/sql/estacionamentos/estacionamentos-insert-com-servicos.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/estacionamentos/estacionamentos-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void criarCheckOut_ComServicos_RetornarValorComTempoMaisServicos() {
        testClient.put()
                .uri("/api/v1/estacionamentos/check-out/{recibo}", "20260101-120000")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("valor").isEqualTo(50.00)
                .jsonPath("desconto").isEqualTo(0.00)
                .jsonPath("servicos.length()").isEqualTo(2);
    }

    @Test
    public void criarCheckOut_ComReciboInexistente_RetornarErrorStatus404() {

        testClient.put()
                .uri("/api/v1/estacionamentos/check-out/{recibo}", "20230313-000000")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("status").isEqualTo("404")
                .jsonPath("path").isEqualTo("/api/v1/estacionamentos/check-out/20230313-000000")
                .jsonPath("method").isEqualTo("PUT");
    }

    @Test
    public void criarCheckOut_ComRoleCliente_RetornarErrorStatus403() {

        testClient.put()
                .uri("/api/v1/estacionamentos/check-out/{recibo}", "20230313-101300")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com.br", "123456"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("status").isEqualTo("403")
                .jsonPath("path").isEqualTo("/api/v1/estacionamentos/check-out/20230313-101300")
                .jsonPath("method").isEqualTo("PUT");
    }

    @Test
    public void buscarEstacionamentos_PorClienteCpf_RetornarSucesso() {

        testClient.get()
                .uri("/api/v1/estacionamentos/cpf/{cpf}?size=1&page=0", "98401203015")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_embedded.estacionamentos.length()").isEqualTo(1)
                .jsonPath("page.totalPages").isEqualTo(2)
                .jsonPath("page.number").isEqualTo(0)
                .jsonPath("page.size").isEqualTo(1)
                .jsonPath("_links.next").exists()
                .jsonPath("_embedded.estacionamentos[0]._links['check-out']").exists()
                .jsonPath("_embedded.estacionamentos[0]._links.delete").doesNotExist();

        testClient.get()
                .uri("/api/v1/estacionamentos/cpf/{cpf}?size=1&page=1", "98401203015")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_embedded.estacionamentos.length()").isEqualTo(1)
                .jsonPath("page.totalPages").isEqualTo(2)
                .jsonPath("page.number").isEqualTo(1)
                .jsonPath("page.size").isEqualTo(1)
                .jsonPath("_links.prev").exists();
    }

    @Test
    public void buscarEstacionamento_PorClienteComCpfPerfilCliente_RetornarErroStatus403() {

        testClient.get()
                .uri("/api/v1/estacionamentos/cpf/{cpf}", "98401203015")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com.br", "123456"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("status").isEqualTo("403")
                .jsonPath("path").isEqualTo("/api/v1/estacionamentos/cpf/98401203015")
                .jsonPath("method").isEqualTo("GET");
    }

    @Test
    public void buscarEstacionamentos_DoClienteLogadoSucesso() {

        testClient.get()
                .uri("/api/v1/estacionamentos?size=1&page=0")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bob@email.com.br", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_embedded.estacionamentos.length()").isEqualTo(1)
                .jsonPath("page.totalPages").isEqualTo(2)
                .jsonPath("page.number").isEqualTo(0)
                .jsonPath("page.size").isEqualTo(1)
                .jsonPath("_embedded.estacionamentos[0]._links.self").exists()
                .jsonPath("_embedded.estacionamentos[0]._links['check-out']").doesNotExist()
                .jsonPath("_embedded.estacionamentos[0]._links.vaga").doesNotExist();

        testClient.get()
                .uri("/api/v1/estacionamentos?size=1&page=1")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bob@email.com.br", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_embedded.estacionamentos.length()").isEqualTo(1)
                .jsonPath("page.totalPages").isEqualTo(2)
                .jsonPath("page.number").isEqualTo(1)
                .jsonPath("page.size").isEqualTo(1);
    }

    @Test
    public void criarCheckin_ComDadosValidos_RetornarLinksDeEstacionamentoEmAberto() {
        EstacionamentoCreateDto createDto = EstacionamentoCreateDto.builder()
                .placa("WER-1111").marca("FIAT").modelo("PALIO 1.0")
                .cor("AZUL").clienteCpf("09191773016")
                .build();

        testClient.post().uri("/api/v1/estacionamentos/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .body(createDto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().value(HttpHeaders.LOCATION, location -> assertThat(location).contains("/api/v1/estacionamentos/"))
                .expectBody()
                .jsonPath("_links.self").exists()
                .jsonPath("_links['check-out']").exists()
                .jsonPath("_links.delete").doesNotExist()
                .jsonPath("_links.cliente.href").value(String.class, href -> assertThat(href).endsWith("/api/v1/clientes/cpf/09191773016"))
                .jsonPath("_links.vaga").exists();
    }

    @Test
    public void checkOut_TrocaOsLinks_EBuscaPeloReciboContinuaFuncionandoDepoisDeFinalizado() {
        testClient.get()
                .uri("/api/v1/estacionamentos/{recibo}", "20230313-101300")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_links['check-out']").exists()
                .jsonPath("_links.delete").doesNotExist();

        testClient.put()
                .uri("/api/v1/estacionamentos/check-out/{recibo}", "20230313-101300")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_links.delete").exists()
                .jsonPath("_links['check-out']").doesNotExist();

        testClient.get()
                .uri("/api/v1/estacionamentos/{recibo}", "20230313-101300")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("dataSaida").exists()
                .jsonPath("_links.self.href").value(String.class, href -> assertThat(href).endsWith("/api/v1/estacionamentos/20230313-101300"))
                .jsonPath("_links.delete").exists();
    }

    @Test
    public void buscarPorRecibo_PeloClienteDono_RetornarSemLinksDeAdmin() {
        testClient.get()
                .uri("/api/v1/estacionamentos/{recibo}", "20230314-101400")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com.br", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("_links.self").exists()
                .jsonPath("_links.cliente.href").value(String.class, href -> assertThat(href).endsWith("/api/v1/clientes/detalhes"))
                .jsonPath("_links['check-out']").doesNotExist()
                .jsonPath("_links.delete").doesNotExist()
                .jsonPath("_links.vaga").doesNotExist();
    }

    @Test
    public void buscarPorRecibo_DeOutroCliente_RetornarErrorStatus403() {
        testClient.get()
                .uri("/api/v1/estacionamentos/{recibo}", "20230313-101300")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com.br", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void buscarEstacionamento_DorClienteLogadoPerfilAdmin_RetornarErroStatus403() {

        testClient.get()
                .uri("/api/v1/estacionamentos", "20230313-101300")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("status").isEqualTo("403")
                .jsonPath("path").isEqualTo("/api/v1/estacionamentos")
                .jsonPath("method").isEqualTo("GET");
    }

    @Test
    @Sql(scripts = {"/sql/estacionamentos/estacionamentos-insert.sql",
            "/sql/estacionamentos/estacionamentos-insert-com-servicos.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/estacionamentos/estacionamentos-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void excluirEstacionamento_FinalizadoComServicos_RetornarStatus204() {
        testClient.put()
                .uri("/api/v1/estacionamentos/check-out/{recibo}", "20260101-120000")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isOk();

        testClient.delete()
                .uri("/api/v1/estacionamentos/{recibo}", "20260101-120000")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isNoContent();

        testClient.delete()
                .uri("/api/v1/estacionamentos/{recibo}", "20260101-120000")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void excluirEstacionamento_EmAberto_RetornarErrorStatus409() {
        testClient.delete()
                .uri("/api/v1/estacionamentos/{recibo}", "20230313-101300")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("status").isEqualTo("409")
                .jsonPath("path").isEqualTo("/api/v1/estacionamentos/20230313-101300")
                .jsonPath("method").isEqualTo("DELETE");
    }

    @Test
    public void excluirEstacionamento_ComReciboInexistente_RetornarErrorStatus404() {
        testClient.delete()
                .uri("/api/v1/estacionamentos/{recibo}", "20230313-000000")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "ana@email.com.br", "123456"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void excluirEstacionamento_ComRoleCliente_RetornarErrorStatus403() {
        testClient.delete()
                .uri("/api/v1/estacionamentos/{recibo}", "20230313-101300")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "bia@email.com.br", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }
}
