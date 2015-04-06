package com.inodex.inoprod.activities.magasiniers;

import java.util.Date;

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
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.inodex.inoprod.R;
import com.inodex.inoprod.activities.InfoProduit;
import com.inodex.inoprod.business.CheminementProvider;
import com.inodex.inoprod.business.Durees.Duree;
import com.inodex.inoprod.business.DureesProvider;
import com.inodex.inoprod.business.KittingProvider;
import com.inodex.inoprod.business.Production.Fil;
import com.inodex.inoprod.business.ProductionProvider;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableCheminement.Cheminement;
import com.inodex.inoprod.business.TableKittingCable.Kitting;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;
import com.inodex.inoprod.business.TimeConverter;

/**
 * Ecran affichant un ensemble de cables � regrouper
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class RegroupementCables extends Activity {

	/** Uri � manipuler */
	private Uri urlKitting = KittingProvider.CONTENT_URI;
	private Uri urlSeq = SequencementProvider.CONTENT_URI;
	private Uri urlChem = CheminementProvider.CONTENT_URI;
	private Uri urlProd = ProductionProvider.CONTENT_URI;
	private Uri urlRac = RaccordementProvider.CONTENT_URI;

	/** Tableau des op�rations � r�aliser */
	private int opId[] = null;

	/** Tableau des infos produit */
	private String labels[];

	/** Indice de l'op�ration courante */
	private int indiceCourant = 0;

	/** Numero de cheminement courant */
	private int numeroCh;
	/** Heure et dates � ajouter � la table de s�quencment */
	private Date dateDebut, dateRealisation;
	private long dureeMesuree = 0;
	private Time heureRealisation = new Time();
	/** Nom de l'op�rateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver � utiliser lors des requ�tes */
	private Cursor cursor, cursorA, cursorB;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation, numeroCom, descriptionOperation;

	/** Elements � r�cuperer de la vue */
	private GridView gridView;
	private ImageButton boutonCheck, infoProduit;
	private TextView numeroComposant, numeroChariot, repereElectrique,
			numeroCheminement, ordreRealisation;

	/** Colonnes utilis�s pour les requ�tes */
	private String columns[] = new String[] { Kitting.NUMERO_FIL_CABLE,
			Kitting.TYPE_FIL_CABLE, Kitting.REFERENCE_FABRICANT1,
			Kitting.REFERENCE_INTERNE, Kitting._id };
	private int[] layouts = new int[] { R.id.numeroFil, R.id.typeCable,
			R.id.referenceFabricant, R.id.referenceInterne };
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION };

	private String columnsKitting[] = new String[] { Kitting.NUMERO_FIL_CABLE,
			Kitting.TYPE_FIL_CABLE, Kitting.REFERENCE_FABRICANT2,
			Kitting.REFERENCE_INTERNE, Kitting.NUMERO_POSITION_CHARIOT,
			Kitting.REPERE_ELECTRIQUE, Kitting.NUMERO_COMPOSANT,
			Kitting.ORDRE_REALISATION, Kitting.LONGUEUR_FIL_CABLE,
			Kitting.UNITE, Kitting.NUMERO_CHEMINEMENT, Kitting._id,
			Kitting.NUMERO_OPERATION };

	private String colRac[] = new String[] { Raccordement.NUMERO_FIL_CABLE,
			Raccordement._id, Raccordement.NUMERO_COMPOSANT_TENANT,
			Raccordement.REPERE_ELECTRIQUE_TENANT,
			Raccordement.NUMERO_OPERATION,
			Raccordement.NUMERO_COMPOSANT_ABOUTISSANT,
			Raccordement.NUMERO_POSITION_CHARIOT,
			Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Raccordement.NUMERO_CHEMINEMENT, Raccordement.LOCALISATION1,
			Raccordement.NUMERO_SECTION_CHEMINEMENT,
			Raccordement.NUMERO_REPERE_TABLE_CHEMINEMENT };

	private String columnsChem[] = new String[] { Cheminement._id,
			Cheminement.NUMERO_SECTION_CHEMINEMENT,
			Cheminement.NUMERO_COMPOSANT_TENANT,
			Cheminement.REPERE_ELECTRIQUE_TENANT,
			Cheminement.ORDRE_REALISATION,
			Cheminement.NUMERO_COMPOSANT_ABOUTISSANT,
			Cheminement.REPERE_ELECTRIQUE_ABOUTISSANT,
			Cheminement.NUMERO_CHEMINEMNT };

	private String columnsProd[] = new String[] { Fil._id,
			Fil.DESIGNATION_PRODUIT, Fil.NUMERO_REVISION_HARNAIS, Fil.STANDARD,
			Fil.NUMERO_HARNAIS_FAISCEAUX, Fil.REFERENCE_FICHIER_SOURCE,
			Fil.NUMERO_COMPOSANT_ABOUTISSANT, Fil.NUMERO_COMPOSANT_TENANT };

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
		setContentView(R.layout.activity_regroupement_cables);

		// R�cup�ration des �l�ments
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr = getContentResolver();
		contact = new ContentValues();
		dateDebut = new Date();

		// R�cuperation des �l�ments de la vue
		gridView = (GridView) findViewById(R.id.gridview);
		numeroComposant = (TextView) findViewById(R.id.textView3);
		numeroChariot = (TextView) findViewById(R.id.textView4);
		repereElectrique = (TextView) findViewById(R.id.textView5);
		numeroCheminement = (TextView) findViewById(R.id.textView6);
		ordreRealisation = (TextView) findViewById(R.id.textView7);

		// R�cuperation du num�ro d'op�ration courant
		clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
		cursor = cr.query(urlSeq, columnsSeq, clause, null, null);
		if (cursor.moveToFirst()) {
			numeroOperation = cursor.getString(cursor
					.getColumnIndex(Operation.NUMERO_OPERATION));
			descriptionOperation = cursor.getString(cursor
					.getColumnIndex(Operation.DESCRIPTION_OPERATION));
		} else {
			Log.e("Regroupement", "Probl�me s�quencement");
		}

		// R�cup�ration du num�ro de cheminement
		numeroCom = descriptionOperation.substring(42, 45);
		clause = new String(Cheminement.NUMERO_COMPOSANT_ABOUTISSANT + "='"
				+ numeroCom + "' OR " + Cheminement.NUMERO_COMPOSANT_TENANT
				+ "='" + numeroCom + "'");
		cursorA = cr.query(urlChem, columnsChem, clause, null, null);
		if (cursorA.moveToFirst()) {
			numeroCh = cursorA.getInt(cursorA
					.getColumnIndex(Cheminement.NUMERO_CHEMINEMNT));
			cursorB = cr.query(urlRac, colRac, "("
					+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + "='"
					+ numeroCom + "' OR "
					+ Raccordement.NUMERO_COMPOSANT_TENANT + "='" + numeroCom
					+ "') AND " + Raccordement.NUMERO_POSITION_CHARIOT
					+ "!='null' ", null, Raccordement._id);
			if (cursorB.moveToFirst()) {
				try {
					numeroChariot
							.append(": "
									+ cursorB.getString(cursorB
											.getColumnIndex(Raccordement.NUMERO_POSITION_CHARIOT)));
				} catch (NullPointerException e) {
				}
			}

			// Affichage des �l�ments du regroupement en cours

			try {
				String numeroCo = cursorA.getString(cursorA
						.getColumnIndex(Cheminement.NUMERO_COMPOSANT_TENANT));
				if (numeroCo.equals(null)) {
					numeroCo = cursorA
							.getString(cursorA
									.getColumnIndex(Cheminement.NUMERO_COMPOSANT_ABOUTISSANT));
				}
				numeroComposant.append(": " + numeroCo);
			} catch (NullPointerException e) {
			}
			try {
				String rep = cursorA.getString(cursorA
						.getColumnIndex(Cheminement.REPERE_ELECTRIQUE_TENANT));
				if (rep.equals(null)) {
					rep = cursorA
							.getString(cursorA
									.getColumnIndex(Cheminement.REPERE_ELECTRIQUE_ABOUTISSANT));
				}
				repereElectrique.append(": " + rep);
			} catch (NullPointerException e) {
			}
			try {
				ordreRealisation
						.append(": "
								+ cursorA.getString(cursorA
										.getColumnIndex(Cheminement.ORDRE_REALISATION)));
			} catch (NullPointerException e) {
			}
			try {
			//	numeroCheminement.append(" " + Integer.toString(numeroCh));
			} catch (NullPointerException e) {
			}
		} else {
			Log.e("Regroupement", numeroCom);
			Log.e("Regroupement", descriptionOperation);
			Log.e("Regroupement", numeroOperation);
		}

		// Affichage des cables � regouper
		displayContentProvider();

		// Affichage du temps n�cessaire
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

				indiceCourant++;
				try {
					int test = opId[indiceCourant]; // Si OK il reste encore
					clause = Operation._id + "='" + test + "'";
					cursor = cr.query(urlSeq, columnsSeq, clause, null,
							Operation._id);
					if (cursor.moveToFirst()) {
						String firstOperation = cursor.getString(cursor
								.getColumnIndex(Operation.DESCRIPTION_OPERATION));
						Intent toNext = null;
						if (firstOperation.startsWith("D�bit du fil")) {
							toNext = new Intent(RegroupementCables.this,
									ImportCoupeCables.class);
							} else if (firstOperation.startsWith("Regroupement des")) {
								toNext = new Intent(RegroupementCables.this,
										RegroupementCables.class);
							} else if (firstOperation.startsWith("D�bit pour")) {
								 toNext = new Intent(RegroupementCables.this,
										SaisieTracabiliteComposant.class);
							} else {
								toNext = new Intent(RegroupementCables.this,
										KittingCablesComposants.class);
							}
				
					toNext.putExtra("Noms", nomPrenomOperateur);
					toNext.putExtra("opId", opId);
					toNext.putExtra("Indice", indiceCourant);
					startActivity(toNext);
					finish();
					}

				} catch (Exception e ) {
					
				}
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
				Intent toInfo = new Intent(RegroupementCables.this,
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
	 * Gen�re l'affichage en utilisant un SimpleCursorAdapter Le layout GridView
	 * est r�cup�r� puis utiliser pour afficher chacun des �l�ments
	 */
	private void displayContentProvider() {
		// Cr�ation du SimpleCursorAdapter affili� au GridView
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.grid_layout_regroupement_cables, null, columns,
				layouts);
		gridView.setAdapter(sca);
		// Requ�te dans la base
		clause = Kitting.NUMERO_COMPOSANT + " ='" + numeroCom + "'";
		cursor = cr.query(urlKitting, columns, clause, null, null);
		sca.changeCursor(cursor);

	}
}
