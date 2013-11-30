package br.com.ntxdev.zup.domain;

import android.graphics.Color;

public class SolicitacaoListItem {

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
}
