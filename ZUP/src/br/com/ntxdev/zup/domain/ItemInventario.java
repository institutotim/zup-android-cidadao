package br.com.ntxdev.zup.domain;

import java.io.Serializable;

public class ItemInventario implements Serializable {

	private static final long serialVersionUID = 1L;
	private long id;
	private double latitude;
	private double longitude;
	private CategoriaInventario categoria;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public CategoriaInventario getCategoria() {
		return categoria;
	}

	public void setCategoria(CategoriaInventario categoria) {
		this.categoria = categoria;
	}
}
