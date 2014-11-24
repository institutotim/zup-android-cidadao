package br.com.lfdb.zup.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import br.com.lfdb.zup.domain.CategoriaInventario;
import br.com.lfdb.zup.util.ImageUtils;

public class CategoriaInventarioService {

	public CategoriaInventario getById(Context context, long id) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String raw = prefs.getString("inventory", "");
		if (raw.isEmpty()) {
			return null;
		}

        String density = ImageUtils.shouldDownloadRetinaIcon(context) ? "retina" : "default";
		try {
			JSONArray array = new JSONObject(raw).getJSONArray("categories");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				if (obj.getLong("id") == id) {
					CategoriaInventario categoria = new CategoriaInventario();
                    JSONObject icon = obj.getJSONObject("icon").getJSONObject(density).getJSONObject("mobile");
                    String[] file = icon.getString("active").split("/");
                    categoria.setIconeAtivo(file[file.length - 1]);
                    file = icon.getString("disabled").split("/");
                    categoria.setIconeInativo(file[file.length - 1]);
					categoria.setId(obj.getLong("id"));
					file = obj.getJSONObject("marker").getJSONObject(density).getString("mobile").split("/");
					categoria.setMarcador(file[file.length - 1]);
					categoria.setNome(obj.getString("title"));
					return categoria;
				}
			}
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage(), e);
		}

		return null;
	}

	public List<CategoriaInventario> getCategorias(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String raw = prefs.getString("inventory", "");
		if (raw.isEmpty()) {
			return Collections.emptyList();
		}

        String density = ImageUtils.shouldDownloadRetinaIcon(context) ? "retina" : "default";
		try {
			List<CategoriaInventario> categorias = new ArrayList<>();
			JSONArray array = new JSONObject(raw).getJSONArray("categories");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				CategoriaInventario categoria = new CategoriaInventario();
                JSONObject icon = obj.getJSONObject("icon").getJSONObject(density).getJSONObject("mobile");
				String[] file = icon.getString("active").split("/");
				categoria.setIconeAtivo(file[file.length - 1]);
                file = icon.getString("disabled").split("/");
                categoria.setIconeInativo(file[file.length - 1]);
				categoria.setId(obj.getLong("id"));
				file = obj.getJSONObject("marker").getJSONObject(density).getString("mobile").split("/");
				categoria.setMarcador(file[file.length - 1]);
				categoria.setNome(obj.getString("title"));
				categorias.add(categoria);
			}
			return categorias;
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage(), e);
			return Collections.emptyList();
		}
	}
}
