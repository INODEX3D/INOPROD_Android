package com.v1.inoprod.business;



import com.v1.inoprod.business.AnnuairePersonel.Employe;
import com.v1.inoprod.business.Nomenclature.Cable;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class NomenclatureProvider extends ContentProvider {
	
	DatabaseNomenclature dbHelper;
	public static final Uri CONTENT_URI = Uri.parse("content://com.v1.inoprod.business.nomenclature");

	// Nom de notre base de données 
		public static final String CONTENT_PROVIDER_DB_NAME = "nomenclature.db";
	// Version de notre base de données 
		public static final int CONTENT_PROVIDER_DB_VERSION = 1;
	// Nom de la table de notre base 
		public static final String CONTENT_PROVIDER_TABLE_NAME = "cable";
	// Le Mime de notre content provider, la premiére partie est toujours identique 
		public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.com.v1.inoprod.business.cable";

	
	// Notre DatabaseHelper 
	private static class DatabaseNomenclature extends SQLiteOpenHelper {

		// Création à partir du Context, du Nom de la table et du numéro de version 
		DatabaseNomenclature(Context context) {
	super(context,NomenclatureProvider.CONTENT_PROVIDER_DB_NAME, null, NomenclatureProvider.CONTENT_PROVIDER_DB_VERSION);
		}
		
		// Création des tables 
		@Override
		public void onCreate(SQLiteDatabase db) {
	db.execSQL("CREATE TABLE " + NomenclatureProvider.CONTENT_PROVIDER_TABLE_NAME + " (" 
		+ Cable._id + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
		+ Cable.ACCESSOIRE_CABLAGE + " STRING, "
		+ Cable.ACCESSOIRE_COMPOSANT + " STRING, "
		+ Cable.NORME_CABLE + " STRING, "
		+ Cable.DESIGNATION_COMPOSANT + " STRING, "
		+ Cable.DESIGNATION_PRODUIT + " STRING, "
		+ Cable.EQUIPEMENT + " STRING, "
		+ Cable.FAMILLE_PRODUIT + " STRING, "
		+ Cable.FOURNISSEUR_FABRICANT + " STRING, "
		+ Cable.NUMERO_COMPOSANT + " STRING, "
		+ Cable.NUMERO_HARNAIS_FAISCEAUX + " FLOAT," 
		+ Cable.NUMERO_REVISION_HARNAIS + " FLOAT," 
		+ Cable.ORDRE_REALISATION + " STRING, "
		+ Cable.QUANTITE + " FLOAT,"
		+ Cable.REFERENCE_FABRICANT1 + " STRING, "
		+ Cable.REFERENCE_FABRICANT2 + " STRING, "
		+ Cable.REFERENCE_FICHIER_SOURCE + " STRING, "
		+ Cable.REFERENCE_IMPOSEE + " BOOLEAN, "
		+ Cable.REFERENCE_INTERNE + " STRING, "
		+ Cable.REPERE_ELECTRIQUE + " STRING, "
		+ Cable.STANDARD + " FLOAT,"
		+ Cable.UNITE + " STRING" + ");");
		
		}

		// Cette méthode sert à gérer la montée de version de notre base 
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	db.execSQL("DROP TABLE IF EXISTS " + NomenclatureProvider.CONTENT_PROVIDER_TABLE_NAME);
			onCreate(db);
		
		}
	

	
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseNomenclature(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
			long id = getId(uri);
			SQLiteDatabase db = dbHelper.getReadableDatabase();	
			if (id < 0) {
				return 	db.query(NomenclatureProvider.CONTENT_PROVIDER_TABLE_NAME,
		projection, selection, selectionArgs, null, null,
			sortOrder);
			} else {
				return 		db.query(NomenclatureProvider.CONTENT_PROVIDER_TABLE_NAME,
			projection, Cable._id + "=" + id, null, null, null,
			null);
			} 
	}

	@Override
	public String getType(Uri uri) {
		return NomenclatureProvider.CONTENT_PROVIDER_MIME;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			
	long id = db.insertOrThrow(	NomenclatureProvider.CONTENT_PROVIDER_TABLE_NAME, null, values);
				
	if (id == -1) {
				throw new RuntimeException(String.format(
				"%s : Failed to insert [%s] for unknown reasons.","NomenclatureProvider", values, uri));
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
						NomenclatureProvider.CONTENT_PROVIDER_TABLE_NAME,
						selection, selectionArgs);
			else
				return db.delete(
						NomenclatureProvider.CONTENT_PROVIDER_TABLE_NAME,
						Cable._id + "=" + id, selectionArgs);
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
	return db.update( NomenclatureProvider.CONTENT_PROVIDER_TABLE_NAME,values, selection, selectionArgs);
		else
				return db.update(NomenclatureProvider.CONTENT_PROVIDER_TABLE_NAME,
				values, Cable._id + "=" + id, null); 
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
				Log.e("NomenclatureProvider", "Number Format Exception : " + e);
			}
		}
		return -1;
		
}

}
