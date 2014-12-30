package com.v1.inoprod.business;

import com.v1.inoprod.business.Durees.Duree;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class DureesProvider extends ContentProvider {
	
	DatabaseDurees dbHelper;
	public static final Uri CONTENT_URI = Uri.parse("content://com.v1.inoprod.business.durees");

	/** Nom de la base de donn�es */
		public static final String CONTENT_PROVIDER_DB_NAME = "durees.db";
	/** Version de la base de donn�es */
		public static final int CONTENT_PROVIDER_DB_VERSION = 1;
	/** Nom de la table de la base de donn�es */
		public static final String CONTENT_PROVIDER_TABLE_NAME = "duree";
	/** Le Mime du content Provider */
		public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.com.v1.inoprod.business.duree";
		

		
		/** Classe interne comprenant la base de donn�es SQLite qui sera utilis�e 
		 * 
		 * @author Arnaud Payet
		 *
		 */
		private static class DatabaseDurees extends SQLiteOpenHelper {

			/** Cr�ation � partir du Context, du Nom de la table et du num�ro de version
			 *  
			 * @param context
			 */
			DatabaseDurees(Context context) {
		super(context,DureesProvider.CONTENT_PROVIDER_DB_NAME, null, DureesProvider.CONTENT_PROVIDER_DB_VERSION);
			}
			
			/** Cr�ation des tables 
			 * @param db, SQLiteDatabase
			 */
			@Override
			public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + DureesProvider.CONTENT_PROVIDER_TABLE_NAME + " (" 
				+ Duree._id + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ Duree.CODE_OPERATION + " FLOAT,"
				+ Duree.DESIGNATION_OPERATION + " STRING,"
				+ Duree.UNITE + " STRING,"
				+ Duree.DUREE_THEORIQUE + " STRING,"
				+ Duree.OPERATION_SOUS_CONTROLE + " BOOLEAN"
				+ ");");
			}

		
			
			/** Cette m�thode sert � g�rer la mont�e de version de la base 
			 * 
			 */
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + DureesProvider.CONTENT_PROVIDER_TABLE_NAME);
				onCreate(db);
			
			}
		

		
		}
		
	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseDurees(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getReadableDatabase();	
		if (id < 0) {
			return 	db.query(DureesProvider.CONTENT_PROVIDER_TABLE_NAME,
	projection, selection, selectionArgs, null, null,
		sortOrder);
		} else {
			return 		db.query(DureesProvider.CONTENT_PROVIDER_TABLE_NAME,
		projection, Duree._id + "=" + id, null, null, null,
		null);
		} 
	}

	@Override
	public String getType(Uri uri) {
		return DureesProvider.CONTENT_PROVIDER_MIME;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			
	long id = db.insertOrThrow(	DureesProvider.CONTENT_PROVIDER_TABLE_NAME, null, values);
				
	if (id == -1) {
				throw new RuntimeException(String.format(
				"%s : Failed to insert [%s] for unknown reasons.","DureesProvider", values, uri));
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
						DureesProvider.CONTENT_PROVIDER_TABLE_NAME,
						selection, selectionArgs);
			else
				return db.delete(
						DureesProvider.CONTENT_PROVIDER_TABLE_NAME,
						Duree._id + "=" + id, selectionArgs);
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
	return db.update( DureesProvider.CONTENT_PROVIDER_TABLE_NAME,values, selection, selectionArgs);
		else
				return db.update(DureesProvider.CONTENT_PROVIDER_TABLE_NAME,
				values, Duree._id + "=" + id, null); 
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
				Log.e("DureesProvider", "Number Format Exception : " + e);
			}
		}
		return -1;
		
}

}
