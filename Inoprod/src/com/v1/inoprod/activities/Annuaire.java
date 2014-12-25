package com.v1.inoprod.activities;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.v1.inoprod.R;
import com.v1.inoprod.business.AnnuairePersonel.Employe;
import com.v1.inoprod.business.AnnuaireProvider;

public class Annuaire extends Activity {
	
	private ImageButton boutonExit = null;
	String columns[] = new String[] { Employe._id, Employe.EMPLOYE_NOM, Employe.EMPLOYE_PRENOM, Employe.EMPLOYE_METIER };

	int[] layouts = new int[] {R.id.idEmp, R.id.nom, R.id.prenom, R.id.metier };


	
	
	
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_annuaire);
		
		
		/*
	
			try {
				insertRecords();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(this, "fichier non lu", Toast.LENGTH_SHORT).show();
			}
	
			
*/
		displayContentProvider();
		
		
		
		
		
		//Retour menu principal
		boutonExit = (ImageButton) findViewById(R.id.imageButton1);	
		boutonExit.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent toMain = new Intent(Annuaire.this, MainActivity.class );
				
				startActivity(toMain);
				
			}
		});
	}

	private void displayContentProvider() {
		SimpleCursorAdapter sca = new SimpleCursorAdapter( this, R.layout.grid_layout, null, columns, layouts);
		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(sca);
		
		ContentResolver cr=getContentResolver(); 
		Uri url = AnnuaireProvider.CONTENT_URI;
		Cursor cursor=cr.query(url, columns, null, null, null);
		
		sca.changeCursor(cursor);
		
		
		
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
