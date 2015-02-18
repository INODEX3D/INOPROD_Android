package com.inodex.inoprod.activities.magasiniers;

import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.inodex.inoprod.R;
import com.inodex.inoprod.business.BOMProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableBOM.BOM;
import com.inodex.inoprod.business.TableSequencement.Operation;

/**
 * Ecran affichant l'ensemble des composants � servir
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class ServitudeComposants extends Activity {

	/** Uri � manipuler */
	private Uri urlBOM = BOMProvider.CONTENT_URI;
	private Uri urlSeq = SequencementProvider.CONTENT_URI;

	/** Curseur et Content Resolver � utiliser lors des requ�tes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	/** Etat de la production */
	private boolean prodAchevee;

	/** Tableau des op�rations � r�aliser */
	private int opId[] = null;

	/** Indice de l'op�ration courante */
	private int indiceCourant = 0;

	private SimpleCursorAdapter sca;

	private int numeroDebit, nbItem, nbRows, idFirst;
	/** Heure et dates � ajouter � la table de s�quencment */
	private Date dateRealisation = new Date();
	private Time heureRealisation = new Time();

	/** Nom de l'op�rateur */
	private String nomPrenomOperateur[] = null;
	private String clause, numeroOperation;

	/** Elements � r�cuperer de la vue */
	private TextView designation, referenceImposee, fournisseur,
			referenceFabricant, referenceInterne, servi;
	private ImageButton petitePause, grandePause, infoButton, boutonCheck;
	private GridView gridView;
	private LinearLayout layoutServi;

	/** Colonnes utilis�s pour les requ�tes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION };

	private String columnsBOM[] = new String[] { BOM.REPERE_ELECTRIQUE_TENANT,
			BOM.NUMERO_COMPOSANT, BOM.NUMERO_POSITION_CHARIOT,
			BOM.ORDRE_REALISATION, BOM.QUANTITE, BOM.UNITE,
			BOM.NUMERO_LOT_SCANNE, BOM.NUMERO_DEBIT, BOM.DESIGNATION_COMPOSANT,
			BOM.FOURNISSEUR_FABRICANT, BOM.REFERENCE_IMPOSEE,
			BOM.REFERENCE_INTERNE, BOM.REFERENCE_FABRICANT2, BOM._id };
	private int[] layouts = new int[] { R.id.repereElectrique,
			R.id.numeroConnecteur, R.id.positionChariot, R.id.ordreRealisation,
			R.id.quantite, R.id.uniteMesure, R.id.numeroLot };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_servitude_composants);

		// R�cup�ration des �l�ments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");

		// initialisation de la production
		prodAchevee = false;
		cr = getContentResolver();
		contact = new ContentValues();

		// R�cuperation des �l�ments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		designation = (TextView) findViewById(R.id.textView3);
		fournisseur = (TextView) findViewById(R.id.textView4);
		referenceFabricant = (TextView) findViewById(R.id.textView5);
		referenceInterne = (TextView) findViewById(R.id.textView6);
		referenceImposee = (TextView) findViewById(R.id.textView7);
		layoutServi = (LinearLayout) findViewById(R.id.layout_servi);

		// R�cuperation du num�ro d'op�ration courant
		clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
		cursor = cr.query(urlSeq, columnsSeq, clause, null, Operation._id + " ASC");
		if (cursor.moveToFirst()) {

			numeroOperation = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
		}

		// R�cup�ration du num�ro de d�bit
		clause = new String(BOM.NUMERO_OPERATION + "='" + numeroOperation + "'");
		cursorA = cr.query(urlBOM, columnsBOM, clause, null, BOM._id +" ASC");
		if (cursorA.moveToFirst()) {
			numeroDebit = cursorA.getInt(cursorA
					.getColumnIndex(BOM.NUMERO_DEBIT));
			clause = new String(BOM.NUMERO_DEBIT + "='" + numeroDebit + "'");
			idFirst = cursorA.getInt(cursorA.getColumnIndex(BOM._id));
			nbRows = cr.query(urlBOM, columnsBOM, clause, null, null)
					.getCount();

		}

		cursor = cr.query(urlBOM, columnsBOM, clause, null, null);
		if (cursor.moveToFirst()) {

			// Affichage des �l�ments du d�bit en cours
			try {
				designation.append(cursor.getString(cursor
						.getColumnIndex(BOM.DESIGNATION_COMPOSANT)));
			} catch (NullPointerException e) {
			}
			try {
				fournisseur.append(cursor.getString(cursor
						.getColumnIndex(BOM.FOURNISSEUR_FABRICANT)));
			} catch (NullPointerException e) {
			}
			try {
				referenceFabricant.append(cursor.getString(cursor
						.getColumnIndex(BOM.REFERENCE_FABRICANT2)));
			} catch (NullPointerException e) {
			}
			try {
				referenceInterne.append(cursor.getString(cursor
						.getColumnIndex(BOM.REFERENCE_INTERNE)));
			} catch (NullPointerException e) {
			}
			try {
				referenceImposee.append(Integer.toString(cursor.getInt(cursor
						.getColumnIndex(BOM.REFERENCE_IMPOSEE))));
			} catch (NullPointerException e) {
			}

		}

		// Affichage de la pr�miere ligne du contenut
		displayContentProvider();

		// Etape suivante
		boutonCheck = (ImageButton) findViewById(R.id.imageButton2);
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				indiceCourant++;
				// Controle de l'�tat de la production
				if (prodAchevee) { // Fin de la prodction
					
					try {
						int test = opId[indiceCourant]; // Si OK il reste encore
						// des cables � d�biter
						Intent toNext = new Intent(ServitudeComposants.this,
								SaisieTracabiliteComposant.class);
						toNext.putExtra("Noms", nomPrenomOperateur);
						toNext.putExtra("opId", opId);
						toNext.putExtra("Indice", indiceCourant);
						startActivity(toNext);
						finish();

					} catch (ArrayIndexOutOfBoundsException e) {
						// Il ne reste plus de cables � d�biter
						// On passe donc au regroupement
						clause = new String(Operation.RANG_1_1
								+ " LIKE '%Constitution%' ");
						cursor = cr.query(urlSeq, columnsSeq, clause, null,
								Operation._id + " ASC");
						// Rempliassage du tableau pour chaque numero de d�bit
						if (cursor.moveToFirst()) {
							opId = new int[cursor.getCount()];
							do {
								opId[cursor.getPosition()] = cursor
										.getInt(cursor
												.getColumnIndex(Operation._id));

							} while (cursor.moveToNext());
						}

						Intent toNext = new Intent(ServitudeComposants.this,
								KittingCablesComposants.class);
						toNext.putExtra("Noms", nomPrenomOperateur);
						toNext.putExtra("opId", opId);
						toNext.putExtra("Indice", 0);
						startActivity(toNext);
						finish();

					}
				} else {// Production toujours en cours
					// On affiche le cable suivant � d�biter
					displayContentProvider();
				}
			}
		});

	}

	/**
	 * Gen�re l'affichage de l'annuaire en utilisant un SimpleCursorAdapter Le
	 * layout GridView est r�cup�r� puis utiliser pour afficher chacun des
	 * �l�ments
	 */
	private void displayContentProvider() {
		// Cr�ation du SimpleCursorAdapter affili� au GridView
		sca = new SimpleCursorAdapter(this,
				R.layout.grid_layout_servitude_composants, null, columnsBOM,
				layouts);

		gridView.setAdapter(sca);

		servi = new TextView(ServitudeComposants.this);
		servi.setWidth(44);
		servi.setHeight(30);
		servi.setTextColor(Color.RED);
		servi.setTextScaleX(9);
		layoutServi.addView(servi);

		// Affichage des cables � d�biter ou d�ja d�bit�
		clause = new String(BOM.NUMERO_DEBIT + "='" + numeroDebit + "' AND "
				+ BOM._id + "<='" + (idFirst) +"'");
		cursor = cr.query(urlBOM, columnsBOM, clause, null, null);
		sca.changeCursor(cursor);

		// V�rification de l'�tat de la production
		if (cursor.getCount() == nbRows) {
			prodAchevee = true;
			Toast.makeText(this, "Production achev�e", Toast.LENGTH_LONG)
					.show();
		}
		
		// MAJ Table de sequencement
				contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0] + " "
						+ nomPrenomOperateur[1]);
				contact.put(Operation.DATE_REALISATION, dateRealisation.toGMTString());
				heureRealisation.setToNow();
				contact.put(Operation.HEURE_REALISATION, heureRealisation.toString());
				cr.update(urlSeq, contact, Operation._id + " = ?",
						new String[] { Integer.toString(idFirst++) });
				contact.clear();

	}
	
	/**Bloquage du bouton retour */
	public void onBackPressed() {

	}
}
