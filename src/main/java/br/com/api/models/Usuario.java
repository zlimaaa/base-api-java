package br.com.api.models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;

import br.com.api.models.base.Pojo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Audited
@Table(name = "usuarios")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class Usuario extends Pojo<Long> {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_usuario")
	@SequenceGenerator(name = "seq_usuario", sequenceName = "seq_usuario", allocationSize = 1)
	@Column(name = "id")
	private Long id;

	@Column(name = "nome", nullable = false)
	private String nome;

	@Column(name = "login", nullable = false, unique = true)
	private String login;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "senha", nullable = false)
	private String senha;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "usuario_perfil", joinColumns = { @JoinColumn(name = "id_usuario") }, inverseJoinColumns = {
			@JoinColumn(name = "id_perfil") })
	private List<Perfil> listaPerfis;

	@Transient
	private String confirmacaoSenha; // Para cadastro e alteracao de senha

	@Transient
	private String novaSenha; // Para alteracao de senha

	/**
	 * Construtores
	 */

	public Usuario() {

	}

	public Usuario(String nome, String login, String email, String senha) {
		this.nome = nome;
		this.login = login;
		this.email = email;
		this.senha = senha;
	}
	
	public Usuario(Long id, String nome, String login, String email, String senha) {
		this.id = id;
		this.nome = nome;
		this.login = login;
		this.email = email;
		this.senha = senha;
	}

}
