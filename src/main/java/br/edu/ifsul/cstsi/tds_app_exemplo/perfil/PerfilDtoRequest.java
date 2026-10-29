package br.edu.ifsul.cstsi.tds_app_exemplo.perfil;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/*
    DTO de entrada do perfil. O nome deve seguir o padrão do Spring Security para papéis: ROLE_<NOME>
    (é esse texto que o @Secured compara, ex.: @Secured({"ROLE_ADMIN"})).
 */
public record PerfilDtoRequest(
        @NotBlank(message = "O nome não pode ser nulo ou vazio")
        @Pattern(regexp = "^ROLE_[A-Z_]+$", message = "O nome deve seguir o padrão ROLE_NOME (ex.: ROLE_ADMIN)")
        String nome
) {
}
