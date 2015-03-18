package com.inodex.inoprod.business;

import android.provider.BaseColumns;

/** Classe définissant les colonnes de la base de données Nomenclature 
 * 
 * @author Arnaud Payet
 *
 */

public class Nomenclature {
	
	public Nomenclature() {}
	
	/** Entité cable de la base de données Nomenclature
	 * 
	 * @author Arnaud Payet
	 *
	 */
	public static final class Cable implements BaseColumns {
		
		public static final String _id = "_id";
		public static final String DESIGNATION_PRODUIT = "DESIGNATION_PRODUIT";
		public static final String REFERENCE_FICHIER_SOURCE = "REFERENCE_FICHIER_SOURCE";
		public static final String NUMERO_REVISION_HARNAIS = "NUMERO_REVISION_HARNAIS";
		public static final String NUMERO_HARNAIS_FAISCEAUX = "NUMERO_HARNAIS_FAISCEAUX";
		public static final String REFERENCE_FABRICANT1 = "REFERENCE_FABRICANT1";
		public static final String STANDARD = "STANDARD";
		public static final String EQUIPEMENT = "EQUIPEMENT";
		public static final String REPERE_ELECTRIQUE = "REPERE_ELECTRIQUE";
		public static final String NUMERO_COMPOSANT= "NUMERO_COMPOSANT";
		public static final String NORME_CABLE = "NORME_CABLE";
		public static final String ORDRE_REALISATION = "ORDRE_REALISATION";
		public static final String ACCESSOIRE_CABLAGE = "ACCESSOIRE_CABLAGE";
		public static final String ACCESSOIRE_COMPOSANT = "ACCESSOIRE_COMPOSANT";
		public static final String DESIGNATION_COMPOSANT = "DESIGNATION_COMPOSANT";
		public static final String FAMILLE_PRODUIT = "FAMILLE_PRODUIT";
		public static final String REFERENCE_FABRICANT2 = "REFERENCE_FABRICANT2";
		public static final String FOURNISSEUR_FABRICANT = "FOURNISSEUR_FABRICANT";
		public static final String REFERENCE_INTERNE = "REFERENCE_INTERNE";
		public static final String REFERENCE_IMPOSEE = "REFERENCE_IMPOSEE";
		public static final String QUANTITE = "QUANTITE";
		public static final String UNITE = "UNITE";
		public static final String DESIGNATION_ACCESSOIRE = "DESIGNATION_ACCESSOIRE";
		
		
		private Cable() {}
		
	}

}
