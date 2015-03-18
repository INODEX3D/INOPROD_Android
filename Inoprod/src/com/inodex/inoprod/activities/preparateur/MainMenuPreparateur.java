package com.inodex.inoprod.activities.preparateur;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.inodex.inoprod.R;
import com.inodex.inoprod.activities.Inoprod;
import com.inodex.inoprod.business.BOMProvider;
import com.inodex.inoprod.business.ChariotProvider;
import com.inodex.inoprod.business.CheminementProvider;
import com.inodex.inoprod.business.Durees.Duree;
import com.inodex.inoprod.business.DureesProvider;
import com.inodex.inoprod.business.KittingProvider;
import com.inodex.inoprod.business.Nomenclature.Cable;
import com.inodex.inoprod.business.NomenclatureProvider;
import com.inodex.inoprod.business.Outillage.Outil;
import com.inodex.inoprod.business.OutillageProvider;
import com.inodex.inoprod.business.Production.Fil;
import com.inodex.inoprod.business.ProductionProvider;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.SupportProvider;
import com.inodex.inoprod.business.TableBOM.BOM;
import com.inodex.inoprod.business.TableChariots.Chariot;
import com.inodex.inoprod.business.TableCheminement.Cheminement;
import com.inodex.inoprod.business.TableKittingCable.Kitting;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;
import com.inodex.inoprod.business.TableSupport.Support;
import com.inodex.inoprod.business.TimeConverter;

/**
 * Menu principal du profil préparateur. Il gère l'import des données sources
 * issues des bases de donnes production et nomenclature. Après l'import, les
 * tables qui en dérivent sont générées. Deux autres boutons servent à la
 * génération des fichiers débit cables et kitting têtes. ATTENTION: Des appuis
 * répétées sur le bouton d'import importeront plusieurs fois les mêmes éléments
 * dans le bases de données. Une meilleure gestion de la création/importation de
 * ces bases est à réaliser.
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */

public class MainMenuPreparateur extends Activity {

	/** Durées théoriques en seconde à affecter aux opérations */
	private long FRETTAGE = 30;
	private long DEBIT_CABLES = 30;
	private long ENFICHAGE_FC_OBTURATEURS = 30;
	private long SERTISSAGE_CONTACTS = 150;
	private long REPRISE_BLINDAGE = 300;
	private long RETENTION_CONTACT = 10;
	private long POSITIONNEMENT_SUPPORT = 20;
	private long KIT_TETE = 180;

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Elements à récuperer de la vue */
	private ImageButton boutonExit = null;
	private Button boutonImport = null;
	private Button boutonDebit = null;
	private Button boutonKitting = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA, cursorB;
	private ContentResolver cr;
	boolean finImport = false;
	/** Uri à manipuler */
	private Uri urlNomenclature = NomenclatureProvider.CONTENT_URI;
	private Uri urlProduction = ProductionProvider.CONTENT_URI;
	private Uri urlDurees = DureesProvider.CONTENT_URI;
	private Uri urlRaccordement = RaccordementProvider.CONTENT_URI;
	private Uri urlSequencement = SequencementProvider.CONTENT_URI;
	private Uri urlKitting = KittingProvider.CONTENT_URI;
	private Uri urlBOM = BOMProvider.CONTENT_URI;
	private Uri urlCheminement = CheminementProvider.CONTENT_URI;
	private Uri urlOutillage = OutillageProvider.CONTENT_URI;
	private Uri urlSupport = SupportProvider.CONTENT_URI;
	private Uri urlChariot = ChariotProvider.CONTENT_URI;

	/** Colonnes utilisés pour les requêtes */

	private String colProd1[] = new String[] { Fil.REPERE_ELECTRIQUE_TENANT,
			Fil.REPERE_ELECTRIQUE_ABOUTISSANT,
			Fil.NUMERO_COMPOSANT_ABOUTISSANT, Fil.NUMERO_COMPOSANT_TENANT,
			Fil.ORDRE_REALISATION, Fil.ETAT_LIAISON_FIL, Fil.NORME_CABLE,
			Fil.NUMERO_REVISION_FIL, Fil._id, Fil.NUMERO_FIL_CABLE,
			Fil.TYPE_FIL_CABLE, Fil.LONGUEUR_FIL_CABLE, Fil.ZONE_ACTIVITE };

	private String colNom1[] = new String[] { Cable.DESIGNATION_COMPOSANT,
			Cable.UNITE, Cable.REFERENCE_FABRICANT1,
			Cable.REFERENCE_FABRICANT2, Cable.REFERENCE_INTERNE,
			Cable.FOURNISSEUR_FABRICANT, Cable.NORME_CABLE, Cable._id,
			Cable.FAMILLE_PRODUIT, Cable.NUMERO_COMPOSANT };

	private String colTim[] = new String[] { Duree._id,
			Duree.DESIGNATION_OPERATION, Duree.DUREE_THEORIQUE };

	private String ColChem1[] = new String[] { Cheminement._id,
			Cheminement.NUMERO_COMPOSANT_TENANT,
			Cheminement.NUMERO_SECTION_CHEMINEMENT,
			Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT,
			Cheminement.REPERE_ELECTRIQUE_TENANT, Cheminement.ZONE_ACTIVITE,
			Cheminement.LOCALISATION1,
			Cheminement.NUMERO_COMPOSANT_ABOUTISSANT,
			Cheminement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Cheminement.NUMERO_FIL_CABLE, Cheminement.TYPE_FIL_CABLE,
			Cheminement.ORDRE_REALISATION, Cheminement.UNITE,
			Cheminement.LONGUEUR_FIL_CABLE, Cheminement.NUMERO_CHEMINEMNT };

	private String ColProd2[] = new String[] { Fil.ETAT_LIAISON_FIL,
			Fil.NUMERO_REVISION_FIL, Fil.FIL_SENSIBLE, Fil.NUMERO_FIL_CABLE,
			Fil.TYPE_FIL_CABLE, Fil.NUMERO_FIL_DANS_CABLE,
			Fil.LONGUEUR_FIL_CABLE, Fil.COULEUR_FIL, Fil.NOM_SIGNAL,
			Fil.ORDRE_REALISATION, Fil.REPERE_ELECTRIQUE_TENANT,
			Fil.NUMERO_COMPOSANT_TENANT, Fil.NUMERO_BORNE_TENANT,
			Fil.TYPE_RACCORDEMENT_TENANT, Fil.REPRISE_BLINDAGE,
			Fil.SANS_REPRISE_BLINDAGE, Fil.REPERE_ELECTRIQUE_ABOUTISSANT,
			Fil.NUMERO_COMPOSANT_ABOUTISSANT, Fil.NUMERO_BORNE_ABOUTISSANT,
			Fil.TYPE_RACCORDEMENT_ABOUTISSANT, Fil.TYPE_ELEMENT_RACCORDE,
			Fil.REFERENCE_FABRICANT2, Fil.REFERENCE_INTERNE,
			Fil.FICHE_INSTRUCTION, Fil.REFERENCE_CONFIGURATION_SERTISSAGE,
			Fil._id, Fil.REFERENCE_OUTIL_TENANT,
			Fil.REFERENCE_ACCESSOIRE_OUTIL_TENANT, Fil.REGLAGE_OUTIL_TENANT,
			Fil.REFERENCE_OUTIL_ABOUTISSANT,
			Fil.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT,
			Fil.REGLAGE_OUTIL_ABOUTISSANT, Fil.OBTURATEUR, Fil.FAUX_CONTACT,
			Fil.ETAT_FINALISATION_PRISE, Fil.ORIENTATION_RACCORD_ARRIERE,
			Fil.ZONE_ACTIVITE, Fil._id, Fil.DESIGNATION_PRODUIT,
			Fil.ACCESSOIRE_COMPOSANT1, Fil.ACCESSOIRE_COMPOSANT2,
			Fil.DESIGNATION_PRODUIT, Fil.NUMERO_REVISION_HARNAIS, Fil.STANDARD,
			Fil.NUMERO_HARNAIS_FAISCEAUX, Fil.REFERENCE_FICHIER_SOURCE,
			Fil.LOCALISATION2, Fil.ZONE_ACTIVITE, Fil.LOCALISATION1 };

	private String colNom3[] = new String[] { Cable.NORME_CABLE,
			Cable.EQUIPEMENT, Cable._id, Cable.NUMERO_COMPOSANT,
			Cable.REPERE_ELECTRIQUE, Cable.ACCESSOIRE_CABLAGE,
			Cable.ACCESSOIRE_COMPOSANT, Cable.DESIGNATION_COMPOSANT,
			Cable.REFERENCE_FABRICANT2, Cable.FAMILLE_PRODUIT,
			Cable.FOURNISSEUR_FABRICANT, Cable.REFERENCE_INTERNE,
			Cable.REFERENCE_IMPOSEE, Cable.QUANTITE, Cable.UNITE };

	private String colProd3[] = new String[] { Fil.ORDRE_REALISATION, Fil._id,
			Fil.NORME_CABLE };

	private String colBOM3[] = new String[] { BOM._id, BOM.NUMERO_CHEMINEMENT,
			BOM.ORDRE_REALISATION, BOM.NUMERO_COMPOSANT,
			BOM.DESIGNATION_COMPOSANT, BOM.REPERE_ELECTRIQUE_TENANT,
			BOM.NUMERO_POSITION_CHARIOT };

	private String colKit3[] = new String[] { Kitting._id,
			Kitting.NUMERO_POSITION_CHARIOT, Kitting.NUMERO_CHEMINEMENT,
			Kitting.REPERE_ELECTRIQUE, Kitting.NUMERO_COMPOSANT,
			Kitting.ORDRE_REALISATION };

	private String colKit4[] = new String[] { Kitting._id,
			Kitting.NUMERO_POSITION_CHARIOT, Kitting.NORME_CABLE };

	private String colOut[] = new String[] { Outil._id, Outil.AFFECTATION,
			Outil.CODE_BARRE, Outil.COMMENTAIRES, Outil.CONSTRUCTEUR,
			Outil.DERNIERE_OPERATION, Outil.IDENTIFICATION, Outil.INTITULE,
			Outil.NUMERO_SERIE, Outil.PERIODE, Outil.PROCHAINE_OPERATION,
			Outil.PROPRIETAIRE, Outil.SECTION, Outil.TYPE, Outil.UNITE };

	private String colSup[] = new String[] { Support._id, Support.AFFECTATION,
			Support.CODE_TAG, Support.DIAMETRE_ADMISSIBLE,
			Support.NUMERO_SERIE, Support.TYPE_SUPPORT };

	private String colCha[] = new String[] { Chariot._id, Chariot.CODE_TAG,
			Chariot.CONNECTEUR_POSITIONNE, Chariot.FACE_CHARIOT,
			Chariot.NUMERO_CHARIOT, Chariot.POSITION_NUMERO };

	private String colRac[] = new String[] { Raccordement._id,
			Raccordement.NUMERO_BORNE_TENANT,
			Raccordement.NUMERO_BORNE_ABOUTISSANT,
			Raccordement.NUMERO_CHEMINEMENT,
			Raccordement.NUMERO_SECTION_CHEMINEMENT,
			Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.ORDRE_REALISATION, Raccordement.FAUX_CONTACT,
			Raccordement.OBTURATEUR, Raccordement.REPRISE_BLINDAGE,
			Raccordement.SANS_REPRISE_BLINDAGE, Raccordement.FIL_SENSIBLE,
			Raccordement.ZONE_ACTIVITE,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT, Raccordement.FIL_SENSIBLE,
			Raccordement.REFERENCE_FABRICANT2, Raccordement.NUMERO_FIL_CABLE,
			Raccordement.LOCALISATION1, Raccordement.TYPE_ELEMENT_RACCORDE };

	private String clause, rep, norme, numeroOperation, num, gamme, rang,
			rang_1, descriptionOperation, num1, referenceCourante,
			numeroChariot, numeroComposant;
	private int debit, indice, chemin, numeroCheminement, indiceChariotA,
			indiceChariotB, indiceFrettage, indiceCheminement,
			indiceControleJalons, indiceControleCheminement,
			indiceControleFinal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu_preparateur);
		cr = getContentResolver();

		// Récupération du nom de l'opérateur
		Intent i = getIntent();
		nomPrenomOperateur = i.getStringArrayExtra("Noms");

		// Retour menu principal
		boutonExit = (ImageButton) findViewById(R.id.exitButton1);
		boutonExit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MainMenuPreparateur.this);
				builder.setMessage("Êtes-vous sur de vouloir quitter le profil ?");
				builder.setCancelable(false);
				builder.setPositiveButton("Oui",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								Intent toMain = new Intent(
										MainMenuPreparateur.this, Inoprod.class);
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

		// Import des données et créations des tables
		boutonImport = (Button) findViewById(R.id.button1);
		boutonImport.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				boolean importReussi = importSources();
				if (importReussi) {
					try {
						creationTables();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					generationTableSequencement();
					finImport = true;
				}

			}

		});

		// Génération du fichier débit cables
		boutonDebit = (Button) findViewById(R.id.button2);
		boutonDebit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean reussi;
				try {
					reussi = genererDebitCable();
					Toast.makeText(MainMenuPreparateur.this,
							"Fichier débit cables crée", Toast.LENGTH_SHORT)
							.show();
				} catch (IOException e) {
					Log.e("Debit", "fichier non lu");
				}

			}

		});

		// Génération du fichier kitting têtes
		boutonKitting = (Button) findViewById(R.id.button3);
		boutonKitting.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean reussi;
				try {
					reussi = genererKittingTetes();
					Toast.makeText(MainMenuPreparateur.this,
							"Fichier kittin têtes crée", Toast.LENGTH_SHORT)
							.show();
				} catch (IOException e) {
					Log.e("Debit", "fichier non lu");
				}

			}

		});

	}

	/**
	 * Création des tables Cheminement, BOM, Raccordement et Kitting à l'issu de
	 * l'import des bases de données sources
	 * 
	 */
	protected void creationTables() throws IOException {
		// Suppression des bd existantes
		getContentResolver().delete(urlCheminement,
				Cheminement._id + "!='null'", null);
		getContentResolver().delete(urlRaccordement,
				Raccordement._id + "!='null'", null);
		getContentResolver().delete(urlKitting, Kitting._id + "!='null'", null);
		getContentResolver().delete(urlBOM, BOM._id + "!='null'", null);

		ContentValues contact = new ContentValues();

		indiceChariotA = 1;
		indiceChariotB = 1;
		int numeroSection = 1;

		// Création de la table de cheminement
		// Tenant
		clause = Fil.NUMERO_COMPOSANT_TENANT + "!='null' GROUP BY "
				+ Fil.NUMERO_COMPOSANT_TENANT;
		cursor = cr.query(urlProduction, ColProd2, clause, null, Fil._id);
		if (cursor.moveToFirst()) {
			do {
				String numeroCo = cursor.getString(cursor
						.getColumnIndex(Fil.NUMERO_COMPOSANT_TENANT));
				clause = Fil.NUMERO_COMPOSANT_TENANT + " LIKE '" + numeroCo
						+ "' GROUP BY " + Fil.NUMERO_FIL_CABLE;
				cursorA = cr.query(urlProduction, ColProd2, clause, null,
						Fil._id);
				if (cursorA.moveToFirst()) {
					do {
						contact.put(Cheminement.LOCALISATION1, cursorA
								.getString(cursorA
										.getColumnIndex(Fil.LOCALISATION1)));
						contact.put(
								Cheminement.LONGUEUR_FIL_CABLE,
								cursorA.getString(cursorA
										.getColumnIndex(Fil.LONGUEUR_FIL_CABLE)));
						contact.put(Cheminement.NUMERO_COMPOSANT_TENANT,
								numeroCo);
						contact.put(Cheminement.NUMERO_FIL_CABLE, cursorA
								.getString(cursorA
										.getColumnIndex(Fil.NUMERO_FIL_CABLE)));
						contact.put(
								Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT,
								cursorA.getString(cursorA
										.getColumnIndex(Fil.LOCALISATION2)));
						contact.put(Cheminement.NUMERO_SECTION_CHEMINEMENT,
								numeroSection);
						contact.put(Cheminement.ORDRE_REALISATION, cursorA
								.getString(cursorA
										.getColumnIndex(Fil.ORDRE_REALISATION)));
						contact.put(
								Cheminement.REPERE_ELECTRIQUE_TENANT,
								cursorA.getString(cursorA
										.getColumnIndex(Fil.REPERE_ELECTRIQUE_TENANT)));
						contact.put(Cheminement.TYPE_FIL_CABLE, cursorA
								.getString(cursorA
										.getColumnIndex(Fil.TYPE_FIL_CABLE)));
						contact.put(Cheminement.ZONE_ACTIVITE, cursorA
								.getString(cursorA
										.getColumnIndex(Fil.ZONE_ACTIVITE)));
						// Ajout de l'entité
						getContentResolver().insert(urlCheminement, contact);
						// Ecrasement de ses données pour passer à la suivante
						contact.clear();

					} while (cursorA.moveToNext());
				}
				numeroSection++;
			} while (cursor.moveToNext());
		}

		// Aboutissant
		clause = Fil.NUMERO_COMPOSANT_ABOUTISSANT + "!='null' GROUP BY "
				+ Fil.NUMERO_COMPOSANT_ABOUTISSANT;
		cursor = cr.query(urlProduction, ColProd2, clause, null, Fil._id);
		if (cursor.moveToFirst()) {
			do {
				String numeroCo = cursor.getString(cursor
						.getColumnIndex(Fil.NUMERO_COMPOSANT_ABOUTISSANT));
				clause = Fil.NUMERO_COMPOSANT_ABOUTISSANT + " LIKE '"
						+ numeroCo + "' GROUP BY " + Fil.NUMERO_FIL_CABLE;
				cursorA = cr.query(urlProduction, ColProd2, clause, null,
						Fil._id);
				if (cursorA.moveToFirst()) {
					do {
						contact.put(Cheminement.LOCALISATION1, cursorA
								.getString(cursorA
										.getColumnIndex(Fil.LOCALISATION1)));
						contact.put(
								Cheminement.LONGUEUR_FIL_CABLE,
								cursorA.getString(cursorA
										.getColumnIndex(Fil.LONGUEUR_FIL_CABLE)));
						contact.put(Cheminement.NUMERO_COMPOSANT_ABOUTISSANT,
								numeroCo);
						contact.put(Cheminement.NUMERO_FIL_CABLE, cursorA
								.getString(cursorA
										.getColumnIndex(Fil.NUMERO_FIL_CABLE)));
						contact.put(
								Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT,
								cursorA.getString(cursorA
										.getColumnIndex(Fil.LOCALISATION2)));
						contact.put(Cheminement.NUMERO_SECTION_CHEMINEMENT,
								numeroSection);
						contact.put(Cheminement.ORDRE_REALISATION, cursorA
								.getString(cursorA
										.getColumnIndex(Fil.ORDRE_REALISATION)));
						contact.put(
								Cheminement.REPERE_ELECTRIQUE_ABOUTISSANT,
								cursorA.getString(cursorA
										.getColumnIndex(Fil.REPERE_ELECTRIQUE_ABOUTISSANT)));
						contact.put(Cheminement.TYPE_FIL_CABLE, cursorA
								.getString(cursorA
										.getColumnIndex(Fil.TYPE_FIL_CABLE)));
						contact.put(Cheminement.ZONE_ACTIVITE, cursorA
								.getString(cursorA
										.getColumnIndex(Fil.ZONE_ACTIVITE)));
						// Ajout de l'entité
						getContentResolver().insert(urlCheminement, contact);
						// Ecrasement de ses données pour passer à la suivante
						contact.clear();

					} while (cursorA.moveToNext());
				}
				numeroSection++;
			} while (cursor.moveToNext());
		}

		/*
		 * String nomFichier = null; File data = new
		 * File(Environment.getDataDirectory().getAbsolutePath() +
		 * "/data/com.inodex.inoprod/");
		 * 
		 * for (String file : data.list()) { Log.d("Nom Fichier", file); if
		 * (file.contains("cheminement")) { nomFichier = file;
		 * Log.e("Nom Fichier", nomFichier); } }
		 * 
		 * InputStream input = new
		 * FileInputStream(Environment.getDataDirectory() .getAbsolutePath() +
		 * "/data/com.inodex.inoprod/" + nomFichier); // Interpretation du
		 * fichier a l'aide de Apache POI POIFSFileSystem fs = new
		 * POIFSFileSystem(input); HSSFWorkbook wb = new HSSFWorkbook(fs);
		 * 
		 * // Feuille outillages HSSFSheet sheet = wb.getSheetAt(0);
		 * 
		 * // Iteration sur chacune des lignes du fichier Iterator rows =
		 * sheet.rowIterator(); rows.next(); HSSFRow row = (HSSFRow)
		 * rows.next(); HashMap<String, Integer> colonnes = new HashMap<String,
		 * Integer>(); // Stockage des indices des colonnes for (int i = 0; i <
		 * row.getLastCellNum(); i++) { colonnes.put(row.getCell(i).toString(),
		 * i);
		 * 
		 * }
		 * 
		 * while (rows.hasNext()) {
		 * 
		 * try { contact.put( Cheminement.NUMERO_SECTION_CHEMINEMENT,
		 * row.getCell( colonnes.get("Numéro de section de cheminement"))
		 * .toString()); } catch (NullPointerException e) { } try {
		 * contact.put(Cheminement.LOCALISATION1,
		 * row.getCell(colonnes.get("Localisation_1 (68)")) .toString()); }
		 * catch (NullPointerException e) { } try { contact.put(
		 * Cheminement.NUMERO_COMPOSANT, row.getCell(
		 * colonnes.get("Numéro de composant (9 & 12)")) .toString()); } catch
		 * (NullPointerException e) { } try {
		 * contact.put(Cheminement.ORDRE_REALISATION,
		 * row.getCell(colonnes.get("Ordre de réalisation (24)")) .toString());
		 * } catch (NullPointerException e) { } try {
		 * contact.put(Cheminement.ZONE_ACTIVITE,
		 * row.getCell(colonnes.get("Zone-activité (67)")) .toString()); } catch
		 * (NullPointerException e) { } try { contact.put(
		 * Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT, row.getCell(
		 * colonnes.get("N° de repère de la table de cheminement"))
		 * .toString()); } catch (NullPointerException e) { } try {
		 * contact.put(Cheminement.TYPE_SUPPORT,
		 * row.getCell(colonnes.get("Type de support")).toString()); } catch
		 * (NullPointerException e) { } try {
		 * contact.put(Cheminement.REPERE_ELECTRIQUE,
		 * row.getCell(colonnes.get("Repère electrique (8 & 11)")) .toString());
		 * } catch (NullPointerException e) { }
		 * 
		 * // Ajout de l'entité getContentResolver().insert(urlCheminement,
		 * contact); // Ecrasement de ses données pour passer à la suivante
		 * contact.clear(); row = (HSSFRow) rows.next();
		 * 
		 * }
		 */

		// Création de la table de kitting
		debit = 1;
		indice = 1;
		numeroOperation = "1-000";
		gamme = "Kitting";
		rang = "Kitting câble";
		cursor = cr.query(urlNomenclature, colNom1, null, null,
				Cable.REFERENCE_INTERNE + " ASC , " + Cable.QUANTITE + " ASC ");
		if (cursor.moveToFirst()) {
			do {
				indiceChariotA++;
				// clause = new String(Chariot._id + "='" + indiceChariot++ +
				// "'");

				norme = cursor.getString(cursor
						.getColumnIndex(Cable.NORME_CABLE));
				clause = new String(Fil.NORME_CABLE + "='" + norme + "' AND "
						+ Fil.REPERE_ELECTRIQUE_TENANT + "!='" + "null" + "'"
						+ " GROUP BY " + Fil.NUMERO_FIL_CABLE);

				rang_1 = "Débit "
						+ cursor.getString(cursor
								.getColumnIndex(Cable.DESIGNATION_COMPOSANT))
						+ " Référence "
						+ cursor.getString(cursor
								.getColumnIndex(Cable.REFERENCE_FABRICANT2))
						+ " ("
						+ cursor.getString(cursor
								.getColumnIndex(Cable.REFERENCE_INTERNE)) + ")";
				cursorA = cr.query(urlProduction, colProd1, clause, null, null);
				if (cursorA.moveToFirst()) {
					do {

						num = numeroOperation + Integer.toString(indice++);

						contact.put(
								Kitting.DESIGNATION_COMPOSANT,
								cursor.getString(cursor
										.getColumnIndex(Cable.DESIGNATION_COMPOSANT)));
						contact.put(Kitting.NORME_CABLE, cursor
								.getString(cursor
										.getColumnIndex(Cable.NORME_CABLE)));
						contact.put(Kitting.UNITE, cursor.getString(cursor
								.getColumnIndex(Cable.UNITE)));
						contact.put(
								Kitting.REFERENCE_FABRICANT1,
								cursor.getString(cursor
										.getColumnIndex(Cable.REFERENCE_FABRICANT1)));
						contact.put(
								Kitting.REFERENCE_FABRICANT2,
								cursor.getString(cursor
										.getColumnIndex(Cable.REFERENCE_FABRICANT2)));
						contact.put(
								Kitting.REFERENCE_INTERNE,
								cursor.getString(cursor
										.getColumnIndex(Cable.REFERENCE_INTERNE)));
						contact.put(
								Kitting.FOURNISSEUR_FABRICANT,
								cursor.getString(cursor
										.getColumnIndex(Cable.FOURNISSEUR_FABRICANT)));

						contact.put(
								Kitting.REPERE_ELECTRIQUE,
								cursorA.getString(cursorA
										.getColumnIndex(Fil.REPERE_ELECTRIQUE_TENANT)));

						contact.put(
								Kitting.NUMERO_COMPOSANT,
								cursorA.getString(cursorA
										.getColumnIndex(Fil.NUMERO_COMPOSANT_TENANT)));

						contact.put(Kitting.ORDRE_REALISATION, cursorA
								.getString(cursorA
										.getColumnIndex(Fil.ORDRE_REALISATION)));

						// Attribution du numéro positionchariot
						String face;
						if (contact.getAsString(Kitting.ORDRE_REALISATION)
								.equals("Tête A")) {
							face = "Face 1";
						} else {
							face = "Face 2";
						}
						clause = Chariot.FACE_CHARIOT + "='" + face + "'";

						cursorB = cr.query(urlChariot, colCha, clause, null,
								Chariot._id + " ASC");
						if (cursorB.moveToFirst()) {
							cursorB.moveToPosition(indiceChariotA);
							numeroChariot = cursorB.getString(cursorB
									.getColumnIndex(Chariot.POSITION_NUMERO));

						}
						contact.put(Kitting.NUMERO_POSITION_CHARIOT,
								numeroChariot);

						contact.put(
								Kitting.NUMERO_REVISION_FIL,
								cursorA.getFloat(cursorA
										.getColumnIndex(Fil.NUMERO_REVISION_FIL)));
						contact.put(Kitting.ETAT_LIAISON_FIL, cursorA
								.getString(cursorA
										.getColumnIndex(Fil.ETAT_LIAISON_FIL)));
						contact.put(Kitting.NUMERO_FIL_CABLE, cursorA
								.getString(cursorA
										.getColumnIndex(Fil.NUMERO_FIL_CABLE)));
						contact.put(Kitting.TYPE_FIL_CABLE, cursorA
								.getString(cursorA
										.getColumnIndex(Fil.TYPE_FIL_CABLE)));
						contact.put(Kitting.NUMERO_DEBIT, debit);
						contact.put(Kitting.NUMERO_OPERATION, num);

						// Numéro cheminement
						clause = new String(Cheminement.NUMERO_COMPOSANT_TENANT
								+ "='"
								+ contact.getAsString(Kitting.NUMERO_COMPOSANT)
								+ "' OR "
								+ Cheminement.NUMERO_COMPOSANT_ABOUTISSANT
								+ "='"
								+ contact.getAsString(Kitting.NUMERO_COMPOSANT)
								+ "'");
						cursorB = cr.query(urlCheminement, ColChem1, clause,
								null, null);

						if (cursorB.moveToFirst()) {
							try {
								numeroCheminement = cursorB
										.getInt(cursorB
												.getColumnIndex(Cheminement.NUMERO_CHEMINEMNT));
								contact.put(Kitting.NUMERO_CHEMINEMENT,
										numeroCheminement);
							} catch (NullPointerException e) {

							}

						}

						// Ajout de l'entité
						getContentResolver().insert(urlKitting, contact);
						// Ecrasement de ses données pour passer à la suivante
						contact.clear();

						// num1= numeroOperation + Integer.toString(indice++);
						descriptionOperation = "Débit du fil n°"
								+ cursorA.getString(cursorA
										.getColumnIndex(Fil.NUMERO_FIL_CABLE));

						// Ajout des opérations à la table de séquencement
						contact.put(Operation.GAMME, gamme);
						contact.put(Operation.RANG_1, rang);
						contact.put(Operation.RANG_1_1, rang_1);
						contact.put(Operation.DESCRIPTION_OPERATION,
								descriptionOperation);
						contact.put(Operation.NUMERO_OPERATION, num);
						contact.put(Operation.DUREE_THEORIQUE, DEBIT_CABLES);

						// Ajout de l'entité
						getContentResolver().insert(urlSequencement, contact);
						// Ecrasement de ses données pour passer à la suivante
						contact.clear();

					} while (cursorA.moveToNext());

					debit++;
				}

			} while (cursor.moveToNext());

			// Regroupement des cables
			clause = new String(Kitting.NUMERO_COMPOSANT + "!='" + "null" + "'"
					+ " GROUP BY " + Kitting.NUMERO_COMPOSANT);
			cursor = cr.query(urlKitting, colKit3, clause, null, null);
			rang_1 = "Regroupement câbles";
			if (cursor.moveToFirst()) {
				do {
					num1 = numeroOperation + Integer.toString(indice++);
					descriptionOperation = "Regroupement des câbles "
							+ cursor.getString(cursor
									.getColumnIndex(Kitting.ORDRE_REALISATION))
							+ " connecteur "
							+ cursor.getString(cursor
									.getColumnIndex(Kitting.NUMERO_COMPOSANT))
							+ " ("
							+ cursor.getString(cursor
									.getColumnIndex(Kitting.REPERE_ELECTRIQUE))
							+ ")";
					rang_1 = descriptionOperation;

					// Ajout des opérations à la table de séquencement
					contact.put(Operation.GAMME, gamme);
					contact.put(Operation.RANG_1, rang);
					contact.put(Operation.RANG_1_1, rang_1);
					contact.put(Operation.DESCRIPTION_OPERATION,
							descriptionOperation);
					contact.put(Operation.NUMERO_OPERATION, num1);
					contact.put(Operation.DUREE_THEORIQUE, DEBIT_CABLES);

					// Ajout de l'entité
					getContentResolver().insert(urlSequencement, contact);
					// Ecrasement de ses données pour passer à la suivante
					contact.clear();

				} while (cursor.moveToNext());
			} else {
				Toast.makeText(this, "Regroupement non établi",
						Toast.LENGTH_SHORT).show();

			}

			Toast.makeText(this, "Table kitting créée", Toast.LENGTH_SHORT)
					.show();
		}

		// Création de la table de raccordement
		int indiceTenant = 1;
		int indiceAboutissant = 1;
		cursor = cr.query(urlProduction, ColProd2, null, null, null);
		if (cursor.moveToFirst()) {

			do {

				contact.put(Raccordement.COULEUR_FIL, cursor.getString(cursor
						.getColumnIndex(Fil.COULEUR_FIL)));
				contact.put(Raccordement.ETAT_FINALISATION_PRISE, cursor
						.getString(cursor
								.getColumnIndex(Fil.ETAT_FINALISATION_PRISE)));
				contact.put(Raccordement.ETAT_LIAISON_FIL, cursor
						.getString(cursor.getColumnIndex(Fil.ETAT_LIAISON_FIL)));
				contact.put(Raccordement.FAUX_CONTACT,
						cursor.getInt(cursor.getColumnIndex(Fil.FAUX_CONTACT)));
				contact.put(Raccordement.FICHE_INSTRUCTION,
						cursor.getString(cursor
								.getColumnIndex(Fil.FICHE_INSTRUCTION)));
				contact.put(Raccordement.FIL_SENSIBLE,
						cursor.getInt(cursor.getColumnIndex(Fil.FIL_SENSIBLE)));
				contact.put(Raccordement.LONGUEUR_FIL_CABLE,
						cursor.getFloat(cursor
								.getColumnIndex(Fil.LONGUEUR_FIL_CABLE)));
				contact.put(Raccordement.NOM_SIGNAL,
						cursor.getString(cursor.getColumnIndex(Fil.NOM_SIGNAL)));
				contact.put(Raccordement.NUMERO_BORNE_ABOUTISSANT, cursor
						.getString(cursor
								.getColumnIndex(Fil.NUMERO_BORNE_ABOUTISSANT)));
				contact.put(Raccordement.NUMERO_BORNE_TENANT, cursor
						.getString(cursor
								.getColumnIndex(Fil.NUMERO_BORNE_TENANT)));
				contact.put(Raccordement.DESIGNATION, cursor.getString(cursor
						.getColumnIndex(Fil.DESIGNATION_PRODUIT)));
				contact.put(
						Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
						cursor.getString(cursor
								.getColumnIndex(Fil.NUMERO_COMPOSANT_ABOUTISSANT)));
				contact.put(Raccordement.NUMERO_COMPOSANT_TENANT, cursor
						.getString(cursor
								.getColumnIndex(Fil.NUMERO_COMPOSANT_TENANT)));
				contact.put(Raccordement.NUMERO_FIL_CABLE, cursor
						.getString(cursor.getColumnIndex(Fil.NUMERO_FIL_CABLE)));
				float nfc = cursor.getFloat(cursor
						.getColumnIndex(Fil.NUMERO_FIL_DANS_CABLE));
				if (nfc != 0) {
					contact.put(Raccordement.NUMERO_FIL_DANS_CABLE, nfc);
				}
				contact.put(Raccordement.NUMERO_REVISION_FIL, cursor
						.getFloat(cursor
								.getColumnIndex(Fil.NUMERO_REVISION_FIL)));
				contact.put(Raccordement.OBTURATEUR,
						cursor.getInt(cursor.getColumnIndex(Fil.OBTURATEUR)));
				contact.put(Raccordement.ORDRE_REALISATION,
						cursor.getString(cursor
								.getColumnIndex(Fil.ORDRE_REALISATION)));
				contact.put(
						Raccordement.ORIENTATION_RACCORD_ARRIERE,
						cursor.getString(cursor
								.getColumnIndex(Fil.ORIENTATION_RACCORD_ARRIERE)));
				contact.put(
						Raccordement.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT,
						cursor.getString(cursor
								.getColumnIndex(Fil.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT)));
				contact.put(
						Raccordement.REFERENCE_ACCESSOIRE_OUTIL_TENANT,
						cursor.getString(cursor
								.getColumnIndex(Fil.REFERENCE_ACCESSOIRE_OUTIL_TENANT)));
				contact.put(
						Raccordement.REFERENCE_CONFIGURATION_SERTISSAGE,
						cursor.getString(cursor
								.getColumnIndex(Fil.REFERENCE_CONFIGURATION_SERTISSAGE)));
				contact.put(Raccordement.REFERENCE_FABRICANT2, cursor
						.getString(cursor
								.getColumnIndex(Fil.REFERENCE_FABRICANT2)));
				contact.put(Raccordement.REFERENCE_INTERNE,
						cursor.getString(cursor
								.getColumnIndex(Fil.REFERENCE_INTERNE)));
				contact.put(
						Raccordement.REFERENCE_OUTIL_ABOUTISSANT,
						cursor.getString(cursor
								.getColumnIndex(Fil.REFERENCE_OUTIL_ABOUTISSANT)));
				contact.put(Raccordement.STANDARD,
						cursor.getString(cursor.getColumnIndex(Fil.STANDARD)));
				contact.put(Raccordement.NUMERO_HARNAIS_FAISCEAUX, cursor
						.getString(cursor
								.getColumnIndex(Fil.NUMERO_HARNAIS_FAISCEAUX)));
				contact.put(
						Raccordement.REFERENCE_OUTIL_ABOUTISSANT,
						cursor.getString(cursor
								.getColumnIndex(Fil.REFERENCE_OUTIL_ABOUTISSANT)));
				contact.put(Raccordement.DESIGNATION, cursor.getString(cursor
						.getColumnIndex(Fil.DESIGNATION_PRODUIT)));
				contact.put(Raccordement.NUMERO_REVISION_HARNAIS, cursor
						.getString(cursor
								.getColumnIndex(Fil.NUMERO_REVISION_HARNAIS)));
				contact.put(Raccordement.REFERENCE_FICHIER_SOURCE, cursor
						.getString(cursor
								.getColumnIndex(Fil.REFERENCE_FICHIER_SOURCE)));
				contact.put(Raccordement.REFERENCE_OUTIL_TENANT, cursor
						.getString(cursor
								.getColumnIndex(Fil.REFERENCE_OUTIL_TENANT)));
				contact.put(Raccordement.REGLAGE_OUTIL_ABOUTISSANT, cursor
						.getString(cursor
								.getColumnIndex(Fil.REGLAGE_OUTIL_ABOUTISSANT)));
				contact.put(Raccordement.REGLAGE_OUTIL_TENANT, cursor
						.getString(cursor
								.getColumnIndex(Fil.REGLAGE_OUTIL_TENANT)));
				contact.put(
						Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
						cursor.getString(cursor
								.getColumnIndex(Fil.REPERE_ELECTRIQUE_ABOUTISSANT)));
				contact.put(Raccordement.REPERE_ELECTRIQUE_TENANT, cursor
						.getString(cursor
								.getColumnIndex(Fil.REPERE_ELECTRIQUE_TENANT)));
				contact.put(Raccordement.REPRISE_BLINDAGE, cursor
						.getString(cursor.getColumnIndex(Fil.REPRISE_BLINDAGE)));
				contact.put(Raccordement.SANS_REPRISE_BLINDAGE, cursor
						.getString(cursor
								.getColumnIndex(Fil.SANS_REPRISE_BLINDAGE)));
				contact.put(Raccordement.TYPE_ELEMENT_RACCORDE, cursor
						.getString(cursor
								.getColumnIndex(Fil.TYPE_ELEMENT_RACCORDE)));
				contact.put(Raccordement.TYPE_FIL_CABLE, cursor
						.getString(cursor.getColumnIndex(Fil.TYPE_FIL_CABLE)));
				contact.put(
						Raccordement.TYPE_RACCORDEMENT_ABOUTISSANT,
						cursor.getString(cursor
								.getColumnIndex(Fil.TYPE_RACCORDEMENT_ABOUTISSANT)));
				contact.put(Raccordement.TYPE_RACCORDEMENT_TENANT, cursor
						.getString(cursor
								.getColumnIndex(Fil.TYPE_RACCORDEMENT_TENANT)));
				contact.put(Raccordement.ACCESSOIRE_COMPOSANT1, cursor
						.getString(cursor
								.getColumnIndex(Fil.ACCESSOIRE_COMPOSANT1)));
				contact.put(Raccordement.ACCESSOIRE_COMPOSANT2, cursor
						.getString(cursor
								.getColumnIndex(Fil.ACCESSOIRE_COMPOSANT2)));
				contact.put(Raccordement.ZONE_ACTIVITE, cursor.getString(cursor
						.getColumnIndex(Fil.ZONE_ACTIVITE)));
				contact.put(Raccordement.LOCALISATION1, cursor.getString(cursor
						.getColumnIndex(Fil.LOCALISATION1)));
				contact.put(Raccordement.NUMERO_REPERE_TABLE_CHEMINEMENT,
						cursor.getString(cursor
								.getColumnIndex(Fil.LOCALISATION2)));

				// Numéro Position Chariot
				clause = new String(Kitting.NUMERO_FIL_CABLE + "='"
						+ contact.getAsString(Raccordement.NUMERO_FIL_CABLE)
						+ "'");
				cursorB = cr.query(urlKitting, colKit3, clause, null, null);

				if (cursorB.moveToFirst()) {
					contact.put(
							Raccordement.NUMERO_POSITION_CHARIOT,
							cursorB.getString(cursorB
									.getColumnIndex(Kitting.NUMERO_POSITION_CHARIOT)));
				}

				// Numéro cheminement + Numéro Opération
				if (contact.getAsString(Raccordement.NUMERO_COMPOSANT_TENANT) != null) {
					numeroComposant = contact
							.getAsString(Raccordement.NUMERO_COMPOSANT_TENANT);

				} else {
					numeroComposant = contact
							.getAsString(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT);

				}

				clause = new String(Cheminement.NUMERO_COMPOSANT_TENANT
						+ " LIKE '%" + numeroComposant + "%' OR "
						+ Cheminement.NUMERO_COMPOSANT_ABOUTISSANT + " LIKE '%"
						+ numeroComposant + "%'");
				cursorB = cr
						.query(urlCheminement, ColChem1, clause, null, null);

				if (cursorB.moveToFirst()) {
					numeroCheminement = cursorB
							.getInt(cursorB
									.getColumnIndex(Cheminement.NUMERO_SECTION_CHEMINEMENT));
					try {
						contact.put(
								Raccordement.NUMERO_CHEMINEMENT,
								cursorB.getInt(cursorB
										.getColumnIndex(Cheminement.NUMERO_CHEMINEMNT)));
					} catch (NullPointerException e) {

					}

					contact.put(Raccordement.NUMERO_SECTION_CHEMINEMENT,
							numeroCheminement);

				}

				// Ajout de l'entité
				getContentResolver().insert(urlRaccordement, contact);
				// Ecrasement de ses données pour passer à la suivante
				contact.clear();

			} while (cursor.moveToNext());
			Toast.makeText(this, "Table raccordement créée", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(this, "Erreur raccordement", Toast.LENGTH_SHORT)
					.show();
		}

		// Création de la table BOM
		debit = 1;
		indice = 1;
		numeroOperation = "2-000";
		gamme = "Kitting";
		rang = "Kitting têtes";
		clause = new String(Cable.NUMERO_COMPOSANT + "!='" + "null" + "' AND "
				+ Cable.REPERE_ELECTRIQUE + "!='" + "null" + "' ");
		cursor = cr.query(urlNomenclature, colNom3, clause, null,
				Cable.REFERENCE_INTERNE + " , " + Cable.QUANTITE);

		if (cursor.moveToFirst()) {
			referenceCourante = cursor.getString(cursor
					.getColumnIndex(Cable.REFERENCE_FABRICANT2));

			do {

				num = numeroOperation + Integer.toString(indice++);

				contact.put(BOM.DESIGNATION_COMPOSANT, cursor.getString(cursor
						.getColumnIndex(Cable.DESIGNATION_COMPOSANT)));

				contact.put(BOM.REFERENCE_FABRICANT2, cursor.getString(cursor
						.getColumnIndex(Cable.REFERENCE_FABRICANT2)));

				try {
					// Changement du numéro de débit
					if (!(contact.get(BOM.REFERENCE_FABRICANT2)
							.equals(referenceCourante))) {
						debit++;
						referenceCourante = contact.get(
								BOM.REFERENCE_FABRICANT2).toString();

					}
				} catch (NullPointerException e) {

				}
				contact.put(BOM.REFERENCE_INTERNE, cursor.getString(cursor
						.getColumnIndex(Cable.REFERENCE_INTERNE)));
				contact.put(BOM.FOURNISSEUR_FABRICANT, cursor.getString(cursor
						.getColumnIndex(Cable.FOURNISSEUR_FABRICANT)));
				contact.put(BOM.EQUIPEMENT, cursor.getString(cursor
						.getColumnIndex(Cable.EQUIPEMENT)));
				contact.put(BOM.REPERE_ELECTRIQUE_TENANT, cursor
						.getString(cursor
								.getColumnIndex(Cable.REPERE_ELECTRIQUE)));
				contact.put(BOM.NUMERO_COMPOSANT, cursor.getString(cursor
						.getColumnIndex(Cable.NUMERO_COMPOSANT)));
				contact.put(BOM.ACCESSOIRE_CABLAGE, cursor.getString(cursor
						.getColumnIndex(Cable.ACCESSOIRE_CABLAGE)));
				contact.put(BOM.ACCESSOIRE_COMPOSANT, cursor.getString(cursor
						.getColumnIndex(Cable.ACCESSOIRE_COMPOSANT)));
				contact.put(BOM.REFERENCE_IMPOSEE, cursor.getString(cursor
						.getColumnIndex(Cable.REFERENCE_IMPOSEE)));
				contact.put(BOM.QUANTITE,
						cursor.getString(cursor.getColumnIndex(Cable.QUANTITE)));
				contact.put(BOM.UNITE,
						cursor.getString(cursor.getColumnIndex(Cable.UNITE)));
				contact.put(BOM.NUMERO_DEBIT, debit);
				contact.put(BOM.NUMERO_OPERATION, num);

				clause = new String(Cheminement.NUMERO_COMPOSANT_TENANT + "='"
						+ contact.getAsString(BOM.NUMERO_COMPOSANT) + "' OR "
						+ Cheminement.NUMERO_COMPOSANT_ABOUTISSANT + "='"
						+ contact.getAsString(BOM.NUMERO_COMPOSANT) + "'");
				cursorB = cr
						.query(urlCheminement, ColChem1, clause, null, null);

				if (cursorB.moveToFirst()) {
					try {
						numeroCheminement = cursorB.getInt(cursorB
								.getColumnIndex(Cheminement.NUMERO_CHEMINEMNT));
						contact.put(BOM.NUMERO_CHEMINEMENT, numeroCheminement);
					} catch (NullPointerException e) {

					}

				}
				clause = new String(Kitting.NUMERO_COMPOSANT
						+ "='"
						+ cursor.getString(cursor
								.getColumnIndex(Cable.NUMERO_COMPOSANT)) + "'");
				cursorB = cr.query(urlKitting, colKit3, clause, null, null);
				if (cursorB.moveToFirst()) {
					contact.put(
							BOM.NUMERO_POSITION_CHARIOT,
							cursorB.getString(cursorB
									.getColumnIndex(Kitting.NUMERO_POSITION_CHARIOT)));
				}

				// Ajouter les Ordre depuis la BD Production
				clause = new String(Fil.NUMERO_COMPOSANT_TENANT + "='"
						+ contact.getAsString(BOM.NUMERO_COMPOSANT) + "' OR "
						+ Fil.NUMERO_COMPOSANT_ABOUTISSANT + "='"
						+ contact.getAsString(BOM.NUMERO_COMPOSANT) + "'");
				cursorB = cr.query(urlProduction, colProd1, clause, null,
						Fil._id);
				if (cursorB.moveToFirst()) {
					contact.put(BOM.ORDRE_REALISATION, cursorB
							.getString(cursorB
									.getColumnIndex(Fil.ORDRE_REALISATION)));
				}

				// Ajout de l'entité
				getContentResolver().insert(urlBOM, contact);
				// Ecrasement de ses données pour passer à la suivante
				contact.clear();

				rang_1 = "Débit "
						+ cursor.getString(cursor
								.getColumnIndex(Cable.DESIGNATION_COMPOSANT))
						+ " réference "
						+ cursor.getString(cursor
								.getColumnIndex(Cable.REFERENCE_FABRICANT2))
						+ " ("
						+ cursor.getString(cursor
								.getColumnIndex(Cable.REFERENCE_INTERNE)) + ")";
				descriptionOperation = "Débit pour connecteur"
						+ cursor.getString(cursor
								.getColumnIndex(Cable.NUMERO_COMPOSANT))
						+ " ("
						+ cursor.getString(cursor
								.getColumnIndex(Cable.REPERE_ELECTRIQUE)) + ")";

				// Ajout des opérations à la table de séquencement
				contact.put(Operation.GAMME, gamme);
				contact.put(Operation.RANG_1, rang);
				contact.put(Operation.RANG_1_1, rang_1);
				contact.put(Operation.DESCRIPTION_OPERATION,
						descriptionOperation);
				contact.put(Operation.NUMERO_OPERATION, num);
				contact.put(Operation.DUREE_THEORIQUE, KIT_TETE);

				// Ajout de l'entité
				getContentResolver().insert(urlSequencement, contact);
				// Ecrasement de ses données pour passer à la suivante
				contact.clear();

			} while (cursor.moveToNext());
			// Regroupement des cables
			clause = new String(BOM.NUMERO_COMPOSANT + "!='" + "null" + "'"
					+ " GROUP BY " + BOM.NUMERO_COMPOSANT);
			cursor = cr.query(urlBOM, colBOM3, clause, null, null);

			if (cursor.moveToFirst()) {
				do {
					num1 = numeroOperation + Integer.toString(indice++);
					rang_1 = "Constitution kit "
							+ cursor.getString(cursor
									.getColumnIndex(BOM.ORDRE_REALISATION))
							+ " connecteur "
							+ cursor.getString(cursor
									.getColumnIndex(BOM.NUMERO_COMPOSANT))
							+ " ("
							+ cursor.getString(cursor
									.getColumnIndex(BOM.REPERE_ELECTRIQUE_TENANT))
							+ ")";
					descriptionOperation = "Intégration des câbles du connecteur "
							+ cursor.getString(cursor
									.getColumnIndex(BOM.NUMERO_COMPOSANT))
							+ " ("
							+ cursor.getString(cursor
									.getColumnIndex(BOM.REPERE_ELECTRIQUE_TENANT))
							+ ")";

					// Ajout des opérations à la table de séquencement
					contact.put(Operation.GAMME, gamme);
					contact.put(Operation.RANG_1, rang);
					contact.put(Operation.RANG_1_1, rang_1);
					contact.put(Operation.DESCRIPTION_OPERATION,
							descriptionOperation);
					contact.put(Operation.NUMERO_OPERATION, num1);
					contact.put(Operation.DUREE_THEORIQUE, KIT_TETE);

					// Ajout de l'entité
					getContentResolver().insert(urlSequencement, contact);
					// Ecrasement de ses données pour passer à la suivante
					contact.clear();

					num1 = numeroOperation + Integer.toString(indice++);
					descriptionOperation = "Regroupement & emballage du kit connecteur "
							+ cursor.getString(cursor
									.getColumnIndex(BOM.NUMERO_COMPOSANT))
							+ " ("
							+ cursor.getString(cursor
									.getColumnIndex(BOM.REPERE_ELECTRIQUE_TENANT))
							+ ")";

					// Ajout des opérations à la table de séquencement
					contact.put(Operation.GAMME, gamme);
					contact.put(Operation.RANG_1, rang);
					contact.put(Operation.RANG_1_1, rang_1);
					contact.put(Operation.DESCRIPTION_OPERATION,
							descriptionOperation);
					contact.put(Operation.NUMERO_OPERATION, num1);
					contact.put(Operation.DUREE_THEORIQUE, KIT_TETE);

					// Ajout de l'entité
					getContentResolver().insert(urlSequencement, contact);
					// Ecrasement de ses données pour passer à la suivante
					contact.clear();

				} while (cursor.moveToNext());
			} else {
				Toast.makeText(this, "Regroupement non établi",
						Toast.LENGTH_SHORT).show();

			}

			debit++;
		} else {
			Log.e("BOM", "Table BOM non créée");
		}
		Toast.makeText(this, "Table BOM créée", Toast.LENGTH_SHORT).show();

	}

	/**
	 * Import des bases de données source à partir des fichiers Excels
	 * 
	 * @return Réussite de l'import
	 */
	private boolean importSources() {
		getContentResolver().delete(urlSequencement,
				Operation._id + "!='null'", null);

		// Import des durées
		try {
			insertRecordsDurees();
		} catch (IOException e) {
			Toast.makeText(this, "Fichier Durees non lu", Toast.LENGTH_SHORT)
					.show();
			return false;
		}

		// Import de la base Production
		try {
			insertRecordsProduction();
		} catch (IOException e) {
			Toast.makeText(this, "Fichier Production non lu",
					Toast.LENGTH_SHORT).show();
			return false;
		}

		// Import de la base Nomenclature
		try {
			insertRecordsNomenclature();
		} catch (IOException e) {
			Toast.makeText(this, "Fichier Nomenclature non lu",
					Toast.LENGTH_SHORT).show();
			return false;
		}

		try {
			insertRecordsOutillage();
		} catch (IOException e) {
			Toast.makeText(this, "Fichier Outillage non lu", Toast.LENGTH_SHORT)
					.show();
			return false;
		}

		return true;

	}

	private void insertRecordsOutillage() throws IOException {
		getContentResolver().delete(urlOutillage, Outil._id + "!='null'", null);
		getContentResolver().delete(urlSupport, Support._id + "!='null'", null);
		getContentResolver().delete(urlChariot, Chariot._id + "!='null'", null);

		// Création d'un InputStream vers le fichier Excel
		/*
		 * InputStream input = this.getResources().openRawResource(
		 * R.raw.table_outillage);
		 */
		String nomFichier = null;
		File data = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ "Android"
				+ File.separator + "data" + File.separator + getPackageName());

		for (String file : data.list()) {
			Log.d("Nom Fichier", file);
			if (file.contains("outillage")) {
				nomFichier = file;
				Log.e("Nom Fichier", nomFichier);
			}
		}

		InputStream input = new FileInputStream(Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator
				+ "Android"
				+ File.separator
				+ "data"
				+ File.separator
				+ getPackageName()
				+ File.separator
				+ nomFichier);

		// Interpretation du fichier a l'aide de Apache POI
		POIFSFileSystem fs = new POIFSFileSystem(input);
		HSSFWorkbook wb = new HSSFWorkbook(fs);

		// Feuille outillages
		HSSFSheet sheet = wb.getSheetAt(0);
		ContentValues contact = new ContentValues();
		// Iteration sur chacune des lignes du fichier
		Iterator rows = sheet.rowIterator();
		rows.next();
		HSSFRow row = (HSSFRow) rows.next();
		HashMap<String, Integer> colonnes = new HashMap<String, Integer>();
		// Stockage des indices des colonnes
		for (int i = 0; i < row.getLastCellNum(); i++) {
			colonnes.put(row.getCell(i).toString(), i);

		}

		while (rows.hasNext()) {
			row = (HSSFRow) rows.next();
			try {
				contact.put(Outil.AFFECTATION,
						row.getCell(colonnes.get("affectation")).toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Outil.CODE_BARRE,
						row.getCell(colonnes.get("code_barre")).toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Outil.COMMENTAIRES,
						row.getCell(colonnes.get("Commentaires")).toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Outil.CONSTRUCTEUR,
						row.getCell(colonnes.get("constructeur")).toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Outil.DERNIERE_OPERATION,
						row.getCell(colonnes.get("derniere operation"))
								.toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Outil.IDENTIFICATION,
						row.getCell(colonnes.get("Identification")).toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Outil.INTITULE,
						row.getCell(colonnes.get("intitule")).toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Outil.NUMERO_SERIE,
						row.getCell(colonnes.get("n_serie")).toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Outil.PERIODE, Float.parseFloat(row.getCell(
						colonnes.get("periode")).toString()));
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Outil.PROCHAINE_OPERATION,
						row.getCell(colonnes.get("prochaine operation"))
								.toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Outil.PROPRIETAIRE,
						row.getCell(colonnes.get("proprietaire")).toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Outil.SECTION, row.getCell(colonnes.get("section"))
						.toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Outil.TYPE, row.getCell(colonnes.get("type"))
						.toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Outil.UNITE, row.getCell(colonnes.get("unite"))
						.toString());
			} catch (NullPointerException e) {
			}

			// Ajout de l'entité
			getContentResolver().insert(urlOutillage, contact);
			// Ecrasement de ses données pour passer à la suivante
			contact.clear();

		}

		// Feuille Support
		sheet = wb.getSheetAt(1);
		rows = sheet.rowIterator();
		row = (HSSFRow) rows.next();
		colonnes = new HashMap<String, Integer>();
		// Stockage des indices des colonnes
		for (int i = 0; i < row.getLastCellNum(); i++) {
			colonnes.put(row.getCell(i).toString(), i);

		}

		while (rows.hasNext()) {
			row = (HSSFRow) rows.next();
			try {
				contact.put(Support.AFFECTATION,
						row.getCell(colonnes.get("Affectation")).toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Support.CODE_TAG,
						row.getCell(colonnes.get("Code TAG")).toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Support.DIAMETRE_ADMISSIBLE,
						row.getCell(colonnes.get("Diamètre admissible"))
								.toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Support.NUMERO_SERIE,
						row.getCell(colonnes.get("Numéro de série du support"))
								.toString());
			} catch (NullPointerException e) {
			}
			try {
				contact.put(Support.TYPE_SUPPORT,
						row.getCell(colonnes.get("Type de support")).toString());
			} catch (NullPointerException e) {
			}

			// Ajout de l'entité
			getContentResolver().insert(urlSupport, contact);
			// Ecrasement de ses données pour passer à la suivante
			contact.clear();

		}

		// Genération des numéros de chariot
		contact = new ContentValues();
		String localisations[] = new String[] { "A", "B", "C", "D", "E", "F",
				"G", "H", "I", "J" };
		for (int i = 0; i < localisations.length; i++) {
			for (int j = 1; j <= 7; j++) {
				contact.put(Chariot.FACE_CHARIOT, "Face 1");
				contact.put(Chariot.NUMERO_CHARIOT, "Chariot 1");
				contact.put(Chariot.POSITION_NUMERO, "CH1-1-"
						+ localisations[i] + j);
				getContentResolver().insert(urlChariot, contact);
				contact.clear();
			}
		}
		for (int i = 0; i < localisations.length; i++) {
			for (int j = 1; j <= 7; j++) {

				contact.put(Chariot.FACE_CHARIOT, "Face 2");
				contact.put(Chariot.NUMERO_CHARIOT, "Chariot 1");
				contact.put(Chariot.POSITION_NUMERO, "CH1-2-"
						+ localisations[i] + j);
				getContentResolver().insert(urlChariot, contact);
				contact.clear();

			}
		}
		for (int i = 0; i < localisations.length; i++) {
			for (int j = 1; j <= 7; j++) {

				contact.put(Chariot.FACE_CHARIOT, "Face 1");
				contact.put(Chariot.NUMERO_CHARIOT, "Chariot 2");
				contact.put(Chariot.POSITION_NUMERO, "CH2-1-"
						+ localisations[i] + j);
				getContentResolver().insert(urlChariot, contact);
				contact.clear();
			}
		}
		for (int i = 0; i < localisations.length; i++) {
			for (int j = 1; j <= 7; j++) {

				contact.put(Chariot.FACE_CHARIOT, "Face 2");
				contact.put(Chariot.NUMERO_CHARIOT, "Chariot 2");
				contact.put(Chariot.POSITION_NUMERO, "CH2-2-"
						+ localisations[i] + j);
				getContentResolver().insert(urlChariot, contact);
				contact.clear();
			}
		}

	}

	/**
	 * Lecture du fichier Excel bd_temps.xls et ajout des lignes correspondantes
	 * à la bases de données Durees
	 * 
	 * @throws IOException
	 */

	private void insertRecordsDurees() throws IOException {
		getContentResolver().delete(urlDurees, Duree._id + "!='null'", null);
		// Création d'un InputStream vers le fichier Excel
		String nomFichier = null;
		File data = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ "Android"
				+ File.separator + "data" + File.separator + getPackageName());

		for (String file : data.list()) {
			Log.d("Nom Fichier", file);
			if (file.contains("temps")) {
				nomFichier = file;
				Log.e("Nom Fichier", nomFichier);
			}
		}

		InputStream input = new FileInputStream(Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator
				+ "Android"
				+ File.separator
				+ "data"
				+ File.separator
				+ getPackageName()
				+ File.separator
				+ nomFichier);
		// Interpretation du fichier a l'aide de Apache POI
		POIFSFileSystem fs = new POIFSFileSystem(input);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);

		// Iteration sur chacune des lignes du fichier
		Iterator rows = sheet.rowIterator();
		// On ne rentre pas les trois premières lignes qui ne comportent que les
		// entêtes des colonnes
		rows.next();
		rows.next();

		ContentValues contact = new ContentValues();

		// Parcours des lignes
		while (rows.hasNext()) {

			HSSFRow row = (HSSFRow) rows.next();
			// Ajout des données correspondantes
			contact.put(Duree.CODE_OPERATION,
					Float.parseFloat(row.getCell(0).toString()));
			contact.put(Duree.DESIGNATION_OPERATION, row.getCell(1).toString());
			contact.put(Duree.DUREE_THEORIQUE, row.getCell(2).toString());
			contact.put(Duree.UNITE, row.getCell(3).toString());
			try {
				if (row.getCell(4).toString().equals("X")) {
					contact.put(Duree.OPERATION_SOUS_CONTROLE, true);
				}
			} catch (Exception e) {
				contact.put(Duree.OPERATION_SOUS_CONTROLE, false);
			}

			// Ajout de l'entité
			getContentResolver().insert(urlDurees, contact);
			// Ecrasement de ses données pour passer à la suivante
			contact.clear();

		}

		KIT_TETE = 180;

		/* Création des variables de duréés pour la table de séquencement */
		cursor = cr.query(urlDurees, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%Fret%'", null, null);
		if (cursor.moveToFirst()) {
			FRETTAGE = TimeConverter.convert(cursor.getString(cursor
					.getColumnIndex(Duree.DUREE_THEORIQUE)));
		}
		cursor = cr.query(urlDurees, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%Débit de câbles%'", null, null);
		if (cursor.moveToFirst()) {
			DEBIT_CABLES = TimeConverter.convert(cursor.getString(cursor
					.getColumnIndex(Duree.DUREE_THEORIQUE)));
		}
		cursor = cr.query(urlDurees, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%Enfichag%'", null, null);
		if (cursor.moveToFirst()) {
			ENFICHAGE_FC_OBTURATEURS = TimeConverter.convert(cursor
					.getString(cursor.getColumnIndex(Duree.DUREE_THEORIQUE)));
		}

		cursor = cr.query(urlDurees, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%Sertissage contacts%'", null, null);
		if (cursor.moveToFirst()) {
			SERTISSAGE_CONTACTS = TimeConverter.convert(cursor.getString(cursor
					.getColumnIndex(Duree.DUREE_THEORIQUE)));
		}

		cursor = cr.query(urlDurees, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%Reprise%'", null, null);
		if (cursor.moveToFirst()) {
			REPRISE_BLINDAGE = TimeConverter.convert(cursor.getString(cursor
					.getColumnIndex(Duree.DUREE_THEORIQUE)));
		}

		cursor = cr.query(urlDurees, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%étention%'", null, null);
		if (cursor.moveToFirst()) {
			RETENTION_CONTACT = TimeConverter.convert(cursor.getString(cursor
					.getColumnIndex(Duree.DUREE_THEORIQUE)));
		}

		cursor = cr.query(urlDurees, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%Posit%'", null, null);
		if (cursor.moveToFirst()) {
			POSITIONNEMENT_SUPPORT = TimeConverter.convert(cursor
					.getString(cursor.getColumnIndex(Duree.DUREE_THEORIQUE)));
		}

		cursor = cr.query(urlDurees, colTim, Duree.DESIGNATION_OPERATION
				+ " LIKE '%Kit%'", null, null);
		if (cursor.moveToFirst()) {
			KIT_TETE = TimeConverter.convert(cursor.getString(cursor
					.getColumnIndex(Duree.DUREE_THEORIQUE)));
		}

	}

	/**
	 * Lecture du fichier Excel bd_production.xls et ajout des lignes
	 * correspondantes à la bases de données Production
	 * 
	 * @throws IOException
	 */
	private void insertRecordsProduction() throws IOException {
		getContentResolver().delete(urlProduction, Fil._id + "!='null'", null);

		// Création d'un InputStream vers le fichier Excel
		String nomFichier = null;
		File data = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ "Android"
				+ File.separator + "data" + File.separator + getPackageName());

		for (String file : data.list()) {
			Log.d("Nom Fichier", file);
			if (file.contains("production")) {
				nomFichier = file;
				Log.e("Nom Fichier", nomFichier);
			}
		}

		InputStream input = new FileInputStream(Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator
				+ "Android"
				+ File.separator
				+ "data"
				+ File.separator
				+ getPackageName()
				+ File.separator
				+ nomFichier);
		// Interpretation du fichier a l'aide de Apache POI
		POIFSFileSystem fs = new POIFSFileSystem(input);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);

		// Iteration sur chacune des lignes du fichier
		Iterator rows = sheet.rowIterator();
		// On ne rentre pas la première ligne qui ne comporte que les entêtes
		// des colonnes
		HSSFRow row = (HSSFRow) rows.next();
		HashMap<String, Integer> colonnes = new HashMap<String, Integer>();

		// Stockage des indices des colonnes
		for (int i = 0; i < row.getLastCellNum(); i++) {
			colonnes.put(row.getCell(i).toString(), i);

		}

		ContentValues contact = new ContentValues();
		// Parcours des lignes
		while (rows.hasNext()) {
			row = (HSSFRow) rows.next();
			// Ajout des données correspondantes
			// Les nombreux try/catch permettent d'éviter des
			// NullPointerException causées par les cellules vides
			try {
				contact.put(Fil.DESIGNATION_PRODUIT,
						row.getCell(colonnes.get("Designation_produit (1)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.REFERENCE_FICHIER_SOURCE,
						row.getCell(
								colonnes.get("Reference_fichier_source (2)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.NUMERO_REVISION_HARNAIS, Float.parseFloat(row
						.getCell(colonnes.get("Numero_revision_harnais (4)"))
						.toString()));
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.NUMERO_HARNAIS_FAISCEAUX, Float.parseFloat(row
						.getCell(colonnes.get("Numero_harnais_faisceaux (6)"))
						.toString()));
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.REFERENCE_FABRICANT1,
						row.getCell(colonnes.get("Reference_fabricant (17)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.STANDARD,
						Float.parseFloat(row.getCell(
								colonnes.get("Standard (21)")).toString()));
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.ZONE_ACTIVITE,
						row.getCell(colonnes.get("Zone-activité (67)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.LOCALISATION1,
						row.getCell(colonnes.get("Localisation_1 (68)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.LOCALISATION2, Float.parseFloat((row
						.getCell(colonnes.get("Localisation_2 (69)"))
						.toString())));
			} catch (Exception e) {
			}

			try {
				contact.put(Fil.NUMERO_ROUTE,
						row.getCell(colonnes.get("Numero_route (62)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.ETAT_LIAISON_FIL,
						row.getCell(colonnes.get("Etat_liaison_fil (3)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.NUMERO_REVISION_FIL, Float.parseFloat(row
						.getCell(colonnes.get("Numero_revision_fil (5)"))
						.toString()));
			} catch (Exception e) {

			}
			try {
				if (row.getCell(colonnes.get("Fil_sensible (66)")).toString()
						.equals("X")) {
					contact.put(Fil.FIL_SENSIBLE, true);
				}
			} catch (Exception e) {
				contact.put(Fil.FIL_SENSIBLE, false);
			}
			try {
				contact.put(Fil.NUMERO_FIL_CABLE,
						row.getCell(colonnes.get("Numero_fil_cable (13)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.NORME_CABLE,
						row.getCell(colonnes.get("Norme_cable (72)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.TYPE_FIL_CABLE,
						row.getCell(colonnes.get("Type_fil_cable (15)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.LONGUEUR_FIL_CABLE, Float.parseFloat(row
						.getCell(colonnes.get("Longueur_fil_cable (16)"))
						.toString()));
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.NUMERO_FIL_DANS_CABLE,
						row.getCell(colonnes.get("Numero_fil_dans_cable (54)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.COULEUR_FIL,
						row.getCell(colonnes.get("Couleur_fil (34)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.ORDRE_REALISATION,
						row.getCell(colonnes.get("Ordre de réalisation (24)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.REPERE_ELECTRIQUE_TENANT,
						row.getCell(
								colonnes.get("Repère electrique tenant (8)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.NUMERO_COMPOSANT_TENANT,
						row.getCell(
								colonnes.get("Numéro de composant tenant (9)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.NUMERO_BORNE_TENANT, Float.parseFloat(row
						.getCell(colonnes.get("Numero_borne_tenant (43)"))
						.toString()));
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.TYPE_RACCORDEMENT_TENANT,
						row.getCell(
								colonnes.get("Type_raccordement_tenant (45)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.REPRISE_BLINDAGE,
						row.getCell(colonnes.get("Reprise_blindage (37)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.SANS_REPRISE_BLINDAGE,
						row.getCell(colonnes.get("Sans_Reprise_blindage (83)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.REPERE_ELECTRIQUE_ABOUTISSANT,
						row.getCell(
								colonnes.get("Repère electrique aboutissant (11)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.NUMERO_COMPOSANT_ABOUTISSANT,
						row.getCell(
								colonnes.get("Numéro de composant aboutissant (12)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.NUMERO_BORNE_ABOUTISSANT,
						row.getCell(
								colonnes.get("Numero_Borne_aboutissant (48)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.TYPE_RACCORDEMENT_ABOUTISSANT,
						row.getCell(
								colonnes.get("Type_raccordement_aboutissant (50)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.TYPE_ELEMENT_RACCORDE,
						row.getCell(colonnes.get("Type_element_raccorde (46)"))
								.toString());
			} catch (Exception e) {
			}

			try {
				contact.put(Fil.REFERENCE_FABRICANT2,
						row.getCell(colonnes.get("Reference_fabricant (18)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.REFERENCE_INTERNE,
						row.getCell(colonnes.get("Reference_interne (19)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.FICHE_INSTRUCTION,
						row.getCell(colonnes.get("Fiche_instruction (25)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.REFERENCE_CONFIGURATION_SERTISSAGE,
						row.getCell(
								colonnes.get("reference_configuration_sertissage (26)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.ACCESSOIRE_COMPOSANT1,
						row.getCell(colonnes.get("Accessoire_composant_1 (42)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.ACCESSOIRE_COMPOSANT2,
						row.getCell(colonnes.get("Accessoire_composant_2 (44)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.REFERENCE_OUTIL_TENANT,
						row.getCell(colonnes.get("Référence_outil_tenant (27)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.REFERENCE_ACCESSOIRE_OUTIL_TENANT,
						row.getCell(
								colonnes.get("Référence_accessoire_outil_tenant (28)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Fil.REGLAGE_OUTIL_TENANT,
						row.getCell(colonnes.get("Reglage_outil_tenant (29)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.REFERENCE_OUTIL_ABOUTISSANT,
						row.getCell(
								colonnes.get("Référence_outil_aboutissant (30)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT,
						row.getCell(
								colonnes.get("Référence_accessoire_outil_aboutissant (31)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.REGLAGE_OUTIL_ABOUTISSANT,
						row.getCell(
								colonnes.get("Reglage_outil_aboutissant (32)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				if (row.getCell(colonnes.get("Obturateur (39)")).toString()
						.equals("X")) {
					contact.put(Fil.OBTURATEUR, true);
				}
			} catch (Exception e) {
				contact.put(Fil.OBTURATEUR, false);
			}
			try {
				if (row.getCell(colonnes.get("Faux-contact (40)")).toString()
						.equals("X")) {
					contact.put(Fil.FAUX_CONTACT, true);
				}
			} catch (Exception e) {
				contact.put(Fil.FAUX_CONTACT, false);
			}
			try {
				contact.put(
						Fil.ETAT_FINALISATION_PRISE,
						row.getCell(
								colonnes.get("Etat_finalisation_prise (64)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Fil.ORIENTATION_RACCORD_ARRIERE,
						row.getCell(
								colonnes.get("Orientation_raccord_arriere (65)"))
								.toString());
			} catch (Exception e) {
			}

			// Ajout de l'entité
			getContentResolver().insert(urlProduction, contact);
			// Ecrasement de ses données pour passer à la suivante
			contact.clear();

		}

	}

	/**
	 * Lecture du fichier Excel bd_nomenclature.xls et ajout des lignes
	 * correspondantes à la bases de données Nomenclature
	 * 
	 * @throws IOException
	 */
	private void insertRecordsNomenclature() throws IOException {
		getContentResolver().delete(urlNomenclature, Cable._id + "!='null'",
				null);
		// Création d'un InputStream vers le fichier Excel
		String nomFichier = null;
		File data = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ "Android"
				+ File.separator + "data" + File.separator + getPackageName());

		for (String file : data.list()) {
			Log.d("Nom Fichier", file);
			if (file.contains("nomenclature")) {
				nomFichier = file;
				Log.e("Nom Fichier", nomFichier);
			}
		}

		InputStream input = new FileInputStream(Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator
				+ "Android"
				+ File.separator
				+ "data"
				+ File.separator
				+ getPackageName()
				+ File.separator
				+ nomFichier);
		// Interpretation du fichier a l'aide de Apache POI
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
			Log.e("Colones", row.getCell(i).toString());

		}
		ContentValues contact = new ContentValues();
		// Parcours des lignes
		while (rows.hasNext()) {
			row = (HSSFRow) rows.next();
			// Ajout des données correspondantes
			// Les nombreux try/catch permettent d'éviter des
			// NullPointerException causées par les cellules vides
			try {
				contact.put(Cable.DESIGNATION_PRODUIT,
						row.getCell(colonnes.get("Designation_produit (1)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Cable.REFERENCE_FICHIER_SOURCE,
						row.getCell(
								colonnes.get("Reference_fichier_source (2)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Cable.NUMERO_REVISION_HARNAIS, Float.parseFloat(row
						.getCell(colonnes.get("Numero_revision_harnais (4)"))
						.toString()));
			} catch (Exception e) {
			}
			try {
				contact.put(Cable.NUMERO_HARNAIS_FAISCEAUX, Float
						.parseFloat(row.getCell(
								colonnes.get("Numero_harnais_faisceaux (6)"))
								.toString()));
			} catch (Exception e) {
			}
			try {
				contact.put(Cable.REFERENCE_FABRICANT1,
						row.getCell(colonnes.get("Reference_fabricant (17)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Cable.STANDARD,
						Float.parseFloat(row.getCell(
								colonnes.get("Standard (21)")).toString()));
			} catch (Exception e) {
			}
			try {
				contact.put(Cable.EQUIPEMENT,
						row.getCell(colonnes.get("Equipement (7)")).toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Cable.REPERE_ELECTRIQUE,
						row.getCell(colonnes.get("Repère_electrique (8 & 11)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Cable.NUMERO_COMPOSANT,
						row.getCell(colonnes.get("Numéro_composant (9 & 12)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Cable.NORME_CABLE,
						row.getCell(colonnes.get("Norme_cable (72)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Cable.ACCESSOIRE_CABLAGE,
						row.getCell(
								colonnes.get("Accessoire_cablage (49 & 63 & 81 & 82)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(
						Cable.ACCESSOIRE_COMPOSANT,
						row.getCell(
								colonnes.get("Accessoire_composant (37 & 42 & 44)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Cable.DESIGNATION_COMPOSANT,
						row.getCell(colonnes.get("Designation_composant (80)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Cable.FAMILLE_PRODUIT,
						row.getCell(colonnes.get("Famille_produit (71)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Cable.REFERENCE_FABRICANT2,
						row.getCell(colonnes.get(" Reference_fabricant (18)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Cable.FOURNISSEUR_FABRICANT,
						row.getCell(colonnes.get("Fournisseur_Fabricant (73)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				contact.put(Cable.REFERENCE_INTERNE,
						row.getCell(colonnes.get("Reference_interne (19)"))
								.toString());
			} catch (Exception e) {
			}
			try {
				if (row.getCell(colonnes.get("Reference_imposee (76)"))
						.toString().equals("X")) {
					contact.put(Cable.REFERENCE_IMPOSEE, true);
				}

			} catch (Exception e) {
				contact.put(Cable.REFERENCE_IMPOSEE, false);
			}
			try {
				contact.put(
						Cable.QUANTITE,
						Float.parseFloat(row.getCell(
								colonnes.get("Quantite (70 & 74)")).toString()));
			} catch (Exception e) {
			}
			try {
				contact.put(Cable.UNITE, row
						.getCell(colonnes.get("Unite (78)")).toString());
			} catch (Exception e) {
			}

			// Ajout de l'entité
			getContentResolver().insert(urlNomenclature, contact);
			// Ecrasement de ses données pour passer à la suivante
			contact.clear();

		}

	}

	/**
	 * Genération du fichier débit cables
	 * 
	 * @return réussite de l'opération
	 * @throws IOException
	 */

	private boolean genererDebitCable() throws IOException {

		String colKitGen1[] = new String[] { Kitting._id, Kitting.NUMERO_DEBIT,
				Kitting.NUMERO_POSITION_CHARIOT, Kitting.NUMERO_COMPOSANT,
				Kitting.REPERE_ELECTRIQUE, Kitting.ORDRE_REALISATION,
				Kitting.ETAT_LIAISON_FIL, Kitting.NUMERO_FIL_CABLE,
				Kitting.NUMERO_REVISION_FIL, Kitting.DESIGNATION_COMPOSANT,
				Kitting.TYPE_FIL_CABLE, Kitting.LONGUEUR_FIL_CABLE,
				Kitting.UNITE, Kitting.REFERENCE_FABRICANT1,
				Kitting.REFERENCE_FABRICANT2, Kitting.REFERENCE_INTERNE,
				Kitting.FOURNISSEUR_FABRICANT, Kitting.NUMERO_LOT_SCANNE,
				Kitting.REFERENCE_FABRICANT_SCANNE };

		String colKitGen2[] = new String[] { Kitting._id,
				Kitting.NUMERO_POSITION_CHARIOT, Kitting.NUMERO_COMPOSANT,
				Kitting.REPERE_ELECTRIQUE, Kitting.ORDRE_REALISATION,
				Kitting.NUMERO_FIL_CABLE, Kitting.TYPE_FIL_CABLE,
				Kitting.REFERENCE_FABRICANT2, Kitting.REFERENCE_INTERNE };

		// Création de la feuille et du Workbook
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Débit");

		int columnIndex = 0;

		// Génération des en têtes
		HSSFRow row = sheet.createRow(0);
		for (int i = 0; i < colKitGen1.length; i++) {
			row.createCell((short) columnIndex++).setCellValue(colKitGen1[i]);
		}

		// Lecture et écriture de la première feuille
		cursor = cr.query(urlKitting, colKitGen1, null, null, Kitting._id);
		if (cursor.moveToFirst()) {
			do {
				columnIndex = 0;
				row = sheet.createRow((short) cursor.getPosition() + 1);
				for (int i = 0; i < colKitGen1.length; i++) {
					row.createCell((short) columnIndex++).setCellValue(
							cursor.getString(cursor
									.getColumnIndex(colKitGen1[i])));
				}

			} while (cursor.moveToNext());

		} else {
			Log.e("debit", "Problème curseur");
			return false;
		}

		// Création de la deuxième feuille
		sheet = wb.createSheet("Regroupement");
		wb.getSheetAt(1);

		columnIndex = 0;

		// Génération des en têtes
		row = sheet.createRow(0);
		for (int i = 0; i < colKitGen2.length; i++) {
			row.createCell((short) columnIndex++).setCellValue(colKitGen2[i]);
		}

		// Lecture et écriture de la deuxième feuille
		cursor = cr.query(urlKitting, colKitGen2, null, null, Kitting._id);
		if (cursor.moveToFirst()) {
			do {
				columnIndex = 0;
				row = sheet.createRow((short) cursor.getPosition() + 1);
				for (int i = 0; i < colKitGen2.length; i++) {
					row.createCell((short) columnIndex++).setCellValue(
							cursor.getString(cursor
									.getColumnIndex(colKitGen2[i])));
				}

			} while (cursor.moveToNext());

		} else {
			Log.e("debit", "Problème curseur");
			return false;
		}
		try {
			FileOutputStream fileOut;
			File debit = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ File.separator
					+ "Android"
					+ File.separator
					+ "data"
					+ File.separator
					+ getPackageName(), "debitCables.xls");

			fileOut = new FileOutputStream(debit);

			wb.write(fileOut);

			fileOut.close();
		} catch (Exception e) {
			Log.e("Debit", " " + e);
		}

		return true;
	}

	/**
	 * Génération du fichier kitting têtes
	 * 
	 * @return réussite de l'opération
	 * @throws IOException
	 */

	private boolean genererKittingTetes() throws IOException {
		String colBOMGen1[] = new String[] { BOM._id, BOM.NUMERO_DEBIT,
				BOM.NUMERO_SECTION_CHEMINEMENT, BOM.NUMERO_POSITION_CHARIOT,
				BOM.EQUIPEMENT, BOM.NUMERO_COMPOSANT,
				BOM.REPERE_ELECTRIQUE_TENANT, BOM.ORDRE_REALISATION,
				BOM.ACCESSOIRE_CABLAGE, BOM.ACCESSOIRE_COMPOSANT,
				BOM.DESIGNATION_COMPOSANT, BOM.FAMILLE_PRODUIT,
				BOM.REFERENCE_FABRICANT2, BOM.FOURNISSEUR_FABRICANT,
				BOM.REFERENCE_INTERNE, BOM.UNITE, BOM.QUANTITE,
				BOM.REFERENCE_FABRICANT_SCANNE, BOM.NUMERO_LOT_SCANNE };

		String colBOMGen2[] = new String[] { BOM._id, BOM.NUMERO_CHEMINEMENT,
				BOM.NUMERO_SECTION_CHEMINEMENT, BOM.NUMERO_POSITION_CHARIOT,
				BOM.NUMERO_COMPOSANT, BOM.REPERE_ELECTRIQUE_TENANT,
				BOM.ORDRE_REALISATION, BOM.ACCESSOIRE_CABLAGE,
				BOM.ACCESSOIRE_COMPOSANT, BOM.DESIGNATION_COMPOSANT,
				BOM.FAMILLE_PRODUIT, BOM.REFERENCE_FABRICANT2,
				BOM.FOURNISSEUR_FABRICANT, BOM.REFERENCE_INTERNE, BOM.UNITE,
				BOM.QUANTITE };

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Débit");

		int columnIndex = 0;

		// Génération des en têtes
		HSSFRow row = sheet.createRow(0);
		for (int i = 0; i < colBOMGen1.length; i++) {
			row.createCell((short) columnIndex++).setCellValue(colBOMGen1[i]);
		}

		cursor = cr.query(urlBOM, colBOMGen1, null, null, BOM._id);
		if (cursor.moveToFirst()) {
			do {
				columnIndex = 0;
				row = sheet.createRow((short) cursor.getPosition() + 1);
				for (int i = 0; i < colBOMGen1.length; i++) {
					row.createCell((short) columnIndex++).setCellValue(
							cursor.getString(cursor
									.getColumnIndex(colBOMGen1[i])));
				}

			} while (cursor.moveToNext());

		} else {
			Log.e("debit", "Problème curseur");
			return false;
		}

		sheet = wb.createSheet("Regroupement");
		wb.getSheetAt(1);

		columnIndex = 0;

		// Génération des en têtes
		row = sheet.createRow(0);
		for (int i = 0; i < colBOMGen2.length; i++) {
			row.createCell((short) columnIndex++).setCellValue(colBOMGen2[i]);
		}

		cursor = cr.query(urlBOM, colBOMGen2, null, null, BOM._id);
		if (cursor.moveToFirst()) {
			do {
				columnIndex = 0;
				row = sheet.createRow((short) cursor.getPosition() + 1);
				for (int i = 0; i < colBOMGen2.length; i++) {
					row.createCell((short) columnIndex++).setCellValue(
							cursor.getString(cursor
									.getColumnIndex(colBOMGen2[i])));
				}

			} while (cursor.moveToNext());

		} else {
			Log.e("debit", "Problème curseur");
			return false;
		}
		try {
			FileOutputStream fileOut;
			File debit = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ File.separator
					+ "Android"
					+ File.separator
					+ "data"
					+ File.separator
					+ getPackageName(), "kittingTetes.xls");

			fileOut = new FileOutputStream(debit);

			wb.write(fileOut);

			fileOut.close();
		} catch (Exception e) {
			Log.e("Debit", " " + e);
		}

		return true;

	}

	/**
	 * Génération des opérations de la table de séquencement pour la productione
	 * et le controle
	 * 
	 */
	private void generationTableSequencement() {

		// Initialisation des indices des numéros d'opérations
		ContentValues contact = new ContentValues();
		indice = 1;
		indiceFrettage = 1;
		indiceCheminement = 1;
		indiceControleJalons = 1;
		indiceControleFinal = 1;
		indiceControleCheminement = 1;
		int FIL_SENSIBLE = 1;

		String numeroComposant;

		/* TETES A */

		// Filtre par connecteur et N° cheminement
		clause = Raccordement.ORDRE_REALISATION + " LIKE '%A%' AND "
				+ Raccordement.TYPE_ELEMENT_RACCORDE + " LIKE 'CNT' AND ("
				+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " IS NULL OR "
				+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
				+ " LIKE '' ) GROUP BY " + Raccordement.NUMERO_COMPOSANT_TENANT;
		cursor = cr.query(urlRaccordement, colRac, clause, null,
				Raccordement.NUMERO_SECTION_CHEMINEMENT);
		if (cursor.moveToFirst()) {
			do {
				numeroComposant = cursor.getString(cursor
						.getColumnIndex(Raccordement.NUMERO_COMPOSANT_TENANT));

				rang = cursor.getString(cursor
						.getColumnIndex(Raccordement.ZONE_ACTIVITE));

				rang_1 = "Connecteur " + numeroComposant;

				gamme = "Raccordement tête A";
				numeroOperation = "4-000";

				// Recherche des obturateurs
				clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='"
						+ numeroComposant + "' AND " + Raccordement.OBTURATEUR
						+ "='" + 1 + "'";
				cursorA = cr.query(urlRaccordement, colRac, clause, null,
						Raccordement._id);
				if (cursorA.moveToFirst()) {
					do {
						descriptionOperation = "Préparation Tête A  "
								+ cursorA
										.getString(cursorA
												.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT));

						// Ajout des opérations à la table de séquencement
						num1 = numeroOperation + Integer.toString(indice++);
						contact.put(Operation.GAMME, gamme);
						contact.put(Operation.RANG_1, rang);
						contact.put(Operation.RANG_1_1, rang_1);
						contact.put(Operation.DESCRIPTION_OPERATION,
								descriptionOperation);
						contact.put(Operation.NUMERO_OPERATION, num1);
						contact.put(Operation.REALISABLE, 1);
						contact.put(
								Operation.RANG_1_1_1,
								cursorA.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));
						contact.put(Operation.DUREE_THEORIQUE,
								ENFICHAGE_FC_OBTURATEURS);

						// Ajout de l'entité
						getContentResolver().insert(urlSequencement, contact);
						// Ecrasement de ses données pour passer à la suivante
						contact.clear();

						contact.put(Raccordement.NUMERO_OPERATION, num1);
						cr.update(
								urlRaccordement,
								contact,
								Raccordement._id
										+ "='"
										+ cursorA.getInt(cursorA
												.getColumnIndex(Raccordement._id))
										+ "'", null);

						contact.clear();

					} while (cursorA.moveToNext());
				}

				// Recherche des faux contacts
				clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='"
						+ numeroComposant + "' AND "
						+ Raccordement.FAUX_CONTACT + "='" + 1 + "'";
				cursorA = cr.query(urlRaccordement, colRac, clause, null,
						Raccordement._id);
				if (cursorA.moveToFirst()) {
					do {
						descriptionOperation = "Préparation Tête A  "
								+ cursorA
										.getString(cursorA
												.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT));

						// Ajout des opérations à la table de séquencement
						num1 = numeroOperation + indice++;
						contact.put(Operation.GAMME, gamme);
						contact.put(Operation.RANG_1, rang);
						contact.put(Operation.RANG_1_1, rang_1);
						contact.put(Operation.DESCRIPTION_OPERATION,
								descriptionOperation);
						contact.put(Operation.NUMERO_OPERATION, num1);
						contact.put(Operation.REALISABLE, 1);
						contact.put(Operation.DUREE_THEORIQUE,
								ENFICHAGE_FC_OBTURATEURS);
						contact.put(
								Operation.RANG_1_1_1,
								cursorA.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

						// Ajout de l'entité
						getContentResolver().insert(urlSequencement, contact);
						// Ecrasement de ses données pour passer à la suivante
						contact.clear();

						contact.put(Raccordement.NUMERO_OPERATION, num1);
						cr.update(
								urlRaccordement,
								contact,
								Raccordement._id
										+ "='"
										+ cursorA.getInt(cursorA
												.getColumnIndex(Raccordement._id))
										+ "'", null);

						contact.clear();

					} while (cursorA.moveToNext());
				}

				// Recherche des reprises blindages
				clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='"
						+ numeroComposant + "' AND ("
						+ Raccordement.REPRISE_BLINDAGE + "!='" + "null"
						+ "' AND " + Raccordement.REPRISE_BLINDAGE
						+ " !='' ) GROUP BY " + Raccordement.REPRISE_BLINDAGE;
				cursorA = cr.query(urlRaccordement, colRac, clause, null,
						Raccordement._id);
				if (cursorA.moveToFirst()) {
					do {
						descriptionOperation = "Reprise Blindage Tête A  "
								+ cursorA
										.getString(cursorA
												.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT));

						// Ajout des opérations à la table de séquencement
						num1 = numeroOperation + Integer.toString(indice++);
						contact.put(Operation.GAMME, gamme);
						contact.put(Operation.RANG_1, rang);
						contact.put(Operation.RANG_1_1, rang_1);
						contact.put(Operation.DESCRIPTION_OPERATION,
								descriptionOperation);
						contact.put(Operation.NUMERO_OPERATION, num1);
						contact.put(Operation.REALISABLE, 1);
						contact.put(Operation.DUREE_THEORIQUE, REPRISE_BLINDAGE);
						contact.put(
								Operation.RANG_1_1_1,
								cursorA.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

						// Ajout de l'entité
						getContentResolver().insert(urlSequencement, contact);
						// Ecrasement de ses données pour passer à la suivante
						contact.clear();

						contact.put(Raccordement.NUMERO_OPERATION, num1);
						cr.update(
								urlRaccordement,
								contact,
								Raccordement._id
										+ "='"
										+ cursorA.getInt(cursorA
												.getColumnIndex(Raccordement._id))
										+ "'", null);

						contact.clear();

					} while (cursorA.moveToNext());
				}

				// Ajout des autres cables
				clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='"
						+ numeroComposant + "' AND  "
						+ Raccordement.NUMERO_OPERATION + " IS NULL GROUP BY "
						+ Raccordement.NUMERO_FIL_CABLE;
				cursorA = cr.query(urlRaccordement, colRac, clause, null, null);

				if (cursorA.moveToFirst()) {
					String refFabricant = cursorA.getString(cursorA
							.getColumnIndex(Raccordement.REFERENCE_FABRICANT2));
					// Verifications de la sensibilité des fils
					if (cursorA.getInt(cursorA
							.getColumnIndex(Raccordement.FIL_SENSIBLE)) == 1) {

						FIL_SENSIBLE = 0;

						// CONTROLE FIL SENSIBLE
						gamme = "Contrôle jalons";
						numeroOperation = "9-000";

						// Controle finalisation
						descriptionOperation = "Contrôle sertissage tête A "
								+ cursorA
										.getString(cursorA
												.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT));

						// Ajout des opérations à la table de séquencement
						num1 = numeroOperation
								+ Integer.toString(indiceControleJalons++);
						contact.put(Operation.GAMME, gamme);
						contact.put(Operation.RANG_1, rang);
						contact.put(Operation.RANG_1_1, rang_1);
						contact.put(Operation.DESCRIPTION_OPERATION,
								descriptionOperation);
						contact.put(Operation.NUMERO_OPERATION, num1);
						contact.put(Operation.REALISABLE, 0);
						contact.put(Operation.DUREE_THEORIQUE,
								SERTISSAGE_CONTACTS);

						// Ajout de l'entité
						getContentResolver().insert(urlSequencement, contact);
						// Ecrasement de ses données pour passer à la
						// suivante
						contact.clear();

						// Controle finalisation
						descriptionOperation = "Contrôle rétention tête A "
								+ cursorA
										.getString(cursorA
												.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT));

						// Ajout des opérations à la table de séquencement
						num1 = numeroOperation
								+ Integer.toString(indiceControleJalons++);
						contact.put(Operation.GAMME, gamme);
						contact.put(Operation.RANG_1, rang);
						contact.put(Operation.RANG_1_1, rang_1);
						contact.put(Operation.DESCRIPTION_OPERATION,
								descriptionOperation);
						contact.put(Operation.NUMERO_OPERATION, num1);
						contact.put(Operation.REALISABLE, 0);
						contact.put(Operation.DUREE_THEORIQUE,
								SERTISSAGE_CONTACTS);

						// Ajout de l'entité
						getContentResolver().insert(urlSequencement, contact);
						// Ecrasement de ses données pour passer à la
						// suivante
						contact.clear();

						gamme = "Raccordement tête A";
						numeroOperation = "4-000";

						do {
							// Dénudage Sertissage
							descriptionOperation = "Denudage Sertissage de contact "
									+ refFabricant
									+ " sur  Tête A  "
									+ cursorA
											.getString(cursorA
													.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT));

							// Ajout des opérations à la table de séquencement
							num1 = numeroOperation + Integer.toString(indice++);
							contact.put(Operation.GAMME, gamme);
							contact.put(Operation.RANG_1, rang);
							contact.put(Operation.RANG_1_1, rang_1);
							contact.put(Operation.DESCRIPTION_OPERATION,
									descriptionOperation);
							contact.put(Operation.NUMERO_OPERATION, num1);
							contact.put(Operation.REALISABLE, 1);
							contact.put(Operation.DUREE_THEORIQUE,
									SERTISSAGE_CONTACTS);
							contact.put(
									Operation.RANG_1_1_1,
									cursorA.getString(cursorA
											.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

							// Ajout de l'entité
							getContentResolver().insert(urlSequencement,
									contact);
							// Ecrasement de ses données pour passer à la
							// suivante
							contact.clear();

							contact.put(Raccordement.NUMERO_OPERATION, num1);
							cr.update(
									urlRaccordement,
									contact,
									Raccordement._id
											+ "='"
											+ cursorA.getInt(cursorA
													.getColumnIndex(Raccordement._id))
											+ "'", null);

							contact.clear();

						} while (cursorA.moveToNext());
						cursorA.moveToFirst();
						do {

							// Enfichage
							descriptionOperation = "Enfichage Tête A  "
									+ cursorA
											.getString(cursorA
													.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT));

							// Ajout des opérations à la table de séquencement
							num1 = numeroOperation + Integer.toString(indice++);
							contact.put(Operation.GAMME, gamme);
							contact.put(Operation.RANG_1, rang);
							contact.put(Operation.RANG_1_1, rang_1);
							contact.put(Operation.DESCRIPTION_OPERATION,
									descriptionOperation);
							contact.put(Operation.NUMERO_OPERATION, num1);
							contact.put(Operation.REALISABLE, 0);
							contact.put(Operation.DUREE_THEORIQUE,
									ENFICHAGE_FC_OBTURATEURS);
							contact.put(
									Operation.RANG_1_1_1,
									cursorA.getString(cursorA
											.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

							// Ajout de l'entité
							getContentResolver().insert(urlSequencement,
									contact);
							// Ecrasement de ses données pour passer à la
							// suivante
							contact.clear();

						} while (cursorA.moveToNext());

					} else {

						do {
							// Dénudage sertissage enfichage
							descriptionOperation = "Denudage Sertissage Enfichage de contact "
									+ refFabricant
									+ " sur  Tête A  "
									+ cursorA
											.getString(cursorA
													.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT));

							// Ajout des opérations à la table de séquencement
							num1 = numeroOperation + Integer.toString(indice++);
							contact.put(Operation.GAMME, gamme);
							contact.put(Operation.RANG_1, rang);
							contact.put(Operation.RANG_1_1, rang_1);
							contact.put(Operation.DESCRIPTION_OPERATION,
									descriptionOperation);
							contact.put(Operation.NUMERO_OPERATION, num1);
							contact.put(Operation.REALISABLE, 1);
							contact.put(Operation.DUREE_THEORIQUE,
									SERTISSAGE_CONTACTS);
							contact.put(
									Operation.RANG_1_1_1,
									cursorA.getString(cursorA
											.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

							// Ajout de l'entité
							getContentResolver().insert(urlSequencement,
									contact);
							// Ecrasement de ses données pour passer à la
							// suivante
							contact.clear();

							contact.put(Raccordement.NUMERO_OPERATION, num1);
							cr.update(
									urlRaccordement,
									contact,
									Raccordement._id
											+ "='"
											+ cursorA.getInt(cursorA
													.getColumnIndex(Raccordement._id))
											+ "'", null);

							contact.clear();

						} while (cursorA.moveToNext());
					}

				}

				// Finalisation têtes
				descriptionOperation = "Finalisation Tête A  "
						+ cursor.getString(cursor
								.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT));

				// Ajout des opérations à la table de séquencement
				num1 = numeroOperation + Integer.toString(indice++);
				contact.put(Operation.GAMME, gamme);
				contact.put(Operation.RANG_1, rang);
				contact.put(Operation.RANG_1_1, rang_1);
				contact.put(Operation.DESCRIPTION_OPERATION,
						descriptionOperation);
				contact.put(Operation.NUMERO_OPERATION, num1);
				contact.put(Operation.REALISABLE, FIL_SENSIBLE);
				contact.put(Operation.DUREE_THEORIQUE, SERTISSAGE_CONTACTS);

				// Ajout de l'entité
				getContentResolver().insert(urlSequencement, contact);
				// Ecrasement de ses données pour passer à la
				// suivante
				contact.clear();

				// Tri aboutissants
				descriptionOperation = "Tri des aboutissants Tête A  "
						+ cursor.getString(cursor
								.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT));

				// Ajout des opérations à la table de séquencement
				num1 = numeroOperation + Integer.toString(indice++);
				contact.put(Operation.GAMME, gamme);
				contact.put(Operation.RANG_1, rang);
				contact.put(Operation.RANG_1_1, rang_1);
				contact.put(Operation.DESCRIPTION_OPERATION,
						descriptionOperation);
				contact.put(Operation.NUMERO_OPERATION, num1);
				contact.put(Operation.REALISABLE, FIL_SENSIBLE);
				contact.put(Operation.DUREE_THEORIQUE, SERTISSAGE_CONTACTS);

				// Ajout de l'entité
				getContentResolver().insert(urlSequencement, contact);
				// Ecrasement de ses données pour passer à la
				// suivante
				contact.clear();

				gamme = "Cheminement";
				numeroOperation = "5-000";

				// Position sur la table de cheminement
				descriptionOperation = "Positionnement Tête A "
						+ cursor.getString(cursor
								.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT));

				// Ajout des opérations à la table de séquencement
				num1 = numeroOperation + Integer.toString(indiceCheminement++);
				contact.put(Operation.GAMME, gamme);
				contact.put(Operation.RANG_1, rang);
				contact.put(Operation.RANG_1_1, rang_1);
				contact.put(Operation.DESCRIPTION_OPERATION,
						descriptionOperation);
				contact.put(Operation.NUMERO_OPERATION, num1);
				contact.put(Operation.REALISABLE, 0);
				contact.put(Operation.DUREE_THEORIQUE, SERTISSAGE_CONTACTS);

				// Ajout de l'entité
				getContentResolver().insert(urlSequencement, contact);
				// Ecrasement de ses données pour passer à la
				// suivante
				contact.clear();

				// Cheminement des têtes A
				clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='"
						+ numeroComposant + "'AND  "
						+ Raccordement.ORDRE_REALISATION + " LIKE '%A%' AND "
						+ Raccordement.FAUX_CONTACT + "='" + 0 + "' AND "
						+ Raccordement.OBTURATEUR + "='" + 0 + "' AND "
						+ Raccordement.REPRISE_BLINDAGE + " IS NULL ";
				cursorA = cr.query(urlRaccordement, colRac, clause
						+ " GROUP BY " + Raccordement.NUMERO_FIL_CABLE, null,
						Raccordement._id);
				if (cursorA.moveToFirst()) {
					do {
						descriptionOperation = "Cheminement Tête A  "
								+ cursorA
										.getString(cursorA
												.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT));

						// Ajout des opérations à la table de séquencement
						num1 = numeroOperation
								+ Integer.toString(indiceCheminement++);
						contact.put(Operation.GAMME, gamme);
						contact.put(Operation.RANG_1, rang);
						contact.put(Operation.RANG_1_1, rang_1);
						contact.put(Operation.DESCRIPTION_OPERATION,
								descriptionOperation);
						contact.put(Operation.NUMERO_OPERATION, num1);
						contact.put(Operation.REALISABLE, 0);
						contact.put(Operation.DUREE_THEORIQUE,
								SERTISSAGE_CONTACTS);
						contact.put(
								Operation.RANG_1_1_1,
								cursorA.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

						// Ajout de l'entité
						getContentResolver().insert(urlSequencement, contact);
						// Ecrasement de ses données pour passer à la suivante
						contact.clear();

					} while (cursorA.moveToNext());
				}

				gamme = "Contrôle final";
				numeroOperation = "11-000";

				// Controle finalisation
				descriptionOperation = "Contrôle final tête A "
						+ cursor.getString(cursor
								.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT));

				// Ajout des opérations à la table de séquencement
				num1 = numeroOperation
						+ Integer.toString(indiceControleFinal++);
				contact.put(Operation.GAMME, gamme);
				contact.put(Operation.RANG_1, rang);
				contact.put(Operation.RANG_1_1, rang_1);
				contact.put(Operation.DESCRIPTION_OPERATION,
						descriptionOperation);
				contact.put(Operation.NUMERO_OPERATION, num1);
				contact.put(Operation.REALISABLE, 0);
				contact.put(Operation.DUREE_THEORIQUE, SERTISSAGE_CONTACTS);

				// Ajout de l'entité
				getContentResolver().insert(urlSequencement, contact);
				// Ecrasement de ses données pour passer à la
				// suivante
				contact.clear();

				gamme = "Frettage";
				numeroOperation = "6-000";

				clause = Cable.FAMILLE_PRODUIT + " LIKE '%Frette%' AND "
						+ Cable.NUMERO_COMPOSANT + "='" + numeroComposant + "'";
				cursorA = cr.query(urlNomenclature, colNom1, clause, null,
						Cable._id);

				if (cursorA.moveToFirst()) {
					Log.i("Frette", numeroComposant);
					// Frettage
					descriptionOperation = "Frettage Zone  "
							+ cursor.getString(cursor
									.getColumnIndex(Raccordement.ZONE_ACTIVITE))
							+ "-"
							+ cursor.getString(cursor
									.getColumnIndex(Raccordement.LOCALISATION1));

					// Ajout des opérations à la table de séquencement
					num1 = numeroOperation + Integer.toString(indiceFrettage++);
					contact.put(Operation.GAMME, gamme);
					contact.put(Operation.RANG_1, rang);
					contact.put(Operation.RANG_1_1, rang_1);
					contact.put(Operation.DESCRIPTION_OPERATION,
							descriptionOperation);
					contact.put(Operation.NUMERO_OPERATION, num1);
					contact.put(Operation.REALISABLE, 0);
					contact.put(Operation.DUREE_THEORIQUE, FRETTAGE);

					// Ajout de l'entité
					getContentResolver().insert(urlSequencement, contact);
					// Ecrasement de ses données pour passer à la
					// suivante
					contact.clear();
				}

			} while (cursor.moveToNext());
		}

		/* TETES B */

		indice = 1;
		FIL_SENSIBLE = 1;

		// Filtre par connecteur et N° cheminement
		clause = Raccordement.ORDRE_REALISATION + " LIKE '%B%' AND "
				+ Raccordement.TYPE_ELEMENT_RACCORDE + " LIKE 'CNT' AND "
				+ Raccordement.NUMERO_COMPOSANT_TENANT + " IS NULL GROUP BY "
				+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT;
		cursor = cr.query(urlRaccordement, colRac, clause, null,
				Raccordement.NUMERO_CHEMINEMENT);
		if (cursor.moveToFirst()) {
			do {
				numeroComposant = cursor
						.getString(cursor
								.getColumnIndex(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT));

				gamme = "Raccordement Tête B";
				numeroOperation = "7-000";
				rang = cursor.getString(cursor
						.getColumnIndex(Raccordement.ZONE_ACTIVITE));

				rang_1 = "Connecteur " + numeroComposant;

				// Recherche des obturateurs
				clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
						+ numeroComposant + "' AND " + Raccordement.OBTURATEUR
						+ "='" + 1 + "'";
				cursorA = cr.query(urlRaccordement, colRac, clause, null,
						Raccordement._id);
				if (cursorA.moveToFirst()) {
					do {
						descriptionOperation = "Préparation Tête B  "
								+ cursorA
										.getString(cursorA
												.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));

						// Ajout des opérations à la table de séquencement
						num1 = numeroOperation + Integer.toString(indice++);
						contact.put(Operation.GAMME, gamme);
						contact.put(Operation.RANG_1, rang);
						contact.put(Operation.RANG_1_1, rang_1);
						contact.put(Operation.DESCRIPTION_OPERATION,
								descriptionOperation);
						contact.put(Operation.NUMERO_OPERATION, num1);
						contact.put(Operation.REALISABLE, 0);
						contact.put(Operation.DUREE_THEORIQUE,
								ENFICHAGE_FC_OBTURATEURS);
						contact.put(
								Operation.RANG_1_1_1,
								cursorA.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

						// Ajout de l'entité
						getContentResolver().insert(urlSequencement, contact);
						// Ecrasement de ses données pour passer à la suivante
						contact.clear();

						contact.put(Raccordement.NUMERO_OPERATION, num1);
						cr.update(
								urlRaccordement,
								contact,
								Raccordement._id
										+ "='"
										+ cursorA.getInt(cursorA
												.getColumnIndex(Raccordement._id))
										+ "'", null);

						contact.clear();

					} while (cursorA.moveToNext());
				}

				// Recherche des faux contacts
				clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
						+ numeroComposant + "' AND "
						+ Raccordement.FAUX_CONTACT + "='" + 1 + "'";
				cursorA = cr.query(urlRaccordement, colRac, clause, null,
						Raccordement._id);
				if (cursorA.moveToFirst()) {
					do {
						descriptionOperation = "Préparation Tête B  "
								+ cursorA
										.getString(cursorA
												.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));

						// Ajout des opérations à la table de séquencement
						num1 = numeroOperation + indice++;
						contact.put(Operation.GAMME, gamme);
						contact.put(Operation.RANG_1, rang);
						contact.put(Operation.RANG_1_1, rang_1);
						contact.put(Operation.DESCRIPTION_OPERATION,
								descriptionOperation);
						contact.put(Operation.NUMERO_OPERATION, num1);
						contact.put(Operation.REALISABLE, 0);
						contact.put(Operation.DUREE_THEORIQUE,
								ENFICHAGE_FC_OBTURATEURS);
						contact.put(
								Operation.RANG_1_1_1,
								cursorA.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

						// Ajout de l'entité
						getContentResolver().insert(urlSequencement, contact);
						// Ecrasement de ses données pour passer à la suivante
						contact.clear();

						contact.put(Raccordement.NUMERO_OPERATION, num1);
						cr.update(
								urlRaccordement,
								contact,
								Raccordement._id
										+ "='"
										+ cursorA.getInt(cursorA
												.getColumnIndex(Raccordement._id))
										+ "'", null);

						contact.clear();

					} while (cursorA.moveToNext());
				}

				// Mise à longueur
				descriptionOperation = "Mise à longueur Tête B  "
						+ cursor.getString(cursor
								.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));

				// Ajout des opérations à la table de séquencement
				num1 = numeroOperation + indice++;
				contact.put(Operation.GAMME, gamme);
				contact.put(Operation.RANG_1, rang);
				contact.put(Operation.RANG_1_1, rang_1);
				contact.put(Operation.DESCRIPTION_OPERATION,
						descriptionOperation);
				contact.put(Operation.NUMERO_OPERATION, num1);
				contact.put(Operation.REALISABLE, 0);
				contact.put(Operation.DUREE_THEORIQUE, ENFICHAGE_FC_OBTURATEURS);

				// Ajout de l'entité
				getContentResolver().insert(urlSequencement, contact);
				// Ecrasement de ses données pour passer à la suivante
				contact.clear();

				// Recherche des reprises blindages
				clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
						+ numeroComposant + "' AND "
						+ Raccordement.REPRISE_BLINDAGE + "!='" + "null"
						+ "' GROUP BY " + Raccordement.REPRISE_BLINDAGE;
				cursorA = cr.query(urlRaccordement, colRac, clause, null,
						Raccordement._id);
				if (cursorA.moveToFirst()) {
					do {
						descriptionOperation = "Reprise Blindage Tête B  "
								+ cursorA
										.getString(cursorA
												.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));

						// Ajout des opérations à la table de séquencement
						num1 = numeroOperation + Integer.toString(indice++);
						contact.put(Operation.GAMME, gamme);
						contact.put(Operation.RANG_1, rang);
						contact.put(Operation.RANG_1_1, rang_1);
						contact.put(Operation.DESCRIPTION_OPERATION,
								descriptionOperation);
						contact.put(Operation.NUMERO_OPERATION, num1);
						contact.put(Operation.REALISABLE, 0);
						contact.put(Operation.DUREE_THEORIQUE,
								ENFICHAGE_FC_OBTURATEURS);
						contact.put(
								Operation.RANG_1_1_1,
								cursorA.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

						// Ajout de l'entité
						getContentResolver().insert(urlSequencement, contact);
						// Ecrasement de ses données pour passer à la suivante
						contact.clear();

						contact.put(Raccordement.NUMERO_OPERATION, num1);
						cr.update(
								urlRaccordement,
								contact,
								Raccordement._id
										+ "='"
										+ cursorA.getInt(cursorA
												.getColumnIndex(Raccordement._id))
										+ "'", null);

						contact.clear();

					} while (cursorA.moveToNext());
				}

				// Ajout des autres cables
				clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
						+ numeroComposant + "' AND "
						+ Raccordement.TYPE_ELEMENT_RACCORDE
						+ " LIKE 'CNT' AND " + Raccordement.NUMERO_OPERATION
						+ " IS NULL GROUP BY " + Raccordement.NUMERO_FIL_CABLE;
				cursorA = cr.query(urlRaccordement, colRac, clause, null, null);

				if (cursorA.moveToFirst()) {
					String refFabricant = cursorA.getString(cursorA
							.getColumnIndex(Raccordement.REFERENCE_FABRICANT2));
					// Verifications de la sensibilité des fils
					if (cursorA.getInt(cursorA
							.getColumnIndex(Raccordement.FIL_SENSIBLE)) == 1) {

						FIL_SENSIBLE = 0;
						// CONTROLE FIL SENSIBLE
						gamme = "Contrôle jalons";
						numeroOperation = "9-000";

						// Controle finalisation
						descriptionOperation = "Contrôle sertissage tête B "
								+ cursorA
										.getString(cursorA
												.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));

						// Ajout des opérations à la table de séquencement
						num1 = numeroOperation
								+ Integer.toString(indiceControleJalons++);
						contact.put(Operation.GAMME, gamme);
						contact.put(Operation.RANG_1, rang);
						contact.put(Operation.RANG_1_1, rang_1);
						contact.put(Operation.DESCRIPTION_OPERATION,
								descriptionOperation);
						contact.put(Operation.NUMERO_OPERATION, num1);
						contact.put(Operation.REALISABLE, 0);
						contact.put(Operation.DUREE_THEORIQUE,
								SERTISSAGE_CONTACTS);

						// Ajout de l'entité
						getContentResolver().insert(urlSequencement, contact);
						// Ecrasement de ses données pour passer à la
						// suivante
						contact.clear();

						// Controle finalisation
						descriptionOperation = "Contrôle rétention tête B "
								+ cursorA
										.getString(cursorA
												.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));

						// Ajout des opérations à la table de séquencement
						num1 = numeroOperation
								+ Integer.toString(indiceControleJalons++);
						contact.put(Operation.GAMME, gamme);
						contact.put(Operation.RANG_1, rang);
						contact.put(Operation.RANG_1_1, rang_1);
						contact.put(Operation.DESCRIPTION_OPERATION,
								descriptionOperation);
						contact.put(Operation.NUMERO_OPERATION, num1);
						contact.put(Operation.REALISABLE, 0);
						contact.put(Operation.DUREE_THEORIQUE,
								SERTISSAGE_CONTACTS);

						// Ajout de l'entité
						getContentResolver().insert(urlSequencement, contact);
						// Ecrasement de ses données pour passer à la
						// suivante
						contact.clear();

						do {
							descriptionOperation = "Denudage Sertissage de contact "
									+ refFabricant
									+ " sur  Tête B  "
									+ cursorA
											.getString(cursorA
													.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));

							// Ajout des opérations à la table de séquencement
							num1 = numeroOperation + Integer.toString(indice++);
							contact.put(Operation.GAMME, gamme);
							contact.put(Operation.RANG_1, rang);
							contact.put(Operation.RANG_1_1, rang_1);
							contact.put(Operation.DESCRIPTION_OPERATION,
									descriptionOperation);
							contact.put(Operation.NUMERO_OPERATION, num1);
							contact.put(Operation.REALISABLE, 0);
							contact.put(Operation.DUREE_THEORIQUE,
									SERTISSAGE_CONTACTS);

							// Ajout de l'entité
							getContentResolver().insert(urlSequencement,
									contact);
							// Ecrasement de ses données pour passer à la
							// suivante
							contact.clear();

							contact.put(Raccordement.NUMERO_OPERATION, num1);
							cr.update(
									urlRaccordement,
									contact,
									Raccordement._id
											+ "='"
											+ cursorA.getInt(cursorA
													.getColumnIndex(Raccordement._id))
											+ "'", null);

							contact.clear();

							gamme = "Raccordement Tête B";
							numeroOperation = "7-000";
						} while (cursorA.moveToNext());
						cursorA.moveToFirst();
						do {

							descriptionOperation = "Enfichage Tête B  "
									+ cursorA
											.getString(cursorA
													.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));

							// Ajout des opérations à la table de séquencement
							num1 = numeroOperation + Integer.toString(indice++);
							contact.put(Operation.GAMME, gamme);
							contact.put(Operation.RANG_1, rang);
							contact.put(Operation.RANG_1_1, rang_1);
							contact.put(Operation.DESCRIPTION_OPERATION,
									descriptionOperation);
							contact.put(Operation.NUMERO_OPERATION, num1);
							contact.put(Operation.REALISABLE, 0);
							contact.put(Operation.DUREE_THEORIQUE,
									ENFICHAGE_FC_OBTURATEURS);
							contact.put(
									Operation.RANG_1_1_1,
									cursorA.getString(cursorA
											.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

							// Ajout de l'entité
							getContentResolver().insert(urlSequencement,
									contact);
							// Ecrasement de ses données pour passer à la
							// suivante
							contact.clear();

						} while (cursorA.moveToNext());

					} else {

						do {
							descriptionOperation = "Denudage Sertissage Enfichage de contact "
									+ refFabricant
									+ " sur  Tête B  "
									+ cursorA
											.getString(cursorA
													.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));

							// Ajout des opérations à la table de séquencement
							num1 = numeroOperation + Integer.toString(indice++);
							contact.put(Operation.GAMME, gamme);
							contact.put(Operation.RANG_1, rang);
							contact.put(Operation.RANG_1_1, rang_1);
							contact.put(Operation.DESCRIPTION_OPERATION,
									descriptionOperation);
							contact.put(Operation.NUMERO_OPERATION, num1);
							contact.put(Operation.REALISABLE, 0);
							contact.put(Operation.DUREE_THEORIQUE,
									SERTISSAGE_CONTACTS);
							contact.put(
									Operation.RANG_1_1_1,
									cursorA.getString(cursorA
											.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

							// Ajout de l'entité
							getContentResolver().insert(urlSequencement,
									contact);
							// Ecrasement de ses données pour passer à la
							// suivante
							contact.clear();

							contact.put(Raccordement.NUMERO_OPERATION, num1);
							cr.update(
									urlRaccordement,
									contact,
									Raccordement._id
											+ "='"
											+ cursorA.getInt(cursorA
													.getColumnIndex(Raccordement._id))
											+ "'", null);

							contact.clear();

						} while (cursorA.moveToNext());
					}

				}

				// Ajout des cosses manchons
				clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
						+ numeroComposant + "' AND ("
						+ Raccordement.TYPE_ELEMENT_RACCORDE
						+ " LIKE 'SPL' OR "
						+ Raccordement.TYPE_ELEMENT_RACCORDE
						+ " LIKE 'TER' ) GROUP BY "
						+ Raccordement.NUMERO_FIL_CABLE;
				cursorA = cr.query(urlRaccordement, colRac, clause, null,
						Raccordement._id);
				if (cursorA.moveToFirst()) {
					do {

						descriptionOperation = "Denudage Sertissage Cosse et Manchons "

								+ " sur  Tête B  "
								+ cursorA
										.getString(cursorA
												.getColumnIndex(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT));

						// Ajout des opérations à la table de séquencement
						num1 = numeroOperation + Integer.toString(indice++);
						contact.put(Operation.GAMME, gamme);
						contact.put(Operation.RANG_1, rang);
						contact.put(Operation.RANG_1_1, rang_1);
						contact.put(Operation.DESCRIPTION_OPERATION,
								descriptionOperation);
						contact.put(Operation.NUMERO_OPERATION, num1);
						contact.put(Operation.REALISABLE, 0);
						contact.put(Operation.DUREE_THEORIQUE,
								SERTISSAGE_CONTACTS);
						contact.put(
								Operation.RANG_1_1_1,
								cursorA.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

						// Ajout de l'entité
						getContentResolver().insert(urlSequencement, contact);
						// Ecrasement de ses données pour passer à la
						// suivante
						contact.clear();

					} while (cursorA.moveToNext());
				}

				// Finalisation têtes
				descriptionOperation = "Finalisation Tête B  "
						+ cursor.getString(cursor
								.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));

				// Ajout des opérations à la table de séquencement
				num1 = numeroOperation + Integer.toString(indice++);
				contact.put(Operation.GAMME, gamme);
				contact.put(Operation.RANG_1, rang);
				contact.put(Operation.RANG_1_1, rang_1);
				contact.put(Operation.DESCRIPTION_OPERATION,
						descriptionOperation);
				contact.put(Operation.NUMERO_OPERATION, num1);
				contact.put(Operation.REALISABLE, 0);
				contact.put(Operation.DUREE_THEORIQUE, SERTISSAGE_CONTACTS);

				// Ajout de l'entité
				getContentResolver().insert(urlSequencement, contact);
				// Ecrasement de ses données pour passer à la
				// suivante
				contact.clear();

				gamme = "Cheminement";
				numeroOperation = "5-000";

				// Cheminement des têtes B
				clause = Raccordement.NUMERO_COMPOSANT_TENANT + "='"
						+ numeroComposant + "' AND  "
						+ Raccordement.ORDRE_REALISATION + " LIKE '%B%' AND "
						+ Raccordement.FAUX_CONTACT + "='" + 0 + "' AND "
						+ Raccordement.OBTURATEUR + "='" + 0 + "' AND "
						+ Raccordement.REPRISE_BLINDAGE + " IS NULL ";
				cursorA = cr.query(urlRaccordement, colRac, clause
						+ " GROUP BY " + Raccordement.NUMERO_FIL_CABLE, null,
						Raccordement._id);
				if (cursorA.moveToFirst()) {
					do {
						descriptionOperation = "Cheminement Tête B/ Tête B  "
								+ cursorA
										.getString(cursorA
												.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT));

						// Ajout des opérations à la table de séquencement
						num1 = numeroOperation
								+ Integer.toString(indiceCheminement++);
						contact.put(Operation.GAMME, gamme);
						contact.put(Operation.RANG_1, rang);
						contact.put(Operation.RANG_1_1, rang_1);
						contact.put(Operation.DESCRIPTION_OPERATION,
								descriptionOperation);
						contact.put(Operation.NUMERO_OPERATION, num1);
						contact.put(Operation.REALISABLE, 0);
						contact.put(Operation.DUREE_THEORIQUE,
								SERTISSAGE_CONTACTS);
						contact.put(
								Operation.RANG_1_1_1,
								cursorA.getString(cursorA
										.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

						// Ajout de l'entité
						getContentResolver().insert(urlSequencement, contact);
						// Ecrasement de ses données pour passer à la suivante
						contact.clear();

					} while (cursorA.moveToNext());
				}

				gamme = "Contrôle final";
				numeroOperation = "11-000";

				// Controle finalisation
				descriptionOperation = "Contrôle final tête B "
						+ cursor.getString(cursor
								.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT));

				// Ajout des opérations à la table de séquencement
				num1 = numeroOperation
						+ Integer.toString(indiceControleFinal++);
				contact.put(Operation.GAMME, gamme);
				contact.put(Operation.RANG_1, rang);
				contact.put(Operation.RANG_1_1, rang_1);
				contact.put(Operation.DESCRIPTION_OPERATION,
						descriptionOperation);
				contact.put(Operation.NUMERO_OPERATION, num1);
				contact.put(Operation.REALISABLE, 0);
				contact.put(Operation.DUREE_THEORIQUE, SERTISSAGE_CONTACTS);

				// Ajout de l'entité
				getContentResolver().insert(urlSequencement, contact);
				// Ecrasement de ses données pour passer à la
				// suivante
				contact.clear();

			} while (cursor.moveToNext());

			// Tetes A cosses manchons

			clause = Raccordement.ORDRE_REALISATION + " LIKE '%A%' AND ("
					+ Raccordement.TYPE_ELEMENT_RACCORDE + " LIKE 'SPL' OR "
					+ Raccordement.TYPE_ELEMENT_RACCORDE
					+ " LIKE 'TER' ) GROUP BY "
					+ Raccordement.NUMERO_COMPOSANT_TENANT;
			cursor = cr.query(urlRaccordement, colRac, clause, null,
					Raccordement.NUMERO_CHEMINEMENT);
			if (cursor.moveToFirst()) {
				do {
					numeroComposant = cursor
							.getString(cursor
									.getColumnIndex(Raccordement.NUMERO_COMPOSANT_TENANT));

					rang = cursor.getString(cursor
							.getColumnIndex(Raccordement.ZONE_ACTIVITE));

					rang_1 = "Connecteur " + numeroComposant;
					gamme = "Raccordement Tête A";
					numeroOperation = "7-000";

					// Ajout des cosses manchons
					clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='"
							+ numeroComposant + "' AND ("
							+ Raccordement.TYPE_ELEMENT_RACCORDE
							+ " LIKE 'SPL' OR "
							+ Raccordement.TYPE_ELEMENT_RACCORDE
							+ " LIKE 'TER' ) GROUP BY "
							+ Raccordement.NUMERO_FIL_CABLE;
					cursorA = cr.query(urlRaccordement, colRac, clause, null,
							Raccordement._id);
					if (cursorA.moveToFirst()) {
						do {

							descriptionOperation = "Denudage Sertissage Cosse et Manchons "

									+ " sur  Tête A  "
									+ cursorA
											.getString(cursorA
													.getColumnIndex(Raccordement.NUMERO_COMPOSANT_TENANT));

							// Ajout des opérations à la table de séquencement
							num1 = numeroOperation + Integer.toString(indice++);
							contact.put(Operation.GAMME, gamme);
							contact.put(Operation.RANG_1, rang);
							contact.put(Operation.RANG_1_1, rang_1);
							contact.put(Operation.DESCRIPTION_OPERATION,
									descriptionOperation);
							contact.put(Operation.NUMERO_OPERATION, num1);
							contact.put(Operation.REALISABLE, 1);
							contact.put(Operation.DUREE_THEORIQUE,
									SERTISSAGE_CONTACTS);
							contact.put(
									Operation.RANG_1_1_1,
									cursorA.getString(cursorA
											.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

							// Ajout de l'entité
							getContentResolver().insert(urlSequencement,
									contact);
							// Ecrasement de ses données pour passer à la
							// suivante
							contact.clear();

						} while (cursorA.moveToNext());
					}

				} while (cursor.moveToNext());
			}

			// Tetes B cosses manchons

			clause = Raccordement.ORDRE_REALISATION + " LIKE '%B%' AND ("
					+ Raccordement.TYPE_ELEMENT_RACCORDE + " LIKE 'SPL' OR "
					+ Raccordement.TYPE_ELEMENT_RACCORDE
					+ " LIKE 'TER' ) GROUP BY "
					+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT;
			cursor = cr.query(urlRaccordement, colRac, clause, null,
					Raccordement.NUMERO_CHEMINEMENT);
			if (cursor.moveToFirst()) {
				do {
					numeroComposant = cursor
							.getString(cursor
									.getColumnIndex(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT));

					rang = cursor.getString(cursor
							.getColumnIndex(Raccordement.ZONE_ACTIVITE));

					rang_1 = "Connecteur " + numeroComposant;
					gamme = "Raccordement Tête B";
					numeroOperation = "7-000";

					// Ajout des cosses manchons
					clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
							+ numeroComposant + "' AND ("
							+ Raccordement.TYPE_ELEMENT_RACCORDE
							+ " LIKE 'SPL' OR "
							+ Raccordement.TYPE_ELEMENT_RACCORDE
							+ " LIKE 'TER' ) GROUP BY "
							+ Raccordement.NUMERO_FIL_CABLE;
					cursorA = cr.query(urlRaccordement, colRac, clause, null,
							Raccordement._id);
					if (cursorA.moveToFirst()) {
						do {

							descriptionOperation = "Denudage Sertissage Cosse et Manchons "

									+ " sur  Tête B  "
									+ cursorA
											.getString(cursorA
													.getColumnIndex(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT));

							// Ajout des opérations à la table de séquencement
							num1 = numeroOperation + Integer.toString(indice++);
							contact.put(Operation.GAMME, gamme);
							contact.put(Operation.RANG_1, rang);
							contact.put(Operation.RANG_1_1, rang_1);
							contact.put(Operation.DESCRIPTION_OPERATION,
									descriptionOperation);
							contact.put(Operation.NUMERO_OPERATION, num1);
							contact.put(Operation.REALISABLE, 1);
							contact.put(Operation.DUREE_THEORIQUE,
									SERTISSAGE_CONTACTS);
							contact.put(
									Operation.RANG_1_1_1,
									cursorA.getString(cursorA
											.getColumnIndex(Raccordement.NUMERO_FIL_CABLE)));

							// Ajout de l'entité
							getContentResolver().insert(urlSequencement,
									contact);
							// Ecrasement de ses données pour passer à la
							// suivante
							contact.clear();

						} while (cursorA.moveToNext());
					}

				} while (cursor.moveToNext());
			}

			gamme = "Contrôle final";
			numeroOperation = "11-000";

			// Controle finalisation
			descriptionOperation = "Contrôle final harnais";

			// Ajout des opérations à la table de séquencement
			num1 = numeroOperation + Integer.toString(indiceControleFinal++);
			contact.put(Operation.GAMME, gamme);
			contact.put(Operation.RANG_1, rang);
			contact.put(Operation.RANG_1_1, rang_1);
			contact.put(Operation.DESCRIPTION_OPERATION, descriptionOperation);
			contact.put(Operation.NUMERO_OPERATION, num1);
			contact.put(Operation.REALISABLE, 0);
			contact.put(Operation.DUREE_THEORIQUE, SERTISSAGE_CONTACTS);

			// Ajout de l'entité
			getContentResolver().insert(urlSequencement, contact);
			// Ecrasement de ses données pour passer à la
			// suivante
			contact.clear();
		}

	}

}
