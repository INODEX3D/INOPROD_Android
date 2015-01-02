package com.v1.inoprod.activities.magasiniers;

import com.v1.inoprod.R;
import com.v1.inoprod.R.layout;
import com.v1.inoprod.business.KittingProvider;
import com.v1.inoprod.business.SequencementProvider;
import com.v1.inoprod.business.TableKittingCable.Kitting;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;

public class RegroupementCables extends Activity {
	
	private Uri urlKitting = KittingProvider.CONTENT_URI;
	private Uri  urlSequencement = SequencementProvider.CONTENT_URI;
	
	// Curseur et Content Resolver à utiliser lors des requêtes
			private Cursor cursor;
			private ContentResolver cr;
			private GridView gridView;
			
			
			private String columns[] = new String[] { Kitting.NUMERO_FIL_CABLE, Kitting.TYPE_FIL_CABLE, Kitting.REFERENCE_FABRICANT1 , Kitting.REFERENCE_INTERNE};
			private int[] layouts = new int[] { R.id.numeroFil, R.id.typeCable, R.id.referenceFabricant, R.id.referenceInterne};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regroupement_cables);
		
		cr= getContentResolver();
		gridView = (GridView) findViewById(R.id.gridview);
	
	}
	
	
	/** Genère l'affichage de l'annuaire en utilisant un SimpleCursorAdapter
	 * Le layout GridView est récupéré puis utiliser pour afficher chacun des éléments
	 */
	private void displayContentProvider() {
		//Création du SimpleCursorAdapter affilié au GridView 
		SimpleCursorAdapter sca = new SimpleCursorAdapter( this, R.layout.grid_layout_regroupement_cables, null, columns, layouts);
		
		gridView.setAdapter(sca);
		//Requête dans la base Annuaire
		cursor=cr.query(urlKitting, columns, null, null, null);
		sca.changeCursor(cursor);
		
		
		
	}
}
