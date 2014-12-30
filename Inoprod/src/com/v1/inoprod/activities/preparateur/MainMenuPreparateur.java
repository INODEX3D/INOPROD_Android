package com.v1.inoprod.activities.preparateur;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.v1.inoprod.R;
import com.v1.inoprod.R.layout;
import com.v1.inoprod.activities.MainActivity;
import com.v1.inoprod.activities.MenuAide;
import com.v1.inoprod.business.AnnuaireProvider;
import com.v1.inoprod.business.DureesProvider;
import com.v1.inoprod.business.NomenclatureProvider;
import com.v1.inoprod.business.ProductionProvider;
import com.v1.inoprod.business.AnnuairePersonel.Employe;
import com.v1.inoprod.business.Durees.Duree;
import com.v1.inoprod.business.Nomenclature.Cable;
import com.v1.inoprod.business.Production.Fil;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainMenuPreparateur extends Activity {
	
	//Bouton de retour r�cup�r� depuis la vue
		private ImageButton boutonExit = null;

	//Bouton d'import des donn�es
		private Button boutonImport = null;
		
		//URL des Contents Providers
		private Uri urlNomenclature = NomenclatureProvider.CONTENT_URI;
		private	Uri urlProduction = ProductionProvider.CONTENT_URI;
		private	Uri urlDurees = DureesProvider.CONTENT_URI;
		
		//Colonnes cascad�s
		private String colProd1[] = new String [] { Fil.REPERE_ELECTRIQUE_TENANT , Fil.NUMERO_CONNECTEUR_TENANT, Fil.ORDRE_REALISATION, 
				Fil.ETAT_LIAISON_FIL, Fil.ETAT_LIAISON_FIL, Fil.NUMERO_REVISION_FIL, Fil.NUMERO_FIL_CABLE, Fil.TYPE_FIL_CABLE, Fil.LONGUEUR_FIL_CABLE };
		
		private String ColNum[] = new String [] { Cable.DESIGNATION_COMPOSANT , Cable.UNITE, Cable.REFERENCE_FABRICANT1, Cable.REFERENCE_FABRICANT2, 
				Cable.REFERENCE_INTERNE, Cable.FOURNISSEUR_FABRICANT };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu_preparateur);
		
		
		//Retour menu principal
		boutonExit = (ImageButton) findViewById(R.id.exitButton1);	
		boutonExit.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent toMain = new Intent(MainMenuPreparateur.this, MainActivity.class );						
				startActivity(toMain);
				
			}
		});
		
		//Import des donn�es et cr�ations des tables
		boutonImport = (Button) findViewById(R.id.button1);
		boutonImport.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			boolean importReussi =importSources();
			if (importReussi) {
				Toast.makeText(MainMenuPreparateur.this, "Import des fichiers sources r�ussis", Toast.LENGTH_SHORT).show();
				creationTables();
			}
				
			}


		});
			
	}
	
	protected void creationTables() {
		
		ContentValues contact = new ContentValues();
		
		//Cr�ation de la table de kitting
		
		
		
	}

	private boolean importSources() {
		
		try {
			insertRecordsDurees();
		} catch (IOException e) {
			Toast.makeText(this, "Fichier Durees non lu", Toast.LENGTH_SHORT).show();
			return false;
		}
		try {
			insertRecordsProduction();
		} catch (IOException e) {
			Toast.makeText(this, "Fichier Production non lu", Toast.LENGTH_SHORT).show();
			return false;
		}
		try {
			insertRecordsNomenclature();
		} catch (IOException e) {
			Toast.makeText(this, "Fichier Nomenclature non lu", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;

		
		
		
		
	}
	
	/** Lecture du fichier Excel bd_temps.xls et ajout des lignes correspondantes � la bases de donn�es Durees
	 * 
	 * @throws IOException
	 */
	
	
	private void insertRecordsDurees() throws IOException { 
		//Cr�ation d'un InputStream vers le fichier Excel
		InputStream input = this.getResources().openRawResource(R.raw.bd_temps);
		//Interpretation du fichier a l'aide de Apache POI
		POIFSFileSystem fs = new POIFSFileSystem( input );
		HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet =  wb.getSheetAt(0);

     // Iteration sur chacune des lignes du fichier
        Iterator rows = sheet.rowIterator(); 
      //On ne rentre pas les trois premi�res lignes qui ne comportent que les ent�tes des colonnes
        rows.next();
        rows.next();
        rows.next();
     
        ContentValues contact = new ContentValues();
        
      //Parcours des lignes
        while( rows.hasNext() ) {   
        	
            HSSFRow row = (HSSFRow) rows.next();
          //Ajout des donn�es correspondantes
            contact.put(Duree.CODE_OPERATION,Float.parseFloat(row.getCell(0).toString()) );
            contact.put(Duree.DESIGNATION_OPERATION,row.getCell(1).toString() );
            contact.put(Duree.DUREE_THEORIQUE,row.getCell(2).toString() );
            contact.put(Duree.UNITE,row.getCell(3).toString() );
            try {
                if (row.getCell(4).toString().equals("X")) {
                    contact.put(Duree.OPERATION_SOUS_CONTROLE, true );
                    } 
                } catch (Exception e){
                    	contact.put(Duree.OPERATION_SOUS_CONTROLE, false );
                 }
            
          //Ajout de l'entit�
            getContentResolver().insert(urlDurees, contact);
          //Ecrasement de ses donn�es pour passer � la suivante
            contact.clear();

		
	}
		
	}


	/** Lecture du fichier Excel bd_production.xls et ajout des lignes correspondantes � la bases de donn�es Production
	 * 
	 * @throws IOException
	 */
	private void insertRecordsProduction() throws IOException { 
		
		//Cr�ation d'un InputStream vers le fichier Excel
		InputStream input = this.getResources().openRawResource(R.raw.bd_production);
		//Interpretation du fichier a l'aide de Apache POI
		POIFSFileSystem fs = new POIFSFileSystem( input );
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet =  wb.getSheetAt(0);
        


        // Iteration sur chacune des lignes du fichier
        Iterator rows = sheet.rowIterator(); 
        //On ne rentre pas la premi�re ligne qui ne comporte que les ent�tes des colonnes
        rows.next();
        
        ContentValues contact = new ContentValues();
        //Parcours des lignes
        while( rows.hasNext() ) {           
            HSSFRow row = (HSSFRow) rows.next();
            //Ajout des donn�es correspondantes
            //Les nombreux try/catch permettent d'�viter des NullPointerException caus�es par les cellules vides
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
           
            //Ajout de l'entit�
            getContentResolver().insert(urlProduction, contact);
            //Ecrasement de ses donn�es pour passer � la suivante
            contact.clear();
            
        }
		
	}

	
	/** Lecture du fichier Excel bd_nomenclature.xls et ajout des lignes correspondantes 
	 * � la bases de donn�es Nomenclature
	 * 
	 * @throws IOException
	 */
	private void insertRecordsNomenclature() throws IOException { 
		//Cr�ation d'un InputStream vers le fichier Excel
		InputStream input = this.getResources().openRawResource(R.raw.bd_nomenclature);
		//Interpretation du fichier a l'aide de Apache POI
		POIFSFileSystem fs = new POIFSFileSystem( input );
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet =  wb.getSheetAt(0);        


     // Iteration sur chacune des lignes du fichier
        Iterator rows = sheet.rowIterator(); 
      //On ne rentre pas la premi�re ligne qui ne comporte que les ent�tes des colonnes
        rows.next();
        
        ContentValues contact = new ContentValues();
      //Parcours des lignes
        while( rows.hasNext() ) {           
            HSSFRow row = (HSSFRow) rows.next();
          //Ajout des donn�es correspondantes
            //Les nombreux try/catch permettent d'�viter des NullPointerException caus�es par les cellules vides
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

            
            //Ajout de l'entit�
            getContentResolver().insert(urlNomenclature, contact);
          //Ecrasement de ses donn�es pour passer � la suivante
            contact.clear();

		
	}
		
	}
}
