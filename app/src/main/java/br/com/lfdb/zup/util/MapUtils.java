package br.com.lfdb.zup.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.ColorRes;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.lfdb.zup.R;

public class MapUtils {

    public static Bitmap createMarker(Context context, @ColorRes int color, int number) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.cluster_marker, null);
        ((TextView) view.getChildAt(0)).setText(String.valueOf(number));
        return ViewUtils.getBitmapFromView(view);
    }

    public static Bitmap createMarker(Context context, int number) {
        return createMarker(context, R.color.light_blue, number);
    }
}
