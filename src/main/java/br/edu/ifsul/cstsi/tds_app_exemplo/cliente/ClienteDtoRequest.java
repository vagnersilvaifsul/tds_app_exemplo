package br.edu.ifsul.cstsi.tds_app_exemplo.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/*
    DTO de entrada do cliente (POST e PUT).
      - senha: obrigatória no POST; no PUT, se não vier, a senha atual é mantida (regra no ClienteService).
      - perfis: nomes dos perfis (ex.: ["ROLE_USER"]); se não vierem, o cliente recebe ROLE_USER.
      - situacao: se não vier, o cliente nasce ativo (POST) ou mantém a atual (PUT).
 */
public record ClienteDtoRequest(
        @NotBlank(message = "O nome não pode ser nulo ou vazio")
        @Size(min = 2, max = 50, message = "O nome deve ter entre 2 e 50 caracteres")
        String nome,
        @NotBlank(message = "O sobrenome não pode ser nulo ou vazio")
        @Size(min = 2, max = 100, message = "O sobrenome deve ter entre 2 e 100 caracteres")
        String sobrenome,
        @NotBlank(message = "O e-mail não pode ser nulo ou vazio")
        @Email(message = "O e-mail deve ser válido")
        String email,
        @Pattern(regexp = "^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[$*&@#])[0-9a-zA-Z$*&@#]{8,}$", message = "A senha deve conter ao menos uma letra maiúscula, uma letra minúscula, um numeral, um caractere especial e um total de 8 caracteres.")
        String senha,
        List<String> perfis,
        Boolean situacao
) {
}
