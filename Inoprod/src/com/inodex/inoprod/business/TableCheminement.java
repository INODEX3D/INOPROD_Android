package com.inodex.inoprod.business;

import android.provider.BaseColumns;

public class TableCheminement {
	
	public TableCheminement() {}
	
	public final static class Cheminement implements BaseColumns {
		public static final String _id = "_id";
		public static final String ORDRE_REALISATION = "ORDRE_REALISATION";
		public static final String REPERE_ELECTRIQUE_TENANT = "REPERE_ELECTRIQUE_TENANT";
		public static final String REPERE_ELECTRIQUE_ABOUTISSANT = "REPERE_ELECTRIQUE_ABOUTISSANT";
		public static final String NUMERO_COMPOSANT_TENANT = "NUMERO_COMPOSANT_TENANT";
		public static final String NUMERO_COMPOSANT_ABOUTISSANT = "NUMERO_COMPOSANT_ABOUTISSANT";
		public static final String ZONE_ACTIVITE = "ZONE_ACTIVITE";
		public static final String LOCALISATION1 = "LOCALISATION1";
		public static final String NUMERO_REPERE_TABLE_CHEMINEMENT = "NUMERO_REPERE_TABLE_CHEMINEMENT";
		public static final String TYPE_SUPPORT = "TYPE_SUPPORT";
		public static final String NUMERO_SECTION_CHEMINEMENT = "NUMERO_SECTION_CHEMINEMENT";
		public static final String CODE_TAG_SCANNE = "CODE_TAG_SCANNE";
		public static final String NUMERO_CHEMINEMNT = "NUMERO_CHEMINEMNT";
		public static final String NUMERO_FIL_CABLE = "NUMERO_FIL_CABLE";
		public static final String NUMERO_ROUTE = "NUMERO_ROUTE";
		public static final String TYPE_FIL_CABLE = "TYPE_FIL_CABLE";
		public static final String LONGUEUR_FIL_CABLE = "LONGUEUR_FIL_CABLE";
		public static final String UNITE = "UNITE";
		
		
	}

}
