package com.inodex.inoprod.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inodex.inoprod.R;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableSequencement.Operation;
import com.inodex.inoprod.business.TimeConverter;

/**
 * Acitivité qui affiche les infos relatives au produit en cours d'utilisation
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class InfoProduit extends Activity {

	/** Labels permettant l'affichage des divers données du produit */
	private TextView designationProduit, numeroHarnais, standardFabrication,
			numeroSerie, numeroTraitement, numeroRevisionHarnais,
			referenceFichierSource, dureeMesure, dureeReference, tauxProg;

	/**
	 * Ensemble des données récupérees depuis l'Intent sous forme de chaines de
	 * caractères
	 */
	private String labels[];

	private Cursor cursor, cursorA;
	private ContentResolver cr;

	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.DUREE_MESUREE, Operation.RANG_1_1_1,
			Operation.DUREE_THEORIQUE };

	/** Uri de la table de sequencement */
	private Uri url = SequencementProvider.CONTENT_URI;

	/** Bouton de retour en arrière */
	private ImageButton boutonExit;

	int dRef, dMes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info_produit);

		// Récupération des infos du produit
		Intent intent = getIntent();
		labels = intent.getStringArrayExtra("Labels");

		// Récupération de la vue
		designationProduit = (TextView) findViewById(R.id.textView2);
		numeroHarnais = (TextView) findViewById(R.id.textView5);
		standardFabrication = (TextView) findViewById(R.id.textView6);
		numeroSerie = (TextView) findViewById(R.id.textView2a);
		numeroTraitement = (TextView) findViewById(R.id.textView5a);
		numeroRevisionHarnais = (TextView) findViewById(R.id.textView6a);
		referenceFichierSource = (TextView) findViewById(R.id.textView3);
		dureeReference = (TextView) findViewById(R.id.textView2b);
		dureeMesure = (TextView) findViewById(R.id.textView2c);
		tauxProg = (TextView) findViewById(R.id.textView4);

		// Affichae des infos sur le produit
		designationProduit.append(" : " + labels[0]);
		numeroHarnais.append(" " + labels[1]);
		standardFabrication.append(" " + labels[2]);
		numeroSerie.append(" " + labels[3]);
		numeroTraitement.append(" " + labels[4]);
		numeroRevisionHarnais.append(" : " + labels[5]);
		referenceFichierSource.append(" " + labels[6]);

		// Calcul de ma duree de reference
		dRef = 0;
		dMes = 0;
		cr = getContentResolver();
		cursor = cr.query(url, columnsSeq, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				dRef += cursor.getInt(cursor
						.getColumnIndex(Operation.DUREE_THEORIQUE));
				dMes += cursor.getInt(cursor
						.getColumnIndex(Operation.DUREE_MESUREE));

			} while (cursor.moveToNext());
		}

		dureeMesure.append(" " + TimeConverter.display((long) dMes));
		dureeReference.append(" " + TimeConverter.display((long) dRef));
		tauxProg.append(" "
				+ Float.toString(((float) (dMes * 100) / dRef)).substring(0, 4)
				+ "%");

		// Retour à l'activité précédente
		boutonExit = (ImageButton) findViewById(R.id.exitButton1);
		boutonExit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();

			}
		});

	}
}
