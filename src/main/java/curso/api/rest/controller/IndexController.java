package curso.api.rest.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import curso.api.rest.model.Usuario;
import curso.api.rest.model.UsuarioDTO;
import curso.api.rest.repositoy.UsuarioRepository;

@RestController
@RequestMapping(value = "/usuario")
public class IndexController {
	
	@Autowired
	private UsuarioRepository usuarioRepository;
		
	@GetMapping(value = "/{id}/codigovenda/{venda}", produces = "application/json")
	public ResponseEntity<Usuario> relatorio(@PathVariable (value = "id") Long id
			                                , @PathVariable (value = "venda") Long venda) {
		
		Optional<Usuario> usuario = usuarioRepository.findById(id);
		
		/*o retorno seria um relatorio*/
		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
	}
	
	@GetMapping(value = "/{id}", produces = "application/json", headers = "X-API-Version=v1")
	@CacheEvict(value = "cacheuser", allEntries = true) // Apaga os caches não utilizados
	@CachePut("cacheuser") // Atualiza os caches necessários
	public ResponseEntity<UsuarioDTO> initV1(@PathVariable (value = "id") Long id) {
		
		// Aqui a pesquisa tem que ser do Usuario mesmo, mas o retorno vai ser UsuarioDTO
		Optional<Usuario> usuario = usuarioRepository.findById(id);
		
		return new ResponseEntity<UsuarioDTO>(new UsuarioDTO(usuario.get()), HttpStatus.OK);
	}
	
	@GetMapping(value = "/{id}", produces = "application/json", headers = "X-API-Version=v2")
	public ResponseEntity<Usuario> initV2(@PathVariable (value = "id") Long id) {
		
		Optional<Usuario> usuario = usuarioRepository.findById(id);
		
		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
	}
	
	@DeleteMapping(value = "/{id}", produces = "application/text")
	public String delete (@PathVariable("id") Long id){
		
		usuarioRepository.deleteById(id);
		
		return "ok";
	}
	
	
	@DeleteMapping(value = "/{id}/venda", produces = "application/text")
	public String deletevenda(@PathVariable("id") Long id){
		
		usuarioRepository.deleteById(id);
		
		return "ok";
	}
	
	/*1) O CacheEvict vai apagar os caches que não são mais usados. O value é o nome dele e
	 * o allEntries vai apagar caches não usados e atualizar os que mudarem*
	 2) O CachePut esse vai identificar que vai ter atualizações e vai colocar no cache
	 (por exemplo, se pegarmos e cadastrarmos novos usuários dentro do BD e tentarmos carregar
	 por esse endpoint, ele irá carregar somente o cache para gente, então ficaremos tentando
	 cadastrar usuários no BD e o cache não vai trazer porque ele não está atualizando
	 isso para gente, então usaremos o CachePut)*/
	@GetMapping(value = "/", produces = "application/json")
	@CacheEvict(value = "cacheusuarios", allEntries = true)
	@CachePut("cacheusuarios")
	public ResponseEntity<List<Usuario>> usuario () {
		
		List<Usuario> list = (List<Usuario>) usuarioRepository.findAll();
				
		return new ResponseEntity<List<Usuario>>(list, HttpStatus.OK);
	}
	
	
	@PostMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario usuario) 
			throws Exception { // Exceção caso informe o cep errado
		
		for (int pos = 0; pos < usuario.getTelefones().size(); pos ++) {
			usuario.getTelefones().get(pos).setUsuario(usuario);
		}
		
		/*Consumindo API pública externa*/
		
		// O URL pertence ao pacote java.net
		URL url = new URL("https://viacep.com.br/ws/"+usuario.getCep()+"/json/");
		URLConnection connection = url.openConnection(); // Abre a conexão para a WebService
		InputStream is = connection.getInputStream(); // Ficam com os dados da requisição c/ o cep passado
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8")); // Preparando a leitura
		
		String cep = "";
		StringBuilder jsonCep = new StringBuilder();
		
		while((cep = br.readLine()) != null) {
			jsonCep.append(cep);
		}
		
		/*Esse Gson vai pegar esse String jsonCep.toString, pegar todos os atributos (cep,
		 * logradouro, localidade e uf) e converter os dados que ele encontrar dentro do
		 * objeto Usuario, pegar no Json e colocar para gente*/
		Usuario userAux = new Gson().fromJson(jsonCep.toString(), Usuario.class);
		
		usuario.setCep(userAux.getCep());
		usuario.setLogradouro(userAux.getLogradouro());
		usuario.setLocalidade(userAux.getLocalidade());
		usuario.setUf(userAux.getUf());
		
		
		/*Consumindo API pública externa*/
		
		String senhacriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
		usuario.setSenha(senhacriptografada);
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
		
	}
	
	
	@PutMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> atualizar(@RequestBody Usuario usuario) {
		
		/*outras rotinas antes de atualizar*/
		
		for (int pos = 0; pos < usuario.getTelefones().size(); pos ++) {
			usuario.getTelefones().get(pos).setUsuario(usuario);
		}
		
		Usuario userTemporario = usuarioRepository.findUserByLogin(usuario.getLogin());
		
		
		if (!userTemporario.getSenha().equals(usuario.getSenha())) { /*Senhas diferentes*/
			String senhacriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
			usuario.setSenha(senhacriptografada);
		}
		
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
		
	}
	
	
	
	@PutMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
	public ResponseEntity updateVenda(@PathVariable Long iduser, 
			                                     @PathVariable Long idvenda) {
		/*outras rotinas antes de atualizar*/
		
		//Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity("Venda atualzada", HttpStatus.OK);
		
	}
	
	
	@PostMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
	public ResponseEntity cadastrarvenda(@PathVariable Long iduser, 
			                                     @PathVariable Long idvenda) {
		
		/*Aqui seria o processo de venda*/
		//Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity("id user :" + iduser + " idvenda :"+ idvenda, HttpStatus.OK);
		
	}
	
	
	

}
