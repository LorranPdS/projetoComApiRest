package curso.api.rest.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import curso.api.rest.service.ImplementacaoUserDetailsSercice;

/*Mapeaia URL, enderecos, autoriza ou bloqueia acessoa a URL*/
@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private ImplementacaoUserDetailsSercice implementacaoUserDetailsSercice;
	
	
	/*Configura as solicitaÁıes de acesso por Http*/
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		/*Ativando a proteÁ„o contra usu·rios que n„o est„o validados por TOKEN*/
		http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
		
		/*Ativando a permiss√£o para acesso a p√°gina incial do sistema EX: sistema.com.br/index*/
		.disable().authorizeRequests().antMatchers("/").permitAll()
		.antMatchers("/index").permitAll()
		
		/*Estamos dizendo aqui que v·rias opÁıes para fazer leitura (GET, POST, PUT, ... 
		 * v·rias opÁıes de uso da API), v·rios clientes ser„o liberados para fazer isso, pois
		 * v·rios sistemas com portas diferentes e servidores diferentes tentando acessar a 
		 * nossa API, ele n„o vai liberar, ent„o temos que liberar essa parte*/
		.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
		
		
		/*URL de Logout - Redireciona ap√≥s o user deslogar do sistema*/
		.anyRequest().authenticated().and().logout().logoutSuccessUrl("/index")
		
		/*Maperia URL de Logout e insvalida o usu√°rio*/
		.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
		
		/*Filtra requisi√ß√µes de login para autentica√ß√£o*/
		.and().addFilterBefore(new JWTLoginFilter("/login", authenticationManager()), 
									UsernamePasswordAuthenticationFilter.class)
		
		/*Filtra demais requisi√ß√µes paa verificar a presen√ß√£o do TOKEN JWT no HEADER HTTP*/
		.addFilterBefore(new JwtApiAutenticacaoFilter(), UsernamePasswordAuthenticationFilter.class);
	
	}
	
	
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

	/*Service que ir√° consultar o usu√°rio no banco de dados*/	
	auth.userDetailsService(implementacaoUserDetailsSercice)
	
	/*Padr√£o de codigi√ß√£o de senha*/
	.passwordEncoder(new BCryptPasswordEncoder());
	
	}

}
