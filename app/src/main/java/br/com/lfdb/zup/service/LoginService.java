package br.com.lfdb.zup.service;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LoginService {

	public void atualizarUsuario(Context context, JSONObject usuario) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString("raw_user", usuario.toString()).commit();	
	}
	
	public void registrarLogin(Context context, JSONObject usuario, String token) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString("raw_user", usuario.toString()).putString("token", token).commit();	
	}
	
	public void registrarLogout(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().remove("raw_user").remove("token").commit();
	}
	
	public boolean usuarioLogado(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.contains("raw_user") && prefs.contains("token");
	}
	
	public String getToken(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString("token", "");
	}
}
