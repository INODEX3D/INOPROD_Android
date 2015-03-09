package com.inodex.inoprod.activities.cableur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.inodex.inoprod.R;
import com.inodex.inoprod.activities.Inoprod;
import com.inodex.inoprod.activities.magasiniers.ImportCoupeCables;
import com.inodex.inoprod.activities.magasiniers.MainMenuMagasinier;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

/**
 * Menu principal du profil opérateur. L'ordre du jour y est affiché.
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class MainMenuCableur extends Activity {

	/** Bouton qui permet de revenir au menu principal */
	private ImageButton boutonExit = null;

	/** Bouton de validation */
	private ImageButton boutonCheck = null;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();

	/** Uri de la table de sequencement */
	private Uri url = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;
	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;

	/** Clause à utiliser lors des requêtes */
	private String clause, firstOperation;

	/** Colonnes utilisés pour les requêtes */
	private String columns[] = { Operation.DESCRIPTION_OPERATION,
			Operation.RANG_1_1, Operation.GAMME, Operation.NOM_OPERATEUR,
			Operation.NUMERO_OPERATION, Operation._id, Operation.REALISABLE,
			Operation.DUREE_THEORIQUE, Operation.DESCRIPTION_OPERATION,
			Operation.DATE_REALISATION };

	private String colRac[] = { Raccordement._id,
			Raccordement.NUMERO_OPERATION, Raccordement.NUMERO_SERIE_OUTIL,
			Raccordement.REFERENCE_OUTIL_ABOUTISSANT,
			Raccordement.REFERENCE_OUTIL_TENANT,
			Raccordement.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT };

	private int layouts[] = { R.id.numeroOperation, R.id.operationsRealiser,
			R.id.referenceOutillage, R.id.numeroSerie };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu_cableur);
		// Récupération du nom de l'opérateur
		Intent i = getIntent();
		nomPrenomOperateur = i.getStringArrayExtra("Noms");

		cr = getContentResolver();

		// Affichage de lordre du jour
		displayContentProvider();

		// Retour menu principal
		boutonExit = (ImageButton) findViewById(R.id.exitButton1);
		boutonExit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MainMenuCableur.this);
				builder.setMessage("Êtes-vous sur de vouloir quitter le profil ?");
				builder.setCancelable(false);
				builder.setPositiveButton("Oui",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								Intent toMain = new Intent(
										MainMenuCableur.this, Inoprod.class);
								startActivity(toMain);
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

		// Etape suivante
		boutonCheck = (ImageButton) findViewById(R.id.imageButton1);
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (cursor.moveToFirst()) {
					firstOperation = cursor.getString(cursor
							.getColumnIndex(Operation.DESCRIPTION_OPERATION));
					Intent toNext = null;
					if (firstOperation.startsWith("Préparation")) {
						toNext = new Intent(MainMenuCableur.this,
								PreparationTa.class);
					} else if (firstOperation.startsWith("Reprise")) {
						toNext = new Intent(MainMenuCableur.this,
								RepriseBlindageTa.class);
					} else if (firstOperation
							.startsWith("Denudage Sertissage Enfichage")) {
						toNext = new Intent(MainMenuCableur.this,
								DenudageSertissageEnfichageTa.class);
					} else if (firstOperation
							.startsWith("Denudage Sertissage de")) {
						toNext = new Intent(MainMenuCableur.this,
								DenudageSertissageContactTa.class);
					} else if (firstOperation.startsWith("Enfichage")) {
						toNext = new Intent(MainMenuCableur.this,
								EnfichagesTa.class);
					} else if (firstOperation.startsWith("Finalisation")) {
						toNext = new Intent(MainMenuCableur.this,
								FinalisationTa.class);
					} else if (firstOperation.startsWith("Tri")) {
						toNext = new Intent(MainMenuCableur.this,
								TriAboutissantsTa.class);
					} else if (firstOperation.startsWith("Positionnement")) {
						toNext = new Intent(MainMenuCableur.this,
								PositionnementTaTab.class);
					} else if (firstOperation.startsWith("Cheminement")) {
						toNext = new Intent(MainMenuCableur.this,
								CheminementTa.class);
					} else if (firstOperation.startsWith("Frettage")) {
						toNext = new Intent(MainMenuCableur.this,
								Frettage.class);
					} else if (firstOperation.startsWith("Mise")) {
						toNext = new Intent(MainMenuCableur.this,
								MiseLongueurTb.class);
					} else if (firstOperation.startsWith("Denudage Sertissage Coss")) {
						toNext = new Intent(MainMenuCableur.this,
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
			}
		});
	}

	/**
	 * Genère l'affichage de l'ordre du jour en utilisant un SimpleCursorAdapter
	 * Le layout GridView est récupéré puis utiliser pour afficher chacun des
	 * éléments
	 */
	private void displayContentProvider() {

		Log.e("Date", Integer.toString(new Date().getDay()));
		clause = new String("(" + Operation.NOM_OPERATEUR + " IS NULL OR "
				+ Operation.NOM_OPERATEUR + " LIKE '' OR "
				+ Operation.DATE_REALISATION + " LIKE '%"
				+ (new Date()).toGMTString().substring(0, 10) + "%') AND "
				+ Operation.REALISABLE + "='" + 1 + "' AND ("
				+ Operation.RANG_1_1 + " LIKE '%P06%' OR " + Operation.RANG_1_1
				+ " LIKE '%J08%' OR " + Operation.RANG_1_1
				+ " LIKE '%P14%' OR " + Operation.RANG_1_1
				+ " LIKE '%P09%'  OR " + Operation.RANG_1_1
				+ " LIKE '%J12%') AND " + Operation.GAMME
				+ "!='Contrôle jalons' AND " + Operation.GAMME
				+ "!='Contrôle final'  ");
		//clause = Operation.DESCRIPTION_OPERATION + " LIKE '%Cosse%'" ; 
		cursor = cr.query(url, columns, clause + " GROUP BY "
				+ Operation.DESCRIPTION_OPERATION, null, 
				Operation._id + " ASC LIMIT 30");

		if (cursor.moveToFirst()) {
			int indice = 1;
			HashMap<String, String> element;
			do {

				element = new HashMap<String, String>();
				element.put(columns[0], "" + indice++);

				if ((cursor.getString(cursor
						.getColumnIndex(Operation.NOM_OPERATEUR)) != null)
						&& (!(cursor.getString(cursor
								.getColumnIndex(Operation.NOM_OPERATEUR)))
								.equals(""))) {
					element.put(
							columns[1],
							"***"
									+ cursor.getString(cursor
											.getColumnIndex(Operation.DESCRIPTION_OPERATION))
									+ "***");

				} else {
					element.put(columns[1], cursor.getString(cursor
							.getColumnIndex(Operation.DESCRIPTION_OPERATION)));
				}
				String numeroOp = cursor.getString(cursor
						.getColumnIndex(Operation.NUMERO_OPERATION));
				cursorA = cr.query(urlRac, colRac,
						Raccordement.NUMERO_OPERATION + "='" + numeroOp + "'",
						null, Operation._id);
				if (cursorA.moveToFirst()) {
					element.put(
							columns[2],
							cursorA.getString(cursorA
									.getColumnIndex(Raccordement.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT)));
					element.put(columns[3], cursorA.getString(cursorA
							.getColumnIndex(Raccordement.NUMERO_SERIE_OUTIL)));

				}
				liste.add(element);
			} while (cursor.moveToNext());
		}

		// Création du SimpleCursorAdapter affilié au GridView
		SimpleAdapter sca = new SimpleAdapter(this, liste,
				R.layout.grid_layout_menu_cableur, columns, layouts);
		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(sca);

		// Génération de l'ordre du jour
		clause = new String("(" + Operation.NOM_OPERATEUR + " IS NULL OR "
				+ Operation.NOM_OPERATEUR + " LIKE '' ) AND "
				+ Operation.REALISABLE + "='" + 1 + "' AND ("
				+ Operation.RANG_1_1 + " LIKE '%P06%' OR " + Operation.RANG_1_1
				+ " LIKE '%J08%' OR " + Operation.RANG_1_1
				+ " LIKE '%P14%' OR " + Operation.RANG_1_1
				+ " LIKE '%P09%'  OR " + Operation.RANG_1_1
				+ " LIKE '%J12%') AND " + Operation.GAMME
				+ "!='Contrôle jalons' AND " + Operation.GAMME
				+ "!='Contrôle final'  ");
		//clause = Operation.DESCRIPTION_OPERATION + " LIKE '%Cosse%'" ; 
		cursor = cr.query(url, columns, clause, null, Operation._id + " ASC"
				+ " LIMIT 70");

		// Rempliassage du tableau pour chaque numero de cable
		if (cursor.moveToFirst()) {
			opId = new int[cursor.getCount()];
			Log.e("Position curseur", "" + cursor.getPosition());
			do {
				opId[cursor.getPosition()] = cursor.getInt(cursor
						.getColumnIndex(Operation._id));

			} while (cursor.moveToNext());

		}

	}

}
