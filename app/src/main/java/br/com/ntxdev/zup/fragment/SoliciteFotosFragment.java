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
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SoliciteActivity;
import br.com.ntxdev.zup.domain.Solicitacao;
import br.com.ntxdev.zup.util.FileUtils;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.util.ImageUtils;
import eu.janmuller.android.simplecropimage.CropImage;

public class SoliciteFotosFragment extends Fragment implements View.OnClickListener {

	private TextView fotoButton;
	private final int CAMERA_RETURN = 1406;
	private final int CROP_RETURN = 1407;
	private final int GALLERY_RETURN = 1408;
	private Uri imagemTemporaria;
	private ImageView fotoFrame;
	private LinearLayout containerFotos;
	private List<String> listaFotos = new ArrayList<String>();
	
	private View temp = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		((SoliciteActivity) getActivity()).setInfo(R.string.adicione_fotos);
		
		if (savedInstanceState != null) {
			imagemTemporaria = Uri.fromFile(new File(savedInstanceState.getString("file")));
		}

		View view = inflater.inflate(R.layout.fragment_solicite_fotos, container, false);

		fotoFrame = (ImageView) view.findViewById(R.id.fotoFrame);
		containerFotos = (LinearLayout) view.findViewById(R.id.containerFotos);

		fotoButton = (TextView) view.findViewById(R.id.fotoButton);
		fotoButton.setOnClickListener(this);
		fotoButton.setTypeface(FontUtils.getRegular(getActivity()));

		return view;
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<String> fotos = null;
        if (getArguments() != null) {
            Solicitacao solicitacao = (Solicitacao) getArguments().getSerializable("solicitacao");
            if (solicitacao != null) {
                fotos = solicitacao.getFotos();
            }
        }

        if (fotos == null || fotos.isEmpty()) {
            fotos = ((SoliciteActivity) getActivity()).getFotos();
        }

        if (fotos != null) {
            for (String foto : fotos) {
                adicionarFoto(foto);
            }
        }
    }

    @Override
	public void onClick(View v) {
		new AlertDialog.Builder(getActivity()).setItems(R.array.foto_menu, new DialogInterface.OnClickListener() {
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
		}).show();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		//outState.putString("file", imagemTemporaria.getPath());
		super.onSaveInstanceState(outState);
	}

	private void selecionarFoto() {
		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		getActivity().startActivityForResult(intent, GALLERY_RETURN);
	}

	private void tirarFoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Arquivo temporário
		imagemTemporaria = Uri.fromFile(new File(FileUtils.getTempImagesFolder(), "tmp_image_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imagemTemporaria);
		intent.putExtra("return-data", true);
		getActivity().startActivityForResult(intent, CAMERA_RETURN);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			temp = null;
			return;
		}

		switch (requestCode) {
		case CROP_RETURN:
			String path = data.getStringExtra(CropImage.IMAGE_PATH);
			if (path == null) {
				temp = null;
				return;
			}
			
			removerFoto(temp);

			((SoliciteActivity) getActivity()).adicionarFoto(path);
			((SoliciteActivity) getActivity()).assertFragmentVisibility();
			adicionarFoto(path);
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
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(FileUtils.getTempImagesFolder(), System.currentTimeMillis() + ".jpg")));
			intent.putExtra(CropImage.ASPECT_X, 1);
			intent.putExtra(CropImage.ASPECT_Y, 1);
			intent.putExtra(CropImage.OUTPUT_X, 800);
			intent.putExtra(CropImage.OUTPUT_Y, 800);

			getActivity().startActivityForResult(intent, CROP_RETURN);
			break;
		}
	}

	private void removerFoto(View view) {
		if (view == null) return;
		
		String foto = (String) view.getTag();
		containerFotos.removeView(view);
		listaFotos.remove(foto);
		
		if (listaFotos.isEmpty()) {
			fotoFrame.setVisibility(View.VISIBLE);
			containerFotos.setVisibility(View.GONE);
		} else {
			containerFotos.setWeightSum(listaFotos.size());
		}
	}

    private void adicionarFoto(String foto) {
        Bitmap bitmap = BitmapFactory.decodeFile(foto);
        listaFotos.add(foto);
        if (listaFotos.size() == 3) {
            fotoButton.setEnabled(false);
        }

        fotoFrame.setVisibility(View.GONE);

        RelativeLayout layout = new RelativeLayout(getActivity());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        if (listaFotos.size() > 1) {
            lp.setMargins((int) ImageUtils.dpToPx(getActivity(), 5), 0, 0, 0);
        }
        layout.setLayoutParams(lp);

        ImageView imgView = new ImageView(getActivity());
        imgView.setId((int) System.currentTimeMillis());

        if (getResources().getDimension(R.dimen.image_resize) != 0) {
            imgView.setImageBitmap(Bitmap.createScaledBitmap(bitmap,
                    (int) (getResources().getDimension(R.dimen.image_resize) / getResources().getDisplayMetrics().density),
                    (int) (getResources().getDimension(R.dimen.image_resize) / getResources().getDisplayMetrics().density), false));
        } else {
            imgView.setImageBitmap(bitmap);
        }
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        imgView.setLayoutParams(layoutParams);

        layout.addView(imgView);

        ImageView btn = new ImageView(getActivity());
        btn.setClickable(true);
        btn.setImageResource(R.drawable.btn_editar_foto);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                new AlertDialog.Builder(getActivity()).setItems(R.array.foto_menu_editar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                removerFoto((View) v.getParent());
                                fotoButton.setEnabled(true);
                                break;
                            case 1:
                                temp = (View) v.getParent();
                                selecionarFoto();
                                break;
                            case 2:
                                temp = (View) v.getParent();
                                tirarFoto();
                                break;
                            case 3:
                                dialog.dismiss();
                                break;
                        }
                    }
                }).show();
            }
        });
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_TOP, imgView.getId());
        params.addRule(RelativeLayout.ALIGN_RIGHT, imgView.getId());
        btn.setLayoutParams(params);

        layout.addView(btn);
        layout.setTag(foto);

        containerFotos.setVisibility(View.VISIBLE);
        containerFotos.setWeightSum(listaFotos.size());
        containerFotos.addView(layout);
    }
}
