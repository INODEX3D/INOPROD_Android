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

/** Acitivité qui affiche l'annuaire du personnel autorisé 
 * 
 * @author Arnaud Payet
 *
 */
public class Annuaire extends Activity {
	
	//Bouton qui permet de revenir au menu principal
	private ImageButton boutonExit = null;
	
	//Chaines de caractères permettant de faire une requete dans la base de données Annuaire
	private String columns[] = new String[] { Employe._id, Employe.EMPLOYE_NOM, Employe.EMPLOYE_PRENOM, Employe.EMPLOYE_METIER };
	private int[] layouts = new int[] {R.id.idEmp, R.id.nom, R.id.prenom, R.id.metier };
	//Uri de AnnuaireProvider
	private Uri url = AnnuaireProvider.CONTENT_URI;
	// Curseur et Content Resolver à utiliser lors des requêtes
	private Cursor cursor;
	private ContentResolver cr; 	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_annuaire);
		cr= getContentResolver(); 
		//Affichage de l'annuaire
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

	/** Genère l'affichage de l'annuaire en utilisant un SimpleCursorAdapter
	 * Le layout GridView est récupéré puis utiliser pour afficher chacun des éléments
	 */
	private void displayContentProvider() {
		//Création du SimpleCursorAdapter affilié au GridView 
		SimpleCursorAdapter sca = new SimpleCursorAdapter( this, R.layout.grid_layout_annuaire, null, columns, layouts);
		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(sca);
		//Requête dans la base Annuaire
		cursor=cr.query(url, columns, null, null, null);
		sca.changeCursor(cursor);
		
		
		
	}
	

		


}
