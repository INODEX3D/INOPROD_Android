package com.inodex.inoprod.activities.magasiniers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.inodex.inoprod.R;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.business.Durees.Duree;
import com.inodex.inoprod.business.DureesProvider;
import com.inodex.inoprod.business.KittingProvider;
import com.inodex.inoprod.business.TimeConverter;
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
	private String clause, numeroOperation, numeroCo;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;
	private HashSet<Integer> rowId = new HashSet<Integer>();

	/** Indice de l'opération courante */
	private int indiceCourant = 0;
	private int indiceLimite = 0;

	/** Liste à afficher dans l'adapteur */
	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();
	private HashMap<String, String> element;
	/** Tableau des infos produit */
	private String labels[];

	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateDebut, dateRealisation;
	private long dureeMesuree = 0;
	private Time heureRealisation = new Time();

	private int numeroDebit, nbRows, idFirst;
	private boolean prodAchevee;
	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA, cursorInfo;
	private ContentResolver cr;
	private ContentValues contact;
	private boolean affichage;
	private Iterator<Integer> rowIt;

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

	private TextView timer;
	private Cursor cursorTime;
	private Uri urlTim = DureesProvider.CONTENT_URI;
	private String colTim[] = new String[] { Duree._id,
			Duree.DESIGNATION_OPERATION, Duree.DUREE_THEORIQUE

	};
	private long dureeTotal;

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
		dateDebut = new Date();

		// initialisation de la production
		prodAchevee = false;

		// Récuperation des éléments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		operation = (TextView) findViewById(R.id.textView3);

		// Récuperation du numéro d'opération courant
		clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
		cursor = cr.query(urlSeq, columnsSeq, clause, null, Operation._id
				+ " ASC");
		if (cursor.moveToFirst()) {
			operation.setText(cursor.getString(cursor
					.getColumnIndex(Operation.RANG_1_1)));
			numeroOperation = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
		}

		// Récupération du numéro de débit
		clause = new String(Kitting.NUMERO_OPERATION + "='" + numeroOperation
				+ "'");
		cursorA = cr.query(urlKitting, columnsKitting, clause, null,
				Kitting._id + " ASC");
		if (cursorA.moveToFirst()) {
			numeroDebit = cursorA.getInt(cursorA
					.getColumnIndex(Kitting.NUMERO_DEBIT));
			numeroCo = cursorA.getString(cursorA
					.getColumnIndex(Kitting.NUMERO_COMPOSANT));
			idFirst = cursorA.getInt(cursorA.getColumnIndex(Kitting._id));
			cursorInfo = cr.query(urlProd, columnsProd, Fil.NUMERO_FIL_CABLE
					+ " = " + Kitting.NUMERO_FIL_CABLE, null, null);
			Log.e("NumeroConnectuer", numeroCo);
			Log.e("NumeroDebit", numeroDebit + "");
		} else {
			Toast.makeText(this, "Debit non trouvée", Toast.LENGTH_LONG).show();
		}

		clause = new String(Kitting.NUMERO_DEBIT + "='" + numeroDebit + "'");
		nbRows = cr.query(urlKitting, columnsKitting, clause, null, null)
				.getCount();

		// Affichage du temps nécessaire
		timer = (TextView) findViewById(R.id.timeDisp);
		dureeTotal = 0;
		cursorTime = cr.query(urlTim, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%Débit%' ", null, Duree._id);
		if (cursorTime.moveToFirst()) {
			dureeTotal += TimeConverter.convert(cursorTime.getString(cursorTime
					.getColumnIndex(Duree.DUREE_THEORIQUE)));

		}
		dureeTotal = dureeTotal * nbRows;
		timer.setTextColor(Color.GREEN);
		timer.setText(TimeConverter.display(dureeTotal));

		contact = new ContentValues();
		affichage = false;

		// Affichage de la prémiere ligne du contenu
		// displayContentProvider();

		// Simulation lecture fichier excel
		InputStream input = null;
		try {
			input = new FileInputStream(Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/Android/data/com.inodex.inoprod/" + "debitCables.xls");
			POIFSFileSystem fs = new POIFSFileSystem(input);
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			// Iteration sur chacune des lignes du fichier
			Iterator rows = sheet.rowIterator();
			HSSFRow row = (HSSFRow) rows.next();
			HashMap<String, Integer> colonnes = new HashMap<String, Integer>();

			// Stockage des indices des colonnes
			for (int i = 0; i < row.getLastCellNum(); i++) {
				colonnes.put(row.getCell(i).toString(), i);
				Log.d("en tete", row.getCell(i).toString());

			}

			while (rows.hasNext()) {
				row = (HSSFRow) rows.next();
				if (Integer.parseInt(row.getCell(
						colonnes.get(Kitting.NUMERO_DEBIT)).toString()) == numeroDebit) {
					rowId.add(row.getRowNum());
				}
			}
			rowIt = rowId.iterator();
			Log.d("Nombre ligne", rowId.size() + "");
			/*
			 * 
			 * for (int i=1; i<= nbRows; i++) { Date debut = new Date(); while(
			 * new Date().getTime() - debut.getTime() <2000) {
			 * 
			 * } displayContentProvider(); indiceCourant++; }
			 */
			input.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("Fichier", "Erreur lecture");
		}

		// Etape suivante
		boutonCheck = (ImageButton) findViewById(R.id.exitButton1);
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

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
						finish();

					} catch (ArrayIndexOutOfBoundsException e) {
						// Il ne reste plus de cables à débiter
						// On passe donc au regroupement
						clause = new String(Operation.RANG_1_1 + " LIKE '%"
								+ "Regroupement des câbles%" + "'");
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
						finish();

					}
				} else { // Production toujours en cours
					// On affiche le cable suivant à débiter

					displayContentProvider();
					indiceCourant++;

				}
			}

		});

		// Info Produit
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);
		infoProduit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent toInfo = new Intent(DebitCables.this, InfoProduit.class);
				labels = new String[7];

				if (cursorInfo.moveToFirst()) {
					labels[0] = cursorInfo.getString(cursorInfo
							.getColumnIndex(Fil.DESIGNATION_PRODUIT));
					labels[1] = cursorInfo.getString(cursorInfo
							.getColumnIndex(Fil.NUMERO_HARNAIS_FAISCEAUX));
					labels[2] = cursorInfo.getString(cursorInfo
							.getColumnIndex(Fil.STANDARD));
					labels[3] = "";
					labels[4] = "";
					labels[5] = cursorInfo.getString(cursorInfo
							.getColumnIndex(Fil.NUMERO_REVISION_HARNAIS));
					labels[6] = cursorInfo.getString(cursorInfo
							.getColumnIndex(Fil.REFERENCE_FICHIER_SOURCE));
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
		InputStream input = null;
		try {
			input = new FileInputStream(Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/Android/data/com.inodex.inoprod/" + "debitCables.xls");
			POIFSFileSystem fs = new POIFSFileSystem(input);
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			// Iteration sur chacune des lignes du fichier
			Iterator rows = sheet.rowIterator();
			HSSFRow row = (HSSFRow) rows.next();
			HashMap<String, Integer> colonnes = new HashMap<String, Integer>();

			if (rowIt.hasNext()) {

				row = (HSSFRow) sheet.getRow(rowIt.next());
				Log.d("N°ligne", row.getRowNum() + "");
				Log.d("N°fil", row.getCell(7).toString());
				element = new HashMap<String, String>();
				element.put(columnsKitting[0], row.getCell(7).toString());

				element.put(columnsKitting[1], row.getCell(2).toString());
				element.put(columnsKitting[2], row.getCell(4).toString());
				element.put(columnsKitting[3], row.getCell(3).toString());
				element.put(columnsKitting[4], row.getCell(5).toString());
				element.put(columnsKitting[5], row.getCell(11).toString());
				element.put(columnsKitting[6], row.getCell(12).toString());
				liste.add(element);
				indiceLimite++;

			} else {
				prodAchevee = true;
				Toast.makeText(this, "Production achevée", Toast.LENGTH_LONG)
						.show();
			}
			input.close();

		} catch (IOException e) {
			Log.e("Fichier", "Erreur lecture");
		}

		// Création du SimpleCursorAdapter affilié au GridView
		SimpleAdapter sca = new SimpleAdapter(this, liste,
				R.layout.grid_layout_debit_cable, columnsKitting, layouts);

		gridView.setAdapter(sca);
		// MAJ Table de sequencement
		dateRealisation = new Date();
		contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0] + " "
				+ nomPrenomOperateur[1]);
		contact.put(Operation.DATE_REALISATION, dateRealisation.toGMTString());
		heureRealisation.setToNow();
		
		dureeMesuree += dateRealisation.getTime() - dateDebut.getTime();
		contact.put(Operation.DUREE_MESUREE, dureeMesuree / 1000);
		contact.put(Operation.HEURE_REALISATION, heureRealisation.toString());
		cr.update(urlSeq, contact, Operation._id + " = ?",
				new String[] { Integer.toString(opId[indiceCourant]) });
		contact.clear();

		// Affichage des cables à débiter ou dèja débité
		/*
		 * clause = new String(Kitting.NUMERO_DEBIT + "='" + numeroDebit +
		 * "' AND " + Kitting._id + "<='" + (opId[indiceCourant]) + "'"); cursor
		 * = cr.query(urlKitting, columnsKitting, clause, null, null);
		 */

		affichage = false;

		// Vérification de l'état de la production
		if (!(rowIt.hasNext())) {
			prodAchevee = true;
			Toast.makeText(this, "Production achevée", Toast.LENGTH_LONG)
					.show();
		}
		
		dateDebut = new Date();
		dureeMesuree=0;

	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}

}
