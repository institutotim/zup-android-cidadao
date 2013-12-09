package br.com.ntxdev.zup;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import br.com.ntxdev.zup.util.FontUtils;

public class RecuperarSenhaActivity extends Activity implements View.OnClickListener {

	private TextView botaoVoltar;
	private TextView botaoEnviar;
	private EditText campoEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recuperar_senha);

		((TextView) findViewById(R.id.esqueceuSenha)).setTypeface(FontUtils.getLight(this));
		
		botaoVoltar = (TextView) findViewById(R.id.botaoVoltar);
		botaoVoltar.setTypeface(FontUtils.getRegular(this));
		botaoVoltar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();				
			}
		});
		
		botaoEnviar = (TextView) findViewById(R.id.botaoEnviar);
		botaoEnviar.setTypeface(FontUtils.getRegular(this));
		botaoEnviar.setOnClickListener(this);
		
		campoEmail = (EditText) findViewById(R.id.campoEmail);
		campoEmail.setTypeface(FontUtils.getLight(this));
	}

	@Override
	public void onClick(View v) {
				
	}
}
