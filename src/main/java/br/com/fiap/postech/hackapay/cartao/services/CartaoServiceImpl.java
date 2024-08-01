package br.com.fiap.postech.hackapay.cartao.services;

import br.com.fiap.postech.hackapay.cartao.entities.Cartao;
import br.com.fiap.postech.hackapay.cartao.repository.CartaoRepository;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CartaoServiceImpl implements CartaoService {

    private final CartaoRepository cartaoRepository;

    @Autowired
    public CartaoServiceImpl(CartaoRepository cartaoRepository) {
        this.cartaoRepository = cartaoRepository;
    }

    @Override
    public Cartao save(Cartao cartao) {
        if (cartaoRepository.findByCpf(cartao.getCpf()).isPresent()) {
            throw new IllegalArgumentException("Já existe um cartao cadastrado com esse cpf.");
        }
        cartao.setId(UUID.randomUUID());
        return cartaoRepository.save(cartao);
    }

    @Override
    public Page<Cartao> findAll(Pageable pageable, Cartao cartao) {
        Example<Cartao> cartaoExample = Example.of(cartao);
        return cartaoRepository.findAll(cartaoExample, pageable);
    }

    @Override
    public Cartao findById(UUID id) {
        return cartaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cartao não encontrado com o ID: " + id));
    }

    @Override
    public Cartao update(UUID id, Cartao cartaoParam) {
        Cartao cartao = findById(id);
        if (StringUtils.isNotEmpty(cartaoParam.getNumero())) {
            cartao.setNumero(cartaoParam.getNumero());
        }
        if (cartaoParam.getId() != null && !cartao.getId().equals(cartaoParam.getId())) {
            throw new IllegalArgumentException("Não é possível alterar o id de um cartao.");
        }
        if (cartaoParam.getCpf() != null && !cartao.getCpf().equals(cartaoParam.getCpf())) {
            throw new IllegalArgumentException("Não é possível alterar o cpf de um cartao.");
        }
        if (cartaoParam.getLimite() != null) {
            cartao.setLimite(cartaoParam.getLimite());
        }
        cartao = cartaoRepository.save(cartao);
        return cartao;
    }

    @Override
    public void delete(UUID id) {
        findById(id);
        cartaoRepository.deleteById(id);
    }
}
