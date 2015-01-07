package com.v1.inoprod.business;



import com.v1.inoprod.business.TableBOM.BOM;
import com.v1.inoprod.business.TableKittingCable.Kitting;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class KittingProvider extends ContentProvider {
	
	
	DatabaseKitting dbHelper;
	public static final Uri CONTENT_URI = Uri.parse("content://com.v1.inoprod.business.kitting");

	/** Nom de la base de données */
		public static final String CONTENT_PROVIDER_DB_NAME = "kitting.db";
	/** Version de la base de données */
		public static final int CONTENT_PROVIDER_DB_VERSION = 1;
	/** Nom de la table de la base de données */
		public static final String CONTENT_PROVIDER_TABLE_NAME = "kitting";
	/** Le Mime du content Provider */
		public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.com.v1.inoprod.business.kitting";
		

		
		/** Classe interne comprenant la base de données SQLite qui sera utilisée 
		 * 
		 * @author Arnaud Payet
		 *
		 */
		private static class DatabaseKitting extends SQLiteOpenHelper {

			/** Création à partir du Context, du Nom de la table et du numéro de version
			 *  
			 * @param context
			 */
			DatabaseKitting(Context context) {
		super(context,KittingProvider.CONTENT_PROVIDER_DB_NAME, null, KittingProvider.CONTENT_PROVIDER_DB_VERSION);
			}
			
			/** Création des tables 
			 * @param db, SQLiteDatabase
			 */
			@Override
			public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + KittingProvider.CONTENT_PROVIDER_TABLE_NAME + " (" 
				+ Kitting._id + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ Kitting.DESIGNATION_COMPOSANT + " STRING," 
				+ Kitting.ETAT_LIAISON_FIL + " STRING," 
				+ Kitting.FOURNISSEUR_FABRICANT + " STRING," 
				+ Kitting.LONGUEUR_FIL_CABLE + " FLOAT,"
				+ Kitting.NUMERO_CHEMINEMENT + " FLOAT,"
				+ Kitting.NUMERO_COMPOSANT + " STRING," 
				+ Kitting.NUMERO_DEBIT + " INTEGER,"
				+ Kitting.NUMERO_FIL_CABLE + " STRING," 
				+ Kitting.NORME_CABLE + " STRING," 
				+ Kitting.NUMERO_LOT_SCANNE + " STRING," 
				+ Kitting.NUMERO_OPERATION + " STRING," 
				+ Kitting.NUMERO_POSITION_CHARIOT + " STRING," 
				+ Kitting.NUMERO_REVISION_FIL + " FLOAT," 
				+ Kitting.ORDRE_REALISATION + " STRING," 
				+ Kitting.REFERENCE_FABRICANT1 + " STRING," 
				+ Kitting.REFERENCE_FABRICANT2 + " STRING," 
				+ Kitting.REFERENCE_FABRICANT_SCANNE + " STRING," 
				+ Kitting.REFERENCE_INTERNE + " STRING," 
				+ Kitting.REPERE_ELECTRIQUE_TENANT + " STRING," 
				+ Kitting.TYPE_FIL_CABLE + " STRING," 
				+ Kitting.UNITE + " STRING" 
				+ ");");
			}

		
			
			/** Cette méthode sert à gérer la montée de version de la base 
			 * 
			 */
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + KittingProvider.CONTENT_PROVIDER_TABLE_NAME);
				onCreate(db);
			
			}
		

		
		}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseKitting(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getReadableDatabase();	
		if (id < 0) {
			return 	db.query(KittingProvider.CONTENT_PROVIDER_TABLE_NAME,
	projection, selection, selectionArgs, null, null,
		sortOrder);
		} else {
			return 		db.query(KittingProvider.CONTENT_PROVIDER_TABLE_NAME,
		projection, Kitting._id + "=" + id, null, null, null,
		null);
		} 
	}

	@Override
	public String getType(Uri uri) {
		return KittingProvider.CONTENT_PROVIDER_MIME;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			
	long id = db.insertOrThrow(	KittingProvider.CONTENT_PROVIDER_TABLE_NAME, null, values);
				
	if (id == -1) {
				throw new RuntimeException(String.format(
				"%s : Failed to insert [%s] for unknown reasons.","KittingProvider", values, uri));
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
						KittingProvider.CONTENT_PROVIDER_TABLE_NAME,
						selection, selectionArgs);
			else
				return db.delete(
						KittingProvider.CONTENT_PROVIDER_TABLE_NAME,
						Kitting._id + "=" + id, selectionArgs);
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
	return db.update( KittingProvider.CONTENT_PROVIDER_TABLE_NAME,values, selection, selectionArgs);
		else
				return db.update(KittingProvider.CONTENT_PROVIDER_TABLE_NAME,
				values, Kitting._id + "=" + id, null); 
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
				Log.e("KittingProvider", "Number Format Exception : " + e);
			}
		}
		return -1;
		
}

}
