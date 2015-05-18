package br.com.lfdb.zup.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.annotation.ColorRes;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.lfdb.zup.R;

public class MapUtils {

    public static Bitmap createMarker(Context context, int color, int number) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.cluster_marker, null);
        GradientDrawable bgDrawable = (GradientDrawable) view.getBackground().mutate();
        bgDrawable.setColor(color);
        bgDrawable.invalidateSelf();
        ((TextView) view.getChildAt(0)).setText(String.valueOf(number));
        return ViewUtils.getBitmapFromView(view);
    }



    public static GradientDrawable getBackground(int color) {
        int strokeWidth = 5; // 3px not dp
        int roundRadius = 15; // 8px not dp
        int strokeColor = Color.parseColor("#2E3135");
        int fillColor = Color.parseColor("#DFDFE0");

        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(fillColor);
        gd.setCornerRadius(roundRadius);
        gd.setStroke(strokeWidth, strokeColor);

        return gd;
    }
}
