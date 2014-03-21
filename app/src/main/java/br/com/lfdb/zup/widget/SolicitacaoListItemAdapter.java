package br.com.lfdb.zup.widget;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import br.com.lfdb.zup.domain.CategoriaRelato;
import br.com.lfdb.zup.domain.SolicitacaoListItem;
import br.com.lfdb.zup.service.CategoriaRelatoService;
import br.com.lfdb.zup.util.DateUtils;
import br.com.lfdb.zup.util.ViewUtils;

public class SolicitacaoListItemAdapter {

	public static SolicitacaoListItem adapt(Context context, JSONObject json) throws Exception {
		CategoriaRelatoService service = new CategoriaRelatoService();
		SolicitacaoListItem item = new SolicitacaoListItem();
		item.setComentario(json.getString("description"));
		item.setData(DateUtils.getIntervaloTempo(DateUtils.parseRFC3339Date(json.getString("created_at"))));
		item.setEndereco(json.getString("address"));
		item.setFotos(new ArrayList<String>());

        JSONArray array = json.getJSONArray("images");
        for (int j = 0; j < array.length(); j++) {
            item.getFotos().add(ViewUtils.isMdpiOrLdpi(context) ? array.getJSONObject(j).getString("low") : array.getJSONObject(j).getString("high"));
        }

        CategoriaRelato categoria;
        if (json.has("category_id")) {
		    categoria = service.getById(context, json.getLong("category_id"));
        } else {
            categoria = service.getById(context, json.getJSONObject("category").getLong("id"));
        }
		item.setTitulo(categoria.getNome());
        item.setCategoria(categoria);
		item.setProtocolo(json.getString("protocol"));

        if (json.has("status_id")) {
            CategoriaRelato.Status status = service.getStatusById(context, categoria.getId(), json.getLong("status_id"));
            item.setStatus(new SolicitacaoListItem.Status(status.getNome(), status.getCor()));
        } else {
            item.setStatus(new SolicitacaoListItem.Status(json.getJSONObject("status").getString("title"),
                    json.getJSONObject("status").getString("color")));
        }

        item.setLatitude(json.getJSONObject("position").getDouble("latitude"));
        item.setLongitude(json.getJSONObject("position").getDouble("longitude"));
		return item;
	}
}
