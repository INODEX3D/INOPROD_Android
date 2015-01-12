package com.inodex.inoprod.activities.magasiniers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableSequencement.Operation;
import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;


/**
 * Activit� demandant l'import du ficier debit cable dans la machine de coupe
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class ImportCoupeCables extends Activity {
	
	/**Elements de la vue */
	private ImageButton boutonCheck = null;
	private TextView operation = null;
	/** Tableau des op�rations � r�aliser */
	private int opId[];
	/** Indice de l'op�ration courante */
	private int indiceCourant;
	
	/** Curseur et Content Resolver � utiliser lors des requ�tes */
	private Cursor cursor;
	private ContentResolver cr;

	/** Clause � utiliser lors des requ�tes */
	private String clause;

	/** Uri de la table de sequencement */
	private Uri urlSeq = SequencementProvider.CONTENT_URI;

	/** Nom de l'op�rateur */
	private String nomPrenomOperateur[] = null;
	
	/** Colonnes utilis�s pour les requ�tes */
	private String columns[] = {Operation._id, Operation.RANG_1_1};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import_coupe_cables);
		
		//R�cup�ration des elements 
		Intent intent = getIntent();
		indiceCourant = intent.getIntExtra("Indice", 0);
		nomPrenomOperateur = intent.getStringArrayExtra("Noms");
		opId = intent.getIntArrayExtra("opId");
		cr= getContentResolver(); 
		
		//Affichage du rang de l'op�ration
		operation = (TextView) findViewById(R.id.textView3);
		clause =  new String(Operation._id + "='" + opId[indiceCourant] + "'" );		
		cursor = cr.query(urlSeq, columns, clause, null, null);
		if (cursor.moveToFirst()){
		operation.setText(cursor.getString(cursor.getColumnIndex(Operation.RANG_1_1)));
		}
	
		
		//Etape suivante
		boutonCheck = (ImageButton) findViewById(R.id.imageButton1);	
		boutonCheck.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent toNext = new Intent(ImportCoupeCables.this, DebitCables.class );	
				toNext.putExtra("Noms", nomPrenomOperateur);
				toNext.putExtra("opId", opId);
				toNext.putExtra("Indice", indiceCourant);
				startActivity(toNext);					
			}
		});
	}
	
	/**Bloquage du bouton retour */
	public void onBackPressed() {

	}
	
	

}
