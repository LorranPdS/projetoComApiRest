package curso.api.rest;

import java.sql.SQLException;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@ControllerAdvice
public class ControleExcecoes extends ResponseEntityExceptionHandler {

	/*Esse m�todo interceptar� erros mais comuns no projeto*/
	@ExceptionHandler({Exception.class, RuntimeException.class, Throwable.class})
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		String msg = "";
		
		if(ex instanceof MethodArgumentNotValidException) {
			
			List<ObjectError> list = 
					((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors();
			
			for (ObjectError objectError : list) {
				msg += objectError.getDefaultMessage() + "\n";
			}
			
		} else {
			msg = ex.getMessage();
		}
		
		
		// Esse objetoErro vai virar um JSON para ser mostrado na tela para gente
		ObjetoErro objetoErro = new ObjetoErro();
		objetoErro.setError(msg);
		objetoErro.setCode(status.value() + " ==> " + status.getReasonPhrase());
		
		return new ResponseEntity<>(objetoErro, headers, status);
	}
	
	/*Essa classe ir� mapear erros que venham a n�vel de BD, como um INSERT, DELETE ou montar uma QUERY,
	 * ent�o vamos mapear erros na parte de SQL*/
	@ExceptionHandler({DataIntegrityViolationException.class, ConstraintViolationException.class,
		PSQLException.class, SQLException.class})
	protected ResponseEntity<Object> handleExceptionDataIntegrity(Exception ex){
		
		String msg = "";
		
		// O professor descobriu que tinha que ser assim debugando a exce��o
		if(ex instanceof DataIntegrityViolationException) {
			msg = ((DataIntegrityViolationException)ex).getCause().getCause().getMessage();
		}
		
		else if(ex instanceof ConstraintViolationException) {
			msg = ((ConstraintViolationException)ex).getCause().getCause().getMessage();
		}
		
		else if(ex instanceof PSQLException) {
			msg = ((PSQLException)ex).getCause().getCause().getMessage();
		}
		
		else if(ex instanceof SQLException) {
			msg = ((SQLException)ex).getCause().getCause().getMessage();
		}
		
		else {
			msg = ex.getMessage(); // Mensagens de erro mais gen�ricas aparecer�o aqui
		}		
		
		/*Nesse setCode, quando acontece esse erro que s�o mandadas pelas exce��es descritas na anota��o
		 * dessa classe, � sempre um erro interno do servidor, e erro interno do servidor � sempre
		 * respondido como INTERNAL_SERVER_ERROR, e geralmente � o 500, e o getReasonPhrase d� uma 
		 * descri��o mais leg�vel do erro*/
		ObjetoErro objetoErro = new ObjetoErro();
		objetoErro.setError(msg);
		objetoErro.setCode(HttpStatus.INTERNAL_SERVER_ERROR + " ==> " 
				+ HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		
		return new ResponseEntity<>(objetoErro, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
}

/*1) � a anota��o ExceptionHandler quem ir� ativar o m�todo handleExceptionDataIntegrity para interceptar
 * esses erros, e dentro dessa anota��o, eu irei passar quais s�o os tipos de erro que eu quero interceptar.
 * Essa anota��o vai tratar a maioria dos erros a nivel de BD
 * 
 * 2) DataIntegrityViolationException - � uma das exce��es mais normais, que � quando tentamos deletar
 * um registro, mas tem uma chave dependendo dela
 * 
 * 3) O PSQLException � espec�fico para erros de SQL no PostgreSQL*/

/* Vai acontecer sim de ter erros que n�o podem estar sendo interceptados, da� voc� vai ter que estar
 * ajustando at� o projeto ficar todo certo */
