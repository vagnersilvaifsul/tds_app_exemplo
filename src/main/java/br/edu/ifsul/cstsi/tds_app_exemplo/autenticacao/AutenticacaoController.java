package br.edu.ifsul.cstsi.tds_app_exemplo.autenticacao;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController //adiciona a classe ao Contexto do app como um REST Controller
@RequestMapping("api/v1/autenticacao") //Endpoint padrão da classe
public class AutenticacaoController {

    private final AuthenticationManager manager;

    //injeção de dependências
    public AutenticacaoController(AuthenticationManager manager) {
        this.manager = manager;
    }

    @PostMapping("/login")
    public ResponseEntity<String> efetuaLogin(@RequestBody UsuarioAutenticacaoDTO data) {
        var authenticationDTO = new UsernamePasswordAuthenticationToken(data.email(), data.senha()); //converte o DTO em DTO do Spring Security

        var authentication = manager.authenticate(authenticationDTO); //autentica o usuário (chama implicitamente loadUserByUsername da service)
        return ResponseEntity.ok("autenticou " + authentication.getPrincipal());
    }
}
