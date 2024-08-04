package br.com.fiap.postech.hackapay.cartao.controller;


import br.com.fiap.postech.hackapay.cartao.entities.Cartao;
import br.com.fiap.postech.hackapay.cartao.helper.CartaoHelper;
import br.com.fiap.postech.hackapay.cartao.helper.ClienteHelper;
import br.com.fiap.postech.hackapay.cartao.helper.UserHelper;
import br.com.fiap.postech.hackapay.cartao.integration.ClienteIntegracao;
import br.com.fiap.postech.hackapay.security.UserDetailsServiceImpl;
import io.restassured.RestAssured;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@ActiveProfiles("test")
public class CartaoControllerIT {

    public static final String CARTAO = "/hackapay/cartao";
    @LocalServerPort
    private int port;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private ClienteIntegracao clienteIntegracao;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Nested
    class CadastrarCartao {
        @Test
        void devePermitirCadastrarCartao() {
            var cartao = CartaoHelper.getCartao(false);
            var userDetails = UserHelper.getUserDetails("umUsuarioQualquer");
            var cliente = ClienteHelper.getCliente();
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            when(clienteIntegracao.getCliente(anyString(), anyString())).thenReturn(cliente);
            given()
                .contentType(MediaType.APPLICATION_JSON_VALUE).body(cartao)
                    .header(HttpHeaders.AUTHORIZATION, UserHelper.getToken(userDetails.getUsername()))
            .when()
                .post(CARTAO)
            .then()
                .statusCode(HttpStatus.OK.value())
                .body(matchesJsonSchemaInClasspath("schemas/cartao.schema.json"));
        }

        @Test
        void deveGerarExcecao_QuandoCadastrarCartao_RequisicaoXml() {
            /*
              Na aula o professor instanciou uma string e enviou no .body()
              Mas como o teste valida o contentType o body pode ser enviado com qualquer conteudo
              ou nem mesmo ser enviado como ficou no teste abaixo.
             */
            var userDetails = UserHelper.getUserDetails("umUsuarioQualquer");
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            given()
                .contentType(MediaType.APPLICATION_XML_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, UserHelper.getToken(userDetails.getUsername()))
            .when()
                .post(CARTAO)
            .then()
                .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .body(matchesJsonSchemaInClasspath("schemas/error.schema.json"));
        }
    }

    @Nested
    class BuscarCartao {
        @Test
        void devePermitirBuscarCartaoPorId() {
            var id = "56833f9a-7fda-49d5-a760-8e1ba41f35a8";
            var userDetails = UserHelper.getUserDetails("umUsuarioQualquer");
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, UserHelper.getToken(userDetails.getUsername()))
            .when()
                .get(CARTAO + "/{id}", id)
            .then()
                .statusCode(HttpStatus.OK.value())
                .body(matchesJsonSchemaInClasspath("schemas/cartao.schema.json"));
            // TODO VERIFICAR A OBRIGATORIEDADE DO ID
        }
        @Test
        void deveGerarExcecao_QuandoBuscarCartaoPorId_idNaoExiste() {
            var id = CartaoHelper.getCartao(true).getId();
            var userDetails = UserHelper.getUserDetails("umUsuarioQualquer");
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, UserHelper.getToken(userDetails.getUsername()))
            .when()
                .get(CARTAO + "/{id}", id)
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void devePermitirBuscarTodosCartao() {
            var userDetails = UserHelper.getUserDetails("umUsuarioQualquer");
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, UserHelper.getToken(userDetails.getUsername()))
            .when()
                .get(CARTAO)
            .then()
                .statusCode(HttpStatus.OK.value())
                .body(matchesJsonSchemaInClasspath("schemas/cartao.page.schema.json"));
        }

        @Test
        void devePermitirBuscarTodosCartao_ComPaginacao() {
            var userDetails = UserHelper.getUserDetails("umUsuarioQualquer");
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            given()
                .queryParam("page", "1")
                .queryParam("size", "1")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, UserHelper.getToken(userDetails.getUsername()))
            .when()
                .get(CARTAO)
            .then()
                .statusCode(HttpStatus.OK.value())
                .body(matchesJsonSchemaInClasspath("schemas/cartao.page.schema.json"));
        }
    }

    @Nested
    class AlterarCartao {
        @Test
        void devePermitirAlterarCartao() {
            var userDetails = UserHelper.getUserDetails("umUsuarioQualquer");
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            var cartao = new Cartao(
                    "52816804046",
                    1002D,
                    RandomStringUtils.random(20, true, true),
                    RandomStringUtils.random(20, true, true),
                    RandomStringUtils.random(20, true, true)
            );
            cartao.setId(UUID.fromString("ab8fdcd5-c9b5-471e-8ad0-380a65d6cc86"));
            given()
                .body(cartao).contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, UserHelper.getToken(userDetails.getUsername()))
            .when()
                .put(CARTAO + "/{id}", cartao.getId())
            .then()
                .statusCode(HttpStatus.ACCEPTED.value())
                .body(matchesJsonSchemaInClasspath("schemas/cartao.schema.json"));
        }

        @Test
        void deveGerarExcecao_QuandoAlterarCartao_RequisicaoXml() {
            var cartao = CartaoHelper.getCartao(true);
            var userDetails = UserHelper.getUserDetails("umUsuarioQualquer");
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            given()
                .body(cartao).contentType(MediaType.APPLICATION_XML_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, UserHelper.getToken(userDetails.getUsername()))
            .when().log().all()
                .put(CARTAO + "/{id}", cartao.getId())
            .then().log().all()
                .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        }

        @Test
        void deveGerarExcecao_QuandoAlterarCartaoPorId_idNaoExiste() {
            var userDetails = UserHelper.getUserDetails("umUsuarioQualquer");
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            var cartao = CartaoHelper.getCartao(true);
            given()
                .body(cartao).contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, UserHelper.getToken(userDetails.getUsername()))
            .when()
                .put(CARTAO + "/{id}", cartao.getId())
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(equalTo("Cartao não encontrado com o ID: " + cartao.getId()));
        }
    }

    @Nested
    class RemoverCartao {
        @Test
        void devePermitirRemoverCartao() {
            var userDetails = UserHelper.getUserDetails("umUsuarioQualquer");
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            var cartao = new Cartao(
                    "ccc@ddd.com",
                    1003D,
                    RandomStringUtils.random(20, true, true),
                    RandomStringUtils.random(20, true, true),
                    RandomStringUtils.random(20, true, true)
            );
            cartao.setId(UUID.fromString("8855e7b2-77b6-448b-97f8-8a0b529f3976"));
            given()
                    .header(HttpHeaders.AUTHORIZATION, UserHelper.getToken(userDetails.getUsername()))
            .when()
                .delete(CARTAO + "/{id}", cartao.getId())
            .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void deveGerarExcecao_QuandoRemoverCartaoPorId_idNaoExiste() {
            var userDetails = UserHelper.getUserDetails("umUsuarioQualquer");
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            var cartao = CartaoHelper.getCartao(true);
            given()
                    .header(HttpHeaders.AUTHORIZATION, UserHelper.getToken(userDetails.getUsername()))
            .when()
                .delete(CARTAO + "/{id}", cartao.getId())
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(equalTo("Cartao não encontrado com o ID: " + cartao.getId()));
        }
    }
}
