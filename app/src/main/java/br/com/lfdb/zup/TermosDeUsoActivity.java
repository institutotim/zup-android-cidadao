package br.com.lfdb.zup;

import br.com.lfdb.zup.util.FontUtils;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TermosDeUsoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_termos_de_uso);
		
		TextView conteudo = (TextView) findViewById(R.id.texto);
		conteudo.setTypeface(FontUtils.getRegular(this));
		conteudo.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus non consectetur "
				+ "lorem. Nullam sed ullamcorper mauris, ac blandit tellus. Mauris tempor ultrices urna. "
				+ "Pellentesque magna enim, elementum a volutpat non, posuere in risus. Proin varius lacinia "
				+ "nisi. Nulla purus lectus, congue ut aliquam et, accumsan at elit. Sed ut velit ipsum."
				+ " Mauris erat tortor, fermentum sit amet orci placerat, tempor tincidunt lacus. Vestibulum"
				+ " ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Nullam gravida"
				+ "ligula at fringilla blandit. In velit lacus, dapibus sed nibh a, rutrum dictum turpis.\n\nMorbi "
				+ "tristique, mauris id pellentesque pellentesque, augue augue aliquet ante, at dictum dui nunc "
				+ "sed leo. Sed consequat ullamcorper augue non condimentum. Praesent eros tellus, tempor in mauris"
				+ " vel, venenatis volutpat dui. Donec ultrices mi quis scelerisque tincidunt. Praesent in vulputat"
				+ "e leo. Suspendisse volutpat vitae purus vulputate fermentum. Integer lectus metus, tincidunt vel"
				+ " sem ut, congue dictum nunc. Nulla in velit semper, vehicula massa sit amet, bibendum dui. Al"
				+ "iquam consectetur eros orci, nec hendrerit nisl dapibus non. Curabitur id congue lectus, id fe"
				+ "rmentum diam. Nunc pulvinar, metus sed posuere lobortis, erat lacus accumsan libero, quis egesta"
				+ "s tortor erat eget metus. Aliquam porta sem ut sem lacinia, id varius massa varius. Quisque sus"
				+ "cipit venenatis euismod. Duis varius porttitor odio sit amet sollicitudin.\n\nMauris tincidunt ves"
				+ "tibulum tortor. Duis non nibh eget orci venenatis lobortis. Nullam sed sagittis metus. Proin sed "
				+ "justo viverra, adipiscing tellus tristique, laoreet tellus. Integer non interdum lacus, nec variu"
				+ "s nibh. Nulla malesuada elit ac pellentesque posuere. Nullam commodo pulvinar metus ut mollis. Ve"
				+ "stibulum in pretium felis. Vivamus tempus feugiat rhoncus. Pellentesque vitae tortor mauris. Duis"
				+ " tempor tellus dignissim diam ornare, quis sollicitudin tortor pulvinar.\n\nPhasellus vel dolor sed"
				+ " ligula vestibulum ultrices et vel felis. Morbi lacinia varius est id rhoncus. Mauris nec lacinia"
				+ " lectus. Quisque ac urna eget enim imperdiet convallis sit amet placerat massa. Nulla eget eros u"
				+ "t lectus dignissim convallis ut sit amet diam. Donec gravida egestas diam, eu ultricies eros temp"
				+ "or eu. Curabitur vel ante bibendum, ultricies massa eu, laoreet diam. Nullam quis cursus erat. In"
				+ "teger elementum odio eros, non gravida est porttitor vitae.\n\nNam rutrum velit non turpis egestas "
				+ "lacinia. Etiam at lacus egestas, convallis metus eget, dignissim metus. Nam orci leo, hendrerit "
				+ "vel diam quis, mollis congue metus. Cras lorem ligula, lacinia ut vulputate ac, sagittis ac lectu"
				+ "s. Nulla sit amet felis ut lorem gravida tempor. Proin id fringilla ipsum, quis commodo mi. Praes"
				+ "ent at lectus quis eros pretium suscipit ut eget velit. Aenean quis tempus metus. Phasellus laoree"
				+ "t velit metus, in elementum enim vestibulum sed. Ut bibendum fringilla rhoncus. Phasellus porta, e"
				+ "ros sit amet aliquam vehicula, ligula est sollicitudin elit, eget ornare magna augue id magna. Pro"
				+ "in quis magna sagittis, euismod quam eget, aliquet odio.");
	}
}
