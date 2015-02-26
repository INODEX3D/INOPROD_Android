package com.inodex.inoprod.activities.controleur;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
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
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableSequencement.Operation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
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
import android.widget.SimpleCursorAdapter;

public class MainMenuControleur extends Activity {

	/** Bouton qui permet de revenir au menu principal */
	private ImageButton boutonExit = null;

	/** Bouton de validation */
	private ImageButton boutonCheck = null;

	/** Tableau des op�rations � r�aliser */
	private int opId[] = null;

	/** Indice de l'op�ration courante */
	private int indiceCourant = 0;

	/** Nom de l'op�rateur */
	private String nomPrenomOperateur[] = null;

	/** Uri de la table de sequencement */
	private Uri url = SequencementProvider.CONTENT_URI;
	/** Curseur et Content Resolver � utiliser lors des requ�tes */
	private Cursor cursor;
	private ContentResolver cr;

	/** Clause � utiliser lors des requ�tes */
	private String clause, firstOperation;

	/** Colonnes utilis�s pour les requ�tes */
	private String columns[] = { Operation.DESCRIPTION_OPERATION,
			Operation.RANG_1_1, Operation.GAMME, Operation.NOM_OPERATEUR,
			Operation.NUMERO_OPERATION, Operation._id, Operation.REALISABLE };
	private int layouts[] = { R.id.operationsRealiser };

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

		// Etape suivante
		boutonCheck = (ImageButton) findViewById(R.id.imageButton1);
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (cursor.moveToFirst()) {
					firstOperation = cursor.getString(cursor
							.getColumnIndex(Operation.DESCRIPTION_OPERATION));
					Intent toNext = null;
					if (firstOperation.startsWith("Contr�le final")) {
						toNext = new Intent(MainMenuControleur.this,
								ControleFinalisationTa.class);
					} else if (firstOperation.startsWith("Contr�le r�tention")) {
						toNext = new Intent(MainMenuControleur.this,
								ControleRetentionTa.class);
					} else if (firstOperation.startsWith("Contr�le sertissage")) {
						toNext = new Intent(MainMenuControleur.this,
								ControleSertissageTa.class);
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
		// Cr�ation du SimpleCursorAdapter affili� au GridView
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.grid_layout_menu_cableur, null, columns, layouts);
		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(sca);
		// Requ�te dans la table sequencement
		clause = new String(Operation.REALISABLE + "='" + 1 + "' AND "
				+ Operation.GAMME + " LIKE 'Cont%' AND "
				+ Operation.NOM_OPERATEUR + " IS NULL AND " + "("
				+ Operation.RANG_1_1 + " LIKE '%P06%' OR " + Operation.RANG_1_1
				+ " LIKE '%J08%' OR " + Operation.RANG_1_1
				+ " LIKE '%P14%' OR " + Operation.RANG_1_1
				+ " LIKE '%P09%'  OR " + Operation.RANG_1_1
				+ " LIKE '%J12%')");
		cursor = cr.query(url, columns, clause + " GROUP BY "
				+ Operation.DESCRIPTION_OPERATION, null, Operation._id + " ASC"
		/* + " LIMIT 30" */);

		sca.changeCursor(cursor);

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