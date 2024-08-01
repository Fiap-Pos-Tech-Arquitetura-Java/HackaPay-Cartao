package br.com.fiap.postech.hackapay.cartao.security;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "br.com.fiap.postech.hackapay.security")
public class SecurityConfiguration {

}
