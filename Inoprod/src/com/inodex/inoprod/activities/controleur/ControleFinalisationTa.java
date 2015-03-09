package com.inodex.inoprod.activities.controleur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.activities.cableur.CheminementTa;
import com.inodex.inoprod.activities.cableur.FinalisationTa;
import com.inodex.inoprod.activities.cableur.MainMenuCableur;
import com.inodex.inoprod.activities.cableur.MiseLongueurTb;
import com.inodex.inoprod.activities.cableur.PreparationTa;
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
import android.content.ActivityNotFoundException;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ControleFinalisationTa extends Activity {

	/** Elements à récuperer de la vue */
	private TextView titre, numeroConnecteur, repereElectrique,
			positionChariot;
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
	private int indiceTab = 0;

	/** Tableau des infos produit */
	private String labels[];

	private List<HashMap<String, String>> liste = new ArrayList<HashMap<String, String>>();

	private String points[] = new String[] { "Vérification référence raccord",
			"Vérification référence connecteur", "Longueur gainage",
			"Orientation raccord", "Etat finalisation" };

	/** Heure et dates à ajouter à la table de séquencment */
	private Date dateDebut, dateRealisation;
	private long dureeMesuree = 0;
	private Time heureRealisation = new Time();

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA, cursorB;
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
			R.id.valeurAttendue, R.id.controleValide, R.id.controleRefuse, R.id.commentaires };
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
			Raccordement.NUMERO_POSITION_CHARIOT, Raccordement.NUMERO_FIL_CABLE };

	private String colNom[] = new String[] { Cable.REFERENCE_INTERNE,
			Cable.REFERENCE_FABRICANT2, Cable.QUANTITE, Cable.UNITE,
			Cable.NUMERO_COMPOSANT, Cable.FAMILLE_PRODUIT, Cable._id, };

	private String colInfo[] = new String[] { Raccordement._id,
			Raccordement.DESIGNATION, Raccordement.NUMERO_REVISION_HARNAIS,
			Raccordement.STANDARD, Raccordement.NUMERO_HARNAIS_FAISCEAUX,
			Raccordement.REFERENCE_FICHIER_SOURCE };
	private Cursor cursorInfo;

	private String ordre;

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
		setContentView(R.layout.activity_controle_finalisation_ta);
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
			numeroConnecteur.append(" : " + numeroCo);
		}

		if (description.contains("A")) {
			clause = new String(Raccordement.NUMERO_COMPOSANT_TENANT + "='"
					+ numeroCo + "'"/*
									 * + "' GROUP BY " +
									 * Raccordement.NUMERO_COMPOSANT_TENANT
									 */);
			titre.setText(R.string.controleFinalisationTa);
			ordre = "A";

		} else {
			clause = new String(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT
					+ "='" + numeroCo + "'"/*
											 * + "' GROUP BY " + Raccordement.
											 * NUMERO_COMPOSANT_ABOUTISSANT
											 */);
			titre.setText(R.string.controleFinalisationTb);
			ordre = "B";

		}
		cursorA = cr.query(urlRac, colRac, clause, null, Raccordement._id
				+ " ASC");
		if (cursorA.moveToFirst()) {

			positionChariot
					.append(" : "
							+ cursorA.getString(cursorA
									.getColumnIndex(Raccordement.NUMERO_POSITION_CHARIOT)));
			if (ordre.equals("A")) {
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_TENANT)));
			} else {
				repereElectrique
						.append(" : "
								+ cursorA.getString(cursorA
										.getColumnIndex(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT)));
			}

		}

		displayContentProvider();

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

		// Validation d'un point de controle
		boutonCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (indiceTab > 5) {

					contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0]
							+ " " + nomPrenomOperateur[1]);
					dateRealisation = new Date();
					contact.put(Operation.DATE_REALISATION,
							dateRealisation.toGMTString());
					heureRealisation.setToNow();
					contact.put(Operation.HEURE_REALISATION,
							heureRealisation.toString());
					dureeMesuree += dateRealisation.getTime()
							- dateDebut.getTime();
					contact.put(Operation.DUREE_MESUREE, dureeMesuree / 1000);
					cr.update(urlSeq, contact, Operation._id + " = ?",
							new String[] { Integer
									.toString(opId[indiceCourant]) });
					contact.clear();

					// Signalement du point de controle
					String clauseSup = "";
					String clauseTotal = null;

					// Recherche des aboutissants
					clause = new String(Raccordement.NUMERO_COMPOSANT_TENANT
							+ "='" + numeroCo + "'");

					cursorA = cr.query(urlRac, colRac, clause, null,
							Raccordement._id + " ASC");
					if (cursorA.moveToFirst()) {

						do {
							if (clauseTotal == null) {
								clauseTotal = Raccordement.NUMERO_FIL_CABLE
										+ "='"
										+ cursorA.getString(cursorA
												.getColumnIndex(Raccordement.NUMERO_FIL_CABLE))
										+ "'";
							} else {

								clauseTotal += " OR "
										+ Raccordement.NUMERO_FIL_CABLE
										+ "='"
										+ cursorA.getString(cursorA
												.getColumnIndex(Raccordement.NUMERO_FIL_CABLE))
										+ "'";
							}
						} while (cursorA.moveToNext());

					}
					cursorA = cr.query(urlRac, colRac, " (" + clauseTotal
							+ ") AND "
							+ Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT
							+ "!='null' GROUP BY "
							+ Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT, null,
							Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT);

					if (cursorA.moveToFirst()) {
						do {

							clauseSup += " OR "
									+ Operation.RANG_1_1
									+ " LIKE '%"
									+ cursorA.getString(cursorA
											.getColumnIndex(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT))
									+ "' ";

						} while (cursorA.moveToNext());
					}

					clause = "(" + Operation.RANG_1_1 + " LIKE '%" + numeroCo
							+ "%'" + clauseSup + " ) AND (" + Operation.GAMME
							+ " LIKE 'Cheminement%' )";
					cursor = cr.query(urlSeq, columnsSeq, clause, null,
							Operation._id);
					if (cursor.moveToFirst()) {

						contact.put(Operation.REALISABLE, 1);
						int id = cursor.getInt(cursor
								.getColumnIndex(Operation._id));
						cr.update(urlSeq, contact, clause, null);
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
							if (nextOperation.startsWith("Contrôle final tête")) {
								toNext = new Intent(
										ControleFinalisationTa.this,
										ControleFinalisationTa.class);
							} else if (nextOperation
									.startsWith("Contrôle rétention")) {
								toNext = new Intent(
										ControleFinalisationTa.this,
										ControleRetentionTa.class);
							} else if (nextOperation
									.startsWith("Contrôle sertissage")) {
								toNext = new Intent(
										ControleFinalisationTa.this,
										ControleSertissageTa.class);
							} else if (nextOperation
									.startsWith("Contrôle final harnais")) {
								toNext = new Intent(
										ControleFinalisationTa.this,
										ControleFinalHarnais.class);
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

				} else {

					AlertDialog.Builder builder = new AlertDialog.Builder(
							ControleFinalisationTa.this);
					builder.setMessage("Contrôle " + points[indiceTab]);
					builder.setCancelable(true);

					builder.setPositiveButton("Valider",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									liste.get(liste.size() - 1).put(
											controle[2], "X");

									indiceTab++;
									if (indiceTab >5) {
										Toast.makeText(ControleFinalisationTa.this, "Contrôle achevée", Toast.LENGTH_LONG)
										.show();
									}
									displayContentProvider();
								}

							});

					builder.setNegativeButton("Refuser",
							new DialogInterface.OnClickListener() {
								public void onClick(
										final DialogInterface dialog,
										final int id) {

									liste.get(liste.size() - 1).put(
											controle[3], "X");

									indiceTab++;
									if (indiceTab >5) {
										Toast.makeText(ControleFinalisationTa.this, "Contrôle achevée", Toast.LENGTH_LONG)
										.show();
										}
									displayContentProvider();
								}
							});

					builder.show();
				}
			}
		});
		/*
		 * boutonCheck.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * 
		 * dateRealisation = new Date(); contact.put(Operation.NOM_OPERATEUR,
		 * nomPrenomOperateur[0] + " " + nomPrenomOperateur[1]);
		 * contact.put(Operation.DATE_REALISATION,
		 * dateRealisation.toGMTString()); heureRealisation.setToNow();
		 * contact.put(Operation.HEURE_REALISATION,
		 * heureRealisation.toString()); dureeMesuree +=
		 * dateRealisation.getTime() - dateDebut.getTime();
		 * contact.put(Operation.DUREE_MESUREE, dureeMesuree / 1000);
		 * cr.update(urlSeq, contact, Operation._id + " = ?", new String[] {
		 * Integer.toString(opId[indiceCourant]) }); contact.clear();
		 * 
		 * // MAJ de la durée dureeMesuree = 0; dateDebut = new Date();
		 * 
		 * // Signalement du point de controle String clauseSup = ""; String
		 * clauseTotal = null;
		 * 
		 * // Recherche des aboutissants clause = new
		 * String(Raccordement.NUMERO_COMPOSANT_TENANT + "='" + numeroCo + "'");
		 * 
		 * cursorA = cr.query(urlRac, colRac, clause, null, Raccordement._id +
		 * " ASC"); if (cursorA.moveToFirst()) {
		 * 
		 * do { if (clauseTotal == null) { clauseTotal =
		 * Raccordement.NUMERO_FIL_CABLE + "='" + cursorA.getString(cursorA
		 * .getColumnIndex(Raccordement.NUMERO_FIL_CABLE)) + "'"; } else {
		 * 
		 * clauseTotal += " OR " + Raccordement.NUMERO_FIL_CABLE + "='" +
		 * cursorA.getString(cursorA
		 * .getColumnIndex(Raccordement.NUMERO_FIL_CABLE)) + "'"; } } while
		 * (cursorA.moveToNext());
		 * 
		 * } cursorA = cr.query(urlRac, colRac, " (" + clauseTotal + ") AND " +
		 * Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT + "!='null' GROUP BY " +
		 * Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT, null,
		 * Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT);
		 * 
		 * if (cursorA.moveToFirst()) { do {
		 * 
		 * clauseSup += " OR " + Operation.RANG_1_1 + " LIKE '%" +
		 * cursorA.getString(cursorA
		 * .getColumnIndex(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT)) + "' ";
		 * 
		 * } while (cursorA.moveToNext()); }
		 * 
		 * clause = "(" + Operation.RANG_1_1 + " LIKE '%" + numeroCo + "%'" +
		 * clauseSup + " ) AND (" + Operation.GAMME + " LIKE 'Cheminement%' )";
		 * cursor = cr.query(urlSeq, columnsSeq, clause, null, Operation._id);
		 * if (cursor.moveToFirst()) {
		 * 
		 * contact.put(Operation.REALISABLE, 1); int id =
		 * cursor.getInt(cursor.getColumnIndex(Operation._id));
		 * cr.update(urlSeq, contact, clause, null); contact.clear(); }
		 * 
		 * indiceCourant++; String nextOperation = null; try { int test =
		 * opId[indiceCourant]; clause = Operation._id + "='" + test + "'";
		 * cursor = cr.query(urlSeq, columnsSeq, clause, null, Operation._id);
		 * 
		 * if (cursor.moveToFirst()) { nextOperation = cursor.getString(cursor
		 * .getColumnIndex(Operation.DESCRIPTION_OPERATION)); Intent toNext =
		 * null; if (nextOperation.startsWith("Contrôle final tête")) { toNext =
		 * new Intent(ControleFinalisationTa.this,
		 * ControleFinalisationTa.class); } else if (nextOperation
		 * .startsWith("Contrôle rétention")) { toNext = new
		 * Intent(ControleFinalisationTa.this, ControleRetentionTa.class); }
		 * else if (nextOperation .startsWith("Contrôle sertissage")) { toNext =
		 * new Intent(ControleFinalisationTa.this, ControleSertissageTa.class);
		 * } else if (nextOperation .startsWith("Contrôle final harnais")) {
		 * toNext = new Intent(ControleFinalisationTa.this,
		 * ControleFinalHarnais.class); }
		 * 
		 * if (toNext != null) {
		 * 
		 * toNext.putExtra("opId", opId); toNext.putExtra("Noms",
		 * nomPrenomOperateur); toNext.putExtra("Indice", indiceCourant);
		 * startActivity(toNext); finish(); } }
		 * 
		 * } catch (ArrayIndexOutOfBoundsException e) { Intent toNext = new
		 * Intent(ControleFinalisationTa.this, MainMenuControleur.class);
		 * toNext.putExtra("Noms", nomPrenomOperateur); toNext.putExtra("opId",
		 * opId); toNext.putExtra("Indice", indiceCourant);
		 * startActivity(toNext); finish();
		 * 
		 * } } });
		 */
		infoProduit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cursorInfo = cr.query(urlRac, colInfo,
						Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " ='"
								+ numeroCo + "' OR "
								+ Raccordement.NUMERO_COMPOSANT_TENANT + "='"
								+ numeroCo + "'", null, null);
				Intent toInfo = new Intent(ControleFinalisationTa.this,
						InfoProduit.class);
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

		// Grande pause
		grandePause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ControleFinalisationTa.this);
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
						ControleFinalisationTa.this);
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
		HashMap<String, String> element;

		if (indiceTab < 3) {
			cursorB = cr.query(urlNom, colNom, Cable.NUMERO_COMPOSANT + "='"
					+ numeroCo + "' AND " + Cable.FAMILLE_PRODUIT
					+ " LIKE '%Gaine%' ", null, Cable._id);
			if (cursorB.moveToFirst()) {

				element = new HashMap<String, String>();
				element.put(controle[0], points[indiceTab]);
				element.put(controle[1], cursorB.getString(cursorB
						.getColumnIndex(colNom[indiceTab])));
				// element.put(controle[2], "" + 1);
				// element.put(controle[3], ""+ 0);
				element.put(controle[4], "");
				liste.add(element);

			}

		} else if (indiceTab>3 && indiceTab<5) {

			element = new HashMap<String, String>();
			element.put(controle[0], points[indiceTab]);
			element.put(controle[1], cursorA.getString(cursorA
					.getColumnIndex(colRac[indiceTab])));
			// element.put(controle[2], "" + 1);
			// element.put(controle[3], ""+ 0);
			element.put(controle[4], "");
			liste.add(element);

		}

		SimpleAdapter sa = new SimpleAdapter(this, liste,
				R.layout.grid_layout_controle_finalisation_ta, controle,
				layouts);

		gridView.setAdapter(sa);

	}

	/** Bloquage du bouton retour */
	public void onBackPressed() {

	}

}
