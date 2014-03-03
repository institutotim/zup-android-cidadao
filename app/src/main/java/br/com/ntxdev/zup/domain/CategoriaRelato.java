package br.com.ntxdev.zup.domain;

import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.Color;

public class CategoriaRelato implements Serializable {

	private static final long serialVersionUID = 1L;
	private long id;
	private String iconeAtivo;
    private String iconeInativo;
	private String marcador;
	private String nome;
    private boolean posicaoArbitraria;
    private ArrayList<CategoriaInventario> categoriasInventario;

	private ArrayList<Status> status;
	
	public CategoriaRelato() {
	}
	
	public CategoriaRelato(long id) {
		this.id = id;
	}

    public ArrayList<CategoriaInventario> getCategoriasInventario() {
        return categoriasInventario;
    }

    public void setCategoriasInventario(ArrayList<CategoriaInventario> categoriasInventario) {
        this.categoriasInventario = categoriasInventario;
    }

    public boolean isPosicaoArbitraria() {
        return posicaoArbitraria;
    }

    public void setPosicaoArbitraria(boolean posicaoArbitraria) {
        this.posicaoArbitraria = posicaoArbitraria;
    }

	public ArrayList<Status> getStatus() {
		return status;
	}

	public void setStatus(ArrayList<Status> status) {
		this.status = status;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

    public String getIconeAtivo() {
        return iconeAtivo;
    }

    public void setIconeAtivo(String iconeAtivo) {
        this.iconeAtivo = iconeAtivo;
    }

    public String getIconeInativo() {
        return iconeInativo;
    }

    public void setIconeInativo(String iconeInativo) {
        this.iconeInativo = iconeInativo;
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

	public static class Status implements Serializable {
		private long id;
		private int cor;
		private String nome;
		
		public Status() {
		}
		
		public Status(long id, String nome, String corHtml) {
			this.id = id;
			this.nome = nome;
			this.cor = Color.parseColor(corHtml);
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
			if (this.getClass() != obj.getClass())
				return false;
			Status other = (Status) obj;
			if (id != other.id)
				return false;
			return true;
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
		if (this.getClass() != obj.getClass())
			return false;
		CategoriaRelato other = (CategoriaRelato) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
