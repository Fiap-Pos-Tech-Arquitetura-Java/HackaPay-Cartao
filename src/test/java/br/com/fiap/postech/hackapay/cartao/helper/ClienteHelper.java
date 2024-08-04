package br.com.fiap.postech.hackapay.cartao.helper;

import br.com.fiap.postech.hackapay.cartao.integration.Cliente;

import java.util.UUID;

public class ClienteHelper {
    public static Cliente getCliente() {
        return new Cliente();
    }
}
