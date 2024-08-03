package br.com.fiap.postech.hackapay.cartao.repository;

import br.com.fiap.postech.hackapay.cartao.entities.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartaoRepository extends JpaRepository<Cartao, UUID> {
    Integer countByCpf(String cpf);

    Optional<Cartao> findByNumero(String numero);
}
