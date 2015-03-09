package com.inodex.inoprod.activities.controleur;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.activities.Inoprod;
import com.inodex.inoprod.activities.cableur.MainMenuCableur;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class DataPack extends Activity {

	/** Bouton qui permet de revenir au menu principal */
	private ImageButton boutonExit = null;
	private Button edition;

	private Uri urlRac = RaccordementProvider.CONTENT_URI;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	/** Colonnes utilisés pour les requêtes */
	private String colSeq[] = new String[] { Operation._id, Operation.GAMME,
			Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.DUREE_MESUREE, Operation.RANG_1_1_1 , Operation.SECONDE_DUREE_MESUREE};
	private Uri urlSeq = SequencementProvider.CONTENT_URI;

	private String colInfo[] = new String[] { Raccordement._id,
			Raccordement.DESIGNATION, Raccordement.NUMERO_REVISION_HARNAIS,
			Raccordement.STANDARD, Raccordement.NUMERO_HARNAIS_FAISCEAUX,
			Raccordement.REFERENCE_FICHIER_SOURCE };
	private Cursor cursorInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_pack);
		cr = getContentResolver();

		// Edition du datapack
		edition = (Button) findViewById(R.id.button1);
		edition.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Création de la feuille et du Workbook
				HSSFWorkbook wb = new HSSFWorkbook();
				HSSFSheet sheet = wb.createSheet("Débit");

				short indiceLigne = 0;
				short indiceColonne = 0;

				// En tête
				HSSFRow row = sheet.createRow(indiceLigne++);
				row.createCell(indiceColonne).setCellValue("Data Pack");

				// Info Produit
				indiceColonne = 0;
				row = sheet.createRow(indiceLigne++);
				row = sheet.createRow(indiceLigne++);
				row.createCell(indiceColonne++).setCellValue("Info Produit");
				row = sheet.createRow(indiceLigne++);
				cursor = cr.query(urlRac, colInfo, null, null, null);
				if (cursor.moveToFirst()) {
					row.createCell(indiceColonne++).setCellValue(
							cursor.getString(cursor
									.getColumnIndex(Raccordement.DESIGNATION)));
					row.createCell(indiceColonne++)
							.setCellValue(
									cursor.getString(cursor
											.getColumnIndex(Raccordement.NUMERO_REVISION_HARNAIS)));
					row.createCell(indiceColonne++).setCellValue(
							cursor.getString(cursor
									.getColumnIndex(Raccordement.STANDARD)));
					row.createCell(indiceColonne++)
							.setCellValue(
									cursor.getString(cursor
											.getColumnIndex(Raccordement.NUMERO_HARNAIS_FAISCEAUX)));
					row.createCell(indiceColonne++)
							.setCellValue(
									cursor.getString(cursor
											.getColumnIndex(Raccordement.REFERENCE_FICHIER_SOURCE)));
				}

				// Sommaire
				indiceColonne = 0;
				row = sheet.createRow(indiceLigne++);
				row = sheet.createRow(indiceLigne++);
				row.createCell(indiceColonne).setCellValue("Sommaire");

				// Infos par connecteur

				cursor = cr.query(urlSeq, colSeq, Operation.RANG_1_1 + 
					" LIKE 'Connecteur%' GROUP BY "
						+ Operation.RANG_1_1, null, null);
				if (cursor.moveToFirst()) {
					do {
						indiceColonne = 0;
						row = sheet.createRow(indiceLigne++);
						String numeroCo = cursor.getString(cursor
								.getColumnIndex(Operation.RANG_1_1));
						row.createCell(indiceColonne++).setCellValue(numeroCo);// A
																				// remplacer
																				// par
																				// repere
						cursorA = cr.query(urlSeq, colSeq, Operation.RANG_1_1
								+ "='" + numeroCo + "'", null, Operation._id);
						if (cursorA.moveToFirst()) {
							//En têtes
							indiceColonne = 0;
							row = sheet.createRow(indiceLigne++);
							row.createCell(indiceColonne++)
							.setCellValue("Description opération");
							row.createCell(indiceColonne++)
							.setCellValue("Date réalisation");
							row.createCell(indiceColonne++)
							.setCellValue("Durée mesurée");
							row.createCell(indiceColonne++)
							.setCellValue("Seconde durée mesurée");
							row.createCell(indiceColonne++)
							.setCellValue("Nom opérateur");
							
							do {
								indiceColonne = 0;
								row = sheet.createRow(indiceLigne++);
								row.createCell(indiceColonne++).setCellValue(
										cursorA.getString(cursorA
												.getColumnIndex(Operation.DESCRIPTION_OPERATION)));
								row.createCell(indiceColonne++).setCellValue(
										cursorA.getString(cursorA
												.getColumnIndex(Operation.DATE_REALISATION)));
								row.createCell(indiceColonne++).setCellValue(
										cursorA.getString(cursorA
												.getColumnIndex(Operation.DUREE_MESUREE)));
								row.createCell(indiceColonne++).setCellValue(
										cursorA.getString(cursorA
												.getColumnIndex(Operation.SECONDE_DUREE_MESUREE)));
								row.createCell(indiceColonne++).setCellValue(
										cursorA.getString(cursorA
												.getColumnIndex(Operation.NOM_OPERATEUR)));
								
								
								
								
							} while (cursorA.moveToNext());
							
							
						}

					} while (cursor.moveToNext());
					
					try {
						FileOutputStream fileOut;
						File debit = new File(Environment.getDataDirectory()
								.getAbsolutePath() + "/data/com.inodex.inoprod/",
								"dataPack.xls");

						fileOut = new FileOutputStream(debit);

						wb.write(fileOut);

						fileOut.close();
					} catch (Exception e) {
						Log.e("DataPack", " " + e);
					}


				}
			}
		});

		// Retour menu principal
		boutonExit = (ImageButton) findViewById(R.id.exitButton1);
		boutonExit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						DataPack.this);
				builder.setMessage("Êtes-vous sur de vouloir quitter le profil ?");
				builder.setCancelable(false);
				builder.setPositiveButton("Oui",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								Intent toMain = new Intent(DataPack.this,
										Inoprod.class);
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
	}
}
