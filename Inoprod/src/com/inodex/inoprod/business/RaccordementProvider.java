package com.inodex.inoprod.business;



import com.inodex.inoprod.business.TableRaccordement.Raccordement;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class RaccordementProvider extends ContentProvider {
	
	DatabaseRaccordement dbHelper;
	public static final Uri CONTENT_URI = Uri.parse("content://com.inodex.inoprod.business.raccordement");

	// Nom de notre base de données 
		public static final String CONTENT_PROVIDER_DB_NAME = "raccordement.db";
	// Version de notre base de données 
		public static final int CONTENT_PROVIDER_DB_VERSION = 1;
	// Nom de la table de notre base 
		public static final String CONTENT_PROVIDER_TABLE_NAME = "raccordement";
	// Le Mime de notre content provider, la premiére partie est toujours identique 
		public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.com.inodex.inoprod.business.raccordement";

	

		private static class DatabaseRaccordement extends SQLiteOpenHelper {

			// Création à partir du Context, du Nom de la table et du numéro de version 
			DatabaseRaccordement(Context context) {
		super(context,RaccordementProvider.CONTENT_PROVIDER_DB_NAME, null, RaccordementProvider.CONTENT_PROVIDER_DB_VERSION);
			}
			
			// Création des tables 
			@Override
			public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + RaccordementProvider.CONTENT_PROVIDER_TABLE_NAME + " (" 
			+ Raccordement._id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ Raccordement.ACCESSOIRE_COMPOSANT1 + " STRING,"
			+ Raccordement.ACCESSOIRE_COMPOSANT2 + " STRING,"
			+ Raccordement.COULEUR_FIL + " STRING,"
			+ Raccordement.ETAT_FINALISATION_PRISE + " STRING,"
			+ Raccordement.ETAT_LIAISON_FIL + " STRING,"
			+ Raccordement.FAUX_CONTACT + " BOOLEAN," 
			+ Raccordement.FICHE_INSTRUCTION + " STRING,"
			+ Raccordement.FIL_SENSIBLE + " BOOLEAN," 
			+ Raccordement.LOCALISATION1 + " STRING,"
			+ Raccordement.LONGUEUR_FIL_CABLE + " FLOAT,"
			+ Raccordement.NOM_SIGNAL + " STRING,"
			+ Raccordement.NUMERO_BORNE_ABOUTISSANT + " FLOAT,"
			+ Raccordement.NUMERO_BORNE_TENANT + " FLOAT,"
			+ Raccordement.NUMERO_COMPOSANT_ABOUTISSANT + " STRING,"
			+ Raccordement.NORME_CABLE + " STRING,"
			+ Raccordement.NUMERO_COMPOSANT_TENANT + " STRING,"
		    + Raccordement.NUMERO_FIL_CABLE + " STRING,"
			+ Raccordement.NUMERO_FIL_DANS_CABLE + " STRING,"
			+ Raccordement.NUMERO_REVISION_FIL + " FLOAT,"
			+ Raccordement.OBTURATEUR + " BOOLEAN," 
			+ Raccordement.ORDRE_REALISATION +  " STRING,"
			+ Raccordement.ORIENTATION_RACCORD_ARRIERE +  " STRING,"
			+ Raccordement.REFERENCE_CONFIGURATION_SERTISSAGE +  " STRING,"
			+ Raccordement.REFERENCE_FABRICANT2 +  " STRING,"
			+ Raccordement.REFERENCE_INTERNE +  " STRING,"
			+ Raccordement.REFERENCE_OUTIL_ABOUTISSANT + " STRING,"
			+ Raccordement.REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT + " STRING,"
			+ Raccordement.REFERENCE_OUTIL_TENANT +  " STRING,"
			+ Raccordement.REFERENCE_ACCESSOIRE_OUTIL_TENANT +  " STRING,"
			+ Raccordement.REGLAGE_OUTIL_TENANT + " STRING,"
			+ Raccordement.REPERE_ELECTRIQUE_ABOUTISSANT +  " STRING,"
			+ Raccordement.REPERE_ELECTRIQUE_TENANT +  " STRING,"
			+ Raccordement.REGLAGE_OUTIL_ABOUTISSANT + " STRING,"
			+ Raccordement.REPRISE_BLINDAGE +  " STRING,"
			+ Raccordement.SANS_REPRISE_BLINDAGE +  " STRING,"
			+ Raccordement.TYPE_ELEMENT_RACCORDE +  " STRING,"
			+ Raccordement.TYPE_FIL_CABLE +  " STRING,"
			+ Raccordement.TYPE_RACCORDEMENT_ABOUTISSANT +  " STRING,"
			+ Raccordement.TYPE_RACCORDEMENT_TENANT +  " STRING,"
			+ Raccordement.ZONE_ACTIVITE +  " STRING,"	
			+ Raccordement.NUMERO_OPERATION +  " STRING,"
			+ Raccordement.NUMERO_REPERE_TABLE_CHEMINEMENT +  " FLOAT,"
			+ Raccordement.NUMERO_CHEMINEMENT +  " FLOAT,"
			+ Raccordement.NUMERO_SECTION_CHEMINEMENT +  " FLOAT,"
			+ Raccordement.NUMERO_POSITION_CHARIOT +  " STRING,"	
			+ Raccordement.NUMERO_SERIE_OUTIL +  " FLOAT,"	
			+ Raccordement.NUMERO_FICHE_JALON +  " STRING"	
			+ ");");
			
			}

			// Cette méthode sert à gérer la montée de version de notre base 
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + RaccordementProvider.CONTENT_PROVIDER_TABLE_NAME);
				onCreate(db);
			
			}
		

		
		}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseRaccordement(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getReadableDatabase();	
		if (id < 0) {
			return 	db.query(RaccordementProvider.CONTENT_PROVIDER_TABLE_NAME,
	projection, selection, selectionArgs, null, null,
		sortOrder);
		} else {
			return 		db.query(RaccordementProvider.CONTENT_PROVIDER_TABLE_NAME,
		projection, Raccordement._id + "=" + id, null, null, null,
		null);
		} 
	}

	@Override
	public String getType(Uri uri) {
		return RaccordementProvider.CONTENT_PROVIDER_MIME;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			
	long id = db.insertOrThrow(RaccordementProvider.CONTENT_PROVIDER_TABLE_NAME, null, values);
				
	if (id == -1) {
				throw new RuntimeException(String.format(
				"%s : Failed to insert [%s] for unknown reasons.","RaccordementProvider", values, uri));
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
						RaccordementProvider.CONTENT_PROVIDER_TABLE_NAME,
						selection, selectionArgs);
			else
				return db.delete(
						RaccordementProvider.CONTENT_PROVIDER_TABLE_NAME,
						Raccordement._id + "=" + id, selectionArgs);
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
	return db.update( RaccordementProvider.CONTENT_PROVIDER_TABLE_NAME,values, selection, selectionArgs);
		else
				return db.update(RaccordementProvider.CONTENT_PROVIDER_TABLE_NAME,
				values, Raccordement._id + "=" + id, null); 
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
				Log.e("RaccordementProvider", "Number Format Exception : " + e);
			}
		}
		return -1;
		
}

}
