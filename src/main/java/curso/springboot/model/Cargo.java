package curso.springboot.model;

public enum Cargo {

	JUNIOR("Junior"), PLENO("Pleno"), SENIOR("Senior");

	private String nome;
	private String valor;

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getValor() {
		return valor = this.name();
	}

	private Cargo(String nome) {
		this.nome = nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getNome() {
		return nome;
	}
	
}
