package br.edu.ifsul.cstsi.tds_app_exemplo.cliente;

import br.edu.ifsul.cstsi.tds_app_exemplo.pedido.Pedido;
import br.edu.ifsul.cstsi.tds_app_exemplo.perfil.Perfil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "clientes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Cliente implements UserDetails { //UserDetails usuário padrão do Spring Boot
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String sobrenome;
    @Column(unique = true)
    private String email;
    private String senha;
    private boolean isConfirmado = false;
    private Boolean situacao; //true = ativo, false = inativo

    /*
        Associações e estratégias de carregamento (fetch):
          - LAZY (preguiçoso): a associação só é buscada no banco quando o código a acessa. É o padrão para coleções
            (@OneToMany e @ManyToMany) e deve ser a escolha na maioria dos casos.
          - EAGER (ansioso): a associação é buscada junto com a entidade, sempre.

        pedidos é LAZY: carregar um cliente (o que acontece em TODA requisição autenticada, ver
        CustomJwtAuthenticationConverter) não deve trazer todos os seus pedidos, e os itens de cada pedido, junto.
     */
    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Pedido> pedidos;

    /*
        perfis permanece EAGER: o Spring Security precisa dos perfis (getAuthorities) em toda requisição autenticada.

        Set (e não List): com List, o Hibernate cria a tabela de junção clientes_perfis SEM chave primária (uma List
        admite elementos repetidos). Como o data.sql roda a cada inicialização (spring.sql.init.mode=always), os
        INSERTs em clientes_perfis eram aceitos de novo a cada execução e duplicavam os registros (os das outras
        tabelas falham por chave primária repetida e são ignorados pelo continue-on-error).
        Com Set (que não admite repetidos), o Hibernate cria a chave primária composta (clientes_id, perfis_id).
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "clientes_perfis",
            joinColumns = @JoinColumn(name = "clientes_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "perfis_id", referencedColumnName = "id"))
    private Set<Perfil> perfis = new LinkedHashSet<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return perfis;
    }

    @Override
    public @Nullable String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    /*
        Usuário habilitado = cliente ativo (situacao = true). O Spring Security consulta este método no login
        (DaoAuthenticationProvider) e lança DisabledException para um cliente inativo, que o TratadorDeExcecoes
        converte em 401. O CustomJwtAuthenticationConverter faz a mesma verificação a cada requisição, para que um
        cliente desativado depois do login não continue usando o token que já tinha.
     */
    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(situacao);
    }

    //Método utilitário para gerar o Hash da senha
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("123"));
    }
}
