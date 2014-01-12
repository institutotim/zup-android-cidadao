package br.com.ntxdev.zup;

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;
import br.com.ntxdev.zup.service.LoginService;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.widget.ImagePagerAdapter;

import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.PageIndicator;

public class OpeningActivity extends FragmentActivity {
	
	private static final int LOGIN_REQUEST = 1010;
	private static final int REGISTER_REQUEST = 1011;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_opening);
		
		ImagePagerAdapter mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), 
				Arrays.asList(R.drawable.tour_img1, R.drawable.tour_img2, R.drawable.tour_img3,
						R.drawable.tour_img4, R.drawable.tour_img5));

		ViewPager mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		PageIndicator mIndicator = (IconPageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);
		
		((TextView) findViewById(R.id.labelNaoPossuiConta)).setTypeface(FontUtils.getRegular(this));
		((TextView) findViewById(R.id.labelPossuiConta)).setTypeface(FontUtils.getRegular(this));
		
		TextView linkPular = (TextView) findViewById(R.id.linkPular);
		linkPular.setTypeface(FontUtils.getBold(this));
		linkPular.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(OpeningActivity.this, MainActivity.class));
				finish();
			}			
		});
		
		TextView botaoCadastrar = (TextView) findViewById(R.id.botaoCadastrar);
		botaoCadastrar.setTypeface(FontUtils.getRegular(this));
		botaoCadastrar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(OpeningActivity.this, CadastroActivity.class), REGISTER_REQUEST);
			}			
		});
		
		TextView botaoLogin = (TextView) findViewById(R.id.botaoLogin);
		botaoLogin.setTypeface(FontUtils.getRegular(this));
		botaoLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(OpeningActivity.this, LoginActivity.class), LOGIN_REQUEST);
			}			
		});
		
		if (new LoginService().usuarioLogado(this)) {
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ((requestCode == LOGIN_REQUEST || requestCode == REGISTER_REQUEST) && resultCode == Activity.RESULT_OK) {
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
	}
}
