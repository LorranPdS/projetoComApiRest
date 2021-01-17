package curso.api.rest;

public class ObjetoErro {

	// Essa ir� retornar a mensagem de erro
	private String error;

	// Essa vai retornar o c�digo do erro
	private String code;

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}

/*
 * Veja que s�o duas coisas que precisamos retornar para facilitar o
 * entendimento do que est� acontecendo, que � a mensagem de erro e o c�digo:
 * s�o as duas coisas que o programador precisa saber para solucionar algum
 * problema
 */