package com.inodex.inoprod.activities.cableur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.business.CheminementProvider;
import com.inodex.inoprod.business.Durees.Duree;
import com.inodex.inoprod.business.Nomenclature.Cable;
import com.inodex.inoprod.business.DureesProvider;
import com.inodex.inoprod.business.NomenclatureProvider;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TimeConverter;
import com.inodex.inoprod.business.TableCheminement.Cheminement;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class Frettage extends Activity {

	/** Elements à récuperer de la vue */
	private TextView typeFrette, nombrePoints, zone, referenceInterne,
			referenceFabricant;
	private ImageButton boutonCheck, infoProduit, boutonAnnuler, boutonAide;
	private ImageButton petitePause, grandePause;
	private GridView gridView;

	/** Uri à manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;
	private Uri urlChe = CheminementProvider.CONTENT_URI;
	private Uri urlNom = NomenclatureProvider.CONTENT_URI;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;

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
	private Cursor cursor, cursorA, cursorB;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCo, description;
	private boolean prodAchevee;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DUREE_MESUREE,
			Operation.DESCRIPTION_OPERATION };

	private int layouts[] = new int[] { R.id.numeroSection,
			R.id.numeroSegregation, R.id.numeroConnecteur,
			R.id.repereElectrique, R.id.typeSupportAboutissant,
			R.id.zoneLocalisation1, R.id.localisationZoneFrettage,
			R.id.typeSupportAboutissant, R.id.zoneLocalisation2 };

	private String colRac[] = new String[] { Raccordement._id,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_CHEMINEMENT,
			Raccordement.NUMERO_SECTION_CHEMINEMENT,
			Raccordement.NUMERO_POSITION_CHARIOT, Raccordement.ZONE_ACTIVITE,
			Raccordement.LOCALISATION1, Raccordement.REFERENCE_INTERNE,
			Raccordement.REFERENCE_FABRICANT2 };

	private String colChe[] = new String[] { Cheminement.LOCALISATION1,
			Cheminement.NUMERO_COMPOSANT_TENANT,
			Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT,
			Cheminement.NUMERO_SECTION_CHEMINEMENT,
			Cheminement.ORDRE_REALISATION,
			Cheminement.REPERE_ELECTRIQUE_TENANT, Cheminement.ZONE_ACTIVITE,
			Cheminement._id, Cheminement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Cheminement.NUMERO_COMPOSANT_ABOUTISSANT, Cheminement.TYPE_SUPPORT

	};

	private String colNom[] = new String[] { Cable._id,
			Cable.DESIGNATION_COMPOSANT, Cable.NUMERO_COMPOSANT,
			Cable.FAMILLE_PRODUIT, Cable.REFERENCE_FABRICANT1,
			Cable.REFERENCE_FABRICANT2, Cable.REFERENCE_INTERNE, Cable.UNITE,
			Cable.QUANTITE };

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
		setContentView(R.layout.activity_frettage);

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

		referenceInterne = (TextView) findViewById(R.id.textView7);
		referenceFabricant = (TextView) findViewById(R.id.textView6);
		zone = (TextView) findViewById(R.id.textView5);
		typeFrette = (TextView) findViewById(R.id.textView3);
		nombrePoints = (TextView) findViewById(R.id.textView4);
		boutonAide = (ImageButton) findViewById(R.id.imageButton2);
		boutonAnnuler = (ImageButton) findViewById(R.id.imageButton4);
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
			Log.e("N° Connecteur", "" + numeroCo);
cursorA = cr.query(urlNom, colNom, Cable.NUMERO_COMPOSANT + "='"
					+ numeroCo + "' AND " + Cable.FAMILLE_PRODUIT
					+ " LIKE '%Frette%'", null, null);
			if (cursorA.moveToFirst()) {
				referenceInterne.append((" : " + cursorA.getString(cursorA
						.getColumnIndex(Cable.REFERENCE_INTERNE))));
				referenceFabricant.append((" : " + cursorA.getString(cursorA
						.getColumnIndex(Cable.REFERENCE_FABRICANT2))));
				typeFrette.append((" : " + cursorA.getString(cursorA
						.getColumnIndex(Cable.FAMILLE_PRODUIT))));
				nombrePoints.append((" : " + cursorA.getString(cursorA
						.getColumnIndex(Cable.QUANTITE))));
			}
			

			cursorA = cr.query(urlRac, colRac,
					Raccordement.NUMERO_COMPOSANT_TENANT + "='" + numeroCo
							+ "' OR "
							+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + "='"
							+ numeroCo + "'", null, Raccordement._id);
			if (cursorA.moveToFirst()) {

				zone.append(" : "
						+ cursorA.getString(cursorA
								.getColumnIndex(Raccordement.ZONE_ACTIVITE))
						+ "-"
						+ cursorA.getString(cursorA
								.getColumnIndex(Raccordement.LOCALISATION1)));

				HashMap<String, String> element = new HashMap<String, String>();

				int numeroSection = cursorA
						.getInt(cursorA
								.getColumnIndex(Raccordement.NUMERO_SECTION_CHEMINEMENT));
				Log.d("Numéro section", "" + numeroSection);
				element.put(colRac[0], "" + numeroSection);
				element.put(colRac[2], numeroCo);
				element.put(colRac[3], cursorA.getString(cursorA
						.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT)));
				cursorB = cr.query(urlChe, colChe,
						Cheminement.NUMERO_SECTION_CHEMINEMENT + "='"
								+ numeroSection + "'", null, Cheminement._id);
				String zonePose = "";

				if (cursorB.moveToFirst()) {
					element.put(colRac[4], cursorB.getString(cursorB
							.getColumnIndex(Cheminement.TYPE_SUPPORT)));
					element.put(
							colRac[5],
							cursorB.getString(cursorB
									.getColumnIndex(Cheminement.ZONE_ACTIVITE))
									+ "-"
									+ cursorB.getString(cursorB
											.getColumnIndex(Cheminement.LOCALISATION1))
									+ "-"
									+ cursorB.getString(cursorB
											.getColumnIndex(Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT)));

				/*	do {
						zonePose += cursorB.getString(cursorB
								.getColumnIndex(Cheminement.ZONE_ACTIVITE))
								+ "-"
								+ cursorB
										.getString(cursorB
												.getColumnIndex(Cheminement.LOCALISATION1))
								+ "-"
								+ cursorB
										.getString(cursorB
												.getColumnIndex(Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT))
								+ ", ";

					} while (cursorB.moveToNext()); */

				}
				element.put(colRac[6], zonePose);
				liste.add(element);
			}

		}

		// Etape suivante
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// Signalement du point de controle
				clause = Operation.RANG_1_1 + " LIKE '%" + numeroCo + "%' AND "
						+ Operation.NUMERO_OPERATION + " LIKE '7-%' ";
				cursor = cr.query(urlSeq, columnsSeq, clause, null,
						Operation._id);
				if (cursor.moveToFirst()) {

					contact.put(Operation.REALISABLE, 1);
					int id = cursor.getInt(cursor.getColumnIndex(Operation._id));
					cr.update(urlSeq, contact, clause, null);
					contact.clear();
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
						if (nextOperation.startsWith("Préparation")) {
							toNext = new Intent(Frettage.this,
									PreparationTa.class);
						} else if (nextOperation.startsWith("Reprise")) {
							toNext = new Intent(Frettage.this,
									RepriseBlindageTa.class);
						} else if (nextOperation
								.startsWith("Denudage Sertissage Enfichage")) {
							toNext = new Intent(Frettage.this,
									DenudageSertissageEnfichageTa.class);
						} else if (nextOperation
								.startsWith("Denudage Sertissage de")) {
							toNext = new Intent(Frettage.this,
									EnfichagesTa.class);
						} else if (nextOperation.startsWith("Finalisation")) {
							toNext = new Intent(Frettage.this,
									FinalisationTa.class);
						} else if (nextOperation.startsWith("Tri")) {
							toNext = new Intent(Frettage.this,
									TriAboutissantsTa.class);
						} else if (nextOperation.startsWith("Positionnement")) {
							toNext = new Intent(Frettage.this,
									PositionnementTaTab.class);
						} else if (nextOperation.startsWith("Cheminement")) {
							toNext = new Intent(Frettage.this,
									CheminementTa.class);
						} else if (nextOperation.startsWith("Frettage")) {
							toNext = new Intent(Frettage.this, Frettage.class);
						} else if (nextOperation.startsWith("Mise")) {
							toNext = new Intent(Frettage.this,
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

				} catch (ArrayIndexOutOfBoundsException e) {
					Intent toNext = new Intent(Frettage.this,
							MainMenuCableur.class);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("opId", opId);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);
					finish();
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
				Intent toInfo = new Intent(Frettage.this, InfoProduit.class);
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
						Frettage.this);
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
						Frettage.this);
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

		// Affichage du contenu
		displayContentProvider();

	}

	private void displayContentProvider() {

		// Création du SimpleCursorAdapter affilié au GridView
		SimpleAdapter sca = new SimpleAdapter(this, liste,
				R.layout.grid_layout_frettage, colRac, layouts);

		gridView.setAdapter(sca);

		// Affichage du temps nécessaire
		timer = (TextView) findViewById(R.id.timeDisp);
		dureeTotal = 0;
		cursorTime = cr.query(urlTim, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%rettage%' ", null, Duree._id);
		if (cursorTime.moveToFirst()) {
			dureeTotal += TimeConverter.convert(cursorTime.getString(cursorTime
					.getColumnIndex(Duree.DUREE_THEORIQUE)));

		}
		dureeTotal = dureeTotal * liste.size();
		timer.setTextColor(Color.GREEN);
		timer.setText(TimeConverter.display(dureeTotal));

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

	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}

}
