package com.v1.inoprod.activities;

import com.v1.inoprod.R;
import com.v1.inoprod.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public class LoginProfil extends Activity {
	
	
	private ImageButton boutonExit = null;

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
	}
}
