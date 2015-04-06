package com.inodex.inoprod.activities.magasiniers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.inodex.inoprod.R;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.activities.cableur.EnfichagesTa;
import com.inodex.inoprod.activities.cableur.RepriseBlindageTa;
import com.inodex.inoprod.business.BOMProvider;
import com.inodex.inoprod.business.CheminementProvider;
import com.inodex.inoprod.business.KittingProvider;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.Production.Fil;
import com.inodex.inoprod.business.TableBOM.BOM;
import com.inodex.inoprod.business.TableKittingCable.Kitting;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

/**
 * Ecran permettant la saisie de la r�f�rence et du num�ro de lot d'un composant
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class SaisieTracabiliteComposant extends Activity {

	/** Uri � manipuler */
	private Uri urlBOM = BOMProvider.CONTENT_URI;
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlChem = CheminementProvider.CONTENT_URI;
	private Uri urlKitting = KittingProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;

	/** Tableau des op�rations � r�aliser */
	private int opId[] = null;
	private Iterator<Integer> rowIt;

	/** Indice de l'op�ration courante */
	private int indiceCourant = 0;
	private HashSet<Integer> rowId = new HashSet<Integer>();

	/** Numero de d�bit courant */
	private int numeroDebit;
	/** Heure et dates � ajouter � la table de s�quencment */
	private Date dateRealisation = new Date();
	private Time heureRealisation = new Time();

	/** Nom de l'op�rateur */
	private String nomPrenomOperateur[] = null;

	/** Colonnes utilis�s pour les requ�tes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION };

	private String columnsBOM[] = new String[] { BOM._id, BOM.NUMERO_DEBIT,
			BOM.NUMERO_COMPOSANT, BOM.NUMERO_OPERATION, BOM.REFERENCE_INTERNE,
			BOM.REPERE_ELECTRIQUE_TENANT, BOM.REFERENCE_FABRICANT2,
			BOM.NUMERO_LOT_SCANNE, BOM.REFERENCE_FABRICANT_SCANNE };
	private String columnsKitting[] = new String[] { Kitting.NUMERO_FIL_CABLE,
			Kitting.NUMERO_POSITION_CHARIOT, Kitting.REPERE_ELECTRIQUE,
			Kitting.NUMERO_COMPOSANT, Kitting.ORDRE_REALISATION,
			Kitting.LONGUEUR_FIL_CABLE, Kitting.UNITE, Kitting.NUMERO_DEBIT,
			Kitting._id, Kitting.NUMERO_OPERATION, Kitting.NUMERO_LOT_SCANNE,
			Kitting.REFERENCE_FABRICANT_SCANNE };

	/** Curseur et Content Resolver � utiliser lors des requ�tes */
	private Cursor cursor, cursorA, cursorB;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation;

	/** Tableau des infos produit */
	private String labels[];

	private String colInfo[] = new String[] { Raccordement._id,
			Raccordement.DESIGNATION, Raccordement.NUMERO_REVISION_HARNAIS,
			Raccordement.STANDARD, Raccordement.NUMERO_HARNAIS_FAISCEAUX,
			Raccordement.REFERENCE_FICHIER_SOURCE };
	private Cursor cursorInfo;

	/** Elements � r�cuperer de la vue */
	private ImageButton infoProduit, exitButton, boutonCheck;
	private EditText referenceArticle, numeroLot;
	private TextView operation;
	private Button scan, scan1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_saisie_tracabilite_composant);

		// R�cup�ration des �l�ments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();

		// R�cuperation des �l�ments de la vue
		referenceArticle = (EditText) findViewById(R.id.editText1);
		numeroLot = (EditText) findViewById(R.id.editText2);
		operation = (TextView) findViewById(R.id.textView3);

		// R�cuperation du num�ro d'op�ration courant
		clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
		cursor = cr.query(urlSeq, columnsSeq, clause, null, null);
		if (cursor.moveToFirst()) {
			operation.setText(cursor.getString(cursor
					.getColumnIndex(Operation.RANG_1_1)));
			numeroOperation = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
		}

		// R�cup�ration du num�ro de d�bit
		clause = new String(BOM.NUMERO_OPERATION + "='" + numeroOperation + "'");
		cursorA = cr.query(urlBOM, columnsBOM, clause, null, null);
		if (cursorA.moveToFirst()) {
			numeroDebit = cursorA.getInt(cursorA
					.getColumnIndex(BOM.NUMERO_DEBIT));
		} else {
			Toast.makeText(this, "Debit non trouv�e", Toast.LENGTH_LONG).show();
		}

		contact = new ContentValues();

		// Validation de la saisie
		boutonCheck = (ImageButton) findViewById(R.id.imageButton1);
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean fin = false;
				// V�rification des champs remplis
				if ((numeroLot.getText().length() == 0)
						|| (referenceArticle.getText().length() == 0)) {
					Toast.makeText(SaisieTracabiliteComposant.this,
							"Veuillez saisir les champs manquants ",
							Toast.LENGTH_LONG).show();
				} else {
					/*
					 * // MAJ de la table BOM contact.put(BOM.NUMERO_LOT_SCANNE,
					 * numeroLot.getText() .toString());
					 * contact.put(BOM.REFERENCE_FABRICANT_SCANNE,
					 * referenceArticle.getText().toString()); cr.update(urlBOM,
					 * contact, BOM.NUMERO_DEBIT + " = ?", new String[] {
					 * Integer.toString(numeroDebit) }); contact.clear();
					 */

					// MAJ du fichier
					// Cr�ation de la feuille et du Workbook
					try {

						String nomFichier = null;
						File data = new File(Environment
								.getExternalStorageDirectory()
								.getAbsolutePath()
								+ "/Android/data/com.inodex.inoprod/"
								+ "kittingTetes.xls");

						InputStream input = new FileInputStream(data);

						// Interpretation du fichier a l'aide de Apache POI
						POIFSFileSystem fs = new POIFSFileSystem(input);
						HSSFWorkbook wb = new HSSFWorkbook(fs);
						HSSFSheet sheet = wb.getSheetAt(0);

						ContentValues contact = new ContentValues();
						Iterator rows = sheet.rowIterator();

						HSSFRow row = (HSSFRow) rows.next();
						HashMap<String, Integer> colonnes = new HashMap<String, Integer>();
						Log.e("Num colon", "" + row.getLastCellNum());
						for (int k = 0; k < row.getLastCellNum(); k++) {
							try {
								colonnes.put(row.getCell(k).toString(), k);
							} catch (NullPointerException e) {
								Log.e("err0", " " + e);
							}

						}

						while (rows.hasNext()) {
							row = (HSSFRow) rows.next();
							if (Integer.parseInt(row.getCell(
									colonnes.get(Kitting.NUMERO_DEBIT))
									.toString()) == numeroDebit) {
								rowId.add(row.getRowNum());
							}
						}
						rowIt = rowId.iterator();
						Log.d("Nombre ligne", rowId.size() + "");

						Log.e("Num colon", "Colonnes finis");
						while (rowIt.hasNext()) {
							row = (HSSFRow) sheet.getRow(rowIt.next());

							try {
								row.getCell(colonnes.get(BOM.NUMERO_LOT_SCANNE))
										.setCellValue(
												numeroLot.getText().toString());
								String ref = row.getCell(
										colonnes.get(BOM.REFERENCE_FABRICANT2))
										.toString();
								fin = ref.equals(referenceArticle.getText()
										.toString());
								if (fin) {
									row.getCell(
											colonnes.get(BOM.REFERENCE_FABRICANT_SCANNE))
											.setCellValue(
													referenceArticle.getText()
															.toString());
								}

							} catch (Exception e) {
								Log.e("err1", " " + e);
							}

						}
						if (!(fin)) {
							Toast.makeText(SaisieTracabiliteComposant.this,
									"La r�f�rence scann� ne correspond pas",
									Toast.LENGTH_SHORT).show();
						}

						Log.e("Num colon", "D�but �criture");
						FileOutputStream fileOut;
						File debit = new File(Environment
								.getExternalStorageDirectory()
								.getAbsolutePath()
								+ "/Android/data/com.inodex.inoprod/"
								+ "kittingTetes.xls");

						fileOut = new FileOutputStream(debit);

						wb.write(fileOut);

						fileOut.close();
						input.close();
					} catch (Exception e) {
						Log.e("err2", " " + e);
					}

					// Mise � jour des tables depuis les fichiers
					/*
					 * try { String nomFichier = null; File data = new
					 * File(Environment.getDataDirectory().getAbsolutePath() +
					 * "/data/com.inodex.inoprod/");
					 * 
					 * for (String file : data.list()) { Log.d("Nom Fichier",
					 * file); if (file.contains("Cables")) { nomFichier = file;
					 * Log.e("Nom Fichier", nomFichier); } }
					 * 
					 * InputStream input = new
					 * FileInputStream(Environment.getDataDirectory()
					 * .getAbsolutePath() + "/data/com.inodex.inoprod/" +
					 * nomFichier); // Interpretation du fichier a l'aide de
					 * Apache POI POIFSFileSystem fs = new
					 * POIFSFileSystem(input); HSSFWorkbook wb = new
					 * HSSFWorkbook(fs); HSSFSheet sheet = wb.getSheet("D�bit");
					 * ContentValues contact = new ContentValues(); Iterator
					 * rows = sheet.rowIterator();
					 * 
					 * HSSFRow row = (HSSFRow) rows.next(); HashMap<String,
					 * Integer> colonnes = new HashMap<String, Integer>(); for
					 * (int k = 0; k < row.getLastCellNum(); k++) {
					 * colonnes.put(row.getCell(k).toString(), k);
					 * 
					 * } // Stockage des indices des colonnes int indice = 1;
					 * while (rows.hasNext()) { row = (HSSFRow) rows.next(); try
					 * { contact.put( Kitting.NUMERO_LOT_SCANNE,
					 * row.getCell(colonnes.get(Kitting.NUMERO_LOT_SCANNE))
					 * .toString()); } catch (NullPointerException e) { } try {
					 * contact.put( Kitting.REFERENCE_FABRICANT_SCANNE,
					 * row.getCell(
					 * colonnes.get(Kitting.REFERENCE_FABRICANT_SCANNE))
					 * .toString()); } catch (NullPointerException e) { }
					 * 
					 * // Ajout de l'entit�
					 * getContentResolver().update(urlKitting, contact,
					 * Kitting._id + "='" + indice + "'", null); // Ecrasement
					 * de ses donn�es pour passer � la suivante contact.clear();
					 * indice++; } } catch (Exception e) { Log.e("err3", " " +
					 * e); }
					 */

					// Ecran suivant
					Intent toNext = new Intent(SaisieTracabiliteComposant.this,
							ServitudeComposants.class);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("opId", opId);
					toNext.putExtra("Indice", indiceCourant);
					if (fin) {
						startActivity(toNext);
						finish();
					}
				}

			}

		});

		// Suppression de la saisie
		exitButton = (ImageButton) findViewById(R.id.exitButton1);
		exitButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				referenceArticle.setText("");
				numeroLot.setText("");
			}
		});

		scan = (Button) findViewById(R.id.scan);
		scan1 = (Button) findViewById(R.id.scan1);

		scan.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent(
							"com.google.zxing.client.android.SCAN");
					intent.setPackage("com.google.zxing.client.android");
					intent.putExtra(
							"com.google.zxing.client.android.SCAN.SCAN_MODE",
							"QR_CODE_MODE");
					startActivityForResult(intent, 0);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(
							SaisieTracabiliteComposant.this,
							"Impossible de trouver une application pour le scan ",
							Toast.LENGTH_LONG).show();
				}

			}
		});

		scan1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent(
							"com.google.zxing.client.android.SCAN");
					intent.setPackage("com.google.zxing.client.android");
					intent.putExtra(
							"com.google.zxing.client.android.SCAN.SCAN_MODE",
							"QR_CODE_MODE");
					startActivityForResult(intent, 0);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(
							SaisieTracabiliteComposant.this,
							"Impossible de trouver une application pour le scan ",
							Toast.LENGTH_LONG).show();
				}

			}
		});

		// Info Produit
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);
		infoProduit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cursorInfo = cr.query(urlRac, colInfo, null, null, null);
				Intent toInfo = new Intent(SaisieTracabiliteComposant.this,
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

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}

	/**
	 * R�cup�ration du code barre scann�
	 * 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				if (numeroLot.getText().equals("")) {
					numeroLot.setText(contents);
				} else {
					referenceArticle.setText(contents);
				}

			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(SaisieTracabiliteComposant.this,
						"Impossible de trouver une application pour le scan ",
						Toast.LENGTH_LONG).show();
			}

		}

	}
}
