package com.inodex.inoprod.activities.controleur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.activities.cableur.DenudageSertissageEnfichageTa;
import com.inodex.inoprod.activities.cableur.MiseLongueurTb;
import com.inodex.inoprod.activities.cableur.PreparationTa;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ControleSertissageTa extends Activity {

	/** Elements à récuperer de la vue */
	private TextView titre, numeroConnecteur, repereElectrique,
			positionChariot;
	private ImageButton boutonCheck, infoProduit, boutonAnnuler, boutonAide;
	private ImageButton petitePause, grandePause;
	private GridView gridView;

	/** Uri à manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;
	private int indiceLimite = 0;
	private int nbRows, nbValide, compteurCables;
	private HashMap<String, String> element;

	/** Tableau des infos produit */
	private String labels[];
	private String ordre = null;

	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();

	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateDebut, dateRealisation;
	private long dureeMesuree = 0;
	private Time heureRealisation = new Time();

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	private boolean prodAchevee;
	private boolean scanEnCours = true;

	private String clause, numeroOperation, numeroCo, clauseTotal,
			oldClauseTotal, numeroCable, description;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.DUREE_MESUREE };

	private int layouts[] = new int[] { R.id.statutLiaison,
			R.id.numeroRevisionLiaison, R.id.typeCable, R.id.numeroFil,
			R.id.numeroFilDansCable, R.id.couleurFil,
			R.id.referenceFabricantContact, R.id.controleValide,
			R.id.controleAcceptable, R.id.controleRefuse };
	private String controle[] = new String[] { "Statut Liaison",
			"Numéro révision liaison", "Type cable", "Numero Fil",
			"Numero Fil dans cable", "Couleur fil",
			"Reference fabricant contact", "Controle valide",
			"Controle acceptable", "Controle refuse" };

	private String colRac[] = new String[] { Raccordement.ETAT_LIAISON_FIL,
			Raccordement.NUMERO_REVISION_FIL, Raccordement.TYPE_FIL_CABLE,
			Raccordement.NUMERO_FIL_CABLE, Raccordement.NUMERO_FIL_DANS_CABLE,
			Raccordement.COULEUR_FIL, Raccordement.REFERENCE_FABRICANT2,
			Raccordement.NUMERO_BORNE_TENANT, Raccordement.REFERENCE_INTERNE,
			Raccordement.REFERENCE_OUTIL_TENANT,
			Raccordement.NUMERO_SERIE_OUTIL, Raccordement.REGLAGE_OUTIL_TENANT,
			Raccordement.REFERENCE_ACCESSOIRE_OUTIL_TENANT, Raccordement._id,
			Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.NUMERO_OPERATION,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.LONGUEUR_FIL_CABLE, Raccordement.ORDRE_REALISATION,
			Raccordement.REPRISE_BLINDAGE };

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
	
	private String Rac;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_controle_sertissage_ta);
		// Initialisation du temps
		dateDebut = new Date();

		// Récupération des éléments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();
		nbValide = 0;

		// Récuperation des éléments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		titre = (TextView) findViewById(R.id.textView1);
		numeroConnecteur = (TextView) findViewById(R.id.textView3);
		boutonAide = (ImageButton) findViewById(R.id.imageButton4);
		boutonAnnuler = (ImageButton) findViewById(R.id.imageButton2);
		boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);
		positionChariot = (TextView) findViewById(R.id.textView5b);
		repereElectrique = (TextView) findViewById(R.id.textView5);
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
					.getColumnIndex(Operation.DESCRIPTION_OPERATION));
			numeroCo = (cursor.getString(cursor
					.getColumnIndex(Operation.RANG_1_1))).substring(11, 14);
			numeroConnecteur.append(" : " + numeroCo);
		}
		
		if (description.contains("tête A")) {
			clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='" + numeroCo
					+ "' AND " + Raccordement.FAUX_CONTACT + "='" + 0
					+ "' AND " + Raccordement.OBTURATEUR + "='" + 0 + "' AND "
					+ Raccordement.REPRISE_BLINDAGE + " IS NULL ";
			titre.setText(R.string.controleSertissageTa);
			ordre = "A";
			Rac = Raccordement.NUMERO_COMPOSANT_TENANT;

		} else {
			clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
					+ numeroCo + "' AND " + Raccordement.FAUX_CONTACT + "='"
					+ 0 + "' AND " + Raccordement.OBTURATEUR + "='" + 0
					+ "' AND " + Raccordement.REPRISE_BLINDAGE + " IS NULL ";
			titre.setText(R.string.controleSertissageTb);
			ordre = "B";
			Rac = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT;

		}
		cursorA = cr.query(urlRac, colRac, clause, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {

			positionChariot
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.NUMERO_POSITION_CHARIOT)));
			if (ordre.equals("A")) {
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT)));
			} else {
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT)));
			}

		}
		nbRows = cr.query(urlRac, colRac,
				clause + " GROUP BY " + Raccordement.NUMERO_FIL_CABLE, null,
				Raccordement._id).getCount();
		Log.e("NombreLignes", "" + nbRows);

		// Affichage du temps nécessaire
		timer = (TextView) findViewById(R.id.timeDisp);
		dureeTotal = 0;
		cursorTime = cr.query(urlTim, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%hemine%' ", null, Duree._id);
		if (cursorTime.moveToFirst()) {
			dureeTotal += TimeConverter.convert(cursorTime.getString(cursorTime
					.getColumnIndex(Duree.DUREE_THEORIQUE)));

		}
		dureeTotal = dureeTotal * nbRows;
		timer.setTextColor(Color.GREEN);
		timer.setText(TimeConverter.display(dureeTotal));

		// Scan
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (prodAchevee) {

					contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0]
							+ " " + nomPrenomOperateur[1]);
					dateRealisation = new Date();
					contact.put(Operation.DATE_REALISATION,
							dateRealisation.toGMTString());
					heureRealisation.setToNow();
					contact.put(Operation.HEURE_REALISATION,
							heureRealisation.toString());
					dureeMesuree += dateRealisation.getTime()
							- dateDebut.getTime();
					contact.put(Operation.DUREE_MESUREE, dureeMesuree / 1000);
					cr.update(urlSeq, contact, Operation._id + " = ?",
							new String[] { Integer
									.toString(opId[indiceCourant]) });
					contact.clear();

					// Signalement du point de controle
					// Cables validés
					clause = Operation.RANG_1_1 + " LIKE '%" + numeroCo
							+ "%' AND (" + Operation.DESCRIPTION_OPERATION
							+ " LIKE 'Enfichage Tête " + ordre + "%' OR "
							+ Operation.DESCRIPTION_OPERATION
							+ " LIKE 'Finalisation Tête " + ordre + "%' OR "
							+ Operation.DESCRIPTION_OPERATION
							+ " LIKE 'Tri des aboutissants Tête " + ordre
							+ "%')";
					cursor = cr.query(urlSeq, columnsSeq, clause, null,
							Operation._id);
					if (cursor.moveToFirst()) {
						do {

							contact.put(Operation.REALISABLE, 1);
							int id = cursor.getInt(cursor
									.getColumnIndex(Operation._id));
							cr.update(urlSeq, contact, Operation._id + "='"
									+ id + "'", null);
							contact.clear();

						} while (cursor.moveToNext());
					}

					// Cables refusées
					clause = Operation.RANG_1_1 + " LIKE '%" + numeroCo
							+ "%' AND " + Operation.DESCRIPTION_OPERATION
							+ " LIKE 'Denudage%' ";
					cursor = cr.query(urlSeq, columnsSeq, clause, null,
							Operation._id);
					if (cursor.moveToFirst()) {
						for (int i = 0; i < (nbRows - nbValide); i++) {

							contact.put(Operation.NOM_OPERATEUR, "");
							int id = cursor.getInt(cursor
									.getColumnIndex(Operation._id));
							cr.update(urlSeq, contact, Operation._id + "='"
									+ id + "'", null);
							contact.clear();
							cursor.moveToNext();
						}
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
							if (nextOperation.startsWith("Contrôle final tête")) {
								toNext = new Intent(ControleSertissageTa.this,
										ControleFinalisationTa.class);
							} else if (nextOperation
									.startsWith("Contrôle rétention")) {
								toNext = new Intent(ControleSertissageTa.this,
										ControleRetentionTa.class);
							} else if (nextOperation
									.startsWith("Contrôle sertissage")) {
								toNext = new Intent(ControleSertissageTa.this,
										ControleSertissageTa.class);
							} else if (nextOperation
									.startsWith("Contrôle final harnais")) {
								toNext = new Intent(ControleSertissageTa.this,
										ControleFinalHarnais.class);
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
						Intent toNext = new Intent(ControleSertissageTa.this,
								MainMenuControleur.class);
						toNext.putExtra("Noms", nomPrenomOperateur);
						toNext.putExtra("opId", opId);
						toNext.putExtra("Indice", indiceCourant);
						startActivity(toNext);
						finish();

					}

				} else {
					if (scanEnCours) {

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
									ControleSertissageTa.this);
							builder.setMessage("Impossible de trouver une application pour le scan. Entrez le N° de cable.");
							builder.setCancelable(false);
							final EditText cable = new EditText(
									ControleSertissageTa.this);
							builder.setView(cable);
							builder.setPositiveButton("Valider",
									new DialogInterface.OnClickListener() {

										public void onClick(
												DialogInterface dialog,
												int which) {
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
													+ "='"
													+ numeroCo
													+ "' ) AND "
													+ Raccordement.REPRISE_BLINDAGE
													+ " IS NULL ";
											cursorA = cr.query(urlRac, colRac,
													clause, null,
													Raccordement._id);
											Log.e("Nb Cables",
													cursorA.getCount() + "");
											if (cursorA.moveToFirst()) {
												HashMap<String, String> element;
												compteurCables = 0;
												do {

													element = new HashMap<String, String>();
													element.put(
															controle[0],
															cursorA.getString(cursorA
																	.getColumnIndex(colRac[0])));
													element.put(
															controle[1],
															cursorA.getString(cursorA
																	.getColumnIndex(colRac[1])));
													element.put(
															controle[2],
															cursorA.getString(cursorA
																	.getColumnIndex(colRac[2])));
													element.put(
															controle[3],
															cursorA.getString(cursorA
																	.getColumnIndex(colRac[3])));
													element.put(
															controle[4],
															cursorA.getString(cursorA
																	.getColumnIndex(colRac[4])));
													element.put(
															controle[5],
															cursorA.getString(cursorA
																	.getColumnIndex(colRac[5])));
													element.put(
															controle[6],
															cursorA.getString(cursorA
																	.getColumnIndex(colRac[6])));

													if (liste.contains(element)) {
														Toast.makeText(
																ControleSertissageTa.this,
																"Ce cable a dèja été utilisé",
																Toast.LENGTH_SHORT)
																.show();
														cursor.moveToLast();
														Log.e("ELEMENt Contenu",
																""
																		+ numeroCable);
													} else {
														scanEnCours = false;
														liste.add(element);
														compteurCables++;
														displayContentProvider();

													}
												} while (cursorA.moveToNext());
												Log.e("Nb Cables retenus",
														compteurCables + "");
											} else {
												Toast.makeText(
														ControleSertissageTa.this,
														"Ce cable ne correspond pas",
														Toast.LENGTH_SHORT)
														.show();
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
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								ControleSertissageTa.this);
						builder.setMessage("Quel est le résultat du controle du cable?");
						builder.setCancelable(false);

						builder.setPositiveButton("Valider",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {

										/*
										 * liste.remove(liste.size() - 1);
										 * 
										 * element.put(controle[7], "X");
										 * 
										 * liste.add(element);
										 */
										liste.get(liste.size() - compteurCables)
												.put(controle[7], "X");

										compteurCables--;
										if (compteurCables == 0) {
											indiceLimite++;
											scanEnCours = true;
											nbValide++;
										}
										displayContentProvider();
										Log.e("Indice Limite", ""
												+ indiceLimite);

									}

								});

						builder.setNegativeButton("Refuser",
								new DialogInterface.OnClickListener() {
									public void onClick(
											final DialogInterface dialog,
											final int id) {

										/*
										 * liste.remove(liste.size() - 1);
										 * 
										 * element.put(controle[7], "X");
										 * 
										 * liste.add(element);
										 */
										liste.get(liste.size() - compteurCables)
												.put(controle[9], "X");

										compteurCables--;
										if (compteurCables == 0) {
											indiceLimite++;
											scanEnCours = true;

										}
										displayContentProvider();
									}
								});
						builder.setNeutralButton("Acceptable",
								new DialogInterface.OnClickListener() {
									public void onClick(
											final DialogInterface dialog,
											final int id) {
										/*
										 * liste.remove(liste.size() - 1);
										 * 
										 * element.put(controle[7], "X");
										 * 
										 * liste.add(element);
										 */
										liste.get(liste.size() - compteurCables)
												.put(controle[8], "X");

										compteurCables--;
										if (compteurCables == 0) {
											indiceLimite++;
											scanEnCours = true;
											nbValide++;
										}
										displayContentProvider();
										;
									}
								});

						builder.show();

					}

				}
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
				Intent toInfo = new Intent(ControleSertissageTa.this,
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

		// Grande pause
		grandePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ControleSertissageTa.this);
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
						ControleSertissageTa.this);
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

		SimpleAdapter sa = new SimpleAdapter(this, liste,
				R.layout.grid_layout_controle_sertissage_ta, controle, layouts);

		gridView.setAdapter(sa);

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

				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				numeroCable = contents;
				clause = Raccordement.NUMERO_FIL_CABLE + "='" + numeroCable
						+ "' AND " + Rac
						+ "='" + numeroCo +  "'  AND " + Raccordement.REPRISE_BLINDAGE
						+ " IS NULL ";
				cursorA = cr.query(urlRac, colRac, clause, null,
						Raccordement._id);
				if (cursorA.moveToFirst()) {
					HashMap<String, String> element;
					compteurCables = 0;
					do {

						element = new HashMap<String, String>();
						element.put(controle[0], cursorA.getString(cursorA
								.getColumnIndex(colRac[0])));
						element.put(controle[1], cursorA.getString(cursorA
								.getColumnIndex(colRac[1])));
						element.put(controle[2], cursorA.getString(cursorA
								.getColumnIndex(colRac[2])));
						element.put(controle[3], cursorA.getString(cursorA
								.getColumnIndex(colRac[3])));
						element.put(controle[4], cursorA.getString(cursorA
								.getColumnIndex(colRac[4])));
						element.put(controle[5], cursorA.getString(cursorA
								.getColumnIndex(colRac[5])));
						element.put(controle[6], cursorA.getString(cursorA
								.getColumnIndex(colRac[6])));

						if (liste.contains(element)) {
							Toast.makeText(ControleSertissageTa.this,
									"Ce cable a dèja été utilisé",
									Toast.LENGTH_SHORT).show();
							cursor.moveToLast();
						} else {
							scanEnCours = false;
							liste.add(element);
							compteurCables++;
							displayContentProvider();

						}
					} while (cursor.moveToNext());

				} else if (resultCode == RESULT_CANCELED) {

					AlertDialog.Builder builder = new AlertDialog.Builder(
							ControleSertissageTa.this);
					builder.setMessage("Impossible de trouver une application pour le scan. Entrez le N° de cable.");
					builder.setCancelable(false);
					final EditText cable = new EditText(
							ControleSertissageTa.this);
					builder.setView(cable);
					builder.setPositiveButton("Valider",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									numeroCable = cable.getText().toString();

									clause = Raccordement.NUMERO_FIL_CABLE + "='" + numeroCable
											+ "' AND " + Rac
											+ "='" + numeroCo +  "'  AND " + Raccordement.REPRISE_BLINDAGE
											+ " IS NULL ";
									cursorA = cr.query(urlRac, colRac, clause,
											null, Raccordement._id);
									if (cursorA.moveToFirst()) {
										HashMap<String, String> element;
										compteurCables = 0;
										do {

											element = new HashMap<String, String>();
											element.put(
													controle[0],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[0])));
											element.put(
													controle[1],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[1])));
											element.put(
													controle[2],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[2])));
											element.put(
													controle[3],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[3])));
											element.put(
													controle[4],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[4])));
											element.put(
													controle[5],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[5])));
											element.put(
													controle[6],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[6])));

											if (liste.contains(element)) {
												Toast.makeText(
														ControleSertissageTa.this,
														"Ce cable a dèja été utilisé",
														Toast.LENGTH_SHORT)
														.show();
												cursor.moveToLast();
											} else {
												scanEnCours = false;
												liste.add(element);
												compteurCables++;
												displayContentProvider();
											}

										} while (cursorA.moveToNext());

									} else {
										Toast.makeText(
												ControleSertissageTa.this,
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
	}

	public void entreCable(String message) {
	}

}
