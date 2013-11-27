package br.com.ntxdev.zup.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import br.com.ntxdev.zup.R;

public class ExploreFragment extends Fragment {

	private static final String TAG = "ExploreFragment";
	private static View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}

		try {
			view = inflater.inflate(R.layout.fragment_explore, container, false);
		} catch (InflateException e) {
			Log.w(TAG, e.getMessage());
		}
		return view;
	}
}
