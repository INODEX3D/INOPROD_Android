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
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.inodex.inoprod.R;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.business.BOMProvider;
import com.inodex.inoprod.business.DureesProvider;
import com.inodex.inoprod.business.ProductionProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TimeConverter;
import com.inodex.inoprod.business.Durees.Duree;
import com.inodex.inoprod.business.Production.Fil;
import com.inodex.inoprod.business.TableBOM.BOM;
import com.inodex.inoprod.business.TableSequencement.Operation;

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

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;

	private SimpleCursorAdapter sca;

	private int numeroDebit, nbItem, nbRows, idFirst;
	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateRealisation = new Date();
	private Time heureRealisation = new Time();
	private Date dateDebut;
	private long dureeMesuree = 0;

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
			Operation.HEURE_REALISATION , Operation.DESCRIPTION_OPERATION};

	private String columnsBOM[] = new String[] { BOM.REPERE_ELECTRIQUE_TENANT,
			BOM.NUMERO_COMPOSANT, BOM.NUMERO_POSITION_CHARIOT,
			BOM.ORDRE_REALISATION, BOM.QUANTITE, BOM.UNITE,
			BOM.NUMERO_LOT_SCANNE, BOM.NUMERO_DEBIT, BOM.DESIGNATION_COMPOSANT,
			BOM.FOURNISSEUR_FABRICANT, BOM.REFERENCE_IMPOSEE,
			BOM.REFERENCE_INTERNE, BOM.REFERENCE_FABRICANT2, BOM._id };
	private int[] layouts = new int[] { R.id.repereElectrique,
			R.id.numeroConnecteur, R.id.positionChariot, R.id.ordreRealisation,
			R.id.quantite, R.id.uniteMesure, R.id.numeroLot };
	
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
		cursor = cr.query(urlSeq, columnsSeq, clause, null, Operation._id + " ASC");
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

			// Affichage des éléments du débit en cours
			try {
				designation.append(": "+cursor.getString(cursor
						.getColumnIndex(BOM.DESIGNATION_COMPOSANT)));
			} catch (NullPointerException e) {
			}
			try {
				fournisseur.append(": "+cursor.getString(cursor
						.getColumnIndex(BOM.FOURNISSEUR_FABRICANT)));
			} catch (NullPointerException e) {
			}
			try {
				referenceFabricant.append(": "+cursor.getString(cursor
						.getColumnIndex(BOM.REFERENCE_FABRICANT2)));
			} catch (NullPointerException e) {
			}
			try {
				referenceInterne.append(": "+cursor.getString(cursor
						.getColumnIndex(BOM.REFERENCE_INTERNE)));
			} catch (NullPointerException e) {
			}
			try {
				referenceImposee.append(": "+Integer.toString(cursor.getInt(cursor
						.getColumnIndex(BOM.REFERENCE_IMPOSEE))));
			} catch (NullPointerException e) {
			}

		}

		// Affichage de la prémiere ligne du contenut
		displayContentProvider();
		
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
				indiceCourant++;
				// Controle de l'état de la production
				if (prodAchevee) { // Fin de la prodction
					
					try {
						int test = opId[indiceCourant]; // Si OK il reste encore
						// des cables à débiter
						Intent toNext = new Intent(ServitudeComposants.this,
								SaisieTracabiliteComposant.class);
						toNext.putExtra("Noms", nomPrenomOperateur);
						toNext.putExtra("opId", opId);
						toNext.putExtra("Indice", indiceCourant);
						startActivity(toNext);
						finish();

					} catch (ArrayIndexOutOfBoundsException e) {
						// Il ne reste plus de cables à débiter
						// On passe donc au regroupement
						clause = new String(Operation.RANG_1_1
								+ " LIKE '%Constitution%' ");
						cursor = cr.query(urlSeq, columnsSeq, clause, null,
								Operation._id + " ASC");
						// Rempliassage du tableau pour chaque numero de débit
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
					// On affiche le cable suivant à débiter
					displayContentProvider();
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
		// Création du SimpleCursorAdapter affilié au GridView
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

		// Affichage des cables à débiter ou dèja débité
		clause = new String(BOM.NUMERO_DEBIT + "='" + numeroDebit + "' AND "
				+ BOM._id + "<='" + (idFirst) +"'");
		cursor = cr.query(urlBOM, columnsBOM, clause, null, null);
		sca.changeCursor(cursor);

		// Vérification de l'état de la production
		if (cursor.getCount() == nbRows) {
			prodAchevee = true;
			Toast.makeText(this, "Production achevée", Toast.LENGTH_LONG)
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
