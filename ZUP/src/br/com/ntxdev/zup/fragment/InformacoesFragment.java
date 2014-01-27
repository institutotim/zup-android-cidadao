package br.com.ntxdev.zup.fragment;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.domain.Arvore;
import br.com.ntxdev.zup.domain.BocaDeLobo;
import br.com.ntxdev.zup.domain.Local;
import br.com.ntxdev.zup.domain.Wifi;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.widget.ImageResourcePagerAdapter;

import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.PageIndicator;

public class InformacoesFragment extends Fragment {

	private Local local;
	private LinearLayout layout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_informacoes, container, false);
		
		local = (Local) getArguments().get("local");
		layout = (LinearLayout) view.findViewById(R.id.conteudo);
		
		preencherDados();
		
		ImageResourcePagerAdapter mAdapter = new ImageResourcePagerAdapter(getActivity().getSupportFragmentManager(), local.getImagens());

		ViewPager mPager = (ViewPager) view.findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		PageIndicator mIndicator = (IconPageIndicator) view.findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);
		
		return view;
	}
	
	private void preencherDados() {
		if (local instanceof Arvore) {
			preencherDados((Arvore) local);
		} else if (local instanceof Wifi) {
			preencherDados((Wifi) local);
		} else if (local instanceof BocaDeLobo) {
			preencherDados((BocaDeLobo) local);
		}
	}
	
	private void preencherDados(Arvore arvore) {
		addView("Data de cadastro", arvore.getDataCadastro());
		addView("Número da árvore", arvore.getNumero());
		addView("Endereço", arvore.getEndereco());
		addView("Bairro", arvore.getBairro());
		addView("Condição do entorno", arvore.getCondicao());
		addView("Localização do passeio", arvore.getLocalizacaoPasseio());
		addView("Interferência na copa", arvore.getInterferenciaCopa());
		addView("Inclinação do tronco", arvore.getInclinacaoTronco());
	}
	
	private void preencherDados(Wifi wifi) {
		addView("Velocidade fornecida", wifi.getVelocidade());
		addView("Frequência", wifi.getFrequencia());
		addView("Participa do Projeto Praças Digitais desde", wifi.getDataCadastro());
		addView("Bairro", wifi.getBairro());
		addView("Referência", wifi.getReferencia());
	}
	
	private void preencherDados(BocaDeLobo boca) {
		addView("Data de cadastro", boca.getDataCadastro());
		addView("Número ID da boca de lobo", boca.getId());
		addView("Endereço", boca.getEndereco());
		addView("Bairro", boca.getBairro());
		addView("Condição do entorno", boca.getCondicao());
	}
	
	@SuppressLint("DefaultLocale")
	private void addView(String label, String content) {
		final float scale = getActivity().getResources().getDisplayMetrics().density;
		
		TextView tvLabel = new TextView(getActivity());
		tvLabel.setText(label.toUpperCase(Locale.US));
		tvLabel.setTextColor(Color.rgb(0x33, 0x33, 0x33));
		tvLabel.setTypeface(FontUtils.getBold(getActivity()));
		tvLabel.setPadding((int) (15 * scale + 0.5f), 0, 0, 0);
		layout.addView(tvLabel);
		
		TextView tvContent = new TextView(getActivity());
		tvContent.setTypeface(FontUtils.getLight(getActivity()));
		tvContent.setTextColor(Color.rgb(0x33, 0x33, 0x33));
		tvContent.setText(content);
		tvContent.setPadding((int) (15 * scale + 0.5f), 0, (int) (15 * scale + 0.5f), (int) (15 * scale + 0.5f));
		layout.addView(tvContent);
	}
}
