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
	
	/*Uma senha unica para compor a autenticacao e ajudar na segurança*/
	private static final String SECRET = "SenhaExtremamenteSecreta";
	
	/*Prefixo padrão de Token*/
	private static final String TOKEN_PREFIX = "Bearer";
	
	private static final String HEADER_STRING = "Authorization";
	
	/*Gerando token de autenticado e adiconando ao cabeçalho e resposta Http*/
	public void addAuthentication(HttpServletResponse response , String username) throws IOException {
		
		/*Montagem do Token*/
		String JWT = Jwts.builder() /*Chama o gerador de Token*/
				        .setSubject(username) /*Adicona o usuario*/
				        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) /*Tempo de expiração*/
				        .signWith(SignatureAlgorithm.HS512, SECRET).compact(); /*Compactação e algoritmos de geração de senha*/
		
		/*Junta token com o prefixo*/
		String token = TOKEN_PREFIX + " " + JWT; /*Bearer 87878we8we787w8e78w78e78w7e87w*/
		
		/*Adiciona no cabe�alho http*/
		response.addHeader(HEADER_STRING, token); /*Authorization: Bearer 87878we8we787w8e78w78e78w7e87w*/
		
		ApplicationContextLoad.getApplicationContext()
        .getBean(UsuarioRepository.class).atualizaTokenUser(JWT, username);
		
		
		// Liberando resposta para portas diferentes que usam a API ou caso clientes WEB
		liberacaoCors(response);
		
		// Escreve o token como resposta no corpo http
		response.getWriter().write("{\"Authorization\": \""+token+"\"}");
		
	}
	
	
	public Authentication getAuhentication(HttpServletRequest request,
			HttpServletResponse response) { // Esse response seria a resposta dada � requisi��o
		
		String token = request.getHeader(HEADER_STRING);
		
		try {
		// IN�CIO DA CONDI��O DO TOKEN
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
			
		} // FIM DA CONDI��O DO TOKEN
		}catch (io.jsonwebtoken.ExpiredJwtException e) {
			try {
				response.getOutputStream().println(
						"Seu Token est� expirado! Fa�a o login ou informe"
						+ " um novo Token para autentica��o");
			} catch (IOException e1) {
			}
		}
	
		liberacaoCors(response); // Esse m�todo ainda n�o existe ent�o clique na l�mpada
		return null; /*N�o autorizado*/
	}

	// Vamos ter que criar 4 condi��es no m�todo abaixo em par�metros que dar�o problema
	private void liberacaoCors(HttpServletResponse response) {
		
		/*Aqui estamos liberando o cliente a acessar a resposta e a requisi��o dessa nossa API*/
		if(response.getHeader("Access-Control-Allow-Origin") == null) {
			response.addHeader("Access-Control-Allow-Origin", "*");
		}
		
		/*Vamos liberar a origem (origin) para esse par�metro em azul*/
		if(response.getHeader("Access-Control-Allow-Headers") == null) {
			response.addHeader("Access-Control-Allow-Headers", "*");
		}
		
		// O professor descobriu tudo depois de ler muito a documenta��o
		if(response.getHeader("Access-Control-Request-Headers") == null) {
			response.addHeader("Access-Control-Request-Headers", "*");
		}
		
		if(response.getHeader("Access-Control-Allow-Methods") == null) {
			response.addHeader("Access-Control-Allow-Methods", "*");
		}
		
	}	

}