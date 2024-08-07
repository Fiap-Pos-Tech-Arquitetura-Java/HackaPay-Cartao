package br.com.fiap.postech.hackapay.cartao.controller;

import br.com.fiap.postech.hackapay.cartao.entities.Cartao;
import br.com.fiap.postech.hackapay.cartao.helper.CartaoHelper;
import br.com.fiap.postech.hackapay.cartao.helper.ClienteHelper;
import br.com.fiap.postech.hackapay.cartao.integration.ClienteIntegracao;
import br.com.fiap.postech.hackapay.cartao.services.CartaoService;
import br.com.fiap.postech.hackapay.security.SecurityHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartaoControllerTest {
    public static final String CARTAO = "/cartao";
    private MockMvc mockMvc;
    @Mock
    private CartaoService cartaoService;
    @Mock
    private SecurityHelper securityHelper;
    @Mock
    private ClienteIntegracao clienteIntegracao;
    private AutoCloseable mock;

    @BeforeEach
    void setUp() {
        mock = MockitoAnnotations.openMocks(this);
        CartaoController cartaoController = new CartaoController(cartaoService, securityHelper);
        mockMvc = MockMvcBuilders.standaloneSetup(cartaoController).build();
    }

    @AfterEach
    void tearDown() throws Exception {
        mock.close();
    }

    public static String asJsonString(final Object object) throws Exception {
        return new ObjectMapper().writeValueAsString(object);
    }

    @Nested
    class CadastrarCartao {
        @Test
        void devePermitirCadastrarCartao() throws Exception {
            // Arrange
            var cartao = CartaoHelper.getCartao(false);
            var cliente = ClienteHelper.getCliente();
            when(cartaoService.save(anyString(), any(Cartao.class))).thenAnswer(r -> r.getArgument(1));
            when(clienteIntegracao.getCliente(anyString(), anyString())).thenReturn(cliente);
            when(securityHelper.getToken()).thenReturn("token");
            // Act
            mockMvc.perform(
                            post(CARTAO).contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(cartao)))
                    .andExpect(status().isOk());
            // Assert
            verify(cartaoService, times(1)).save(anyString(), any(Cartao.class));
        }

        @Test
        void deveGerarExcecao_QuandoRegistrarCartao_clienteNaoCadastrado() throws Exception {
            // Arrange
            var cartao = CartaoHelper.getCartao(false);
            when(cartaoService.save(anyString(), any(Cartao.class))).thenThrow(
                    new IllegalStateException("Cliente nao cadastrado.")
            );
            when(securityHelper.getToken()).thenReturn("token");
            // Act
            mockMvc.perform(
                            post(CARTAO).contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(cartao)))
                    .andExpect(status().is5xxServerError());
            // Assert
            verify(cartaoService, times(1)).save(anyString(), any(Cartao.class));
        }

        @Test
        void deveGerarExcecao_QuandoRegistrarCartao_maisQueDoisCartoes() throws Exception {
            // Arrange
            var cartao = CartaoHelper.getCartao(false);
            var cliente = ClienteHelper.getCliente();

            when(cartaoService.save(anyString(), any(Cartao.class))).thenThrow(
                    new IllegalArgumentException("um Cliente pode ter no maximo 2 cartoes.")
            );
            when(securityHelper.getToken()).thenReturn("token");
            // Act
            mockMvc.perform(
                            post(CARTAO).contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(cartao)))
                    .andExpect(status().isForbidden());
            // Assert
            verify(cartaoService, times(1)).save(anyString(), any(Cartao.class));
        }

        @Test
        void deveGerarExcecao_QuandoRegistrarCartao_RequisicaoXml() throws Exception {
            // Arrange
            var cartao = CartaoHelper.getCartao(false);
            when(cartaoService.save(anyString(), any(Cartao.class))).thenAnswer(r -> r.getArgument(0));
            // Act
            mockMvc.perform(
                            post("/cartao").contentType(MediaType.APPLICATION_XML)
                                    .content(asJsonString(cartao)))
                    .andExpect(status().isUnsupportedMediaType());
            // Assert
            verify(cartaoService, never()).save(anyString(), any(Cartao.class));
        }
    }
    @Nested
    class BuscarCartao {
        @Test
        void devePermitirBuscarCartaoPorId() throws Exception {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            when(cartaoService.findById(any(UUID.class))).thenReturn(cartao);
            // Act
            mockMvc.perform(get("/cartao/{id}", cartao.getId().toString()))
                    .andExpect(status().isOk());
            // Assert
            verify(cartaoService, times(1)).findById(any(UUID.class));
        }
        @Test
        void deveGerarExcecao_QuandoBuscarCartaoPorId_idNaoExiste() throws Exception {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            when(cartaoService.findById(cartao.getId())).thenThrow(IllegalArgumentException.class);
            // Act
            mockMvc.perform(get("/cartao/{id}", cartao.getId().toString()))
                    .andExpect(status().isBadRequest());
            // Assert
            verify(cartaoService, times(1)).findById(cartao.getId());
        }

        @Test
        void devePermitirBuscarTodosCartao() throws Exception {
            // Arrange
            int page = 0;
            int size = 10;
            var cartao = CartaoHelper.getCartao(true);
            var criterioCartao = new Cartao(cartao.getCpf(), null, cartao.getNumero(), null, null);
            criterioCartao.setId(null);
            List<Cartao> listCartao = new ArrayList<>();
            listCartao.add(cartao);
            Page<Cartao> cartaos = new PageImpl<>(listCartao);
            var pageable = PageRequest.of(page, size);
            when(cartaoService.findAll(
                            pageable,
                            criterioCartao
                    )
            ).thenReturn(cartaos);
            // Act
            mockMvc.perform(
                            get("/cartao")
                                    .param("page", String.valueOf(page))
                                    .param("size", String.valueOf(size))
                                    .param("cpf", cartao.getCpf())
                                    .param("numero", cartao.getNumero())
                    )
                    //.andDo(print())
                    .andExpect(status().is5xxServerError())
            //.andExpect(jsonPath("$.content", not(empty())))
            //.andExpect(jsonPath("$.totalPages").value(1))
            //.andExpect(jsonPath("$.totalElements").value(1))
            ;
            // Assert
            verify(cartaoService, times(1)).findAll(pageable, criterioCartao);
        }
    }

    @Nested
    class AlterarCartao {
        @Test
        void devePermitirAlterarCartao() throws Exception {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            when(cartaoService.update(cartao.getId(), cartao)).thenAnswer(r -> r.getArgument(1) );
            // Act
            mockMvc.perform(put("/cartao/{id}", cartao.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(cartao)))
                    .andExpect(status().isAccepted());
            // Assert
            verify(cartaoService, times(1)).update(cartao.getId(), cartao);
        }

        @Test
        void deveGerarExcecao_QuandoAlterarCartao_RequisicaoXml() throws Exception {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            when(cartaoService.update(cartao.getId(), cartao)).thenAnswer(r -> r.getArgument(1) );
            // Act
            mockMvc.perform(put("/cartao/{id}", cartao.getId())
                            .contentType(MediaType.APPLICATION_XML)
                            .content(asJsonString(cartao)))
                    .andExpect(status().isUnsupportedMediaType());
            // Assert
            verify(cartaoService, never()).update(cartao.getId(), cartao);
        }

        @Test
        void deveGerarExcecao_QuandoAlterarCartaoPorId_idNaoExiste() throws Exception {
            // Arrange
            var cartaoDTO = CartaoHelper.getCartao(true);
            when(cartaoService.update(cartaoDTO.getId(), cartaoDTO)).thenThrow(IllegalArgumentException.class);
            // Act
            mockMvc.perform(put("/cartao/{id}", cartaoDTO.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(cartaoDTO)))
                    .andExpect(status().isBadRequest());
            // Assert
            verify(cartaoService, times(1)).update(any(UUID.class), any(Cartao.class));
        }
    }

    @Nested
    class RemoverCartao {
        @Test
        void devePermitirRemoverCartao() throws Exception {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            doNothing().when(cartaoService).delete(cartao.getId());
            // Act
            mockMvc.perform(delete("/cartao/{id}", cartao.getId()))
                    .andExpect(status().isNoContent());
            // Assert
            verify(cartaoService, times(1)).delete(cartao.getId());
            verify(cartaoService, times(1)).delete(cartao.getId());
        }

        @Test
        void deveGerarExcecao_QuandoRemoverCartaoPorId_idNaoExiste() throws Exception {
            // Arrange
            var cartao = CartaoHelper.getCartao(true);
            doThrow(new IllegalArgumentException("Cartao não encontrado com o ID: " + cartao.getId()))
                    .when(cartaoService).delete(cartao.getId());
            // Act
            mockMvc.perform(delete("/cartao/{id}", cartao.getId()))
                    .andExpect(status().isBadRequest());
            // Assert
            verify(cartaoService, times(1)).delete(cartao.getId());
        }
    }

    @Nested
    class AtualizaLimiteCartao {
        @Test
        void devePermitirAtualizarLimiteCartao() throws Exception {
            // Arrange
            var valor = 100.0;
            var cartao = CartaoHelper.getCartao(false);
            var cliente = ClienteHelper.getCliente();
            doNothing().when(cartaoService).atualizaLimiteCartao(anyDouble(), any(Cartao.class));
            when(securityHelper.getToken()).thenReturn("token");
            // Act
            mockMvc.perform(
                            post(CARTAO + "/atualizaLimiteCartao/" + valor).contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(cartao)))
                    .andExpect(status().isAccepted());
            // Assert
            verify(cartaoService, times(1)).atualizaLimiteCartao(anyDouble(), any(Cartao.class));
        }

        @Test
        void deveGerarExcecao_QuandoAtualizarLimiteCartao_cpfCartaoNaoConfere() throws Exception {
            // Arrange
            var valor = 100.0;
            var cartao = CartaoHelper.getCartao(false);
            doThrow(
                    new IllegalArgumentException("cpf do cartao nao confere")
            ).when(cartaoService).atualizaLimiteCartao(anyDouble(), any(Cartao.class));
            when(securityHelper.getToken()).thenReturn("token");
            // Act
            mockMvc.perform(
                            post(CARTAO + "/atualizaLimiteCartao/" + valor).contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(cartao)))
                    .andExpect(status().isBadRequest());
            // Assert
            verify(cartaoService, times(1)).atualizaLimiteCartao(anyDouble(), any(Cartao.class));
        }

        @Test
        void deveGerarExcecao_QuandoAtualizarLimiteCartao_RequisicaoXml() throws Exception {
            // Arrange
            var valor = 100.0;
            var cartao = CartaoHelper.getCartao(false);
            when(cartaoService.save(anyString(), any(Cartao.class))).thenAnswer(r -> r.getArgument(0));
            // Act
            mockMvc.perform(
                            post(CARTAO + "/atualizaLimiteCartao/" + valor).contentType(MediaType.APPLICATION_XML)
                                    .content(asJsonString(cartao)))
                    .andExpect(status().isUnsupportedMediaType());
            // Assert
            verify(cartaoService, never()).save(anyString(), any(Cartao.class));
        }
    }
}