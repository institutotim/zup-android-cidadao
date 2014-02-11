package br.com.ntxdev.zup.domain;

public class BocaDeLobo extends Local {

	private static final long serialVersionUID = 1L;
	private String id;
	private String endereco;
	private String condicao;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getCondicao() {
		return condicao;
	}

	public void setCondicao(String condicao) {
		this.condicao = condicao;
	}
}
