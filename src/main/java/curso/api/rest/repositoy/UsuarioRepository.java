package curso.api.rest.repositoy;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import curso.api.rest.model.Usuario;

@Repository
public interface UsuarioRepository  extends CrudRepository<Usuario, Long>{
	
	@Query("select u from Usuario u where u.login = ?1")
	Usuario findUserByLogin(String login);
	
	@Transactional // Por ser uma transação, tem que ter essa anotação
	@Modifying // Como vamos fazer uma atualização no BD, coloque essa anotação
	@Query(nativeQuery = true, value = "update usuario set token = ?1 where login = ?2")
	void atualizaTokenUser(String token, String login);

}
