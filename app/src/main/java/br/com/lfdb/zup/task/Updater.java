package br.com.lfdb.zup.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.lfdb.zup.api.model.ReportCategory;
import br.com.lfdb.zup.api.model.ReportCategoryStatus;
import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.core.ConstantesBase;
import br.com.lfdb.zup.util.FileUtils;
import br.com.lfdb.zup.util.ImageUtils;

public class Updater {

	public void update(Context context) throws Exception {
		try {
			HttpClient client = new OkApacheClient();
            HttpGet get = new HttpGet(Constantes.REST_URL + "/reports/categories" + ConstantesBase.getCategoriasRelatoQuery(context) + "&display_type=full");
			HttpResponse response = client.execute(get);
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				saveCategories(context, EntityUtils.toString(response.getEntity(), "UTF-8"), "reports");
                setupStatuses(context);
			}
			
			get = new HttpGet(Constantes.REST_URL + "/inventory/categories" + ConstantesBase.getCategoriasInventarioQuery(context));
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

    private void setupStatuses(Context context) throws Exception {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String raw = prefs.getString("reports", "{\"categories\":[]}");
        List<ReportCategory> categories = ConstantesBase.GSON.fromJson(new JSONObject(raw)
                .getJSONArray("categories").toString(), new TypeToken<List<ReportCategory>>() {
        }.getType());
        Set<ReportCategoryStatus> statuses = new HashSet<>();
        for (ReportCategory category : categories) {
            statuses.addAll(category.getStatuses());
        }
        prefs.edit().putString("statuses", ConstantesBase.GSON.toJson(new ArrayList<>(statuses))).apply();
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
            if (array.getJSONObject(i).has("pin")) {
                markerUrl = array.getJSONObject(i).getJSONObject("pin").getJSONObject(density).getString("mobile");
                FileUtils.downloadImage(context, type, markerUrl);
            }
			JSONObject iconUrl = array.getJSONObject(i).getJSONObject("icon").getJSONObject(density).getJSONObject("mobile");
			FileUtils.downloadImage(context, type, iconUrl.getString("active").startsWith("http") ? iconUrl.getString("active") : Constantes.REST_URL + iconUrl.getString("active"));
            FileUtils.downloadImage(context, type, iconUrl.getString("disabled").startsWith("http") ? iconUrl.getString("disabled") : Constantes.REST_URL + iconUrl.getString("disabled"));

            if (array.getJSONObject(i).has("subcategories")) {
                for (int j = 0; j < array.getJSONObject(i).getJSONArray("subcategories").length(); j++) {
                    JSONObject subcategory = array.getJSONObject(i).getJSONArray("subcategories").getJSONObject(j);

                    String marker = subcategory.getJSONObject("marker").getJSONObject(density).getString("mobile");
                    FileUtils.downloadImage(context, type, marker);
                    JSONObject icon = subcategory.getJSONObject("icon").getJSONObject(density).getJSONObject("mobile");
                    FileUtils.downloadImage(context, type, icon.getString("active").startsWith("http") ? icon.getString("active") : Constantes.REST_URL + icon.getString("active"));
                    FileUtils.downloadImage(context, type, icon.getString("disabled").startsWith("http") ? icon.getString("disabled") : Constantes.REST_URL + icon.getString("disabled"));
                }
            }
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString(type, json).apply();
	}
}
