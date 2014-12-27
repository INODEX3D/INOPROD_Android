package com.v1.inoprod.activities;




import com.v1.inoprod.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

/** Activité qui affiche le menu d'aide qui comporte les divers boutons utilisables au sein de l'application
 * 
 * @author Arnaud Payet
 *
 */
public class MenuAide extends Activity {
	
	//Bouton de retour récupéré depuis la vue
	private ImageButton boutonExit = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_aide);
		
		
		//Retour menu principal
				boutonExit = (ImageButton) findViewById(R.id.exitButton1);	
				boutonExit.setOnClickListener( new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent toMain = new Intent(MenuAide.this, MainActivity.class );						
						startActivity(toMain);
						
					}
				});
				
	}
}
