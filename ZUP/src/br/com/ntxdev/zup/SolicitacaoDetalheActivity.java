package br.com.ntxdev.zup;

import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import br.com.ntxdev.zup.domain.SolicitacaoListItem;
import br.com.ntxdev.zup.fragment.ImageViewFragment;
import br.com.ntxdev.zup.util.FontUtils;

import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.PageIndicator;

public class SolicitacaoDetalheActivity extends FragmentActivity {

	private SolicitacaoListItem solicitacao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_solicitacao_detalhe);

		solicitacao = (SolicitacaoListItem) getIntent().getExtras().getSerializable("solicitacao");
		
		TextView protocolo = (TextView) findViewById(R.id.protocolo);
		protocolo.setText(getString(R.string.protocolo) + " " + solicitacao.getProtocolo());
		protocolo.setTypeface(FontUtils.getBold(this));
		
		TextView titulo = (TextView) findViewById(R.id.titulo);
		titulo.setText(solicitacao.getTitulo());
		titulo.setTypeface(FontUtils.getLight(this));
		
		TextView data = (TextView) findViewById(R.id.data);
		data.setText(getString(R.string.enviada) + " " + solicitacao.getData());
		data.setTypeface(FontUtils.getBold(this));

		ImagePagerAdapter mAdapter = new ImagePagerAdapter(getSupportFragmentManager());

		ViewPager mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		PageIndicator mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);
		
		TextView comentario = (TextView) findViewById(R.id.comentario);
		comentario.setTypeface(FontUtils.getRegular(this));
		comentario.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi porta a lacus at varius. Suspendisse aliquam nulla eu volutpat sagittis. Aenean nibh diam, fringilla quis arcu in, interdum consequat felis. In vestibulum bibendum varius. Etiam eu pulvinar sem. Vestibulum vel sagittis nisi, ac pulvinar sem.");

		TextView link = (TextView) findViewById(R.id.linkLocal);
		link.setTypeface(FontUtils.getBold(this));
		link.setText(getString(R.string.ver_detalhes_de) + " " + getString(R.string.boca_de_lobo) + " 65564567");
		
		Button botaoVoltar = (Button) findViewById(R.id.botaoVoltar);
		botaoVoltar.setTypeface(FontUtils.getRegular(this));
		botaoVoltar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		TextView indicadorStatus = (TextView) findViewById(R.id.indicadorStatus);
		indicadorStatus.setTypeface(FontUtils.getBold(this));
		switch (solicitacao.getStatus()) {
		case EM_ABERTO:
			indicadorStatus.setText(R.string.em_aberto);
			indicadorStatus.setBackgroundResource(R.drawable.status_red_bg);
			break;
		case EM_ANDAMENTO:
			indicadorStatus.setText(R.string.em_andamento);
			indicadorStatus.setBackgroundResource(R.drawable.status_orange_bg);
			break;
		case RESOLVIDO:
			indicadorStatus.setText(R.string.resolvido);
			indicadorStatus.setBackgroundResource(R.drawable.status_green_bg);
			break;
		case NAO_RESOLVIDO:
			indicadorStatus.setText(R.string.nao_resolvido);
			indicadorStatus.setBackgroundResource(R.drawable.status_gray_bg);
			break;
		}
	}

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
}
