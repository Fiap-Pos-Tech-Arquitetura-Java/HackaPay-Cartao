package br.com.fiap.postech.hackapay.cartao.services;

import br.com.fiap.postech.hackapay.cartao.entities.Cartao;
import br.com.fiap.postech.hackapay.cartao.integration.Cliente;
import br.com.fiap.postech.hackapay.cartao.integration.ClienteIntegracao;
import br.com.fiap.postech.hackapay.cartao.repository.CartaoRepository;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CartaoServiceImpl implements CartaoService {

    private final CartaoRepository cartaoRepository;
    private final ClienteIntegracao clienteIntegracao;

    @Autowired
    public CartaoServiceImpl(CartaoRepository cartaoRepository, ClienteIntegracao clienteIntegracao) {
        this.cartaoRepository = cartaoRepository;
        this.clienteIntegracao = clienteIntegracao;
    }

    @Override
    public Cartao save(String token, Cartao cartao) {
        if (cartaoRepository.countByCpf(cartao.getCpf()) == 2) {
            throw new IllegalArgumentException("um Cliente pode ter no maximo 2 cartoes.");
        }
        Cliente cliente = clienteIntegracao.getCliente(token, cartao.getCpf());
        if (cliente == null) {
            throw new IllegalStateException("Cliente nao cadastrado.");
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

    @Override
    public void atualizaLimiteCartao(Double valor, Cartao cartao) {
        Optional<Cartao> optionalCartao = cartaoRepository.findByNumero(cartao.getNumero());
        optionalCartao.ifPresentOrElse(
                (cartaoAux) -> {
                    if (!cartaoAux.getCpf().equals(cartao.getCpf())) {
                        throw new IllegalArgumentException("cpf do cartao nao confere");
                    } else if (!cartaoAux.getCvv().equals(cartao.getCvv())) {
                        throw new IllegalArgumentException("cvv do cartao nao confere");
                    } else if (!cartaoAux.getDataValidade().equals(cartao.getDataValidade())) {
                        throw new IllegalArgumentException("data de validade do cartao nao confere");
                    }
                    cartaoAux.setLimite(cartaoAux.getLimite() - valor);
                    if (cartaoAux.getLimite().compareTo(0D) < 0) {
                        throw new IllegalArgumentException("nao ha mais limite disponivel no cartao.");
                    }
                    cartaoRepository.save(cartaoAux);
                }, () -> {
                    throw new IllegalArgumentException("cartao nao encontrado");
                }
        );
    }
}
