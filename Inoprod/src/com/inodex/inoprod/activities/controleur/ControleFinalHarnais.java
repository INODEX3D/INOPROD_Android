package com.inodex.inoprod.activities.controleur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.business.Durees.Duree;
import com.inodex.inoprod.business.Nomenclature.Cable;
import com.inodex.inoprod.business.DureesProvider;
import com.inodex.inoprod.business.NomenclatureProvider;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TimeConverter;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ControleFinalHarnais extends Activity {

	/** Elements à récuperer de la vue */
	private TextView designationProduit, numeroHarnais, numeroSerie,
			numeroTraitement, numeroRevision, standard, referenceFichierSource;
	private ImageButton boutonCheck, infoProduit, boutonAnnuler, boutonAide;
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

	/** Tableau des infos produit */
	private String labels[];

	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();

	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateDebut, dateRealisation;
	private long dureeMesuree = 0;
	private Time heureRealisation = new Time();

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCo, description;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.DUREE_MESUREE };

	private int layouts[] = new int[] { R.id.pointsVerifier,
			R.id.valeurAttendue, R.id.commentaires };
	private String controle[] = new String[] { "Point vérifier",
			"Valeur attendu", "Controle valide", "Controle refuse",
			"Commentaires" };

	private String colRac[] = new String[] {
			Raccordement.ACCESSOIRE_COMPOSANT1,
			Raccordement.REFERENCE_FABRICANT2, Raccordement.LONGUEUR_FIL_CABLE,
			Raccordement.ORIENTATION_RACCORD_ARRIERE,
			Raccordement.ETAT_FINALISATION_PRISE,
			Raccordement.REFERENCE_ACCESSOIRE_OUTIL_TENANT,
			Raccordement.REFERENCE_OUTIL_TENANT, Raccordement._id,
			Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.ORDRE_REALISATION,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_POSITION_CHARIOT, Raccordement.STANDARD,
			Raccordement.NUMERO_HARNAIS_FAISCEAUX,
			Raccordement.NUMERO_REVISION_HARNAIS,
			Raccordement.REFERENCE_FICHIER_SOURCE, Raccordement.DESIGNATION };

	private String colNom[] = new String[] { Cable._id,
			Cable.DESIGNATION_COMPOSANT, Cable.NUMERO_COMPOSANT,
			Cable.FAMILLE_PRODUIT, Cable.REFERENCE_FABRICANT1,
			Cable.REFERENCE_FABRICANT2, Cable.REFERENCE_INTERNE, Cable.UNITE,
			Cable.QUANTITE };
	
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
		setContentView(R.layout.activity_controle_final_harnais);
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
		numeroSerie = (TextView) findViewById(R.id.TextView04);
		numeroRevision = (TextView) findViewById(R.id.TextView05);
		designationProduit = (TextView) findViewById(R.id.TextView01);
		numeroHarnais = (TextView) findViewById(R.id.textView3);
		standard = (TextView) findViewById(R.id.TextView02);
		referenceFichierSource = (TextView) findViewById(R.id.textView5);
		boutonAide = (ImageButton) findViewById(R.id.imageButton4);

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
					.getColumnIndex(Operation.DESCRIPTION_OPERATION));
			numeroCo = (cursor.getString(cursor
					.getColumnIndex(Operation.RANG_1_1))).substring(11, 14);

		}

		clause = new String(Raccordement.NUMERO_COMPOSANT_TENANT + "='"
				+ numeroCo + "' OR "
				+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + "='" + numeroCo
				+ "'");
		cursorA = cr.query(urlRac, colRac, null, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {
			standard.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Raccordement.STANDARD)));
			numeroRevision
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.NUMERO_REVISION_HARNAIS)));
			numeroHarnais
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.NUMERO_HARNAIS_FAISCEAUX)));
			designationProduit.append(" : "
					+ cursorA.getString(cursorA
							.getColumnIndex(Raccordement.DESIGNATION)));
			referenceFichierSource
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.REFERENCE_FICHIER_SOURCE)));
		}

		clause = Cable.FAMILLE_PRODUIT + " LIKE '%Gaine%' GROUP BY "
				+ Cable.NUMERO_COMPOSANT;
		cursor = cr.query(urlNom, colNom, clause, null, Cable._id);
		if (cursor.moveToFirst()) {
			HashMap<String, String> element;

			do {

				element = new HashMap<String, String>();
				String nc = cursor.getString(cursor
						.getColumnIndex(Cable.NUMERO_COMPOSANT));
				element.put(controle[0], "Présence de gaine sur " + nc);

				clause = Cable.FAMILLE_PRODUIT + " LIKE '%Gaine%' AND "
						+ Cable.NUMERO_COMPOSANT + "='" + nc + "'";

				element.put(controle[1],
						cr.query(urlNom, colNom, clause, null, Cable._id)
								.getCount() + " gaines");

				element.put(controle[4], "");
				liste.add(element);

			} while (cursor.moveToNext());
		}
		
		// Affichage du temps nécessaire
				timer = (TextView) findViewById(R.id.timeDisp);
				dureeTotal = 0;
				cursorTime = cr.query(urlTim, colTim, Duree.DESIGNATION_OPERATION
						+ " LIKE '%hemine%' ", null, Duree._id);
				if (cursorTime.moveToFirst()) {
					dureeTotal += TimeConverter.convert(cursorTime.getString(cursorTime
							.getColumnIndex(Duree.DUREE_THEORIQUE)));

				}
				
				timer.setTextColor(Color.GREEN);
				timer.setText(TimeConverter.display(dureeTotal));

		displayContentProvider();

		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
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

				Intent toNext = new Intent(ControleFinalHarnais.this,
						DataPack.class);

				startActivity(toNext);
				finish();

			}
		});

		// Grande pause
		grandePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ControleFinalHarnais.this);
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
						ControleFinalHarnais.this);
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

	}

	private void displayContentProvider() {

		SimpleAdapter sa = new SimpleAdapter(this, liste,
				R.layout.grid_layout_controle_finalisation_ta, controle,
				layouts);

		gridView.setAdapter(sa);

	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}
}
