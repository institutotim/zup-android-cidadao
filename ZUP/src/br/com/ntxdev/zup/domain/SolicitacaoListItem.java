package br.com.ntxdev.zup.domain;

import java.io.Serializable;
import java.util.List;

import android.graphics.Color;

public class SolicitacaoListItem implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Status {
		EM_ABERTO(Color.rgb(0xff, 0x60, 0x49)),
		EM_ANDAMENTO(Color.rgb(0xff, 0xac, 0x2d)),
		RESOLVIDO(Color.rgb(0x78, 0xc9, 0x53)),
		NAO_RESOLVIDO(Color.rgb(0x99, 0x99, 0x99));
		
		private int color;

		Status(int color) {
			this.color = color;
		}
		
		public int getColor() {
			return color;
		}
	}
	
	private String protocolo;
	private String titulo;
	private String data;
	private Status status;
	private String comentario;
	private String endereco;
	
	private List<Integer> fotos;

	public String getProtocolo() {
		return protocolo;
	}

	public void setProtocolo(String protocolo) {
		this.protocolo = protocolo;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public List<Integer> getFotos() {
		return fotos;
	}

	public void setFotos(List<Integer> fotos) {
		this.fotos = fotos;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}
}
