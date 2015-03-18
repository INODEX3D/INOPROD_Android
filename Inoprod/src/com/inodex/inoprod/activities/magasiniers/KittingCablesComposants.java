package com.inodex.inoprod.activities.magasiniers;

import java.util.Date;

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
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.inodex.inoprod.R;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.activities.cableur.PreparationTa;
import com.inodex.inoprod.business.BOMProvider;
import com.inodex.inoprod.business.CheminementProvider;
import com.inodex.inoprod.business.DureesProvider;
import com.inodex.inoprod.business.KittingProvider;
import com.inodex.inoprod.business.ProductionProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TimeConverter;
import com.inodex.inoprod.business.Durees.Duree;
import com.inodex.inoprod.business.Production.Fil;
import com.inodex.inoprod.business.TableBOM.BOM;
import com.inodex.inoprod.business.TableCheminement.Cheminement;
import com.inodex.inoprod.business.TableKittingCable.Kitting;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

/**
 * Ecran affichant un ensemble de têtes à regrouper
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class KittingCablesComposants extends Activity {

	/** Uri à manipuler */
	private Uri urlBOM = BOMProvider.CONTENT_URI;
	private Uri urlKitting = KittingProvider.CONTENT_URI;
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlChem = CheminementProvider.CONTENT_URI;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;
	private GridView gridView;

	private boolean prodAchevee;
	private String clause, numeroOperation, numeroCom, descriptionOperation;

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
	private Date dateDebut;
	private long dureeMesuree = 0;

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Elements à récuperer de la vue */
	private TextView numeroComposant, numeroChariot, repereElectrique,
			numeroCheminement, ordreRealisation;
	private ImageButton boutonCheck, infoProduit;
	private ImageButton petitePause, grandePause;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.DUREE_MESUREE };

	private String columnsBOM[] = new String[] { BOM.REPERE_ELECTRIQUE_TENANT,
			BOM.NUMERO_COMPOSANT, BOM.NUMERO_POSITION_CHARIOT,
			BOM.ORDRE_REALISATION, BOM.QUANTITE, BOM.UNITE,
			BOM.NUMERO_LOT_SCANNE, BOM.NUMERO_DEBIT, BOM.DESIGNATION_COMPOSANT,
			BOM.FOURNISSEUR_FABRICANT, BOM.REFERENCE_IMPOSEE,
			BOM.REFERENCE_INTERNE, BOM.REFERENCE_FABRICANT2, BOM._id,
			BOM.NUMERO_POSITION_CHARIOT };
	private int[] layouts = new int[] { R.id.numeroCable, R.id.typeCable,
			R.id.designation, R.id.referenceFabricant, R.id.referenceInterne,
			R.id.quantite, R.id.uniteMesure };

	private String columnsKitting[] = new String[] { Kitting.NUMERO_FIL_CABLE,
			Kitting.TYPE_FIL_CABLE, Kitting.DESIGNATION_COMPOSANT,
			Kitting.REFERENCE_FABRICANT2, Kitting.REFERENCE_INTERNE,
			Kitting._id, Kitting.UNITE, Kitting.NUMERO_POSITION_CHARIOT,
			Kitting.REPERE_ELECTRIQUE, Kitting.NUMERO_COMPOSANT,
			Kitting.ORDRE_REALISATION, Kitting.LONGUEUR_FIL_CABLE,
			Kitting.NUMERO_CHEMINEMENT, Kitting._id, Kitting.NUMERO_OPERATION };

	private String columnsChem[] = new String[] { Cheminement._id,
			Cheminement.NUMERO_SECTION_CHEMINEMENT,
			Cheminement.NUMERO_COMPOSANT_TENANT, Cheminement.REPERE_ELECTRIQUE_TENANT,
			Cheminement.NUMERO_COMPOSANT_ABOUTISSANT, Cheminement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Cheminement.ORDRE_REALISATION };

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
		setContentView(R.layout.activity_kitting_cables_composants);
		// Initialisation du temps
		dateDebut = new Date();

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
		petitePause = (ImageButton) findViewById(R.id.imageButton2);
		grandePause = (ImageButton) findViewById(R.id.imageButton1);

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
		numeroCom = descriptionOperation.substring(37, 40);
		clause = new String(Cheminement.NUMERO_COMPOSANT_ABOUTISSANT + "='" + numeroCom
				+ "' OR " + Cheminement.NUMERO_COMPOSANT_TENANT + "='" + numeroCom
				+ "'");
		cursorA = cr.query(urlChem, columnsChem, clause, null, null);
		if (cursorA.moveToFirst()) {
			numeroCh = cursorA.getInt(cursorA
					.getColumnIndex(Cheminement.NUMERO_SECTION_CHEMINEMENT));

			// Affichage des éléments du regroupement en cours
			/*
			 * try { numeroChariot.append(cursorA.getString(cursorA
			 * .getColumnIndex(Cheminement.))); } catch (NullPointerException e)
			 * { }
			 */
			try {
				String numeroCo = cursorA.getString(cursorA
						.getColumnIndex(Cheminement.NUMERO_COMPOSANT_TENANT));
				if (numeroCo.equals(null)) {
					numeroCo = cursorA.getString(cursorA
							.getColumnIndex(Cheminement.NUMERO_COMPOSANT_ABOUTISSANT));
				}
				numeroComposant.append(": "
						+ numeroCo );
				
			} catch (NullPointerException e) {
			}
			try {
				String rep = cursorA.getString(cursorA
						.getColumnIndex(Cheminement.REPERE_ELECTRIQUE_TENANT));
				if (rep.equals(null)) {
					rep = cursorA.getString(cursorA
							.getColumnIndex(Cheminement.REPERE_ELECTRIQUE_ABOUTISSANT));
				}
				repereElectrique
						.append(": "
								+ rep);
			} catch (NullPointerException e) {
			}
			try {
				ordreRealisation
						.append(" "
								+ cursorA.getString(cursorA
										.getColumnIndex(Cheminement.ORDRE_REALISATION)));
			} catch (NullPointerException e) {
			}
			try {
				numeroCheminement.append(": " + Integer.toString(numeroCh));
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
				dateRealisation = new Date();
				contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0]
						+ " " + nomPrenomOperateur[1]);
				contact.put(Operation.DATE_REALISATION,
						dateRealisation.toGMTString());
				heureRealisation.setToNow();
				contact.put(Operation.HEURE_REALISATION,
						heureRealisation.toString());
				dureeMesuree += dateRealisation.getTime() - dateDebut.getTime();
				contact.put(Operation.DUREE_MESUREE, dureeMesuree / 1000);
				cr.update(urlSeq, contact, Operation._id + " = ?",
						new String[] { Integer.toString(opId[indiceCourant]) });
				contact.clear();

				// MAJ de la durée
				dureeMesuree = 0;
				dateDebut = new Date();

				indiceCourant += 2;
				try {
					int test = opId[indiceCourant];// Si OK il reste encore
					// des cables à regrouper
					Intent toNext = new Intent(KittingCablesComposants.this,
							KittingCablesComposants.class);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("opId", opId);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);
					finish();

				} catch (ArrayIndexOutOfBoundsException e) {

					// Il ne reste plus de cables à regrouper
					// On retourne à l'écran d'accueil

					Intent toMain = new Intent(KittingCablesComposants.this,
							MainMenuMagasinier.class);
					toMain.putExtra("Noms", nomPrenomOperateur);
					startActivity(toMain);
					finish();

				}
			}

		});
		// Affichage du temps nécessaire
				timer = (TextView) findViewById(R.id.timeDisp);
				dureeTotal = 0;
				cursorTime = cr.query(urlTim, colTim, Duree.DESIGNATION_OPERATION
						+ " LIKE '%kit%' ", null, Duree._id);
				if (cursorTime.moveToFirst()) {
					dureeTotal += TimeConverter.convert(cursorTime.getString(cursorTime
							.getColumnIndex(Duree.DUREE_THEORIQUE)));

				}

				timer.setTextColor(Color.GREEN);
				timer.setText(TimeConverter.display(dureeTotal));

		// Grande pause
		grandePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						KittingCablesComposants.this);
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
						KittingCablesComposants.this);
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
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);
		infoProduit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cursorInfo = cr.query(urlProd, columnsProd,
						Fil.NUMERO_COMPOSANT_ABOUTISSANT + " ='" + numeroCom
								+ "' OR " + Fil.NUMERO_COMPOSANT_TENANT + "='"
								+ numeroCom + "'", null, null);
				Intent toInfo = new Intent(KittingCablesComposants.this,
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
	 * Genère l'affichage en utilisant un SimpleCursorAdapter Le layout GridView
	 * est récupéré puis utiliser pour afficher chacun des éléments
	 */
	private void displayContentProvider() {
		// Création du SimpleCursorAdapter affilié au GridView
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.grid_layout_kitting_cables_composants, null,
				columnsKitting, layouts);

		gridView.setAdapter(sca);
		// Requête dans la base Cheminement
		clause = Kitting.NUMERO_CHEMINEMENT + " ='" + numeroCh + "'";
		cursor = cr.query(urlKitting, columnsKitting, clause, null, null);
		sca.changeCursor(cursor);
	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}
}
