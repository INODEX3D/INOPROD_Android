package com.v1.inoprod.business;

import android.provider.BaseColumns;

public class TableBOM {
	
	
	public TableBOM() {}
	
	public static final class BOM implements BaseColumns {
		
		public static final String _id = "_id";
		public static final String REPERE_ELECTRIQUE_TENANT = "REPERE_ELECTRIQUE_TENANT";
		public static final String NUMERO_COMPOSANT = "NUMERO_COMPOSANT";
		public static final String ORDRE_REALISATION = "ORDRE_REALISATION";
		public static final String EQUIPEMENT = "EQUIPEMENT";
		public static final String DESIGNATION_COMPOSANT = "DESIGNATION_COMPOSANT";
		public static final String UNITE = "UNITE";
		public static final String QUANTITE = "QUANITE";
		public static final String ACCESSOIRE_CABLAGE = "ACCESSOIRE_CABLAGE";
		public static final String ACCESSOIRE_COMPOSANT = "ACCESSOIRE_COMPOSANT";
		public static final String REFERENCE_FABRICANT2 = "REFERENCE_FABRICANT2";
		public static final String FOURNISSEUR_FABRICANT = "FOURNISSEUR_FABRICANT";
		public static final String REFERENCE_INTERNE = "REFERENCE_INTERNE";
		public static final String REFERENCE_IMPOSEE = "REFERENCE_IMPOSEE";
		public static final String FAMILLE_PRODUIT = "FAMILLE_PRODUIT";
		public static final String REFERENCE_FABRICANT_SCANNE = "REFERENCE_FABRICANT_SCANNE";
		public static final String NUMERO_LOT_SCANNE = "NUMERO_LOT_SCANNE";
		public static final String NUMERO_OPERATION = "NUMERO_OPERATION";
		public static final String NUMERO_CHEMINEMENT = "NUMERO_CHEMINEMENT";
		public static final String NUMERO_SECTION_CHEMINEMENT = "NUMERO_SECTION_CHEMINEMENT";
		public static final String NUMERO_DEBIT = "NUMERO_DEBIT";
		public static final String NUMERO_POSITION_CHARIOT = "NUMERO_POSITION_CHARIOT";
		
	}

}
