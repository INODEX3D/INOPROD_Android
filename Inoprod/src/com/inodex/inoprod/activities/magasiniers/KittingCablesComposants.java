package com.inodex.inoprod.activities.magasiniers;

import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.inodex.inoprod.R;
import com.inodex.inoprod.business.BOMProvider;
import com.inodex.inoprod.business.CheminementProvider;
import com.inodex.inoprod.business.KittingProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableBOM.BOM;
import com.inodex.inoprod.business.TableCheminement.Cheminement;
import com.inodex.inoprod.business.TableKittingCable.Kitting;
import com.inodex.inoprod.business.TableSequencement.Operation;

/**
 * Ecran affichant un ensemble de t�tes � regrouper
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class KittingCablesComposants extends Activity {

	/** Uri � manipuler */
	private Uri urlBOM = BOMProvider.CONTENT_URI;
	private Uri urlKitting = KittingProvider.CONTENT_URI;
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlChem = CheminementProvider.CONTENT_URI;

	/** Curseur et Content Resolver � utiliser lors des requ�tes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;
	private GridView gridView;

	private boolean prodAchevee;
	private String clause, numeroOperation, numeroCom, descriptionOperation;

	/** Tableau des op�rations � r�aliser */
	private int opId[] = null;

	/** Indice de l'op�ration courante */
	private int indiceCourant = 0;
	/** Numero de cheminement courant */
	private int numeroCh;
	/** Heure et dates � ajouter � la table de s�quencment */
	private Date dateRealisation = new Date();
	private Time heureRealisation = new Time();

	/** Nom de l'op�rateur */
	private String nomPrenomOperateur[] = null;

	/** Elements � r�cuperer de la vue */
	private TextView numeroComposant, numeroChariot, repereElectrique,
			numeroCheminement, ordreRealisation;
	private ImageButton boutonCheck;

	/** Colonnes utilis�s pour les requ�tes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION };

	private String columnsBOM[] = new String[] { BOM.REPERE_ELECTRIQUE_TENANT,
			BOM.NUMERO_COMPOSANT, BOM.NUMERO_POSITION_CHARIOT,
			BOM.ORDRE_REALISATION, BOM.QUANTITE, BOM.UNITE,
			BOM.NUMERO_LOT_SCANNE, BOM.NUMERO_DEBIT, BOM.DESIGNATION_COMPOSANT,
			BOM.FOURNISSEUR_FABRICANT, BOM.REFERENCE_IMPOSEE,
			BOM.REFERENCE_INTERNE, BOM.REFERENCE_FABRICANT2, BOM._id };
	private int[] layouts = new int[] { R.id.numeroCable, R.id.typeCable,
			R.id.designation, R.id.referenceFabricant, R.id.referenceInterne,
			R.id.quantite, R.id.uniteMesure };

	private String columnsKitting[] = new String[] { Kitting.NUMERO_FIL_CABLE,
			Kitting.TYPE_FIL_CABLE, Kitting.REFERENCE_FABRICANT2,
			Kitting.REFERENCE_INTERNE, Kitting.NUMERO_POSITION_CHARIOT,
			Kitting.REPERE_ELECTRIQUE, Kitting.NUMERO_COMPOSANT,
			Kitting.ORDRE_REALISATION, Kitting.LONGUEUR_FIL_CABLE,
			Kitting.UNITE, Kitting.NUMERO_CHEMINEMENT, Kitting._id,
			Kitting.NUMERO_OPERATION };

	private String columnsChem[] = new String[] { Cheminement._id,
			Cheminement.NUMERO_SECTION_CHEMINEMENT,
			Cheminement.NUMERO_COMPOSANT, Cheminement.REPERE_ELECTRIQUE,
			Cheminement.ORDRE_REALISATION };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kitting_cables_composants);

		// R�cup�ration des �l�ments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();

		// R�cuperation des �l�ments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		numeroComposant = (TextView) findViewById(R.id.textView3);
		numeroChariot = (TextView) findViewById(R.id.textView4);
		repereElectrique = (TextView) findViewById(R.id.textView5);
		numeroCheminement = (TextView) findViewById(R.id.textView6);
		ordreRealisation = (TextView) findViewById(R.id.textView7);

		// R�cuperation du num�ro d'op�ration courant
		clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
		cursor = cr.query(urlSeq, columnsSeq, clause, null, null);
		if (cursor.moveToFirst()) {
			numeroOperation = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
			descriptionOperation = cursor.getString(cursor
					.getColumnIndex(Operation.DESCRIPTION_OPERATION));
		} else {
			Log.e("Regroupement", "Probl�me s�quencement");
		}

		// R�cup�ration du num�ro de cheminement
		numeroCom = descriptionOperation.substring(37, 40);
		clause = new String(Cheminement.NUMERO_COMPOSANT + "='" + numeroCom
				+ "'");
		cursorA = cr.query(urlChem, columnsChem, clause, null, null);
		if (cursorA.moveToFirst()) {
			numeroCh = cursorA.getInt(cursorA
					.getColumnIndex(Cheminement.NUMERO_SECTION_CHEMINEMENT));

			// Affichage des �l�ments du regroupement en cours
			try {
				numeroChariot.append(cursorA.getString(cursorA
						.getColumnIndex(Kitting.NUMERO_POSITION_CHARIOT)));
			} catch (NullPointerException e) {
			}
			try {
				numeroComposant.append(cursorA.getString(cursorA
						.getColumnIndex(Cheminement.NUMERO_COMPOSANT)));
			} catch (NullPointerException e) {
			}
			try {
				repereElectrique.append(cursorA.getString(cursorA
						.getColumnIndex(Cheminement.REPERE_ELECTRIQUE)));
			} catch (NullPointerException e) {
			}
			try {
				ordreRealisation.append(cursorA.getString(cursorA
						.getColumnIndex(Cheminement.ORDRE_REALISATION)));
			} catch (NullPointerException e) {
			}
			try {
				numeroCheminement.append(Integer.toString(numeroCh));
			} catch (NullPointerException e) {
			}
		} else {
			Log.e("Regroupement", numeroCom);
			Log.e("Regroupement", descriptionOperation);
			Log.e("Regroupement", numeroOperation);
		}

		// Affichage des cables � regouper
		displayContentProvider();

		// Etape suivante
		boutonCheck = (ImageButton) findViewById(R.id.exitButton1);
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// MAJ Table de sequencement
				contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0]
						+ " " + nomPrenomOperateur[1]);
				contact.put(Operation.DATE_REALISATION,
						dateRealisation.toGMTString());
				heureRealisation.setToNow();
				contact.put(Operation.HEURE_REALISATION,
						heureRealisation.toString());
				cr.update(urlSeq, contact, Operation._id + " = ?",
						new String[] { Integer.toString(opId[indiceCourant]) });
				contact.clear();

				indiceCourant += 2;
				try {
					int test = opId[indiceCourant];// Si OK il reste encore
					// des cables � regrouper
					Intent toNext = new Intent(KittingCablesComposants.this,
							KittingCablesComposants.class);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("opId", opId);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);

				} catch (ArrayIndexOutOfBoundsException e) {

					// Il ne reste plus de cables � regrouper
					// On retourne � l'�cran d'accueil

					Intent toMain = new Intent(KittingCablesComposants.this,
							MainMenuMagasinier.class);
					toMain.putExtra("Noms", nomPrenomOperateur);
					startActivity(toMain);

				}
			}

		});

	}

	/**
	 * Gen�re l'affichage en utilisant un SimpleCursorAdapter Le layout GridView
	 * est r�cup�r� puis utiliser pour afficher chacun des �l�ments
	 */
	private void displayContentProvider() {
		// Cr�ation du SimpleCursorAdapter affili� au GridView
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.grid_layout_kitting_cables_composants, null,
				columnsKitting, layouts);

		gridView.setAdapter(sca);
		// Requ�te dans la base Cheminement
		clause = Kitting.NUMERO_CHEMINEMENT + " ='" + numeroCh + "'";
		cursor = cr.query(urlKitting, columnsKitting, clause, null, null);
		sca.changeCursor(cursor);
	}
	
	/**Bloquage du bouton retour */
	public void onBackPressed() {

	}
}
