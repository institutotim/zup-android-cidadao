package br.com.lfdb.zup.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.apache.OkApacheClient;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.util.FileUtils;
import br.com.lfdb.zup.util.ImageUtils;

public class Updater {

	public void update(Context context) throws Exception {
		try {
			HttpClient client = new OkApacheClient();
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

            updateFeatureFlags(context);
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage(), e);
            throw e;
		}
	}

    private void updateFeatureFlags(Context context) throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(Constantes.REST_URL + "/feature_flags").build();
        Response response = client.newCall(request).execute();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("features", response.body().string()).apply();
    }

    private void saveCategories(Context context, String json, String type) throws Exception {
		JSONArray array = new JSONObject(json).getJSONArray("categories");

        String density = ImageUtils.shouldDownloadRetinaIcon(context) ? "retina" : "default";
		for (int i = 0; i < array.length(); i++) {
			String markerUrl = array.getJSONObject(i).getJSONObject("marker").getJSONObject(density).getString("mobile");
            FileUtils.downloadImage(context, type, markerUrl);
			JSONObject iconUrl = array.getJSONObject(i).getJSONObject("icon").getJSONObject(density).getJSONObject("mobile");
			FileUtils.downloadImage(context, type, iconUrl.getString("active").startsWith("http") ? iconUrl.getString("active") : Constantes.REST_URL + iconUrl.getString("active"));
            FileUtils.downloadImage(context, type, iconUrl.getString("disabled").startsWith("http") ? iconUrl.getString("disabled") : Constantes.REST_URL + iconUrl.getString("disabled"));

            if (array.getJSONObject(i).has("subcategories")) {
                for (int j = 0; j < array.getJSONObject(i).getJSONArray("subcategories").length(); j++) {
                    JSONObject subcategory = array.getJSONObject(i).getJSONArray("subcategories").getJSONObject(j);

                    String marker = subcategory.getJSONObject("marker").getJSONObject(density).getString("mobile");
                    FileUtils.downloadImage(context, type, marker);
                    JSONObject icon = subcategory.getJSONObject("icon").getJSONObject(density).getJSONObject("mobile");
                    FileUtils.downloadImage(context, type, icon.getString("active").startsWith("http") ? icon.getString("active") : Constantes.REST_URL + iconUrl.getString("active"));
                    FileUtils.downloadImage(context, type, icon.getString("disabled").startsWith("http") ? icon.getString("disabled") : Constantes.REST_URL + iconUrl.getString("disabled"));
                }
            }
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString(type, json).apply();
	}
}
