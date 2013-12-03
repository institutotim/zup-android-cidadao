package br.com.ntxdev.zup.widget;

import java.util.Arrays;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.fragment.ImageViewFragment;

import com.viewpagerindicator.IconPagerAdapter;

public class ImagePagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

	protected final List<Integer> imagens = Arrays.asList(R.drawable.img_1, R.drawable.img_2, R.drawable.img_3);

	public ImagePagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return ImageViewFragment.newInstance(imagens.get(position));
	}

	@Override
	public int getCount() {
		return imagens.size();
	}

	@Override
	public int getIconResId(int index) {
		return R.drawable.ic_launcher;
	}
}