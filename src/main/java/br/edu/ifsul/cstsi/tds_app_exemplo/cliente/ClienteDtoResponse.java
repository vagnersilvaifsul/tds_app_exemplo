package br.edu.ifsul.cstsi.tds_app_exemplo.cliente;

import br.edu.ifsul.cstsi.tds_app_exemplo.perfil.Perfil;

import java.util.List;

/*
    DTO de saída do cliente. A senha (mesmo o hash) NUNCA sai da API.
 */
public record ClienteDtoResponse(
        Long id,
        String nome,
        String sobrenome,
        String email,
        boolean confirmado,
        Boolean situacao,
        List<String> perfis
) {
    public ClienteDtoResponse(Cliente cliente) {
        this(cliente.getId(), cliente.getNome(), cliente.getSobrenome(), cliente.getEmail(), cliente.isConfirmado(),
                cliente.getSituacao(), cliente.getPerfis().stream().map(Perfil::getNome).toList());
    }
}
