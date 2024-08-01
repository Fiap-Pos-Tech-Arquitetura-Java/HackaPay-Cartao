package br.com.fiap.postech.hackapay.cartao.controller;

import br.com.fiap.postech.hackapay.cartao.entities.Cartao;
import br.com.fiap.postech.hackapay.cartao.services.CartaoService;
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

    @Autowired
    public CartaoController(CartaoService cartaoService) {
        this.cartaoService = cartaoService;
    }

    @Operation(summary = "registra um cartao")
    @PostMapping
    public ResponseEntity<Cartao> save(@Valid @RequestBody Cartao cartaoDTO) {
        Cartao savedCartaoDTO = cartaoService.save(cartaoDTO);
        return new ResponseEntity<>(savedCartaoDTO, HttpStatus.CREATED);
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
