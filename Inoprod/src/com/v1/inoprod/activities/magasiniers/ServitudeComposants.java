package com.v1.inoprod.activities.magasiniers;

import com.v1.inoprod.R;
import com.v1.inoprod.R.layout;
import com.v1.inoprod.business.BOMProvider;
import com.v1.inoprod.business.KittingProvider;
import com.v1.inoprod.business.SequencementProvider;
import com.v1.inoprod.business.TableBOM.BOM;
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

public class ServitudeComposants extends Activity {
	
	private Uri urlBOM = BOMProvider.CONTENT_URI;
	private Uri  urlSequencement = SequencementProvider.CONTENT_URI;
	
	// Curseur et Content Resolver à utiliser lors des requêtes
	private Cursor cursor;
	private ContentResolver cr;
	private GridView gridView;
	
	
	private String columns[] = new String[] { BOM.REPERE_ELECTRIQUE_TENANT, 
			BOM.NUMERO_CONNECTEUR_TENANT, BOM.NUMERO_POSITION_CHARIOT, BOM.ORDRE_REALISATION, BOM.QUANTITE,
			BOM.UNITE, BOM.NUMERO_LOT_SCANNE};
	private int[] layouts = new int[] { R.id.repereElectrique, R.id.numeroConnecteur, R.id.positionChariot,
			R.id.quantite, R.id.uniteMesure, R.id.numeroLot};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_servitude_composants);
		
		cr= getContentResolver();
		gridView = (GridView) findViewById(R.id.gridview);
	}
	
	private void displayContentProvider() {
		//Création du SimpleCursorAdapter affilié au GridView 
		SimpleCursorAdapter sca = new SimpleCursorAdapter( this, R.layout.grid_layout_servitude_composants, null, columns, layouts);
		
		gridView.setAdapter(sca);
		//Requête dans la base Annuaire
		cursor=cr.query(urlBOM, columns, null, null, null);
		sca.changeCursor(cursor);
		
		
		
	}
}
