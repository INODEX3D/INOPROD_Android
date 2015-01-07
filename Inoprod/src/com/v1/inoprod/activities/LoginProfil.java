package com.v1.inoprod.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.v1.inoprod.R;
import com.v1.inoprod.activities.cableur.MainMenuCableur;
import com.v1.inoprod.activities.controleur.MainMenuControleur;
import com.v1.inoprod.activities.magasiniers.MainMenuMagasinier;
import com.v1.inoprod.activities.preparateur.MainMenuPreparateur;
import com.v1.inoprod.business.AnnuairePersonel.Employe;
import com.v1.inoprod.business.AnnuaireProvider;

/**
 * Activité qui gère la connexion des employés en fonction de leur profil
 * 
 * @author Arnaud Payet
 * 
 */
public class LoginProfil extends Activity {

	// Elements à récuperer depuis la vue
	private ImageButton boutonExit = null;
	private ImageButton boutonConnect = null;
	private EditText identifiant = null;
	private EditText mdp = null;
	private Button scan = null;

	// Uri de l'Annuaire
	private Uri url = AnnuaireProvider.CONTENT_URI;

	// Curseur et Content Resolver à utiliser lors des requêtes
	private Cursor cursor;
	private ContentResolver cr;

	// Chaînes de caractères à manipuler pour l'identification
	String idRenseigne, mdpRenseigne, profil;
	private String columns[] = new String[] { Employe._id, Employe.EMPLOYE_NOM,
			Employe.EMPLOYE_PRENOM, Employe.EMPLOYE_METIER };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_profil);
		identifiant = (EditText) findViewById(R.id.editText1);
		mdp = (EditText) findViewById(R.id.editText2);

		// Scan de l'identifiant
		scan = (Button) findViewById(R.id.scan);
		scan.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent(
							"com.google.zxing.client.android.SCAN");
					intent.setPackage("com.google.zxing.client.android");
					intent.putExtra(
							"com.google.zxing.client.android.SCAN.SCAN_MODE",
							"QR_CODE_MODE");
					startActivityForResult(intent, 0);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(
							LoginProfil.this,
							"Impossible de trouver une application pour gérer le scan",
							Toast.LENGTH_SHORT).show();
				}

			}
		});

		// Retour menu principal
		boutonExit = (ImageButton) findViewById(R.id.exitButton1);
		boutonExit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent toMain = new Intent(LoginProfil.this, MainActivity.class);

				startActivity(toMain);

			}
		});

		// Connexion à un profil
		boutonConnect = (ImageButton) findViewById(R.id.imageButton1);
		boutonConnect.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Récuperation des EditText

				cr = getContentResolver();

				// Recuperation du texte renseigné
				idRenseigne = identifiant.getText().toString();
				mdpRenseigne = mdp.getText().toString();
				// Cas où un des champs à renseigner est vide
				if (idRenseigne.equals("")) {
					Toast.makeText(LoginProfil.this,
							"Veuillez vous identifier", Toast.LENGTH_SHORT)
							.show();
				} /*
				 * else if (mdpRenseigne.equals(null)) {
				 * Toast.makeText(LoginProfil.this,
				 * "Veuillez entrer votre mot de passe",
				 * Toast.LENGTH_SHORT).show(); }
				 */else {
					// Chaine correspondant à la clause à utiliser lors de la
					// seléction
					String clause = new String(Employe.EMPLOYE_NOM + "='"
							+ idRenseigne + "'");
					// Recherche de l'employé correspondant
					cursor = cr.query(url, columns, clause, null, null);
					if (cursor.moveToFirst()) {

						profil = cursor.getString(cursor
								.getColumnIndex(Employe.EMPLOYE_METIER));
						String noms[] = new String[] {
								cursor.getString(cursor
										.getColumnIndex(Employe.EMPLOYE_NOM)),
								cursor.getString(cursor
										.getColumnIndex(Employe.EMPLOYE_PRENOM)) };
						// Redirection en fonction du profil connecté
						if (profil.equals("Câbleur")) {
							Intent toCab = new Intent(LoginProfil.this,
									MainMenuCableur.class);
							toCab.putExtra("Noms", noms);
							startActivity(toCab);
						} else if (profil.equals("Contrôleur")) {
							Intent toCon = new Intent(LoginProfil.this,
									MainMenuControleur.class);
							toCon.putExtra("Noms", noms);
							startActivity(toCon);
						} else if (profil.equals("Préparateur")) {
							Intent toPre = new Intent(LoginProfil.this,
									MainMenuPreparateur.class);
							toPre.putExtra("Noms", noms);
							startActivity(toPre);
						} else if (profil.equals("Magasinier")) {
							Intent toMag = new Intent(LoginProfil.this,
									MainMenuMagasinier.class);
							toMag.putExtra("Noms", noms);
							startActivity(toMag);
						} else {
							Toast.makeText(LoginProfil.this,
									"Métier non renseigné", Toast.LENGTH_SHORT)
									.show();
						}
					} else {
						Toast.makeText(LoginProfil.this,
								"Identifiant ou mot de passe incorrect",
								Toast.LENGTH_SHORT).show();

					}
				}
			}
		});
	}

	// Récupération du code barre scanné
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				identifiant.setText(contents);
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(LoginProfil.this,
						"Echec du scan de l'identifiant", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

}
