package br.com.fiap.postech.hackapay.cartao.controller;

import br.com.fiap.postech.hackapay.cartao.entities.Cartao;
import br.com.fiap.postech.hackapay.cartao.services.CartaoService;
import br.com.fiap.postech.hackapay.security.SecurityHelper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cartao")
public class CartaoController {

    private final CartaoService cartaoService;
    private final SecurityHelper securityHelper;

    @Autowired
    public CartaoController(CartaoService cartaoService, SecurityHelper securityHelper) {
        this.cartaoService = cartaoService;
        this.securityHelper = securityHelper;
    }

    @Operation(summary = "registra um cartao")
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody Cartao cartaoDTO) {
        try {
            String token = securityHelper.getToken();
            Cartao savedCartaoDTO = cartaoService.save(token, cartaoDTO);
            return new ResponseEntity<>(savedCartaoDTO, HttpStatus.OK);
        } catch (IllegalArgumentException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "lista todos os cartaos")
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Page<Cartao>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String numero
    ) {
        Cartao cartao = new Cartao(cpf, null, numero, null, null);
        cartao.setId(null);
        var pageable = PageRequest.of(page, size);
        var cartaos = cartaoService.findAll(pageable, cartao);
        return new ResponseEntity<>(cartaos, HttpStatus.OK);
    }

    @Operation(summary = "lista um cartao por seu id")
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        try {
            Cartao cartao = cartaoService.findById(id);
            return ResponseEntity.ok(cartao);
        } catch (IllegalArgumentException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "altera um cartao por seu id")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @Valid @RequestBody Cartao cartaoDTO) {
        try {
            Cartao updatedCartao = cartaoService.update(id, cartaoDTO);
            return new ResponseEntity<>(updatedCartao, HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "atualiza o limite de um cartao dentro do processo de pagamento")
    @PostMapping("/atualizaLimiteCartao/{valor}")
    public ResponseEntity<?> atualizaLimiteCartao(@PathVariable Double valor, @Valid @RequestBody Cartao cartaoDTO) {
        try {
            cartaoService.atualizaLimiteCartao(valor, cartaoDTO);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "remove um cartao por seu id")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        try {
            cartaoService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException
                exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
