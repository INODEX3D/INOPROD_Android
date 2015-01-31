package com.inodex.inoprod.activities.cableur;

import java.util.Date;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

public class MiseLongueurTb extends Activity {

	private ImageButton boutonCheck, infoProduit;

	/** Elements � r�cuperer de la vue */
	private TextView numeroConnecteur, repereElectrique;

	/** Uri � manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;

	/** Tableau des op�rations � r�aliser */
	private int opId[] = null;

	/** Indice de l'op�ration courante */
	private int indiceCourant = 0;

	/** Tableau des infos produit */
	private String labels[];

	/** Heure et dates � ajouter � la table de s�quencment */
	private Date dateRealisation = new Date();
	private Time heureRealisation = new Time();

	/** Nom de l'op�rateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver � utiliser lors des requ�tes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCo;

	/** Colonnes utilis�s pour les requ�tes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION };

	private int layouts[] = new int[] { R.id.designation,
			R.id.referenceFabricant, R.id.referenceInterne, R.id.numeroBorne, };

	private String colRac[] = new String[] { Raccordement.DESIGNATION,
			Raccordement.REFERENCE_FABRICANT2, Raccordement.REFERENCE_INTERNE,
			Raccordement.NUMERO_BORNE_TENANT, Raccordement._id,
			Raccordement.OBTURATEUR, Raccordement.FAUX_CONTACT,
			Raccordement.NUMERO_OPERATION,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.NUMERO_BORNE_ABOUTISSANT };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mise_longueur_tb);
		// R�cup�ration des �l�ments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");

		cr = getContentResolver();
		contact = new ContentValues();

		// R�cuperation des �l�ments de la vue

		numeroConnecteur = (TextView) findViewById(R.id.textView3);

		boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);

		repereElectrique = (TextView) findViewById(R.id.textView5);

		// R�cuperation du num�ro d'op�ration courant
		clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
		cursor = cr.query(urlSeq, columnsSeq, clause, null, Operation._id
				+ " ASC");
		if (cursor.moveToFirst()) {
			numeroOperation = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
			numeroCo = (cursor.getString(cursor
					.getColumnIndex(Operation.RANG_1_1))).substring(11, 14);
			numeroConnecteur.append(" : " + numeroCo);
			Log.e("Connnecteur", numeroCo);
		}

		// Recuperation de la premi�re op�ration
		clause = new String(Raccordement.NUMERO_OPERATION + "='"
				+ numeroOperation + "'");
		cursorA = cr.query(urlRac, colRac, clause, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {

			repereElectrique
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT)));

		}
		
		// Etape suivante
				boutonCheck.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						indiceCourant++;
						String nextOperation = null;
						try {
							int test = opId[indiceCourant];

							Intent toNext = new Intent(MiseLongueurTb.this,
									RepriseBlindageTa.class);

							toNext.putExtra("opId", opId);
							toNext.putExtra("Noms", nomPrenomOperateur);
							toNext.putExtra("Indice", indiceCourant);
							startActivity(toNext);

						} catch (ArrayIndexOutOfBoundsException e) {
							Intent toNext = new Intent(MiseLongueurTb.this,
									MainMenuCableur.class);
							toNext.putExtra("Noms", nomPrenomOperateur);
							toNext.putExtra("opId", opId);
							toNext.putExtra("Indice", indiceCourant);
							startActivity(toNext);

						}
					}

				});
	}
}
