package com.inodex.inoprod.activities.cableur;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.inodex.inoprod.R;
import com.inodex.inoprod.business.NomenclatureProvider;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableCheminement.Cheminement;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

public class FinalisationTa extends Activity {

	/** Elements � r�cuperer de la vue */
	private TextView titre, numeroConnecteur, orientation, repereElectrique,
			etatFinalisation, positionChariot;
	private ImageButton boutonCheck, infoProduit, retour, boutonAide;
	private ImageButton petitePause, grandePause;
	private GridView gridView;

	/** Uri � manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;
	private Uri urlNom = NomenclatureProvider.CONTENT_URI;

	/** Tableau des op�rations � r�aliser */
	private int opId[] = null;

	/** Indice de l'op�ration courante */
	private int indiceCourant = 0;

	/** Tableau des infos produit */
	private String labels[];

	/** Heure et dates � ajouter � la table de s�quencment */
	private Date dateDebut, dateRealisation;
	private long dureeMesuree = 0;
	private Time heureRealisation = new Time();

	/** Nom de l'op�rateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver � utiliser lors des requ�tes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCo, description;

	/** Colonnes utilis�s pour les requ�tes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.DUREE_MESUREE };

	private int layouts[] = new int[] { R.id.designation,
			R.id.referenceFabricant, R.id.referenceInterne, R.id.quantite,
			R.id.unite };

	private String colRac[] = new String[] {
			Raccordement.ACCESSOIRE_COMPOSANT1,
			Raccordement.REFERENCE_ACCESSOIRE_OUTIL_TENANT,
			Raccordement.REFERENCE_OUTIL_TENANT, Raccordement._id,
			Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.ORDRE_REALISATION,
			Raccordement.ORIENTATION_RACCORD_ARRIERE,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.ETAT_FINALISATION_PRISE };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_finalisation_ta);
		// Initialisation du temps
		dateDebut = new Date();

		// R�cup�ration des �l�ments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();

		// R�cuperation des �l�ments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		titre = (TextView) findViewById(R.id.textView1);
		numeroConnecteur = (TextView) findViewById(R.id.textView3);
		boutonAide = (ImageButton) findViewById(R.id.imageButton4);
		retour = (ImageButton) findViewById(R.id.imageButton2);
		boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);
		positionChariot = (TextView) findViewById(R.id.textView7);
		orientation = (TextView) findViewById(R.id.textView4);
		etatFinalisation = (TextView) findViewById(R.id.textView6);
		repereElectrique = (TextView) findViewById(R.id.textView5);
		petitePause = (ImageButton) findViewById(R.id.imageButton1);
		grandePause = (ImageButton) findViewById(R.id.exitButton1);

		// R�cuperation du num�ro d'op�ration courant
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

		if (description.contains("T�te A")) {
			clause = new String(Raccordement.NUMERO_COMPOSANT_TENANT + "='"
					+ numeroCo + "' GROUP BY "
					+ Raccordement.NUMERO_COMPOSANT_TENANT);
			titre.setText(R.string.finalisation_ta);
			/*
			 * repereElectrique .append(" : " + cursorA.getString(cursorA
			 * .getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT)));
			 */
		} else {
			clause = new String(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
					+ "='" + numeroCo + "' GROUP BY "
					+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT);
			titre.setText(R.string.finalisationTb);
			/*
			 * repereElectrique .append(" : " + cursorA.getString(cursorA
			 * .getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT)));
			 */
		}
		cursorA = cr.query(urlRac, colRac, clause, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {

			positionChariot
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.NUMERO_POSITION_CHARIOT)));
			orientation
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.ORIENTATION_RACCORD_ARRIERE)));
			etatFinalisation
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.ETAT_FINALISATION_PRISE)));

		}

		// Affichage du contenu
		displayContentProvider();

		// Etape suivante
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// Signalement du point de controle
				clause = Operation.RANG_1_1 + "='" + numeroCo + "' AND ( "
						+ Operation.DESCRIPTION_OPERATION
						+ " LIKE 'Contr�le r�tention%' OR + "
						+ Operation.DESCRIPTION_OPERATION
						+ " LIKE 'Contr�le final%')";
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
						if (nextOperation.startsWith("Pr�paration")) {
							toNext = new Intent(FinalisationTa.this,
									PreparationTa.class);
						} else if (nextOperation.startsWith("Reprise")) {
							toNext = new Intent(FinalisationTa.this,
									RepriseBlindageTa.class);
						} else if (nextOperation
								.startsWith("Denudage Sertissage Enfichage")) {
							toNext = new Intent(FinalisationTa.this,
									DenudageSertissageEnfichageTa.class);
						} else if (nextOperation
								.startsWith("Denudage Sertissage de")) {
							toNext = new Intent(FinalisationTa.this,
									EnfichagesTa.class);

						} else if (nextOperation.startsWith("Finalisation")) {
							toNext = new Intent(FinalisationTa.this,
									FinalisationTa.class);
						} else if (nextOperation.startsWith("Tri")) {
							toNext = new Intent(FinalisationTa.this,
									TriAboutissantsTa.class);
						} else if (nextOperation.startsWith("Positionnement")) {
							toNext = new Intent(FinalisationTa.this,
									PositionnementTaTab.class);
						} else if (nextOperation.startsWith("Cheminement")) {
							toNext = new Intent(FinalisationTa.this,
									CheminementTa.class);
						} else if (nextOperation.startsWith("Frettage")) {
							toNext = new Intent(FinalisationTa.this,
									Frettage.class);
						}
						if (toNext != null) {

							toNext.putExtra("opId", opId);
							toNext.putExtra("Noms", nomPrenomOperateur);
							toNext.putExtra("Indice", indiceCourant);
							startActivity(toNext);
						}

					}

				} catch (ArrayIndexOutOfBoundsException e) {
					Intent toNext = new Intent(FinalisationTa.this,
							MainMenuCableur.class);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("opId", opId);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);

				}
			}

		});

		// Petite Pause
		petitePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dureeMesuree += new Date().getTime() - dateDebut.getTime();
				AlertDialog.Builder builder = new AlertDialog.Builder(
						FinalisationTa.this);
				builder.setMessage("L'op�ration est en pause. Cliquez sur le bouton pour reprendre.");
				builder.setCancelable(false);

				builder.setNegativeButton("Retour",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {

								dateDebut = new Date();
								dialog.cancel();

							}
						});
				builder.show();

			}
		});

	}

	private void displayContentProvider() {
		// Cr�ation du SimpleCursorAdapter affili� au GridView
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.grid_layout_finalisation_ta, cursorA, colRac, layouts);

		gridView.setAdapter(sca);
		// MAJ Table de sequencement
		dateRealisation = new Date();
		contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0] + " "
				+ nomPrenomOperateur[1]);
		contact.put(Operation.DATE_REALISATION, dateRealisation.toGMTString());
		heureRealisation.setToNow();
		contact.put(Operation.HEURE_REALISATION, heureRealisation.toString());
		dureeMesuree += dateRealisation.getTime() - dateDebut.getTime();
		contact.put(Operation.DUREE_MESUREE, dureeMesuree / 1000);
		cr.update(urlSeq, contact, Operation._id + " = ?",
				new String[] { Integer.toString(opId[indiceCourant]) });
		contact.clear();

		// MAJ de la dur�e
		dureeMesuree = 0;
		dateDebut = new Date();
	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}
}
