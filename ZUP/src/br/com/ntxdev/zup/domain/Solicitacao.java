package br.com.ntxdev.zup.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Solicitacao implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Tipo {
		BOCA_LOBO, COLETA_ENTULHO
	}

	private Tipo tipo;
	private float latitude;
	private float longitude;
	private List<String> fotos = new ArrayList<String>();

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public float getLatitude() {
		return latitude;
	}

	public float getLongitude() {
		return longitude;
	}
	
	public void setLatitudeLongitude(float latitude, float longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public List<String> getFotos() {
		return fotos;
	}
	
	public void adicionarFoto(String foto) {
		fotos.add(foto);
	}
}
