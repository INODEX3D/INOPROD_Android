package com.v1.inoprod.activities;




import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.v1.inoprod.R;
import com.v1.inoprod.business.AnnuaireProvider;
import com.v1.inoprod.business.AnnuairePersonel.Employe;

public class MainActivity extends Activity {
	
	private ImageButton boutonAnnuaire = null;
	private ImageButton boutonAide = null;
	private ImageButton boutonExit = null;
	private ImageButton boutonLogin = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Accés à l'annuaire
		boutonAnnuaire = (ImageButton) findViewById(R.id.imageButton2);	
		boutonAnnuaire.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent toAnnuaire = new Intent(MainActivity.this, Annuaire.class );
				
				startActivity(toAnnuaire);
				
			}
		});
		
		//Accés à l'aide
		boutonAide = (ImageButton) findViewById(R.id.imageButton3);	
		boutonAide.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent toAide = new Intent(MainActivity.this, MenuAide.class );
				
				startActivity(toAide);
				
			}
		});
		
		//Accés au login
				boutonLogin = (ImageButton) findViewById(R.id.imageButton1);	
				boutonLogin.setOnClickListener( new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent toLogin = new Intent(MainActivity.this, LoginProfil.class );
						
						startActivity(toLogin);
						
					}
				});
		
		
		//Quitter l'application
		boutonExit = (ImageButton) findViewById(R.id.imageButton4);	
		boutonExit.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		
		try {
			insertRecords();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "fichier non lu", Toast.LENGTH_SHORT).show();
		}

	}
	
	
	private void insertRecords() throws IOException { 

		InputStream input = this.getResources().openRawResource(R.raw.annuaire_personel);
		POIFSFileSystem fs = new POIFSFileSystem( input );
	
		
        HSSFWorkbook wb = new HSSFWorkbook(fs);

        HSSFSheet sheet =  wb.getSheetAt(0);
        


        // Iterate over each row in the sheet
        Iterator rows = sheet.rowIterator(); 
        rows.next();
     
        ContentValues contact = new ContentValues();
        while( rows.hasNext() ) {           
            HSSFRow row = (HSSFRow) rows.next();

            contact.put(Employe.EMPLOYE_NOM,row.getCell(1).toString() );
            contact.put(Employe.EMPLOYE_PRENOM,row.getCell(2).toString() );
            contact.put(Employe.EMPLOYE_METIER,row.getCell(3).toString() );
            getContentResolver().insert(AnnuaireProvider.CONTENT_URI, contact);
            contact.clear();

		
	}
		
	}
}
