package br.com.ntxdev.zup.domain;

import java.io.Serializable;
import java.util.ArrayList;

public class BuscaExplore implements Serializable {

	private static final long serialVersionUID = 1L;

	private ArrayList<Long> idsCategoriaInventario = new ArrayList<Long>();
	private ArrayList<Long> idsCategoriaRelato = new ArrayList<Long>();
	private Periodo periodo = Periodo.ULTIMOS_6_MESES;
	private Status status = Status.TODOS;

	public enum Periodo {
		ULTIMOS_6_MESES, ULTIMOS_3_MESES, ULTIMO_MES, ULTIMA_SEMANA
	}

	public enum Status {
		TODOS, RESOLVIDOS, EM_ANDAMENTO, EM_ABERTO, NAO_RESOLVIDOS
	}

	public Periodo getPeriodo() {
		return periodo;
	}

	public void setPeriodo(Periodo periodo) {
		this.periodo = periodo;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public ArrayList<Long> getIdsCategoriaInventario() {
		return idsCategoriaInventario;
	}

	public void setIdsCategoriaInventario(ArrayList<Long> idsCategoriaInventario) {
		this.idsCategoriaInventario = idsCategoriaInventario;
	}

	public ArrayList<Long> getIdsCategoriaRelato() {
		return idsCategoriaRelato;
	}

	public void setIdsCategoriaRelato(ArrayList<Long> idsCategoriaRelato) {
		this.idsCategoriaRelato = idsCategoriaRelato;
	}
}
