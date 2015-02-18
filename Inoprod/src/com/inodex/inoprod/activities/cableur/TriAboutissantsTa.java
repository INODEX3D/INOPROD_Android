package com.inodex.inoprod.activities.cableur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TriAboutissantsTa extends Activity {

	/** Elements à récuperer de la vue */
	private TextView titre, numeroConnecteur, repereElectrique, nombreGroupe,
			positionChariot;
	private ImageButton boutonCheck, infoProduit, retour;
	private ImageButton petitePause, grandePause;
	private GridView gridView;

	/** Uri à manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;
	private int numeroGroupe = 1;

	/** Tableau des infos produit */
	private String labels[];

	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();

	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateDebut, dateRealisation;
	private long dureeMesuree = 0;
	private Time heureRealisation = new Time();

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA, cursorB;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCo, description, clauseTotal;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.REALISABLE, Operation.DUREE_MESUREE };

	private int layouts[] = new int[] { R.id.groupe, R.id.numeroFilCable,
			R.id.typeCable, R.id.numeroSegregation, R.id.connecteurAboutissant,
			R.id.zoneLocalisation, R.id.numeroRoute, R.id.nombreFilArrivantTb };

	private String colRac[] = new String[] {
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_FIL_CABLE, Raccordement.TYPE_FIL_CABLE,
			Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.ZONE_ACTIVITE, Raccordement._id,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.LOCALISATION1 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tri_aboutissants_ta);
		// Initialisation du temps
		dateDebut = new Date();

		// Récupération des éléments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();

		// Récuperation des éléments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		titre = (TextView) findViewById(R.id.textView1);
		numeroConnecteur = (TextView) findViewById(R.id.textView3);
		positionChariot = (TextView) findViewById(R.id.textView7);
		repereElectrique = (TextView) findViewById(R.id.textView5);
		nombreGroupe = (TextView) findViewById(R.id.textView4);
		retour = (ImageButton) findViewById(R.id.imageButton2);
		boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);
		petitePause = (ImageButton) findViewById(R.id.imageButton1);
		grandePause = (ImageButton) findViewById(R.id.exitButton1);

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
				+ numeroCo + "'");

		cursorA = cr.query(urlRac, colRac, clause, null, Raccordement._id
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
			do {
				if (clauseTotal == null) {
					clauseTotal = Raccordement.NUMERO_FIL_CABLE
							+ "='"
							+ cursorA
									.getString(cursorA
											.getColumnIndex(Raccordement.NUMERO_FIL_CABLE))
							+ "'";
				} else {

					clauseTotal += " OR "
							+ Raccordement.NUMERO_FIL_CABLE
							+ "='"
							+ cursorA
									.getString(cursorA
											.getColumnIndex(Raccordement.NUMERO_FIL_CABLE))
							+ "'";
				}
			} while (cursorA.moveToNext());

		}

		cursorA = cr.query(urlRac, colRac, " (" + clauseTotal + ") AND "
				+ Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT + "!='null' GROUP BY " + Raccordement.NUMERO_FIL_CABLE,
				null, Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT);

		if (cursorA.moveToFirst()) {

			String repCourant = cursorA
					.getString(cursorA
							.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));
			numeroGroupe = 1;
			do {
				HashMap<String, String> element;
				if (!(repCourant
						.equals(cursorA.getString(cursorA
								.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT))))) {
					numeroGroupe++;
					repCourant = cursorA
							.getString(cursorA
									.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));
				}

				int nbFils = cr.query(
						urlRac,
						colRac,
						" (" + clauseTotal + ") AND "
								+ Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT
								+ "='" + repCourant + "' GROUP BY "
								+ Raccordement.NUMERO_FIL_CABLE, null,
						Raccordement._id).getCount();

				element = new HashMap<String, String>();
				element.put(colRac[0], Integer.toString(numeroGroupe));
				element.put(colRac[1],
						cursorA.getString(cursorA.getColumnIndex(colRac[1])));
				element.put(colRac[2],
						cursorA.getString(cursorA.getColumnIndex(colRac[2])));
				/*
				 * element.put(colRac[3],
				 * cursorA.getString(cursorA.getColumnIndex(colRac[3])));
				 */
				element.put(colRac[4], repCourant);
				element.put(
						colRac[5],
						cursorA.getString(cursorA.getColumnIndex(colRac[5]))
								+ "-"
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.LOCALISATION1)));
				/*
				 * element.put(colRac[6],
				 * cursorA.getString(cursorA.getColumnIndex(colRac[6])));
				 */
				element.put(colRac[7], "" + nbFils);

				liste.add(element);
			} while (cursorA.moveToNext());
		}

		// Etape suivante
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// MAJ Table de sequencement
				dateRealisation = new Date();
				contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0]
						+ " " + nomPrenomOperateur[1]);
				contact.put(Operation.DATE_REALISATION,
						dateRealisation.toGMTString());
				heureRealisation.setToNow();
				contact.put(Operation.HEURE_REALISATION,
						heureRealisation.toString());
				dureeMesuree += dateRealisation.getTime() - dateDebut.getTime();
				contact.put(Operation.DUREE_MESUREE, dureeMesuree / 1000);
				cr.update(urlSeq, contact, Operation._id + " = ?",
						new String[] { Integer.toString(opId[indiceCourant]) });
				contact.clear();

				// Signalement du point de controle
				clause = Operation.RANG_1_1 + " LIKE '%" + numeroCo
						+ "%' AND ( " + Operation.DESCRIPTION_OPERATION
						+ " LIKE 'Contrôle rétention%' OR "
						+ Operation.DESCRIPTION_OPERATION
						+ " LIKE 'Contrôle final%')";
				cursor = cr.query(urlSeq, columnsSeq, clause, null,
						Operation._id);
				if (cursor.moveToFirst()) {
					do {
						contact.put(Operation.REALISABLE, 1);
						int id = cursor.getInt(cursor
								.getColumnIndex(Operation._id));
						cr.update(urlSeq, contact, Operation._id + "='" + id
								+ "'", null);
						contact.clear();
					} while (cursor.moveToNext());
				}

				indiceCourant++;
				String nextOperation = null;
				try {
					int test = opId[indiceCourant];

					clause = Operation._id + "='" + test + "'";
					cursor = cr.query(urlSeq, columnsSeq, clause, null,
							Operation._id);
					if (cursor.moveToFirst()) {
						nextOperation = cursor.getString(cursor
								.getColumnIndex(Operation.DESCRIPTION_OPERATION));
						Intent toNext = null;
						if (nextOperation.startsWith("Préparation")) {
							toNext = new Intent(TriAboutissantsTa.this,
									PreparationTa.class);
						} else if (nextOperation.startsWith("Reprise")) {
							toNext = new Intent(TriAboutissantsTa.this,
									RepriseBlindageTa.class);
						} else if (nextOperation
								.startsWith("Denudage Sertissage Enfichage")) {
							toNext = new Intent(TriAboutissantsTa.this,
									DenudageSertissageEnfichageTa.class);
						} else if (nextOperation
								.startsWith("Denudage Sertissage de")) {
							toNext = new Intent(TriAboutissantsTa.this,
									EnfichagesTa.class);
						} else if (nextOperation.startsWith("Finalisation")) {
							toNext = new Intent(TriAboutissantsTa.this,
									FinalisationTa.class);
						} else if (nextOperation.startsWith("Tri")) {
							toNext = new Intent(TriAboutissantsTa.this,
									TriAboutissantsTa.class);
						} else if (nextOperation.startsWith("Positionnement")) {
							toNext = new Intent(TriAboutissantsTa.this,
									PositionnementTaTab.class);
						} else if (nextOperation.startsWith("Cheminement")) {
							toNext = new Intent(TriAboutissantsTa.this,
									CheminementTa.class);
						}
						if (toNext != null) {

							toNext.putExtra("opId", opId);
							toNext.putExtra("Noms", nomPrenomOperateur);
							toNext.putExtra("Indice", indiceCourant);
							startActivity(toNext);
							finish();
						}

					}

				} catch (ArrayIndexOutOfBoundsException e) {
					Intent toNext = new Intent(TriAboutissantsTa.this,
							MainMenuCableur.class);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("opId", opId);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);
					finish();

				}
			}

		});

		// Affichage du contenu
		displayContentProvider();

		// Etape suivante

		// Info Produit
	}

	private void displayContentProvider() {

		// Création du SimpleCursorAdapter affilié au GridView

		SimpleAdapter sca = new SimpleAdapter(this, liste,
				R.layout.grid_layout_tri_aboutissants_ta, colRac, layouts);

		gridView.setAdapter(sca);

	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}
}
