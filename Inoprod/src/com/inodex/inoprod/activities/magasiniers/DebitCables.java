package com.inodex.inoprod.activities.magasiniers;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.inodex.inoprod.R;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.business.Durees.Duree;
import com.inodex.inoprod.business.DureesProvider;
import com.inodex.inoprod.business.KittingProvider;
import com.inodex.inoprod.business.Production.Fil;
import com.inodex.inoprod.business.ProductionProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableKittingCable.Kitting;
import com.inodex.inoprod.business.TableSequencement.Operation;

/**
 * Ecran affichant au fur et à mesure le débit des cables
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class DebitCables extends Activity {

	/** Elements à récuperer de la vue */
	private TextView temps = null;
	private ImageButton boutonCheck, infoProduit;
	private TextView operation = null;
	private GridView gridView;

	/** Uri à manipuler */
	private Uri urlDuree = DureesProvider.CONTENT_URI;
	private Uri urlKitting = KittingProvider.CONTENT_URI;
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlProd = ProductionProvider.CONTENT_URI;
	private String clause, numeroOperation;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;

	/** Tableau des infos produit */
	private String labels[];

	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateRealisation = new Date();
	private Time heureRealisation = new Time();

	private int numeroDebit, nbRows, idFirst;
	private boolean prodAchevee;
	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA, cursorB;
	private ContentResolver cr;
	private ContentValues contact;

	/** Colonnes utilisés pour les requêtes */
	private String columnsDuree[] = new String[] { Duree._id,
			Duree.CODE_OPERATION, Duree.DUREE_THEORIQUE };

	private String columnsKitting[] = new String[] { Kitting.NUMERO_FIL_CABLE,
			Kitting.NUMERO_POSITION_CHARIOT, Kitting.REPERE_ELECTRIQUE,
			Kitting.NUMERO_COMPOSANT, Kitting.ORDRE_REALISATION,
			Kitting.LONGUEUR_FIL_CABLE, Kitting.UNITE, Kitting.NUMERO_DEBIT,
			Kitting._id, Kitting.NUMERO_OPERATION };
	private int[] layouts = new int[] { R.id.numeroFil, R.id.positionChariot,
			R.id.repereElectrique, R.id.numeroConnecteur,
			R.id.ordreRealisation, R.id.longueurCoupe, R.id.uniteMesure };

	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION };

	private String columnsProd[] = new String[] { Fil._id,
			Fil.DESIGNATION_PRODUIT, Fil.NUMERO_REVISION_HARNAIS, Fil.STANDARD,
			Fil.NUMERO_HARNAIS_FAISCEAUX, Fil.REFERENCE_FICHIER_SOURCE };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debit_cables);

		// Récupération des éléments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		

		// initialisation de la production
		prodAchevee = false;

		// Récuperation des éléments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		operation = (TextView) findViewById(R.id.textView3);

		// Récuperation du numéro d'opération courant
		clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
		cursor = cr.query(urlSeq, columnsSeq, clause, null, Operation._id + " ASC" );
		if (cursor.moveToFirst()) {
			operation.setText(cursor.getString(cursor
					.getColumnIndex(Operation.RANG_1_1)));
			numeroOperation = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
		}

		// Récupération du numéro de débit
		clause = new String(Kitting.NUMERO_OPERATION + "='" + numeroOperation
				+ "'");
		cursorA = cr.query(urlKitting, columnsKitting, clause, null, Kitting._id + " ASC");
		if (cursorA.moveToFirst()) {
			numeroDebit = cursorA.getInt(cursorA
					.getColumnIndex(Kitting.NUMERO_DEBIT));
			idFirst = cursorA.getInt(cursorA.getColumnIndex(Kitting._id));
			cursorB = cr.query(urlProd, columnsProd,Fil.NUMERO_FIL_CABLE + " = " +Kitting.NUMERO_FIL_CABLE , null,null );
			
		} else {
			Toast.makeText(this, "Debit non trouvée", Toast.LENGTH_LONG).show();
		}

		clause = new String(Kitting.NUMERO_DEBIT + "='" + numeroDebit + "'");
		nbRows = cr.query(urlKitting, columnsKitting, clause, null, null)
				.getCount();

		contact = new ContentValues();

		// Affichage de la prémiere ligne du contenu
		displayContentProvider();

		// Etape suivante
		boutonCheck = (ImageButton) findViewById(R.id.exitButton1);
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				indiceCourant++;
				// Controle de l'état de la production
				if (prodAchevee) { // Fin de la prodction
					
					try {
						int test = opId[indiceCourant]; // Si OK il reste encore
														// des cables à débiter
						Intent toNext = new Intent(DebitCables.this,
								ImportCoupeCables.class);
						toNext.putExtra("Noms", nomPrenomOperateur);
						toNext.putExtra("opId", opId);
						toNext.putExtra("Indice", indiceCourant);
						startActivity(toNext);

					} catch (ArrayIndexOutOfBoundsException e) {
						// Il ne reste plus de cables à débiter
						// On passe donc au regroupement
						clause = new String(Operation.RANG_1_1 + "='"
								+ "Regroupement câbles" + "'");
						cursor = cr.query(urlSeq, columnsSeq, clause, null,
								Operation._id + " ASC");
						// Rempliassage du tableau pour chaque regroupement
						if (cursor.moveToFirst()) {
							opId = new int[cursor.getCount()];
							do {
								opId[cursor.getPosition()] = cursor
										.getInt(cursor
												.getColumnIndex(Operation._id));

							} while (cursor.moveToNext());
						}

						Intent toNext = new Intent(DebitCables.this,
								RegroupementCables.class);
						toNext.putExtra("Noms", nomPrenomOperateur);
						toNext.putExtra("opId", opId);
						toNext.putExtra("Indice", 0);
						startActivity(toNext);

					}
				} else { // Production toujours en cours
					// On affiche le cable suivant à débiter
					displayContentProvider();
				}
			}
		});
		
		// Info Produit
				infoProduit = (ImageButton) findViewById(R.id.infoButton1);
				infoProduit.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent toInfo = new Intent(DebitCables.this,
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
	 * Genère l'affichage de l'annuaire en utilisant un SimpleCursorAdapter Le
	 * layout GridView est récupéré puis utiliser pour afficher chacun des
	 * éléments
	 */
	private void displayContentProvider() {
		// Création du SimpleCursorAdapter affilié au GridView
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.grid_layout_debit_cable, null, columnsKitting, layouts);

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

		// Affichage des cables à débiter ou dèja débité
		clause = new String(Kitting.NUMERO_DEBIT + "='" + numeroDebit
				+ "' AND " + Kitting._id + "<='" + (opId[indiceCourant]) + "'");
		cursor = cr.query(urlKitting, columnsKitting, clause, null, null);
		sca.changeCursor(cursor);

		// Vérification de l'état de la production
		if (cursor.getCount() == nbRows) {
			prodAchevee = true;
			Toast.makeText(this, "Production achevée", Toast.LENGTH_LONG)
					.show();
		}

	}
	
	
	/**Bloquage du bouton retour */
	public void onBackPressed() {

	}

}
