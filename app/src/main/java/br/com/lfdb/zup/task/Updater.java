package br.com.lfdb.zup.task;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.util.FileUtils;

public class Updater {

	public void update(Context context) throws Exception {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(Constantes.REST_URL + "/reports/categories?display_type=full");
			HttpResponse response = client.execute(get);
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				saveCategories(context, EntityUtils.toString(response.getEntity(), "UTF-8"), "reports");
			}
			
			get = new HttpGet(Constantes.REST_URL + "/inventory/categories");
			response = client.execute(get);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				saveCategories(context, EntityUtils.toString(response.getEntity(), "UTF-8"), "inventory");
			}
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage(), e);
            throw e;
		}
	}
	
	private void saveCategories(Context context, String json, String type) throws Exception {
		JSONArray array = new JSONObject(json).getJSONArray("categories");
		for (int i = 0; i < array.length(); i++) {
			String markerUrl = array.getJSONObject(i).getJSONObject("marker").getJSONObject("default").getString("mobile");
			FileUtils.downloadImage(context, type, markerUrl);
			JSONObject iconUrl = array.getJSONObject(i).getJSONObject("icon").getJSONObject("default").getJSONObject("mobile");
			FileUtils.downloadImage(context, type, iconUrl.getString("active"));
            FileUtils.downloadImage(context, type, iconUrl.getString("disabled"));
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString(type, json).commit();
	}
}
