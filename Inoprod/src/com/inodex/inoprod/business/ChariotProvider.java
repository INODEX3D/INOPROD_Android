package com.inodex.inoprod.business;

import com.inodex.inoprod.business.Outillage.Outil;


import com.inodex.inoprod.business.TableChariots.Chariot;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class ChariotProvider extends ContentProvider {
	
	DatabaseChariot dbHelper;
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.inodex.inoprod.business.chariots");

	/** Nom de la base de données */
	public static final String CONTENT_PROVIDER_DB_NAME = "chariots.db";
	/** Version de la base de données */
	public static final int CONTENT_PROVIDER_DB_VERSION = 1;
	/** Nom de la table de la base de données */
	public static final String CONTENT_PROVIDER_TABLE_NAME = "chariot";
	/** Le Mime du content Provider */
	public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.com.inodex.inoprod.business.chariot";

	/**
	 * Classe interne comprenant la base de données SQLite qui sera utilisée
	 * 
	 * @author Arnaud Payet
	 * 
	 */
	private static class DatabaseChariot extends SQLiteOpenHelper {

		/**
		 * Création à partir du Context, du Nom de la table et du numéro de
		 * version
		 * 
		 * @param context
		 */
		DatabaseChariot(Context context) {
			super(context, ChariotProvider.CONTENT_PROVIDER_DB_NAME, null,
					ChariotProvider.CONTENT_PROVIDER_DB_VERSION);
		}

		/**
		 * Création des tables
		 * 
		 * @param db
		 *            , SQLiteDatabase
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE "
					+ ChariotProvider.CONTENT_PROVIDER_TABLE_NAME + " ("
					+ Chariot._id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ Chariot.CODE_TAG + " STRING,"
					+ Chariot.CONNECTEUR_POSITIONNE + " STRING,"
					+ Chariot.FACE_CHARIOT + " STRING,"
					+ Chariot.NUMERO_CHARIOT + " STRING," 
					+ Chariot.POSITION_NUMERO +" STRING"
					 + ");");
		}

		/**
		 * Cette méthode sert à gérer la montée de version de la base
		 * 
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "
					+ ChariotProvider.CONTENT_PROVIDER_TABLE_NAME);
			onCreate(db);

		}

	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseChariot(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		if (id < 0) {
			return db
					.query(ChariotProvider.CONTENT_PROVIDER_TABLE_NAME,
							projection, selection, selectionArgs, null, null,
							sortOrder);
		} else {
			return db.query(ChariotProvider.CONTENT_PROVIDER_TABLE_NAME,
					projection,Chariot._id + "=" + id, null, null, null, null);
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

			long id = db
					.insertOrThrow(
							ChariotProvider.CONTENT_PROVIDER_TABLE_NAME,
							null, values);

			if (id == -1) {
				throw new RuntimeException(String.format(
						"%s : Failed to insert [%s] for unknown reasons.",
						"ChariotProvider", values, uri));
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
				return db.delete(ChariotProvider.CONTENT_PROVIDER_TABLE_NAME,
						selection, selectionArgs);
			else
				return db.delete(ChariotProvider.CONTENT_PROVIDER_TABLE_NAME,
						Chariot._id + "=" + id, selectionArgs);
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
				return db.update(ChariotProvider.CONTENT_PROVIDER_TABLE_NAME,
						values, selection, selectionArgs);
			else
				return db.update(ChariotProvider.CONTENT_PROVIDER_TABLE_NAME,
						values,Chariot._id + "=" + id, null);
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
				Log.e("ChariotProvider", "Number Format Exception : " + e);
			}
		}
		return -1;

	}

}
