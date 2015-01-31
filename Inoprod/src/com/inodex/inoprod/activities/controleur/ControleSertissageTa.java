package com.inodex.inoprod.activities.controleur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.activities.cableur.DenudageSertissageEnfichageTa;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ControleSertissageTa extends Activity {

	/** Elements � r�cuperer de la vue */
	private TextView titre, numeroConnecteur, repereElectrique,
			positionChariot;
	private ImageButton boutonCheck, infoProduit, boutonAnnuler, boutonAide;
	private GridView gridView;

	/** Uri � manipuler */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;

	/** Tableau des op�rations � r�aliser */
	private int opId[] = null;

	/** Indice de l'op�ration courante */
	private int indiceCourant = 0;
	private int indiceLimite = 0;
	private int nbRows;

	/** Tableau des infos produit */
	private String labels[];

	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();

	/** Heure et dates � ajouter � la table de s�quencment */
	private Date dateRealisation = new Date();
	private Time heureRealisation = new Time();

	/** Nom de l'op�rateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver � utiliser lors des requ�tes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	private boolean prodAchevee;

	private String clause, numeroOperation, numeroCo, clauseTotal,
			oldClauseTotal, numeroCable, description;

	/** Colonnes utilis�s pour les requ�tes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION };

	private int layouts[] = new int[] { R.id.statutLiaison,
			R.id.numeroRevisionLiaison, R.id.typeCable, R.id.numeroFil,
			R.id.numeroFilDansCable, R.id.couleurFil,
			R.id.referenceFabricantContact, R.id.controleValide,
			R.id.controleAcceptable, R.id.controleRefuse };
	private String controle[] = new String[] { "Statut Liaison",
			"Num�ro r�vision liaison", "Type cable", "Numero Fil",
			"Numero Fil dans cable", "Couleur fil",
			"Reference fabricant contact", "Controle valide",
			"Controle acceptable", "Controle refuse" };

	private String colRac[] = new String[] { Raccordement.ETAT_LIAISON_FIL,
			Raccordement.NUMERO_REVISION_FIL, Raccordement.TYPE_FIL_CABLE,
			Raccordement.NUMERO_FIL_CABLE, Raccordement.NUMERO_FIL_DANS_CABLE,
			Raccordement.COULEUR_FIL, Raccordement.NUMERO_BORNE_TENANT,
			Raccordement.REFERENCE_FABRICANT2, Raccordement.REFERENCE_INTERNE,
			Raccordement.REFERENCE_OUTIL_TENANT,
			Raccordement.NUMERO_SERIE_OUTIL, Raccordement.REGLAGE_OUTIL_TENANT,
			Raccordement.REFERENCE_ACCESSOIRE_OUTIL_TENANT, Raccordement._id,
			Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.NUMERO_OPERATION,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.LONGUEUR_FIL_CABLE, Raccordement.ORDRE_REALISATION };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_controle_sertissage_ta);

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
		positionChariot = (TextView) findViewById(R.id.textView5b);
		repereElectrique = (TextView) findViewById(R.id.textView5);

		// R�cuperation du num�ro d'op�ration courant
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
			numeroConnecteur.append(" : " + numeroCo);
		}

		if (description.contains("T�te A")) {
			clause = new String(Raccordement.NUMERO_COMPOSANT_TENANT + "='"
					+ numeroCo + "'");
			titre.setText(R.string.controleSertissageTa);

		} else {
			clause = new String(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
					+ "='" + numeroCo + "'");
			titre.setText(R.string.controleSertissageTb);

		}
		cursorA = cr.query(urlRac, colRac, clause, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {

			positionChariot
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.NUMERO_POSITION_CHARIOT)));

		}
		nbRows = cr.query(urlRac, colRac,
				clause + " GROUP BY " + Raccordement.NUMERO_FIL_CABLE, null,
				Raccordement._id).getCount();
		Log.e("NombreLignes", "" + nbRows);

		// Scan
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (prodAchevee) {
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
								toNext = new Intent(ControleSertissageTa.this,
										ControleFinalisationTa.class);
							} else if (nextOperation
									.startsWith("Contr�le r�tention")) {
								toNext = new Intent(ControleSertissageTa.this,
										ControleRetentionTa.class);
							} else if (nextOperation
									.startsWith("Contr�le sertissage")) {
								toNext = new Intent(ControleSertissageTa.this,
										ControleSertissageTa.class);
							}
							if (toNext != null) {

								toNext.putExtra("opId", opId);
								toNext.putExtra("Noms", nomPrenomOperateur);
								toNext.putExtra("Indice", indiceCourant);
								startActivity(toNext);
							}
						}

					} catch (ArrayIndexOutOfBoundsException e) {
						Intent toNext = new Intent(ControleSertissageTa.this,
								MainMenuControleur.class);
						toNext.putExtra("Noms", nomPrenomOperateur);
						toNext.putExtra("opId", opId);
						toNext.putExtra("Indice", indiceCourant);
						startActivity(toNext);

					}

				} else {

					try {
						Intent intent = new Intent(
								"com.google.zxing.client.android.SCAN");
						intent.setPackage("com.google.zxing.client.android");
						intent.putExtra(
								"com.google.zxing.client.android.SCAN.SCAN_MODE",
								"QR_CODE_MODE");
						startActivityForResult(intent, 0);
					} catch (ActivityNotFoundException e) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								ControleSertissageTa.this);
						builder.setMessage("Impossible de trouver une application pour le scan. Entrez le N� de cable.");
						builder.setCancelable(false);
						final EditText cable = new EditText(
								ControleSertissageTa.this);
						builder.setView(cable);
						builder.setPositiveButton("Contr�le valid�",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										numeroCable = cable.getText()
												.toString();

										clause = Raccordement.NUMERO_FIL_CABLE
												+ "='"
												+ numeroCable
												+ "' AND ("
												+ Raccordement.NUMERO_COMPOSANT_TENANT
												+ "='"
												+ numeroCo
												+ "' OR "
												+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
												+ "='" + numeroCo + "' )";
										cursorA = cr.query(urlRac, colRac,
												clause, null, Raccordement._id);
										if (cursorA.moveToFirst()) {
											HashMap<String, String> element;

											element = new HashMap<String, String>();
											element.put(
													controle[0],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[0])));
											element.put(
													controle[1],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[1])));
											element.put(
													controle[2],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[2])));
											element.put(
													controle[3],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[3])));
											element.put(
													controle[4],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[4])));
											element.put(
													controle[5],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[5])));
											element.put(
													controle[6],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[6])));
											element.put(controle[7], "X");
											liste.add(element);

										
											indiceLimite++;
											displayContentProvider();

										} else {
											Toast.makeText(
													ControleSertissageTa.this,
													"Ce cable ne correspond pas",
													Toast.LENGTH_SHORT).show();
										}
									}

								});

						builder.setNegativeButton("Contr�le refus�",
								new DialogInterface.OnClickListener() {
									public void onClick(
											final DialogInterface dialog,
											final int id) {

										numeroCable = cable.getText()
												.toString();

										clause = Raccordement.NUMERO_FIL_CABLE
												+ "='"
												+ numeroCable
												+ "' AND ("
												+ Raccordement.NUMERO_COMPOSANT_TENANT
												+ "='"
												+ numeroCo
												+ "' OR "
												+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
												+ "='" + numeroCo + "' )";
										cursorA = cr.query(urlRac, colRac,
												clause, null, Raccordement._id);
										if (cursorA.moveToFirst()) {
											HashMap<String, String> element;

											element = new HashMap<String, String>();
											element.put(
													controle[0],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[0])));
											element.put(
													controle[1],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[1])));
											element.put(
													controle[2],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[2])));
											element.put(
													controle[3],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[3])));
											element.put(
													controle[4],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[4])));
											element.put(
													controle[5],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[5])));
											element.put(
													controle[6],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[6])));
											element.put(controle[9], "X");
											liste.add(element);

									
											indiceLimite++;
											displayContentProvider();

										} else {
											Toast.makeText(
													ControleSertissageTa.this,
													"Ce cable ne correspond pas",
													Toast.LENGTH_SHORT).show();
										}

									}
								});
						builder.setNeutralButton("Contr�le acceptable",
								new DialogInterface.OnClickListener() {
									public void onClick(
											final DialogInterface dialog,
											final int id) {

										numeroCable = cable.getText()
												.toString();

										clause = Raccordement.NUMERO_FIL_CABLE
												+ "='"
												+ numeroCable
												+ "' AND ("
												+ Raccordement.NUMERO_COMPOSANT_TENANT
												+ "='"
												+ numeroCo
												+ "' OR "
												+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
												+ "='" + numeroCo + "' )";
										cursorA = cr.query(urlRac, colRac,
												clause, null, Raccordement._id);
										if (cursorA.moveToFirst()) {
											HashMap<String, String> element;

											element = new HashMap<String, String>();
											element.put(
													controle[0],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[0])));
											element.put(
													controle[1],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[1])));
											element.put(
													controle[2],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[2])));
											element.put(
													controle[3],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[3])));
											element.put(
													controle[4],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[4])));
											element.put(
													controle[5],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[5])));
											element.put(
													controle[6],
													cursorA.getString(cursorA
															.getColumnIndex(colRac[6])));
											element.put(controle[8], "X");
											liste.add(element);

											indiceLimite++;
											displayContentProvider();

										} else {
											Toast.makeText(
													ControleSertissageTa.this,
													"Ce cable ne correspond pas",
													Toast.LENGTH_SHORT).show();
										}

									}
								});

						builder.show();
					}

				}
			}
		});

	}

	private void displayContentProvider() {

		SimpleAdapter sa = new SimpleAdapter(this, liste,
				R.layout.grid_layout_controle_sertissage_ta, controle,
				layouts);

		gridView.setAdapter(sa);

		contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0] + " "
				+ nomPrenomOperateur[1]);
		contact.put(Operation.DATE_REALISATION, dateRealisation.toGMTString());
		heureRealisation.setToNow();
		contact.put(Operation.HEURE_REALISATION, heureRealisation.toString());
		cr.update(urlSeq, contact, Operation._id + " = ?",
				new String[] { Integer.toString(opId[indiceCourant]) });
		contact.clear();

		// V�rification de l'�tat de la production
		if (indiceLimite == nbRows) {
			prodAchevee = true;
			Toast.makeText(this, "Production achev�e", Toast.LENGTH_LONG)
					.show();
		}

	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}

	// R�cup�ration du code barre scann�
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				/* ACTION A EFFECTUER */
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(ControleSertissageTa.this,
						"Echec du scan de l'identifiant", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

}
