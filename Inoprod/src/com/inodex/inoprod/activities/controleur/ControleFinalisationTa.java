package com.inodex.inoprod.activities.controleur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.activities.cableur.CheminementTa;
import com.inodex.inoprod.activities.cableur.FinalisationTa;
import com.inodex.inoprod.activities.cableur.MainMenuCableur;
import com.inodex.inoprod.activities.cableur.PreparationTa;
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
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ControleFinalisationTa extends Activity {

	/** Elements � r�cuperer de la vue */
	private TextView titre, numeroConnecteur, repereElectrique,
			positionChariot;
	private ImageButton boutonCheck, infoProduit, boutonAnnuler, boutonAide;
	private ImageButton petitePause, grandePause;
	private GridView gridView;

	/** Uri � manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;

	/** Tableau des op�rations � r�aliser */
	private int opId[] = null;

	/** Indice de l'op�ration courante */
	private int indiceCourant = 0;

	/** Tableau des infos produit */
	private String labels[];

	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();

	private String points[] = new String[] { "V�rification r�f�rence raccord",
			"V�rification r�f�rence connecteur", "Longueur gainage",
			"Orientation raccord", "Etat finalisation" };

	/** Heure et dates � ajouter � la table de s�quencment */
	private Date dateDebut, dateRealisation;
	private long dureeMesuree =0;
	private Time heureRealisation = new Time();

	/** Nom de l'op�rateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver � utiliser lors des requ�tes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCo, description;

	/** Colonnes utilis�s pour les requ�tes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.DUREE_MESUREE };

	private int layouts[] = new int[] { R.id.pointsVerifier,
			R.id.valeurAttendue, 
			R.id.commentaires };
	private String controle[] = new String[] { "Point v�rifier",
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
			Raccordement.NUMERO_POSITION_CHARIOT };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_controle_finalisation_ta);
		//Initialisation du temps
				dateDebut = new Date();

		// R�cup�ration des �l�ments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();

		// R�cuperation des �l�ments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		titre = (TextView) findViewById(R.id.textView1);
		numeroConnecteur = (TextView) findViewById(R.id.textView3);
		boutonAide = (ImageButton) findViewById(R.id.imageButton4);
		boutonAnnuler = (ImageButton) findViewById(R.id.imageButton2);
		boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
		infoProduit = (ImageButton) findViewById(R.id.infoButton1);
		positionChariot = (TextView) findViewById(R.id.textView7);
		repereElectrique = (TextView) findViewById(R.id.textView5);
		petitePause = (ImageButton) findViewById(R.id.imageButton1);
		grandePause = (ImageButton) findViewById(R.id.exitButton1);

		// R�cuperation du num�ro d'op�ration courant
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
			numeroConnecteur.append(" : " + numeroCo);
		}

		if (description.contains("A")) {
			clause = new String(Raccordement.NUMERO_COMPOSANT_TENANT + "='"
					+ numeroCo + "' GROUP BY "
					+ Raccordement.NUMERO_COMPOSANT_TENANT);
			titre.setText(R.string.controleFinalisationTa);

		} else {
			clause = new String(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
					+ "='" + numeroCo + "' GROUP BY "
					+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT);
			titre.setText(R.string.controleFinalisationTb);

		}
		cursorA = cr.query(urlRac, colRac, clause, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {

			positionChariot
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.NUMERO_POSITION_CHARIOT)));

			HashMap<String, String> element;

			for (int i = 0; i < 5; i++) {

				element = new HashMap<String, String>();
				element.put(controle[0], points[i]);
				element.put(controle[1],
						cursorA.getString(cursorA.getColumnIndex(colRac[i])));
				//element.put(controle[2], "" + 1);
				//element.put(controle[3], ""+ 0);
				element.put(controle[4], "");
				liste.add(element);

			}

		}

		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
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
				
				//MAJ de la dur�e
				dureeMesuree = 0;
				dateDebut= new Date();
				
				// Signalement du point de controle
				clause = Operation.RANG_1_1 + " LIKE '%" + numeroCo + "%' AND ("
						+ Operation.GAMME
						+ " LIKE 'Cheminement%' OR " + Operation.GAMME + " LIKE 'Frett%')";
				cursor = cr.query(urlSeq, columnsSeq, clause, null,
						Operation._id);
				if (cursor.moveToFirst()) {
					
					contact.put(Operation.REALISABLE, 1);
					int id = cursor.getInt(cursor.getColumnIndex(Operation._id));
					cr.update(urlSeq, contact,clause , null);
					contact.clear();
				}

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
						if (nextOperation.startsWith("Contr�le final")) {
							toNext = new Intent(ControleFinalisationTa.this,
									ControleFinalisationTa.class);
						} else if (nextOperation
								.startsWith("Contr�le r�tention")) {
							toNext = new Intent(ControleFinalisationTa.this,
									ControleRetentionTa.class);
						} else if (nextOperation
								.startsWith("Contr�le sertissage")) {
							toNext = new Intent(ControleFinalisationTa.this,
									ControleSertissageTa.class);
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
					Intent toNext = new Intent(ControleFinalisationTa.this,
							MainMenuControleur.class);
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("opId", opId);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);
					finish();

				}
			}
		});
		
		// Grande pause
				grandePause.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								ControleFinalisationTa.this);
						builder.setMessage("�tes-vous sur de vouloir quitter l'application ?");
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
		
		//Petite Pause
				petitePause.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dureeMesuree += new Date().getTime() - dateDebut.getTime();
						AlertDialog.Builder builder = new AlertDialog.Builder(
								ControleFinalisationTa.this);
						builder.setMessage("L'op�ration est en pause. Cliquez sur le bouton pour reprendre.");
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
		
		

		displayContentProvider();
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
