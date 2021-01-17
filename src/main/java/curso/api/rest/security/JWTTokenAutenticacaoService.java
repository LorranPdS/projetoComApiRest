package curso.api.rest.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import curso.api.rest.ApplicationContextLoad;
import curso.api.rest.model.Usuario;
import curso.api.rest.repositoy.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Component
public class JWTTokenAutenticacaoService {
	
	
	/*Tem de validade do Token 2 dias*/
	private static final long EXPIRATION_TIME = 172800000;
	
	/*Uma senha unica para compor a autenticacao e ajudar na seguranÃ§a*/
	private static final String SECRET = "SenhaExtremamenteSecreta";
	
	/*Prefixo padrÃ£o de Token*/
	private static final String TOKEN_PREFIX = "Bearer";
	
	private static final String HEADER_STRING = "Authorization";
	
	/*Gerando token de autenticado e adiconando ao cabeÃ§alho e resposta Http*/
	public void addAuthentication(HttpServletResponse response , String username) throws IOException {
		
		/*Montagem do Token*/
		String JWT = Jwts.builder() /*Chama o gerador de Token*/
				        .setSubject(username) /*Adicona o usuario*/
				        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) /*Tempo de expiraÃ§Ã£o*/
				        .signWith(SignatureAlgorithm.HS512, SECRET).compact(); /*CompactaÃ§Ã£o e algoritmos de geraÃ§Ã£o de senha*/
		
		/*Junta token com o prefixo*/
		String token = TOKEN_PREFIX + " " + JWT; /*Bearer 87878we8we787w8e78w78e78w7e87w*/
		
		/*Adiciona no cabeçalho http*/
		response.addHeader(HEADER_STRING, token); /*Authorization: Bearer 87878we8we787w8e78w78e78w7e87w*/
		
		ApplicationContextLoad.getApplicationContext()
        .getBean(UsuarioRepository.class).atualizaTokenUser(JWT, username);
		
		
		// Liberando resposta para portas diferentes que usam a API ou caso clientes WEB
		liberacaoCors(response);
		
		// Escreve o token como resposta no corpo http
		response.getWriter().write("{\"Authorization\": \""+token+"\"}");
		
	}
	
	
	public Authentication getAuhentication(HttpServletRequest request,
			HttpServletResponse response) { // Esse response seria a resposta dada à requisição
		
		String token = request.getHeader(HEADER_STRING);
		
		try {
		// INÍCIO DA CONDIÇÃO DO TOKEN
		if (token != null) {
			
			String tokenLimpo = token.replace(TOKEN_PREFIX, "").trim();
			
			String user = Jwts.parser().setSigningKey(SECRET)
								.parseClaimsJws(tokenLimpo)
								.getBody().getSubject();
			if (user != null) {
				
				Usuario usuario = ApplicationContextLoad.getApplicationContext()
						        .getBean(UsuarioRepository.class).findUserByLogin(user);
				
				if (usuario != null) {
					
					if(tokenLimpo.equalsIgnoreCase(usuario.getToken())) {
						
					return new UsernamePasswordAuthenticationToken(
							usuario.getLogin(), 
							usuario.getSenha(),
							usuario.getAuthorities());
					
					}
				}
			}
			
		} // FIM DA CONDIÇÃO DO TOKEN
		}catch (io.jsonwebtoken.ExpiredJwtException e) {
			try {
				response.getOutputStream().println(
						"Seu Token está expirado! Faça o login ou informe"
						+ " um novo Token para autenticação");
			} catch (IOException e1) {
			}
		}
	
		liberacaoCors(response); // Esse método ainda não existe então clique na lâmpada
		return null; /*Não autorizado*/
	}

	// Vamos ter que criar 4 condições no método abaixo em parâmetros que darão problema
	private void liberacaoCors(HttpServletResponse response) {
		
		/*Aqui estamos liberando o cliente a acessar a resposta e a requisição dessa nossa API*/
		if(response.getHeader("Access-Control-Allow-Origin") == null) {
			response.addHeader("Access-Control-Allow-Origin", "*");
		}
		
		/*Vamos liberar a origem (origin) para esse parâmetro em azul*/
		if(response.getHeader("Access-Control-Allow-Headers") == null) {
			response.addHeader("Access-Control-Allow-Headers", "*");
		}
		
		// O professor descobriu tudo depois de ler muito a documentação
		if(response.getHeader("Access-Control-Request-Headers") == null) {
			response.addHeader("Access-Control-Request-Headers", "*");
		}
		
		if(response.getHeader("Access-Control-Allow-Methods") == null) {
			response.addHeader("Access-Control-Allow-Methods", "*");
		}
		
	}	

}