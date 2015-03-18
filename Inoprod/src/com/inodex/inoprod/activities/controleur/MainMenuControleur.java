package com.inodex.inoprod.activities.controleur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.activities.Inoprod;
import com.inodex.inoprod.activities.cableur.CheminementTa;
import com.inodex.inoprod.activities.cableur.DenudageSertissageContactTa;
import com.inodex.inoprod.activities.cableur.DenudageSertissageEnfichageTa;
import com.inodex.inoprod.activities.cableur.EnfichagesTa;
import com.inodex.inoprod.activities.cableur.FinalisationTa;
import com.inodex.inoprod.activities.cableur.MainMenuCableur;
import com.inodex.inoprod.activities.cableur.PositionnementTaTab;
import com.inodex.inoprod.activities.cableur.PreparationTa;
import com.inodex.inoprod.activities.cableur.RepriseBlindageTa;
import com.inodex.inoprod.activities.cableur.TriAboutissantsTa;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

public class MainMenuControleur extends Activity {

	/** Bouton qui permet de revenir au menu principal */
	private ImageButton boutonExit = null;

	/** Bouton de validation */
	private ImageButton boutonCheck, infoProduit;

	/** Tableau des op�rations � r�aliser */
	private int opId[] = null;

	/** Indice de l'op�ration courante */
	private int indiceCourant = 0;

	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();

	/** Nom de l'op�rateur */
	private String nomPrenomOperateur[] = null;

	/** Uri de la table de sequencement */
	private Uri url = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;
	/** Curseur et Content Resolver � utiliser lors des requ�tes */
	private Cursor cursor, cursorA, cursorB;
	private ContentResolver cr;

	/** Clause � utiliser lors des requ�tes */
	private String clause, firstOperation;

	/** Colonnes utilis�s pour les requ�tes */
	private String columns[] = { Operation.DESCRIPTION_OPERATION,
			Operation.RANG_1_1, Operation.GAMME, Operation.NOM_OPERATEUR,
			Operation.NUMERO_OPERATION, Operation._id, Operation.REALISABLE,
			Operation.DATE_REALISATION };

	private String colRac[] = { Raccordement._id,
			Raccordement.NUMERO_OPERATION, Raccordement.NUMERO_SERIE_OUTIL,
			Raccordement.REFERENCE_OUTIL_ABOUTISSANT,
			Raccordement.REFERENCE_OUTIL_TENANT,
			Raccordement.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT };
	private int layouts[] = { R.id.numeroOperation, R.id.operationsRealiser,
			R.id.referenceOutillage, R.id.numeroSerie };

	private String colInfo[] = new String[] { Raccordement._id,
			Raccordement.DESIGNATION, Raccordement.NUMERO_REVISION_HARNAIS,
			Raccordement.STANDARD, Raccordement.NUMERO_HARNAIS_FAISCEAUX,
			Raccordement.REFERENCE_FICHIER_SOURCE };
	private Cursor cursorInfo;

	/** Tableau des infos produit */
	private String labels[];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu_controleur);
		// R�cup�ration du nom de l'op�rateur
		Intent i = getIntent();
		nomPrenomOperateur = i.getStringArrayExtra("Noms");

		cr = getContentResolver();

		// Affichage de lordre du jour
		displayContentProvider();

		// Verification du controle final harnais
		cursorA = cr.query(url, columns, Operation.DESCRIPTION_OPERATION
				+ " LIKE 'Contr�le%'", null, Operation._id);
		cursorB = cr.query(url, columns, Operation.DESCRIPTION_OPERATION
				+ " LIKE 'Contr�le%' AND " + Operation.NOM_OPERATEUR
				+ "!='null' ", null, Operation._id);
		if (cursorB.getCount() == cursorA.getCount() - 1) {
			cursorB = cr.query(url, columns, Operation.DESCRIPTION_OPERATION
					+ " LIKE 'Contr�le final harnais'  ", null, Operation._id);
			if (cursorB.moveToFirst()) {
				ContentValues contact = new ContentValues();
				contact.put(Operation.REALISABLE, 1);
				cr.update(url, contact, Operation.DESCRIPTION_OPERATION
						+ " LIKE 'Contr�le final harnais'  ", null);
			}
		}

		// Retour menu principal
		boutonExit = (ImageButton) findViewById(R.id.exitButton1);
		boutonExit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MainMenuControleur.this);
				builder.setMessage("�tes-vous sur de vouloir quitter le profil ?");
				builder.setCancelable(false);
				builder.setPositiveButton("Oui",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								Intent toMain = new Intent(
										MainMenuControleur.this, Inoprod.class);
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

		// Info Produit
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);
		infoProduit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cursorInfo = cr.query(urlRac, colInfo, null, null, null);
				Intent toInfo = new Intent(MainMenuControleur.this,
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

		// Etape suivante
		boutonCheck = (ImageButton) findViewById(R.id.imageButton1);
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (cursor.moveToFirst()) {
					firstOperation = cursor.getString(cursor
							.getColumnIndex(Operation.DESCRIPTION_OPERATION));
					Intent toNext = null;
					if (firstOperation.startsWith("Contr�le final t�te")) {
						toNext = new Intent(MainMenuControleur.this,
								ControleFinalisationTa.class);
					} else if (firstOperation.startsWith("Contr�le r�tention")) {
						toNext = new Intent(MainMenuControleur.this,
								ControleRetentionTa.class);
					} else if (firstOperation.startsWith("Contr�le sertissage")) {
						toNext = new Intent(MainMenuControleur.this,
								ControleSertissageTa.class);
					} else if (firstOperation
							.startsWith("Contr�le final harnais")) {
						toNext = new Intent(MainMenuControleur.this,
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
			}
		});
	}

	/**
	 * Gen�re l'affichage de l'ordre du jour en utilisant un SimpleCursorAdapter
	 * Le layout GridView est r�cup�r� puis utiliser pour afficher chacun des
	 * �l�ments
	 */
	private void displayContentProvider() {

		// Requ�te dans la table sequencement
		clause = new String("(" + Operation.REALISABLE + "='" + 1 + "' OR "
				+ Operation.DATE_REALISATION + " LIKE '%"
				+ (new Date()).toGMTString().substring(0, 10) + "%') AND "
				+ Operation.GAMME + " LIKE 'Cont%' AND ("
				+ Operation.NOM_OPERATEUR + " IS NULL OR "
				+ Operation.DATE_REALISATION + " LIKE '%"
				+ (new Date()).toGMTString().substring(0, 10) + "%') ");

		cursor = cr.query(url, columns, clause + " GROUP BY "
				+ Operation.DESCRIPTION_OPERATION, null, Operation._id + " ASC"
		/* + " LIMIT 30" */);

		if (cursor.moveToFirst()) {
			int indice = 1;
			HashMap<String, String> element;
			do {

				element = new HashMap<String, String>();
				element.put(columns[0], "" + indice++);
				if (cursor.getString(cursor
						.getColumnIndex(Operation.DATE_REALISATION)) != null) {
					element.put(
							columns[1],
							""
									+ cursor.getString(cursor
											.getColumnIndex(Operation.DESCRIPTION_OPERATION))
									+ "*** Achev�e");

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

		// Cr�ation du SimpleCursorAdapter affili� au GridView
		SimpleAdapter sca = new SimpleAdapter(this, liste,
				R.layout.grid_layout_menu_cableur, columns, layouts);
		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(sca);

		clause = new String(Operation.REALISABLE + "='" + 1 + "'  AND "
				+ Operation.GAMME + " LIKE 'Cont%' AND "
				+ Operation.NOM_OPERATEUR + " IS NULL ");

		cursor = cr.query(url, columns, clause, null, Operation._id + " ASC");

		// Rempliassage du tableau pour chaque numero de cable
		if (cursor.moveToFirst()) {
			opId = new int[cursor.getCount()];
			do {
				opId[cursor.getPosition()] = cursor.getInt(cursor
						.getColumnIndex(Operation._id));

			} while (cursor.moveToNext());

		}

	}

}