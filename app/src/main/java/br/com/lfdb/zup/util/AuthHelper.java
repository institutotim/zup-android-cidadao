package br.com.lfdb.zup.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import br.com.lfdb.zup.OpeningActivity;

public class AuthHelper {

    public static void redirectSessionExpired(Context context) {
        Intent intent = new Intent(context, OpeningActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.finish();
        }

        Toast.makeText(context, "Sessão expirada, por favor realize seu login novamente", Toast.LENGTH_LONG).show();
    }
}
