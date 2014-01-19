package br.com.ntxdev.zup;

import org.json.JSONObject;

import br.com.ntxdev.zup.domain.SolicitacaoListItem;
import br.com.ntxdev.zup.util.DateUtils;

public class SolicitacaoListItemAdapter {

	public static SolicitacaoListItem adapt(JSONObject json) throws Exception {
		SolicitacaoListItem item = new SolicitacaoListItem();
		item.setComentario(json.getString("description"));
		item.setData(DateUtils.getIntervaloTempo(DateUtils.parseRFC3339Date(json.getString("created_at"))));
		//item.setTitulo(titulo);
		return item;
	}
}
