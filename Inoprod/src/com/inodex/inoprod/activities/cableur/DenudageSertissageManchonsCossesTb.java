package com.inodex.inoprod.activities.cableur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
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

public class DenudageSertissageManchonsCossesTb extends Activity {

	/** Elements à récuperer de la vue */
	private TextView titre, numeroConnecteur, designation, repereElectrique,
			referenceInterne, longueur, gainage, positionChariot,
			referenceFabricant, referencePince, numeroSeriePince, zone,
			empreinte;
	private ImageButton boutonCheck, infoProduit, retour, boutonAide,
			petitePause, grandePause;
	private GridView gridView;

	/** Uri à manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;
	private Uri urlNom = NomenclatureProvider.CONTENT_URI;

	/** Liste à afficher dans l'adapteur */
	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();

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

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA, cursorB;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCo, clauseTotal,
			oldClauseTotal, numeroCable, b;
	private boolean prodAchevee, teteB;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.DUREE_MESUREE, Operation.RANG_1_1_1 };

	private int layouts[] = new int[] { R.id.statutLiaison,
			R.id.numeroRevisionLiaison, R.id.typeCable1, R.id.numeroFil1,
			R.id.numeroFilDansCable1, R.id.couleurFil1, R.id.numeroBorne,
			R.id.typeCable2, R.id.numeroFil2, R.id.numeroFilDansCable2,
			R.id.couleurFil2 };

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

			Raccordement.REGLAGE_OUTIL_ABOUTISSANT,
			Raccordement.REGLAGE_OUTIL_TENANT, Raccordement.ZONE_ACTIVITE,
			Raccordement.LOCALISATION1,
			Raccordement.NUMERO_REPERE_TABLE_CHEMINEMENT,
			Raccordement.REFERENCE_OUTIL_ABOUTISSANT };

	private String colNom[] = new String[] { Cable.REFERENCE_INTERNE,
			Cable.REFERENCE_FABRICANT2, Cable.QUANTITE, Cable.UNITE,
			Cable.NUMERO_COMPOSANT, Cable.FAMILLE_PRODUIT, Cable._id,
			Cable.DESIGNATION_COMPOSANT };

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
		setContentView(R.layout.activity_denudage_sertissage_manchons_cosses_tb);
		// Récupération des éléments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();
		clauseTotal = "";
		dateDebut = new Date();

		// initialisation de la production
		prodAchevee = false;

		// Récuperation des éléments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		titre = (TextView) findViewById(R.id.textView1);
		numeroConnecteur = (TextView) findViewById(R.id.textView3);
		designation = (TextView) findViewById(R.id.textView3a);
		repereElectrique = (TextView) findViewById(R.id.textView5);
		referenceInterne = (TextView) findViewById(R.id.textView5aa);
		numeroSeriePince = (TextView) findViewById(R.id.TextView01);
		referencePince = (TextView) findViewById(R.id.textView7);
		gainage = (TextView) findViewById(R.id.textView5a);
		positionChariot = (TextView) findViewById(R.id.textView5b);
		referenceFabricant = (TextView) findViewById(R.id.textView5d);
		longueur = (TextView) findViewById(R.id.textView5c);
		boutonAide = (ImageButton) findViewById(R.id.imageButton4);
		retour = (ImageButton) findViewById(R.id.imageButton2);
		boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);
		petitePause = (ImageButton) findViewById(R.id.imageButton1);
		grandePause = (ImageButton) findViewById(R.id.exitButton1);
		zone = (TextView) findViewById(R.id.TextView02);

		// Récuperation du numéro d'opération courant
		clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
		cursor = cr.query(urlSeq, columnsSeq, clause, null, Operation._id
				+ " ASC");
		if (cursor.moveToFirst()) {
			numeroOperation = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
			numeroCo = (cursor.getString(cursor
					.getColumnIndex(Operation.RANG_1_1))).substring(11);
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

			referenceInterne.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Cable.REFERENCE_INTERNE)));
			referenceFabricant.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Cable.REFERENCE_FABRICANT2)));
			designation.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Cable.DESIGNATION_COMPOSANT)));

		}

		// Recuperation de la première opération
		clause = new String(Raccordement.NUMERO_COMPOSANT_TENANT + "='"
				+ numeroCo + "' OR "
				+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + "='" + numeroCo
				+ "'");
		cursorA = cr.query(urlRac, colRac, clause, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {

			positionChariot
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.NUMERO_POSITION_CHARIOT)));

			zone.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Raccordement.ZONE_ACTIVITE))
					+ "-"
					+ cursorA.getString(cursorA
							.getColumnIndex(Raccordement.LOCALISATION1))
					+ "-"
					+ cursorA.getString(cursorA
							.getColumnIndex(Raccordement.NUMERO_REPERE_TABLE_CHEMINEMENT)));
			numeroSeriePince.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Raccordement.NUMERO_SERIE_OUTIL)));

			if (numeroOperation.startsWith("4")) {
				titre.setText(R.string.denudageSertissageEnfichageTa);
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT)));
				referencePince
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REFERENCE_OUTIL_TENANT)));
				empreinte
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REGLAGE_OUTIL_TENANT)));

				clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='"
						+ numeroCo + "' AND ( " + Raccordement.FAUX_CONTACT
						+ "='" + 0 + "' OR " + Raccordement.OBTURATEUR + "='"
						+ 0 + "' OR " + Raccordement.REPRISE_BLINDAGE
						+ " IS NULL )";
				teteB = false;
				b = Raccordement.NUMERO_BORNE_TENANT;
			} else {
				titre.setText(R.string.denudageSertissageEnfichageTb);
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT)));
				try {
					empreinte
							.append(" : "
									+ cursorA.getString(cursorA
											.getColumnIndex(Raccordement.REGLAGE_OUTIL_ABOUTISSANT)));
				} catch (NullPointerException e) {

				}
				referencePince
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REFERENCE_OUTIL_ABOUTISSANT)));
				clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
						+ numeroCo + "' AND ( " + Raccordement.FAUX_CONTACT
						+ "='" + 0 + "' OR " + Raccordement.OBTURATEUR + "='"
						+ 0 + "' OR " + Raccordement.REPRISE_BLINDAGE
						+ " IS NULL )";
				teteB = true;
				b = Raccordement.NUMERO_BORNE_ABOUTISSANT;
			}

		}

		// Initialisation du nombre de ligne à atteindre
		nbRows = cr.query(urlRac, colRac,
				clause + " GROUP BY " + Raccordement.NUMERO_FIL_CABLE, null,
				Raccordement._id).getCount();

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
								.contains("Denudage")) {
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

		// Affichage du contenu
		displayContentProvider();

		// Etape suivante

		// Info Produit

		// Scan
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
								toNext = new Intent(
										DenudageSertissageManchonsCossesTb.this,
										PreparationTa.class);
							} else if (nextOperation.startsWith("Reprise")) {
								toNext = new Intent(
										DenudageSertissageManchonsCossesTb.this,
										RepriseBlindageTa.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage Enfichage")) {
								toNext = new Intent(
										DenudageSertissageManchonsCossesTb.this,
										DenudageSertissageEnfichageTa.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage de")) {
								toNext = new Intent(
										DenudageSertissageManchonsCossesTb.this,
										DenudageSertissageContactTa.class);
							} else if (nextOperation.startsWith("Finalisation")) {
								toNext = new Intent(
										DenudageSertissageManchonsCossesTb.this,
										FinalisationTa.class);
							} else if (nextOperation.startsWith("Tri")) {
								toNext = new Intent(
										DenudageSertissageManchonsCossesTb.this,
										TriAboutissantsTa.class);
							} else if (nextOperation
									.startsWith("Positionnement")) {
								toNext = new Intent(
										DenudageSertissageManchonsCossesTb.this,
										PositionnementTaTab.class);
							} else if (nextOperation.startsWith("Cheminement")) {
								toNext = new Intent(
										DenudageSertissageManchonsCossesTb.this,
										CheminementTa.class);
							} else if (nextOperation.startsWith("Mise")) {
								toNext = new Intent(
										DenudageSertissageManchonsCossesTb.this,
										MiseLongueurTb.class);
							} else if (nextOperation.startsWith("Mise")) {
								toNext = new Intent(
										DenudageSertissageManchonsCossesTb.this,
										MiseLongueurTb.class);
							} else if (nextOperation.startsWith("Denudage Sertissage Coss")) {
								toNext = new Intent(
										DenudageSertissageManchonsCossesTb.this,
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
								DenudageSertissageManchonsCossesTb.this,
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

		// Grande pause
		grandePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						DenudageSertissageManchonsCossesTb.this);
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

		// Info Produit

		infoProduit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cursorInfo = cr.query(urlRac, colInfo,
						Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
								+ numeroCo + "' OR "
								+ Raccordement.NUMERO_COMPOSANT_TENANT + "='"
								+ numeroCo + "'", null, null);
				Intent toInfo = new Intent(
						DenudageSertissageManchonsCossesTb.this,
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
						DenudageSertissageManchonsCossesTb.this);
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
		SimpleAdapter sca = new SimpleAdapter(this, liste,
				R.layout.grid_layout_denudage_sertissage_manchons_cosses_tb,
				colRac, layouts);
		gridView.setAdapter(sca);
		dateRealisation = new Date();
		contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0] + " "
				+ nomPrenomOperateur[1]);
		contact.put(Operation.DATE_REALISATION, dateRealisation.toGMTString());
		heureRealisation.setToNow();
		contact.put(Operation.HEURE_REALISATION, heureRealisation.toString());
		dureeMesuree += dateRealisation.getTime() - dateDebut.getTime();
		contact.put(Operation.DUREE_MESUREE, dureeMesuree / 1000);
		/*
		 * cr.update(urlSeq, contact, Operation._id + " = ?", new String[] {
		 * Integer.toString(opId[indiceCourant]) });
		 */
		cr.update(urlSeq, contact, Operation.DESCRIPTION_OPERATION
				+ " LIKE '%Cosse%' AND " + Operation.RANG_1_1 + " LIKE '%"
				+ numeroCo + "%'", null);
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
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

				clause = Raccordement.NUMERO_FIL_CABLE + "='" + numeroCable
						+ "' AND (" + Raccordement.NUMERO_COMPOSANT_TENANT
						+ "='" + numeroCo + "' OR "
						+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + "='"
						+ numeroCo + "' )";
				cursorA = cr.query(urlRac, colRac, clause, null,
						Raccordement._id);
				if (cursorA.moveToFirst()) {
					HashMap<String, String> element;
					clause = Raccordement.NUMERO_FIL_DANS_CABLE
							+ " LIKE '%"
							+ cursorA
									.getInt(cursorA
											.getColumnIndex(Raccordement.NUMERO_FIL_DANS_CABLE))
							+ "%' AND (" + Raccordement.NUMERO_COMPOSANT_TENANT
							+ "='" + numeroCo + "' OR "
							+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + "='"
							+ numeroCo + "' )";
					cursorB = cr.query(urlRac, colRac, clause, null,
							Raccordement._id);

					if (cursorB.moveToFirst()) {

						do {
							element = new HashMap<String, String>();
							if (cursorB.getFloat(cursorB.getColumnIndex(b)) == 1.0) {

								element.put(
										colRac[0],
										""
												+ cursorB.getString(cursorB
														.getColumnIndex(Raccordement.ETAT_LIAISON_FIL)));
								element.put(
										colRac[1],
										""
												+ cursorB.getString(cursorB
														.getColumnIndex(Raccordement.NUMERO_REVISION_FIL)));
								element.put(
										colRac[2],
										""
												+ cursorB.getString(cursorB
														.getColumnIndex(Raccordement.TYPE_FIL_CABLE)));
								element.put(
										colRac[3],
										""
												+ cursorB.getString(cursorB
														.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));
								element.put(
										colRac[4],
										""
												+ cursorB.getString(cursorB
														.getColumnIndex(Raccordement.NUMERO_FIL_DANS_CABLE)));
								element.put(
										colRac[5],
										""
												+ cursorB.getString(cursorB
														.getColumnIndex(Raccordement.COULEUR_FIL)));
								element.put(
										colRac[6],
										""
												+ cursorB.getString(cursorB
														.getColumnIndex(b)));
							} else {
								element.put(
										colRac[6],
										""
												+ cursorB.getString(cursorB
														.getColumnIndex(b)));
								element.put(
										colRac[7],
										""
												+ cursorB.getString(cursorB
														.getColumnIndex(Raccordement.TYPE_FIL_CABLE)));
								element.put(
										colRac[8],
										""
												+ cursorB.getString(cursorB
														.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));
								element.put(
										colRac[9],
										""
												+ cursorB.getString(cursorB
														.getColumnIndex(Raccordement.NUMERO_FIL_DANS_CABLE)));
								element.put(
										colRac[10],
										""
												+ cursorB.getString(cursorB
														.getColumnIndex(Raccordement.COULEUR_FIL)));

							}
							liste.add(element);
							indiceLimite++;
							displayContentProvider();
							// indiceCourant++;

						} while (cursorA.moveToNext());

						// Ajout du cable à la liste des
						// éléments à afficher
					}

				} else {
					Toast.makeText(DenudageSertissageManchonsCossesTb.this,
							"Ce cable ne correspond pas", Toast.LENGTH_SHORT)
							.show();
				}

			} else if (resultCode == RESULT_CANCELED) {
				// entreCable("Impossible de trouver une application pour le scan. Entrez le N° de cable.");
			}
		}
	}

	public void entreCable(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				DenudageSertissageManchonsCossesTb.this);
		builder.setMessage(message);
		builder.setCancelable(false);
		final EditText cable = new EditText(
				DenudageSertissageManchonsCossesTb.this);
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
							Log.e("Cable", "trouve");
							clause = Raccordement.NUMERO_FIL_DANS_CABLE
									+ " LIKE '%"
									+ cursorA.getInt(cursorA
											.getColumnIndex(Raccordement.NUMERO_FIL_DANS_CABLE))
									+ "%' AND ("
									+ Raccordement.NUMERO_COMPOSANT_TENANT
									+ "='" + numeroCo + "' OR "
									+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
									+ "='" + numeroCo + "' )";
							cursorB = cr.query(urlRac, colRac, clause, null,
									Raccordement._id);

							if (cursorB.moveToFirst()) {
								Log.e("Borne", "" + b);
								Log.e("Cable", "trouve 2");
								do {
									element = new HashMap<String, String>();
									if (cursorB.getFloat(cursorB
											.getColumnIndex(b)) == 1.0) {

										element.put(
												colRac[0],
												""
														+ cursorB
																.getString(cursorB
																		.getColumnIndex(Raccordement.ETAT_LIAISON_FIL)));
										element.put(
												colRac[1],
												""
														+ cursorB
																.getString(cursorB
																		.getColumnIndex(Raccordement.NUMERO_REVISION_FIL)));
										element.put(
												colRac[2],
												""
														+ cursorB
																.getString(cursorB
																		.getColumnIndex(Raccordement.TYPE_FIL_CABLE)));
										element.put(
												colRac[3],
												""
														+ cursorB
																.getString(cursorB
																		.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));
										element.put(
												colRac[4],
												""
														+ cursorB
																.getString(cursorB
																		.getColumnIndex(Raccordement.NUMERO_FIL_DANS_CABLE)));
										element.put(
												colRac[5],
												""
														+ cursorB
																.getString(cursorB
																		.getColumnIndex(Raccordement.COULEUR_FIL)));
										element.put(
												colRac[6],
												""
														+ cursorB
																.getString(cursorB
																		.getColumnIndex(b)));
									} else {
										element.put(
												colRac[6],
												""
														+ cursorB
																.getString(cursorB
																		.getColumnIndex(b)));
										element.put(
												colRac[7],
												""
														+ cursorB
																.getString(cursorB
																		.getColumnIndex(Raccordement.TYPE_FIL_CABLE)));
										element.put(
												colRac[8],
												""
														+ cursorB
																.getString(cursorB
																		.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));
										element.put(
												colRac[9],
												""
														+ cursorB
																.getString(cursorB
																		.getColumnIndex(Raccordement.NUMERO_FIL_DANS_CABLE)));
										element.put(
												colRac[10],
												""
														+ cursorB
																.getString(cursorB
																		.getColumnIndex(Raccordement.COULEUR_FIL)));

									}
									liste.add(element);
									indiceLimite++;
									displayContentProvider();
									indiceCourant++;

								} while (cursorB.moveToNext());

								// Ajout du cable à la liste des
								// éléments à afficher
							}

						} else {
							Toast.makeText(
									DenudageSertissageManchonsCossesTb.this,
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
