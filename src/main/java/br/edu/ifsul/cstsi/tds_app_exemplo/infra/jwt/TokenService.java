package br.edu.ifsul.cstsi.tds_app_exemplo.infra.jwt;

import br.edu.ifsul.cstsi.tds_app_exemplo.cliente.Cliente;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/*
    Serviço de emissão do token JWT, usando a infraestrutura nativa do Spring Security (JwtEncoder).
 */

@Service //adiciona ao Context do app
public class TokenService {

    private static final String ISSUER = "API de exemplo da disciplina de TDS";

    private final JwtEncoder jwtEncoder;

    public TokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String geraToken(Cliente cliente) {
        Instant agora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(cliente.getUsername()) //email do Cliente
                .issuedAt(agora) //gerado em
                .expiresAt(agora.plus(Duration.ofHours(2))) //expira em
                .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        );

        return jwtEncoder.encode(parameters).getTokenValue();
    }
}
