package com.inodex.inoprod.business;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.inodex.inoprod.business.AnnuairePersonel.Employe;

/**
 * Classe derivant de Content Provider servant à la création et la manipulation
 * de la base de données Annuaire
 * 
 * @author Arnaud Payet
 * 
 */
public class AnnuaireProvider extends ContentProvider {

	DatabaseAnnuaire dbHelper;
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.inodex.inoprod.business.annuairepersonel");

	/** Nom de la base de données */
	public static final String CONTENT_PROVIDER_DB_NAME = "annuairepersonel.db";
	/** Version de la base de données */
	public static final int CONTENT_PROVIDER_DB_VERSION = 1;
	/** Nom de la table de la base de données */
	public static final String CONTENT_PROVIDER_TABLE_NAME = "employe";
	/** Le Mime du content Provider */
	public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.com.inodex.inoprod.business.employe";

	/**
	 * Classe interne comprenant la base de données SQLite qui sera utilisée
	 * 
	 * @author Arnaud Payet
	 * 
	 */
	private static class DatabaseAnnuaire extends SQLiteOpenHelper {

		/**
		 * Création à partir du Context, du Nom de la table et du numéro de
		 * version
		 * 
		 * @param context
		 */
		DatabaseAnnuaire(Context context) {
			super(context, AnnuaireProvider.CONTENT_PROVIDER_DB_NAME, null,
					AnnuaireProvider.CONTENT_PROVIDER_DB_VERSION);
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
					+ AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME + " ("
					+ Employe._id + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
					+ Employe.EMPLOYE_NOM + " STRING ,"
					+ Employe.EMPLOYE_PRENOM + " STRING ,"
					+ Employe.EMPLOYE_METIER + " STRING " + ");");
		}

		/**
		 * Cette méthode sert à gérer la montée de version de la base
		 * 
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "
					+ AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME);
			onCreate(db);

		}

	}

	/**
	 * Suppression d'un élément de la base de données
	 * 
	 * @param Uri
	 *            de la base
	 * @param selection
	 *            , équivalent au WHERE clause
	 * @param selectionArgs
	 *            , arguments séléctionnés
	 * @return indice de l'élément supprimé
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			if (id < 0)
				return db.delete(AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME,
						selection, selectionArgs);
			else
				return db.delete(AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME,
						Employe._id + "=" + id, selectionArgs);
		} finally {
			db.close();
		}
	}

	/**
	 * Permet d'obtenir le MIME de la base
	 * 
	 * @param Uri
	 *            de la base
	 * @return MIME du Content Provider
	 */
	@Override
	public String getType(Uri arg0) {
		return AnnuaireProvider.CONTENT_PROVIDER_MIME;
	}

	/**
	 * Permet d'ajouter un élément à la base de données
	 * 
	 * @param Uri
	 *            de la base
	 * @param values
	 *            , valeur de l'élément à rajouter
	 * @return Uri de la base
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {

			long id = db.insertOrThrow(
					AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME, null, values);

			if (id == -1) {
				throw new RuntimeException(String.format(
						"%s : Failed to insert [%s] for unknown reasons.",
						"AnnuaireProvider", values, uri));
			} else {
				return ContentUris.withAppendedId(uri, id);
			}

		} finally {
			db.close();
		}
	}

	/**
	 * Création de la base de données
	 * 
	 * @return Réussite de la création
	 */
	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseAnnuaire(getContext());
		return true;
	}

	/**
	 * Requete d'éléments de la base de données
	 * 
	 * @param Uri
	 *            de la base
	 * @param projection
	 *            , colonnes séléctionnés
	 * @param selection
	 *            , équivalent au WHERE clause
	 * @param selectionArgs
	 *            , arguments séléctionnés
	 * @param sortOrder
	 *            , ordre de tri
	 * @return Curseur contenant les éléments de la requête
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		if (id < 0) {
			return db
					.query(AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME,
							projection, selection, selectionArgs, null, null,
							sortOrder);
		} else {
			return db.query(AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME,
					projection, Employe._id + "=" + id, null, null, null, null);
		}
	}

	/**
	 * Mise à jour d'éléments de la base de données
	 * 
	 * @param Uri
	 *            de la base
	 * @param values
	 *            , valeurs à mettre à jour
	 * @param selection
	 *            , équivalent au WHERE clause
	 * @param selectionArgs
	 *            , arguments séléctionné
	 * @return id de l'élement mis à jour
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		try {
			if (id < 0)
				return db.update(AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME,
						values, selection, selectionArgs);
			else
				return db.update(AnnuaireProvider.CONTENT_PROVIDER_TABLE_NAME,
						values, Employe._id + "=" + id, null);
		} finally {
			db.close();
		}
	}

	/**
	 * Obtention de l'id à partir d'une Uri
	 * 
	 * @param Uri
	 *            de la base
	 * @return id
	 */
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
