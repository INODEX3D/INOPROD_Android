package com.inodex.inoprod.activities.cableur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.inodex.inoprod.R;
import com.inodex.inoprod.business.NomenclatureProvider;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

public class PreparationTa extends Activity {

	/** Elements à récuperer de la vue */
	private TextView titre, numeroConnecteur, repereElectrique,
			positionChariot;
	private ImageButton boutonCheck, infoProduit, retour, boutonAide;
	private ImageButton petitePause, grandePause;
	private GridView gridView;

	/** Uri à manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;
	private Uri urlNom = NomenclatureProvider.CONTENT_URI;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;
	private int indiceLimite = 0;

	/** Tableau des infos produit */
	private String labels[];

	private int nbRows;
	

	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();

	private List<HashMap<String, String>> oldListe = null;
	/** Heure et dates à ajouter à la table de séquencment */
	private Time heureRealisation = new Time();
	private Date dateDebut, dateRealisation;
	private long dureeMesuree =0;
	
	

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCo;
	private boolean prodAchevee;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.DUREE_MESUREE };

	private int layouts[] = new int[] { R.id.designation,
			R.id.referenceFabricant, R.id.referenceInterne, R.id.numeroBorne,
			R.id.articleMonte };

	private String colRac[] = new String[] { Raccordement.DESIGNATION,
			Raccordement.REFERENCE_FABRICANT2, Raccordement.REFERENCE_INTERNE,
			Raccordement.NUMERO_BORNE_TENANT, Raccordement._id,
			Raccordement.OBTURATEUR, Raccordement.FAUX_CONTACT,
			Raccordement.NUMERO_OPERATION,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.NUMERO_BORNE_ABOUTISSANT };


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preparation_ta);
		//Initialisation du temps
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
		titre = (TextView) findViewById(R.id.textView1);
		numeroConnecteur = (TextView) findViewById(R.id.textView3);
		boutonAide = (ImageButton) findViewById(R.id.imageButton4);
		retour = (ImageButton) findViewById(R.id.imageButton2);
		boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);
		positionChariot = (TextView) findViewById(R.id.textView7);
		repereElectrique = (TextView) findViewById(R.id.textView5);
		petitePause = (ImageButton) findViewById(R.id.imageButton1);
		grandePause = (ImageButton) findViewById(R.id.exitButton1);

		// Récuperation du numéro d'opération courant
		clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
		cursor = cr.query(urlSeq, columnsSeq, clause, null, Operation._id
				+ " ASC");
		if (cursor.moveToFirst()) {
			numeroOperation = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
			numeroCo = (cursor.getString(cursor
					.getColumnIndex(Operation.RANG_1_1))).substring(11, 14);
			numeroConnecteur.append(" : " + numeroCo);
			Log.e("Connnecteur", numeroCo);
		}

		// Affichage du titre en fonction de l'ordre

		// Recuperation de la première opération
		clause = new String(Raccordement.NUMERO_OPERATION + "='"
				+ numeroOperation + "'");
		cursorA = cr.query(urlRac, colRac, clause, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {

			positionChariot
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.NUMERO_POSITION_CHARIOT)));

			if (numeroOperation.startsWith("4")) {
				titre.setText(R.string.preparationTa);
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT)));
				clause = Raccordement.NUMERO_COMPOSANT_TENANT + " ='"
						+ numeroCo + "' AND (" + Raccordement.FAUX_CONTACT
						+ "='" + 1 + "' OR " + Raccordement.OBTURATEUR + "='"
						+ 1 + "' )";
			} else {
				titre.setText(R.string.preparationTb);
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT)));
				clause = Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
						+ numeroCo + "' AND (" + Raccordement.FAUX_CONTACT
						+ "='" + 1 + "' OR " + Raccordement.OBTURATEUR + "='"
						+ 1 + "' )";
				colRac[3] = Raccordement.NUMERO_BORNE_ABOUTISSANT;
			}

		}

		nbRows = cr.query(urlRac, colRac, clause, null, Raccordement._id)
				.getCount();
		Log.e("NombreLignes", "" + nbRows);

	

		// Etape suivante

		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				indiceLimite++;
				// Controle de l'état de la production
				if (prodAchevee) { // Fin de la prodction
					
					String nextOperation = null;
					try {
						int test = opId[indiceCourant];
						clause = Operation._id + "='" + test + "'";
						cursor = cr.query(urlSeq, columnsSeq, clause, null,
								Operation._id);
						if (cursor.moveToFirst()) {
							nextOperation = cursor.getString(cursor
									.getColumnIndex(Operation.DESCRIPTION_OPERATION));
							Intent toNext = null;
							if (nextOperation.startsWith("Préparation")) {
								toNext = new Intent(PreparationTa.this,
										PreparationTa.class);
							} else if (nextOperation.startsWith("Reprise")) {
								toNext = new Intent(PreparationTa.this,
										RepriseBlindageTa.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage Enfichage")) {
								toNext = new Intent(PreparationTa.this,
										DenudageSertissageEnfichageTa.class);
							} else if (nextOperation
									.startsWith("Denudage Sertissage de")) {
								toNext = new Intent(PreparationTa.this,
										DenudageSertissageContactTa.class);
							}
							if (toNext != null) {

								toNext.putExtra("opId", opId);
								toNext.putExtra("Noms", nomPrenomOperateur);
								toNext.putExtra("Indice", indiceCourant);
								startActivity(toNext);
								finish();
							}

						}

					} catch (ArrayIndexOutOfBoundsException e) {
						Intent toNext = new Intent(PreparationTa.this,
								MainMenuCableur.class);
						toNext.putExtra("Noms", nomPrenomOperateur);
						toNext.putExtra("opId", opId);
						toNext.putExtra("Indice", indiceCourant);
						startActivity(toNext);
						finish();

					}
				} else { // Production toujours en cours

					cursorA = cr.query(urlRac, colRac, clause, null,
							Raccordement._id + " LIMIT " + indiceLimite);
					oldListe = liste;
					if (cursorA.moveToLast()) {
						HashMap<String, String> element;

						element = new HashMap<String, String>();
						element.put(colRac[0], cursorA.getString(cursorA
								.getColumnIndex(colRac[0])));
						element.put(colRac[1], cursorA.getString(cursorA
								.getColumnIndex(colRac[1])));
						element.put(colRac[2], cursorA.getString(cursorA
								.getColumnIndex(colRac[2])));
						element.put(colRac[3], cursorA.getString(cursorA
								.getColumnIndex(colRac[3])));
						element.put(colRac[4], "X");

						liste.add(element);

						displayContentProvider();
						indiceCourant++;

					}
				}
			}
		});

		// Retour arrière
		retour.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (indiceLimite > 0) {
					indiceLimite--;
					Log.e("Indice", "" + indiceLimite);
				}
				if (indiceCourant > 0) {
					indiceCourant--;
					Log.e("Indice", "" + indiceLimite);
				}

				//liste = oldListe;
				if (!(liste.isEmpty())) { 
				liste.remove(liste.size() -1);
				}
				
				//MAJ de la durée
				dureeMesuree = 0;
				dateDebut= new Date();

				// Vérification de l'état de la production
				prodAchevee = (indiceLimite >= nbRows);
				displayContentProvider();
			}
		});
		
		//Grande pause
				grandePause.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								PreparationTa.this);
						builder.setMessage("Êtes-vous sur de vouloir quitter l'application ?");
						builder.setCancelable(false);
						builder.setPositiveButton("Oui",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
									/*	Intent toMain = new Intent(
												CheminementTa.this,
												MainActivity.class);
										startActivity(toMain); */
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

		
		
		//Petite Pause
		petitePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dureeMesuree += new Date().getTime() - dateDebut.getTime();
				AlertDialog.Builder builder = new AlertDialog.Builder(
						PreparationTa.this);
				builder.setMessage("L'opération est en pause. Cliquez sur le bouton pour reprendre.");
				builder.setCancelable(false);

				builder.setNegativeButton("Retour",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {

								dateDebut= new Date();
								dialog.cancel();

							}
						});
				builder.show();

			}
		});
		

		// Info Produit
	}

	private void displayContentProvider() {
		// Création du SimpleCursorAdapter affilié au GridView
		SimpleAdapter sca = new SimpleAdapter(this, liste,
				R.layout.grid_layout_preparation_ta, colRac, layouts);

		gridView.setAdapter(sca);
		// MAJ Table de sequencement

		contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0] + " "
				+ nomPrenomOperateur[1]);
		dateRealisation = new Date();

		
		contact.put(Operation.DATE_REALISATION, dateRealisation.toGMTString());
		heureRealisation.setToNow();
		contact.put(Operation.HEURE_REALISATION, heureRealisation.toString());
		
		dureeMesuree += dateRealisation.getTime() - dateDebut.getTime();
		contact.put(Operation.DUREE_MESUREE, dureeMesuree / 1000);
		cr.update(urlSeq, contact, Operation._id + " = ?",
				new String[] { Integer.toString(opId[indiceCourant]) });
		contact.clear();
		
		//MAJ de la durée
		dureeMesuree = 0;
		dateDebut= new Date();

		// Vérification de l'état de la production
		if (indiceLimite == nbRows) {
			prodAchevee = true;
			Toast.makeText(this, "Production achevée", Toast.LENGTH_LONG)
					.show();
		}

	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}
}
