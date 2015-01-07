package com.v1.inoprod.activities.magasiniers;

import com.v1.inoprod.R;
import com.v1.inoprod.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class ImportCoupeCables extends Activity {
	
	private ImageButton boutonCheck = null;
	private TextView operation = null;
	
	int operationId;

	// Nom de l'opérateur
	private String nomPrenomOperateur[] = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import_coupe_cables);
		
		//Etape suivante
		boutonCheck = (ImageButton) findViewById(R.id.imageButton1);	
		boutonCheck.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent toNext = new Intent(ImportCoupeCables.this, DebitCables.class );	
				toNext.putExtra("Noms", nomPrenomOperateur);
				startActivity(toNext);					
			}
		});
	}
	
	
	// Récupération du nom de l'operateur
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
			 nomPrenomOperateur = intent.getStringArrayExtra("Noms");
			 operationId = intent.getIntExtra("opId", 1);
			 
			}
		}
	}
}
