package com.v1.inoprod.activities.magasiniers;

import com.v1.inoprod.R;
import com.v1.inoprod.R.layout;
import com.v1.inoprod.business.AnnuaireProvider;
import com.v1.inoprod.business.KittingProvider;
import com.v1.inoprod.business.Durees.Duree;
import com.v1.inoprod.business.DureesProvider;
import com.v1.inoprod.business.AnnuairePersonel.Employe;
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
import android.widget.TextView;

public class DebitCables extends Activity {
	
	private TextView temps = null;
	
	//Uri de l'Annuaire
	private Uri urlDuree = DureesProvider.CONTENT_URI;
	private Uri urlKitting = KittingProvider.CONTENT_URI;
	private Uri  urlSequencement = SequencementProvider.CONTENT_URI;
	
	// Curseur et Content Resolver à utiliser lors des requêtes
		private Cursor cursor;
		private ContentResolver cr;
		private GridView gridView;
		
		private String columnsDuree[] = new String[] { Duree._id, Duree.CODE_OPERATION , Duree.DUREE_THEORIQUE};
		
		private String columns[] = new String[] { Kitting.NUMERO_FIL_CABLE, Kitting.NUMERO_POSITION_CHARIOT, Kitting.REPERE_ELECTRIQUE_TENANT, 
				Kitting.NUMERO_CONNECTEUR_TENANT, Kitting.ORDRE_REALISATION, Kitting.LONGUEUR_FIL_CABLE, Kitting.UNITE};
		private int[] layouts = new int[] { R.id.numeroFil, R.id.positionChariot, R.id.repereElectrique, R.id.numeroConnecteur, R.id.ordreRealisation,
				R.id.longueurCoupe, R.id.uniteMesure};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debit_cables);
		cr= getContentResolver();
		gridView = (GridView) findViewById(R.id.gridview);
	
		setTemps(2);
		
	}

	private void setTemps(float operation) {
		temps = (TextView) findViewById(R.id.temps);
		 
		
		//Chaine correspondant à la clause à utiliser lors de la seléction
		String clause = new String ( Duree.CODE_OPERATION + "='" + operation + "'");
		
		cursor = cr.query(urlDuree, columnsDuree,clause , null, null);
		if (cursor.moveToFirst()) {
			temps.setText(cursor.getString(cursor.getColumnIndex(Duree.DUREE_THEORIQUE)));
		} 
		
	}
	
	/** Genère l'affichage de l'annuaire en utilisant un SimpleCursorAdapter
	 * Le layout GridView est récupéré puis utiliser pour afficher chacun des éléments
	 */
	private void displayContentProvider() {
		//Création du SimpleCursorAdapter affilié au GridView 
		SimpleCursorAdapter sca = new SimpleCursorAdapter( this, R.layout.grid_layout_debit_cable, null, columns, layouts);
		
		gridView.setAdapter(sca);
		//Requête dans la base Annuaire
		cursor=cr.query(urlKitting, columns, null, null, null);
		sca.changeCursor(cursor);
		
		
		
	}
	
}
