package com.inodex.inoprod.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inodex.inoprod.R;

/**
 * Acitivité qui affiche les infos relatives au produit en cours d'utilisation
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class InfoProduit extends Activity {

	/** Labels permettant l'affichage des divers données du produit */
	private TextView designationProduit, numeroHarnais, standardFabrication,
			numeroSerie, numeroTraitement, numeroRevisionHarnais,
			referenceFichierSource;

	/**
	 * Ensemble des données récupérees depuis l'Intent sous forme de chaines de
	 * caractères
	 */
	private String labels[];

	/** Bouton de retour en arrière */
	private ImageButton boutonExit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info_produit);

		// Récupération des infos du produit
		Intent intent = getIntent();
		labels = intent.getStringArrayExtra("Labels");

		// Récupération de la vue
		designationProduit = (TextView) findViewById(R.id.textView2);
		numeroHarnais = (TextView) findViewById(R.id.textView5);
		standardFabrication = (TextView) findViewById(R.id.textView6);
		numeroSerie = (TextView) findViewById(R.id.textView2a);
		numeroTraitement = (TextView) findViewById(R.id.textView5a);
		numeroRevisionHarnais = (TextView) findViewById(R.id.textView6a);
		referenceFichierSource = (TextView) findViewById(R.id.textView3);

		// Affichae des infos sur le produit
		designationProduit.append(" : " +labels[0]);
		numeroHarnais.append(" " +labels[1]);
		standardFabrication.append(" " +labels[2]);
		numeroSerie.append(" " +labels[3]);
		numeroTraitement.append(" " +labels[4]);
		numeroRevisionHarnais.append(" : " +labels[5]);
		referenceFichierSource.append(" " +labels[6]);

		// Retour à l'activité précédente
		boutonExit = (ImageButton) findViewById(R.id.exitButton1);
		boutonExit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();

			}
		});

	}
}
