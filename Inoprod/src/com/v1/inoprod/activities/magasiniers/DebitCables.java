package com.v1.inoprod.activities.magasiniers;

import com.v1.inoprod.R;
import com.v1.inoprod.R.layout;
import com.v1.inoprod.business.AnnuaireProvider;
import com.v1.inoprod.business.Durees.Duree;
import com.v1.inoprod.business.DureesProvider;
import com.v1.inoprod.business.AnnuairePersonel.Employe;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DebitCables extends Activity {
	
	private TextView temps = null;
	
	//Uri de l'Annuaire
	private Uri urlDuree = DureesProvider.CONTENT_URI;
	
	// Curseur et Content Resolver à utiliser lors des requêtes
		private Cursor cursor;
		private ContentResolver cr;
		
		private String columnsDuree[] = new String[] { Duree._id, Duree.CODE_OPERATION , Duree.DUREE_THEORIQUE};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debit_cables);
	
		setTemps(2);
		
	}

	private void setTemps(float operation) {
		temps = (TextView) findViewById(R.id.temps);
		cr= getContentResolver(); 
		
		//Chaine correspondant à la clause à utiliser lors de la seléction
		String clause = new String ( Duree.CODE_OPERATION + "='" + operation + "'");
		
		cursor = cr.query(urlDuree, columnsDuree,clause , null, null);
		if (cursor.moveToFirst()) {
			temps.setText(cursor.getString(cursor.getColumnIndex(Duree.DUREE_THEORIQUE)));
		} 
		
	}
}
