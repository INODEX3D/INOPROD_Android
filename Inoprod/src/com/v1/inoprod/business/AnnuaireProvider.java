package com.v1.inoprod.business;

import com.v1.inoprod.business.AnnuairePersonel.Employe;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;


public class AnnuaireProvider extends ContentProvider  {
	


	DatabaseHelper dbHelper;
	public static final Uri CONTENT_URI = Uri.parse("content://com.v1.inoprod.business.annuairepersonel");

	// Nom de notre base de données 
		public static final String CONTENT_PROVIDER_DB_NAME = "annuairepersonel.db";
	// Version de notre base de données 
		public static final int CONTENT_PROVIDER_DB_VERSION = 1;
	// Nom de la table de notre base 
		public static final String CONTENT_PROVIDER_TABLE_NAME = "employe";
	// Le Mime de notre content provider, la premiére partie est toujours identique 
		public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.com.v1.inoprod.business.employe";

		
	// Notre DatabaseHelper 
	private static class DatabaseHelper extends SQLiteOpenHelper {

		// Création à partir du Context, du Nom de la table et du numéro de version 
		DatabaseHelper(Context context) {
	super(context,AnnuaireProvider.CONTENT_PROVIDER_DB_NAME, null, AnnuaireProvider.CONTENT_PROVIDER_DB_VERSION);
		}
		
		// Création des tables 
		@Override
		public void onCreate(SQLiteDatabase db) {
	db.execSQL("CREATE TABLE " + AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME + " (" + Employe._id + " INTEGER PRIMARY KEY AUTOINCREMENT ," + Employe.EMPLOYE_NOM + " STRING ," + Employe.EMPLOYE_PRENOM + " STRING ," + Employe.EMPLOYE_METIER + " STRING " + ");");
		}

		// Cette méthode sert à gérer la montée de version de notre base 
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	db.execSQL("DROP TABLE IF EXISTS " + AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME);
			onCreate(db);
		
		}
	

	
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			if (id < 0)
				return db.delete(
						AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME,
						selection, selectionArgs);
			else
				return db.delete(
						AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME,
						Employe._id + "=" + id, selectionArgs);
		} finally {
			db.close();
		}
}


	@Override
	public String getType(Uri arg0) {
		return AnnuaireProvider.CONTENT_PROVIDER_MIME;
	}


	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
	long id = db.insertOrThrow(			 AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME, null, values);
				
	if (id == -1) {
				throw new RuntimeException(String.format(
				"%s : Failed to insert [%s] for unknown reasons.","AnnuaireProvider", values, uri));
			} else {
				return ContentUris.withAppendedId(uri, id);
			}
			
	} finally {
				db.close();
			}
	}


	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}


	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getReadableDatabase();	
		if (id < 0) {
			return 	db.query(AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME,
	projection, selection, selectionArgs, null, null,
		sortOrder);
		} else {
			return 		db.query(AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME,
		projection, Employe._id + "=" + id, null, null, null,
		null);
		} 
	}
	
	

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
	try {
			if (id < 0)
	return db.update( AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME,values, selection, selectionArgs);
		else
				return db.update(								AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME,
				values, Employe._id + "=" + id, null); 
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
				Log.e("AnnuaireProvider", "Number Format Exception : " + e);
			}
		}
		return -1;
		
}




}
