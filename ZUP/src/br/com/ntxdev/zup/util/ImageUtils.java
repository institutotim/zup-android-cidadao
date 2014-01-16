package br.com.ntxdev.zup.util;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.DisplayMetrics;

public class ImageUtils {

	public static Bitmap toGrayscale(Bitmap srcBitmap) {
		int height = srcBitmap.getHeight();
		int width = srcBitmap.getWidth();
		
		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	    Canvas c = new Canvas(bmpGrayscale);
	    Paint paint = new Paint();
	    ColorMatrix cm = new ColorMatrix();
	    cm.setSaturation(0);
	    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
	    paint.setColorFilter(f);
	    c.drawBitmap(srcBitmap, 0, 0, paint);
	    return bmpGrayscale;
	}
	
	public static Bitmap loadFromFile(String file) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeFile(new File(FileUtils.getImagesFolder() + File.separator + file).toString(), options);
	}
	
	public static Bitmap getScaled(Activity activity, Bitmap bitmapOrg) {
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();

		float scaleWidth = metrics.scaledDensity;
		float scaleHeight = metrics.scaledDensity;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		return Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);
	}
	
	public static StateListDrawable getStateListDrawable(Activity activity, String filename) {
		Bitmap original = ImageUtils.getScaled(activity, ImageUtils.loadFromFile(filename));
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[] {android.R.attr.state_pressed}, new BitmapDrawable(activity.getResources(), original));
		states.addState(new int[] {}, new BitmapDrawable(activity.getResources(), ImageUtils.toGrayscale(original)));
		return states;
	}
}
