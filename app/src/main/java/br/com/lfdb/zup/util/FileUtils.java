package br.com.lfdb.zup.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.os.Environment;

public class FileUtils {

	public static boolean imageExists(Context context, String filename) {
		File imagesFolder = getImagesFolder(context);
		if (!imagesFolder.exists()) {
			imagesFolder.mkdirs();
		}
		
		return new File(imagesFolder, filename).exists();
	}

    public static boolean imageExists(Context context, String subfolder, String filename) {
        File imagesFolder = getImagesFolder(context, subfolder);
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }

        return new File(imagesFolder, filename).exists();
    }
	
	public static File getImagesFolder(Context context) {
		return new File(context.getFilesDir() + File.separator + "images" + File.separator + "images");
	}

    public static File getImagesFolder(Context context, String subfolder) {
        return new File(context.getFilesDir() + File.separator + "images" + File.separator + "images" + File.separator + subfolder);
    }

    public static File getTempImagesFolder() {
        File imagesFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "ZUP" + File.separator + "temp");
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }

        return imagesFolder;
    }
	
	public static void downloadImage(Context context, String url) throws Exception {
		String[] parts = url.split("/");
		String filename = parts[parts.length - 1];
		if (!imageExists(context, filename)) {
			URL u = new URL(url);
			URLConnection ucon = u.openConnection();
			InputStream is = ucon.getInputStream();
	        BufferedInputStream bis = new BufferedInputStream(is);
	        ByteArrayBuffer baf = new ByteArrayBuffer(50);
	        int current;
	        while ((current = bis.read()) != -1) {
	            baf.append((byte) current);
	        }
	        FileOutputStream fos = new FileOutputStream(new File(getImagesFolder(context), filename));
	        fos.write(baf.toByteArray());
	        fos.close();
		}
	}

    public static void downloadImage(Context context, String subfolder, String url) throws Exception {
        String[] parts = url.split("/");
        String filename = parts[parts.length - 1];
        if (!imageExists(context, subfolder, filename)) {
            URL u = new URL(url);
            URLConnection ucon = u.openConnection();
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            int current;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            FileOutputStream fos = new FileOutputStream(new File(getImagesFolder(context, subfolder), filename));
            fos.write(baf.toByteArray());
            fos.close();
        }
    }
}
