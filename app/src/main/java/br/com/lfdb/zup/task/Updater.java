package br.com.lfdb.zup.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.lfdb.zup.api.model.InventoryCategoriesResponse;
import br.com.lfdb.zup.api.model.InventoryCategory;
import br.com.lfdb.zup.api.model.ReportCategoriesResponse;
import br.com.lfdb.zup.api.model.ReportCategory;
import br.com.lfdb.zup.api.model.ReportCategoryStatus;
import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.core.ConstantesBase;
import br.com.lfdb.zup.util.FileUtils;
import br.com.lfdb.zup.util.ImageUtils;

public class Updater {

    public void update(Context context) throws Exception {
        long start = System.currentTimeMillis();
        try {
            Request request = new Request.Builder()
                    .url(Constantes.REST_URL + "/reports/categories" + ConstantesBase.getCategoriasRelatoQuery(context) + "&display_type=full")
                    .build();
            Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
            saveCategories(context, response.body().string(), "reports");
            setupStatuses(context);

            request = new Request.Builder()
                    .url(Constantes.REST_URL + "/inventory/categories" + ConstantesBase.getCategoriasInventarioQuery(context))
                    .build();
            response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
            saveCategories(context, response.body().string(), "inventory");

            updateFeatureFlags(context);
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
            throw e;
        }
        Log.d("UPDATE", "Update took " + (System.currentTimeMillis() - start) + " milliseconds");
    }

    private void setupStatuses(Context context) throws Exception {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String raw = prefs.getString("reports", "{\"categories\":[]}");
        List<ReportCategory> categories = ConstantesBase.GSON.fromJson(raw, ReportCategoriesResponse.class).getCategories();
        Set<ReportCategoryStatus> statuses = new HashSet<>();
        for (ReportCategory category : categories) {
            statuses.addAll(category.getStatuses());

            if (!category.getSubcategories().isEmpty()) {
                for (ReportCategory subcategory : category.getSubcategories()) {
                    statuses.addAll(subcategory.getStatuses());
                }
            }
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
        String density = ImageUtils.shouldDownloadRetinaIcon(context) ? "retina" : "default";
        if (type.equals("reports")) {
            long start = System.currentTimeMillis();
            ReportCategoriesResponse response = ConstantesBase.GSON.fromJson(json, ReportCategoriesResponse.class);
            Log.d("UPDATE", "Parsing took " + (System.currentTimeMillis() - start) + " milliseconds");
            for (ReportCategory category : response.getCategories()) {
                downloadImages(context, type, density, category);
                for (ReportCategory sub : category.getSubcategories()) downloadImages(context, type, density, sub);
            }
        } else {
            InventoryCategoriesResponse response = ConstantesBase.GSON.fromJson(json, InventoryCategoriesResponse.class);
            for (InventoryCategory category : response.getCategories()) {
                if (density.equals("retina")) {
                    if (category.getPin() != null) FileUtils.downloadImage(context, type, category.getPin().getRetina().getMobile());
                    FileUtils.downloadImage(context, type, category.getMarker().getRetina().getMobile());
                    FileUtils.downloadImage(context, type, category.getIcon().getRetina().getMobile().getActive());
                    FileUtils.downloadImage(context, type, category.getIcon().getRetina().getMobile().getDisabled());
                } else {
                    if (category.getPin() != null) FileUtils.downloadImage(context, type, category.getPin().getCommon().getMobile());
                    FileUtils.downloadImage(context, type, category.getMarker().getCommon().getMobile());
                    FileUtils.downloadImage(context, type, category.getIcon().getCommon().getMobile().getActive());
                    FileUtils.downloadImage(context, type, category.getIcon().getCommon().getMobile().getDisabled());
                }
            }
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(type, json).apply();
    }

    private void downloadImages(Context context, String type, String density, ReportCategory category) throws Exception {
        if (density.equals("retina")) {
            FileUtils.downloadImage(context, type, category.getMarker().getRetina().getMobile());
            FileUtils.downloadImage(context, type, category.getIcon().getRetina().getMobile().getActive());
            FileUtils.downloadImage(context, type, category.getIcon().getRetina().getMobile().getDisabled());
        } else {
            FileUtils.downloadImage(context, type, category.getMarker().getCommon().getMobile());
            FileUtils.downloadImage(context, type, category.getIcon().getCommon().getMobile().getActive());
            FileUtils.downloadImage(context, type, category.getIcon().getCommon().getMobile().getDisabled());
        }
    }
}
