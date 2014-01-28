package br.com.ntxdev.zup.domain;

import java.util.List;

import android.graphics.Color;

public class CategoriaRelato {

	private long id;
	private String icone;
	private String marcador;
	private String nome;

	private List<Status> status;
	
	public CategoriaRelato() {
	}
	
	public CategoriaRelato(long id) {
		this.id = id;
	}

	public List<Status> getStatus() {
		return status;
	}

	public void setStatus(List<Status> status) {
		this.status = status;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getIcone() {
		return icone;
	}

	public void setIcone(String icone) {
		this.icone = icone;
	}

	public String getMarcador() {
		return marcador;
	}

	public void setMarcador(String marcador) {
		this.marcador = marcador;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public static class Status {
		private long id;
		private int cor;
		private String nome;
		
		public Status() {
		}
		
		public Status(String nome, String corHtml) {
			this.nome = nome;
			this.cor = Color.parseColor(corHtml);
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public int getCor() {
			return cor;
		}

		public void setCor(int cor) {
			this.cor = cor;
		}

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CategoriaRelato other = (CategoriaRelato) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
