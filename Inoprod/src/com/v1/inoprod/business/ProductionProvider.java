package com.v1.inoprod.business;

import com.v1.inoprod.business.AnnuairePersonel.Employe;
import com.v1.inoprod.business.Nomenclature.Cable;
import com.v1.inoprod.business.Production.Fil;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class ProductionProvider extends ContentProvider {
	
	DatabaseProduction dbHelper;
	public static final Uri CONTENT_URI = Uri.parse("content://com.v1.inoprod.business.production");

	// Nom de notre base de données 
		public static final String CONTENT_PROVIDER_DB_NAME = "production.db";
	// Version de notre base de données 
		public static final int CONTENT_PROVIDER_DB_VERSION = 1;
	// Nom de la table de notre base 
		public static final String CONTENT_PROVIDER_TABLE_NAME = "fil";
	// Le Mime de notre content provider, la premiére partie est toujours identique 
		public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.com.v1.inoprod.business.fil";

	

		private static class DatabaseProduction extends SQLiteOpenHelper {

			// Création à partir du Context, du Nom de la table et du numéro de version 
			DatabaseProduction(Context context) {
		super(context,ProductionProvider.CONTENT_PROVIDER_DB_NAME, null, ProductionProvider.CONTENT_PROVIDER_DB_VERSION);
			}
			
			// Création des tables 
			@Override
			public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + ProductionProvider.CONTENT_PROVIDER_TABLE_NAME + " (" 
			+ Fil._id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ Fil.ACCESSOIRE_COMPOSANT1 + " STRING,"
			+ Fil.ACCESSOIRE_COMPOSANT2 + " STRING,"
			+ Fil.COULEUR_FIL + " STRING,"
			+ Fil.DESIGNATION_PRODUIT + " STRING,"
			+ Fil.ETAT_FINALISATION_PRISE + " STRING,"
			+ Fil.ETAT_LIAISON_FIL + " STRING,"
			+ Fil.FAUX_CONTACT + " BOOLEAN," 
			+ Fil.FICHE_INSTRUCTION + " STRING,"
			+ Fil.FIL_SENSIBLE + " BOOLEAN," 
			+ Fil.LOCALISATION1 + " STRING,"
			+ Fil.LOCALISATION2 + " FLOAT,"
			+ Fil.LONGUEUR_FIL_CABLE + " FLOAT,"
			+ Fil.NOM_SIGNAL + " STRING,"
			+ Fil.NUMERO_BORNE_ABOUTISSANT + " FLOAT,"
			+ Fil.NUMERO_BORNE_TENANT + " FLOAT,"
			+ Fil.NUMERO_COMPOSANT_ABOUTISSANT + " STRING,"
			+ Fil.NUMERO_COMPOSANT_TENANT + " STRING,"
		    + Fil.NUMERO_FIL_CABLE + " STRING,"
		    + Fil.NORME_CABLE + " STRING,"
			+ Fil.NUMERO_FIL_DANS_CABLE + " STRING,"
			+ Fil.NUMERO_HARNAIS_FAISCEAUX + " FLOAT," 
			+ Fil.NUMERO_REVISION_HARNAIS + " FLOAT,"
			+ Fil.NUMERO_REVISION_FIL + " FLOAT,"
			+ Fil.NUMERO_ROUTE +  " STRING,"
			+ Fil.OBTURATEUR + " BOOLEAN," 
			+ Fil.ORDRE_REALISATION +  " STRING,"
			+ Fil.ORIENTATION_RACCORD_ARRIERE +  " STRING,"
			+ Fil.REFERENCE_CONFIGURATION_SERTISSAGE +  " STRING,"
			+ Fil.REFERENCE_FABRICANT1 +  " STRING,"
			+ Fil.REFERENCE_FABRICANT2 +  " STRING,"
			+ Fil.REFERENCE_FICHIER_SOURCE +  " STRING,"
			+ Fil.REFERENCE_INTERNE +  " STRING,"
			+ Fil.REFERENCE_OUTIL_ABOUTISSANT + " STRING,"
			+ Fil.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT + " STRING,"
			+ Fil.REFERENCE_OUTIL_TENANT +  " STRING,"
			+ Fil.REFERENCE_ACCESSOIRE_OUTIL_TENANT +  " STRING,"
			+ Fil.REGLAGE_OUTIL_TENANT + " STRING,"
			+ Fil.REPERE_ELECTRIQUE_ABOUTISSANT +  " STRING,"
			+ Fil.REPERE_ELECTRIQUE_TENANT +  " STRING,"
			+ Fil.REGLAGE_OUTIL_ABOUTISSANT + " STRING,"
			+ Fil.REPRISE_BLINDAGE +  " STRING,"
			+ Fil.SANS_REPRISE_BLINDAGE +  " STRING,"
			+ Fil.STANDARD + " FLOAT,"
			+ Fil.TYPE_ELEMENT_RACCORDE +  " STRING,"
			+ Fil.TYPE_FIL_CABLE +  " STRING,"
			+ Fil.TYPE_RACCORDEMENT_ABOUTISSANT +  " STRING,"
			+ Fil.TYPE_RACCORDEMENT_TENANT +  " STRING,"
			+ Fil.ZONE_ACTIVITE +  " STRING"	
			+ ");");
			
			}

			// Cette méthode sert à gérer la montée de version de notre base 
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + ProductionProvider.CONTENT_PROVIDER_TABLE_NAME);
				onCreate(db);
			
			}
		

		
		}
		
		
	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseProduction(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
			long id = getId(uri);
			SQLiteDatabase db = dbHelper.getReadableDatabase();	
			if (id < 0) {
				return 	db.query(ProductionProvider.CONTENT_PROVIDER_TABLE_NAME,
		projection, selection, selectionArgs, null, null,
			sortOrder);
			} else {
				return 		db.query(ProductionProvider.CONTENT_PROVIDER_TABLE_NAME,
			projection, Fil._id + "=" + id, null, null, null,
			null);
			} 
	}



	@Override
	public String getType(Uri uri) {
		return ProductionProvider.CONTENT_PROVIDER_MIME;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			
	long id = db.insertOrThrow(ProductionProvider.CONTENT_PROVIDER_TABLE_NAME, null, values);
				
	if (id == -1) {
				throw new RuntimeException(String.format(
				"%s : Failed to insert [%s] for unknown reasons.","ProductionProvider", values, uri));
			} else {
				return ContentUris.withAppendedId(uri, id);
			}
			
	} finally {
				db.close();
			}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			if (id < 0)
				return db.delete(
						ProductionProvider.CONTENT_PROVIDER_TABLE_NAME,
						selection, selectionArgs);
			else
				return db.delete(
						ProductionProvider.CONTENT_PROVIDER_TABLE_NAME,
						Fil._id + "=" + id, selectionArgs);
		} finally {
			db.close();
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
	try {
			if (id < 0)
	return db.update( ProductionProvider.CONTENT_PROVIDER_TABLE_NAME,values, selection, selectionArgs);
		else
				return db.update(ProductionProvider.CONTENT_PROVIDER_TABLE_NAME,
				values, Fil._id + "=" + id, null); 
			} finally {
				db.close();
			}
	}
	
	private long getId(Uri uri) {
		String lastPathSegment = uri.getLastPathSegment();
		if (lastPathSegment != null) {
			try {
				return Long.parseLong(lastPathSegment);
			} catch (NumberFormatException e) {
				Log.e("ProductionProvider", "Number Format Exception : " + e);
			}
		}
		return -1;
		
}

}
