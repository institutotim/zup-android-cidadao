package br.com.lfdb.zup.service;

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

import br.com.lfdb.zup.domain.CategoriaInventario;
import br.com.lfdb.zup.domain.CategoriaRelato;

public class CategoriaRelatoService {

	public List<CategoriaRelato.Status> getStatus(Context context, long categoriaId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String raw = prefs.getString("reports", "");
		List<CategoriaRelato.Status> status = new ArrayList<CategoriaRelato.Status>();

		if (raw.isEmpty()) {
			return status;
		}
		
		try {
			JSONArray array = new JSONObject(raw).getJSONArray("categories");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				if (obj.getLong("id") == categoriaId) {
					JSONArray statuses = obj.getJSONArray("statuses");
					for (int j = 0; j < statuses.length(); j++) {						
						status.add(new CategoriaRelato.Status(statuses.getJSONObject(j).getLong("id"), 
								statuses.getJSONObject(j).getString("title"),
								statuses.getJSONObject(j).getString("color")));						
					}
				}
			}
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage(), e);
		}
		
		return status;
	}
	
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
							return new CategoriaRelato.Status(statuses.getJSONObject(j).getLong("id"), 
									statuses.getJSONObject(j).getString("title"),
									statuses.getJSONObject(j).getString("color"));
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage(), e);
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
                    JSONObject icon = obj.getJSONObject("icon").getJSONObject("default").getJSONObject("mobile");
                    String[] file = icon.getString("active").split("/");
                    categoria.setIconeAtivo(file[file.length - 1]);
                    file = icon.getString("disabled").split("/");
                    categoria.setIconeInativo(file[file.length - 1]);
					categoria.setId(obj.getLong("id"));
					file = obj.getJSONObject("marker").getJSONObject("default").getString("mobile").split("/");
					categoria.setMarcador(file[file.length - 1]);
					categoria.setNome(obj.getString("title"));
					categoria.setStatus(extrairStatus(obj.getJSONArray("statuses")));
                    categoria.setCategoriasInventario(extrairCategoriasInventario(context, obj.getJSONArray("inventory_categories")));
					return categoria;
				}
			}
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage(), e);
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
                JSONObject icon = obj.getJSONObject("icon").getJSONObject("default").getJSONObject("mobile");
                String[] file = icon.getString("active").split("/");
                categoria.setIconeAtivo(file[file.length - 1]);
                file = icon.getString("disabled").split("/");
                categoria.setIconeInativo(file[file.length - 1]);
				categoria.setId(obj.getLong("id"));
				file = obj.getJSONObject("marker").getJSONObject("default").getString("mobile").split("/");
				categoria.setMarcador(file[file.length - 1]);
				categoria.setNome(obj.getString("title"));
				categoria.setStatus(extrairStatus(obj.getJSONArray("statuses")));
                categoria.setCategoriasInventario(extrairCategoriasInventario(context, obj.getJSONArray("inventory_categories")));
				categorias.add(categoria);
			}
			return categorias;
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage(), e);
			return Collections.emptyList();
		}
	}

    private ArrayList<CategoriaInventario> extrairCategoriasInventario(Context context, JSONArray array) throws JSONException {
        ArrayList<CategoriaInventario> categorias = new ArrayList<CategoriaInventario>();

        CategoriaInventarioService service = new CategoriaInventarioService();
        for (int i = 0; i < array.length(); i++) {
            categorias.add(service.getById(context, array.getJSONObject(i).getLong("id")));
        }

        return categorias;
    }
	
	private ArrayList<CategoriaRelato.Status> extrairStatus(JSONArray lista) throws JSONException {
		ArrayList<CategoriaRelato.Status> status = new ArrayList<CategoriaRelato.Status>();
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
