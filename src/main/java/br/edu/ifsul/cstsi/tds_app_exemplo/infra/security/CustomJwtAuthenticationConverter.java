package br.edu.ifsul.cstsi.tds_app_exemplo.infra.security;

import br.edu.ifsul.cstsi.tds_app_exemplo.autenticacao.AutenticacaoRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/*
    Converte o Jwt já validado (assinatura e expiração conferidas pelo JwtDecoder, ver JwtConfig) em uma
    Authentication do Spring Security.

    Por padrão, o OAuth2 Resource Server monta as authorities a partir de claims do próprio token (ex.:
    "scope"/"scp" viram authorities "SCOPE_x"). Este projeto usa outra estratégia: as permissões vêm do
    banco de dados (Cliente#getAuthorities(), que retorna os perfis do usuário).
 */
@Component //adiciona ao Context do app
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final AutenticacaoRepository repository;

    public CustomJwtAuthenticationConverter(AutenticacaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        var usuario = repository.findByEmail(jwt.getSubject());

        if (usuario == null) {
            throw new BadCredentialsException("Usuário do token JWT não encontrado");
        }
        return new UsernamePasswordAuthenticationToken(usuario, jwt, usuario.getAuthorities());
    }

}
