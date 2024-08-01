package br.com.fiap.postech.hackapay.cartao.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tb_cartao")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Cartao {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "cpf", nullable = false)
    private String cpf;
    @Column(name = "limite", nullable = false)
    private Integer limite;
    @Column(name = "numero", nullable = false)
    private String numero;
    @Column(name = "data_validade", nullable = false)
    @JsonProperty("data_validade")
    private String dataValidade;
    @Column(name = "cvv", nullable = false)
    private String cvv;

    public Cartao() {
        super();
    }

    public Cartao(String cpf, Integer limite, String numero, String dataValidade, String cvv) {
        this.cpf = cpf;
        this.limite = limite;
        this.numero = numero;
        this.dataValidade = dataValidade;
        this.cvv = cvv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cartao cartao)) return false;
        return Objects.equals(id, cartao.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Integer getLimite() {
        return limite;
    }

    public void setLimite(Integer limite) {
        this.limite = limite;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(String dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}
