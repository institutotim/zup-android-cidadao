package br.com.ntxdev.zup.domain;

public class Arvore extends Local {

	private static final long serialVersionUID = 1L;

	private String numero;
	private String endereco;
	private String condicao;
	private String localizacaoPasseio;
	private String interferenciaCopa;
	private String inclinacaoTronco;

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
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

	public String getLocalizacaoPasseio() {
		return localizacaoPasseio;
	}

	public void setLocalizacaoPasseio(String localizacaoPasseio) {
		this.localizacaoPasseio = localizacaoPasseio;
	}

	public String getInterferenciaCopa() {
		return interferenciaCopa;
	}

	public void setInterferenciaCopa(String interferenciaCopa) {
		this.interferenciaCopa = interferenciaCopa;
	}

	public String getInclinacaoTronco() {
		return inclinacaoTronco;
	}

	public void setInclinacaoTronco(String inclinacaoTronco) {
		this.inclinacaoTronco = inclinacaoTronco;
	}
}
