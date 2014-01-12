package br.com.ntxdev.zup.service;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import br.com.ntxdev.zup.domain.Usuario;

public class UsuarioService {
	
	public Usuario getUsuarioAtivo(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			return extrairDoJSON(new JSONObject(prefs.getString("raw_user", "")));
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage());
			return null;
		}
	}

	public Usuario extrairDoJSON(JSONObject j) throws JSONException {
		Usuario u = new Usuario();
		u.setBairro(j.optString("district"));
		u.setCep(j.optString("postal_code"));
		u.setComplemento(j.optString("address_additional"));
		u.setCpf(j.optString("document"));
		u.setEmail(j.getString("email"));
		u.setEndereco(j.optString("address"));
		u.setNome(j.getString("name"));
		u.setTelefone(j.getString("phone"));
		
		return u;
	}
	
	public JSONObject converterParaJSON(Usuario usuario) throws JSONException {
		JSONObject json = new JSONObject();
		
		json.put("district", usuario.getBairro());
		json.put("postal_code", usuario.getCep());
		json.put("address_additional", usuario.getComplemento());
		json.put("document", usuario.getCpf());
		json.put("email", usuario.getEmail());
		json.put("address", usuario.getEndereco());
		json.put("name", usuario.getNome());
		json.put("phone", usuario.getTelefone());
		json.put("password", usuario.getSenha());
		json.put("password_confirmation", usuario.getConfirmacaoSenha());
		
		return json;
	}
}
