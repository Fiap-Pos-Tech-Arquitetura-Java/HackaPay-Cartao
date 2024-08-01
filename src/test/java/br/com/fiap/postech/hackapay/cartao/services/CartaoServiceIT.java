package br.com.fiap.postech.hackapay.cartao.services;

import br.com.fiap.postech.hackapay.cartao.entities.Cartao;
import br.com.fiap.postech.hackapay.cartao.helper.CartaoHelper;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Transactional
public class CartaoServiceIT {
    @Autowired
    private CartaoService cartaoService;

    @Nested
    class CadastrarCartao {
        @Test
        void devePermitirCadastrarCartao() {
            // Arrange
            var cartao = CartaoHelper.getCartao(false);
            // Act
            var cartaoSalvo = cartaoService.save(cartao);
            // Assert
            assertThat(cartaoSalvo)
                    .isInstanceOf(Cartao.class)
                    .isNotNull();
            assertThat(cartaoSalvo.getNumero()).isEqualTo(cartao.getNumero());
            assertThat(cartaoSalvo.getId()).isNotNull();
        }
    }

    @Nested
    class BuscarCartao {
        @Test
        void devePermitirBuscarCartaoPorId() {
            // Arrange
            var id = UUID.fromString("56833f9a-7fda-49d5-a760-8e1ba41f35a8");
            var numero = "4417810025751018";
            // Act
            var cartaoObtido = cartaoService.findById(id);
            // Assert
            assertThat(cartaoObtido).isNotNull().isInstanceOf(Cartao.class);
            assertThat(cartaoObtido.getNumero()).isEqualTo(numero);
            assertThat(cartaoObtido.getId()).isNotNull();
            assertThat(cartaoObtido.getId()).isEqualTo(id);
        }

        @Test
        void deveGerarExcecao_QuandoBuscarCartaoPorId_idNaoExiste() {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            UUID uuid = cartao.getId();
            // Act &&  Assert
            assertThatThrownBy(() -> cartaoService.findById(uuid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cartao não encontrado com o ID: " + cartao.getId());
        }

        @Test
        void devePermitirBuscarTodosCartao() {
            // Arrange
            Cartao criteriosDeBusca = new Cartao();
            criteriosDeBusca.setId(null);
            // Act
            var listaCartaosObtidos = cartaoService.findAll(Pageable.unpaged(), criteriosDeBusca);
            // Assert
            assertThat(listaCartaosObtidos).isNotNull().isInstanceOf(Page.class);
            assertThat(listaCartaosObtidos.getContent()).asList().hasSize(3);
            assertThat(listaCartaosObtidos.getContent()).asList().allSatisfy(
                    cartaoObtido -> {
                        assertThat(cartaoObtido).isNotNull();
                    }
            );
        }
    }

    @Nested
    class AlterarCartao {

        @Test
        void devePermitirAlterarCartao() {
            // Arrange
            var id = UUID.fromString("ab8fdcd5-c9b5-471e-8ad0-380a65d6cc86");
            var numero = "44170025752222";
            var cpf = "52816804046";
            var cartao = new Cartao(cpf, null, numero,
                    RandomStringUtils.random(20, true, true),
                    RandomStringUtils.random(20, true, true)
            );
            cartao.setId(null);
            // Act
            var cartaoAtualizada = cartaoService.update(id, cartao);
            // Assert
            assertThat(cartaoAtualizada).isNotNull().isInstanceOf(Cartao.class);
            assertThat(cartaoAtualizada.getId()).isNotNull();
            assertThat(cartaoAtualizada.getNumero()).isEqualTo(numero);
        }

        @Test
        void deveGerarExcecao_QuandoAlterarCartaoPorId_idNaoExiste() {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            var uuid = cartao.getId();
            // Act &&  Assert
            assertThatThrownBy(() -> cartaoService.update(uuid, cartao))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cartao não encontrado com o ID: " + cartao.getId());
        }
    }

    @Nested
    class RemoverCartao {
        @Test
        void devePermitirRemoverCartao() {
            // Arrange
            var id = UUID.fromString("8855e7b2-77b6-448b-97f8-8a0b529f3976");
            // Act
            cartaoService.delete(id);
            // Assert
            assertThatThrownBy(() -> cartaoService.findById(id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cartao não encontrado com o ID: " + id);
            ;
        }

        @Test
        void deveGerarExcecao_QuandRemoverCartaoPorId_idNaoExiste() {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            var uuid = cartao.getId();
            // Act &&  Assert
            assertThatThrownBy(() -> cartaoService.delete(uuid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cartao não encontrado com o ID: " + cartao.getId());
            ;
        }
    }
}
