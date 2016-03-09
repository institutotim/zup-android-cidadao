package br.com.lfdb.zup.util;

import android.content.Context;
import android.widget.Toast;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

@EBean
public class ToastHelper {

  @UiThread public static void show(Context context, String msg, int duration){
    Toast.makeText(context, msg, duration).show();
  }

}
