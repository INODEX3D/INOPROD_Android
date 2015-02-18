package com.inodex.inoprod.activities.cableur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.inodex.inoprod.R;
import com.inodex.inoprod.business.CheminementProvider;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableCheminement.Cheminement;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

public class CheminementTa extends Activity {

	/** Elements à récuperer de la vue */
	private TextView titre, numeroConnecteur, numeroCheminement,
			repereElectrique, zone, positionChariot;
	private ImageButton boutonCheck, infoProduit, retour;
	private ImageButton petitePause, grandePause;
	private GridView gridView;

	/** Uri à manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;
	private Uri urlChe = CheminementProvider.CONTENT_URI;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;
	private int indiceLimite = 0;
	private int nbRows;

	/** Tableau des infos produit */
	private String labels[];

	/** Liste à afficher dans l'adapteur */
	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();

	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateDebut, dateRealisation;
	private long dureeMesuree = 0;
	private Time heureRealisation = new Time();

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA, cursorB, cursorC;
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
			Operation.DUREE_MESUREE };

	private int layouts[] = new int[] { R.id.numeroRevisionLiaison,
			R.id.numeroFilCable, R.id.typeCable, R.id.connecteurAboutissant,
			R.id.zoneLocalisation, R.id.nombreSection,
			R.id.localisationZonePose, R.id.nombreFilsArrivantTb };

	private String colRac[] = new String[] { Raccordement.NUMERO_REVISION_FIL,
			Raccordement.NUMERO_FIL_CABLE, Raccordement.TYPE_FIL_CABLE,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.ZONE_ACTIVITE, Raccordement._id,
			Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_OPERATION,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.NUMERO_CHEMINEMENT, Raccordement.LOCALISATION1,
			Raccordement.NUMERO_SECTION_CHEMINEMENT,
			Raccordement.NUMERO_REPERE_TABLE_CHEMINEMENT };

	private String colChe[] = new String[] { Cheminement.LOCALISATION1,
			Cheminement.NUMERO_COMPOSANT,
			Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT,
			Cheminement.NUMERO_SECTION_CHEMINEMENT,
			Cheminement.ORDRE_REALISATION, Cheminement.REPERE_ELECTRIQUE,
			Cheminement.ZONE_ACTIVITE, Cheminement._id,
			Cheminement.TYPE_SUPPORT

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cheminement_ta);
		// Initialisation du temps
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

		repereElectrique = (TextView) findViewById(R.id.textView5);
		numeroCheminement = (TextView) findViewById(R.id.textView7);
		zone = (TextView) findViewById(R.id.textView6);
		positionChariot = (TextView) findViewById(R.id.textView4);
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
			numeroCo = (cursor.getString(cursor
					.getColumnIndex(Operation.RANG_1_1))).substring(11, 14);
			numeroConnecteur.append(" : " + numeroCo);
			description = cursor.getString(cursor
					.getColumnIndex(Operation.DESCRIPTION_OPERATION));
		}

		// Recuperation de la première opération
		if (description.contains("Tête A")) {
		clause = new String(Raccordement.NUMERO_COMPOSANT_TENANT + "='"
				+ numeroCo + "'");
		} else {
			clause = new String(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + "='"
					+ numeroCo + "'");
		}
		cursorA = cr.query(urlRac, colRac, clause, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {

			positionChariot
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.NUMERO_POSITION_CHARIOT)));
			zone.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Raccordement.ZONE_ACTIVITE)) +"-" 
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.LOCALISATION1)));

			numeroCheminement.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Raccordement.NUMERO_CHEMINEMENT)));

			if (description.contains("Tête A")) {
				titre.setText(R.string.cheminementTa);
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT)));
				clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='"
						+ numeroCo + "'AND  " + Raccordement.FAUX_CONTACT
						+ "='" + 0 + "' AND " + Raccordement.OBTURATEUR + "='"
						+ 0 + "' AND " + Raccordement.REPRISE_BLINDAGE
						+ " IS NULL ";
			} else {
				titre.setText(R.string.cheminementsCablesTb);
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT)));
				clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
						+ numeroCo + "' AND  " + Raccordement.FAUX_CONTACT
						+ "='" + 0 + "' AND " + Raccordement.OBTURATEUR + "='"
						+ 0 + "' AND " + Raccordement.REPRISE_BLINDAGE
						+ " IS NULL ";
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
								toNext = new Intent(CheminementTa.this,
										PreparationTa.class);
							} else if (nextOperation.startsWith("Reprise")) {
								toNext = new Intent(CheminementTa.this,
										RepriseBlindageTa.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage Enfichage")) {
								toNext = new Intent(CheminementTa.this,
										DenudageSertissageEnfichageTa.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage de")) {
								toNext = new Intent(CheminementTa.this,
										DenudageSertissageContactTa.class);
							} else if (nextOperation.startsWith("Finalisation")) {
								toNext = new Intent(CheminementTa.this,
										FinalisationTa.class);
							} else if (nextOperation.startsWith("Tri")) {
								toNext = new Intent(CheminementTa.this,
										TriAboutissantsTa.class);
							} else if (nextOperation
									.startsWith("Positionnement")) {
								toNext = new Intent(CheminementTa.this,
										PositionnementTaTab.class);
							} else if (nextOperation.startsWith("Cheminement")) {
								toNext = new Intent(CheminementTa.this,
										CheminementTa.class);
							} else if (nextOperation.startsWith("Frettage")) {
								toNext = new Intent(CheminementTa.this,
										Frettage.class);
							}
							if (toNext != null) {

								toNext.putExtra("opId", opId);
								toNext.putExtra("Noms", nomPrenomOperateur);
								toNext.putExtra("Indice", indiceCourant);
								startActivity(toNext);
								finish();
							}

						}

						// Aucune opération suivante: retour au menu principal
					} catch (ArrayIndexOutOfBoundsException e) {
						Intent toNext = new Intent(CheminementTa.this,
								MainMenuCableur.class);
						toNext.putExtra("Noms", nomPrenomOperateur);
						toNext.putExtra("opId", opId);
						toNext.putExtra("Indice", indiceCourant);
						startActivity(toNext);
						finish();

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
						// Si aucun scan détécté, ajout du cable au clavier
					} catch (ActivityNotFoundException e) {
						// entreCable("Impossible de trouver une application pour le scan. Entrez le n° de cable : ");
					}
				}

			}
		});

		// Retour arrière
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

				// MAJ de la durée
				dureeMesuree = 0;
				dateDebut = new Date();

				// MAJ de la clause
				clauseTotal = oldClauseTotal;
				// Vérification de l'état de la production
				prodAchevee = (indiceLimite >= nbRows);
				displayContentProvider();
			}
		});

		// Grande pause
		grandePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						CheminementTa.this);
				builder.setMessage("Êtes-vous sur de vouloir quitter l'application ?");
				builder.setCancelable(false);
				builder.setPositiveButton("Oui",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								/*
								 * Intent toMain = new Intent(
								 * CheminementTa.this, MainActivity.class);
								 * startActivity(toMain);
								 */
								finish();

							}

						});

				builder.setNegativeButton("Non",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {

								dialog.cancel();

							}
						});
				builder.show();

			}
		});

		// Petite Pause
		petitePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dureeMesuree += new Date().getTime() - dateDebut.getTime();
				AlertDialog.Builder builder = new AlertDialog.Builder(
						CheminementTa.this);
				builder.setMessage("L'opération est en pause. Cliquez sur le bouton pour reprendre.");
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

	/**
	 * Affichage du contenu
	 * 
	 */
	private void displayContentProvider() {
		// Création du SimpleAdapter affilié au GridView

		SimpleAdapter sca = new SimpleAdapter(this, liste,
				R.layout.grid_layout_cheminement_ta, colRac, layouts);

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

		// MAJ de la durée
		dureeMesuree = 0;
		dateDebut = new Date();

		// Vérification de l'état de la production
		if (indiceLimite >= nbRows) {
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
				numeroCable = contents;
				clause = Raccordement.NUMERO_FIL_CABLE + "='" + numeroCable
						+ "' AND (" + Raccordement.NUMERO_COMPOSANT_TENANT
						+ "='" + numeroCo + "' OR "
						+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + "='"
						+ numeroCo + "' )";
				cursorA = cr.query(urlRac, colRac, clause, null,
						Raccordement._id);
				if (cursorA.moveToFirst()) {
					HashMap<String, String> element;

					element = new HashMap<String, String>();
					element.put(
							colRac[0],
							""
									+ cursorA.getString(cursorA
											.getColumnIndex(Raccordement.NUMERO_REVISION_FIL)));
					element.put(colRac[1], numeroCable);
					element.put(colRac[2], cursorA.getString(cursorA
							.getColumnIndex(Raccordement.TYPE_FIL_CABLE)));
					/*
					 * element.put(colRac[3], cursorA.getString(cursorA
					 * .getColumnIndex(colRac[3])));
					 */
					cursorB = cr.query(urlRac, colRac,
							Raccordement.NUMERO_FIL_CABLE + "='" + numeroCable
									+ "' AND " + Raccordement.ORDRE_REALISATION
									+ " LIKE '%B%'", null, Raccordement._id);
					if (cursorB.moveToFirst()) {
						String rep = cursorB
								.getString(cursorB
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));
						element.put(colRac[4], rep);
						element.put(
								colRac[5],
								cursorB.getString(cursorB
										.getColumnIndex(Raccordement.ZONE_ACTIVITE))
										+ "-"
										+ cursorB.getString(cursorB
												.getColumnIndex(Raccordement.LOCALISATION1))
										+ "-"
										+ cursorB.getString(cursorB
												.getColumnIndex(Raccordement.NUMERO_REPERE_TABLE_CHEMINEMENT)));

						cursor = cr
								.query(urlRac,
										colRac,
										Raccordement.NUMERO_FIL_CABLE
												+ "='"
												+ numeroCable
												+ "' AND "
												+ Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT
												+ "='" + rep + "' GROUP BY "
												+ Raccordement.NUMERO_FIL_CABLE,
										null, Raccordement._id);
						if (cursor.moveToFirst()) {
							String zonePose = "";
							do {
								int numeroSection = cursor
										.getInt(cursor
												.getColumnIndex(Cheminement.NUMERO_SECTION_CHEMINEMENT));
								Log.e("Cheminement", "" + numeroSection);
								cursorC = cr.query(urlChe, colChe,
										Cheminement.NUMERO_SECTION_CHEMINEMENT
												+ "='" + numeroSection + "'",
										null, Cheminement._id);
								if (cursorC.moveToFirst()) {
									do {
										zonePose += cursorA
												.getString(cursorA
														.getColumnIndex(Cheminement.ZONE_ACTIVITE))
												+ "-"
												+ cursorA
														.getString(cursorA
																.getColumnIndex(Cheminement.LOCALISATION1))
												+ "-"
												+ cursorA
														.getString(cursorA
																.getColumnIndex(Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT))
												+ ", ";
										Log.e("Zone posé", zonePose);
									} while (cursorC.moveToNext());
								}
							} while (cursor.moveToNext());
							element.put(colRac[7], zonePose);

							element.put(colRac[8], "" + cursor.getCount());

						}
					}

					/*
					 * element.put(colRac[6], cursorA.getString(cursorA
					 * .getColumnIndex(colRac[6])));
					 */// AVOIR

					liste.add(element);

					// Ajout du cable à la liste des
					// éléments à afficher

					indiceLimite++;
					displayContentProvider();
					indiceCourant++;
				} else {
					// Le cable n'est pas utilisé pour
					// ce connecteur
					Toast.makeText(CheminementTa.this,
							"Ce cable ne correspond pas", Toast.LENGTH_SHORT)
							.show();
				}
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
			} else if (resultCode == RESULT_CANCELED) {

				entreCable("Echec du scan. Entrez le n° de cable :");
			}
		}

	}

	/**
	 * Entrée du numéro de cable au clavier dans une dialog box
	 * 
	 * @param message
	 *            à afficher dans la dialog box
	 */
	public void entreCable(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				CheminementTa.this);
		builder.setMessage(message);
		builder.setCancelable(false);
		final EditText cable = new EditText(CheminementTa.this);
		builder.setView(cable);
		builder.setPositiveButton("Valider",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// Recherche du cable entré
						numeroCable = cable.getText().toString();
						Log.e("N°Cable", numeroCable);

						clause = Raccordement.NUMERO_FIL_CABLE + "='"
								+ numeroCable + "' AND ("
								+ Raccordement.NUMERO_COMPOSANT_TENANT + "='"
								+ numeroCo + "' OR "
								+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
								+ "='" + numeroCo + "' )";
						cursorA = cr.query(urlRac, colRac, clause, null,
								Raccordement._id);
						if (cursorA.moveToFirst()) {
							HashMap<String, String> element;

							element = new HashMap<String, String>();
							element.put(
									colRac[0],
									""
											+ cursorA.getString(cursorA
													.getColumnIndex(Raccordement.NUMERO_REVISION_FIL)));
							element.put(colRac[1], numeroCable);
							element.put(colRac[2], cursorA.getString(cursorA
									.getColumnIndex(Raccordement.TYPE_FIL_CABLE)));
							/*
							 * element.put(colRac[3], cursorA.getString(cursorA
							 * .getColumnIndex(colRac[3])));
							 */
							cursorB = cr.query(urlRac, colRac,
									Raccordement.NUMERO_FIL_CABLE + "='" + numeroCable
											+ "' AND " + Raccordement.ORDRE_REALISATION
											+ " LIKE '%B%'", null, Raccordement._id);
							if (cursorB.moveToFirst()) {
								String rep = cursorB
										.getString(cursorB
												.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));
								element.put(colRac[3], rep);
								
								// Signalement du point de controle
								clause = Operation.DESCRIPTION_OPERATION + " LIKE '%" + rep + "%' AND ("
										+ Operation.GAMME
										+ " LIKE 'Cheminement%' OR " + Operation.GAMME + " LIKE 'Frett%')";
								cursor = cr.query(urlSeq, columnsSeq, clause, null,
										Operation._id);
								if (cursor.moveToFirst()) {
									
									contact.put(Operation.REALISABLE, 1);
									int id = cursor.getInt(cursor.getColumnIndex(Operation._id));
									cr.update(urlSeq, contact,clause , null);
									contact.clear();
								}
								
								
								element.put(
										colRac[4],
										cursorB.getString(cursorB
												.getColumnIndex(Raccordement.ZONE_ACTIVITE))
												+ "-"
												+ cursorB.getString(cursorB
														.getColumnIndex(Raccordement.LOCALISATION1))
												+ "-"
												+ cursorB.getString(cursorB
														.getColumnIndex(Raccordement.NUMERO_REPERE_TABLE_CHEMINEMENT)));

								cursor = cr
										.query(urlRac,
												colRac,
												Raccordement.NUMERO_FIL_CABLE
														+ "='"
														+ numeroCable
														+ "' AND "
														+ Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT
														+ "='" + rep + "' GROUP BY "
														+ Raccordement.NUMERO_FIL_CABLE,
												null, Raccordement._id);
								if (cursor.moveToFirst()) {
									String zonePose = "";
									do {
										int numeroSection = cursor
												.getInt(cursor
														.getColumnIndex(Cheminement.NUMERO_SECTION_CHEMINEMENT));
										Log.e("Cheminement", "" + numeroSection);
										cursorC = cr.query(urlChe, colChe,
												Cheminement.NUMERO_SECTION_CHEMINEMENT
														+ "='" + numeroSection + "'",
												null, Cheminement._id);
										if (cursorC.moveToFirst()) {
											do {
												zonePose += cursorC
														.getString(cursorC
																.getColumnIndex(Cheminement.ZONE_ACTIVITE))
														+ "-"
														+ cursorC
																.getString(cursorC
																		.getColumnIndex(Cheminement.LOCALISATION1))
														+ "-"
														+ cursorC
																.getString(cursorC
																		.getColumnIndex(Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT))
														+ ", ";
												Log.e("Zone posé", zonePose);
											} while (cursorC.moveToNext());
										}
									} while (cursor.moveToNext());
									element.put(colRac[6], zonePose);

									element.put(colRac[7], "" + cursor.getCount());

								}
							}

							/*
							 * element.put(colRac[6], cursorA.getString(cursorA
							 * .getColumnIndex(colRac[6])));
							 */// AVOIR

							liste.add(element);

							// Ajout du cable à la liste des
							// éléments à afficher

							indiceLimite++;
							displayContentProvider();
							indiceCourant++;
						} else {
							// Le cable n'est pas utilisé pour
							// ce connecteur
							Toast.makeText(CheminementTa.this,
									"Ce cable ne correspond pas",
									Toast.LENGTH_SHORT).show();
						}
					}

				});

		builder.setNegativeButton("Annuler",
				new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {

						dialog.cancel();

					}
				});

		builder.show();
	}

}
