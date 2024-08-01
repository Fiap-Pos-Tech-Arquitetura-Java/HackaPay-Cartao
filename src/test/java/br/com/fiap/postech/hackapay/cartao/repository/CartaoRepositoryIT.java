package br.com.fiap.postech.hackapay.cartao.repository;

import br.com.fiap.postech.hackapay.cartao.entities.Cartao;
import br.com.fiap.postech.hackapay.cartao.helper.CartaoHelper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Transactional
public class CartaoRepositoryIT {
    @Autowired
    private CartaoRepository cartaoRepository;

    @Test
    void devePermitirCriarEstrutura() {
        var totalRegistros = cartaoRepository.count();
        assertThat(totalRegistros).isEqualTo(3);
    }

    @Test
    void devePermitirCadastrarCartao() {
        // Arrange
        var cartao = CartaoHelper.getCartao(true);
        // Act
        var cartaoCadastrado = cartaoRepository.save(cartao);
        // Assert
        assertThat(cartaoCadastrado).isInstanceOf(Cartao.class).isNotNull();
        assertThat(cartaoCadastrado.getId()).isEqualTo(cartao.getId());
        assertThat(cartaoCadastrado.getNumero()).isEqualTo(cartao.getNumero());
        assertThat(cartaoCadastrado.getLimite()).isEqualTo(cartao.getLimite());
    }
    @Test
    void devePermitirBuscarCartao() {
        // Arrange
        var id = UUID.fromString("56833f9a-7fda-49d5-a760-8e1ba41f35a8");
        var numero = "4417810025751018";
        // Act
        var cartaoOpcional = cartaoRepository.findById(id);
        // Assert
        assertThat(cartaoOpcional).isPresent();
        cartaoOpcional.ifPresent(
                cartaoRecebido -> {
                    assertThat(cartaoRecebido).isInstanceOf(Cartao.class).isNotNull();
                    assertThat(cartaoRecebido.getId()).isEqualTo(id);
                    assertThat(cartaoRecebido.getNumero()).isEqualTo(numero);
                }
        );
    }
    @Test
    void devePermitirRemoverCartao() {
        // Arrange
        var id = UUID.fromString("8855e7b2-77b6-448b-97f8-8a0b529f3976");
        // Act
        cartaoRepository.deleteById(id);
        // Assert
        var cartaoOpcional = cartaoRepository.findById(id);
        assertThat(cartaoOpcional).isEmpty();
    }
    @Test
    void devePermitirListarCartaos() {
        // Arrange
        // Act
        var cartaosListados = cartaoRepository.findAll();
        // Assert
        assertThat(cartaosListados).hasSize(3);
    }
}
