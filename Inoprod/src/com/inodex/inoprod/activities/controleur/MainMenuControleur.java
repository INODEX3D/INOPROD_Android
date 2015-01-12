package com.inodex.inoprod.activities.controleur;

import com.inodex.inoprod.R;
import com.inodex.inoprod.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainMenuControleur extends Activity {

	// Nom de l'opérateur
	private String nomPrenomOperateur[] = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu_controleur);
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
