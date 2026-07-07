package br.edu.ifsul.cstsi.tds_app_exemplo.autenticacao;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service //adiciona a classe ao Context do app como um serviço de autenticação (note que implementa UserDetailsService, o serviço de autenticação de usuários do spring boot)
public class AutenticacaoService implements UserDetailsService {
    private final AutenticacaoRepository rep;

    //injeção de dependência
    public AutenticacaoService(AutenticacaoRepository rep){
        this.rep = rep;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return rep.findByEmail(username);
    }
}
