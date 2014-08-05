package br.com.lfdb.zup.domain;

import java.util.Calendar;

import br.com.lfdb.zup.util.DateUtils;

public enum Periodo {
	ULTIMOS_6_MESES, ULTIMOS_3_MESES, ULTIMO_MES, ULTIMA_SEMANA;

	public String getDateString() {
		Calendar cal = Calendar.getInstance();

		switch (this) {
		case ULTIMA_SEMANA:
			cal.add(Calendar.WEEK_OF_YEAR, -1);
			break;
		case ULTIMO_MES:
			cal.add(Calendar.MONTH, -1);
			break;
		case ULTIMOS_3_MESES:
			cal.add(Calendar.MONTH, -3);
			break;
		case ULTIMOS_6_MESES:
			cal.add(Calendar.MONTH, -6);
			break;
		}

		return DateUtils.getIsoString(cal.getTime());
	}
}
