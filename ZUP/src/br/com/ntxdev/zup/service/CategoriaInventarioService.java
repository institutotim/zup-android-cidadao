package br.com.ntxdev.zup.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import br.com.ntxdev.zup.domain.CategoriaInventario;

public class CategoriaInventarioService {

	public CategoriaInventario getById(Context context, long id) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String raw = prefs.getString("inventory", "");
		if (raw.isEmpty()) {
			return null;
		}

		try {
			JSONArray array = new JSONObject(raw).getJSONArray("categories");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				if (obj.getLong("id") == id) {
					CategoriaInventario categoria = new CategoriaInventario();
					String[] file = obj.getJSONObject("icon").getString("url").split("/");
					categoria.setIcone(file[file.length - 1]);
					categoria.setId(obj.getLong("id"));
					file = obj.getJSONObject("marker").getString("url").split("/");
					categoria.setMarcador(file[file.length - 1]);
					categoria.setNome(obj.getString("title"));
					return categoria;
				}
			}
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage());
		}

		return null;
	}

	public List<CategoriaInventario> getCategorias(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String raw = prefs.getString("inventory", "");
		if (raw.isEmpty()) {
			return Collections.emptyList();
		}

		try {
			List<CategoriaInventario> categorias = new ArrayList<CategoriaInventario>();
			JSONArray array = new JSONObject(raw).getJSONArray("categories");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				CategoriaInventario categoria = new CategoriaInventario();
				String[] file = obj.getJSONObject("icon").getString("url").split("/");
				categoria.setIcone(file[file.length - 1]);
				categoria.setId(obj.getLong("id"));
				file = obj.getJSONObject("marker").getString("url").split("/");
				categoria.setMarcador(file[file.length - 1]);
				categoria.setNome(obj.getString("title"));
				categorias.add(categoria);
			}
			return categorias;
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage());
			return Collections.emptyList();
		}
	}
}
