package com.inodex.inoprod.activities.cableur;

import java.util.Date;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TriAboutissantsTa extends Activity {

	/** Elements � r�cuperer de la vue */
	private TextView titre, numeroConnecteur, repereElectrique, nombreGroupe,
			positionChariot;
	private ImageButton boutonCheck, infoProduit, retour;
	private GridView gridView;

	/** Uri � manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;

	/** Tableau des op�rations � r�aliser */
	private int opId[] = null;

	/** Indice de l'op�ration courante */
	private int indiceCourant = 0;

	/** Tableau des infos produit */
	private String labels[];

	/** Heure et dates � ajouter � la table de s�quencment */
	private Date dateRealisation = new Date();
	private Time heureRealisation = new Time();

	/** Nom de l'op�rateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver � utiliser lors des requ�tes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation;;

	/** Colonnes utilis�s pour les requ�tes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION };

	private int layouts[] = new int[] { R.id.groupe, R.id.numeroFilCable,
			R.id.typeCable, R.id.numeroSegregation, R.id.connecteurAboutissant,
			R.id.zoneLocalisation, R.id.numeroRoute, R.id.nombreFilsArrivantTb };

	private String columnsRac[] = new String[] { Raccordement.NUMERO_FIL_CABLE,
			Raccordement.TYPE_FIL_CABLE,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.ZONE_ACTIVITE, Raccordement._id,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tri_aboutissants_ta);
		
		// R�cup�ration des �l�ments
				Intent intent = getIntent();
				indiceCourant = intent.getIntExtra("Indice", 0);
				nomPrenomOperateur = intent.getStringArrayExtra("Noms");
				opId = intent.getIntArrayExtra("opId");
				cr = getContentResolver();
				
				// R�cuperation des �l�ments de la vue
				gridView = (GridView) findViewById(R.id.gridview);
				titre = (TextView) findViewById(R.id.textView1);
				numeroConnecteur = (TextView) findViewById(R.id.textView3);
				positionChariot = (TextView) findViewById(R.id.textView7);
				repereElectrique = (TextView) findViewById(R.id.textView5);
				nombreGroupe = (TextView) findViewById(R.id.textView4);
				retour = (ImageButton) findViewById(R.id.imageButton2);
				boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
				infoProduit = (ImageButton) findViewById(R.id.infoButton1);

				// R�cuperation du num�ro d'op�ration courant
				clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
				cursor = cr.query(urlSeq, columnsSeq, clause, null, Operation._id
						+ " ASC");
				if (cursor.moveToFirst()) {
					numeroOperation = cursor.getString(cursor
							.getColumnIndex(Operation.NUMERO_OPERATION));
				}

				// Affichage du contenu
				displayContentProvider();

				// Etape suivante

				// Info Produit
			}

			private void displayContentProvider() {
				// Cr�ation du SimpleCursorAdapter affili� au GridView
				SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
						R.layout.grid_layout_tri_aboutissants_ta, null,
						null, layouts);

				gridView.setAdapter(sca);
				// MAJ Table de sequencement
				contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0] + " "
						+ nomPrenomOperateur[1]);
				contact.put(Operation.DATE_REALISATION, dateRealisation.toGMTString());
				heureRealisation.setToNow();
				contact.put(Operation.HEURE_REALISATION, heureRealisation.toString());
				cr.update(urlSeq, contact, Operation._id + " = ?",
						new String[] { Integer.toString(opId[indiceCourant]) });
				contact.clear();

			}

			/** Bloquage du bouton retour */
			public void onBackPressed() {

			}
}
