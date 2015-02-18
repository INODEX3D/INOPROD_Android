package com.inodex.inoprod.activities;

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

import com.inodex.inoprod.R;
import com.inodex.inoprod.business.AnnuairePersonel.Employe;
import com.inodex.inoprod.business.AnnuaireProvider;

/**
 * Activité principale qui lance l'application, affiche et gère le menu
 * principal Cette activité se charge également de la création de l'annuaire
 * lorsqu'elle est lancée pour la toute première fois.
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class Inoprod extends Activity {

	/** ImageButtons à récuperer depuis la vue */
	private ImageButton boutonAnnuaire = null;
	private ImageButton boutonAide = null;
	private ImageButton boutonExit = null;
	private ImageButton boutonLogin = null;

	/** URL de l'annuaire */
	private Uri urlAnnuaire = AnnuaireProvider.CONTENT_URI;

	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor;
	private ContentResolver cr;

	/** Chaine de caractéres pour les reqûetes tests */
	private String columnsAnnuaire[] = new String[] { Employe._id };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Accés à l'annuaire
		boutonAnnuaire = (ImageButton) findViewById(R.id.imageButton2);
		boutonAnnuaire.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent toAnnuaire = new Intent(Inoprod.this,
						Annuaire.class);
				startActivity(toAnnuaire);
				finish();

			}
		});

		// Accés à l'aide
		boutonAide = (ImageButton) findViewById(R.id.imageButton3);
		boutonAide.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent toAide = new Intent(Inoprod.this, MenuAide.class);

				startActivity(toAide);
				finish();

			}
		});

		// Accés au login
		boutonLogin = (ImageButton) findViewById(R.id.imageButton1);
		boutonLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent toLogin = new Intent(Inoprod.this,
						LoginProfil.class);

				startActivity(toLogin);
				finish();

			}
		});

		// Quitter l'application
		boutonExit = (ImageButton) findViewById(R.id.imageButton4);
		boutonExit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// Initialisation du Content Resolver qui permet l'accés au Content
		// Providers
		ContentResolver cr = getContentResolver();

		/*
		 * Création de la BD Annuaire La base de donnée est crée une fois au
		 * premier lancement de l'application et n'est ensuite plus retouchée.
		 * Afin d'éviter qu'une base de donnée identique soit rajouter à la base
		 * existant, on effectue une requête qui permet de déterminer si la base
		 * à dèja été remplie ou non.
		 */
		cursor = cr.query(urlAnnuaire, columnsAnnuaire, null, null, null);
		if (!(cursor.moveToFirst())) {
			try {
				// Ajout des élements du fichier Excel à la base
				insertRecordsAnnuaire();
			} catch (IOException e) {
				Toast.makeText(this, "Fichier Annuaire Personel non lu",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Lecture du fichier Excel annuaire_personel.xls et ajout des lignes
	 * correspondantes à la bases de données AnnuairePersonel
	 * 
	 * @throws IOException
	 */

	private void insertRecordsAnnuaire() throws IOException {
		// Création d'un InputStream vers le fichier Excel
		InputStream input = this.getResources().openRawResource(
				R.raw.annuaire_personel);
		// Interpretation du fichier a l'aide de Apache POI
		POIFSFileSystem fs = new POIFSFileSystem(input);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);

		// Iteration sur chacune des lignes du fichier
		Iterator rows = sheet.rowIterator();
		// On ne rentre pas les deux premières lignes qui ne comportent que les
		// entêtes des colonnes
		rows.next();
		rows.next();

		ContentValues contact = new ContentValues();

		// Parcours des lignes
		while (rows.hasNext()) {

			HSSFRow row = (HSSFRow) rows.next();
			// Ajout des données correspondantes
			contact.put(Employe.EMPLOYE_NOM, row.getCell(1).toString());
			contact.put(Employe.EMPLOYE_PRENOM, row.getCell(2).toString());
			contact.put(Employe.EMPLOYE_METIER, row.getCell(3).toString());

			// Ajout de l'entité
			getContentResolver().insert(urlAnnuaire, contact);
			// Ecrasement de ses données pour passer à la suivante
			contact.clear();

		}

	}

}
