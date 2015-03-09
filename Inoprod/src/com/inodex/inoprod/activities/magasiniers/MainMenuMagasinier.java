package com.inodex.inoprod.activities.magasiniers;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

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
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;

import com.inodex.inoprod.R;
import com.inodex.inoprod.activities.Inoprod;
import com.inodex.inoprod.business.KittingProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.Outillage.Outil;
import com.inodex.inoprod.business.TableKittingCable.Kitting;
import com.inodex.inoprod.business.TableSequencement.Operation;

/**
 * Menu principal du profil magasinier. L'ordre du jour y est affich�.
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class MainMenuMagasinier extends Activity {

	/** Bouton qui permet de revenir au menu principal */
	private ImageButton boutonExit = null;

	/** Bouton de validation */
	private ImageButton boutonCheck = null;

	/** Nom de l'op�rateur */
	private String nomPrenomOperateur[] = null;

	/** Colonnes utilis�s pour les requ�tes */
	private String columns[] = { Operation._id, Operation.RANG_1_1,
			Operation.GAMME };
	
	private int layouts[] = { R.id.ordreOperations, R.id.operationsRealiser };

	/** Tableau des op�rations � r�aliser */
	private int opId[] = null;

	/** Indice de l'op�ration courante */
	private int indiceCourant = 0;

	/** Uri de la table de sequencement */
	private Uri url = SequencementProvider.CONTENT_URI;
	
	/** Curseur et Content Resolver � utiliser lors des requ�tes */
	private Cursor cursor;
	private ContentResolver cr;

	/** Clause � utiliser lors des requ�tes */
	private String clause;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu_magasinier);

		// R�cup�ration du nom de l'op�rateur
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
				builder.setMessage("�tes-vous sur de vouloir quitter le profil ?");
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
					Intent toNext = new Intent(MainMenuMagasinier.this,
							ImportCoupeCables.class);
					toNext.putExtra("opId", opId);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);
					finish();
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
		// Cr�ation du SimpleCursorAdapter affili� au GridView
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.grid_layout_menu_magasinier, null, columns, layouts);
		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(sca);
		// Requ�te dans la table sequencement
		clause = new String(Operation.GAMME + "='" + "Kitting" + "'"
				+ " GROUP BY " + Operation.RANG_1_1);
		cursor = cr.query(url, columns, clause, null, Operation._id + " ASC");

		sca.changeCursor(cursor);

		clause = new String(Operation.RANG_1_1 + " LIKE '%" + "D�bit"
				+ "%' AND " + Operation.RANG_1 + "='" + "Kitting c�ble" + "'");
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
