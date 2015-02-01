package com.inodex.inoprod.activities.cableur;

import java.util.Date;

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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.inodex.inoprod.R;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

public class EnfichagesTa extends Activity {

	/** Elements à récuperer de la vue */
	private TextView titre, numeroConnecteur, designation, repereElectrique,
			referenceInterne, longueur, gainage, positionChariot,
			referenceFabricant;
	private ImageButton boutonCheck, infoProduit, retour, boutonAide;
	private ImageButton petitePause, grandePause;
	private GridView gridView;

	/** Uri à manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;
	private int indiceLimite = 0;
	private int nbRows;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;

	/** Tableau des infos produit */
	private String labels[];

	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateDebut, dateRealisation;
	private long dureeMesuree =0;
	
	private Time heureRealisation = new Time();

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCo, clauseTotal,
			oldClauseTotal, numeroCable, description;
	private boolean prodAchevee;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.REALISABLE , Operation.DUREE_MESUREE};

	private int layouts[] = new int[] { R.id.statutLiaison,
			R.id.numeroRevisionLiaison, R.id.typeCable, R.id.numeroFil,
			R.id.numeroFilDansCable, R.id.couleurFil, R.id.numeroBorne };

	private String colRac[] = new String[] { Raccordement.ETAT_LIAISON_FIL,
			Raccordement.NUMERO_REVISION_FIL, Raccordement.TYPE_FIL_CABLE,
			Raccordement.NUMERO_FIL_CABLE, Raccordement.NUMERO_FIL_DANS_CABLE,
			Raccordement.COULEUR_FIL, Raccordement.NUMERO_BORNE_TENANT,
			Raccordement._id, Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.NUMERO_OPERATION,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.LONGUEUR_FIL_CABLE, Raccordement.REFERENCE_FABRICANT2,
			Raccordement.REFERENCE_INTERNE };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enfichages_ta);
		
		//Initialisation du temps
				dateDebut = new Date();
		// Récupération des éléments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();

		// initialisation de la production
		prodAchevee = false;

		// Récuperation des éléments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		titre = (TextView) findViewById(R.id.textView1);
		numeroConnecteur = (TextView) findViewById(R.id.textView3);
		designation = (TextView) findViewById(R.id.textView3a);
		repereElectrique = (TextView) findViewById(R.id.textView5);
		referenceInterne = (TextView) findViewById(R.id.textView5aa);
		gainage = (TextView) findViewById(R.id.textView5a);
		positionChariot = (TextView) findViewById(R.id.textView5b);
		referenceFabricant = (TextView) findViewById(R.id.textView5c);
		longueur = (TextView) findViewById(R.id.textView5d);
		boutonAide = (ImageButton) findViewById(R.id.imageButton4);
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

		// Affichage du contenu
		// Recuperation de la première opération

		if (description.contains("Tête A")) {
			clause = new String(Raccordement.NUMERO_COMPOSANT_TENANT + "='"
					+ numeroCo + "'");
		} else {
			clause = new String(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
					+ "='" + numeroCo + "'");
		}
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
			referenceFabricant
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.REFERENCE_FABRICANT2)));
			referenceInterne.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Raccordement.REFERENCE_INTERNE)));

			if (numeroOperation.startsWith("4")) {
				titre.setText(R.string.enfichageTa);
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
				titre.setText(R.string.enfichageTb);
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

		// Initialisation du nombre de ligne à atteindre
		nbRows = cr.query(urlRac, colRac,
				clause + " GROUP BY " + Raccordement.NUMERO_FIL_CABLE, null,
				Raccordement._id).getCount();
		Log.e("NombreLignes", "" + nbRows);


		// Bouton de validation
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// Vérification de l'état de la production
				if (prodAchevee) {

					indiceCourant++;
					String nextOperation = null;
					// Passage à l'étape suivante en fonction de sa description
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
								toNext = new Intent(EnfichagesTa.this,
										PreparationTa.class);
							} else if (nextOperation.startsWith("Reprise")) {
								toNext = new Intent(EnfichagesTa.this,
										RepriseBlindageTa.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage Enfichage")) {
								toNext = new Intent(EnfichagesTa.this,
										DenudageSertissageEnfichageTa.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage de")) {
								toNext = new Intent(EnfichagesTa.this,
										EnfichagesTa.class);

							} else if (nextOperation.startsWith("Finalisation")) {
								toNext = new Intent(EnfichagesTa.this,
										FinalisationTa.class);
							} else if (nextOperation.startsWith("Tri")) {
								toNext = new Intent(EnfichagesTa.this,
										TriAboutissantsTa.class);
							} else if (nextOperation
									.startsWith("Positionnement")) {
								toNext = new Intent(EnfichagesTa.this,
										PositionnementTaTab.class);
							} else if (nextOperation.startsWith("Cheminement")) {
								toNext = new Intent(EnfichagesTa.this,
										CheminementTa.class);
							}
							if (toNext != null) {

								toNext.putExtra("opId", opId);
								toNext.putExtra("Noms", nomPrenomOperateur);
								toNext.putExtra("Indice", indiceCourant);
								startActivity(toNext);
							}

						}

						// Aucune opération suivante: retour au menu principal
					} catch (ArrayIndexOutOfBoundsException e) {
						Intent toNext = new Intent(EnfichagesTa.this,
								MainMenuCableur.class);
						toNext.putExtra("Noms", nomPrenomOperateur);
						toNext.putExtra("opId", opId);
						toNext.putExtra("Indice", indiceCourant);
						startActivity(toNext);

					}
					// Si production non achevée
				} else {
					// SCAN du numéro de cable
					try {
						Intent intent = new Intent(
								"com.google.zxing.client.android.SCAN");
						intent.setPackage("com.google.zxing.client.android");
						intent.putExtra(
								"com.google.zxing.client.android.SCAN.SCAN_MODE",
								"QR_CODE_MODE");
						startActivityForResult(intent, 0);
					} catch (ActivityNotFoundException e) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								EnfichagesTa.this);
						builder.setMessage("Impossible de trouver une application pour le scan. Entrez le N° de cable.");
						builder.setCancelable(false);
						final EditText cable = new EditText(EnfichagesTa.this);
						builder.setView(cable);
						builder.setPositiveButton("Valider",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										// Recherche du cable entré
										numeroCable = cable.getText()
												.toString();
										

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
											// Ajout du cable à la liste des
											// éléments à afficher
											indiceLimite++;
											displayContentProvider();
											indiceCourant++;
										} else {
											// Le cable n'est pas utilisé pour
											// ce connecteur
											Toast.makeText(
													EnfichagesTa.this,
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

		// Retour arrière
		retour.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (indiceLimite > 0) {
					indiceLimite--;
					Log.e("Indice", "" + indiceLimite);
				}
				if (indiceCourant > 0) {
					indiceCourant--;
					Log.e("Indice", "" + indiceLimite);
				}
				
				//MAJ de la durée
				dureeMesuree = 0;
				dateDebut= new Date();

				clauseTotal = oldClauseTotal;
				// Vérification de l'état de la production
				prodAchevee = (indiceLimite >= nbRows);
				displayContentProvider();
			}
		});
		
		//Petite Pause
				petitePause.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dureeMesuree += new Date().getTime() - dateDebut.getTime();
						AlertDialog.Builder builder = new AlertDialog.Builder(
								EnfichagesTa.this);
						builder.setMessage("L'opération est en pause. Cliquez sur le bouton pour reprendre.");
						builder.setCancelable(false);

						builder.setNegativeButton("Retour",
								new DialogInterface.OnClickListener() {
									public void onClick(final DialogInterface dialog,
											final int id) {

										dateDebut= new Date();
										dialog.cancel();

									}
								});
						builder.show();

					}
				});
	}

	
	/**
	 * Affichage du contenu
	 * 
	 */
	private void displayContentProvider() {
		// Création du SimpleCursorAdapter affilié au GridView
		cursor = cr.query(urlRac, colRac, Raccordement.NUMERO_COMPOSANT_TENANT
				+ "='" + numeroCo + "' AND (" + clauseTotal + ")", null,
				Raccordement._id);
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.grid_layout_enfichage_ta, cursor, colRac, layouts);

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
		
		//MAJ de la durée
		dureeMesuree = 0;
		dateDebut= new Date();

		// Vérification de l'état de la production
		if (indiceLimite == nbRows) {
			prodAchevee = true;
			Toast.makeText(this, "Production achevée", Toast.LENGTH_LONG)
					.show();
		}

	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}

	/**
	 * Récupération du code barre scanné
	 * 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				/* ACTION A EFFECTUER */
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(EnfichagesTa.this, "Echec du scan ",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}
