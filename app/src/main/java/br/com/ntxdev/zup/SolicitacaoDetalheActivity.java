package br.com.ntxdev.zup;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;
import br.com.ntxdev.zup.domain.SolicitacaoListItem;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.util.ImageUtils;
import br.com.ntxdev.zup.widget.ImagePagerAdapter;

import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.PageIndicator;

public class SolicitacaoDetalheActivity extends FragmentActivity {

	private SolicitacaoListItem solicitacao;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_solicitacao_detalhe);

		solicitacao = (SolicitacaoListItem) getIntent().getExtras().getSerializable("solicitacao");
		boolean alterarLabel = getIntent().getExtras().getBoolean("alterar_botao", false);

		TextView protocolo = (TextView) findViewById(R.id.protocolo);
		protocolo.setText(getString(R.string.protocolo) + " " + solicitacao.getProtocolo());
		protocolo.setTypeface(FontUtils.getBold(this));

		TextView titulo = (TextView) findViewById(R.id.titulo);
		titulo.setText(solicitacao.getTitulo());
		titulo.setTypeface(FontUtils.getLight(this));

        TextView endereco = (TextView) findViewById(R.id.endereco);
        endereco.setText(solicitacao.getEndereco());
        endereco.setTypeface(FontUtils.getLight(this));

		TextView data = (TextView) findViewById(R.id.data);
		data.setText(getString(R.string.enviada) + " " + solicitacao.getData());
		data.setTypeface(FontUtils.getBold(this));

		ViewPager mPager = (ViewPager) findViewById(R.id.pager);
		if (solicitacao.getFotos().isEmpty()) {
			mPager.setVisibility(View.GONE);
		} else {
			ImagePagerAdapter mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), solicitacao.getFotos());
			mPager.setAdapter(mAdapter);
			PageIndicator mIndicator = (IconPageIndicator) findViewById(R.id.indicator);
			mIndicator.setViewPager(mPager);
		}		

		TextView comentario = (TextView) findViewById(R.id.comentario);
		comentario.setTypeface(FontUtils.getRegular(this));
		comentario.setText(solicitacao.getComentario());

		TextView link = (TextView) findViewById(R.id.linkLocal);
		link.setTypeface(FontUtils.getBold(this));
		link.setText(getString(R.string.ver_detalhes_de) + " " + getString(R.string.boca_de_lobo) + " 65564567");

		TextView botaoVoltar = (TextView) findViewById(R.id.botaoVoltar);
		if (alterarLabel) {
			botaoVoltar.setText(R.string.solicitaces_maiusculo);
		}
		botaoVoltar.setTypeface(FontUtils.getRegular(this));
		botaoVoltar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		TextView indicadorStatus = (TextView) findViewById(R.id.indicadorStatus);
		indicadorStatus.setTypeface(FontUtils.getBold(this));
		int fiveDp = (int) ImageUtils.dpToPx(this, 5);
		int tenDp = (int) ImageUtils.dpToPx(this, 10);
		indicadorStatus.setPadding(tenDp, fiveDp, tenDp, fiveDp);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			indicadorStatus.setBackgroundDrawable(ImageUtils.getStatusBackground(this, solicitacao.getStatus().getCor()));
		} else {
			indicadorStatus.setBackground(ImageUtils.getStatusBackground(this, solicitacao.getStatus().getCor()));
		}
		indicadorStatus.setText(solicitacao.getStatus().getNome());
	}
}
