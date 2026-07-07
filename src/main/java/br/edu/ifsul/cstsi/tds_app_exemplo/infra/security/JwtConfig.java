package br.edu.ifsul.cstsi.tds_app_exemplo.infra.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;

/*
    Configuração nativa do Spring Security (OAuth2 Resource Server) para emissão e validação de tokens JWT.
 */

@Configuration //adiciona ao Context do app
public class JwtConfig {

    @Value(value = "${api.security.token.secret}")
    private String secret;

    private SecretKeySpec secretKey() {
        return new SecretKeySpec(secret.getBytes(), "HmacSHA256");
    }

    @Bean //usado pelo TokenService para gerar (assinar) o token no login
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey()));
    }

    @Bean //usado automaticamente pelo Spring Security (via oauth2ResourceServer, veja sua adição no método filterChain da classe SecurityConfig) para validar o token de cada requisição
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(secretKey())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
