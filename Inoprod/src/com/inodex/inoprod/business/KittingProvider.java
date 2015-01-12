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

import com.inodex.inoprod.business.TableKittingCable.Kitting;

/**
 * Classe derivant de Content Provider servant � la cr�ation et la manipulation
 * de la table Kitting
 * 
 * @author Arnaud Payet
 * 
 */
public class KittingProvider extends ContentProvider {

	DatabaseKitting dbHelper;
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.inodex.inoprod.business.kitting");

	/** Nom de la base de donn�es */
	public static final String CONTENT_PROVIDER_DB_NAME = "kitting.db";
	/** Version de la base de donn�es */
	public static final int CONTENT_PROVIDER_DB_VERSION = 1;
	/** Nom de la table de la base de donn�es */
	public static final String CONTENT_PROVIDER_TABLE_NAME = "kitting";
	/** Le Mime du content Provider */
	public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.com.inodex.inoprod.business.kitting";

	/**
	 * Classe interne comprenant la base de donn�es SQLite qui sera utilis�e
	 * 
	 * @author Arnaud Payet
	 * 
	 */
	private static class DatabaseKitting extends SQLiteOpenHelper {

		/**
		 * Cr�ation � partir du Context, du Nom de la table et du num�ro de
		 * version
		 * 
		 * @param context
		 */
		DatabaseKitting(Context context) {
			super(context, KittingProvider.CONTENT_PROVIDER_DB_NAME, null,
					KittingProvider.CONTENT_PROVIDER_DB_VERSION);
		}

		/**
		 * Cr�ation des tables
		 * 
		 * @param db
		 *            , SQLiteDatabase
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE "
					+ KittingProvider.CONTENT_PROVIDER_TABLE_NAME + " ("
					+ Kitting._id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ Kitting.DESIGNATION_COMPOSANT + " STRING,"
					+ Kitting.ETAT_LIAISON_FIL + " STRING,"
					+ Kitting.FOURNISSEUR_FABRICANT + " STRING,"
					+ Kitting.LONGUEUR_FIL_CABLE + " FLOAT,"
					+ Kitting.NUMERO_CHEMINEMENT + " INTEGER,"
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
					+ Kitting.REPERE_ELECTRIQUE + " STRING,"
					+ Kitting.TYPE_FIL_CABLE + " STRING," + Kitting.UNITE
					+ " STRING" + ");");
		}

		/**
		 * Cette m�thode sert � g�rer la mont�e de version de la base
		 * 
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "
					+ KittingProvider.CONTENT_PROVIDER_TABLE_NAME);
			onCreate(db);

		}

	}

	/**
	 * Cr�ation de la base de donn�es
	 * 
	 * @return R�ussite de la cr�ation
	 */
	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseKitting(getContext());
		return true;
	}

	/**
	 * Requete d'�l�ments de la base de donn�es
	 * 
	 * @param Uri
	 *            de la base
	 * @param projection
	 *            , colonnes s�l�ctionn�s
	 * @param selection
	 *            , �quivalent au WHERE clause
	 * @param selectionArgs
	 *            , arguments s�l�ctionn�s
	 * @param sortOrder
	 *            , ordre de tri
	 * @return Curseur contenant les �l�ments de la requ�te
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		if (id < 0) {
			return db
					.query(KittingProvider.CONTENT_PROVIDER_TABLE_NAME,
							projection, selection, selectionArgs, null, null,
							sortOrder);
		} else {
			return db.query(KittingProvider.CONTENT_PROVIDER_TABLE_NAME,
					projection, Kitting._id + "=" + id, null, null, null, null);
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
		return KittingProvider.CONTENT_PROVIDER_MIME;
	}

	/**
	 * Permet d'ajouter un �l�ment � la base de donn�es
	 * 
	 * @param Uri
	 *            de la base
	 * @param values
	 *            , valeur de l'�l�ment � rajouter
	 * @return Uri de la base
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {

			long id = db.insertOrThrow(
					KittingProvider.CONTENT_PROVIDER_TABLE_NAME, null, values);

			if (id == -1) {
				throw new RuntimeException(String.format(
						"%s : Failed to insert [%s] for unknown reasons.",
						"KittingProvider", values, uri));
			} else {
				return ContentUris.withAppendedId(uri, id);
			}

		} finally {
			db.close();
		}
	}

	/**
	 * Suppression d'un �l�ment de la base de donn�es
	 * 
	 * @param Uri
	 *            de la base
	 * @param selection
	 *            , �quivalent au WHERE clause
	 * @param selectionArgs
	 *            , arguments s�l�ctionn�s
	 * @return indice de l'�l�ment supprim�
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			if (id < 0)
				return db.delete(KittingProvider.CONTENT_PROVIDER_TABLE_NAME,
						selection, selectionArgs);
			else
				return db.delete(KittingProvider.CONTENT_PROVIDER_TABLE_NAME,
						Kitting._id + "=" + id, selectionArgs);
		} finally {
			db.close();
		}
	}

	/**
	 * Mise � jour d'�l�ments de la base de donn�es
	 * 
	 * @param Uri
	 *            de la base
	 * @param values
	 *            , valeurs � mettre � jour
	 * @param selection
	 *            , �quivalent au WHERE clause
	 * @param selectionArgs
	 *            , arguments s�l�ctionn�
	 * @return id de l'�lement mis � jour
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		long id = getId(uri);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		try {
			if (id < 0)
				return db.update(KittingProvider.CONTENT_PROVIDER_TABLE_NAME,
						values, selection, selectionArgs);
			else
				return db.update(KittingProvider.CONTENT_PROVIDER_TABLE_NAME,
						values, Kitting._id + "=" + id, null);
		} finally {
			db.close();
		}
	}

	/**
	 * Obtention de l'id � partir d'une Uri
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
				Log.e("KittingProvider", "Number Format Exception : " + e);
			}
		}
		return -1;

	}

}
