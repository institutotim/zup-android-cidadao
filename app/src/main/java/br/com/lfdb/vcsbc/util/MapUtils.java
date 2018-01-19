package br.com.lfdb.vcsbc.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.lfdb.vcsbc.R;
import br.com.lfdb.vcsbc.ZupApplication;

public class MapUtils {

    public static Bitmap createMarker(Context context, int color, int number) {
        if (context == null) context = ZupApplication.getContext();
        ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.cluster_marker, null);
        GradientDrawable bgDrawable = (GradientDrawable) view.getBackground().mutate();
        bgDrawable.setColor(color);
        bgDrawable.invalidateSelf();
        ((TextView) view.getChildAt(0)).setText(String.valueOf(number));
        return ViewUtils.getBitmapFromView(view);
    }
}
