package com.v1.inoprod.activities.cableur;

import com.v1.inoprod.R;
import com.v1.inoprod.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainMenuCableur extends Activity {
	
	//Nom de l'opérateur
	private String nomPrenomOperateur[]= null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu_cableur);
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
