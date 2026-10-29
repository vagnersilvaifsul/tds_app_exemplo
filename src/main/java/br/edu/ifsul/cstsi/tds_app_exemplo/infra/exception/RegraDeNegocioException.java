package br.edu.ifsul.cstsi.tds_app_exemplo.infra.exception;

/*
    Exceção para violações de regras de negócio (ex.: e-mail já cadastrado, estoque insuficiente).
    É lançada pelos services e convertida em 409 Conflict pelo TratadorDeExcecoes.
 */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
