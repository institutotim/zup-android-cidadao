package br.com.ntxdev.zup.fragment;

import java.util.List;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SoliciteActivity;
import br.com.ntxdev.zup.domain.CategoriaRelato;
import br.com.ntxdev.zup.service.CategoriaRelatoService;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.util.ImageUtils;

public class SoliciteTipoFragment extends Fragment implements View.OnClickListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_solicite_tipo, container, false);

		((SoliciteActivity) getActivity()).exibirBarraInferior(false);
		((SoliciteActivity) getActivity()).setInfo(R.string.selecione_a_categoria);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		montarCategoriasRelatos();
	}

	@Override
	public void onClick(View v) {
		unselectAll();
		CategoriaRelato categoria = (CategoriaRelato) v.getTag();
		TextView view = (TextView) v;
		view.setTextColor(Color.BLACK);
		view.setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable(getResources(), ImageUtils.getScaled(getActivity(), ((CategoriaRelato) categoria).getIcone())), null, null);
		((SoliciteActivity) getActivity()).setCategoria(categoria);
	}

	private void montarCategoriasRelatos() {
		List<CategoriaRelato> categorias = new CategoriaRelatoService().getCategorias(getActivity());
		LinearLayout ll = (LinearLayout) getView().findViewById(R.id.container);

		LinearLayout container = null;

		for (int i = 0; i < categorias.size(); i++) {
			if (i % 3 == 0) {
				if (container != null) ll.addView(container);
				container = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.linearlayout_row, null);
			}

			TextView view = (TextView) getActivity().getLayoutInflater().inflate(R.layout.categoria_filtro_item, container, false);
			view.setTag(categorias.get(i));
			view.setText(categorias.get(i).getNome());
			view.setTypeface(FontUtils.getRegular(getActivity()));
			view.setCompoundDrawablesWithIntrinsicBounds(null,
					ImageUtils.getStateListDrawable(getActivity(), categorias.get(i).getIcone()), null, null);
			view.setOnClickListener(this);
			container.addView(view);
		}

		if (container != null)
			ll.addView(container);
	}
	
	private void unselectAll() {
		LinearLayout container = (LinearLayout) getView().findViewById(R.id.container);
		for (int i = 0; i < container.getChildCount(); i++) {
			LinearLayout view = (LinearLayout) container.getChildAt(i);
			for (int j = 0; j < view.getChildCount(); j++) {
				TextView txt = (TextView) view.getChildAt(j);
				CategoriaRelato categoria = (CategoriaRelato) txt.getTag();
				txt.setCompoundDrawablesWithIntrinsicBounds(null, ImageUtils.getStateListDrawable(getActivity(), categoria.getIcone()), null, null);
				txt.setTextColor(getResources().getColorStateList(R.color.icon_text_color));
			}
		}
	}
}
