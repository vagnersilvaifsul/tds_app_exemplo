package br.edu.ifsul.cstsi.tds_app_exemplo.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration //adiciona ao Context do app
@EnableWebSecurity //habilita a segurança web
@EnableMethodSecurity(securedEnabled = true) //habilita a granularidade de segurança por método (nos controllers)
public class SecurityConfig {

    @Bean //injeta no Context do app o gerenciador de autenticação
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean //injeta no Context do app o esquema de encriptação
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean //personaliza as configurações da cadeia de filtros (lembre que o starter já pré-configurou)
    public SecurityFilterChain filterChain(HttpSecurity http) {

        //Basic Authentication
        http
            .csrf(csrf -> csrf.disable()) //desabilita a proteção contra ataques Cross-site Request Forger
            .authorizeHttpRequests(authorize -> {
                authorize.requestMatchers(HttpMethod.POST, "/api/v1/autenticacao/login").permitAll(); //exceto, a rota de login
                authorize.anyRequest().authenticated(); //demais rotas devem ser autenticadas
            })
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
