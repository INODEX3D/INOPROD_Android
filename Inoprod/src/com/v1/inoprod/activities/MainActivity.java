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
import com.v1.inoprod.business.Nomenclature.Cable;
import com.v1.inoprod.business.NomenclatureProvider;
import com.v1.inoprod.business.Production.Fil;
import com.v1.inoprod.business.ProductionProvider;

/** Activité principale qui lance l'application, affiche et gère le menu principal 
 * 
 * @author Arnaud Payet
 *
 */
public class MainActivity extends Activity {
	
	//ImageButton à récuperer depuis la vue
	private ImageButton boutonAnnuaire = null;
	private ImageButton boutonAide = null;
	private ImageButton boutonExit = null;
	private ImageButton boutonLogin = null;
	
	//URL des Contents Providers
	private Uri urlAnnuaire = AnnuaireProvider.CONTENT_URI;
	private Uri urlNomenclature = NomenclatureProvider.CONTENT_URI;
	private Uri urlProduction = ProductionProvider.CONTENT_URI;
	
	// Curseur et Content Resolver à utiliser lors des requêtes
	private Cursor cursor;
	private ContentResolver cr;
	
	//Chaine de caractéres pour les reqûetes tests
	private String columnsAnnuaire[] = new String[] { Employe._id };
	private String columnsProduction[] = new String[] { Fil._id };
	private String columnsNomenclature[] = new String[] { Cable._id };

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
		
		//Initialisation du Content Resolver qui permet l'accés au Content Providers
		ContentResolver cr=getContentResolver(); 
		
		/*Création de la BD Annuaire
		La base de donnée est crée une fois au premier lancement de l'application et n'est ensuite plus retouchée.
		Afin d'éviter qu'une base de donnée identique soit rajouter à la base existant, on effectue une requête
		qui permet de déterminer si la base à dèja été remplie ou non.
	*/
		cursor=cr.query(urlAnnuaire, columnsAnnuaire, null, null, null);
		if (!(cursor.moveToFirst())){
			try {
				//Ajout des élements du fichier Excel à la base
				insertRecordsAnnuaire();
			} catch (IOException e) {
				Toast.makeText(this, "Fichier Annuaire Personel non lu", Toast.LENGTH_SHORT).show();
			}
		}
		
		
		/*Création de la BD Nomenclature
		La base de donnée est crée une fois au premier lancement de l'application et n'est ensuite plus retouchée.
		Afin d'éviter qu'une base de donnée identique soit rajouter à la base existant, on effectue une requête
		qui permet de déterminer si la base à dèja été remplie ou non.
	*/
		cursor=cr.query(urlNomenclature, columnsNomenclature, null, null, null);
		if (!(cursor.moveToFirst())){
			try {
				//Ajout des élements du fichier Excel à la base
				insertRecordsNomenclature();
			} catch (IOException e) {
				Toast.makeText(this, "Fichier Nomenclature non lu", Toast.LENGTH_SHORT).show();
			}
		}
		
		
		/*Création de la BD Nomenclature
		La base de donnée est crée une fois au premier lancement de l'application et n'est ensuite plus retouchée.
		Afin d'éviter qu'une base de donnée identique soit rajouter à la base existant, on effectue une requête
		qui permet de déterminer si la base à dèja été remplie ou non.
	*/
				cursor=cr.query(urlProduction, columnsProduction, null, null, null);
				if (!(cursor.moveToFirst())){
					try {
						//Ajout des élements du fichier Excel à la base
						insertRecordsProduction();
					} catch (IOException e) {
						Toast.makeText(this, "Fichier Production non lu", Toast.LENGTH_SHORT).show();
					}
				}
				
		
		
		

	}
	
	
	/** Lecture du fichier Excel bd_production.xls et ajout des lignes correspondantes à la bases de données Production
	 * 
	 * @throws IOException
	 */
	private void insertRecordsProduction() throws IOException { 
		
		//Création d'un InputStream vers le fichier Excel
		InputStream input = this.getResources().openRawResource(R.raw.bd_production);
		//Interpretation du fichier a l'aide de Apache POI
		POIFSFileSystem fs = new POIFSFileSystem( input );
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet =  wb.getSheetAt(0);
        


        // Iteration sur chacune des lignes du fichier
        Iterator rows = sheet.rowIterator(); 
        //On ne rentre pas la première ligne qui ne comporte que les entêtes des colonnes
        rows.next();
        
        ContentValues contact = new ContentValues();
        //Parcours des lignes
        while( rows.hasNext() ) {           
            HSSFRow row = (HSSFRow) rows.next();
            //Ajout des données correspondantes
            //Les nombreux try/catch permettent d'éviter des NullPointerException causées par les cellules vides
            contact.put(Fil.DESIGNATION_PRODUIT,row.getCell(0).toString() );
            contact.put(Fil.REFERENCE_FICHIER_SOURCE,row.getCell(1).toString() );
            contact.put(Fil.NUMERO_REVISION_HARNAIS,Float.parseFloat(row.getCell(2).toString()) );
            contact.put(Fil.NUMERO_HARNAIS_FAISCEAUX,Float.parseFloat(row.getCell(3).toString()) );
            contact.put(Fil.REFERENCE_FABRICANT1,row.getCell(4).toString() );
            contact.put(Fil.STANDARD,Float.parseFloat(row.getCell(5).toString()) );
            contact.put(Fil.ZONE_ACTIVITE,row.getCell(6).toString() );
            contact.put(Fil.LOCALISATION1,row.getCell(7).toString() );
            contact.put(Fil.LOCALISATION2,Float.parseFloat((row.getCell(8).toString())) );
            try {
            contact.put(Fil.NUMERO_ROUTE,row.getCell(9).toString() );
        } catch (Exception e) {}
            contact.put(Fil.ETAT_LIAISON_FIL,row.getCell(10).toString() );
            contact.put(Fil.NUMERO_REVISION_FIL,Float.parseFloat(row.getCell(11).toString()) );
            try {
            if (row.getCell(12).toString().equals("X")) {
                contact.put(Fil.FIL_SENSIBLE, true );
                } 
            } catch (Exception e){
                	contact.put(Fil.FIL_SENSIBLE, false );
               }
            try {
            contact.put(Fil.NUMERO_FIL_CABLE,row.getCell(13).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Fil.TYPE_FIL_CABLE,row.getCell(14).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Fil.LONGUEUR_FIL_CABLE,Float.parseFloat(row.getCell(15).toString()) );
            } catch (Exception e) {}
            try {
            contact.put(Fil.NUMERO_FIL_DANS_CABLE,row.getCell(16).toString() );
        } catch (Exception e) {}
            try {
            contact.put(Fil.COULEUR_FIL,row.getCell(17).toString() );
        } catch (Exception e) {}
            try {
            contact.put(Fil.ORDRE_REALISATION,row.getCell(19).toString() );
        } catch (Exception e) {}
            try {
            contact.put(Fil.REPERE_ELECTRIQUE_TENANT,row.getCell(20).toString() );
        } catch (Exception e) {}
            try {
            contact.put(Fil.NUMERO_CONNECTEUR_TENANT,row.getCell(21).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Fil.NUMERO_BORNE_TENANT,Float.parseFloat(row.getCell(22).toString()) );
            } catch (Exception e) {}
            try {
            contact.put(Fil.TYPE_RACCORDEMENT_TENANT,row.getCell(23).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Fil.REPRISE_BLINDAGE,row.getCell(24).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Fil.SANS_REPRISE_BLINDAGE,row.getCell(25).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Fil.REPERE_ELECTRIQUE_ABOUTISSANT,row.getCell(26).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Fil.NUMERO_CONNECTEUR_ABOUTISSANT,row.getCell(27).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Fil.NUMERO_BORNE_ABOUTISSANT,row.getCell(28).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Fil.TYPE_RACCORDEMENT_ABOUTISSANT,row.getCell(29).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Fil.BORNE_ACCESSOIRE_CABLAGE1,Float.parseFloat(row.getCell(30).toString()) );
            } catch (Exception e) {}
            try {
            contact.put(Fil.ACCESSOIRE_CABLAGE1,row.getCell(31).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Fil.BORNE2_ACCESSOIRE_CABLAGE1,Float.parseFloat(row.getCell(32).toString()) );
            } catch (Exception e) {}
            contact.put(Fil.TYPE_ELEMENT_RACCORDE,row.getCell(33).toString() );
        try {
            contact.put(Fil.BORNE_ACCESSOIRE_CABLAGE2,Float.parseFloat(row.getCell(34).toString()) );
        } catch (Exception e) {}
        try {
            contact.put(Fil.ACCESSOIRE_CABLAGE2,row.getCell(35).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.BORNE2_ACCESSOIRE_CABLAGE2,Float.parseFloat(row.getCell(36).toString()) );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REFERENCE_FABRICANT2,row.getCell(37).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REFERENCE_INTERNE,row.getCell(38).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.FICHE_INSTRUCTION,row.getCell(39).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REFERENCE_CONFIGURATION_SERTISSAGE,row.getCell(40).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REFERENCE_OUTIL_TENANT,row.getCell(41).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REFERENCE_ACCESSOIRE_OUTIL_TENANT,row.getCell(42).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REGLAGE_OUTIL_TENANT,row.getCell(43).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REFERENCE_OUTIL_ABOUTISSANT,row.getCell(44).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT,row.getCell(45).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REGLAGE_OUTIL_ABOUTISSANT,row.getCell(46).toString() );
        } catch (Exception e) {}
        try {
            if (row.getCell(47).toString().equals("X")) {
                contact.put(Fil.OBTURATEUR, true );
                } 
        } catch (Exception e) {
                	contact.put(Fil.OBTURATEUR, false );
        }
              try {
            if (row.getCell(48).toString().equals("X")) {
                contact.put(Fil.FAUX_CONTACT, true );
                } } catch (Exception e) {
                	contact.put(Fil.FAUX_CONTACT, false );
               }
              try {
            contact.put(Fil.ETAT_FINALISATION_PRISE,row.getCell(49).toString() );
              } catch (Exception e) {}
              try {
            contact.put(Fil.ORIENTATION_RACCORD_ARRIERE,row.getCell(50).toString() );
              } catch (Exception e) {}
           
            //Ajout de l'entité
            getContentResolver().insert(urlProduction, contact);
            //Ecrasement de ses données pour passer à la suivante
            contact.clear();
            
        }
		
	}

	
	/** Lecture du fichier Excel bd_nomenclature.xls et ajout des lignes correspondantes 
	 * à la bases de données Nomenclature
	 * 
	 * @throws IOException
	 */
	private void insertRecordsNomenclature() throws IOException { 
		//Création d'un InputStream vers le fichier Excel
		InputStream input = this.getResources().openRawResource(R.raw.bd_nomenclature);
		//Interpretation du fichier a l'aide de Apache POI
		POIFSFileSystem fs = new POIFSFileSystem( input );
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet =  wb.getSheetAt(0);        


     // Iteration sur chacune des lignes du fichier
        Iterator rows = sheet.rowIterator(); 
      //On ne rentre pas la première ligne qui ne comporte que les entêtes des colonnes
        rows.next();
        
        ContentValues contact = new ContentValues();
      //Parcours des lignes
        while( rows.hasNext() ) {           
            HSSFRow row = (HSSFRow) rows.next();
          //Ajout des données correspondantes
            //Les nombreux try/catch permettent d'éviter des NullPointerException causées par les cellules vides
            contact.put(Cable.DESIGNATION_PRODUIT,row.getCell(0).toString() );
            contact.put(Cable.REFERENCE_FICHIER_SOURCE,row.getCell(1).toString() );
            contact.put(Cable.NUMERO_REVISION_HARNAIS,Float.parseFloat(row.getCell(2).toString() ));
            contact.put(Cable.NUMERO_HARNAIS_FAISCEAUX,Float.parseFloat(row.getCell(3).toString() ));
            contact.put(Cable.REFERENCE_FABRICANT1,row.getCell(4).toString() );
            contact.put(Cable.STANDARD,Float.parseFloat(row.getCell(5).toString() ));
            try {
            contact.put(Cable.EQUIPEMENT,row.getCell(6).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Cable.REPERE_ELECTRIQUE,row.getCell(7).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Cable.NUMERO_CONNECTEUR,row.getCell(8).toString() );
            } catch (Exception e) {}
            contact.put(Cable.ORDRE_REALISATION,row.getCell(9).toString() );
            try {
            contact.put(Cable.ACCESSOIRE_CABLAGE,row.getCell(10).toString() );
            } catch (Exception e) {}
            contact.put(Cable.DESIGNATION_COMPOSANT,row.getCell(11).toString() );
            contact.put(Cable.FAMILLE_PRODUIT,row.getCell(12).toString() );
            contact.put(Cable.REFERENCE_FABRICANT2,row.getCell(13).toString() );
            contact.put(Cable.FOURNISSEUR_FABRICANT,row.getCell(14).toString() );
            contact.put(Cable.REFERENCE_INTERNE,row.getCell(15).toString() );
            try {
            if (row.getCell(16).toString().equals("X") ) {
            contact.put(Cable.REFERENCE_IMPOSEE, true );
            } 
      
            
            } catch (Exception e) {
            	contact.put(Cable.REFERENCE_IMPOSEE, false );
            	}
            contact.put(Cable.QUANTITE,Float.parseFloat(row.getCell(17).toString()) );
            contact.put(Cable.UNITE,row.getCell(18).toString() );

            
            //Ajout de l'entité
            getContentResolver().insert(urlNomenclature, contact);
          //Ecrasement de ses données pour passer à la suivante
            contact.clear();

		
	}
		
	}
	
	/** Lecture du fichier Excel annuaire_personel.xls et ajout des lignes correspondantes 
	 * à la bases de données AnnuairePersonel
	 * 
	 * @throws IOException
	 */

	private void insertRecordsAnnuaire() throws IOException { 
		//Création d'un InputStream vers le fichier Excel
		InputStream input = this.getResources().openRawResource(R.raw.annuaire_personel);
		//Interpretation du fichier a l'aide de Apache POI
		POIFSFileSystem fs = new POIFSFileSystem( input );
		HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet =  wb.getSheetAt(0);

     // Iteration sur chacune des lignes du fichier
        Iterator rows = sheet.rowIterator(); 
      //On ne rentre pas les deux premières lignes qui ne comportent que les entêtes des colonnes
        rows.next();
        rows.next();
     
        ContentValues contact = new ContentValues();
        
      //Parcours des lignes
        while( rows.hasNext() ) {   
        	
            HSSFRow row = (HSSFRow) rows.next();
          //Ajout des données correspondantes
            contact.put(Employe.EMPLOYE_NOM,row.getCell(1).toString() );
            contact.put(Employe.EMPLOYE_PRENOM,row.getCell(2).toString() );
            contact.put(Employe.EMPLOYE_METIER,row.getCell(3).toString() );
            
          //Ajout de l'entité
            getContentResolver().insert(urlAnnuaire, contact);
          //Ecrasement de ses données pour passer à la suivante
            contact.clear();

		
	}
		
	}
	
	

}
