package br.com.fiap.postech.hackapay.cartao.helper;

import br.com.fiap.postech.hackapay.cartao.entities.Cartao;

import java.util.UUID;

public class CartaoHelper {
    public static Cartao getCartao(boolean geraId) {
        var cartao = new Cartao(
                "25310413030",
                1000D,
                "4417810025751018",
                "12/30",
                "234"
        );
        if (geraId) {
            cartao.setId(UUID.randomUUID());
        }
        return cartao;
    }
}
