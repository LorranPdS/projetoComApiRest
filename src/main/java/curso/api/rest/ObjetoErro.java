package curso.api.rest;

public class ObjetoErro {

	// Essa irá retornar a mensagem de erro
	private String error;

	// Essa vai retornar o código do erro
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
 * Veja que são duas coisas que precisamos retornar para facilitar o
 * entendimento do que está acontecendo, que é a mensagem de erro e o código:
 * são as duas coisas que o programador precisa saber para solucionar algum
 * problema
 */