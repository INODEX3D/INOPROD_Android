package com.v1.inoprod.business;



import com.v1.inoprod.business.Nomenclature.Cable;
import com.v1.inoprod.business.Outillage.Outil;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class OutillageProvider extends ContentProvider {
	
	
	DatabaseOutillage dbHelper;
	public static final Uri CONTENT_URI = Uri.parse("content://com.v1.inoprod.business.outillage");

	/** Nom de la base de données */
		public static final String CONTENT_PROVIDER_DB_NAME = "outillage.db";
	/** Version de la base de données */
		public static final int CONTENT_PROVIDER_DB_VERSION = 1;
	/** Nom de la table de la base de données */
		public static final String CONTENT_PROVIDER_TABLE_NAME = "outil";
	/** Le Mime du content Provider */
		public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.com.v1.inoprod.business.outil";
		
		
		/** Classe interne comprenant la base de données SQLite qui sera utilisée 
		 * 
		 * @author Arnaud Payet
		 *
		 */
		private static class DatabaseOutillage extends SQLiteOpenHelper {

			/** Création à partir du Context, du Nom de la table et du numéro de version
			 *  
			 * @param context
			 */
			DatabaseOutillage(Context context) {
		super(context,OutillageProvider.CONTENT_PROVIDER_DB_NAME, null, OutillageProvider.CONTENT_PROVIDER_DB_VERSION);
			}
			
			/** Création des tables 
			 * @param db, SQLiteDatabase
			 */
			@Override
			public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + OutillageProvider.CONTENT_PROVIDER_TABLE_NAME + " (" 
				+ Outil._id + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ Outil.AFFECTATION + " STRING,"
				+ Outil.CODE_BARRE  + " STRING,"
				+ Outil.COMMENTAIRES + " STRING,"
				+ Outil.CONSTRUCTEUR + " STRING,"
				+ Outil.DERNIERE_OPERATION + " STRING,"
				+ Outil.IDENTIFICATION +" FLOAT,"
				+ Outil.INTITULE + " STRING,"
				+ Outil.NUMERO_SERIE + " STRING,"
				+ Outil.PERIODE + " FLOAT," 
				+ Outil.PROCHAINE_OPERATION + " STRING,"
				+ Outil.PROPRIETAIRE + " STRING,"
				+ Outil.SECTION + " STRING,"
				+ Outil.TYPE + " STRING,"
				+ Outil.UNITE + " STRING"			
				+ ");");
			}

		
			
			/** Cette méthode sert à gérer la montée de version de la base 
			 * 
			 */
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + OutillageProvider.CONTENT_PROVIDER_TABLE_NAME);
				onCreate(db);
			
			}
		

		
		}


	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseOutillage(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getReadableDatabase();	
		if (id < 0) {
			return 	db.query(OutillageProvider.CONTENT_PROVIDER_TABLE_NAME,
	projection, selection, selectionArgs, null, null,
		sortOrder);
		} else {
			return 		db.query(OutillageProvider.CONTENT_PROVIDER_TABLE_NAME,
		projection, Outil._id + "=" + id, null, null, null,
		null);
		} 
	}

	@Override
	public String getType(Uri uri) {
		return OutillageProvider.CONTENT_PROVIDER_MIME;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			
	long id = db.insertOrThrow(	OutillageProvider.CONTENT_PROVIDER_TABLE_NAME, null, values);
				
	if (id == -1) {
				throw new RuntimeException(String.format(
				"%s : Failed to insert [%s] for unknown reasons.","OutillageProvider", values, uri));
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
						OutillageProvider.CONTENT_PROVIDER_TABLE_NAME,
						selection, selectionArgs);
			else
				return db.delete(
						OutillageProvider.CONTENT_PROVIDER_TABLE_NAME,
						Outil._id + "=" + id, selectionArgs);
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
	return db.update( OutillageProvider.CONTENT_PROVIDER_TABLE_NAME,values, selection, selectionArgs);
		else
				return db.update(OutillageProvider.CONTENT_PROVIDER_TABLE_NAME,
				values, Outil._id + "=" + id, null); 
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
				Log.e("OutillageProvider", "Number Format Exception : " + e);
			}
		}
		return -1;
		
}

}
