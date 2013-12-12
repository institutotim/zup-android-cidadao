package br.com.ntxdev.zup;

import java.util.Arrays;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import br.com.ntxdev.zup.domain.Arvore;
import br.com.ntxdev.zup.domain.BocaDeLobo;
import br.com.ntxdev.zup.domain.Local;
import br.com.ntxdev.zup.domain.Wifi;
import br.com.ntxdev.zup.fragment.ComentariosFragment;
import br.com.ntxdev.zup.fragment.InformacoesFragment;
import br.com.ntxdev.zup.fragment.SolicitacoesFragment;
import br.com.ntxdev.zup.util.FontUtils;

public class DetalheMapaActivity extends FragmentActivity implements View.OnClickListener {

	private InformacoesFragment infoFragment;
	private ComentariosFragment comFragment;
	private SolicitacoesFragment solFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detalhe_mapa);
		
		TextView titulo = (TextView) findViewById(R.id.titulo);
		titulo.setText(getIntent().getStringExtra("title"));
		titulo.setTypeface(FontUtils.getLight(this));
		
		TextView voltar = (TextView) findViewById(R.id.botaoVoltar);
		voltar.setTypeface(FontUtils.getRegular(this));
		voltar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();				
			}
		});		
		
		TextView informacoes = (TextView) findViewById(R.id.botaoInformacoes);
		informacoes.setTypeface(FontUtils.getLight(this));
		informacoes.setOnClickListener(this);
		
		TextView solicitacoes = (TextView) findViewById(R.id.botaoSolicitacoes);
		solicitacoes.setTypeface(FontUtils.getLight(this));
		solicitacoes.setOnClickListener(this);
		
		TextView comentarios = (TextView) findViewById(R.id.botaoComentarios);
		comentarios.setTypeface(FontUtils.getLight(this));
		comentarios.setOnClickListener(this);
		

		infoFragment = new InformacoesFragment();
		infoFragment.setArguments(generateData());
		solFragment = new SolicitacoesFragment();
		comFragment = new ComentariosFragment();
		
		getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, solFragment).add(R.id.fragments_place, infoFragment).add(R.id.fragments_place, comFragment).commit();
			
		if (getIntent().getStringExtra("title").startsWith("Árvore") || getIntent().getStringExtra("title").startsWith("Praça") || getIntent().getBooleanExtra("info_page", false)) {
			if (!getIntent().getBooleanExtra("info_page", false)) {
				comentarios.setVisibility(View.GONE);
				solicitacoes.setVisibility(View.GONE);
			}
			((TextView) findViewById(R.id.botaoInformacoes)).setTextColor(getResources().getColorStateList(R.color.text_next_color));
			((TextView) findViewById(R.id.botaoSolicitacoes)).setTextColor(getResources().getColorStateList(R.color.text_previous_color));
			((TextView) findViewById(R.id.botaoComentarios)).setTextColor(getResources().getColorStateList(R.color.text_previous_color));
			getSupportFragmentManager().beginTransaction().hide(solFragment).hide(comFragment).show(infoFragment).commit();
		} else {
			getSupportFragmentManager().beginTransaction().show(solFragment).hide(comFragment).hide(infoFragment).commit();
		}
	}

	@Override
	public void onClick(View v) {
		((TextView) findViewById(R.id.botaoInformacoes)).setTextColor(getResources().getColorStateList(R.color.text_previous_color));
		((TextView) findViewById(R.id.botaoSolicitacoes)).setTextColor(getResources().getColorStateList(R.color.text_previous_color));
		((TextView) findViewById(R.id.botaoComentarios)).setTextColor(getResources().getColorStateList(R.color.text_previous_color));
		
		switch (v.getId()) {
		case R.id.botaoInformacoes:
			getSupportFragmentManager().beginTransaction().show(infoFragment).hide(comFragment).hide(solFragment).commit();
			break;
		case R.id.botaoComentarios:
			getSupportFragmentManager().beginTransaction().hide(infoFragment).show(comFragment).hide(solFragment).commit();
			break;
		case R.id.botaoSolicitacoes:
			getSupportFragmentManager().beginTransaction().hide(infoFragment).hide(comFragment).show(solFragment).commit();
			break;
		default:
			return;
		}
		
		((TextView) findViewById(v.getId())).setTextColor(getResources().getColorStateList(R.color.text_next_color));				
	}

	private Bundle generateData() {
		Bundle bundle = new Bundle();
		Local local = null;
		
		if (getIntent().getStringExtra("title").startsWith("Boca")) {
			BocaDeLobo boca = new BocaDeLobo();
			boca.setBairro("Higienópolis");
			boca.setCondicao("Comercial");
			boca.setDataCadastro("10/02/2014");
			boca.setEndereco("Avenida Higienópolis, 748");
			boca.setId("4758268");
			boca.setImagens(Arrays.asList(R.drawable.bocalobo5));
			local = boca;
		} else if (getIntent().getStringExtra("title").startsWith("Árvore")) {
			Arvore arvore = new Arvore();
			arvore.setBairro("Bela Vista");
			arvore.setCondicao("Residencial");
			arvore.setDataCadastro("10/02/2014");
			arvore.setEndereco("Rua Una, 45");
			arvore.setInclinacaoTronco("Menor que 10°");
			arvore.setInterferenciaCopa("Muro");
			arvore.setLocalizacaoPasseio("Centralizada");
			arvore.setNumero("07828-00034-r-1-01");
			arvore.setImagens(Arrays.asList(R.drawable.arvore1));
			local = arvore;
		} else if (getIntent().getStringExtra("title").startsWith("Praça")) {
			Wifi wifi = new Wifi();
			wifi.setBairro("República");
			wifi.setDataCadastro("01/08/2013");
			wifi.setFrequencia("2,4 GHz e 5,0 GHz");
			wifi.setReferencia("Próximo a Biblioteca Mário de Andrade e do Edifício Copan");
			wifi.setVelocidade("Minimo de 512 kbps por usuário");
			wifi.setImagens(Arrays.asList(R.drawable.pracawifi1));
			local = wifi;
		}
		
		bundle.putSerializable("local", local);
		return bundle;
	}
}
