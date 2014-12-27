package com.v1.inoprod.activities;

import com.v1.inoprod.R;
import com.v1.inoprod.R.layout;
import com.v1.inoprod.activities.magasiniers.MainMenuMagasinier;
import com.v1.inoprod.business.AnnuaireProvider;
import com.v1.inoprod.business.AnnuairePersonel.Employe;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

/** Activité qui gère la connexion des employés en fonction de leur profil
 * 
 * @author Arnaud Payet
 *
 */
public class LoginProfil extends Activity {
	
	//Elements à récuperer depuis la vue
	private ImageButton boutonExit = null;
	private ImageButton boutonConnect = null;
	private EditText identifiant = null;
	private EditText mdp = null;
	
	//Uri de l'Annuaire
	private Uri url = AnnuaireProvider.CONTENT_URI;
	
	// Curseur et Content Resolver à utiliser lors des requêtes
	private Cursor cursor;
	private ContentResolver cr;
	
	//Chaînes de caractères à manipuler pour l'identification
	String idRenseigne, mdpRenseigne, profil;
	private String columns[] = new String[] { Employe._id, Employe.EMPLOYE_NOM, Employe.EMPLOYE_PRENOM, Employe.EMPLOYE_METIER };
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_profil);
		
		
		//Retour menu principal
		boutonExit = (ImageButton) findViewById(R.id.exitButton1);	
		boutonExit.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent toMain = new Intent(LoginProfil.this, MainActivity.class );
				
				startActivity(toMain);
				
			}
		});
		
		
		//Connexion à un profil		
		boutonConnect= (ImageButton) findViewById(R.id.imageButton1);
		boutonConnect.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Récuperation des EditText
				identifiant = (EditText) findViewById(R.id.editText1);
				mdp = (EditText) findViewById(R.id.editText2);
				cr= getContentResolver(); 
				
				//Recuperation du texte renseigné
				idRenseigne= identifiant.getText().toString();
				mdpRenseigne = mdp.getText().toString();
				//Cas où un des champs à renseigner est vide
				if (idRenseigne.equals("")) {
					Toast.makeText(LoginProfil.this, "Veuillez vous identifier", Toast.LENGTH_SHORT).show();
				} /*else if (mdpRenseigne.equals(null)) {
					Toast.makeText(LoginProfil.this, "Veuillez entrer votre mot de passe", Toast.LENGTH_SHORT).show();
				}*/ else
				{
					//Chaine correspondant à la clause à utiliser lors de la seléction
					String clause = new String ( Employe.EMPLOYE_NOM + "='" + idRenseigne + "'");
					//Recherche de l'employé correspondant
				cursor = cr.query(url, columns,clause , null, null);
				if (cursor.moveToFirst()) {
					
					profil =cursor.getString(cursor.getColumnIndex(Employe.EMPLOYE_METIER));
					//Redirection en fonction du profil connecté
					if (profil.equals("Câbleur")) {
						//TO DO
					} else if (profil.equals("Contrôleur")) {
						//TO DO
					} else if (profil.equals("Préparateur")) {
						//TO DO
					} else if (profil.equals("Magasinier")) {
						Intent toMag = new Intent(LoginProfil.this, MainMenuMagasinier.class );	
						startActivity(toMag);
					} else {
						Toast.makeText(LoginProfil.this, "Métier non renseigné", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(LoginProfil.this, "Identifiant ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
					
				}
				}
			}
		});
	}
}
