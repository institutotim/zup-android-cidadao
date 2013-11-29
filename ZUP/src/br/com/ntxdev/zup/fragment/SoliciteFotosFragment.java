package br.com.ntxdev.zup.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SoliciteActivity;
import eu.janmuller.android.simplecropimage.CropImage;

public class SoliciteFotosFragment extends Fragment implements View.OnClickListener {

	private Button fotoButton;
	private final int CAMERA_RETURN = 1406;
	private final int CROP_RETURN = 1407;
	private final int GALLERY_RETURN = 1408;
	private Uri imagemTemporaria;
	private ImageView fotoFrame;
	private LinearLayout containerFotos;
	private List<String> listaFotos = new ArrayList<String>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		((SoliciteActivity) getActivity()).setInfo(R.string.adicione_fotos);
		
		View view = inflater.inflate(R.layout.fragment_solicite_fotos, container, false);
		
		fotoFrame = (ImageView) view.findViewById(R.id.fotoFrame);
		containerFotos = (LinearLayout) view.findViewById(R.id.containerFotos);
		
		fotoButton = (Button) view.findViewById(R.id.fotoButton);
		fotoButton.setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onClick(View v) {
		new AlertDialog.Builder(getActivity())
			.setItems(R.array.foto_menu, new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int item) {
					switch (item) {
					case 0:
						selecionarFoto();
						break;
					case 1:
						tirarFoto();
						break;
					case 2:
						dialog.dismiss();
						break;
					}
				}
			})
			.show();
	}
	
	private void selecionarFoto() {
		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, GALLERY_RETURN);
	}
	
	private void tirarFoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Arquivo temporário
		imagemTemporaria = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tmp_image_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imagemTemporaria);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, CAMERA_RETURN);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		
		switch (requestCode) {
		case CROP_RETURN:
			String path = data.getStringExtra(CropImage.IMAGE_PATH);
            if (path == null) {
                return;
            }
            
            ((SoliciteActivity) getActivity()).adicionarFoto(path);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            listaFotos.add(path);
            fotoFrame.setVisibility(View.GONE);
            ImageView imgView = new ImageView(getActivity());
            imgView.setImageBitmap(bitmap);
            imgView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            imgView.setTag(path);
            containerFotos.setVisibility(View.VISIBLE);
            containerFotos.setWeightSum(listaFotos.size());
            containerFotos.addView(imgView);
			break;
		case GALLERY_RETURN:
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
	        cursor.moveToFirst();
	        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	        String picturePath = cursor.getString(columnIndex);
	        cursor.close();
	        imagemTemporaria = Uri.fromFile(new File(picturePath));
		case CAMERA_RETURN:
			Intent intent = new Intent(getActivity(), CropImage.class);
			intent.putExtra(CropImage.IMAGE_PATH, imagemTemporaria.getPath());
			intent.putExtra(CropImage.SCALE, true);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
							System.currentTimeMillis() + ".jpg")));			
	        intent.putExtra(CropImage.ASPECT_X, 1);
	        intent.putExtra(CropImage.ASPECT_Y, 1);
	        intent.putExtra(CropImage.OUTPUT_X, 320);
	        intent.putExtra(CropImage.OUTPUT_Y, 320);
	                    
	        startActivityForResult(intent, CROP_RETURN);
			break;
		}
	}
}
