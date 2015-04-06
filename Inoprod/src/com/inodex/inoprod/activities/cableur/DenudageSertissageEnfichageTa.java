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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.inodex.inoprod.R;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.business.DureesProvider;
import com.inodex.inoprod.business.NomenclatureProvider;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TimeConverter;
import com.inodex.inoprod.business.Durees.Duree;
import com.inodex.inoprod.business.Nomenclature.Cable;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

/**
 * Ecran 56
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class DenudageSertissageEnfichageTa extends Activity {

	/** Elements à récuperer de la vue */
	private TextView titre, numeroConnecteur, designation, repereElectrique,
			referenceInterne, longueur, gainage, positionChariot,
			referenceFabricant, instruction;
	private ImageButton boutonCheck, infoProduit, retour, boutonAide;
	private ImageButton petitePause, grandePause;
	private GridView gridView;

	/** Uri à manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;
	private Uri urlNom = NomenclatureProvider.CONTENT_URI;

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
	private List<HashMap<String, String>> oldListe = new ArrayList<HashMap<String, String>>();
	private HashMap<String, String> element;
	private int nbCablesAjoutes;
	private HashMap<String, String> straps = new HashMap<String, String>();

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

	private String clause, numeroOperation, numeroCo, clauseTotal,
			oldClauseTotal, numeroCable, b, bc, nc, nco;
	private boolean prodAchevee, teteB;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.DUREE_MESUREE, Operation.RANG_1_1_1 };

	private int layouts[] = new int[] { R.id.statutLiaison,
			R.id.numeroRevisionLiaison, R.id.typeCable, R.id.numeroFil,
			R.id.numeroFilDansCable, R.id.couleurFil, R.id.numeroBorne,
			R.id.referenceFabricantContact, R.id.referenceInterneContact,
			R.id.referencePince, R.id.numeroSeriePince, R.id.reglagePince,
			R.id.referencePositionneur };

	private String colRac[] = new String[] { Raccordement.ETAT_LIAISON_FIL,
			Raccordement.NUMERO_REVISION_FIL, Raccordement.TYPE_FIL_CABLE,
			Raccordement.NUMERO_FIL_CABLE, Raccordement.NUMERO_FIL_DANS_CABLE,
			Raccordement.COULEUR_FIL, Raccordement.NUMERO_BORNE_TENANT,
			Raccordement.REFERENCE_FABRICANT2, Raccordement.REFERENCE_INTERNE,
			Raccordement.REFERENCE_OUTIL_TENANT,
			Raccordement.NUMERO_SERIE_OUTIL,
			Raccordement.REGLAGE_OUTIL_ABOUTISSANT,
			Raccordement.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT,
			Raccordement._id, Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.NUMERO_OPERATION,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.LONGUEUR_FIL_CABLE, Raccordement.ORDRE_REALISATION,
			Raccordement.NUMERO_BORNE_ABOUTISSANT,
			Raccordement.NUMERO_BORNE_TENANT };

	private String colNom[] = new String[] { Cable.REFERENCE_INTERNE,
			Cable.REFERENCE_FABRICANT2, Cable.QUANTITE, Cable.UNITE,
			Cable.NUMERO_COMPOSANT, Cable.FAMILLE_PRODUIT, Cable._id, };

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
		setContentView(R.layout.activity_denudage_sertissage_enfichage_ta);

		// Initialisation du temps
		dateDebut = new Date();

		// Récupération des éléments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();
		clauseTotal = " ";
		nbCablesAjoutes = 0;

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
		referenceFabricant = (TextView) findViewById(R.id.textView5d);
		longueur = (TextView) findViewById(R.id.textView5c);
		instruction = (TextView) findViewById(R.id.textView2);
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
			numeroCo = (cursor.getString(cursor
					.getColumnIndex(Operation.RANG_1_1))).substring(11, 14);
			numeroConnecteur.append(" : " + numeroCo);
		}

		cursorA = cr.query(urlNom, colNom, Cable.NUMERO_COMPOSANT + "='"
				+ numeroCo + "' AND " + Cable.FAMILLE_PRODUIT
				+ " LIKE '%Gaine%'", null, null);
		if (cursorA.moveToFirst()) {
			gainage.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Cable.FAMILLE_PRODUIT)));
			longueur.append(" : "
					+ cursorA.getString(cursorA.getColumnIndex(Cable.QUANTITE))
					+ cursorA.getString(cursorA.getColumnIndex(Cable.UNITE)));
		}

		// Recuperation de la première opération
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

			referenceInterne.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Raccordement.REFERENCE_INTERNE)));
			referenceFabricant
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.REFERENCE_FABRICANT2)));

			if (numeroOperation.startsWith("4")) {
				titre.setText(R.string.denudageSertissageEnfichageTa);
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT)));
				clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='"
						+ numeroCo + "' AND ( " + Raccordement.FAUX_CONTACT
						+ "='" + 0 + "' OR " + Raccordement.OBTURATEUR + "='"
						+ 0 + "' OR " + Raccordement.REPRISE_BLINDAGE
						+ " IS NULL )";
				teteB = false;
				b = Raccordement.NUMERO_BORNE_TENANT;
				bc = Raccordement.NUMERO_BORNE_ABOUTISSANT;
				nc = Raccordement.NUMERO_COMPOSANT_TENANT;
				nco = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT;
			} else {
				titre.setText(R.string.denudageSertissageEnfichageTb);
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT)));
				clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
						+ numeroCo + "' AND ( " + Raccordement.FAUX_CONTACT
						+ "='" + 0 + "' OR " + Raccordement.OBTURATEUR + "='"
						+ 0 + "' OR " + Raccordement.REPRISE_BLINDAGE
						+ " IS NULL )";
				teteB = true;
				colRac[6] = Raccordement.NUMERO_BORNE_ABOUTISSANT;
				colRac[8] = Raccordement.REFERENCE_OUTIL_ABOUTISSANT;
				colRac[10] = Raccordement.REGLAGE_OUTIL_ABOUTISSANT;
				colRac[11] = Raccordement.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT;
				b = Raccordement.NUMERO_BORNE_ABOUTISSANT;
				bc = Raccordement.NUMERO_BORNE_TENANT;
				nco = Raccordement.NUMERO_COMPOSANT_TENANT;
				nc = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT;
			}

		}

		nbRows = 0;
		for (int i : opId) {
			cursorA = cr.query(urlSeq, columnsSeq, Operation._id + "='" + i
					+ "'", null, Operation._id);
			if (cursorA.moveToFirst()) {
				if (cursorA.getString(
						cursorA.getColumnIndex(Operation.RANG_1_1)).contains(
						numeroCo)
						&& cursorA
								.getString(
										cursorA.getColumnIndex(Operation.DESCRIPTION_OPERATION))
								.contains("Denudage Sertissage Enfichage")) {
					nbRows++;
				}

			}
		}

		// Affichage du temps nécessaire
		timer = (TextView) findViewById(R.id.timeDisp);
		dureeTotal = 0;
		cursorTime = cr.query(urlTim, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%fichage%' ", null, Duree._id);
		if (cursorTime.moveToFirst()) {
			dureeTotal += TimeConverter.convert(cursorTime.getString(cursorTime
					.getColumnIndex(Duree.DUREE_THEORIQUE)));

		}
		dureeTotal = dureeTotal * nbRows;
		timer.setTextColor(Color.GREEN);
		timer.setText(TimeConverter.display(dureeTotal));
		// Bouton de validation
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// Vérification de l'état de la production
				if (prodAchevee) {

					String nextOperation = null;

					if (teteB) {
						// Signalement du point de controle
						// Cables validés
						clause = Operation.RANG_1_1 + " LIKE '%" + numeroCo
								+ "%' AND (" 
								+ Operation.DESCRIPTION_OPERATION
								+ " LIKE 'Finalisation Tête%' OR "
								+ Operation.DESCRIPTION_OPERATION
								+ " LIKE 'Tri des aboutissants Tête%')";
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
					}

					indiceCourant += nbRows;

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
								toNext = new Intent(
										DenudageSertissageEnfichageTa.this,
										PreparationTa.class);
							} else if (nextOperation.startsWith("Reprise")) {
								toNext = new Intent(
										DenudageSertissageEnfichageTa.this,
										RepriseBlindageTa.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage Enfichage")) {
								toNext = new Intent(
										DenudageSertissageEnfichageTa.this,
										DenudageSertissageEnfichageTa.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage de")) {
								toNext = new Intent(
										DenudageSertissageEnfichageTa.this,
										DenudageSertissageContactTa.class);
							} else if (nextOperation.startsWith("Finalisation")) {
								toNext = new Intent(
										DenudageSertissageEnfichageTa.this,
										FinalisationTa.class);
							} else if (nextOperation.startsWith("Tri")) {
								toNext = new Intent(
										DenudageSertissageEnfichageTa.this,
										TriAboutissantsTa.class);
							} else if (nextOperation
									.startsWith("Positionnement")) {
								toNext = new Intent(
										DenudageSertissageEnfichageTa.this,
										PositionnementTaTab.class);
							} else if (nextOperation.startsWith("Cheminement")) {
								toNext = new Intent(
										DenudageSertissageEnfichageTa.this,
										CheminementTa.class);
							} else if (nextOperation.startsWith("Mise")) {
								toNext = new Intent(
										DenudageSertissageEnfichageTa.this,
										MiseLongueurTb.class);

							} else if (nextOperation
									.startsWith("Denudage Sertissage Coss")) {
								toNext = new Intent(
										DenudageSertissageEnfichageTa.this,
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
						// Aucune opération suivante: retour au menu principal
					} catch (ArrayIndexOutOfBoundsException e) {
						Intent toNext = new Intent(
								DenudageSertissageEnfichageTa.this,
								MainMenuCableur.class);
						toNext.putExtra("Noms", nomPrenomOperateur);
						toNext.putExtra("opId", opId);
						toNext.putExtra("Indice", indiceCourant);
						startActivity(toNext);
						finish();

					}
					// Si production non achevée
				} else {
					// SCAN du numéro de cabl
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
						entreCable("Impossible de trouver une application pour le scan. Entrez le N° de cable.");
					}

				}
			}
		});

		// Info Produit

		infoProduit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cursorInfo = cr.query(urlRac, colInfo,
						Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
								+ numeroCo + "' OR "
								+ Raccordement.NUMERO_COMPOSANT_TENANT + "='"
								+ numeroCo + "'", null, null);
				Intent toInfo = new Intent(DenudageSertissageEnfichageTa.this,
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

		// Retour arrière
		retour.setOnClickListener(new View.OnClickListener() {

			@Override
			// MAJ des indices
			public void onClick(View v) {
				// MAJ des indices
				if (indiceLimite > 0) {
					indiceLimite--;
					Log.e("Indice", "" + indiceLimite);
					indiceCourant--;
					Log.e("Indice", "" + indiceLimite);
				}

				// MAJ de la durée
				dureeMesuree = 0;
				dateDebut = new Date();

				for (int i = 0; i < nbCablesAjoutes; i++) {
					Log.e("Taille liste", liste.size() + "");
					element = liste.get(liste.size() - 1);

					contact.put(Operation.DATE_REALISATION, "");
					cr.update(
							urlSeq,
							contact,
							Operation.RANG_1_1_1
									+ "='"
									+ element
											.get(Raccordement.NUMERO_FIL_CABLE)
									+ "' AND "
									+ Operation.DESCRIPTION_OPERATION
									+ " LIKE '%Denudage Sertissage de%' AND "
									+ Operation.RANG_1_1 + " LIKE '%"
									+ numeroCo + "%'", null);
					liste.remove(liste.size() - 1);
				}

				displayContentProvider();
				prodAchevee = false;
			}
		});

		// Grande pause
		grandePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						DenudageSertissageEnfichageTa.this);
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
						DenudageSertissageEnfichageTa.this);
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
		// Vérification de l'état de la production
		int fin = cr.query(
				urlSeq,
				columnsSeq,
				Operation.DATE_REALISATION + "!='null' AND "
						+ Operation.RANG_1_1 + " LIKE '%" + numeroCo
						+ "%' AND " + Operation.DESCRIPTION_OPERATION
						+ " LIKE '%Sertissage Enfichage%'", null, null)
				.getCount();
		Log.e("Fin", "" + fin);
		if (fin >= cr.query(
				urlSeq,
				columnsSeq,
				Operation.RANG_1_1 + " LIKE '%" + numeroCo + "%' AND "
						+ Operation.DESCRIPTION_OPERATION
						+ " LIKE '%Sertissage Enfichage%'", null, null)
				.getCount()) {
			prodAchevee = true;
			Toast.makeText(this, "Production achevée", Toast.LENGTH_LONG)
					.show();

		}

		SimpleAdapter sca = new SimpleAdapter(this, liste,
				R.layout.grid_layout_denudage_sertissage_enfichage_ta, colRac,
				layouts);

		gridView.setAdapter(sca);

	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}

	// Récupération du code barre scanné
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				Log.e("SCAN", "OK");
				String contents = intent.getStringExtra("SCAN_RESULT");
				numeroCable = contents;
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

				clause = Operation.RANG_1_1_1 + "='" + numeroCable + "' AND "
						+ Operation.DESCRIPTION_OPERATION
						+ " LIKE '%Sertissage Enfichage%' AND "
						+ Operation.RANG_1_1 + " LIKE '%" + numeroCo
						+ "%' AND " + Operation.DATE_REALISATION
						+ "!='null' AND " + Operation.DATE_REALISATION + "!=''";
				cursor = cr.query(urlSeq, columnsSeq, clause, null, null);
				if (cursor.moveToFirst()) {
					Toast.makeText(DenudageSertissageEnfichageTa.this,
							"Ce cable a dèja été utilisé", Toast.LENGTH_SHORT)
							.show();

				} else if (straps.get(numeroCable) != null) {
					nbCablesAjoutes = 0;
					oldListe = liste;
					clause = Raccordement.NUMERO_FIL_CABLE + "='" + numeroCable
							+ "' AND " + straps.get(numeroCable) + "='"
							+ numeroCo + "' AND " + Raccordement.FIL_SENSIBLE
							+ "='0' AND ( " + Raccordement.REPRISE_BLINDAGE
							+ " IS NULL OR " + Raccordement.REPRISE_BLINDAGE
							+ "='' )";
					cursorA = cr.query(urlRac, colRac, clause, null,
							Raccordement._id);
					if (cursorA.moveToFirst()) {
						String borne = cursorA
								.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_BORNE_TENANT));
						boolean tenant = true;
						if (borne == null) {
							borne = cursorA
									.getString(cursorA
											.getColumnIndex(Raccordement.NUMERO_BORNE_ABOUTISSANT));
							tenant = false;
						}
						addCables(borne, numeroCo, tenant, true);
					}

				} else if (straps.get(numeroCable) == null) {
					nbCablesAjoutes = 0;
					oldListe = liste;
					String borneT, borneA;
					Log.e("Strap", "non ");
					// Verification de l'existence d'un strap
					clause = Raccordement.NUMERO_FIL_CABLE + "='" + numeroCable
							+ "' AND " + Raccordement.NUMERO_COMPOSANT_TENANT
							+ "='" + numeroCo + "' AND "
							+ Raccordement.FIL_SENSIBLE + "='0' AND ( "
							+ Raccordement.REPRISE_BLINDAGE + " IS NULL OR "
							+ Raccordement.REPRISE_BLINDAGE + "='' )";
					cursorA = cr.query(urlRac, colRac, clause, null,
							Raccordement._id);
					// Verification de l'existence d'un strap
					clause = Raccordement.NUMERO_FIL_CABLE + "='" + numeroCable
							+ "' AND "
							+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + "='"
							+ numeroCo + "' AND " + Raccordement.FIL_SENSIBLE
							+ "='0' AND ( " + Raccordement.REPRISE_BLINDAGE
							+ " IS NULL OR " + Raccordement.REPRISE_BLINDAGE
							+ "='' )";
					cursorB = cr.query(urlRac, colRac, clause, null,
							Raccordement._id);

					if (cursorB.moveToFirst() && cursorA.moveToFirst()) {
						// Présence d'un strap
						Log.e("Strap", "OUI");
						borneA = cursorB
								.getString(cursorB
										.getColumnIndex(Raccordement.NUMERO_BORNE_ABOUTISSANT));
						borneT = cursorA
								.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_BORNE_TENANT));
						Cursor c1, c2;
						c1 = cr.query(urlRac, colRac,
								Raccordement.NUMERO_BORNE_TENANT + "='"
										+ borneT + "' AND "
										+ Raccordement.NUMERO_COMPOSANT_TENANT
										+ "='" + numeroCo + "'", null, null);
						c2 = cr.query(
								urlRac,
								colRac,
								Raccordement.NUMERO_BORNE_ABOUTISSANT
										+ "='"
										+ borneA
										+ "' AND "
										+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
										+ "='" + numeroCo + "'", null, null);
						if (c1.getCount() > c2.getCount()) {
							c1.moveToFirst();
							addCables(
									c1.getString(c1
											.getColumnIndex(Raccordement.NUMERO_BORNE_TENANT)),
									numeroCo, true, false);
							do {
								straps.put(
										c1.getString(c1
												.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)),
										Raccordement.NUMERO_COMPOSANT_ABOUTISSANT);
							} while (c1.moveToNext());
						} else {
							c2.moveToFirst();
							addCables(
									c2.getString(c2
											.getColumnIndex(Raccordement.NUMERO_BORNE_ABOUTISSANT)),
									numeroCo, false, false);
							do {
								straps.put(
										c2.getString(c2
												.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)),
										Raccordement.NUMERO_COMPOSANT_TENANT);
							} while (c2.moveToNext());
						}

					} else if (cursorA.moveToFirst()
							&& (cursorA.getCount() == 1)) {
						Log.e("Strap", "1 ");
						// Monofilaire

						addCables(
								cursorA.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_BORNE_TENANT)),
								numeroCo, true, true);

					} else if (cursorA.moveToFirst() && cursorA.getCount() > 1) {
						// Paire torsadé
						Log.e("Strap",
								"2"
										+ " Borne: "
										+ cursorA.getString(cursorA
												.getColumnIndex(Raccordement.NUMERO_BORNE_TENANT)));
						do {
							addCables(
									cursorA.getString(cursorA
											.getColumnIndex(Raccordement.NUMERO_BORNE_TENANT)),
									numeroCo, true, true);
						} while (cursorA.moveToNext());
					} else if (cursorB.moveToFirst()
							&& (cursorB.getCount() == 1)) {
						// Monofilaire
						Log.e("Strap", "3 ");
						addCables(
								cursorB.getString(cursorB
										.getColumnIndex(Raccordement.NUMERO_BORNE_ABOUTISSANT)),
								numeroCo, true, true);

					} else if (cursorB.moveToFirst() && cursorB.getCount() > 1) {
						// Paire torsadé
						Log.e("Strap", "4 ");
						do {
							addCables(
									cursorB.getString(cursorB
											.getColumnIndex(Raccordement.NUMERO_BORNE_ABOUTISSANT)),
									numeroCo, true, true);
						} while (cursorB.moveToNext());

					} else {
						// Le cable n'est pas utilisé pour
						// ce connecteur
						Toast.makeText(DenudageSertissageEnfichageTa.this,
								"Ce cable ne correspond pas",
								Toast.LENGTH_SHORT).show();

					}
				}
				displayContentProvider();
			}

		} else if (resultCode == RESULT_CANCELED) {
			entreCable("Echec du scan. Entrez le n° de cable :");
		}
	}

	public void entreCable(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				DenudageSertissageEnfichageTa.this);
		builder.setMessage(message);
		builder.setCancelable(false);
		final EditText cable = new EditText(DenudageSertissageEnfichageTa.this);
		builder.setView(cable);
		builder.setPositiveButton("Valider",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// Recherche du cable entré
						numeroCable = cable.getText().toString();

						clause = Operation.RANG_1_1_1 + "='" + numeroCable
								+ "' AND " + Operation.DESCRIPTION_OPERATION
								+ " LIKE '%Sertissage Enfichage%' AND "
								+ Operation.RANG_1_1 + " LIKE '%" + numeroCo
								+ "%' AND " + Operation.DATE_REALISATION
								+ "!='null' AND " + Operation.DATE_REALISATION
								+ "!=''";
						cursor = cr.query(urlSeq, columnsSeq, clause, null,
								null);
						if (cursor.moveToFirst()) {
							Toast.makeText(DenudageSertissageEnfichageTa.this,
									"Ce cable a dèja été utilisé",
									Toast.LENGTH_SHORT).show();

						} else if (straps.get(numeroCable) != null) {
							nbCablesAjoutes = 0;
							oldListe = liste;
							clause = Raccordement.NUMERO_FIL_CABLE + "='"
									+ numeroCable + "' AND "
									+ straps.get(numeroCable) + "='" + numeroCo
									+ "' AND " + Raccordement.FIL_SENSIBLE
									+ "='0' AND ( "
									+ Raccordement.REPRISE_BLINDAGE
									+ " IS NULL OR "
									+ Raccordement.REPRISE_BLINDAGE + "='' )";
							cursorA = cr.query(urlRac, colRac, clause, null,
									Raccordement._id);
							if (cursorA.moveToFirst()) {
								String borne = cursorA.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_BORNE_TENANT));
								boolean tenant = true;
								if (borne == null) {
									borne = cursorA.getString(cursorA
											.getColumnIndex(Raccordement.NUMERO_BORNE_ABOUTISSANT));
									tenant = false;
								}
								addCables(borne, numeroCo, tenant, true);
							}

						} else if (straps.get(numeroCable) == null) {
							nbCablesAjoutes = 0;
							oldListe = liste;
							String borneT, borneA;
							Log.e("Strap", "non ");
							// Verification de l'existence d'un strap
							clause = Raccordement.NUMERO_FIL_CABLE + "='"
									+ numeroCable + "' AND "
									+ Raccordement.NUMERO_COMPOSANT_TENANT
									+ "='" + numeroCo + "' AND "
									+ Raccordement.FIL_SENSIBLE + "='0' AND ( "
									+ Raccordement.REPRISE_BLINDAGE
									+ " IS NULL OR "
									+ Raccordement.REPRISE_BLINDAGE + "='' )";
							cursorA = cr.query(urlRac, colRac, clause, null,
									Raccordement._id);
							// Verification de l'existence d'un strap
							clause = Raccordement.NUMERO_FIL_CABLE + "='"
									+ numeroCable + "' AND "
									+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
									+ "='" + numeroCo + "' AND "
									+ Raccordement.FIL_SENSIBLE + "='0' AND ( "
									+ Raccordement.REPRISE_BLINDAGE
									+ " IS NULL OR "
									+ Raccordement.REPRISE_BLINDAGE + "='' )";
							cursorB = cr.query(urlRac, colRac, clause, null,
									Raccordement._id);

							if (cursorB.moveToFirst() && cursorA.moveToFirst()) {
								// Présence d'un strap
								Log.e("Strap", "OUI");
								borneA = cursorB.getString(cursorB
										.getColumnIndex(Raccordement.NUMERO_BORNE_ABOUTISSANT));
								borneT = cursorA.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_BORNE_TENANT));
								Cursor c1, c2;
								c1 = cr.query(
										urlRac,
										colRac,
										Raccordement.NUMERO_BORNE_TENANT
												+ "='"
												+ borneT
												+ "' AND "
												+ Raccordement.NUMERO_COMPOSANT_TENANT
												+ "='" + numeroCo + "'", null,
										null);
								c2 = cr.query(
										urlRac,
										colRac,
										Raccordement.NUMERO_BORNE_ABOUTISSANT
												+ "='"
												+ borneA
												+ "' AND "
												+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
												+ "='" + numeroCo + "'", null,
										null);
								if (c1.getCount() > c2.getCount()) {
									c1.moveToFirst();
									addCables(
											c1.getString(c1
													.getColumnIndex(Raccordement.NUMERO_BORNE_TENANT)),
											numeroCo, true, false);
									do {
										straps.put(
												c1.getString(c1
														.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)),
												Raccordement.NUMERO_COMPOSANT_ABOUTISSANT);
									} while (c1.moveToNext());
								} else {
									c2.moveToFirst();
									addCables(
											c2.getString(c2
													.getColumnIndex(Raccordement.NUMERO_BORNE_ABOUTISSANT)),
											numeroCo, false, false);
									do {
										straps.put(
												c2.getString(c2
														.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)),
												Raccordement.NUMERO_COMPOSANT_TENANT);
									} while (c2.moveToNext());
								}

							} else if (cursorA.moveToFirst()
									&& (cursorA.getCount() == 1)) {
								Log.e("Strap", "1 ");
								// Monofilaire
								cursorA.moveToFirst();
								borneT = cursorA.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_BORNE_TENANT));
								addCables(borneT, numeroCo, true, true);

							} else if (cursorA.moveToFirst()
									&& cursorA.getCount() > 1) {
								// Paire torsadé
								Log.e("Strap",
										"2"
												+ " Borne: "
												+ cursorA.getString(cursorA
														.getColumnIndex(Raccordement.NUMERO_BORNE_TENANT)));
								do {
									addCables(
											cursorA.getString(cursorA
													.getColumnIndex(Raccordement.NUMERO_BORNE_TENANT)),
											numeroCo, true, true);
								} while (cursorA.moveToNext());
							} else if (cursorB.moveToFirst()
									&& (cursorB.getCount() == 1)) {
								// Monofilaire
								Log.e("Strap", "3 ");
								addCables(
										cursorB.getString(cursorB
												.getColumnIndex(Raccordement.NUMERO_BORNE_ABOUTISSANT)),
										numeroCo, false, true);

							} else if (cursorB.moveToFirst()
									&& cursorB.getCount() > 1) {
								// Paire torsadé
								Log.e("Strap", "4 ");
								do {
									addCables(
											cursorB.getString(cursorB
													.getColumnIndex(Raccordement.NUMERO_BORNE_ABOUTISSANT)),
											numeroCo, false, true);
								} while (cursorB.moveToNext());

							} else {
								// Le cable n'est pas utilisé pour
								// ce connecteur
								Toast.makeText(
										DenudageSertissageEnfichageTa.this,
										"Ce cable ne correspond pas",
										Toast.LENGTH_SHORT).show();

							}
						}
						displayContentProvider();
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

	private void addCables(String numeroBorne, String numeroComposant,
			boolean tenant, boolean save) {
		String clause;
		if (tenant) {
			clause = Raccordement.NUMERO_BORNE_TENANT + "='" + numeroBorne
					+ "' AND " + Raccordement.NUMERO_COMPOSANT_TENANT + "='"
					+ numeroComposant + "'";
		} else {
			clause = Raccordement.NUMERO_BORNE_ABOUTISSANT + "='" + numeroBorne
					+ "' AND " + Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
					+ "='" + numeroComposant + "'";
		}
		Cursor c = cr.query(urlRac, colRac, clause, null, Raccordement._id);
		if (c.moveToFirst()) {
			dateRealisation = new Date();
			do {
				nbCablesAjoutes++;
				HashMap<String, String> element = new HashMap<String, String>();
				for (int i = 0; i < 13; i++) {
					element.put(colRac[i],
							c.getString(c.getColumnIndex(colRac[i])));
				}
				liste.add(element);

				if (save) {
					// MAJ Table de sequencement

					contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0]
							+ " " + nomPrenomOperateur[1]);
					contact.put(Operation.DATE_REALISATION,
							dateRealisation.toGMTString());
					heureRealisation.setToNow();
					contact.put(Operation.HEURE_REALISATION,
							heureRealisation.toString());
					dureeMesuree += dateRealisation.getTime()
							- dateDebut.getTime();
					contact.put(Operation.DUREE_MESUREE, dureeMesuree / 1000);
					try {
						cr.update(
								urlSeq,
								contact,
								Operation.RANG_1_1_1
										+ "='"
										+ c.getString(c
												.getColumnIndex(Raccordement.NUMERO_FIL_CABLE))
										+ "' AND "
										+ Operation.DESCRIPTION_OPERATION
										+ " LIKE '%Sertissage Enfichage%' AND "
										+ Operation.RANG_1_1 + " LIKE '%"
										+ numeroCo + "%'", null);
					} catch (Exception e) {

					}

					contact.clear();

				}

			} while (c.moveToNext());
			// MAJ de la durée
			dureeMesuree = 0;
			dateDebut = new Date();

		}

	}

}
