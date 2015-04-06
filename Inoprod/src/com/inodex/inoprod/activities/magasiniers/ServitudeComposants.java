package com.inodex.inoprod.activities.magasiniers;

import java.io.File;
import java.io.FileInputStream;
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
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.inodex.inoprod.R;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.business.BOMProvider;
import com.inodex.inoprod.business.Durees.Duree;
import com.inodex.inoprod.business.DureesProvider;
import com.inodex.inoprod.business.Production.Fil;
import com.inodex.inoprod.business.ProductionProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableBOM.BOM;
import com.inodex.inoprod.business.TableKittingCable.Kitting;
import com.inodex.inoprod.business.TableSequencement.Operation;
import com.inodex.inoprod.business.TimeConverter;

/**
 * Ecran affichant l'ensemble des composants à servir
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class ServitudeComposants extends Activity {

	/** Uri à manipuler */
	private Uri urlBOM = BOMProvider.CONTENT_URI;
	private Uri urlSeq = SequencementProvider.CONTENT_URI;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	/** Etat de la production */
	private boolean prodAchevee;

	/** Liste à afficher dans l'adapteur */
	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();
	private HashMap<String, String> element;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;
	private HashSet<Integer> rowId = new HashSet<Integer>();

	/** Indice de l'opération courante */
	private int indiceCourant = 0;
	private int indiceLimite = 0;

	private SimpleCursorAdapter sca;

	private int numeroDebit, nbItem, nbRows, idFirst;
	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateDebut, dateRealisation;
	private long dureeMesuree = 0;
	private Time heureRealisation = new Time();
	private Iterator<Integer> rowIt;

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;
	private String clause, numeroOperation, numeroCom, descriptionOperation;

	/** Tableau des infos produit */
	private String labels[];

	/** Elements à récuperer de la vue */
	private TextView designation, referenceImposee, fournisseur,
			referenceFabricant, referenceInterne, servi;
	private ImageButton petitePause, grandePause, infoButton, boutonCheck;
	private GridView gridView;
	private LinearLayout layoutServi;

	/** Colonnes utilisés pour les requêtes */
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
	private int[] layouts = new int[] { R.id.repereElectrique,
			R.id.numeroConnecteur, R.id.positionChariot, R.id.ordreRealisation,
			R.id.quantite, R.id.uniteMesure, R.id.numeroLot, R.id.articleServi };

	private String columnsProd[] = new String[] { Fil._id,
			Fil.DESIGNATION_PRODUIT, Fil.NUMERO_REVISION_HARNAIS, Fil.STANDARD,
			Fil.NUMERO_HARNAIS_FAISCEAUX, Fil.REFERENCE_FICHIER_SOURCE,
			Fil.NUMERO_COMPOSANT_ABOUTISSANT, Fil.NUMERO_COMPOSANT_TENANT };
	private Cursor cursorInfo;
	private Uri urlProd = ProductionProvider.CONTENT_URI;

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
		setContentView(R.layout.activity_servitude_composants);

		// Récupération des éléments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		dateDebut = new Date();
		dureeMesuree = 0;

		// initialisation de la production
		prodAchevee = false;
		cr = getContentResolver();
		contact = new ContentValues();

		// Récuperation des éléments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		designation = (TextView) findViewById(R.id.textView3);
		fournisseur = (TextView) findViewById(R.id.textView4);
		referenceFabricant = (TextView) findViewById(R.id.textView5);
		referenceInterne = (TextView) findViewById(R.id.textView6);
		referenceImposee = (TextView) findViewById(R.id.textView7);
		layoutServi = (LinearLayout) findViewById(R.id.layout_servi);
		petitePause = (ImageButton) findViewById(R.id.imageButton1);
		grandePause = (ImageButton) findViewById(R.id.exitButton1);

		// Récuperation du numéro d'opération courant
		clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
		cursor = cr.query(urlSeq, columnsSeq, clause, null, Operation._id
				+ " ASC");
		if (cursor.moveToFirst()) {

			numeroOperation = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
			descriptionOperation = cursor.getString(cursor
					.getColumnIndex(Operation.DESCRIPTION_OPERATION));
			numeroCom = descriptionOperation.substring(21, 24);
			Log.e("N", numeroCom);
		}

		// Récupération du numéro de débit
		clause = new String(BOM.NUMERO_OPERATION + "='" + numeroOperation + "'");
		cursorA = cr.query(urlBOM, columnsBOM, clause, null, BOM._id + " ASC");
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

			// Affichage des éléments du débit en cours
			try {
				designation.append(": "
						+ cursor.getString(cursor
								.getColumnIndex(BOM.DESIGNATION_COMPOSANT)));
			} catch (NullPointerException e) {
			}
			try {
				fournisseur.append(" "
						+ cursor.getString(cursor
								.getColumnIndex(BOM.FOURNISSEUR_FABRICANT)));
			} catch (NullPointerException e) {
			}
			try {
				referenceFabricant.append(": "
						+ cursor.getString(cursor
								.getColumnIndex(BOM.REFERENCE_FABRICANT2)));
			} catch (NullPointerException e) {
			}
			try {
				referenceInterne.append(": "
						+ cursor.getString(cursor
								.getColumnIndex(BOM.REFERENCE_INTERNE)));
			} catch (NullPointerException e) {
			}
			try {
				int ref = cursor.getInt(cursor
						.getColumnIndex(BOM.REFERENCE_IMPOSEE));
				if (ref == 0) {
					referenceImposee.append(": Non ");
				} else {
					referenceImposee.append(": Oui ");
				}
			} catch (NullPointerException e) {
			}

		}

		// Simulation lecture fichier excel
		InputStream input = null;
		try {
			input = new FileInputStream(Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/Android/data/com.inodex.inoprod/" + "kittingTetes.xls");
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
				if (Integer.parseInt(row
						.getCell(colonnes.get(BOM.NUMERO_DEBIT)).toString()) == numeroDebit
						) {
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

		// Affichage de la prémiere ligne du contenut
		// displayContentProvider();

		// Affichage du temps nécessaire
		timer = (TextView) findViewById(R.id.timeDisp);
		dureeTotal = 0;
		cursorTime = cr.query(urlTim, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%kit%' ", null, Duree._id);
		if (cursorTime.moveToFirst()) {
			dureeTotal += TimeConverter.convert(cursorTime.getString(cursorTime
					.getColumnIndex(Duree.DUREE_THEORIQUE)));

		}
		dureeTotal = dureeTotal * nbRows;
		timer.setTextColor(Color.GREEN);
		timer.setText(TimeConverter.display(dureeTotal));

		// Etape suivante
		boutonCheck = (ImageButton) findViewById(R.id.imageButton2);
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// Controle de l'état de la production
				if (prodAchevee) { // Fin de la prodction

					// MAJ table BOM
					try {
						File data = new File(Environment
								.getExternalStorageDirectory()
								.getAbsolutePath()
								+ "/Android/data/com.inodex.inoprod/"
								+ "kittingTetes.xls");

						InputStream input = new FileInputStream(data);
						// Interpretation
						// du
						// fichier
						// a
						// l'aide
						// de
						// Apache
						// POI
						POIFSFileSystem fs = new POIFSFileSystem(input);
						HSSFWorkbook wb = new HSSFWorkbook(fs);
						HSSFSheet sheet = wb.getSheet("Débit");
						ContentValues contact = new ContentValues();
						Iterator rows = sheet.rowIterator();

						HSSFRow row = (HSSFRow) rows.next();
						HashMap<String, Integer> colonnes = new HashMap<String, Integer>();
						for (int k = 0; k < row.getLastCellNum(); k++) {
							colonnes.put(row.getCell(k).toString(), k);

						} // Stockage des indices des colonnes int indice = 1;
						while (rows.hasNext()) {
							row = (HSSFRow) rows.next();
							try {
								contact.put(
										BOM.NUMERO_LOT_SCANNE,
										row.getCell(
												colonnes.get(BOM.NUMERO_LOT_SCANNE))
												.toString());
							} catch (NullPointerException e) {
								Log.e("erreur lecture", e.getMessage()
										.toString());
							}
							try {
								contact.put(
										BOM.REFERENCE_FABRICANT_SCANNE,
										row.getCell(
												colonnes.get(BOM.REFERENCE_FABRICANT_SCANNE))
												.toString());
							} catch (NullPointerException e) {
								Log.e("erreur lecture", e.getMessage()
										.toString());
							}

							// Ajout de l'entité
							getContentResolver().update(urlBOM, contact,
									BOM._id + "='" + row.getRowNum() + 1 + "'",
									null); // Ecrasement de ses
											// données pour
											// passer à la
											// suivante
							contact.clear();
						}
						input.close();
					} catch (Exception e) {
						Log.e("err3", " " + e);
					}

					try {
						int test = opId[indiceCourant]; // Si OK il reste encore
						clause = Operation._id + "='" + test + "'";
						cursor = cr.query(urlSeq, columnsSeq, clause, null,
								Operation._id);
						if (cursor.moveToFirst()) {
							String firstOperation = cursor.getString(cursor
									.getColumnIndex(Operation.DESCRIPTION_OPERATION));
							Intent toNext = null;
							if (firstOperation.startsWith("Débit du fil")) {
								toNext = new Intent(ServitudeComposants.this,
										ImportCoupeCables.class);
							} else if (firstOperation
									.startsWith("Regroupement des")) {
								toNext = new Intent(ServitudeComposants.this,
										RegroupementCables.class);
							} else if (firstOperation.startsWith("Débit pour")) {
								toNext = new Intent(ServitudeComposants.this,
										SaisieTracabiliteComposant.class);
							} else {
								toNext = new Intent(ServitudeComposants.this,
										KittingCablesComposants.class);
							}

							toNext.putExtra("Noms", nomPrenomOperateur);
							toNext.putExtra("opId", opId);
							toNext.putExtra("Indice", indiceCourant);
							startActivity(toNext);
							finish();
						}

					} catch (Exception e) {

					}
				} else {// Production toujours en cours
					// On affiche le cable suivant à débiter
					displayContentProvider();
					indiceCourant++;

				}
			}
		});

		// Grande pause
		grandePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ServitudeComposants.this);
				builder.setMessage("Êtes-vous sur de vouloir quitter l'application ?");
				builder.setCancelable(false);
				builder.setPositiveButton("Oui",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								/*
								 * Intent toMain = new Intent(
								 * CheminementTa.this, MainActivity.class);
								 * startActivity(toMain);
								 */
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

		// Petite Pause
		petitePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dureeMesuree += new Date().getTime() - dateDebut.getTime();
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ServitudeComposants.this);
				builder.setMessage("L'opération est en pause. Cliquez sur le bouton pour reprendre.");
				builder.setCancelable(false);

				builder.setNegativeButton("Retour",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {

								dateDebut = new Date();
								dialog.cancel();

							}
						});
				builder.show();

			}
		});

		// Info Produit
		ImageButton infoProduit = (ImageButton) findViewById(R.id.infoButton1);
		infoProduit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cursorInfo = cr.query(urlProd, columnsProd,
						Fil.NUMERO_COMPOSANT_ABOUTISSANT + " ='" + numeroCom
								+ "' OR " + Fil.NUMERO_COMPOSANT_TENANT + "='"
								+ numeroCom + "'", null, null);
				Intent toInfo = new Intent(ServitudeComposants.this,
						InfoProduit.class);
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
					+ "/Android/data/com.inodex.inoprod/" + "kittingTetes.xls");
			POIFSFileSystem fs = new POIFSFileSystem(input);
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			// Iteration sur chacune des lignes du fichier
			Iterator rows = sheet.rowIterator();
			HSSFRow row = (HSSFRow) rows.next();
			HashMap<String, Integer> colonnes = new HashMap<String, Integer>();

			if (rowId.iterator().hasNext()) {

				row = (HSSFRow) sheet.getRow(rowIt.next());
				Log.d("N°ligne", row.getRowNum() + "");
				Log.d("N°fil", row.getCell(7).toString());
				element = new HashMap<String, String>();
				element.put(columnsBOM[0], row.getCell(6).toString());

				element.put(columnsBOM[1], row.getCell(5).toString());
				element.put(columnsBOM[2], row.getCell(3).toString());
				element.put(columnsBOM[3], row.getCell(7).toString());
				element.put(columnsBOM[4], row.getCell(16).toString());
				element.put(columnsBOM[5], row.getCell(15).toString());
				element.put(columnsBOM[6], row.getCell(18).toString());
				element.put(columnsBOM[7], "X");
				liste.add(element);
				indiceLimite++;

			}
			input.close();

		} catch (IOException e) {
			Log.e("Fichier", "Erreur lecture");
		}

		// Création du SimpleCursorAdapter affilié au GridView
		SimpleAdapter sca = new SimpleAdapter(this, liste,
				R.layout.grid_layout_servitude_composants, columnsBOM, layouts);

		gridView.setAdapter(sca);

		// Vérification de l'état de la production
		if (indiceLimite == nbRows) {
			prodAchevee = true;
			Toast.makeText(this, "Production achevée", Toast.LENGTH_LONG)
					.show();
		}

		// MAJ Table de sequencement
		dateRealisation = new Date();
		contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0] + " "
				+ nomPrenomOperateur[1]);
		contact.put(Operation.DATE_REALISATION, dateRealisation.toGMTString());
		heureRealisation.setToNow();
		contact.put(Operation.HEURE_REALISATION, heureRealisation.toString());

		dureeMesuree += dateRealisation.getTime() - dateDebut.getTime();
		contact.put(Operation.DUREE_MESUREE, dureeMesuree / 1000);
		cr.update(urlSeq, contact, Operation._id + " = ?",
				new String[] { Integer.toString(opId[indiceCourant]) });
		contact.clear();

		dateDebut = new Date();
		dureeMesuree = 0;

	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}
}
