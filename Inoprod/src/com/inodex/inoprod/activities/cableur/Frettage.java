package com.inodex.inoprod.activities.cableur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.business.CheminementProvider;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableCheminement.Cheminement;
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
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class Frettage extends Activity {

	/** Elements à récuperer de la vue */
	private TextView typeFrette, nombrePoints, zone, referenceInterne,
			referenceFabricant;
	private ImageButton boutonCheck, infoProduit, boutonAnnuler, boutonAide;
	private ImageButton petitePause, grandePause;
	private GridView gridView;

	/** Uri à manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;
	private Uri urlChe = CheminementProvider.CONTENT_URI;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;

	/** Tableau des infos produit */
	private String labels[];

	/** Liste à afficher dans l'adapteur */
	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();

	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateDebut, dateRealisation;
	private long dureeMesuree =0;
	private Time heureRealisation = new Time();

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCo, description;
	private boolean prodAchevee;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION,
			Operation.DUREE_MESUREE , Operation.DESCRIPTION_OPERATION};

	private int layouts[] = new int[] { R.id.numeroSection,
			R.id.numeroSegregation, R.id.numeroConnecteur,
			R.id.repereElectrique, R.id.typeSupportAboutissant,
			R.id.zoneLocalisation1, R.id.localisationZoneFrettage,
			R.id.typeSupportAboutissant, R.id.zoneLocalisation2 };

	private String columnsRac[] = new String[] { Raccordement._id,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_CHEMINEMENT,
			Raccordement.NUMERO_SECTION_CHEMINEMENT,
			Raccordement.NUMERO_POSITION_CHARIOT, Raccordement.ZONE_ACTIVITE,
			Raccordement.LOCALISATION1 };

	private String colChe[] = new String[] { Cheminement._id,
			Cheminement.NUMERO_COMPOSANT, Cheminement.LOCALISATION1,
			Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT,
			Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT,
			Cheminement.TYPE_SUPPORT };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_frettage);
		
		//Initialisation du temps
				dateDebut = new Date();
		// Récupération des éléments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();

		// initialisation de la production
		prodAchevee = false;

		// Récuperation des éléments de la vue
		gridView = (GridView) findViewById(R.id.gridview);

		referenceInterne = (TextView) findViewById(R.id.textView7);
		referenceFabricant = (TextView) findViewById(R.id.textView6);
		zone = (TextView) findViewById(R.id.textView5);
		typeFrette = (TextView) findViewById(R.id.textView3);
		nombrePoints = (TextView) findViewById(R.id.textView4);
		boutonAide = (ImageButton) findViewById(R.id.imageButton2);
		boutonAnnuler = (ImageButton) findViewById(R.id.imageButton4);
		boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);
		petitePause = (ImageButton) findViewById(R.id.imageButton1);
		grandePause = (ImageButton) findViewById(R.id.exitButton1);

		// Récuperation du numéro d'opération courant
		clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
		cursor = cr.query(urlSeq, columnsSeq, clause, null, Operation._id
				+ " ASC");
		if (cursor.moveToFirst()) {
			numeroOperation = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
			description = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
			numeroCo = (cursor.getString(cursor
					.getColumnIndex(Operation.RANG_1_1))).substring(11, 14);

		}

		// Etape suivante
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				indiceCourant++;
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
							toNext = new Intent(Frettage.this,
									PreparationTa.class);
						} else if (nextOperation.startsWith("Reprise")) {
							toNext = new Intent(Frettage.this,
									RepriseBlindageTa.class);
						} else if (nextOperation
								.startsWith("Denudage Sertissage Enfichage")) {
							toNext = new Intent(Frettage.this,
									DenudageSertissageEnfichageTa.class);
						} else if (nextOperation
								.startsWith("Denudage Sertissage de")) {
							toNext = new Intent(Frettage.this,
									EnfichagesTa.class);
						} else if (nextOperation.startsWith("Finalisation")) {
							toNext = new Intent(Frettage.this,
									FinalisationTa.class);
						} else if (nextOperation.startsWith("Tri")) {
							toNext = new Intent(Frettage.this,
									TriAboutissantsTa.class);
						} else if (nextOperation.startsWith("Positionnement")) {
							toNext = new Intent(Frettage.this,
									PositionnementTaTab.class);
						} else if (nextOperation.startsWith("Cheminement")) {
							toNext = new Intent(Frettage.this,
									CheminementTa.class);
						} else if (nextOperation.startsWith("Frettage")) {
							toNext = new Intent(Frettage.this,
									Frettage.class);
						}
						if (toNext != null) {

							toNext.putExtra("opId", opId);
							toNext.putExtra("Noms", nomPrenomOperateur);
							toNext.putExtra("Indice", indiceCourant);
							startActivity(toNext);
						}

					}

				} catch (ArrayIndexOutOfBoundsException e) {
					Intent toNext = new Intent(Frettage.this,
							MainMenuCableur.class);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("opId", opId);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);
				}
			}

		});
		
		//Petite Pause
				petitePause.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dureeMesuree += new Date().getTime() - dateDebut.getTime();
						AlertDialog.Builder builder = new AlertDialog.Builder(
								Frettage.this);
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

		// Affichage du contenu
		displayContentProvider();

	}

	private void displayContentProvider() {
		// Création du SimpleCursorAdapter affilié au GridView
		SimpleAdapter sca = new SimpleAdapter(this, liste,
				R.layout.grid_layout_frettage, null, layouts);

		gridView.setAdapter(sca);
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
		
		//MAJ de la durée
		dureeMesuree = 0;
		dateDebut= new Date();

	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}

}
