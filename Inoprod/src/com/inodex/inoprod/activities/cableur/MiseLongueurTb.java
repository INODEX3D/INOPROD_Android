package com.inodex.inoprod.activities.cableur;

import java.util.Date;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.business.DureesProvider;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TimeConverter;
import com.inodex.inoprod.business.Durees.Duree;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

public class MiseLongueurTb extends Activity {

	private ImageButton boutonCheck, infoProduit;

	/** Elements à récuperer de la vue */
	private TextView numeroConnecteur, repereElectrique;

	/** Uri à manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;

	/** Tableau des opérations à réaliser */
	private int opId[] = null;

	/** Indice de l'opération courante */
	private int indiceCourant = 0;

	/** Tableau des infos produit */
	private String labels[];

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

	private String clause, numeroOperation, numeroCo;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION };

	private int layouts[] = new int[] { R.id.designation,
			R.id.referenceFabricant, R.id.referenceInterne, R.id.numeroBorne, };

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
	
	private String colInfo[] = new String[] { Raccordement._id,
			Raccordement.DESIGNATION, Raccordement.NUMERO_REVISION_HARNAIS, Raccordement.STANDARD,
			Raccordement.NUMERO_HARNAIS_FAISCEAUX, Raccordement.REFERENCE_FICHIER_SOURCE};
	private Cursor cursorInfo;
	
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
		setContentView(R.layout.activity_mise_longueur_tb);
		// Récupération des éléments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");

		cr = getContentResolver();
		contact = new ContentValues();

		// Récuperation des éléments de la vue

		numeroConnecteur = (TextView) findViewById(R.id.textView3);

		boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);

		repereElectrique = (TextView) findViewById(R.id.textView5);

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

		// Recuperation de la première opération
		clause = new String(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + "='"
				+ numeroCo + "'");
		cursorA = cr.query(urlRac, colRac, clause, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {

			repereElectrique
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT)));

		}
		
		// Affichage du temps nécessaire
				timer = (TextView) findViewById(R.id.timeDisp);
				dureeTotal = 0;
				cursorTime = cr.query(urlTim, colTim, Duree.DESIGNATION_OPERATION
						+ " LIKE '%Mise%' ", null, Duree._id);
				if (cursorTime.moveToFirst()) {
					dureeTotal += TimeConverter.convert(cursorTime.getString(cursorTime
							.getColumnIndex(Duree.DUREE_THEORIQUE)));

				}
				
				timer.setTextColor(Color.GREEN);
				timer.setText(TimeConverter.display(dureeTotal));
		
		// Etape suivante
				boutonCheck.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						
						dateRealisation = new Date();
						contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0] + " "
								+ nomPrenomOperateur[1]);
						contact.put(Operation.DATE_REALISATION, dateRealisation.toGMTString());
						heureRealisation.setToNow();
						contact.put(Operation.HEURE_REALISATION, heureRealisation.toString());
					
						cr.update(urlSeq, contact, Operation._id + " = ?",
								new String[] { Integer.toString(opId[indiceCourant]) });
						contact.clear();

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
									toNext = new Intent(MiseLongueurTb.this,
											PreparationTa.class);
								} else if (nextOperation.startsWith("Reprise")) {
									toNext = new Intent(MiseLongueurTb.this,
											RepriseBlindageTa.class);
								} else if (nextOperation
										.startsWith("Denudage Sertissage Enfichage")) {
									toNext = new Intent(MiseLongueurTb.this,
											DenudageSertissageEnfichageTa.class);
								} else if (nextOperation
										.startsWith("Denudage Sertissage de")) {
									toNext = new Intent(MiseLongueurTb.this,
											EnfichagesTa.class);

								} else if (nextOperation.startsWith("Finalisation")) {
									toNext = new Intent(MiseLongueurTb.this,
											FinalisationTa.class);
								} else if (nextOperation.startsWith("Tri")) {
									toNext = new Intent(MiseLongueurTb.this,
											TriAboutissantsTa.class);
								} else if (nextOperation
										.startsWith("Positionnement")) {
									toNext = new Intent(MiseLongueurTb.this,
											PositionnementTaTab.class);
								} else if (nextOperation.startsWith("Cheminement")) {
									toNext = new Intent(MiseLongueurTb.this,
											CheminementTa.class);
								} else if (nextOperation
										.startsWith("Mise")) {
									toNext = new Intent(MiseLongueurTb.this,
											MiseLongueurTb.class);
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
							Intent toNext = new Intent(MiseLongueurTb.this,
									MainMenuCableur.class);
							toNext.putExtra("Noms", nomPrenomOperateur);
							toNext.putExtra("opId", opId);
							toNext.putExtra("Indice", indiceCourant);
							startActivity(toNext);
							finish();

						}
					}

				});
				
				// Info Produit
				
				infoProduit.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						cursorInfo = cr.query(urlRac, colInfo, Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
								+ " ='" + numeroCo + "' OR " + Raccordement.NUMERO_COMPOSANT_TENANT + "='" + numeroCo+"'" , null, null);
						Intent toInfo = new Intent(MiseLongueurTb.this, InfoProduit.class);
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
}
