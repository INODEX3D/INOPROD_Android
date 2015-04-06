package com.inodex.inoprod.activities.magasiniers;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

import com.inodex.inoprod.R;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.activities.Inoprod;
import com.inodex.inoprod.activities.controleur.MainMenuControleur;
import com.inodex.inoprod.business.KittingProvider;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.Outillage.Outil;
import com.inodex.inoprod.business.TableKittingCable.Kitting;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

/**
 * Menu principal du profil magasinier. L'ordre du jour y est affiché.
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class MainMenuMagasinier extends Activity {

	/** Bouton qui permet de revenir au menu principal */
	private ImageButton boutonExit = null;

	/** Bouton de validation */
	private ImageButton boutonCheck, infoProduit;

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Colonnes utilisés pour les requêtes */
	private String columns[] = { Operation._id, Operation.RANG_1_1,
			Operation.GAMME, Operation.DESCRIPTION_OPERATION,
			Operation.DATE_REALISATION, Operation.NOM_OPERATEUR };

	private int layouts[] = { R.id.ordreOperations, R.id.operationsRealiser };

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;

	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();

	/** Uri de la table de sequencement */
	private Uri url = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;
	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor;
	private ContentResolver cr;

	/** Clause à utiliser lors des requêtes */
	private String clause;

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
		setContentView(R.layout.activity_main_menu_magasinier);

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
						MainMenuMagasinier.this);
				builder.setMessage("Êtes-vous sur de vouloir quitter le profil ?");
				builder.setCancelable(false);
				builder.setPositiveButton("Oui",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								Intent toMain = new Intent(
										MainMenuMagasinier.this, Inoprod.class);
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
					Intent toNext = null;
					String firstOperation = cursor.getString(cursor
							.getColumnIndex(Operation.DESCRIPTION_OPERATION));
					if (firstOperation.startsWith("Débit du fil")) {
						toNext = new Intent(MainMenuMagasinier.this,
								ImportCoupeCables.class);
					} else if (firstOperation.startsWith("Regroupement des")) {
						toNext = new Intent(MainMenuMagasinier.this,
								RegroupementCables.class);
					} else if (firstOperation.startsWith("Débit pour")) {
						toNext = new Intent(MainMenuMagasinier.this,
								SaisieTracabiliteComposant.class);
					} else {
						toNext = new Intent(MainMenuMagasinier.this,
								KittingCablesComposants.class);
					}
					toNext.putExtra("opId", opId);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);
					finish();
				}
			}
		});

		// Info Produit
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);
		infoProduit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cursorInfo = cr.query(urlRac, colInfo, null, null, null);
				Intent toInfo = new Intent(MainMenuMagasinier.this,
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

	/**
	 * Genère l'affichage de l'ordre du jour en utilisant un SimpleCursorAdapter
	 * Le layout GridView est récupéré puis utiliser pour afficher chacun des
	 * éléments
	 */
	private void displayContentProvider() {
		// Création du SimpleCursorAdapter affilié au GridView
		// Requête dans la table sequencement
		clause = new String(Operation.GAMME + "='" + "Kitting" + "'"
				+ " GROUP BY " + Operation.RANG_1_1);
		cursor = cr.query(url, columns, clause, null, Operation._id + " ASC");
		if (cursor.moveToFirst()) {
			int indice = 1;
			HashMap<String, String> element;
			do {

				element = new HashMap<String, String>();
				element.put(columns[0], "" + indice++);

				if ((cursor.getString(cursor
						.getColumnIndex(Operation.DATE_REALISATION)) != null)
						&& (!(cursor.getString(cursor
								.getColumnIndex(Operation.NOM_OPERATEUR)))
								.equals(""))) {
					element.put(
							columns[1],
							""
									+ cursor.getString(cursor
											.getColumnIndex(Operation.RANG_1_1))
									+ "*** Achevée");

				} else {
					element.put(columns[1], cursor.getString(cursor
							.getColumnIndex(Operation.RANG_1_1)));
				}

				liste.add(element);
			} while (cursor.moveToNext());
		}

		SimpleAdapter sca = new SimpleAdapter(this, liste,
				R.layout.grid_layout_menu_magasinier, columns, layouts);
		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(sca);

		clause = new String(Operation.GAMME + "='" + "Kitting" + "' AND "
				+ Operation.DATE_REALISATION + " IS NULL");
		//clause = Operation.DESCRIPTION_OPERATION + " LIKE 'Regrou%'";
		cursor = cr.query(url, columns, clause, null, Operation._id + " ASC");
		// Rempliassage du tableau pour chaque numero de cable
		if (cursor.moveToFirst()) {
			opId = new int[cursor.getCount()];
			do {
				opId[cursor.getPosition()] = cursor.getInt(cursor
						.getColumnIndex(Operation._id));
				Log.e("N°", cursor.getInt(cursor.getColumnIndex(Operation._id))
						+ "");

			} while (cursor.moveToNext());
		}

	}

}
