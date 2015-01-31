package com.inodex.inoprod.activities.cableur;

import java.util.Date;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.business.CheminementProvider;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class PositionnementTaTab extends Activity {

	/** Elements à récuperer de la vue */
	private TextView titre, numeroConnecteur, numeroCheminement,
			repereElectrique, zone, positionChariot;
	private ImageButton boutonCheck, infoProduit, retour;
	private GridView gridView, gridView1;

	/** Uri à manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;
	private Uri urlChe = CheminementProvider.CONTENT_URI;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;

	/** Tableau des infos produit */
	private String labels[];

	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateRealisation = new Date();
	private Time heureRealisation = new Time();

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCo, description, clauseTotal;;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION };

	private int layouts1[] = new int[] { R.id.numeroSection,
			R.id.typeSupportAboutissant, R.id.numeroSegregation,
			R.id.zoneLocalisation };

	private int layouts2[] = new int[] { R.id.numeroSection,
			R.id.localisationZone, R.id.typeSupportAboutissant,
			R.id.numeroSegregation, R.id.zoneLocalisation };

	private String colRac1[] = new String[] {
			Raccordement.NUMERO_SECTION_CHEMINEMENT,
			Raccordement.TYPE_RACCORDEMENT_ABOUTISSANT,
			Raccordement.NUMERO_COMPOSANT_TENANT, Raccordement.ZONE_ACTIVITE,
			Raccordement._id, Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_OPERATION,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.NUMERO_CHEMINEMENT };

	private String colRac2[] = new String[] {
			Raccordement.NUMERO_SECTION_CHEMINEMENT,
			Raccordement.TYPE_RACCORDEMENT_ABOUTISSANT,
			 Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.ZONE_ACTIVITE, Raccordement._id,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_OPERATION,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.NUMERO_CHEMINEMENT };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_positionnement_ta_tab);
		// Récupération des éléments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();

		// Récuperation des éléments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		gridView1 = (GridView) findViewById(R.id.GridView01);
		titre = (TextView) findViewById(R.id.textView1);
		numeroConnecteur = (TextView) findViewById(R.id.textView3);

		repereElectrique = (TextView) findViewById(R.id.textView5);
		numeroCheminement = (TextView) findViewById(R.id.textView7);
		zone = (TextView) findViewById(R.id.textView6);
		positionChariot = (TextView) findViewById(R.id.textView4);
		retour = (ImageButton) findViewById(R.id.imageButton2);
		boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);

		// Récuperation du numéro d'opération courant
		clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
		cursor = cr.query(urlSeq, columnsSeq, clause, null, Operation._id
				+ " ASC");
		if (cursor.moveToFirst()) {
			numeroOperation = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
			description = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
			numeroCo = (cursor.getString(cursor
					.getColumnIndex(Operation.RANG_1_1))).substring(11, 14);
			numeroConnecteur.append(" : " + numeroCo);
		}
		
		clause = new String(Raccordement.NUMERO_COMPOSANT_TENANT + "='"
				+ numeroCo + "' GROUP BY "
				+ Raccordement.NUMERO_COMPOSANT_TENANT);

		cursorA = cr.query(urlRac, colRac1, clause, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {

			positionChariot
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.NUMERO_POSITION_CHARIOT)));
			repereElectrique
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT)));
		}

		// Affichage du contenu
		displayContentProvider();

		// Etape suivante

		// Etape suivante
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				indiceCourant++;
				String nextOperation = null;
				try {
					int test = opId[indiceCourant];

					Intent toNext = new Intent(PositionnementTaTab.this,
							CheminementTa.class);

					toNext.putExtra("opId", opId);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);

				} catch (ArrayIndexOutOfBoundsException e) {
					Intent toNext = new Intent(PositionnementTaTab.this,
							MainMenuCableur.class);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("opId", opId);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);

				}
			}

		});


		// Info Produit
	}

	private void displayContentProvider() {
		// Création du SimpleCursorAdapter affilié au GridView
		cursor = cr.query(urlRac, colRac1, clause , null, Raccordement._id + " LIMIT 1");
		SimpleCursorAdapter sca1 = new SimpleCursorAdapter(this,
				R.layout.grid_layout_reprise_blindage_ta1, cursor, colRac1,
				layouts1);

		gridView1.setAdapter(sca1);

		cursorA = cr.query(urlRac, colRac2, clause, null, Raccordement._id + " LIMIT 1");
		SimpleCursorAdapter sca2 = new SimpleCursorAdapter(this,
				R.layout.grid_layout_reprise_blindage_ta2, cursor, colRac2,
				layouts2);

		// MAJ Table de sequencement
		contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0] + " "
				+ nomPrenomOperateur[1]);
		contact.put(Operation.DATE_REALISATION, dateRealisation.toGMTString());
		heureRealisation.setToNow();
		contact.put(Operation.HEURE_REALISATION, heureRealisation.toString());
		cr.update(urlSeq, contact, Operation._id + " = ?",
				new String[] { Integer.toString(opId[indiceCourant]) });
		contact.clear();

	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}

}
