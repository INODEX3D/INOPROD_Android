package com.v1.inoprod.activities.magasiniers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;

import com.v1.inoprod.R;
import com.v1.inoprod.activities.Annuaire;
import com.v1.inoprod.activities.MainActivity;
import com.v1.inoprod.business.AnnuaireProvider;
import com.v1.inoprod.business.SequencementProvider;
import com.v1.inoprod.business.Production.Fil;
import com.v1.inoprod.business.TableSequencement.Operation;

public class MainMenuMagasinier extends Activity {
	
	//Bouton qui permet de revenir au menu principal
	private ImageButton boutonExit = null;
	
	private ImageButton boutonCheck = null;

	// Nom de l'opérateur
	private String nomPrenomOperateur[] = null;
	
	private String columns[] = {Operation._id, Operation.RANG_1_1, Operation.GAMME  };
	private int layouts[] = { R.id.ordreOperations , R.id.operationsRealiser };
	
	//Uri de la table de sequencement
		private Uri url = SequencementProvider.CONTENT_URI;
		// Curseur et Content Resolver à utiliser lors des requêtes
		private Cursor cursor;
		private ContentResolver cr; 	
		private String clause;

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu_magasinier);
		
		cr= getContentResolver(); 
		//Affichage de l'annuaire
		displayContentProvider();
		
		//Retour menu principal
				boutonExit = (ImageButton) findViewById(R.id.exitButton1);	
				boutonExit.setOnClickListener( new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent toMain = new Intent(MainMenuMagasinier.this, MainActivity.class );		
						startActivity(toMain);					
					}
				});
				
				//Etape suivante
				boutonCheck = (ImageButton) findViewById(R.id.imageButton1);	
				boutonCheck.setOnClickListener( new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if (cursor.moveToFirst()) {
						Intent toNext = new Intent(MainMenuMagasinier.this, ImportCoupeCables.class );
						toNext.putExtra("opId", cursor.getInt(cursor.getColumnIndex(Operation._id)));
						toNext.putExtra("Noms", nomPrenomOperateur);
						startActivity(toNext);	
						}
					}
				});
	}

	private void displayContentProvider() {
		//Création du SimpleCursorAdapter affilié au GridView 
		SimpleCursorAdapter sca = new SimpleCursorAdapter( this, R.layout.grid_layout_menu_magasinier, null, columns, layouts);
		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(sca);
		//Requête dans la table sequencement
		clause = clause = new String(Operation.GAMME + "='" + "Kitting" + "'" + " GROUP BY " + Operation.RANG_1_1);
		cursor=cr.query(url, columns, clause, null, null);
		sca.changeCursor(cursor);
				
		
		
	}

	// Récupération du nom de l'operateur
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				nomPrenomOperateur = intent.getStringArrayExtra("Noms");
			}
		}
	}
}
