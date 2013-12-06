package br.com.ntxdev.zup;

import br.com.ntxdev.zup.util.FontUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity implements View.OnClickListener {

	private Button botaoCancelar;
	private Button botaoEntrar;
	private EditText campoSenha;
	private EditText campoEmail;
	private TextView linkEsqueciSenha;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		((TextView) findViewById(R.id.login)).setTypeface(FontUtils.getLight(this));
		
		linkEsqueciSenha = (TextView) findViewById(R.id.linkEsqueciSenha);
		linkEsqueciSenha.setTypeface(FontUtils.getBold(this));
		linkEsqueciSenha.setOnClickListener(this);
		
		botaoCancelar = (Button) findViewById(R.id.botaoCancelar);
		botaoCancelar.setTypeface(FontUtils.getRegular(this));
		botaoCancelar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();				
			}
		});
		botaoEntrar = (Button) findViewById(R.id.botaoEntrar);
		botaoEntrar.setTypeface(FontUtils.getRegular(this));
		botaoEntrar.setOnClickListener(this);
		
		campoSenha = (EditText) findViewById(R.id.campoSenha);
		campoSenha.setTypeface(FontUtils.getLight(this));
		
		campoEmail = (EditText) findViewById(R.id.campoEmail);
		campoEmail.setTypeface(FontUtils.getLight(this));
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == linkEsqueciSenha.getId()) {
			startActivity(new Intent(this, RecuperarSenhaActivity.class));
		}
	}
}
