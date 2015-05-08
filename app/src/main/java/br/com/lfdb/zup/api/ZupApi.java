package br.com.lfdb.zup.api;

import android.content.Context;
import android.support.annotation.NonNull;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import br.com.lfdb.zup.BuildConfig;
import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.core.ConstantesBase;
import br.com.lfdb.zup.service.LoginService;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class ZupApi {

    private static ZupService service;

    public static boolean validateCityBoundary(Context context, double latitude, double longitude)
            throws Exception {
        String call = Constantes.REST_URL + "/utils/city-boundary/validate?latitude=" + latitude +
                "&longitude=" + longitude;
        String header = new LoginService().getToken(context);

        Request request = new Request.Builder()
                .url(call)
                .header("X-App-Token", header)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        JSONObject json = new JSONObject(response.body().string());
        return json.optBoolean("inside_boundaries", false);
    }

    @NonNull
    public static ZupService get(final Context context) {
        if (service == null) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setRequestInterceptor(request -> request.addHeader("X-App-Token",
                            new LoginService().getToken(context)))
                    .setEndpoint(Constantes.REST_URL)
                    .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                    .setConverter(new GsonConverter(ConstantesBase.GSON))
                    .build();
            service = restAdapter.create(ZupService.class);
        }
        return service;
    }
}
