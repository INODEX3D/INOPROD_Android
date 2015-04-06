package com.inodex.inoprod.activities.cableur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.activities.Inoprod;
import com.inodex.inoprod.business.DureesProvider;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TimeConverter;
import com.inodex.inoprod.business.Durees.Duree;
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
import android.graphics.Color;
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
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class RepriseBlindageTa extends Activity {

	/** Elements à récuperer de la vue */
	private TextView titre, numeroConnecteur, repereElectrique,
			positionChariot, numeroReprise, sensReprise;
	private ImageButton boutonCheck, infoProduit, retour, boutonAide;
	private ImageButton petitePause, grandePause;
	private GridView gridView1, gridView2;

	/** Uri à manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;
	private int indiceLimite = 0;
	private int nbRows;

	/** Tableau des infos produit */
	private String labels[];

	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateDebut, dateRealisation;
	private long dureeMesuree = 0;
	private Time heureRealisation = new Time();

	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA, cursorB;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCable, numeroCo, clauseTotal,
			clauseTotal1, numeroRep, oldClauseTotal;
	private boolean prodAchevee;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.DUREE_MESUREE };

	private int layouts1[] = new int[] { R.id.statutLiaison,
			R.id.numeroRevisionLiaison, R.id.numeroCable, R.id.typeCable,
			R.id.numeroFilReprise, R.id.typeFilReprise,
			R.id.referenceFabricantManchon, R.id.referenceInterneManchon,
			R.id.referenceOutillage, R.id.numeroSerieOutillage,
			R.id.reglageTemperature };

	private String colRac1[] = new String[] { Raccordement.ETAT_LIAISON_FIL,
			Raccordement.NUMERO_REVISION_FIL, Raccordement.NUMERO_FIL_CABLE,
			Raccordement.TYPE_FIL_CABLE, Raccordement.REFERENCE_FABRICANT2,
			Raccordement.REFERENCE_INTERNE,
			Raccordement.REFERENCE_OUTIL_TENANT,
			Raccordement.REFERENCE_ACCESSOIRE_OUTIL_TENANT,
			Raccordement.REGLAGE_OUTIL_TENANT, Raccordement._id,
			Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.ORDRE_REALISATION, Raccordement.NUMERO_OPERATION,
			Raccordement.REPRISE_BLINDAGE, Raccordement.SANS_REPRISE_BLINDAGE,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT };

	private String colInfo[] = new String[] { Raccordement._id,
			Raccordement.DESIGNATION, Raccordement.NUMERO_REVISION_HARNAIS,
			Raccordement.STANDARD, Raccordement.NUMERO_HARNAIS_FAISCEAUX,
			Raccordement.REFERENCE_FICHIER_SOURCE };
	private Cursor cursorInfo;

	private TextView timer;
	private Cursor cursorTime;
	private Uri urlTim = DureesProvider.CONTENT_URI;
	private String colTim[] = new String[] { Duree._id,
			Duree.DESIGNATION_OPERATION, Duree.DUREE_THEORIQUE

	};
	private long dureeTotal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reprise_blindage_ta);

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
		gridView1 = (GridView) findViewById(R.id.gridview);
		titre = (TextView) findViewById(R.id.textView1);
		numeroConnecteur = (TextView) findViewById(R.id.textView3);
		boutonAide = (ImageButton) findViewById(R.id.imageButton4);
		retour = (ImageButton) findViewById(R.id.imageButton2);
		boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);
		positionChariot = (TextView) findViewById(R.id.textView4);
		repereElectrique = (TextView) findViewById(R.id.textView5);
		numeroReprise = (TextView) findViewById(R.id.textView7);
		sensReprise = (TextView) findViewById(R.id.textView6);
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
			Log.e("Connnecteur", numeroCo);
		}

		// Recuperation de la première opération
		clause = new String(Raccordement.NUMERO_OPERATION + "='"
				+ numeroOperation + "'");
		cursorA = cr.query(urlRac, colRac1, clause, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {

			positionChariot
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.NUMERO_POSITION_CHARIOT)));

			if (numeroOperation.startsWith("4")) {
				titre.setText(R.string.repriseBlindageTa);
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT)));
				clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='"
						+ numeroCo + "' AND " + Raccordement.REPRISE_BLINDAGE
						+ "!='" + "null" + "' AND "
						+ Raccordement.REPRISE_BLINDAGE + "!='' GROUP BY "
						+ Raccordement.REPRISE_BLINDAGE;

			} else {
				titre.setText(R.string.repriseBlindageTb);
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT)));
				clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
						+ numeroCo + "' AND " + Raccordement.REPRISE_BLINDAGE
						+ "!='" + "null" + "' AND "
						+ Raccordement.REPRISE_BLINDAGE + "!='' GROUP BY "
						+ Raccordement.REPRISE_BLINDAGE;
				colRac1[6] = Raccordement.REFERENCE_OUTIL_ABOUTISSANT;
				colRac1[7] = Raccordement.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT;
				colRac1[8] = Raccordement.REGLAGE_OUTIL_ABOUTISSANT;
			}

		}

		nbRows = cr.query(urlRac, colRac1, clause, null, Raccordement._id)
				.getCount();
		Log.e("NombreLignes", "" + nbRows);

		// Affichage du temps nécessaire
		timer = (TextView) findViewById(R.id.timeDisp);
		dureeTotal = 0;
		cursorTime = cr.query(urlTim, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%Reprise%' ", null, Duree._id);
		if (cursorTime.moveToFirst()) {
			dureeTotal += TimeConverter.convert(cursorTime.getString(cursorTime
					.getColumnIndex(Duree.DUREE_THEORIQUE)));
			Log.d("Duree", cursorTime.getString(cursorTime
					.getColumnIndex(Duree.DUREE_THEORIQUE)));
			Log.d("Duree",
					""
							+ TimeConverter.convert(cursorTime.getString(cursorTime
									.getColumnIndex(Duree.DUREE_THEORIQUE))));

		}
		dureeTotal = dureeTotal * nbRows;
		timer.setTextColor(Color.GREEN);
		timer.setText(TimeConverter.display(dureeTotal));

		// Scan
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (prodAchevee) {
					// indiceCourant++;
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
								toNext = new Intent(RepriseBlindageTa.this,
										PreparationTa.class);
							} else if (nextOperation.startsWith("Reprise")) {
								toNext = new Intent(RepriseBlindageTa.this,
										RepriseBlindageTa.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage Enfichage")) {
								toNext = new Intent(RepriseBlindageTa.this,
										DenudageSertissageEnfichageTa.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage de")) {
								toNext = new Intent(RepriseBlindageTa.this,
										DenudageSertissageContactTa.class);
							} else if (nextOperation.startsWith("Enfichage")) {
								toNext = new Intent(RepriseBlindageTa.this,
										EnfichagesTa.class);
							} else if (nextOperation.startsWith("Mise")) {
								toNext = new Intent(RepriseBlindageTa.this,
										MiseLongueurTb.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage Coss")) {
								toNext = new Intent(
										RepriseBlindageTa.this,
										DenudageSertissageManchonsCossesTb.class);
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
						Intent toNext = new Intent(RepriseBlindageTa.this,
								MainMenuCableur.class);
						toNext.putExtra("Noms", nomPrenomOperateur);
						toNext.putExtra("opId", opId);
						toNext.putExtra("Indice", indiceCourant);
						startActivity(toNext);
						finish();

					}

				} else {

					try {
						Intent intent = new Intent(
								"com.google.zxing.client.android.SCAN");
						intent.setPackage("com.google.zxing.client.android");
						intent.putExtra(
								"com.google.zxing.client.android.SCAN.SCAN_MODE",
								"QR_CODE_MODE");
						startActivityForResult(intent, 0);
					} catch (ActivityNotFoundException e) {
						// entreCable("Impossible de trouver une application pour le scan. Entrez le N° de cable.");
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
					indiceCourant--;
					Log.e("Indice", "" + indiceLimite);

				}

				// MAJ de la durée
				dureeMesuree = 0;
				dateDebut = new Date();

				clauseTotal = oldClauseTotal;

				if (liste.isEmpty()) {

				} else {
					liste.remove(liste.size() - 1);
				}
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
						RepriseBlindageTa.this);
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

		infoProduit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cursorInfo = cr.query(urlRac, colInfo,
						Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
								+ numeroCo + "' OR "
								+ Raccordement.NUMERO_COMPOSANT_TENANT + "='"
								+ numeroCo + "'", null, null);
				Intent toInfo = new Intent(RepriseBlindageTa.this,
						InfoProduit.class);
				labels = new String[7];

				if (cursorInfo.moveToFirst()) {
					labels[0] = cursorInfo.getString(cursorInfo
							.getColumnIndex(Raccordement.DESIGNATION));
					labels[1] = cursorInfo.getString(cursorInfo
							.getColumnIndex(Raccordement.NUMERO_HARNAIS_FAISCEAUX));
					labels[2] = cursorInfo.getString(cursorInfo
							.getColumnIndex(Raccordement.STANDARD));
					labels[3] = "";
					labels[4] = "";
					labels[5] = cursorInfo.getString(cursorInfo
							.getColumnIndex(Raccordement.NUMERO_REVISION_HARNAIS));
					labels[6] = cursorInfo.getString(cursorInfo
							.getColumnIndex(Raccordement.REFERENCE_FICHIER_SOURCE));
					toInfo.putExtra("Labels", labels);
				}

				startActivity(toInfo);

			}
		});

		// Petite Pause
		petitePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dureeMesuree += new Date().getTime() - dateDebut.getTime();
				AlertDialog.Builder builder = new AlertDialog.Builder(
						RepriseBlindageTa.this);
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

	private void displayContentProvider() {
		// Création du SimpleCursorAdapter affilié au GridView

		SimpleAdapter sca1 = new SimpleAdapter(this, liste,
				R.layout.grid_layout_reprise_blindage_ta1, colRac1, layouts1);

		gridView1.setAdapter(sca1);

		// gridView2.setAdapter(sca2);
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
		if (indiceLimite == nbRows) {
			prodAchevee = true;
			Toast.makeText(this, "Production achevée", Toast.LENGTH_LONG)
					.show();
		}

	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}

	// Récupération du code barre scanné
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				numeroCable = contents;

				clause = Raccordement.NUMERO_FIL_CABLE + "='" + numeroCable
						+ "' AND " + Raccordement.REPRISE_BLINDAGE + "!='"
						+ "null" + "' AND " + Raccordement.REPRISE_BLINDAGE
						+ "!='' ";
				cursorA = cr.query(urlRac, colRac1, clause, null,
						Raccordement._id);
				if (cursorA.moveToFirst()) {

					numeroRep = cursorA.getString(cursorA
							.getColumnIndex(Raccordement.REPRISE_BLINDAGE));
					numeroReprise.setText("Numero Reprise: " + numeroRep);
					sensReprise
							.setText("Sens reprise : "
									+ cursorA.getString(cursorA
											.getColumnIndex(Raccordement.SANS_REPRISE_BLINDAGE)));

					clause = Raccordement.NUMERO_FIL_CABLE + "!='"
							+ numeroCable + "' AND "
							+ Raccordement.REPRISE_BLINDAGE + "='" + numeroRep
							+ "' GROUP BY " + Raccordement.NUMERO_FIL_CABLE;
					cursorB = cr.query(urlRac, colRac1, clause, null,
							Raccordement._id);
					if (cursorB.moveToFirst()) {
						do {

							HashMap<String, String> element;

							element = new HashMap<String, String>();
							element.put(colRac1[0], cursorA.getString(cursorA
									.getColumnIndex(colRac1[0])));
							element.put(colRac1[1], cursorA.getString(cursorA
									.getColumnIndex(colRac1[1])));
							element.put(colRac1[2], cursorA.getString(cursorA
									.getColumnIndex(colRac1[2])));
							element.put(colRac1[3], cursorA.getString(cursorA
									.getColumnIndex(colRac1[3])));
							element.put(colRac1[4], cursorB.getString(cursorB
									.getColumnIndex(colRac1[2])));
							element.put(colRac1[5], cursorB.getString(cursorB
									.getColumnIndex(colRac1[3])));
							element.put(colRac1[6], cursorA.getString(cursorA
									.getColumnIndex(colRac1[4])));
							element.put(colRac1[7], cursorA.getString(cursorA
									.getColumnIndex(colRac1[5])));
							element.put(colRac1[8], cursorA.getString(cursorA
									.getColumnIndex(colRac1[6])));
							element.put(colRac1[9], cursorA.getString(cursorA
									.getColumnIndex(colRac1[7])));
							element.put(colRac1[10], cursorA.getString(cursorA
									.getColumnIndex(colRac1[8])));

							liste.add(element);

						} while (cursorB.moveToNext());

					}

					indiceLimite++;
					displayContentProvider();
					indiceCourant++;
				} else {
					Toast.makeText(RepriseBlindageTa.this,
							"Ce cable ne correspond pas", Toast.LENGTH_SHORT)
							.show();

				}

				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
			} else if (resultCode == RESULT_CANCELED) {
				entreCable("Echec du scan. Entrez le n° de cable :");
			}
		}
	}

	public void entreCable(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				RepriseBlindageTa.this);
		builder.setMessage(message);
		builder.setCancelable(false);
		final EditText cable = new EditText(RepriseBlindageTa.this);
		builder.setView(cable);
		builder.setPositiveButton("Valider",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						numeroCable = cable.getText().toString();
						Log.e("N°Cable", numeroCable);

						clause = Raccordement.NUMERO_FIL_CABLE + "='"
								+ numeroCable + "' AND "
								+ Raccordement.REPRISE_BLINDAGE + "!='"
								+ "null" + "' AND "
								+ Raccordement.REPRISE_BLINDAGE + "!='' ";
						cursorA = cr.query(urlRac, colRac1, clause, null,
								Raccordement._id);
						if (cursorA.moveToFirst()) {

							numeroRep = cursorA.getString(cursorA
									.getColumnIndex(Raccordement.REPRISE_BLINDAGE));
							numeroReprise.setText("Numero Reprise: "
									+ numeroRep);
							sensReprise.setText("Sens reprise : "
									+ cursorA.getString(cursorA
											.getColumnIndex(Raccordement.SANS_REPRISE_BLINDAGE)));

							clause = Raccordement.NUMERO_FIL_CABLE + "!='"
									+ numeroCable + "' AND "
									+ Raccordement.REPRISE_BLINDAGE + "='"
									+ numeroRep + "' GROUP BY "
									+ Raccordement.NUMERO_FIL_CABLE;
							cursorB = cr.query(urlRac, colRac1, clause, null,
									Raccordement._id);
							if (cursorB.moveToFirst()) {
								do {

									HashMap<String, String> element;

									element = new HashMap<String, String>();
									element.put(
											colRac1[0],
											cursorA.getString(cursorA
													.getColumnIndex(colRac1[0])));
									element.put(
											colRac1[1],
											cursorA.getString(cursorA
													.getColumnIndex(colRac1[1])));
									element.put(
											colRac1[2],
											cursorA.getString(cursorA
													.getColumnIndex(colRac1[2])));
									element.put(
											colRac1[3],
											cursorA.getString(cursorA
													.getColumnIndex(colRac1[3])));
									element.put(
											colRac1[4],
											cursorB.getString(cursorB
													.getColumnIndex(colRac1[2])));
									element.put(
											colRac1[5],
											cursorB.getString(cursorB
													.getColumnIndex(colRac1[3])));
									element.put(
											colRac1[6],
											cursorA.getString(cursorA
													.getColumnIndex(colRac1[4])));
									element.put(
											colRac1[7],
											cursorA.getString(cursorA
													.getColumnIndex(colRac1[5])));
									element.put(
											colRac1[8],
											cursorA.getString(cursorA
													.getColumnIndex(colRac1[6])));
									element.put(
											colRac1[9],
											cursorA.getString(cursorA
													.getColumnIndex(colRac1[7])));
									element.put(
											colRac1[10],
											cursorA.getString(cursorA
													.getColumnIndex(colRac1[8])));

									liste.add(element);

								} while (cursorB.moveToNext());

							} else {
								HashMap<String, String> element;

								element = new HashMap<String, String>();
								element.put(
										colRac1[0],
										cursorA.getString(cursorA
												.getColumnIndex(colRac1[0])));
								element.put(
										colRac1[1],
										cursorA.getString(cursorA
												.getColumnIndex(colRac1[1])));
								element.put(
										colRac1[2],
										cursorA.getString(cursorA
												.getColumnIndex(colRac1[2])));
								element.put(
										colRac1[3],
										cursorA.getString(cursorA
												.getColumnIndex(colRac1[3])));
							
								element.put(
										colRac1[6],
										cursorA.getString(cursorA
												.getColumnIndex(colRac1[4])));
								element.put(
										colRac1[7],
										cursorA.getString(cursorA
												.getColumnIndex(colRac1[5])));
								element.put(
										colRac1[8],
										cursorA.getString(cursorA
												.getColumnIndex(colRac1[6])));
								element.put(
										colRac1[9],
										cursorA.getString(cursorA
												.getColumnIndex(colRac1[7])));
								element.put(
										colRac1[10],
										cursorA.getString(cursorA
												.getColumnIndex(colRac1[8])));

								liste.add(element);

							}

							indiceLimite++;
							displayContentProvider();
							indiceCourant++;
						} else {
							Toast.makeText(RepriseBlindageTa.this,
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