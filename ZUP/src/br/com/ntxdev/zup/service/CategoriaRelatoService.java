package br.com.ntxdev.zup.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import br.com.ntxdev.zup.domain.CategoriaRelato;

public class CategoriaRelatoService {

	public CategoriaRelato.Status getStatusById(Context context, long categoriaId, long statusId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String raw = prefs.getString("reports", "");
		if (raw.isEmpty()) {
			return null;
		}

		try {
			JSONArray array = new JSONObject(raw).getJSONArray("categories");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				if (obj.getLong("id") == categoriaId) {
					JSONArray statuses = obj.getJSONArray("statuses");
					for (int j = 0; j < statuses.length(); j++) {
						if (statuses.getJSONObject(j).getLong("id") == statusId) {
							return new CategoriaRelato.Status(statuses.getJSONObject(j).getString("title"),
									statuses.getJSONObject(j).getString("color"));
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage());
		}

		return null;
	}
	
	public CategoriaRelato getById(Context context, long id) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String raw = prefs.getString("reports", "");
		if (raw.isEmpty()) {
			return null;
		}

		try {
			JSONArray array = new JSONObject(raw).getJSONArray("categories");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				if (obj.getLong("id") == id) {
					CategoriaRelato categoria = new CategoriaRelato();
					String[] file = obj.getJSONObject("icon").getString("url").split("/");
					categoria.setIcone(file[file.length - 1]);
					categoria.setId(obj.getLong("id"));
					file = obj.getJSONObject("marker").getString("url").split("/");
					categoria.setMarcador(file[file.length - 1]);
					categoria.setNome(obj.getString("title"));
					categoria.setStatus(extrairStatus(obj.getJSONArray("statuses")));
					return categoria;
				}
			}
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage());
		}

		return null;
	}
	
	public List<CategoriaRelato> getCategorias(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String raw = prefs.getString("reports", "");
		if (raw.isEmpty()) {
			return Collections.emptyList();
		}
		
		try {
			List<CategoriaRelato> categorias = new ArrayList<CategoriaRelato>();
			JSONArray array = new JSONObject(raw).getJSONArray("categories");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				CategoriaRelato categoria = new CategoriaRelato();
				String[] file = obj.getJSONObject("icon").getString("url").split("/");
				categoria.setIcone(file[file.length - 1]);
				categoria.setId(obj.getLong("id"));
				file = obj.getJSONObject("marker").getString("url").split("/");
				categoria.setMarcador(file[file.length - 1]);
				categoria.setNome(obj.getString("title"));
				categoria.setStatus(extrairStatus(obj.getJSONArray("statuses")));
				categorias.add(categoria);
			}
			return categorias;
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage());
			return Collections.emptyList();
		}
	}
	
	private List<CategoriaRelato.Status> extrairStatus(JSONArray lista) throws JSONException {
		List<CategoriaRelato.Status> status = new ArrayList<CategoriaRelato.Status>();
		for (int i = 0; i < lista.length(); i++) {
			JSONObject obj = lista.getJSONObject(i);
			CategoriaRelato.Status s = new CategoriaRelato.Status();
			s.setCor(Color.parseColor(obj.getString("color")));
			s.setId(obj.getLong("id"));
			s.setNome(obj.getString("title"));
			status.add(s);
		}
		return status;
	}
}
