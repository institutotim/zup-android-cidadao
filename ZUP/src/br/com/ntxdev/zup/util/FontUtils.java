package br.com.ntxdev.zup.util;

import android.content.Context;
import android.graphics.Typeface;

public class FontUtils {

	public static Typeface getLight(Context context) {
		return Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Light.ttf");
	}
	
	public static Typeface getExtraBold(Context context) {
		return Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-ExtraBold.ttf");
	}
	
	public static Typeface getBold(Context context) {
		return Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Bold.ttf");
	}
}
