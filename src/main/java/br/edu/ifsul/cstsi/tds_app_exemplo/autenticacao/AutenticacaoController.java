package br.edu.ifsul.cstsi.tds_app_exemplo.autenticacao;

import br.edu.ifsul.cstsi.tds_app_exemplo.cliente.Cliente;
import br.edu.ifsul.cstsi.tds_app_exemplo.infra.security.TokenJwtDto;
import br.edu.ifsul.cstsi.tds_app_exemplo.infra.security.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController //adiciona ao Contexto do app como um REST Controller
@RequestMapping("api/v1/autenticacao") //Endpoint padrão da classe
public class AutenticacaoController {

    private final AuthenticationManager manager;
    private final TokenService tokenService;

    //injeção de dependências
    public AutenticacaoController(AuthenticationManager manager, TokenService tokenService) {
        this.manager = manager;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenJwtDto> efetuaLogin(@RequestBody UsuarioAutenticacaoDto data) {
        var authenticationDTO = new UsernamePasswordAuthenticationToken(data.email(), data.senha()); //converte o DTO em DTO do Spring Security

        var authentication = manager.authenticate(authenticationDTO); //autentica o usuário (esse objeto contém o usuário e a senha)
        var tokenJWT = tokenService.geraToken((Cliente) authentication.getPrincipal()); //gera o token JWT para enviar na response
        return ResponseEntity.ok(new TokenJwtDto(tokenJWT)); //envia a response com o token JWT
    }
}
