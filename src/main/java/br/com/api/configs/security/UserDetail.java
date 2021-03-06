package br.com.api.configs.security;

import org.springframework.security.core.userdetails.User;

import br.com.api.models.Usuario;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserDetail extends User {

	private static final long serialVersionUID = 1L;
	
	private Usuario usuario;
	
	public UserDetail(Usuario usuario) {
		super(usuario.getEmail(), usuario.getSenha(), usuario.getListaPerfis());
		this.usuario = usuario;
	}
}
 