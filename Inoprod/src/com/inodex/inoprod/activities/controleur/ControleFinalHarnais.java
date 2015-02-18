package com.inodex.inoprod.activities.controleur;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;
import com.inodex.inoprod.business.RaccordementProvider;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableRaccordement.Raccordement;
import com.inodex.inoprod.business.TableSequencement.Operation;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

public class ControleFinalHarnais extends Activity {
	
	/** Elements à récuperer de la vue */
	private TextView designationProduit, numeroHarnais, numeroSerie, numeroTraitement,
			numeroRevision, standard, referenceFichierSource;
	private ImageButton boutonCheck, infoProduit, boutonAnnuler, boutonAide;
	private ImageButton petitePause, grandePause;
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

	/** Colonnes utilisés pour les requêtes */
	private String columnsSeq[] = new String[] { Operation._id,
			Operation.GAMME, Operation.RANG_1_1, Operation.NUMERO_OPERATION,
			Operation.NOM_OPERATEUR, Operation.DATE_REALISATION,
			Operation.HEURE_REALISATION, Operation.DESCRIPTION_OPERATION,
			Operation.DUREE_MESUREE };

	private int layouts[] = new int[] { R.id.pointsVerifier,
			R.id.valeurAttendue, 
			R.id.commentaires };
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
			Raccordement.NUMERO_POSITION_CHARIOT };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_controle_final_harnais);
		
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
	}
}
