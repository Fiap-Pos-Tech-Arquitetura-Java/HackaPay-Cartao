package br.com.fiap.postech.hackapay.cartao.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ClienteIntegracao {

    @Value("${hackapay.cliente.url}")
    String baseURI;

    public Cliente getCliente(String token, String cpf) {
        RestClient restClient = RestClient.create();
        return restClient.get()
                .uri(baseURI + "/findByCpf/{cpf}", cpf)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Cliente.class);
    }
}
