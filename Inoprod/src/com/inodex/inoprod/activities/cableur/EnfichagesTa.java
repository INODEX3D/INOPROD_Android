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
import android.graphics.Color;
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

public class EnfichagesTa extends Activity {

	/** Elements � r�cuperer de la vue */
	private TextView titre, numeroConnecteur, designation, repereElectrique,
			referenceInterne, longueur, gainage, positionChariot,
			referenceFabricant;
	private ImageButton boutonCheck, infoProduit, retour, boutonAide;
	private ImageButton petitePause, grandePause;
	private GridView gridView;

	/** Uri � manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;
	private Uri urlNom = NomenclatureProvider.CONTENT_URI;

	/** Tableau des op�rations � r�aliser */
	private int opId[] = null;
	private int indiceLimite = 0;
	private int nbRows;

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
	private Cursor cursor, cursorA, cursorB;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCo, clauseTotal,
			oldClauseTotal, numeroCable, description, b;
	private boolean prodAchevee;

	/** Colonnes utilis�s pour les requ�tes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.REALISABLE, Operation.DUREE_MESUREE, Operation.RANG_1_1_1 };

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

	private String colNom[] = new String[] { Cable._id,
			Cable.DESIGNATION_COMPOSANT, Cable.NUMERO_COMPOSANT,
			Cable.FAMILLE_PRODUIT, Cable.REFERENCE_FABRICANT1,
			Cable.REFERENCE_FABRICANT2, Cable.REFERENCE_INTERNE,
			Cable.REPERE_ELECTRIQUE, Cable.QUANTITE, Cable.UNITE };

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
		setContentView(R.layout.activity_enfichages_ta);

		// Initialisation du temps
		dateDebut = new Date();
		// R�cup�ration des �l�ments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();
		clauseTotal = "";

		// initialisation de la production
		prodAchevee = false;

		// R�cuperation des �l�ments de la vue
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
		boutonAide = (ImageButton) findViewById(R.id.imageButton4);
		retour = (ImageButton) findViewById(R.id.imageButton2);
		boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);
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

		// Affichage du contenu
		// Recuperation de la premi�re op�ration

		if (description.contains("T�te A")) {
			clause = new String(Raccordement.NUMERO_COMPOSANT_TENANT + "='"
					+ numeroCo + "'AND " + Raccordement.REPRISE_BLINDAGE
					+ " IS NULL ");

		} else {
			clause = new String(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
					+ "='" + numeroCo + "'AND " + Raccordement.REPRISE_BLINDAGE
					+ " IS NULL ");
		}
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

			Cursor cursorB = cr.query(urlNom, colNom, Cable.NUMERO_COMPOSANT
					+ "='" + numeroCo + "' AND " + Cable.FAMILLE_PRODUIT
					+ " LIKE '%onnector%'", null, Cable._id);
			if (cursorB.moveToFirst()) {

				designation.append(" : "
						+ cursorB.getString(cursorB
								.getColumnIndex(Cable.DESIGNATION_COMPOSANT)));
				referenceFabricant.append(" : "
						+ cursorB.getString(cursorB
								.getColumnIndex(Cable.REFERENCE_FABRICANT2)));
				referenceInterne.append(" : "
						+ cursorB.getString(cursorB
								.getColumnIndex(Cable.REFERENCE_INTERNE)));
				repereElectrique.append(" : "
						+ cursorB.getString(cursorB
								.getColumnIndex(Cable.REPERE_ELECTRIQUE)));
			}

			if (numeroOperation.startsWith("4")) {
				titre.setText(R.string.enfichageTa);

				clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='"
						+ numeroCo + "' AND  " + Raccordement.FAUX_CONTACT
						+ "='" + 0 + "' AND " + Raccordement.OBTURATEUR + "='"
						+ 0 + "' AND " + Raccordement.REPRISE_BLINDAGE
						+ " IS NULL ";
				b = Raccordement.NUMERO_BORNE_TENANT;
			} else {
				titre.setText(R.string.enfichageTb);

				clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
						+ numeroCo + "' AND  " + Raccordement.FAUX_CONTACT
						+ "='" + 0 + "' AND " + Raccordement.OBTURATEUR + "='"
						+ 0 + "' AND " + Raccordement.REPRISE_BLINDAGE
						+ " IS NULL ";
				colRac[6] = Raccordement.NUMERO_BORNE_ABOUTISSANT;
				b = Raccordement.NUMERO_BORNE_ABOUTISSANT;
			}

		}

		// Initialisation du nombre de ligne � atteindre
		nbRows = cr.query(urlRac, colRac,
				clause + " GROUP BY " + Raccordement.NUMERO_FIL_CABLE, null,
				Raccordement._id).getCount();
		Log.e("NombreLignes", "" + nbRows);

		// Affichage du temps n�cessaire
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

				// V�rification de l'�tat de la production
				if (prodAchevee) {

					// indiceCourant++;
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
							} else if (nextOperation.startsWith("Mise")) {
								toNext = new Intent(EnfichagesTa.this,
										MiseLongueurTb.class);
							}
							if (toNext != null) {

								toNext.putExtra("opId", opId);
								toNext.putExtra("Noms", nomPrenomOperateur);
								toNext.putExtra("Indice", indiceCourant);
								startActivity(toNext);
								finish();
							}

						}

						// Aucune op�ration suivante: retour au menu principal
					} catch (ArrayIndexOutOfBoundsException e) {
						Intent toNext = new Intent(EnfichagesTa.this,
								MainMenuCableur.class);
						toNext.putExtra("Noms", nomPrenomOperateur);
						toNext.putExtra("opId", opId);
						toNext.putExtra("Indice", indiceCourant);
						startActivity(toNext);
						finish();

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
					} catch (ActivityNotFoundException e) {
						entreCable("Impossible de trouver une application pour le scan. Entrez le N� de cable.");
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
				Intent toInfo = new Intent(EnfichagesTa.this, InfoProduit.class);
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

		// Retour arri�re
		retour.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (indiceLimite > 0) {
					indiceLimite--;
					Log.e("Indice", "" + indiceLimite);
					indiceCourant--;
					Log.e("Indice", "" + indiceLimite);
				}

				// MAJ de la dur�e
				dureeMesuree = 0;
				dateDebut = new Date();

				clauseTotal = oldClauseTotal;
				// V�rification de l'�tat de la production
				prodAchevee = (indiceLimite >= nbRows);
				displayContentProvider();
			}
		});

		// Grande pause
		grandePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						EnfichagesTa.this);
				builder.setMessage("�tes-vous sur de vouloir quitter l'application ?");
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
						EnfichagesTa.this);
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

	/**
	 * Affichage du contenu
	 * 
	 */
	private void displayContentProvider() {
		// Cr�ation du SimpleCursorAdapter affili� au GridView
		/*if (description.contains("T�te A")) { */
			cursor = cr.query(urlRac, colRac,
					Raccordement.NUMERO_COMPOSANT_TENANT + "='" + numeroCo
							+ "' AND (" + clauseTotal + ") AND "
							+ Raccordement.REPRISE_BLINDAGE
							+ " IS NULL ", null,
					Raccordement._id);
		/*} else {
			cursor = cr.query(urlRac, colRac,
					Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + "='" + numeroCo
							+ "' AND (" + clauseTotal + ") AND "
							+ Raccordement.REPRISE_BLINDAGE
							+ " IS NULL GROUP BY "
							+ Raccordement.NUMERO_BORNE_ABOUTISSANT, null,
					Raccordement._id);
		}*/
		Log.e("Curseur", ""+cursor.getCount());
		Log.e("Clause total", clauseTotal);
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

		// MAJ de la dur�e
		dureeMesuree = 0;
		dateDebut = new Date();

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

	/**
	 * R�cup�ration du code barre scann�
	 * 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				numeroCable = contents;
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				if (clauseTotal.contains(numeroCable)) {
					Toast.makeText(EnfichagesTa.this,
							"Ce cable a d�ja �t� utilis�", Toast.LENGTH_SHORT)
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
						int borne = cursorA.getInt(cursorA
								.getColumnIndex(b));
						Log.e("Borne", "" + borne);
						Log.e(" OU", b);
						clause =  b + "='"
								+ borne + ".0' AND ("
								+ Raccordement.NUMERO_COMPOSANT_TENANT
								+ "='" + numeroCo + "' OR "
								+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
								+ "='" + numeroCo + "' )";
						cursorB = cr.query(urlRac, colRac,clause, null, Raccordement._id);
				
						if (cursorB.moveToFirst()) {
							
							do {
								numeroCable = cursorB.getString(cursorB
										.getColumnIndex(Raccordement.NUMERO_FIL_CABLE));
								Log.e("N� Cable", numeroCable);
								if (clauseTotal.equals("")) {
									clauseTotal = Raccordement.NUMERO_FIL_CABLE
											+ "='" + numeroCable + "'";
								} else {
									
									oldClauseTotal = clauseTotal;
									clauseTotal += " OR "
											+ Raccordement.NUMERO_FIL_CABLE
											+ "='" + numeroCable + "'";
								}
								// Ajout du cable � la liste des
								// �l�ments � afficher
								indiceLimite++;
								displayContentProvider();
								indiceCourant++;
							} while (cursorB.moveToNext());
						}
					} else {
						// Le cable n'est pas utilis� pour
						// ce connecteur
						Toast.makeText(EnfichagesTa.this,
								"Ce cable ne correspond pas",
								Toast.LENGTH_SHORT).show();

					}
				}
			}

		} else if (resultCode == RESULT_CANCELED) {
			entreCable("Echec du scan. Entrez le n� de cable :");
		}

	}

	/**
	 * Entr�e du num�ro de cable au clavier dans une dialog box
	 * 
	 * @param message
	 *            � afficher dans la dialog box
	 */
	public void entreCable(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(EnfichagesTa.this);
		builder.setMessage(message);
		builder.setCancelable(false);
		final EditText cable = new EditText(EnfichagesTa.this);
		builder.setView(cable);
		builder.setPositiveButton("Valider",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// Recherche du cable entr�
						numeroCable = cable.getText().toString();
						if (clauseTotal.contains(numeroCable)) {
							Toast.makeText(EnfichagesTa.this,
									"Ce cable a d�ja �t� utilis�",
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
								clause =  b + "='"
										+ borne + ".0' AND ("
										+ Raccordement.NUMERO_COMPOSANT_TENANT
										+ "='" + numeroCo + "' OR "
										+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
										+ "='" + numeroCo + "' )";
								cursorB = cr.query(urlRac, colRac,clause, null, Raccordement._id);
						
								if (cursorB.moveToFirst()) {
									
									do {
										numeroCable = cursorB.getString(cursorB
												.getColumnIndex(Raccordement.NUMERO_FIL_CABLE));
										Log.e("N� Cable", numeroCable);
										if (clauseTotal.equals("")) {
											clauseTotal = Raccordement.NUMERO_FIL_CABLE
													+ "='" + numeroCable + "'";
										} else {
											
											oldClauseTotal = clauseTotal;
											clauseTotal += " OR "
													+ Raccordement.NUMERO_FIL_CABLE
													+ "='" + numeroCable + "'";
										}
										// Ajout du cable � la liste des
										// �l�ments � afficher
										indiceLimite++;
										displayContentProvider();
										indiceCourant++;
									} while (cursorB.moveToNext());
								}
							} else {
								// Le cable n'est pas utilis� pour
								// ce connecteur
								Toast.makeText(EnfichagesTa.this,
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
