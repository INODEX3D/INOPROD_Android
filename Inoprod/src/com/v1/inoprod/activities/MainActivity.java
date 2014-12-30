package com.v1.inoprod.activities;




import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.v1.inoprod.R;
import com.v1.inoprod.business.AnnuaireProvider;
import com.v1.inoprod.business.AnnuairePersonel.Employe;
import com.v1.inoprod.business.Durees.Duree;
import com.v1.inoprod.business.DureesProvider;
import com.v1.inoprod.business.Nomenclature.Cable;
import com.v1.inoprod.business.NomenclatureProvider;
import com.v1.inoprod.business.Production.Fil;
import com.v1.inoprod.business.ProductionProvider;

/** Activit� principale qui lance l'application, affiche et g�re le menu principal 
 * 
 * @author Arnaud Payet
 *
 */
public class MainActivity extends Activity {
	
	//ImageButton � r�cuperer depuis la vue
	private ImageButton boutonAnnuaire = null;
	private ImageButton boutonAide = null;
	private ImageButton boutonExit = null;
	private ImageButton boutonLogin = null;
	
	//URL des Contents Providers
	private Uri urlAnnuaire = AnnuaireProvider.CONTENT_URI;
	private Uri urlNomenclature = NomenclatureProvider.CONTENT_URI;
	private Uri urlProduction = ProductionProvider.CONTENT_URI;
	private Uri urlDurees = DureesProvider.CONTENT_URI;
	
	// Curseur et Content Resolver � utiliser lors des requ�tes
	private Cursor cursor;
	private ContentResolver cr;
	
	//Chaine de caract�res pour les req�etes tests
	private String columnsAnnuaire[] = new String[] { Employe._id };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Acc�s � l'annuaire
		boutonAnnuaire = (ImageButton) findViewById(R.id.imageButton2);	
		boutonAnnuaire.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent toAnnuaire = new Intent(MainActivity.this, Annuaire.class );				
				startActivity(toAnnuaire);
				
			}
		});
		
		//Acc�s � l'aide
		boutonAide = (ImageButton) findViewById(R.id.imageButton3);	
		boutonAide.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent toAide = new Intent(MainActivity.this, MenuAide.class );
				
				startActivity(toAide);
				
			}
		});
		
		//Acc�s au login
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
		
		//Initialisation du Content Resolver qui permet l'acc�s au Content Providers
		ContentResolver cr=getContentResolver(); 
		
		/*Cr�ation de la BD Annuaire
		La base de donn�e est cr�e une fois au premier lancement de l'application et n'est ensuite plus retouch�e.
		Afin d'�viter qu'une base de donn�e identique soit rajouter � la base existant, on effectue une requ�te
		qui permet de d�terminer si la base � d�ja �t� remplie ou non.
	*/
		cursor=cr.query(urlAnnuaire, columnsAnnuaire, null, null, null);
		if (!(cursor.moveToFirst())){
			try {
				//Ajout des �lements du fichier Excel � la base
				insertRecordsAnnuaire();
			} catch (IOException e) {
				Toast.makeText(this, "Fichier Annuaire Personel non lu", Toast.LENGTH_SHORT).show();
			}
		}
		}
		
		
		
	
	/** Lecture du fichier Excel annuaire_personel.xls et ajout des lignes correspondantes 
	 * � la bases de donn�es AnnuairePersonel
	 * 
	 * @throws IOException
	 */

	private void insertRecordsAnnuaire() throws IOException { 
		//Cr�ation d'un InputStream vers le fichier Excel
		InputStream input = this.getResources().openRawResource(R.raw.annuaire_personel);
		//Interpretation du fichier a l'aide de Apache POI
		POIFSFileSystem fs = new POIFSFileSystem( input );
		HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet =  wb.getSheetAt(0);

     // Iteration sur chacune des lignes du fichier
        Iterator rows = sheet.rowIterator(); 
      //On ne rentre pas les deux premi�res lignes qui ne comportent que les ent�tes des colonnes
        rows.next();
        rows.next();
     
        ContentValues contact = new ContentValues();
        
      //Parcours des lignes
        while( rows.hasNext() ) {   
        	
            HSSFRow row = (HSSFRow) rows.next();
          //Ajout des donn�es correspondantes
            contact.put(Employe.EMPLOYE_NOM,row.getCell(1).toString() );
            contact.put(Employe.EMPLOYE_PRENOM,row.getCell(2).toString() );
            contact.put(Employe.EMPLOYE_METIER,row.getCell(3).toString() );
            
          //Ajout de l'entit�
            getContentResolver().insert(urlAnnuaire, contact);
          //Ecrasement de ses donn�es pour passer � la suivante
            contact.clear();

		
	}
		
	}
	
	

}
