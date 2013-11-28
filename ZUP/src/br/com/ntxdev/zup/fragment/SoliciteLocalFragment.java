package br.com.ntxdev.zup.fragment;

import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SoliciteActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SoliciteLocalFragment extends Fragment {

	private static final String TAG = "ExploreFragment";
	private static View view;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}

		((SoliciteActivity) getActivity()).exibirBarraInferior(true);
		((SoliciteActivity) getActivity()).setInfo(R.string.selecione_o_local);
		
		try {
			view = inflater.inflate(R.layout.fragment_solicite_local, container, false);
		} catch (InflateException e) {
			Log.w(TAG, e.getMessage());
		}
		return view;
	}
}
