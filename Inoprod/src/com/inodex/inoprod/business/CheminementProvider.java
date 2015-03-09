package com.inodex.inoprod.business;

import com.inodex.inoprod.business.TableCheminement.Cheminement;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;


/**
 * Classe derivant de Content Provider servant à la création et la manipulation
 * de la table Cheminement
 * 
 * @author Arnaud Payet
 * 
 */
public class CheminementProvider extends ContentProvider {

	DatabaseCheminement dbHelper;
	
	/** URI de la base de données */
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.inodex.inoprod.business.cheminement");

	/** Nom de la base de données */
	public static final String CONTENT_PROVIDER_DB_NAME = "cheminement.db";
	/** Version de la base de données */
	public static final int CONTENT_PROVIDER_DB_VERSION = 1;
	/** Nom de la table de la base de données */
	public static final String CONTENT_PROVIDER_TABLE_NAME = "cheminement";
	/** Le Mime du content Provider */
	public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.com.inodex.inoprod.business.cheminement";

	/**
	 * Classe interne comprenant la base de données SQLite qui sera utilisée
	 * 
	 * @author Arnaud Payet
	 * 
	 */
	private static class DatabaseCheminement extends SQLiteOpenHelper {

		/**
		 * Création à partir du Context, du Nom de la table et du numéro de
		 * version
		 * 
		 * @param context
		 */
		DatabaseCheminement(Context context) {
			super(context, CheminementProvider.CONTENT_PROVIDER_DB_NAME, null,
					CheminementProvider.CONTENT_PROVIDER_DB_VERSION);
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
					+ CheminementProvider.CONTENT_PROVIDER_TABLE_NAME + " ("
					+ Cheminement._id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ Cheminement.CODE_TAG_SCANNE + " STRING,"
					+ Cheminement.LOCALISATION1 + " STRING,"
					+ Cheminement.NUMERO_COMPOSANT_TENANT + " STRING,"
					+ Cheminement.NUMERO_COMPOSANT_ABOUTISSANT + " STRING,"
					+ Cheminement.NUMERO_REPERE_TABLE_CHEMINEMENT + " STRING,"
					+ Cheminement.NUMERO_SECTION_CHEMINEMENT + " STRING,"
					+ Cheminement.ORDRE_REALISATION + " STRING,"
					+ Cheminement.REPERE_ELECTRIQUE_TENANT + " STRING,"
					+ Cheminement.REPERE_ELECTRIQUE_ABOUTISSANT + " STRING,"
					+ Cheminement.NUMERO_FIL_CABLE + " STRING,"
					+ Cheminement.NUMERO_CHEMINEMNT + " INTEGER,"
					+ Cheminement.NUMERO_ROUTE + " STRING,"
					+ Cheminement.UNITE + " STRING,"
					+ Cheminement.LONGUEUR_FIL_CABLE + " INTEGER,"
					+ Cheminement.TYPE_FIL_CABLE + " STRING,"
					+ Cheminement.TYPE_SUPPORT + " STRING,"
					+ Cheminement.ZONE_ACTIVITE + " STRING" + ");");
		}

		/**
		 * Cette méthode sert à gérer la montée de version de la base
		 * 
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "
					+ CheminementProvider.CONTENT_PROVIDER_TABLE_NAME);
			onCreate(db);

		}

	}

	
	/**
	 * Création de la base de données
	 * 
	 * @return Réussite de la création
	 */
	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseCheminement(getContext());
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
					.query(CheminementProvider.CONTENT_PROVIDER_TABLE_NAME,
							projection, selection, selectionArgs, null, null,
							sortOrder);
		} else {
			return db.query(CheminementProvider.CONTENT_PROVIDER_TABLE_NAME,
					projection, Cheminement._id + "=" + id, null, null, null,
					null);
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
	public String getType(Uri uri) {
		return CheminementProvider.CONTENT_PROVIDER_MIME;
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
					CheminementProvider.CONTENT_PROVIDER_TABLE_NAME, null,
					values);

			if (id == -1) {
				throw new RuntimeException(String.format(
						"%s : Failed to insert [%s] for unknown reasons.",
						"CheminementProvider", values, uri));
			} else {
				return ContentUris.withAppendedId(uri, id);
			}

		} finally {
			db.close();
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
				return db.delete(
						CheminementProvider.CONTENT_PROVIDER_TABLE_NAME,
						selection, selectionArgs);
			else
				return db.delete(
						CheminementProvider.CONTENT_PROVIDER_TABLE_NAME,
						Cheminement._id + "=" + id, selectionArgs);
		} finally {
			db.close();
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
				return db.update(
						CheminementProvider.CONTENT_PROVIDER_TABLE_NAME,
						values, selection, selectionArgs);
			else
				return db.update(
						CheminementProvider.CONTENT_PROVIDER_TABLE_NAME,
						values, Cheminement._id + "=" + id, null);
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
				Log.e("BOMProvider", "Number Format Exception : " + e);
			}
		}
		return -1;

	}

}
