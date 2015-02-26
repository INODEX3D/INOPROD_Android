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
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.business.CheminementProvider;
import com.inodex.inoprod.business.KittingProvider;
import com.inodex.inoprod.business.ProductionProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.Production.Fil;
import com.inodex.inoprod.business.TableCheminement.Cheminement;
import com.inodex.inoprod.business.TableKittingCable.Kitting;
import com.inodex.inoprod.business.TableSequencement.Operation;

/**
 * Ecran affichant un ensemble de cables à regrouper
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class RegroupementCables extends Activity {

	/** Uri à manipuler */
	private Uri urlKitting = KittingProvider.CONTENT_URI;
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlChem = CheminementProvider.CONTENT_URI;
	private Uri urlProd = ProductionProvider.CONTENT_URI;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;
	
	/** Tableau des infos produit */
	private String labels[];

	/** Indice de l'opération courante */
	private int indiceCourant = 0;

	/** Numero de cheminement courant */
	private int numeroCh;
	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateRealisation = new Date();
	private Time heureRealisation = new Time();

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA, cursorB;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCom, descriptionOperation;

	/** Elements à récuperer de la vue */
	private GridView gridView;
	private ImageButton boutonCheck, infoProduit;
	private TextView numeroComposant, numeroChariot, repereElectrique,
			numeroCheminement, ordreRealisation;

	/** Colonnes utilisés pour les requêtes */
	private String columns[] = new String[] { Kitting.NUMERO_FIL_CABLE,
			Kitting.TYPE_FIL_CABLE, Kitting.REFERENCE_FABRICANT1,
			Kitting.REFERENCE_INTERNE, Kitting._id };
	private int[] layouts = new int[] { R.id.numeroFil, R.id.typeCable,
			R.id.referenceFabricant, R.id.referenceInterne };
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION };

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
	
	private String columnsProd[] = new String[] { Fil._id,
			Fil.DESIGNATION_PRODUIT, Fil.NUMERO_REVISION_HARNAIS, Fil.STANDARD,
			Fil.NUMERO_HARNAIS_FAISCEAUX, Fil.REFERENCE_FICHIER_SOURCE };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regroupement_cables);

		// Récupération des éléments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();

		// Récuperation des éléments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		numeroComposant = (TextView) findViewById(R.id.textView3);
		numeroChariot = (TextView) findViewById(R.id.textView4);
		repereElectrique = (TextView) findViewById(R.id.textView5);
		numeroCheminement = (TextView) findViewById(R.id.textView6);
		ordreRealisation = (TextView) findViewById(R.id.textView7);

		// Récuperation du numéro d'opération courant
		clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
		cursor = cr.query(urlSeq, columnsSeq, clause, null, null);
		if (cursor.moveToFirst()) {
			numeroOperation = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
			descriptionOperation = cursor.getString(cursor
					.getColumnIndex(Operation.DESCRIPTION_OPERATION));
		} else {
			Log.e("Regroupement", "Problème séquencement");
		}

		// Récupération du numéro de cheminement
		numeroCom = descriptionOperation.substring(42, 45);
		clause = new String(Cheminement.NUMERO_COMPOSANT + "='" + numeroCom
				+ "'");
		cursorA = cr.query(urlChem, columnsChem, clause, null, null);
		if (cursorA.moveToFirst()) {
			numeroCh = cursorA.getInt(cursorA
					.getColumnIndex(Cheminement.NUMERO_SECTION_CHEMINEMENT));
			cursorB = cr.query(urlProd, columnsProd,Fil.NUMERO_FIL_CABLE + " = " +Kitting.NUMERO_FIL_CABLE , null,null );

			// Affichage des éléments du regroupement en cours
		/*	try {
			 numeroChariot.append(cursorA.getString(cursorA.getColumnIndex(Cheminement.NUMERO_POSITION_CHARIOT)));
			} catch (NullPointerException e) {
			} */
			try {
				numeroComposant.append(": "+cursorA.getString(cursorA
						.getColumnIndex(Cheminement.NUMERO_COMPOSANT)));
			} catch (NullPointerException e) {
			}
			try {
				repereElectrique.append(": "+cursorA.getString(cursorA
						.getColumnIndex(Cheminement.REPERE_ELECTRIQUE)));
			} catch (NullPointerException e) {
			}
			try {
				ordreRealisation.append(": "+cursorA.getString(cursorA
						.getColumnIndex(Cheminement.ORDRE_REALISATION)));
			} catch (NullPointerException e) {
			}
			try {
				numeroCheminement.append(": "+Integer.toString(numeroCh));
			} catch (NullPointerException e) {
			}
		} else {
			Log.e("Regroupement", numeroCom);
			Log.e("Regroupement", descriptionOperation);
			Log.e("Regroupement", numeroOperation);
		}

		// Affichage des cables à regouper
		displayContentProvider();

		// Etape suivante
		boutonCheck = (ImageButton) findViewById(R.id.exitButton1);
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// MAJ Table de sequencement
				contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0] + " "
						+ nomPrenomOperateur[1]);
				contact.put(Operation.DATE_REALISATION, dateRealisation.toGMTString());
				heureRealisation.setToNow();
				contact.put(Operation.HEURE_REALISATION, heureRealisation.toString());
				cr.update(urlSeq, contact, Operation._id + " = ?",
						new String[] { Integer.toString(opId[indiceCourant]) });
				contact.clear();

				indiceCourant++;
				try {
					int test = opId[indiceCourant];// Si OK il reste encore
					// des cables à regrouper
					Intent toNext = new Intent(RegroupementCables.this,
							RegroupementCables.class);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("opId", opId);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);
					finish();

				} catch (ArrayIndexOutOfBoundsException e) {
					// Il ne reste plus de cables à regrouper
					// On passe donc au kitting têtes
					String s = "Débit";
					clause = new String(Operation.RANG_1 + "='"
							+ "Kitting têtes" + "' AND " + Operation.RANG_1_1
							+ " LIKE '%Débit%' ");
					cursor = cr.query(urlSeq, columnsSeq, clause, null,
							Operation._id + " ASC");
					// Rempliassage du tableau pour chaque rang
					if (cursor.moveToFirst()) {
						opId = new int[cursor.getCount()];
						do {
							opId[cursor.getPosition()] = cursor.getInt(cursor
									.getColumnIndex(Operation._id));

						} while (cursor.moveToNext());
					} else {
						Log.e("Regroupement", "Opération non trouvée");
					}

					Intent toNext = new Intent(RegroupementCables.this,
							SaisieTracabiliteComposant.class);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("opId", opId);
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
				Intent toInfo = new Intent(RegroupementCables.this,
						InfoProduit.class);
				labels= new String[7];
				
				if (cursorB.moveToFirst()) {
				labels[0] = cursorB.getString(cursorB.getColumnIndex(Fil.DESIGNATION_PRODUIT));
				labels[1] = cursorB.getString(cursorB.getColumnIndex(Fil.NUMERO_HARNAIS_FAISCEAUX));
				labels[2] = cursorB.getString(cursorB.getColumnIndex(Fil.STANDARD));
				labels[3] = "";
				labels[4] = "";
				labels[5] = cursorB.getString(cursorB.getColumnIndex(Fil.NUMERO_REVISION_HARNAIS));
				labels[6] = cursorB.getString(cursorB.getColumnIndex(Fil.REFERENCE_FICHIER_SOURCE));
				toInfo.putExtra("Labels", labels);
				}
				
				
				
				startActivity(toInfo);
				
			}
		});

	}

	/**
	 * Genère l'affichage en utilisant un SimpleCursorAdapter Le layout GridView
	 * est récupéré puis utiliser pour afficher chacun des éléments
	 */
	private void displayContentProvider() {
		// Création du SimpleCursorAdapter affilié au GridView
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.grid_layout_regroupement_cables, null, columns,
				layouts);
		gridView.setAdapter(sca);
		// Requête dans la base
		clause = Kitting.NUMERO_CHEMINEMENT + " ='" + numeroCh + "'";
		cursor = cr.query(urlKitting, columns, clause, null, null);
		sca.changeCursor(cursor);

	}
}
