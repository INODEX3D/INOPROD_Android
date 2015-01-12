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

import com.inodex.inoprod.business.TableBOM.BOM;

/**
 * Classe derivant de Content Provider servant à la création et la manipulation
 * de la table BOM
 * 
 * @author Arnaud Payet
 * 
 */
public class BOMProvider extends ContentProvider {

	DatabaseBOM dbHelper;
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.inodex.inoprod.business.bom");

	/** Nom de la base de données */
	public static final String CONTENT_PROVIDER_DB_NAME = "bom.db";
	/** Version de la base de données */
	public static final int CONTENT_PROVIDER_DB_VERSION = 1;
	/** Nom de la table de la base de données */
	public static final String CONTENT_PROVIDER_TABLE_NAME = "bom";
	/** Le Mime du content Provider */
	public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.com.inodex.inoprod.business.bom";

	/**
	 * Classe interne comprenant la base de données SQLite qui sera utilisée
	 * 
	 * @author Arnaud Payet
	 * 
	 */
	private static class DatabaseBOM extends SQLiteOpenHelper {

		/**
		 * Création à partir du Context, du Nom de la table et du numéro de
		 * version
		 * 
		 * @param context
		 */
		DatabaseBOM(Context context) {
			super(context, BOMProvider.CONTENT_PROVIDER_DB_NAME, null,
					BOMProvider.CONTENT_PROVIDER_DB_VERSION);
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
					+ BOMProvider.CONTENT_PROVIDER_TABLE_NAME + " (" + BOM._id
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ BOM.ACCESSOIRE_CABLAGE + " STRING,"
					+ BOM.ACCESSOIRE_COMPOSANT + " STRING,"
					+ BOM.DESIGNATION_COMPOSANT + " STRING," + BOM.EQUIPEMENT
					+ " STRING," + BOM.FAMILLE_PRODUIT + " STRING,"
					+ BOM.FOURNISSEUR_FABRICANT + " STRING,"
					+ BOM.NUMERO_CHEMINEMENT + " FLOAT," + BOM.NUMERO_COMPOSANT
					+ " STRING," + BOM.NUMERO_DEBIT + " FLOAT,"
					+ BOM.NUMERO_LOT_SCANNE + " STRING," + BOM.NUMERO_OPERATION
					+ " STRING," + BOM.NUMERO_POSITION_CHARIOT + " STRING,"
					+ BOM.NUMERO_SECTION_CHEMINEMENT + " FLOAT,"
					+ BOM.ORDRE_REALISATION + " STRING," + BOM.QUANTITE
					+ " FLOAT," + BOM.REFERENCE_FABRICANT2 + " STRING,"
					+ BOM.REFERENCE_FABRICANT_SCANNE + " STRING,"
					+ BOM.REFERENCE_INTERNE + " STRING,"
					+ BOM.REPERE_ELECTRIQUE_TENANT + " STRING,"
					+ BOM.REFERENCE_IMPOSEE + " BOOLEAN," + BOM.UNITE
					+ " STRING" + ");");
		}

		/**
		 * Cette méthode sert à gérer la montée de version de la base
		 * 
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "
					+ BOMProvider.CONTENT_PROVIDER_TABLE_NAME);
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
		dbHelper = new DatabaseBOM(getContext());
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
					.query(BOMProvider.CONTENT_PROVIDER_TABLE_NAME, projection,
							selection, selectionArgs, null, null, sortOrder);
		} else {
			return db.query(BOMProvider.CONTENT_PROVIDER_TABLE_NAME,
					projection, BOM._id + "=" + id, null, null, null, null);
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
		return BOMProvider.CONTENT_PROVIDER_MIME;
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

			long id = db.insertOrThrow(BOMProvider.CONTENT_PROVIDER_TABLE_NAME,
					null, values);

			if (id == -1) {
				throw new RuntimeException(String.format(
						"%s : Failed to insert [%s] for unknown reasons.",
						"BOMProvider", values, uri));
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
				return db.delete(BOMProvider.CONTENT_PROVIDER_TABLE_NAME,
						selection, selectionArgs);
			else
				return db.delete(BOMProvider.CONTENT_PROVIDER_TABLE_NAME,
						BOM._id + "=" + id, selectionArgs);
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
				return db.update(BOMProvider.CONTENT_PROVIDER_TABLE_NAME,
						values, selection, selectionArgs);
			else
				return db.update(BOMProvider.CONTENT_PROVIDER_TABLE_NAME,
						values, BOM._id + "=" + id, null);
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
