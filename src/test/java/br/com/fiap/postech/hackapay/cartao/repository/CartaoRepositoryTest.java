package br.com.fiap.postech.hackapay.cartao.repository;

import br.com.fiap.postech.hackapay.cartao.entities.Cartao;
import br.com.fiap.postech.hackapay.cartao.helper.CartaoHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartaoRepositoryTest {
    @Mock
    private CartaoRepository cartaoRepository;

    AutoCloseable openMocks;
    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void devePermitirCadastrarCartao() {
        // Arrange
        var cartao = CartaoHelper.getCartao(false);
        when(cartaoRepository.save(any(Cartao.class))).thenReturn(cartao);
        // Act
        var savedCartao = cartaoRepository.save(cartao);
        // Assert
        assertThat(savedCartao).isNotNull().isEqualTo(cartao);
        verify(cartaoRepository, times(1)).save(any(Cartao.class));
    }

    @Test
    void devePermitirBuscarCartao() {
        // Arrange
        var cartao = CartaoHelper.getCartao(true);
        when(cartaoRepository.findById(cartao.getId())).thenReturn(Optional.of(cartao));
        // Act
        var cartaoOpcional = cartaoRepository.findById(cartao.getId());
        // Assert
        assertThat(cartaoOpcional).isNotNull().containsSame(cartao);
        cartaoOpcional.ifPresent(
                cartaoRecebido -> {
                    assertThat(cartaoRecebido).isInstanceOf(Cartao.class).isNotNull();
                    assertThat(cartaoRecebido.getId()).isEqualTo(cartao.getId());
                    assertThat(cartaoRecebido.getNumero()).isEqualTo(cartao.getNumero());
                }
        );
        verify(cartaoRepository, times(1)).findById(cartao.getId());
    }
    @Test
    void devePermitirRemoverCartao() {
        //Arrange
        var id = UUID.randomUUID();
        doNothing().when(cartaoRepository).deleteById(id);
        //Act
        cartaoRepository.deleteById(id);
        //Assert
        verify(cartaoRepository, times(1)).deleteById(id);
    }
    @Test
    void devePermitirListarCartaos() {
        // Arrange
        var cartao1 = CartaoHelper.getCartao(true);
        var cartao2 = CartaoHelper.getCartao(true);
        var listaCartaos = Arrays.asList(
                cartao1,
                cartao2
        );
        when(cartaoRepository.findAll()).thenReturn(listaCartaos);
        // Act
        var cartaosListados = cartaoRepository.findAll();
        assertThat(cartaosListados)
                .hasSize(2)
                .containsExactlyInAnyOrder(cartao1, cartao2);
        verify(cartaoRepository, times(1)).findAll();
    }
}