package br.com.ntxdev.zup.domain;

import java.io.Serializable;
import java.util.List;

public abstract class Local implements Serializable {

	private static final long serialVersionUID = 1L;
	private String dataCadastro;
	private String bairro;
	private List<Integer> imagens;

	public String getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(String dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	public List<Integer> getImagens() {
		return imagens;
	}

	public void setImagens(List<Integer> imagens) {
		this.imagens = imagens;
	}
}
