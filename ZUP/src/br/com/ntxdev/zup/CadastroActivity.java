package br.com.ntxdev.zup;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import br.com.ntxdev.zup.util.FontUtils;

public class CadastroActivity extends Activity {

	private Button botaoCancelar;
	private Button botaoCriar;
	
	private EditText campoNome;
	private EditText campoEmail;
	private EditText campoSenha;
	private EditText campoConfirmarSenha;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cadastro);
		
		((TextView) findViewById(R.id.novaConta)).setTypeface(FontUtils.getLight(this));
		((TextView) findViewById(R.id.textView1)).setTypeface(FontUtils.getLight(this));
		((TextView) findViewById(R.id.textView2)).setTypeface(FontUtils.getLight(this));
		
		botaoCancelar = (Button) findViewById(R.id.botaoCancelar);
		botaoCancelar.setTypeface(FontUtils.getRegular(this));
		botaoCriar = (Button) findViewById(R.id.botaoCriar);
		botaoCriar.setTypeface(FontUtils.getRegular(this));
		
		campoNome = (EditText) findViewById(R.id.campoNome);
		campoNome.setTypeface(FontUtils.getLight(this));
		
		campoSenha = (EditText) findViewById(R.id.campoSenha);
		campoSenha.setTypeface(FontUtils.getLight(this));
		
		campoConfirmarSenha = (EditText) findViewById(R.id.campoConfirmarSenha);
		campoConfirmarSenha.setTypeface(FontUtils.getLight(this));
		
		campoEmail = (EditText) findViewById(R.id.campoEmail);
		campoEmail.setTypeface(FontUtils.getLight(this));
	}
}
