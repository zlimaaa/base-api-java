package com.projeto.backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.projeto.backend.constants.ExceptionsConstantes;
import com.projeto.backend.constants.ValidacaoConstantes;
import com.projeto.backend.converter.DozerConverter;
import com.projeto.backend.dtos.UsuarioDTO;
import com.projeto.backend.enums.PerfilEnum;
import com.projeto.backend.exceptions.CustomException;
import com.projeto.backend.exceptions.ValidationException;
import com.projeto.backend.models.Perfil;
import com.projeto.backend.models.Usuario;
import com.projeto.backend.repositories.UsuarioRepository;
import com.projeto.backend.services.base.ServiceGenerico;
import com.projeto.backend.utils.Utils;

@Service
public class UsuarioService extends ServiceGenerico<Usuario, UsuarioDTO, Long, UsuarioRepository> {

	@Autowired
	private UsuarioRepository repository;

	@Autowired
	private PerfilService perfilService;

	@Override
	public UsuarioRepository getRepositorio() {
		return repository;
	}

	@Override
	public List<UsuarioDTO> consultarTodos() {
		return this.converterListaEntidadeParaListaDTO(this.repository.findByAtivo(true));
	}

	public UsuarioDTO consultar(Long id) {
		Optional<Usuario> usuario = this.consultarPorId(id);

		if (usuario.isPresent()) {
			return converterEntidadeParaDTO(usuario.get());
		}

		throw new EntityNotFoundException(ExceptionsConstantes.USUARIO_NAO_ENCONTRADO);

	}

	public UsuarioDTO cadastro(UsuarioDTO usuarioDTO) {
		Usuario usuario = converterDTOParaEntidade(usuarioDTO);
		usuario = this.salvar(usuario, null);
		return converterEntidadeParaDTO(usuario);
	}

	@Override
	protected void resolverPreDependencias(Usuario entidade) throws CustomException {
		this.vincularPerfisAoUsuario(entidade);
	}

	private void vincularPerfisAoUsuario(Usuario usuario) {

		// Em caso de cadastro de usuario, o mesmo inicia com perfil de usuario
		if (usuario.getListaPerfis() == null || usuario.getListaPerfis().isEmpty()) {
			List<Perfil> perfis = new ArrayList<>();
			Perfil perfil = new Perfil(PerfilEnum.USUARIO);
			perfis.add(perfil);
			usuario.setListaPerfis(perfis);
		}

		List<Perfil> perfis = new ArrayList<>();

		for (Perfil perfil : usuario.getListaPerfis()) {
			perfis.add(this.perfilService.consultarOuCadastrarPerfilPeloNome(perfil));
		}

		usuario.setListaPerfis(perfis);
	}

	@Override
	protected void validarInclusao(Usuario entidade) throws CustomException {
		this.validarUnicidade(entidade);

		if (!entidade.getSenha().equals(entidade.getConfirmacaoSenha())) {
			throw new ValidationException(ValidacaoConstantes.SENHA_E_CONFIRMACAO_SENHA_DIFERENTES);
		}
		
		String hashSenha = Utils.gerarHashSenha(entidade.getSenha());
		entidade.setSenha(hashSenha);
	}

	@Override
	protected void validarAlteracao(Usuario entidade) throws CustomException {
		// TODO fazer as validações de alteracao
		this.validarUnicidade(entidade);
	}

	@Override
	protected void validarUnicidade(Usuario entidade) throws CustomException {
		Long idUsuario = entidade.getId() == null ? 0l : entidade.getId();
		Long count = repository.countByEmailAndAtivoAndIdNot(entidade.getEmail(), true, idUsuario);

		if (count > 0l) {
			throw new CustomException(ExceptionsConstantes.EMAIL_JA_CADASTRADO);
		}
	}

	@Override
	public Usuario converterDTOParaEntidade(UsuarioDTO entidadeDTO) {
		return DozerConverter.converterObjeto(entidadeDTO, Usuario.class);
	}

	@Override
	public UsuarioDTO converterEntidadeParaDTO(Usuario entidade) {
		return DozerConverter.converterObjeto(entidade, UsuarioDTO.class);
	}

	@Override
	protected List<UsuarioDTO> converterListaEntidadeParaListaDTO(List<Usuario> listaEntidades) throws CustomException {
		return DozerConverter.converterListaObjetos(listaEntidades, UsuarioDTO.class);
	}
	
}
