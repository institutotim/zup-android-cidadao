package br.com.ntxdev.zup.task;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import br.com.ntxdev.zup.core.Constantes;
import br.com.ntxdev.zup.util.FileUtils;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Updater {

	public void update(Context context) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(Constantes.REST_URL + "/reports/categories");
			HttpResponse response = client.execute(get);
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				saveCategories(context, EntityUtils.toString(response.getEntity()), "reports");
			}
			
			get = new HttpGet(Constantes.REST_URL + "/inventory/categories");
			response = client.execute(get);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				saveCategories(context, EntityUtils.toString(response.getEntity()), "inventory");
			}
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage());
		}
	}
	
	private void saveCategories(Context context, String json, String type) throws Exception {
		JSONArray array = type.equals("inventory") ? new JSONObject(json).getJSONArray("categories") : new JSONArray(json);
		for (int i = 0; i < array.length(); i++) {
			String markerUrl = array.getJSONObject(i).getJSONObject("marker").getString("url");
			FileUtils.downloadImage(markerUrl);
			String iconUrl = array.getJSONObject(i).getJSONObject("icon").getString("url");
			FileUtils.downloadImage(iconUrl);
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString(type, json).commit();
	}
}
