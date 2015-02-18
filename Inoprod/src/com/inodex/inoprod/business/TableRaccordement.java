package com.inodex.inoprod.business;

import android.provider.BaseColumns;

public class TableRaccordement {
	
	public TableRaccordement() {} 
	
	public static final class Raccordement implements BaseColumns {
		public static final String _id = "_id";
		public static final String ETAT_LIAISON_FIL = "ETAT_LIAISON_FIL";
		public static final String NUMERO_REVISION_FIL = "NUMERO_REVISION_FIL";
		public static final String FIL_SENSIBLE = "FIL_SENSIBLE";
		public static final String NUMERO_FIL_CABLE = "NUMERO_FIL_CABLE";
		public static final String NORME_CABLE = "NORME_CABLE";
		public static final String TYPE_FIL_CABLE = "TYPE_FIL_CABLE";
		public static final String LONGUEUR_FIL_CABLE = "LONGUEUR_FIL_CABLE";
		public static final String NUMERO_FIL_DANS_CABLE = "NUMERO_FIL_DANS_CABLE";
		public static final String COULEUR_FIL = "COULEUR_FIL";
		public static final String NOM_SIGNAL = "NOM_SIGNAL";
		public static final String ORDRE_REALISATION = "ORDRE_REALISATION";
		public static final String REPERE_ELECTRIQUE_TENANT = "REPERE_ELECTRIQUE_TENANT";
		public static final String NUMERO_COMPOSANT_TENANT = "NUMERO_COMPOSANT_TENANT";
		public static final String NUMERO_BORNE_TENANT = "NUMERO_BORNE_TENANT";
		public static final String TYPE_RACCORDEMENT_TENANT = "TYPE_RACCORDEMENT_TENANT";
		public static final String REPRISE_BLINDAGE = "REPRISE_BLINDAGE";
		public static final String SANS_REPRISE_BLINDAGE = "SANS_REPRISE_BLINDAGE";
		public static final String REPERE_ELECTRIQUE_ABOUTISSANT = "REPERE_ELECTRIQUE_ABOUTISSANT";
		public static final String NUMERO_COMPOSANT_ABOUTISSANT = "NUMERO_COMPOSANT_ABOUTISSANT";
		public static final String NUMERO_BORNE_ABOUTISSANT = "NUMERO_BORNE_ABOUTISSANT";
		public static final String TYPE_RACCORDEMENT_ABOUTISSANT = "TYPE_RACCORDEMENT_ABOUTISSANT";
		public static final String TYPE_ELEMENT_RACCORDE = "TYPE_ELEMENT_RACCORDE";
		public static final String REFERENCE_FABRICANT2 = "REFERENCE_FABRICANT2";
		public static final String REFERENCE_INTERNE = "REFERENCE_INTERNE";
		public static final String FICHE_INSTRUCTION = "FICHE_INSTRUCTION";
		public static final String REFERENCE_CONFIGURATION_SERTISSAGE = "REFERENCE_CONFIGURATION_SERTISSAGE";
		public static final String ACCESSOIRE_COMPOSANT1 = "ACCESSOIRE_COMPOSANT1";
		public static final String ACCESSOIRE_COMPOSANT2 = "ACCESSOIRE_COMPOSANT2";
		public static final String REFERENCE_OUTIL_TENANT = "REFERENCE_OUTIL_TENANT";
		public static final String REFERENCE_ACCESSOIRE_OUTIL_TENANT = "REFERENCE_ACCESSOIRE_OUTIL_TENANT";
		public static final String REGLAGE_OUTIL_TENANT = "REGLAGE_OUTIL_TENANT";
		public static final String REFERENCE_OUTIL_ABOUTISSANT = "REFERENCE_OUTIL_ABOUTISSANT";
		public static final String REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT = "REFERENCE_ACCESSOIRE_OUTIL_ABOUTISSANT";
		public static final String REGLAGE_OUTIL_ABOUTISSANT = "REGLAGE_OUTIL_ABOUTISSANT";
		public static final String OBTURATEUR = "OBTURATEUR";
		public static final String FAUX_CONTACT = "FAUX_CONTACT";
		public static final String ETAT_FINALISATION_PRISE = "ETAT_FINALISATION_PRISE";
		public static final String ORIENTATION_RACCORD_ARRIERE = "ORIENTATION_RACCORD_ARRIERE";
		public static final String NUMERO_OPERATION = "NUMERO_OPERATION";
		public static final String ZONE_ACTIVITE = "ZONE_ACTIVITE";
		public static final String LOCALISATION1 = "LOCALISATION1";
		public static final String NUMERO_REPERE_TABLE_CHEMINEMENT = "NUMERO_REPERE_TABLE_CHEMINEMENT";
		public static final String NUMERO_SECTION_CHEMINEMENT = "NUMERO_SECTION_CHEMINEMENT";
		public static final String NUMERO_CHEMINEMENT = "NUMERO_CHEMINEMENT";
		public static final String NUMERO_POSITION_CHARIOT = "NUMERO_POSITION_CHARIOT";
		public static final String NUMERO_SERIE_OUTIL = "NUMERO_SERIE_OUTIL";
		public static final String NUMERO_FICHE_JALON = "NUMERO_FICHE_JALON";
		public static final String DESIGNATION = "DESIGNATION";
		public static final String NUMERO_REVISION_HARNAIS = "NUMERO_REVISION_HARNAIS";
		public static final String STANDARD = "STANDARD";
		public static final String REFERENCE_FICHIER_SOURCE = "REFERENCE_FICHIER_SOURCE";
		public static final String NUMERO_HARNAIS_FAISCEAUX = "NUMERO_HARNAIS_FAISCEAUX";
	}

}
