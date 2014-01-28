package br.com.ntxdev.zup;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import br.com.ntxdev.zup.domain.CategoriaRelato;
import br.com.ntxdev.zup.domain.SolicitacaoListItem;
import br.com.ntxdev.zup.service.CategoriaRelatoService;
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
	
	public static SolicitacaoListItem adapt(Context context, JSONObject json) throws Exception {
		CategoriaRelatoService service = new CategoriaRelatoService();
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
		CategoriaRelato categoria = service.getById(context, json.getLong("category_id"));
		item.setTitulo(categoria.getNome());
		item.setProtocolo(json.getString("protocol"));
		CategoriaRelato.Status status = service.getStatusById(context, categoria.getId(), json.getLong("status_id"));
		item.setStatus(new SolicitacaoListItem.Status(status.getNome(), status.getCor()));
		return item;
	}
}
