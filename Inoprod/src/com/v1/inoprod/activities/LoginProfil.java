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

/** Activit� qui g�re la connexion des employ�s en fonction de leur profil
 * 
 * @author Arnaud Payet
 *
 */
public class LoginProfil extends Activity {
	
	//Elements � r�cuperer depuis la vue
	private ImageButton boutonExit = null;
	private ImageButton boutonConnect = null;
	private EditText identifiant = null;
	private EditText mdp = null;
	
	//Uri de l'Annuaire
	private Uri url = AnnuaireProvider.CONTENT_URI;
	
	// Curseur et Content Resolver � utiliser lors des requ�tes
	private Cursor cursor;
	private ContentResolver cr;
	
	//Cha�nes de caract�res � manipuler pour l'identification
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
		
		
		//Connexion � un profil		
		boutonConnect= (ImageButton) findViewById(R.id.imageButton1);
		boutonConnect.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//R�cuperation des EditText
				identifiant = (EditText) findViewById(R.id.editText1);
				mdp = (EditText) findViewById(R.id.editText2);
				cr= getContentResolver(); 
				
				//Recuperation du texte renseign�
				idRenseigne= identifiant.getText().toString();
				mdpRenseigne = mdp.getText().toString();
				//Cas o� un des champs � renseigner est vide
				if (idRenseigne.equals("")) {
					Toast.makeText(LoginProfil.this, "Veuillez vous identifier", Toast.LENGTH_SHORT).show();
				} /*else if (mdpRenseigne.equals(null)) {
					Toast.makeText(LoginProfil.this, "Veuillez entrer votre mot de passe", Toast.LENGTH_SHORT).show();
				}*/ else
				{
					//Chaine correspondant � la clause � utiliser lors de la sel�ction
					String clause = new String ( Employe.EMPLOYE_NOM + "='" + idRenseigne + "'");
					//Recherche de l'employ� correspondant
				cursor = cr.query(url, columns,clause , null, null);
				if (cursor.moveToFirst()) {
					
					profil =cursor.getString(cursor.getColumnIndex(Employe.EMPLOYE_METIER));
					//Redirection en fonction du profil connect�
					if (profil.equals("C�bleur")) {
						//TO DO
					} else if (profil.equals("Contr�leur")) {
						//TO DO
					} else if (profil.equals("Pr�parateur")) {
						//TO DO
					} else if (profil.equals("Magasinier")) {
						Intent toMag = new Intent(LoginProfil.this, MainMenuMagasinier.class );	
						startActivity(toMag);
					} else {
						Toast.makeText(LoginProfil.this, "M�tier non renseign�", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(LoginProfil.this, "Identifiant ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
					
				}
				}
			}
		});
	}
}
