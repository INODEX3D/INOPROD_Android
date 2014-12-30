package com.v1.inoprod.business;

import android.provider.BaseColumns;

public class Outillage {
	
	public Outillage() {}
	
	public static final class Outil implements BaseColumns {
		public static final String _id = "_id";
		public static final String IDENTIFICATION = "IDENTIFICATION";
		public static final String INTITULE = "INTITULE";
		public static final String CONSTRUCTEUR = "CONSTRUCTEUR";
		public static final String TYPE = "TYPE";
		public static final String CODE_BARRE = "CODE_BARRE";
		public static final String NUMERO_SERIE = "NUMERO_SERIE";
		public static final String PROPRIETAIRE = "PROPRIETAIRE";
		public static final String SECTION = "SECTION";
		public static final String AFFECTATION = "AFFECTATION";
		public static final String PERIODE = "PERIODE";
		public static final String UNITE = "UNITE";
		public static final String DERNIERE_OPERATION = "DERNIERE_OPERATION";
		public static final String PROCHAINE_OPERATION = "PROCHAINE_OPERATION";
		public static final String COMMENTAIRES = "COMMENTAIRES";
	}

}
