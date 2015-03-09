package com.inodex.inoprod.activities.cableur;

import java.util.Date;

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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class DenudageSertissageContactTa extends Activity {

	/** Elements à récuperer de la vue */
	private TextView titre, numeroConnecteur, repereElectrique, longueur,
			gainage, positionChariot, instruction;
	private ImageButton boutonCheck, infoProduit, retour, boutonAide;
	private ImageButton petitePause, grandePause;
	private GridView gridView;
	private Button scan;

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
	private boolean prodAchevee;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.REALISABLE, Operation.DUREE_MESUREE, Operation.RANG_1_1_1 };

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
			Raccordement.REFERENCE_OUTIL_ABOUTISSANT,
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
			Raccordement.NUMERO_BORNE_TENANT,
			Raccordement.NUMERO_BORNE_ABOUTISSANT };

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
		setContentView(R.layout.activity_denudage_sertissage_contact_ta);
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

		// initialisation de la production
		prodAchevee = false;

		// Récuperation des éléments de la vue
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
			/*
			 * longueur.append(" : " + cursorA.getString(cursorA
			 * .getColumnIndex(Raccordement.LONGUEUR_FIL_CABLE)));
			 */

			if (numeroOperation.startsWith("4")) {
				titre.setText(R.string.denudageSertissageTa);
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT)));
				clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='"
						+ numeroCo + "' AND " + Raccordement.FAUX_CONTACT
						+ "='" + 0 + "' AND " + Raccordement.OBTURATEUR + "='"
						+ 0 + "' AND " + Raccordement.REPRISE_BLINDAGE
						+ " IS NULL ";
				b = Raccordement.NUMERO_BORNE_TENANT;
			} else {
				titre.setText(R.string.denudageSertissageTb);
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT)));
				clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
						+ numeroCo + "' AND  " + Raccordement.FAUX_CONTACT
						+ "='" + 0 + "' AND " + Raccordement.OBTURATEUR + "='"
						+ 0 + "' AND " + Raccordement.REPRISE_BLINDAGE
						+ " IS NULL ";
				colRac[8] = Raccordement.REFERENCE_OUTIL_ABOUTISSANT;
				colRac[10] = Raccordement.REGLAGE_OUTIL_ABOUTISSANT;
				colRac[11] = Raccordement.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT;
				b = Raccordement.NUMERO_BORNE_ABOUTISSANT;

			}

		}

		/*
		 * nbRows = cr.query(urlRac, colRac, clause + " GROUP BY " +
		 * Raccordement.NUMERO_FIL_CABLE, null, Raccordement._id).getCount();
		 */
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
								.contains("Sertissage de")) {
					nbRows++;
				}

			}
		}
		Log.e("NombreLignes", "" + nbRows);
		// Affichage du temps nécessaire
		timer = (TextView) findViewById(R.id.timeDisp);
		dureeTotal = 0;
		cursorTime = cr.query(urlTim, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%ertissa%' ", null, Duree._id);
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

					// Signalement du point de controle
					clause = Operation.RANG_1_1 + " LIKE '%" + numeroCo
							+ "%' AND " + Operation.DESCRIPTION_OPERATION
							+ " LIKE 'Contrôle sertissage tête A%'";
					cursor = cr.query(urlSeq, columnsSeq, clause, null,
							Operation._id);
					if (cursor.moveToFirst()) {
						contact.put(Operation.REALISABLE, 1);
						int id = cursor.getInt(cursor
								.getColumnIndex(Operation._id));
						Log.e("Controle ID", "" + id);
						cr.update(urlSeq, contact, Operation._id + "='" + id
								+ "'", null);
						contact.clear();
					}

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
							} else if (nextOperation.startsWith("Mise")) {
								toNext = new Intent(
										DenudageSertissageContactTa.this,
										MiseLongueurTb.class);
							} else if (nextOperation.startsWith("Mise")) {
								toNext = new Intent(
										DenudageSertissageContactTa.this,
										MiseLongueurTb.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage Coss")) {
								toNext = new Intent(
										DenudageSertissageContactTa.this,
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
								DenudageSertissageContactTa.this,
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
				Intent toInfo = new Intent(DenudageSertissageContactTa.this,
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
						DenudageSertissageContactTa.this);
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
						DenudageSertissageContactTa.this);
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
		cursor = cr.query(urlRac, colRac, "("
				+ Raccordement.NUMERO_COMPOSANT_TENANT + "='" + numeroCo
				+ "' OR " + Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + "='"
				+ numeroCo + "') AND (" + clauseTotal + ") AND "
				+ Raccordement.REPRISE_BLINDAGE + " IS NULL ", null,
				Raccordement._id);
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.grid_layout_denudage_sertissage_contact_ta, cursor,
				colRac, layouts);

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

		/*
		 * cr.update(urlSeq, contact, Operation._id + " = ?", new String[] {
		 * Integer.toString(opId[indiceCourant]) });
		 */
		cr.update(urlSeq, contact, Operation.RANG_1_1_1 + "='" + numeroCable
				+ "' AND " + Operation.DESCRIPTION_OPERATION
				+ " LIKE '%Denudage Sertissage de%' AND " + Operation.RANG_1_1
				+ " LIKE '%" + numeroCo + "%'", null);

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
				Log.e("SCAN", "OK");
				String contents = intent.getStringExtra("SCAN_RESULT");
				numeroCable = contents;
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

				if (clauseTotal.contains(numeroCable)) {
					// Cable dèja utilisé
					Toast.makeText(DenudageSertissageContactTa.this,
							"Ce cable a dèja été utilisé", Toast.LENGTH_SHORT)
							.show();
				} else {
					clause = Raccordement.NUMERO_FIL_CABLE + "='" + numeroCable
							+ "' AND (" + Raccordement.NUMERO_COMPOSANT_TENANT
							+ "='" + numeroCo + "' OR "
							+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + "='"
							+ numeroCo + "' )";
					cursorA = cr.query(urlRac, colRac, clause, null,
							Raccordement._id);
					if (cursorA.moveToFirst()) {
						if (clauseTotal.equals(" ")) {
							clauseTotal = Raccordement.NUMERO_FIL_CABLE + "='"
									+ numeroCable + "'";
						} else {
							oldClauseTotal = clauseTotal;
							clauseTotal += " OR "
									+ Raccordement.NUMERO_FIL_CABLE + "='"
									+ numeroCable + "'";
						}
						// Ajout du cable à la liste des
						// éléments à afficher

						Log.e("clause", clauseTotal);
						indiceLimite++;
						displayContentProvider();
						indiceCourant++;
					} else {
						// Le cable n'est pas utilisé pour
						// ce connecteur
						Toast.makeText(DenudageSertissageContactTa.this,
								"Ce cable ne correspond pas",
								Toast.LENGTH_SHORT).show();

					}
				}
			}

		} else if (resultCode == RESULT_CANCELED) {
			entreCable("Echec du scan. Entrez le n° de cable :");
		}
	}

	public void entreCable(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				DenudageSertissageContactTa.this);
		builder.setMessage(message);
		builder.setCancelable(false);
		final EditText cable = new EditText(DenudageSertissageContactTa.this);
		builder.setView(cable);
		builder.setPositiveButton("Valider",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// Recherche du cable entré
						numeroCable = cable.getText().toString();
						if (clauseTotal.contains(numeroCable)) {
							// Cable dèja utilisé
							Toast.makeText(DenudageSertissageContactTa.this,
									"Ce cable a dèja été utilisé",
									Toast.LENGTH_SHORT).show();
						} else {

							clause = Raccordement.NUMERO_FIL_CABLE + "='"
									+ numeroCable + "' AND ("
									+ Raccordement.NUMERO_COMPOSANT_TENANT
									+ "='" + numeroCo + "' OR "
									+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
									+ "='" + numeroCo + "' )";
							cursorA = cr.query(urlRac, colRac, clause, null,
									Raccordement._id);
							if (cursorA.moveToFirst()) {
								int borne = cursorA.getInt(cursorA
										.getColumnIndex(b));
								Log.e("Borne", "" + borne);
								Log.e(" OU", b);
								clause = b
										+ "='"
										+ borne
										+ ".0' AND ("
										+ Raccordement.NUMERO_COMPOSANT_TENANT
										+ "='"
										+ numeroCo
										+ "' OR "
										+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
										+ "='" + numeroCo + "' )";
								cursorB = cr.query(urlRac, colRac, clause,
										null, Raccordement._id);

								if (cursorB.moveToFirst()) {

									do {
										numeroCable = cursorB.getString(cursorB
												.getColumnIndex(Raccordement.NUMERO_FIL_CABLE));
										Log.e("N° Cable", numeroCable);
										if (clauseTotal.equals(" ")) {
											clauseTotal = Raccordement.NUMERO_FIL_CABLE
													+ "='" + numeroCable + "'";
										} else {

											oldClauseTotal = clauseTotal;
											clauseTotal += " OR "
													+ Raccordement.NUMERO_FIL_CABLE
													+ "='" + numeroCable + "'";
										}
										// Ajout du cable à la liste des
										// éléments à afficher
										indiceLimite++;
										displayContentProvider();
										indiceCourant++;
									} while (cursorB.moveToNext());
								}
							} else {
								// Le cable n'est pas utilisé pour
								// ce connecteur
								Toast.makeText(
										DenudageSertissageContactTa.this,
										"Ce cable ne correspond pas",
										Toast.LENGTH_SHORT).show();

							}
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
