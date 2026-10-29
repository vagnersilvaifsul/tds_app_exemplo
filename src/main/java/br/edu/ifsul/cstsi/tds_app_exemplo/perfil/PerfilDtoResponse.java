package br.edu.ifsul.cstsi.tds_app_exemplo.perfil;

public record PerfilDtoResponse(Long id, String nome) {
    public PerfilDtoResponse(Perfil perfil) {
        this(perfil.getId(), perfil.getNome());
    }
}
