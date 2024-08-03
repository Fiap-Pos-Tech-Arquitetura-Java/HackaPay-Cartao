package br.com.fiap.postech.hackapay.cartao.services;

import br.com.fiap.postech.hackapay.cartao.entities.Cartao;
import br.com.fiap.postech.hackapay.cartao.helper.CartaoHelper;
import br.com.fiap.postech.hackapay.cartao.integration.ClienteIntegracao;
import br.com.fiap.postech.hackapay.cartao.repository.CartaoRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartaoServiceTest {
    private CartaoService cartaoService;

    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private ClienteIntegracao clienteIntegracao;

    private AutoCloseable mock;

    @BeforeEach
    void setUp() {
        mock = MockitoAnnotations.openMocks(this);
        cartaoService = new CartaoServiceImpl(cartaoRepository, clienteIntegracao);
    }

    @AfterEach
    void tearDown() throws Exception {
        mock.close();
    }

    @Nested
    class CadastrarCartao {
        @Test
        void devePermitirCadastrarCartao() {
            // Arrange
            var cartao = CartaoHelper.getCartao(false);
            when(cartaoRepository.save(any(Cartao.class))).thenAnswer(r -> r.getArgument(0));
            // Act
            var cartaoSalvo = cartaoService.save(cartao);
            // Assert
            assertThat(cartaoSalvo)
                    .isInstanceOf(Cartao.class)
                    .isNotNull();
            assertThat(cartaoSalvo.getNumero()).isEqualTo(cartao.getNumero());
            assertThat(cartaoSalvo.getId()).isNotNull();
            verify(cartaoRepository, times(1)).save(any(Cartao.class));
        }

        @Test
        void deveGerarExcecao_QuandoCadastrarCartao_cpfTemDoisCartoes() {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            when(cartaoRepository.countByCpf(cartao.getCpf())).thenReturn(2);
            // Act
            assertThatThrownBy(() -> cartaoService.save(cartao))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("um Cliente pode ter no maximo 2 cartoes.");
            // Assert
            verify(cartaoRepository, times(1)).countByCpf(anyString());
            verify(cartaoRepository, never()).save(any(Cartao.class));
        }
    }

    @Nested
    class BuscarCartao {
        @Test
        void devePermitirBuscarCartaoPorId() {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            when(cartaoRepository.findById(cartao.getId())).thenReturn(Optional.of(cartao));
            // Act
            var cartaoObtido = cartaoService.findById(cartao.getId());
            // Assert
            assertThat(cartaoObtido).isEqualTo(cartao);
            verify(cartaoRepository, times(1)).findById(any(UUID.class));
        }

        @Test
        void deveGerarExcecao_QuandoBuscarCartaoPorId_idNaoExiste() {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            when(cartaoRepository.findById(cartao.getId())).thenReturn(Optional.empty());
            UUID uuid = cartao.getId();
            // Act
            assertThatThrownBy(() -> cartaoService.findById(uuid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cartao não encontrado com o ID: " + cartao.getId());
            // Assert
            verify(cartaoRepository, times(1)).findById(any(UUID.class));
        }

        @Test
        void devePermitirBuscarTodosCartao() {
            // Arrange
            Cartao criteriosDeBusca = CartaoHelper.getCartao(false);
            Page<Cartao> cartaos = new PageImpl<>(Arrays.asList(
                    CartaoHelper.getCartao(true),
                    CartaoHelper.getCartao(true),
                    CartaoHelper.getCartao(true)
            ));
            when(cartaoRepository.findAll(any(Example.class), any(Pageable.class))).thenReturn(cartaos);
            // Act
            var cartaosObtidos = cartaoService.findAll(Pageable.unpaged(), criteriosDeBusca);
            // Assert
            assertThat(cartaosObtidos).hasSize(3);
            assertThat(cartaosObtidos.getContent()).asList().allSatisfy(
                    cartao -> {
                        assertThat(cartao)
                                .isNotNull()
                                .isInstanceOf(Cartao.class);
                    }
            );
            verify(cartaoRepository, times(1)).findAll(any(Example.class), any(Pageable.class));
        }
    }

    @Nested
    class AlterarCartao {
        @Test
        void devePermitirAlterarCartao() {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            var cartaoReferencia = new Cartao(
                    cartao.getCpf(),
                    cartao.getLimite(),
                    cartao.getNumero(),
                    cartao.getDataValidade(),
                    cartao.getCvv()
            );
            var novoCartao = new Cartao(
                    cartao.getCpf(),
                    1001,
                    RandomStringUtils.random(20, true, true),
                    RandomStringUtils.random(20, true, true),
                    RandomStringUtils.random(20, true, true)
            );
            novoCartao.setId(cartao.getId());
            when(cartaoRepository.findById(cartao.getId())).thenReturn(Optional.of(cartao));
            when(cartaoRepository.save(any(Cartao.class))).thenAnswer(r -> r.getArgument(0));
            // Act
            var cartaoSalvo = cartaoService.update(cartao.getId(), novoCartao);
            // Assert
            assertThat(cartaoSalvo)
                    .isInstanceOf(Cartao.class)
                    .isNotNull();
            assertThat(cartaoSalvo.getNumero()).isEqualTo(novoCartao.getNumero());
            assertThat(cartaoSalvo.getNumero()).isNotEqualTo(cartaoReferencia.getNumero());

            verify(cartaoRepository, times(1)).findById(any(UUID.class));
            verify(cartaoRepository, times(1)).save(any(Cartao.class));
        }

        @Test
        void devePermitirAlterarCartao_enderecoComId() {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            var cartaoReferencia = new Cartao(
                    cartao.getCpf(),
                    cartao.getLimite(),
                    cartao.getNumero(),
                    cartao.getDataValidade(),
                    cartao.getCvv()
            );
            var novoCartao = new Cartao(
                    cartao.getCpf(),
                    1001,
                    RandomStringUtils.random(20, true, true),
                    RandomStringUtils.random(20, true, true),
                    RandomStringUtils.random(20, true, true)
            );
            novoCartao.setId(cartao.getId());
            when(cartaoRepository.findById(cartao.getId())).thenReturn(Optional.of(cartao));
            when(cartaoRepository.save(any(Cartao.class))).thenAnswer(r -> r.getArgument(0));
            // Act
            var cartaoSalvo = cartaoService.update(cartao.getId(), novoCartao);
            // Assert
            assertThat(cartaoSalvo)
                    .isInstanceOf(Cartao.class)
                    .isNotNull();
            assertThat(cartaoSalvo.getNumero()).isEqualTo(novoCartao.getNumero());
            assertThat(cartaoSalvo.getNumero()).isNotEqualTo(cartaoReferencia.getNumero());

            verify(cartaoRepository, times(1)).findById(any(UUID.class));
            verify(cartaoRepository, times(1)).save(any(Cartao.class));
        }

        @Test
        void devePermitirAlterarCartao_semBody() {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);

            var cartaoReferencia = new Cartao(
                    cartao.getCpf(),
                    cartao.getLimite(),
                    cartao.getNumero(),
                    cartao.getDataValidade(),
                    cartao.getCvv()
            );
            var novoCartao = new Cartao(null, null, null, null, null);

            novoCartao.setId(cartao.getId());
            when(cartaoRepository.findById(cartao.getId())).thenReturn(Optional.of(cartao));
            when(cartaoRepository.save(any(Cartao.class))).thenAnswer(r -> r.getArgument(0));
            // Act
            var cartaoSalvo = cartaoService.update(cartao.getId(), novoCartao);
            // Assert
            assertThat(cartaoSalvo)
                    .isInstanceOf(Cartao.class)
                    .isNotNull();
            assertThat(cartaoSalvo.getNumero()).isEqualTo(cartaoReferencia.getNumero());

            verify(cartaoRepository, times(1)).findById(any(UUID.class));
            verify(cartaoRepository, times(1)).save(any(Cartao.class));
        }

        @Test
        void deveGerarExcecao_QuandoAlterarCartaoPorId_idNaoExiste() {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            when(cartaoRepository.findById(cartao.getId())).thenReturn(Optional.empty());
            UUID uuid = cartao.getId();
            // Act && Assert
            assertThatThrownBy(() -> cartaoService.update(uuid, cartao))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cartao não encontrado com o ID: " + cartao.getId());
            verify(cartaoRepository, times(1)).findById(any(UUID.class));
            verify(cartaoRepository, never()).save(any(Cartao.class));
        }

        @Test
        void deveGerarExcecao_QuandoAlterarCartaoPorId_alterandoId() {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            var cartaoParam = CartaoHelper.getCartao(true);
            when(cartaoRepository.findById(cartao.getId())).thenReturn(Optional.of(cartao));
            UUID uuid = cartao.getId();
            // Act && Assert
            assertThatThrownBy(() -> cartaoService.update(uuid, cartaoParam))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Não é possível alterar o id de um cartao.");
            verify(cartaoRepository, times(1)).findById(any(UUID.class));
            verify(cartaoRepository, never()).save(any(Cartao.class));
        }

        @Test
        void deveGerarExcecao_QuandoAlterarCartaoPorId_alterandoCpf() {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            var cartaoParam = CartaoHelper.getCartao(true);
            cartaoParam.setId(cartao.getId());
            cartaoParam.setCpf("03485066001");
            when(cartaoRepository.findById(cartao.getId())).thenReturn(Optional.of(cartao));
            UUID uuid = cartao.getId();
            // Act && Assert
            assertThatThrownBy(() -> cartaoService.update(uuid, cartaoParam))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Não é possível alterar o cpf de um cartao.");
            verify(cartaoRepository, times(1)).findById(any(UUID.class));
            verify(cartaoRepository, never()).save(any(Cartao.class));
        }
    }

    @Nested
    class RemoverCartao {
        @Test
        void devePermitirRemoverCartao() {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            when(cartaoRepository.findById(cartao.getId())).thenReturn(Optional.of(cartao));
            doNothing().when(cartaoRepository).deleteById(cartao.getId());
            // Act
            cartaoService.delete(cartao.getId());
            // Assert
            verify(cartaoRepository, times(1)).findById(any(UUID.class));
            verify(cartaoRepository, times(1)).deleteById(any(UUID.class));
        }

        @Test
        void deveGerarExcecao_QuandRemoverCartaoPorId_idNaoExiste() {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            doNothing().when(cartaoRepository).deleteById(cartao.getId());
            UUID uuid = cartao.getId();
            // Act && Assert
            assertThatThrownBy(() -> cartaoService.delete(uuid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cartao não encontrado com o ID: " + cartao.getId());
            verify(cartaoRepository, times(1)).findById(any(UUID.class));
            verify(cartaoRepository, never()).deleteById(any(UUID.class));
        }
    }
}