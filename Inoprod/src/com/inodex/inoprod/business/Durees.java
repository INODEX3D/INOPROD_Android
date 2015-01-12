package com.inodex.inoprod.business;

import android.provider.BaseColumns;

public class Durees {
	
	public Durees() {}
	
	public static final class Duree implements BaseColumns {
		public static final String _id = "_id";
		public static final String CODE_OPERATION = "CODE_OPERATION";
		public static final String DESIGNATION_OPERATION = "DESIGNATION_OPERATION";
		public static final String DUREE_THEORIQUE = "DUREE_THEORIQUE";
		public static final String UNITE = "UNITE";
		public static final String OPERATION_SOUS_CONTROLE = "OPERATION_SOUS_CONTROLE";
	}

}
