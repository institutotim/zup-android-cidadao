package br.com.ntxdev.zup.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.os.Environment;

public class FileUtils {

	public static boolean imageExists(String filename) {
		File imagesFolder = getImagesFolder();
		if (!imagesFolder.exists()) {
			imagesFolder.mkdirs();
		}
		
		return new File(imagesFolder, filename).exists();
	}
	
	public static File getImagesFolder() {
		return new File(Environment.getExternalStorageDirectory() + File.separator + "ZUP" + File.separator + "images");
	}
	
	public static File getTempImagesFolder() {
		File imagesFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "ZUP" + File.separator + "temp");
		if (!imagesFolder.exists()) {
			imagesFolder.mkdirs();
		}
		
		return imagesFolder;
	}
	
	public static void downloadImage(String url) throws Exception {
		String[] parts = url.split("/");
		String filename = parts[parts.length - 1];
		if (!imageExists(filename)) {
			URL u = new URL(url);
			URLConnection ucon = u.openConnection();
			InputStream is = ucon.getInputStream();
	        BufferedInputStream bis = new BufferedInputStream(is);
	        ByteArrayBuffer baf = new ByteArrayBuffer(50);
	        int current = 0;
	        while ((current = bis.read()) != -1) {
	            baf.append((byte) current);
	        }
	        FileOutputStream fos = new FileOutputStream(new File(getImagesFolder(), filename));
	        fos.write(baf.toByteArray());
	        fos.close();
		}
	}
}
