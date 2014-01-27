package br.com.ntxdev.zup;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import br.com.ntxdev.zup.domain.SolicitacaoListItem;
import br.com.ntxdev.zup.util.DateUtils;

public class SolicitacaoListItemAdapter {

	public static SolicitacaoListItem adapt(JSONObject json) throws Exception {
		SolicitacaoListItem item = new SolicitacaoListItem();
		item.setComentario(json.getString("description"));
		item.setData(DateUtils.getIntervaloTempo(DateUtils.parseRFC3339Date(json.getString("created_at"))));
		item.setEndereco(json.getString("address"));
		item.setFotos(new ArrayList<String>());
		JSONArray array = json.getJSONArray("images");
		for (int i = 0; i < array.length(); i++) {
			String[] parts = array.getJSONObject(i).getString("url").split("/");
			item.getFotos().add(parts[parts.length - 1]);
		}
		item.setTitulo(json.getJSONObject("category").getString("title"));
		item.setProtocolo(json.getString("protocol"));
		item.setStatus(new SolicitacaoListItem.Status(json.getJSONObject("status").getString("title"),
				json.getJSONObject("status").getString("color")));
		return item;
	}
}
