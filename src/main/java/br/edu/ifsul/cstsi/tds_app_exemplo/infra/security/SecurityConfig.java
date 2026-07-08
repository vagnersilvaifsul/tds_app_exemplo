package br.edu.ifsul.cstsi.tds_app_exemplo.infra.security;

import br.edu.ifsul.cstsi.tds_app_exemplo.infra.jwt.CustomJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration //adiciona ao Context do app
@EnableWebSecurity //habilita a segurança web
@EnableMethodSecurity(securedEnabled = true) //habilita a granularidade de segurança por método (nos controllers)
public class SecurityConfig {

    private final CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    //injeção de dependências
    public SecurityConfig(CustomJwtAuthenticationConverter customJwtAuthenticationConverter) {
        this.customJwtAuthenticationConverter = customJwtAuthenticationConverter;
    }

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

        PathPatternRequestMatcher.Builder path = PathPatternRequestMatcher.withDefaults();

        //Configuração JWT Authentication
        http
            .csrf(csrf -> csrf.disable()) //desabilita a proteção contra ataques Cross-site Request Forger, comum em conexões Stateless
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //sem sessão (desabilita o stateful)
            .authorizeHttpRequests(authorize -> {  //configurar a autorização
                authorize.requestMatchers(
                        path.matcher("/v3/api-docs/**"),
                        path.matcher("/swagger-ui.html"),
                        path.matcher("/swagger-ui/**")
                ).permitAll(); //exceto, a rota de documentação (para doc em html no navegador)
                authorize.requestMatchers(path.matcher(HttpMethod.POST, "/api/v1/autenticacao/login")).permitAll(); //exceto, a rota de login
                authorize.anyRequest().authenticated(); //demais rotas devem ser autenticadas
            })
            // Habilita o Resource Server nativo do Spring Security: ele registra o próprio filtro que lê o header Authorization,
            // valida o JWT (via o bean JwtDecoder, ver JwtConfig) e monta a Authentication (via o CustomJwtAuthenticationConverter, que busca o Cliente (usuário) no banco).
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter))
            );

        return http.build();
    }
}
