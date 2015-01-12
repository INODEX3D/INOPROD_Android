package com.inodex.inoprod.business;

import android.provider.BaseColumns;

public class TableCheminement {
	
	public TableCheminement() {}
	
	public final static class Cheminement implements BaseColumns {
		public static final String _id = "_id";
		public static final String ORDRE_REALISATION = "ORDRE_REALISATION";
		public static final String REPERE_ELECTRIQUE = "REPERE_ELECTRIQUE";
		public static final String NUMERO_COMPOSANT = "NUMERO_COMPOSANT";
		public static final String ZONE_ACTIVITE = "ZONE_ACTIVITE";
		public static final String LOCALISATION1 = "LOCALISATION1";
		public static final String NUMERO_REPERE_TABLE_CHEMINEMENT = "NUMERO_REPERE_TABLE_CHEMINEMENT";
		public static final String TYPE_SUPPORT = "TYPE_SUPPORT";
		public static final String NUMERO_SECTION_CHEMINEMENT = "NUMERO_SECTION_CHEMINEMENT";
		public static final String CODE_TAG_SCANNE = "CODE_TAG_SCANNE";
		
		
	}

}
