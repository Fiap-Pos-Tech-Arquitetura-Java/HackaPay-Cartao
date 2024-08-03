package br.com.fiap.postech.hackapay.cartao.services;

import br.com.fiap.postech.hackapay.cartao.entities.Cartao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CartaoService {
    Cartao save(String token, Cartao cartao);

    Page<Cartao> findAll(Pageable pageable, Cartao cartao);

    Cartao findById(UUID id);

    Cartao update(UUID id, Cartao cartao);

    void delete(UUID id);

    void atualizaLimiteCartao(Double valor, Cartao cartaoDTO);
}
