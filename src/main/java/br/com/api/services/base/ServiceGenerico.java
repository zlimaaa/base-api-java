package br.com.api.services.base;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import br.com.api.constants.ExceptionsConstantes;
import br.com.api.exceptions.CustomException;
import br.com.api.models.Usuario;
import br.com.api.models.base.Pojo;
import br.com.api.utils.ValidacaoUtils;

public abstract class ServiceGenerico<ENTIDADE extends Pojo<ID>, ENTIDADEDTO,  ID extends Serializable, REPOSITORIO extends JpaRepository<ENTIDADE, ID>> {

	public abstract REPOSITORIO getRepositorio();

	@Transactional(readOnly = true)
	public Optional<ENTIDADE> consultarPorId(ID id) {
		return this.getRepositorio().findById(id);
	}

	@Transactional(readOnly = true)
	protected List<ENTIDADEDTO> consultarTodos() throws CustomException {
		return null;
	}

	// Exclusão fisíca
	@Transactional(rollbackFor = Exception.class)
	public void deletar(ID id, Usuario usuario) throws CustomException {
		ENTIDADE entidade = this.consultarPorId(id).get();
		entidade.setUsuario(ValidacaoUtils.isUsuarioValido(usuario) ? usuario : null);
		this.validarExclusao(entidade);
		this.resolverPreExclusao(entidade);
		this.getRepositorio().deleteById(entidade.getId());
	}
	
	// Exclusão fisíca
	@Transactional(rollbackFor = Exception.class)
	public void deletarSemValidacao(ID id) { 
		this.getRepositorio().deleteById(id);
	}

	// Exclusão lógica
	@Transactional(rollbackFor = Exception.class)
	public void excluir(ID id, Usuario usuario) throws CustomException {
		ENTIDADE entidade = this.consultarPorId(id).get();
		entidade.setUsuario(ValidacaoUtils.isUsuarioValido(usuario) ? usuario : null);
		this.validarExclusao(entidade);
		this.resolverPreExclusao(entidade);
		entidade.setAtivo(false);
		entidade.setDataExclusao(LocalDateTime.now());		
		this.salvarEntidade(entidade);
		this.resolverPosExclusao(entidade);
	}
	
	// Exclusão lógica
	@Transactional(rollbackFor = Exception.class)
	public void excluirSemValidacao(ID id, Usuario usuario) throws CustomException { 
		ENTIDADE entidade = this.consultarPorId(id).get();
		entidade.setUsuario(ValidacaoUtils.isUsuarioValido(usuario) ? usuario : null);
		entidade.setAtivo(false);
		entidade.setDataExclusao(LocalDateTime.now());
		
		this.salvarEntidade(entidade);
	}
	
	// Incluir um novo registro ou Alterar um existente
	@Transactional(rollbackFor = Exception.class)
	public ENTIDADE salvar(ENTIDADE entidade, Usuario usuario) throws CustomException {
		entidade.setUsuario(ValidacaoUtils.isUsuarioValido(usuario) ? usuario : null);
		if(entidade.getId() == null) {
			this.validarInclusao(entidade);
			entidade.setDataInclusao(LocalDateTime.now());
		}else{
			this.validarAlteracao(entidade);
			Optional<ENTIDADE> entidadeOld = this.consultarPorId(entidade.getId());
			
			entidadeOld.orElseThrow(() -> new EntityNotFoundException(ExceptionsConstantes.ENTIDADE_NAO_ENCONTRADA));
			
			entidade.setDataInclusao(entidadeOld.get().getDataInclusao());
		}
		this.validarUnicidade(entidade);
		this.resolverPreDependencias(entidade);		
		entidade.setAtivo(true);
		ENTIDADE entidadeBanco = this.salvarEntidade(entidade);
		entidade.setId(entidadeBanco.getId());
		this.resolverPosDependencias(entidade);

		return entidade;
	}
	
	// Método principal pra salvar inclusão, alteração ou exclusão lógica
	@Transactional(rollbackFor = Exception.class)
	protected ENTIDADE salvarEntidade(ENTIDADE entidade) throws CustomException {
		entidade.setDataAlteracao(LocalDateTime.now());		
		
		ENTIDADE pojoBanco = null;
		try {
			pojoBanco = this.getRepositorio().save(entidade);
		} catch (ObjectOptimisticLockingFailureException e) {
			e.printStackTrace();
			throw new CustomException(ExceptionsConstantes.ENTIDADE_JA_ALTERADA);
		}
		return pojoBanco;
	}
		
	protected void resolverPreDependencias(ENTIDADE entidade) throws CustomException {}

	protected void resolverPosDependencias(ENTIDADE entidade) throws CustomException {}

	protected void resolverPreExclusao(ENTIDADE entidade) throws CustomException {}

	protected void resolverPosExclusao(ENTIDADE entidade) throws CustomException {}

	protected void validarAlteracao(ENTIDADE entidade) throws CustomException {}
	
	protected ENTIDADE validarCamposAlterados(ENTIDADE entidade) throws CustomException { return null; }

	protected void validarExclusao(ENTIDADE entidade) throws CustomException {}

	protected void validarInclusao(ENTIDADE entidade) throws CustomException {}

	protected void validarUnicidade(ENTIDADE entidade) throws CustomException {}
	
	// Utilizado para conversão do input em model e do model em response
	public abstract ENTIDADE converterDTOParaEntidade(ENTIDADEDTO entidadeDTO);

	public abstract ENTIDADEDTO converterEntidadeParaDTO(ENTIDADE entidade);

	protected List<ENTIDADE> converterListaDTOParaListaEntidade(List<ENTIDADEDTO> listaEntidadesDTO) throws CustomException { return null; }

	protected List<ENTIDADEDTO> converterListaEntidadeParaListaDTO(List<ENTIDADE> listaEntidades) throws CustomException { return null; }
	// --
}

