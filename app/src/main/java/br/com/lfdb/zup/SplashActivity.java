package br.com.lfdb.zup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.core.ConstantesBase;
import br.com.lfdb.zup.domain.SolicitacaoListItem;
import br.com.lfdb.zup.service.LoginService;
import br.com.lfdb.zup.task.Updater;
import br.com.lfdb.zup.util.NetworkUtils;
import br.com.lfdb.zup.widget.SolicitacaoListItemAdapter;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.json.JSONObject;

@EActivity(R.layout.activity_splash) public class SplashActivity extends Activity {

  private SolicitacaoListItem item = null;

  @AfterViews void init() {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
      getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
    categoryUpdater();
  }

  boolean isConnected(){
    if (!NetworkUtils.isInternetPresent(SplashActivity.this)) {
      return false;
    }
    return true;
  }

  @Background void categoryUpdater() {
    long start, finish;
    boolean error = isConnected();
    start = System.currentTimeMillis();
    try {
      Context context = SplashActivity.this;
      new Updater().update(context);
      long reportId = getIntent().getLongExtra("report_id", -1);
      if (reportId != -1 && new LoginService().usuarioLogado(context)) {
        OkHttpClient client = new OkHttpClient();
        Request request =
            new Request.Builder().addHeader("X-App-Token", new LoginService().getToken(context))
                .url(Constantes.REST_URL
                    + "/reports/items/"
                    + reportId
                    + ConstantesBase.getItemRelatoQuery(context))
                .get()
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new Exception();
        item = SolicitacaoListItemAdapter.adapt(context,
            new JSONObject(response.body().string()).getJSONObject("report"));
      }
    } catch (Exception e) {
      error = true;
    }
    if (!error) {
      finish = System.currentTimeMillis();
      if (finish - start < 3500) {
        try {
          Thread.sleep(finish - start);
        } catch (Exception e) {
          Log.w("ZUP", e.getMessage());
        }
      }
      Intent intent = new Intent(SplashActivity.this, OpeningActivity.class);
      startActivity(intent);
      if (item != null) {
        intent = new Intent(SplashActivity.this, SolicitacaoDetalheActivity.class);
        intent.putExtra("solicitacao", item);
        startActivity(intent);
      }
      finish();
    } else {
      if (error) {
        new AlertDialog.Builder(SplashActivity.this).setTitle("Falha na sincronização")
            .setMessage(
                "Não foi possível realizar o sincronismo de dados. Deseja tentar novamente?")
            .setPositiveButton("Sim", (dialog, which) -> {
              dialog.dismiss();
              categoryUpdater();
            })
            .setNegativeButton("Não", (dialog, which) -> {
              dialog.dismiss();
              finish();
            })
            .show();
      } else {
        Toast.makeText(getApplicationContext(), "Conexão com a Internet indisponível",
            Toast.LENGTH_SHORT).show();
        finish();
      }
    }
  }
}
