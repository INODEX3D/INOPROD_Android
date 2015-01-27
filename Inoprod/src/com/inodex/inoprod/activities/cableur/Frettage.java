package com.inodex.inoprod.activities.cableur;

import java.util.Date;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class Frettage extends Activity {

	/** Elements à récuperer de la vue */
	private TextView typeFrette, nombrePoints, zone, referenceInterne,
			referenceFabricant;
	private ImageButton boutonCheck, infoProduit, boutonAnnuler, boutonAide;
	private GridView gridView;

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
	private Date dateRealisation = new Date();
	private Time heureRealisation = new Time();

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor, cursorA;
	private ContentResolver cr;
	private ContentValues contact;

	private String clause, numeroOperation;;
	private boolean prodAchevee;

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION };

	private int layouts[] = new int[] { R.id.numeroSection,
			R.id.numeroSegregation, R.id.numeroConnecteur,
			R.id.repereElectrique, R.id.typeSupportAboutissant,
			R.id.zoneLocalisation1, R.id.localisationZoneFrettage,
			R.id.typeSupportAboutissant, R.id.zoneLocalisation2 };

	private String columnsRac[] = new String[] { /* A Voir */};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_frettage);
		// Récupération des éléments
				Intent intent = getIntent();
				indiceCourant = intent.getIntExtra("Indice", 0);
				nomPrenomOperateur = intent.getStringArrayExtra("Noms");
				opId = intent.getIntArrayExtra("opId");
				cr = getContentResolver();

				// initialisation de la production
				prodAchevee = false;

				// Récuperation des éléments de la vue
				gridView = (GridView) findViewById(R.id.gridview);
				
				referenceInterne = (TextView) findViewById(R.id.textView7);
				
				referenceFabricant = (TextView) findViewById(R.id.textView6);
				zone= (TextView) findViewById(R.id.textView5);
				typeFrette=(TextView) findViewById(R.id.textView3);
				nombrePoints=(TextView) findViewById(R.id.textView4);
				boutonAide = (ImageButton) findViewById(R.id.imageButton2);
				boutonAnnuler = (ImageButton) findViewById(R.id.imageButton4);
				boutonCheck = (ImageButton) findViewById(R.id.imageButton3);
				infoProduit = (ImageButton) findViewById(R.id.infoButton1);
				

				// Récuperation du numéro d'opération courant
				clause = new String(Operation._id + "='" + opId[indiceCourant] + "'");
				cursor = cr.query(urlSeq, columnsSeq, clause, null, Operation._id
						+ " ASC");
				if (cursor.moveToFirst()) {
					numeroOperation = cursor.getString(cursor
							.getColumnIndex(Operation.NUMERO_OPERATION));
				}

				// Affichage du contenu
				displayContentProvider();

				// Etape suivante

				// Info Produit
			}

			private void displayContentProvider() {
				// Création du SimpleCursorAdapter affilié au GridView
				SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
						R.layout.grid_layout_frettage, null,
						null, layouts);

				gridView.setAdapter(sca);
				// MAJ Table de sequencement
				contact.put(Operation.NOM_OPERATEUR, nomPrenomOperateur[0] + " "
						+ nomPrenomOperateur[1]);
				contact.put(Operation.DATE_REALISATION, dateRealisation.toGMTString());
				heureRealisation.setToNow();
				contact.put(Operation.HEURE_REALISATION, heureRealisation.toString());
				cr.update(urlSeq, contact, Operation._id + " = ?",
						new String[] { Integer.toString(opId[indiceCourant]) });
				contact.clear();

			}

			/** Bloquage du bouton retour */
			public void onBackPressed() {

			}

		}
