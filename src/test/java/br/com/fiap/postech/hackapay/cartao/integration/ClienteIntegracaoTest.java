package br.com.fiap.postech.hackapay.cartao.integration;

import br.com.fiap.postech.hackapay.cartao.helper.ClienteHelper;
import br.com.fiap.postech.hackapay.cartao.helper.UserHelper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockRestServiceServer
@ExtendWith(MockitoExtension.class)
class ClienteIntegracaoTest {

    @SpyBean
    RestClient.Builder builder;

    @Autowired
    @InjectMocks
    private ClienteIntegracao clienteIntegracao;

    private MockRestServiceServer mockServer;

    @Test
    void atualizaLimiteCartao() throws URISyntaxException {
        var cpf = "cpf";
        var token = "token";
        var uri = "http://localhost:8081/api/cliente/findByCpf/" + cpf;
        var userDetails = UserHelper.getUserDetails("umUsuarioQualquer");

        mockServer = MockRestServiceServer.bindTo(builder).build();
        mockServer.expect(requestTo(uri)).andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", "application/json"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, UserHelper.getToken(userDetails.getUsername())))
                .andRespond(withSuccess());

        assertThatThrownBy(() -> clienteIntegracao.getCliente(token, cpf))
                .isInstanceOf(ResourceAccessException.class);
    }
}