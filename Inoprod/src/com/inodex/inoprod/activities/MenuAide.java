package com.inodex.inoprod.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.inodex.inoprod.R;

/**
 * Activité qui affiche le menu d'aide qui comporte les divers boutons
 * utilisables au sein de l'application
 * 
 * @author Arnaud Payet
 * @version 1.1
 */
public class MenuAide extends Activity {

	/** Bouton de retour récupéré depuis la vue */
	private ImageButton boutonExit = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_aide);

		// Retour menu principal
		boutonExit = (ImageButton) findViewById(R.id.exitButton1);
		boutonExit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent toMain = new Intent(MenuAide.this, Inoprod.class);
				startActivity(toMain);
				finish();

			}
		});

	}
}
