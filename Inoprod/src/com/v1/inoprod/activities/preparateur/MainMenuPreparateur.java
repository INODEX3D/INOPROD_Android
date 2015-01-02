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
import com.v1.inoprod.business.KittingProvider;
import com.v1.inoprod.business.NomenclatureProvider;
import com.v1.inoprod.business.ProductionProvider;
import com.v1.inoprod.business.AnnuairePersonel.Employe;
import com.v1.inoprod.business.Durees.Duree;
import com.v1.inoprod.business.Nomenclature.Cable;
import com.v1.inoprod.business.Production.Fil;
import com.v1.inoprod.business.RaccordementProvider;
import com.v1.inoprod.business.SequencementProvider;
import com.v1.inoprod.business.TableRaccordement.Raccordement;

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
	
	//Bouton de retour récupéré depuis la vue
		private ImageButton boutonExit = null;

	//Bouton d'import des données
		private Button boutonImport = null;
		private Button boutonDebit = null;
		private Button boutonKitting = null;
		
		// Curseur et Content Resolver à utiliser lors des requêtes
		private Cursor cursor;
		private ContentResolver cr;
		
		//URL des Contents Providers
		private Uri urlNomenclature = NomenclatureProvider.CONTENT_URI;
		private	Uri urlProduction = ProductionProvider.CONTENT_URI;
		private	Uri urlDurees = DureesProvider.CONTENT_URI;
		private	Uri urlRaccordement = RaccordementProvider.CONTENT_URI;
		private	Uri urlSequencement =SequencementProvider.CONTENT_URI;
		private	Uri urlKitting = KittingProvider.CONTENT_URI;
		
		//Colonnes cascadés
		private String colProd1[] = new String [] { Fil.REPERE_ELECTRIQUE_TENANT , Fil.NUMERO_COMPOSANT_TENANT, Fil.ORDRE_REALISATION, 
				Fil.ETAT_LIAISON_FIL, Fil.ETAT_LIAISON_FIL, Fil.NUMERO_REVISION_FIL, Fil.NUMERO_FIL_CABLE, Fil.TYPE_FIL_CABLE, Fil.LONGUEUR_FIL_CABLE };
		
		private String ColNum[] = new String [] { Cable.DESIGNATION_COMPOSANT , Cable.UNITE, Cable.REFERENCE_FABRICANT1, Cable.REFERENCE_FABRICANT2, 
				Cable.REFERENCE_INTERNE, Cable.FOURNISSEUR_FABRICANT };
		
		private String ColProd2[] = new String [] { Fil.ETAT_LIAISON_FIL , Fil.NUMERO_REVISION_FIL , Fil.FIL_SENSIBLE , Fil.NUMERO_FIL_CABLE
				 , Fil.TYPE_FIL_CABLE , Fil.NUMERO_FIL_DANS_CABLE, Fil.LONGUEUR_FIL_CABLE , Fil.COULEUR_FIL , Fil.NOM_SIGNAL , Fil.ORDRE_REALISATION
				 , Fil.REPERE_ELECTRIQUE_TENANT , Fil.NUMERO_COMPOSANT_TENANT , Fil.NUMERO_BORNE_TENANT, Fil.TYPE_RACCORDEMENT_TENANT , Fil.REPRISE_BLINDAGE
				 , Fil.SANS_REPRISE_BLINDAGE, Fil.REPERE_ELECTRIQUE_ABOUTISSANT , Fil.NUMERO_COMPOSANT_ABOUTISSANT , Fil.NUMERO_BORNE_ABOUTISSANT, 
				 Fil.TYPE_RACCORDEMENT_ABOUTISSANT, Fil.TYPE_ELEMENT_RACCORDE, Fil.REFERENCE_FABRICANT2, Fil.REFERENCE_INTERNE, Fil.FICHE_INSTRUCTION,
				 Fil.REFERENCE_CONFIGURATION_SERTISSAGE , Fil._id, Fil.REFERENCE_OUTIL_TENANT, Fil.REFERENCE_ACCESSOIRE_OUTIL_TENANT, Fil.REGLAGE_OUTIL_TENANT,
				 Fil.REFERENCE_OUTIL_ABOUTISSANT, Fil.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT, Fil.REGLAGE_OUTIL_ABOUTISSANT, Fil.OBTURATEUR, Fil.FAUX_CONTACT,
				 Fil.ETAT_FINALISATION_PRISE , Fil.ORIENTATION_RACCORD_ARRIERE };
				 
	
	
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
		
		//Import des données et créations des tables
		boutonImport = (Button) findViewById(R.id.button1);
		boutonImport.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			boolean importReussi =importSources();
			if (importReussi) {
				Toast.makeText(MainMenuPreparateur.this, "Import des fichiers sources réussis", Toast.LENGTH_SHORT).show();
				creationTables();
			}
				
			}


		});
			
	}
	
	protected void creationTables() {
		
		ContentValues contact = new ContentValues();
		cr= getContentResolver(); 
		
		//Création de la table de raccordement
		cursor = cr.query(urlProduction, ColProd2, null, null, null);
		if (cursor.moveToFirst()) {
				
		
		do {
			
		contact.put(Raccordement.COULEUR_FIL, cursor.getString(cursor.getColumnIndex(Fil.COULEUR_FIL)));
		contact.put(Raccordement.ETAT_FINALISATION_PRISE, cursor.getString(cursor.getColumnIndex(Fil.ETAT_FINALISATION_PRISE)));
		contact.put(Raccordement.ETAT_LIAISON_FIL, cursor.getString(cursor.getColumnIndex(Fil.ETAT_LIAISON_FIL)));
		contact.put(Raccordement.FAUX_CONTACT, cursor.getInt(cursor.getColumnIndex(Fil.FAUX_CONTACT)));
		contact.put(Raccordement.FICHE_INSTRUCTION, cursor.getString(cursor.getColumnIndex(Fil.FICHE_INSTRUCTION)));
		contact.put(Raccordement.FIL_SENSIBLE, cursor.getInt(cursor.getColumnIndex(Fil.FIL_SENSIBLE)));
		contact.put(Raccordement.LONGUEUR_FIL_CABLE, cursor.getFloat(cursor.getColumnIndex(Fil.LONGUEUR_FIL_CABLE)));
		contact.put(Raccordement.NOM_SIGNAL, cursor.getString(cursor.getColumnIndex(Fil.NOM_SIGNAL)));
		contact.put(Raccordement.NUMERO_BORNE_ABOUTISSANT, cursor.getString(cursor.getColumnIndex(Fil.NUMERO_BORNE_ABOUTISSANT)));
		contact.put(Raccordement.NUMERO_BORNE_TENANT, cursor.getString(cursor.getColumnIndex(Fil.NUMERO_BORNE_TENANT)));
		contact.put(Raccordement.NUMERO_COMPOSANT_ABOUTISSANT, cursor.getString(cursor.getColumnIndex(Fil.NUMERO_COMPOSANT_ABOUTISSANT)));
		contact.put(Raccordement.NUMERO_COMPOSANT_TENANT, cursor.getString(cursor.getColumnIndex(Fil.NUMERO_COMPOSANT_TENANT)));
		contact.put(Raccordement.NUMERO_FIL_CABLE, cursor.getString(cursor.getColumnIndex(Fil.NUMERO_FIL_CABLE)));
		contact.put(Raccordement.NUMERO_FIL_DANS_CABLE, cursor.getFloat(cursor.getColumnIndex(Fil.NUMERO_FIL_DANS_CABLE)));
		contact.put(Raccordement.NUMERO_REVISION_FIL, cursor.getFloat(cursor.getColumnIndex(Fil.NUMERO_REVISION_FIL)));
		contact.put(Raccordement.OBTURATEUR, cursor.getInt(cursor.getColumnIndex(Fil.OBTURATEUR)));
		contact.put(Raccordement.ORDRE_REALISATION, cursor.getString(cursor.getColumnIndex(Fil.ORDRE_REALISATION)));
		contact.put(Raccordement.ORIENTATION_RACCORD_ARRIERE, cursor.getString(cursor.getColumnIndex(Fil.ORIENTATION_RACCORD_ARRIERE)));
		contact.put(Raccordement.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT, cursor.getString(cursor.getColumnIndex(Fil.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT)));
		contact.put(Raccordement.REFERENCE_ACCESSOIRE_OUTIL_TENANT, cursor.getString(cursor.getColumnIndex(Fil.REFERENCE_ACCESSOIRE_OUTIL_TENANT)));
		contact.put(Raccordement.REFERENCE_CONFIGURATION_SERTISSAGE, cursor.getString(cursor.getColumnIndex(Fil.REFERENCE_CONFIGURATION_SERTISSAGE)));
		contact.put(Raccordement.REFERENCE_FABRICANT2, cursor.getString(cursor.getColumnIndex(Fil.REFERENCE_FABRICANT2)));
		contact.put(Raccordement.REFERENCE_INTERNE, cursor.getString(cursor.getColumnIndex(Fil.REFERENCE_INTERNE)));
		contact.put(Raccordement.REFERENCE_OUTIL_ABOUTISSANT, cursor.getString(cursor.getColumnIndex(Fil.REFERENCE_OUTIL_ABOUTISSANT)));
		contact.put(Raccordement.REFERENCE_OUTIL_TENANT, cursor.getString(cursor.getColumnIndex(Fil.REFERENCE_OUTIL_TENANT)));
		contact.put(Raccordement.REGLAGE_OUTIL_ABOUTISSANT, cursor.getString(cursor.getColumnIndex(Fil.REGLAGE_OUTIL_ABOUTISSANT)));
		contact.put(Raccordement.REGLAGE_OUTIL_TENANT, cursor.getString(cursor.getColumnIndex(Fil.REGLAGE_OUTIL_TENANT)));
		contact.put(Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT, cursor.getString(cursor.getColumnIndex(Fil.REPERE_ELECTRIQUE_ABOUTISSANT)));
		contact.put(Raccordement.REPERE_ELECTRIQUE_TENANT, cursor.getString(cursor.getColumnIndex(Fil.REPERE_ELECTRIQUE_TENANT)));
		contact.put(Raccordement.REPRISE_BLINDAGE, cursor.getString(cursor.getColumnIndex(Fil.REPRISE_BLINDAGE)));
		contact.put(Raccordement.SANS_REPRISE_BLINDAGE, cursor.getString(cursor.getColumnIndex(Fil.SANS_REPRISE_BLINDAGE)));
		contact.put(Raccordement.TYPE_ELEMENT_RACCORDE, cursor.getString(cursor.getColumnIndex(Fil.TYPE_ELEMENT_RACCORDE)));
		contact.put(Raccordement.TYPE_FIL_CABLE, cursor.getString(cursor.getColumnIndex(Fil.TYPE_FIL_CABLE)));
		contact.put(Raccordement.TYPE_RACCORDEMENT_ABOUTISSANT, cursor.getString(cursor.getColumnIndex(Fil.TYPE_RACCORDEMENT_ABOUTISSANT)));
		contact.put(Raccordement.TYPE_RACCORDEMENT_TENANT, cursor.getString(cursor.getColumnIndex(Fil.TYPE_RACCORDEMENT_TENANT)));
		
		//Ajout de l'entité
        getContentResolver().insert(urlRaccordement, contact);
        //Ecrasement de ses données pour passer à la suivante
        contact.clear();
        
		} while (cursor.moveToNext());
		Toast.makeText(this, "Table raccordement créée", Toast.LENGTH_SHORT).show();
		
		} 
		
		//Création de la table de kitting
		//TO DO
		
		
		
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
	
	/** Lecture du fichier Excel bd_temps.xls et ajout des lignes correspondantes à la bases de données Durees
	 * 
	 * @throws IOException
	 */
	
	
	private void insertRecordsDurees() throws IOException { 
		//Création d'un InputStream vers le fichier Excel
		InputStream input = this.getResources().openRawResource(R.raw.bd_temps);
		//Interpretation du fichier a l'aide de Apache POI
		POIFSFileSystem fs = new POIFSFileSystem( input );
		HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet =  wb.getSheetAt(0);

     // Iteration sur chacune des lignes du fichier
        Iterator rows = sheet.rowIterator(); 
      //On ne rentre pas les trois premières lignes qui ne comportent que les entêtes des colonnes
        rows.next();
        rows.next();
        rows.next();
     
        ContentValues contact = new ContentValues();
        
      //Parcours des lignes
        while( rows.hasNext() ) {   
        	
            HSSFRow row = (HSSFRow) rows.next();
          //Ajout des données correspondantes
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
            
          //Ajout de l'entité
            getContentResolver().insert(urlDurees, contact);
          //Ecrasement de ses données pour passer à la suivante
            contact.clear();

		
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
            contact.put(Fil.NUMERO_COMPOSANT_TENANT,row.getCell(21).toString() );
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
            contact.put(Fil.NUMERO_COMPOSANT_ABOUTISSANT,row.getCell(27).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Fil.NUMERO_BORNE_ABOUTISSANT,row.getCell(28).toString() );
            } catch (Exception e) {}
            try {
            contact.put(Fil.TYPE_RACCORDEMENT_ABOUTISSANT,row.getCell(29).toString() );
            } catch (Exception e) {}
            
            contact.put(Fil.TYPE_ELEMENT_RACCORDE,row.getCell(30).toString() );
      
        try {
            contact.put(Fil.REFERENCE_FABRICANT2,row.getCell(31).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REFERENCE_INTERNE,row.getCell(32).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.FICHE_INSTRUCTION,row.getCell(33).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REFERENCE_CONFIGURATION_SERTISSAGE,row.getCell(34).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.ACCESSOIRE_COMPOSANT1,row.getCell(35).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.ACCESSOIRE_COMPOSANT2,row.getCell(36).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REFERENCE_OUTIL_TENANT,row.getCell(37).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REFERENCE_ACCESSOIRE_OUTIL_TENANT,row.getCell(38).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REGLAGE_OUTIL_TENANT,row.getCell(39).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REFERENCE_OUTIL_ABOUTISSANT,row.getCell(40).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT,row.getCell(41).toString() );
        } catch (Exception e) {}
        try {
            contact.put(Fil.REGLAGE_OUTIL_ABOUTISSANT,row.getCell(42).toString() );
        } catch (Exception e) {}
        try {
            if (row.getCell(43).toString().equals("X")) {
                contact.put(Fil.OBTURATEUR, true );
                } 
        } catch (Exception e) {
                	contact.put(Fil.OBTURATEUR, false );
        }
              try {
            if (row.getCell(44).toString().equals("X")) {
                contact.put(Fil.FAUX_CONTACT, true );
                } } catch (Exception e) {
                	contact.put(Fil.FAUX_CONTACT, false );
               }
              try {
            contact.put(Fil.ETAT_FINALISATION_PRISE,row.getCell(45).toString() );
              } catch (Exception e) {}
              try {
            contact.put(Fil.ORIENTATION_RACCORD_ARRIERE,row.getCell(46).toString() );
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
}
