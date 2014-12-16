package br.com.lfdb.zup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.PageIndicator;

import br.com.lfdb.zup.domain.SolicitacaoListItem;
import br.com.lfdb.zup.util.FontUtils;
import br.com.lfdb.zup.util.ImageUtils;
import br.com.lfdb.zup.widget.ImagePagerAdapter;
import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SolicitacaoDetalheActivity extends FragmentActivity {

    @InjectView(R.id.categoryIcon)
    ImageView categoryIcon;
    @InjectView(R.id.categoryName)
    TextView categoryName;
    @InjectView(R.id.subcategoryName)
    TextView subcategoryName;

    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_solicitacao_detalhe);
        ButterKnife.inject(this);

        SolicitacaoListItem solicitacao = (SolicitacaoListItem) getIntent().getExtras().getSerializable("solicitacao");
		boolean alterarLabel = getIntent().getExtras().getBoolean("alterar_botao", false);

        TextView protocolo = (TextView) findViewById(R.id.protocolo);
        if (solicitacao.getProtocolo() == null || solicitacao.getProtocolo().equalsIgnoreCase("null")) {
            protocolo.setVisibility(View.GONE);
        } else {
            protocolo.setText(getString(R.string.protocolo) + " " + solicitacao.getProtocolo());
            protocolo.setTypeface(FontUtils.getBold(this));
        }

		TextView endereco = (TextView) findViewById(R.id.endereco);
        endereco.setText(solicitacao.getEndereco());
        endereco.setTypeface(FontUtils.getLight(this));

        TextView referencia = (TextView) findViewById(R.id.referencia);
        referencia.setText(solicitacao.getReferencia());
        referencia.setTypeface(FontUtils.getLight(this));

		TextView data = (TextView) findViewById(R.id.data);
		data.setText(getString(R.string.enviada) + " " + solicitacao.getData());
		data.setTypeface(FontUtils.getBold(this));

		ViewPager mPager = (ViewPager) findViewById(R.id.pager);
		if (solicitacao.getFotos().isEmpty()) {
			mPager.setVisibility(View.GONE);
            findViewById(R.id.indicator).setVisibility(View.GONE);

            GoogleMap map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            map.getUiSettings().setAllGesturesEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);

            CameraPosition p = new CameraPosition.Builder().target(new LatLng(solicitacao.getLatitude(),
                    solicitacao.getLongitude())).zoom(15).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
            map.moveCamera(update);

            map.addMarker(new MarkerOptions()
                    .position(new LatLng(solicitacao.getLatitude(), solicitacao.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getScaled(this, "reports", solicitacao.getCategoria().getMarcador()))));
		} else {
            findViewById(R.id.map).setVisibility(View.GONE);
			ImagePagerAdapter mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), solicitacao.getFotos());
			mPager.setAdapter(mAdapter);
			PageIndicator mIndicator = (IconPageIndicator) findViewById(R.id.indicator);
			mIndicator.setViewPager(mPager);
		}		

		TextView comentario = (TextView) findViewById(R.id.comentario);
		comentario.setTypeface(FontUtils.getRegular(this));
		comentario.setText(solicitacao.getComentario());

		TextView botaoVoltar = (TextView) findViewById(R.id.botaoVoltar);
		if (alterarLabel) {
			botaoVoltar.setText(R.string.solicitaces_maiusculo);
		}
		botaoVoltar.setTypeface(FontUtils.getRegular(this));
		botaoVoltar.setOnClickListener(v -> finish());

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

        if (solicitacao.getCategoria().getCategoriaMae() != null) {
            categoryIcon.setImageBitmap(ImageUtils.getScaledCustom(this, "reports",
                    solicitacao.getCategoria().getCategoriaMae().getIconeAtivo(), 0.75f));
            categoryName.setText(solicitacao.getCategoria().getNome());
            subcategoryName.setText(solicitacao.getCategoria().getCategoriaMae().getNome());
        } else {
            categoryIcon.setImageBitmap(ImageUtils.getScaledCustom(this, "reports",
                    solicitacao.getCategoria().getIconeAtivo(), 0.75f));
            categoryName.setText(solicitacao.getCategoria().getNome());
            subcategoryName.setVisibility(View.GONE);
        }
	}

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }
}
