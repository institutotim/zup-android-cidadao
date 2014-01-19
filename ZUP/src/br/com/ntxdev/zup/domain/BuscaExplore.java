package br.com.ntxdev.zup.domain;

import java.io.Serializable;

public class BuscaExplore implements Serializable {

	private static final long serialVersionUID = 1L;

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
}
