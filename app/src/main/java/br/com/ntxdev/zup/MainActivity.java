package br.com.ntxdev.zup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;
import br.com.ntxdev.zup.domain.BuscaEstatisticas;
import br.com.ntxdev.zup.domain.BuscaExplore;
import br.com.ntxdev.zup.fragment.EstatisticasFragment;
import br.com.ntxdev.zup.fragment.ExploreFragment;
import br.com.ntxdev.zup.fragment.MinhaContaFragment;
import br.com.ntxdev.zup.service.CategoriaRelatoService;
import br.com.ntxdev.zup.service.LoginService;
import br.com.ntxdev.zup.util.FontUtils;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
	
	private static final int SOLICITACAO_REQUEST_CODE = 1010;
	public static final int FILTRO_CODE = 1007;
	
	private TextView current;
	
	private EstatisticasFragment estatisticasFragment;
	private ExploreFragment exploreFragment;
	private MinhaContaFragment minhaContaFragment;
	
	private Fragment atual;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TextView exploreButton = (TextView) findViewById(R.id.exploreButton);
		exploreButton.setOnClickListener(this);
		exploreButton.setTypeface(FontUtils.getRegular(this));
		
		TextView soliciteButton = (TextView) findViewById(R.id.soliciteButton);
		soliciteButton.setOnClickListener(this);
		soliciteButton.setTypeface(FontUtils.getRegular(this));
		
		TextView minhaContaButton = (TextView) findViewById(R.id.minhaContaButton);
		minhaContaButton.setOnClickListener(this);
		minhaContaButton.setTypeface(FontUtils.getRegular(this));
		
		TextView estatisticasButton = (TextView) findViewById(R.id.estatisticasButton);
		estatisticasButton.setOnClickListener(this);
		estatisticasButton.setTypeface(FontUtils.getRegular(this));
		
		if (new CategoriaRelatoService().getCategorias(this).isEmpty()) {
			findViewById(R.id.footer).setVisibility(View.GONE);
		}
		
		current = exploreButton;
		exploreFragment = new ExploreFragment();
		setFragment(exploreFragment);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == current.getId()) {
			return;
		}
		
		if (!new LoginService().usuarioLogado(this) && v.getId() == R.id.minhaContaButton) {
			startActivity(new Intent(this, LoginActivity.class));
			return;
		}
		
		if (v.getId() == R.id.soliciteButton) {
			startActivityForResult(new Intent(this, SoliciteActivity.class), SOLICITACAO_REQUEST_CODE);
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
			if (estatisticasFragment == null) {
				estatisticasFragment = new EstatisticasFragment();
				setFragment(estatisticasFragment);
			} else {
				activeFragment(estatisticasFragment);
			}
			break;
		case R.id.exploreButton:
			view.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.tabbar_icon_explore_active), null, null);
			if (exploreFragment == null) {
				exploreFragment = new ExploreFragment();
				setFragment(exploreFragment);
			} else {
				activeFragment(exploreFragment);
			}
			break;
		case R.id.minhaContaButton:			
			view.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.tabbar_icon_conta_active), null, null);
			if (minhaContaFragment == null) {
				minhaContaFragment = new MinhaContaFragment();
				setFragment(minhaContaFragment);
			} else {
				activeFragment(minhaContaFragment);
			}
			break;				
		}
		current = view;
	}

	private void setFragment(Fragment fragment) {
		FragmentTransaction fm = getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, fragment);
		if (atual != null) {
			fm.hide(atual);
		}
		fm.commit();
		atual = fragment;
	}
	
	private void activeFragment(Fragment fragment) {
		getSupportFragmentManager().beginTransaction().hide(atual).show(fragment).commit();
		atual = fragment;
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SOLICITACAO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			exploreFragment.refresh();
		} else if (requestCode == FILTRO_CODE && resultCode == Activity.RESULT_OK) {
			exploreFragment.aplicarFiltro((BuscaExplore) data.getSerializableExtra("busca"));
		} else if (requestCode == EstatisticasFragment.REQUEST_FILTRO && resultCode == Activity.RESULT_OK) {
			estatisticasFragment.aplicarFiltro((BuscaEstatisticas) data.getSerializableExtra("busca"));
		}
	}
}
