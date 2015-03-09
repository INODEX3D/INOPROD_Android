package com.inodex.inoprod.activities.cableur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.business.CheminementProvider;
import com.inodex.inoprod.business.DureesProvider;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TimeConverter;
import com.inodex.inoprod.business.Durees.Duree;
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

public class PositionnementTaTab extends Activity {

	/** Elements à récuperer de la vue */
	private TextView titre, numeroConnecteur, numeroCheminement,
			repereElectrique, zone, positionChariot;
	private ImageButton boutonCheck, infoProduit, retour;
	private ImageButton petitePause, grandePause;
	private GridView gridView, gridView1;

	/** Uri à manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;
	private Uri urlChe = CheminementProvider.CONTENT_URI;

	private List<HashMap<String, String>> liste1 = new ArrayList<HashMap<String, String>>();
	private List<HashMap<String, String>> liste2 = new ArrayList<HashMap<String, String>>();

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;

	/** Tableau des infos produit */
	private String labels[];

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

	private String clause, numeroOperation, numeroCo, description, clauseTotal;;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DUREE_MESUREE };

	private int layouts1[] = new int[] { R.id.numeroSection,
			R.id.typeSupportAboutissant, R.id.zoneLocalisation,
			R.id.numeroSegregation };

	private int layouts2[] = new int[] { R.id.numeroSection,
			R.id.localisationZone, R.id.typeSupportAboutissant,
			R.id.zoneLocalisation, R.id.numeroSegregation };

	private String colRac1[] = new String[] {
			Raccordement.NUMERO_SECTION_CHEMINEMENT,
			Raccordement.TYPE_RACCORDEMENT_ABOUTISSANT,
			Raccordement.NUMERO_COMPOSANT_TENANT, Raccordement.ZONE_ACTIVITE,
			Raccordement._id, Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_OPERATION,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.NUMERO_CHEMINEMENT, Raccordement.LOCALISATION1 };

	private String colRac2[] = new String[] {
			Raccordement.NUMERO_SECTION_CHEMINEMENT,
			Raccordement.TYPE_RACCORDEMENT_ABOUTISSANT,
			Raccordement.NUMERO_COMPOSANT_TENANT, Raccordement.ZONE_ACTIVITE,
			Raccordement._id, Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_OPERATION,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.NUMERO_CHEMINEMENT };

	private String colChe1[] = new String[] {
			Cheminement.NUMERO_SECTION_CHEMINEMENT, Cheminement.LOCALISATION1,
			Cheminement.NUMERO_COMPOSANT_TENANT,
			Cheminement.NUMERO_COMPOSANT_ABOUTISSANT,
			Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT,
			Cheminement.TYPE_SUPPORT,

			Cheminement.ORDRE_REALISATION,
			Cheminement.REPERE_ELECTRIQUE_TENANT,
			Cheminement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Cheminement.ZONE_ACTIVITE, Cheminement._id

	};

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
		setContentView(R.layout.activity_positionnement_ta_tab);
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
		gridView = (GridView) findViewById(R.id.gridview);
		gridView1 = (GridView) findViewById(R.id.GridView01);
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
			description = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
			numeroCo = (cursor.getString(cursor
					.getColumnIndex(Operation.RANG_1_1))).substring(11, 14);
			numeroConnecteur.append(" : " + numeroCo);
		}

		clause = new String(Raccordement.NUMERO_COMPOSANT_TENANT + "='"
				+ numeroCo + "'" /*
								 * + "' GROUP BY " +
								 * Raccordement.NUMERO_COMPOSANT_TENANT
								 */);

		cursorA = cr.query(urlRac, colRac1, clause, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {

			positionChariot
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.NUMERO_POSITION_CHARIOT)));
			repereElectrique
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT)));
			zone.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Raccordement.ZONE_ACTIVITE))
					+ "-"
					+ cursorA.getString(cursorA
							.getColumnIndex(Raccordement.LOCALISATION1)));
			numeroCheminement.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Raccordement.NUMERO_CHEMINEMENT)));
		}

		// Affichage du temps nécessaire
		timer = (TextView) findViewById(R.id.timeDisp);
		dureeTotal = 0;
		cursorTime = cr.query(urlTim, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%hemine%' ", null, Duree._id);
		if (cursorTime.moveToFirst()) {
			dureeTotal += TimeConverter.convert(cursorTime.getString(cursorTime
					.getColumnIndex(Duree.DUREE_THEORIQUE)));

		}

		timer.setTextColor(Color.GREEN);
		timer.setText(TimeConverter.display(dureeTotal));

		// Affichage du contenu
		displayContentProvider();

		// Etape suivante

		// Etape suivante
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				indiceCourant++;
				String nextOperation = null;
				try {
					int test = opId[indiceCourant];

					Intent toNext = new Intent(PositionnementTaTab.this,
							CheminementTa.class);

					toNext.putExtra("opId", opId);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);
					finish();

				} catch (ArrayIndexOutOfBoundsException e) {
					Intent toNext = new Intent(PositionnementTaTab.this,
							MainMenuCableur.class);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("opId", opId);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);
					finish();

				}
			}

		});

		// Grande pause
		grandePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						PositionnementTaTab.this);
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
						PositionnementTaTab.this);
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

		// Info Produit

		infoProduit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cursorInfo = cr.query(urlRac, colInfo,
						Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
								+ numeroCo + "' OR "
								+ Raccordement.NUMERO_COMPOSANT_TENANT + "='"
								+ numeroCo + "'", null, null);
				Intent toInfo = new Intent(PositionnementTaTab.this,
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
	}

	private void displayContentProvider() {

		clause = new String(Cheminement.NUMERO_COMPOSANT_ABOUTISSANT + "='"
				+ numeroCo + "' OR " + Cheminement.NUMERO_COMPOSANT_TENANT
				+ "='" + numeroCo + "'");
		cursor = cr.query(urlChe, colChe1, clause, null, Cheminement._id);
		int numeroSection;

		String zonePose = "";
		if (cursor.moveToFirst()) {
			numeroSection = cursor.getInt(cursor
					.getColumnIndex(Cheminement.NUMERO_SECTION_CHEMINEMENT));
			cursorA = cr.query(urlChe, colChe1,
					Cheminement.NUMERO_SECTION_CHEMINEMENT + "='"
							+ numeroSection + "'", null, Cheminement._id);
			Log.e("Chem", "" + cursorA.getCount());

			if (cursorA.moveToFirst()) {

				HashMap<String, String> element1;
				HashMap<String, String> element2;

				element1 = new HashMap<String, String>();
				element2 = new HashMap<String, String>();
				element1.put(colChe1[0], "" + numeroSection);
				element2.put(colChe1[0], "" + numeroSection);

				do {
					zonePose += cursorA.getString(cursorA
							.getColumnIndex(Cheminement.ZONE_ACTIVITE))
							+ "-"
							+ cursorA.getString(cursorA
									.getColumnIndex(Cheminement.LOCALISATION1))
							+ "-"
							+ cursorA
									.getString(cursorA
											.getColumnIndex(Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT))
							+ ", ";

					if (!(cursorA.getString(cursorA
							.getColumnIndex(Cheminement.TYPE_SUPPORT))
							==null)) {
						if (cursorA
								.getString(
										cursorA.getColumnIndex(Cheminement.TYPE_SUPPORT))
								.contains("rivation")) {

							element1.put(colChe1[1], cursorA.getString(cursorA
									.getColumnIndex(Cheminement.TYPE_SUPPORT)));
							element2.put(colChe1[2], cursorA.getString(cursorA
									.getColumnIndex(Cheminement.TYPE_SUPPORT)));
							element1.put(
									colChe1[2],
									cursorA.getString(cursorA
											.getColumnIndex(Cheminement.ZONE_ACTIVITE))
											+ "-"
											+ cursorA.getString(cursorA
													.getColumnIndex(Cheminement.LOCALISATION1)));

							element2.put(
									colChe1[3],
									cursorA.getString(cursorA
											.getColumnIndex(Cheminement.ZONE_ACTIVITE))
											+ "-"
											+ cursorA.getString(cursorA
													.getColumnIndex(Cheminement.LOCALISATION1)));
						}
					}

				} while (cursorA.moveToNext());
				liste1.add(element1);

				element2.put(colChe1[1], zonePose);
				liste2.add(element2);

			}

		}

		SimpleAdapter sca1 = new SimpleAdapter(this, liste1,
				R.layout.grid_layout_positionnement1, colChe1, layouts1);

		gridView.setAdapter(sca1);

		SimpleAdapter sca2 = new SimpleAdapter(this, liste2,
				R.layout.grid_layout_positionnement2, colChe1, layouts2);
		gridView1.setAdapter(sca2);

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
