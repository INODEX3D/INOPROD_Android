package com.v1.inoprod.activities;




import com.v1.inoprod.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;


public class MenuAide extends Activity {
	
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
