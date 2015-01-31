package com.inodex.inoprod.activities.cableur;

import java.util.Date;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class DenudageSertissageContactTa extends Activity {

	/** Elements � r�cuperer de la vue */
	private TextView titre, numeroConnecteur, repereElectrique, longueur,
			gainage, positionChariot, instruction;
	private ImageButton boutonCheck, infoProduit, retour, boutonAide;
	private GridView gridView;
	private Button scan;

	/** Uri � manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;

	/** Tableau des op�rations � r�aliser */
	private int opId[] = null;

	/** Indice de l'op�ration courante */
	private int indiceCourant = 0;
	private int indiceLimite = 0;
	private int nbRows;

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

	private String clause, numeroOperation, numeroCo, clauseTotal,
			oldClauseTotal, numeroCable;
	private boolean prodAchevee;

	/** Colonnes utilis�s pour les requ�tes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.REALISABLE };

	private int layouts[] = new int[] { R.id.statutLiaison,
			R.id.numeroRevisionLiaison, R.id.typeCable, R.id.numeroFil,
			R.id.numeroFilDansCable, R.id.couleurFil,
			R.id.referenceFabricantContact, R.id.referenceInterneContact,
			R.id.referencePince, R.id.numeroSeriePince, R.id.reglagePince,
			R.id.referencePositionneur };

	private String colRac[] = new String[] { Raccordement.ETAT_LIAISON_FIL,
			Raccordement.NUMERO_REVISION_FIL, Raccordement.TYPE_FIL_CABLE,
			Raccordement.NUMERO_FIL_CABLE, Raccordement.NUMERO_FIL_DANS_CABLE,
			Raccordement.COULEUR_FIL, Raccordement.REFERENCE_FABRICANT2,
			Raccordement.REFERENCE_INTERNE,
			Raccordement.REFERENCE_OUTIL_TENANT,
			Raccordement.NUMERO_SERIE_OUTIL, Raccordement.REGLAGE_OUTIL_TENANT,
			Raccordement.REFERENCE_ACCESSOIRE_OUTIL_TENANT, Raccordement._id,
			Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.NUMERO_OPERATION,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.LONGUEUR_FIL_CABLE, Raccordement.ORDRE_REALISATION };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_denudage_sertissage_contact_ta);
		// R�cup�ration des �l�ments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();

		// initialisation de la production
		prodAchevee = false;

		// R�cuperation des �l�ments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		titre = (TextView) findViewById(R.id.textView1);
		numeroConnecteur = (TextView) findViewById(R.id.textView3);
		repereElectrique = (TextView) findViewById(R.id.textView5);
		gainage = (TextView) findViewById(R.id.textView5a);
		positionChariot = (TextView) findViewById(R.id.textView5b);
		longueur = (TextView) findViewById(R.id.textView5c);
		instruction = (TextView) findViewById(R.id.textView2);
		boutonAide = (ImageButton) findViewById(R.id.imageButton4);
		retour = (ImageButton) findViewById(R.id.imageButton2);
		boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);

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
		}

		// Recuperation de la premi�re op�ration
		clause = new String(Raccordement.NUMERO_OPERATION + "='"
				+ numeroOperation + "'");
		cursorA = cr.query(urlRac, colRac, clause, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {

			positionChariot
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.NUMERO_POSITION_CHARIOT)));
			longueur.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Raccordement.LONGUEUR_FIL_CABLE)));

			if (numeroOperation.startsWith("4")) {
				titre.setText(R.string.denudageSertissageTa);
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT)));
				clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='"
						+ numeroCo + "' AND ( " + Raccordement.FAUX_CONTACT
						+ "='" + 0 + "' OR " + Raccordement.OBTURATEUR + "='"
						+ 0 + "' OR " + Raccordement.REPRISE_BLINDAGE
						+ " IS NULL )";
			} else {
				titre.setText(R.string.denudageSertissageTa);
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT)));
				clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
						+ numeroCo + "' AND ( " + Raccordement.FAUX_CONTACT
						+ "='" + 0 + "' OR " + Raccordement.OBTURATEUR + "='"
						+ 0 + "' OR " + Raccordement.REPRISE_BLINDAGE
						+ " IS NULL )";
			}

		}

		nbRows = cr.query(urlRac, colRac,
				clause + " GROUP BY " + Raccordement.NUMERO_FIL_CABLE, null,
				Raccordement._id).getCount();
		Log.e("NombreLignes", "" + nbRows);

		// Bouton de validation
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// V�rification de l'�tat de la production
				if (prodAchevee) {

					// Signalement du point de controle
					clause = Operation.RANG_1_1 + "='" + numeroCo + "' AND "
							+ Operation.DESCRIPTION_OPERATION
							+ " LIKE 'Contr�le sertissage%'";
					cursor = cr.query(urlSeq, columnsSeq, clause, null,
							Operation._id);
					if (cursor.moveToFirst()) {
						contact.put(Operation.REALISABLE, 1);
						int id = cursor.getInt(cursor.getColumnIndex(Operation._id));
						cr.update(urlSeq, contact,Operation._id + "='" + id +"'" , null);
						contact.clear();
					}

					indiceCourant++;
					String nextOperation = null;
					// Passage � l'�tape suivante en fonction de sa description
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
								toNext = new Intent(
										DenudageSertissageContactTa.this,
										PreparationTa.class);
							} else if (nextOperation.startsWith("Reprise")) {
								toNext = new Intent(
										DenudageSertissageContactTa.this,
										RepriseBlindageTa.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage Enfichage")) {
								toNext = new Intent(
										DenudageSertissageContactTa.this,
										DenudageSertissageEnfichageTa.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage de")) {
								toNext = new Intent(
										DenudageSertissageContactTa.this,
										DenudageSertissageContactTa.class);
							} else if (nextOperation.startsWith("Enfichage")) {
								toNext = new Intent(
										DenudageSertissageContactTa.this,
										EnfichagesTa.class);
							} else if (nextOperation.startsWith("Finalisation")) {
								toNext = new Intent(
										DenudageSertissageContactTa.this,
										FinalisationTa.class);
							} else if (nextOperation.startsWith("Tri")) {
								toNext = new Intent(
										DenudageSertissageContactTa.this,
										TriAboutissantsTa.class);
							} else if (nextOperation
									.startsWith("Positionnement")) {
								toNext = new Intent(
										DenudageSertissageContactTa.this,
										PositionnementTaTab.class);
							} else if (nextOperation.startsWith("Cheminement")) {
								toNext = new Intent(
										DenudageSertissageContactTa.this,
										CheminementTa.class);
							}
							
							if (toNext != null) {

								toNext.putExtra("opId", opId);
								toNext.putExtra("Noms", nomPrenomOperateur);
								toNext.putExtra("Indice", indiceCourant);
								startActivity(toNext);
							}

						}
						// Aucune op�ration suivante: retour au menu principal
					} catch (ArrayIndexOutOfBoundsException e) {
						Intent toNext = new Intent(
								DenudageSertissageContactTa.this,
								MainMenuCableur.class);
						toNext.putExtra("Noms", nomPrenomOperateur);
						toNext.putExtra("opId", opId);
						toNext.putExtra("Indice", indiceCourant);
						startActivity(toNext);

					}
					// Si production non achev�e
				} else {
					// SCAN du num�ro de cable
					try {
						Intent intent = new Intent(
								"com.google.zxing.client.android.SCAN");
						intent.setPackage("com.google.zxing.client.android");
						intent.putExtra(
								"com.google.zxing.client.android.SCAN.SCAN_MODE",
								"QR_CODE_MODE");
						startActivityForResult(intent, 0);
						// Si aucun scan d�t�ct�, ajout du cable au clavier
					} catch (ActivityNotFoundException e) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								DenudageSertissageContactTa.this);
						builder.setMessage("Impossible de trouver une application pour le scan. Entrez le N� de cable.");
						builder.setCancelable(false);
						final EditText cable = new EditText(
								DenudageSertissageContactTa.this);
						builder.setView(cable);
						builder.setPositiveButton("Valider",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										// Recherche du cable entr�
										numeroCable = cable.getText()
												.toString();
										Log.e("N�Cable", numeroCable);

										clause = Raccordement.NUMERO_FIL_CABLE
												+ "='"
												+ numeroCable
												+ "' AND ("
												+ Raccordement.NUMERO_COMPOSANT_TENANT
												+ "='"
												+ numeroCo
												+ "' OR "
												+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
												+ "='" + numeroCo + "' )";
										cursorA = cr.query(urlRac, colRac,
												clause, null, Raccordement._id);
										if (cursorA.moveToFirst()) {
											if (clauseTotal == null) {
												clauseTotal = Raccordement.NUMERO_FIL_CABLE
														+ "='"
														+ numeroCable
														+ "'";
											} else {
												oldClauseTotal = clauseTotal;
												clauseTotal += " OR "
														+ Raccordement.NUMERO_FIL_CABLE
														+ "='" + numeroCable
														+ "'";
											}
											// Ajout du cable � la liste des
											// �l�ments � afficher

											Log.e("clause", clauseTotal);
											indiceLimite++;
											displayContentProvider();
											indiceCourant++;
										} else {
											// Le cable n'est pas utilis� pour
											// ce connecteur
											Toast.makeText(
													DenudageSertissageContactTa.this,
													"Ce cable ne correspond pas",
													Toast.LENGTH_SHORT).show();

										}
									}

								});

						builder.setNegativeButton("Annuler",
								new DialogInterface.OnClickListener() {
									public void onClick(
											final DialogInterface dialog,
											final int id) {

										dialog.cancel();

									}
								});

						builder.show();
					}

				}
			}
		});

		// Retour arri�re
		retour.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// MAJ des indices
				if (indiceLimite > 0) {
					indiceLimite--;
					Log.e("Indice", "" + indiceLimite);
				}
				if (indiceCourant > 0) {
					indiceCourant--;
					Log.e("Indice", "" + indiceLimite);
				}

				// MAJ de la clause
				clauseTotal = oldClauseTotal;
				// V�rification de l'�tat de la production
				prodAchevee = (indiceLimite >= nbRows);
				displayContentProvider();
			}
		});
	}

	private void displayContentProvider() {
		// Cr�ation du SimpleCursorAdapter affili� au GridView
		cursor = cr.query(urlRac, colRac, Raccordement.NUMERO_COMPOSANT_TENANT
				+ "='" + numeroCo + "' AND (" + clauseTotal + ")", null,
				Raccordement._id);
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.grid_layout_denudage_sertissage_contact_ta, cursor,
				colRac, layouts);

		gridView.setAdapter(sca);
		// MAJ Table de sequencement
		contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0] + " "
				+ nomPrenomOperateur[1]);
		contact.put(Operation.DATE_REALISATION, dateRealisation.toGMTString());
		heureRealisation.setToNow();
		contact.put(Operation.HEURE_REALISATION, heureRealisation.toString());
		cr.update(urlSeq, contact, Operation._id + " = ?",
				new String[] { Integer.toString(opId[indiceCourant]) });
		contact.clear();

		// V�rification de l'�tat de la production
		if (indiceLimite == nbRows) {
			prodAchevee = true;
			Toast.makeText(this, "Production achev�e", Toast.LENGTH_LONG)
					.show();
		}

	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}

	// R�cup�ration du code barre scann�
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				/* ACTION A EFFECTUER */
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(DenudageSertissageContactTa.this,
						"Echec du scan de l'identifiant", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

}
