package com.inodex.inoprod.activities.magasiniers;

import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.inodex.inoprod.R;
import com.inodex.inoprod.business.BOMProvider;
import com.inodex.inoprod.business.CheminementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableBOM.BOM;
import com.inodex.inoprod.business.TableSequencement.Operation;

/**
 * Ecran permettant la saisie de la référence et du numéro de lot d'un composant
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class SaisieTracabiliteComposant extends Activity {

	/** Uri à manipuler */
	private Uri urlBOM = BOMProvider.CONTENT_URI;
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlChem = CheminementProvider.CONTENT_URI;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;

	/** Numero de débit courant */
	private int numeroDebit;
	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateRealisation = new Date();
	private Time heureRealisation = new Time();

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION };

	private String columnsBOM[] = new String[] { BOM._id, BOM.NUMERO_DEBIT,
			BOM.NUMERO_COMPOSANT, BOM.NUMERO_OPERATION, BOM.REFERENCE_INTERNE,
			BOM.REPERE_ELECTRIQUE_TENANT, BOM.REFERENCE_FABRICANT2,
			BOM.NUMERO_LOT_SCANNE, BOM.REFERENCE_FABRICANT_SCANNE };

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation;

	/** Elements à récuperer de la vue */
	private ImageButton infoButton, exitButton, boutonCheck;
	private EditText referenceArticle, numeroLot;
	private TextView operation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_saisie_tracabilite_composant);

		// Récupération des éléments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();

		// Récuperation des éléments de la vue
		referenceArticle = (EditText) findViewById(R.id.editText1);
		numeroLot = (EditText) findViewById(R.id.editText2);
		operation = (TextView) findViewById(R.id.textView3);

		// Récuperation du numéro d'opération courant
		clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
		cursor = cr.query(urlSeq, columnsSeq, clause, null, null);
		if (cursor.moveToFirst()) {
			operation.setText(cursor.getString(cursor
					.getColumnIndex(Operation.RANG_1_1)));
			numeroOperation = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
		}

		// Récupération du numéro de débit
		clause = new String(BOM.NUMERO_OPERATION + "='" + numeroOperation + "'");
		cursorA = cr.query(urlBOM, columnsBOM, clause, null, null);
		if (cursorA.moveToFirst()) {
			numeroDebit = cursorA.getInt(cursorA
					.getColumnIndex(BOM.NUMERO_DEBIT));
		} else {
			Toast.makeText(this, "Debit non trouvée", Toast.LENGTH_LONG).show();
		}

		contact = new ContentValues();

		// Validation de la saisie
		boutonCheck = (ImageButton) findViewById(R.id.imageButton1);
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Vérification des champs remplis
				if ((numeroLot.getText().length() == 0)
						|| (referenceArticle.getText().length() == 0)) {
					Toast.makeText(SaisieTracabiliteComposant.this,
							"Veuillez saisir les champs manquants ",
							Toast.LENGTH_LONG).show();
				} else {
					// MAJ de la table BOM
					contact.put(BOM.NUMERO_LOT_SCANNE, numeroLot.getText()
							.toString());
					contact.put(BOM.REFERENCE_FABRICANT_SCANNE,
							referenceArticle.getText().toString());
					cr.update(urlBOM, contact, BOM.NUMERO_DEBIT + " = ?",
							new String[] { Integer.toString(numeroDebit) });
					contact.clear();

					// Ecran suivant
					Intent toNext = new Intent(SaisieTracabiliteComposant.this,
							ServitudeComposants.class);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("opId", opId);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);
				}

			}

		});
		
		// Suppression de la saisie
				exitButton = (ImageButton) findViewById(R.id.exitButton1);
				exitButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						referenceArticle.setText("");
						numeroLot.setText("");
					}
				});
				

	}
	
	
	/**Bloquage du bouton retour */
	public void onBackPressed() {

	}
}
