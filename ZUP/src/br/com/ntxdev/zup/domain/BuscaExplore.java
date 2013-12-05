package br.com.ntxdev.zup.domain;

import java.io.Serializable;

public class BuscaExplore implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean limpezaBocaDeLobo;
	private boolean coletaDeEntulho;
	private Periodo periodo = Periodo.ULTIMOS_6_MESES;
	private Status status = Status.TODOS;
	private boolean exibirFlorestaUrbana;
	private boolean exibirPracasWifi;
	private boolean exibirBocasLobo;

	public enum Periodo {
		ULTIMOS_6_MESES, ULTIMOS_3_MESES, ULTIMO_MES, ULTIMA_SEMANA
	}

	public enum Status {
		TODOS, RESOLVIDOS, EM_ANDAMENTO, EM_ABERTO, NAO_RESOLVIDOS
	}

	public boolean isLimpezaBocaDeLobo() {
		return limpezaBocaDeLobo;
	}

	public void setLimpezaBocaDeLobo(boolean limpezaBocaDeLobo) {
		this.limpezaBocaDeLobo = limpezaBocaDeLobo;
	}

	public boolean isColetaDeEntulho() {
		return coletaDeEntulho;
	}

	public void setColetaDeEntulho(boolean coletaDeEntulho) {
		this.coletaDeEntulho = coletaDeEntulho;
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

	public boolean isExibirFlorestaUrbana() {
		return exibirFlorestaUrbana;
	}

	public void setExibirFlorestaUrbana(boolean exibirFlorestaUrbana) {
		this.exibirFlorestaUrbana = exibirFlorestaUrbana;
	}

	public boolean isExibirPracasWifi() {
		return exibirPracasWifi;
	}

	public void setExibirPracasWifi(boolean exibirPracasWifi) {
		this.exibirPracasWifi = exibirPracasWifi;
	}

	public boolean isExibirBocasLobo() {
		return exibirBocasLobo;
	}

	public void setExibirBocasLobo(boolean exibirBocasLobo) {
		this.exibirBocasLobo = exibirBocasLobo;
	}
}
