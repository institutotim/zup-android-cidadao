package br.com.ntxdev.zup;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import br.com.ntxdev.zup.fragment.EstatisticasFragment;
import br.com.ntxdev.zup.fragment.ExploreFragment;
import br.com.ntxdev.zup.fragment.MinhaContaFragment;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
	
	private TextView current;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.exploreButton).setOnClickListener(this);
		findViewById(R.id.soliciteButton).setOnClickListener(this);
		findViewById(R.id.minhaContaButton).setOnClickListener(this);
		findViewById(R.id.estatisticasButton).setOnClickListener(this);
		
		current = (TextView) findViewById(R.id.exploreButton);
		setFragment(new ExploreFragment());
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == current.getId()) {
			return;
		}
		
		if (v.getId() == R.id.soliciteButton) {
			startActivity(new Intent(this, SoliciteActivity.class));
			return;
		}
		
		unselectCurrent();
		selectCurrent(v.getId());
	}

	private void selectCurrent(int id) {
		TextView view = (TextView) findViewById(id);
		view.setTextColor(Color.WHITE);
		switch (id) {		
		case R.id.estatisticasButton:
			view.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.tabbar_icon_estatisticas_active), null, null);
			setFragment(new EstatisticasFragment());
			break;
		case R.id.exploreButton:
			view.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.tabbar_icon_explore_active), null, null);
			setFragment(new ExploreFragment());
			break;
		case R.id.minhaContaButton:
			view.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.tabbar_icon_conta_active), null, null);
			setFragment(new MinhaContaFragment());
			break;				
		}
		current = view;
	}

	private void setFragment(Fragment fragment) {
		getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, fragment).commit();
	}

	private void unselectCurrent() {
		current.setTextColor(getResources().getColorStateList(R.color.tabbar_text_color));
		switch (current.getId()) {
		case R.id.estatisticasButton:
			current.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.tabbar_icon_estatisticas), null, null);
			break;
		case R.id.exploreButton:
			current.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.tabbar_icon_explore), null, null);
			break;
		case R.id.minhaContaButton:
			current.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.tabbar_icon_conta), null, null);
			break;
		}
	}
}
