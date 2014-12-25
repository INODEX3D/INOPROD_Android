package com.v1.inoprod.business;

import android.provider.BaseColumns;


public class AnnuairePersonel  {
	
	public AnnuairePersonel() {
		
	}
	
	public static final class Employe implements BaseColumns {
		
		
		
		public static final String _id = "_id";
		public static final String EMPLOYE_NOM = "EMPLOYE_NOM";
		public static final String EMPLOYE_PRENOM = "EMPLOYE_PRENOM"; 
		public static final String EMPLOYE_METIER = "EMPLOYE_METIER"; 
		
		private Employe() {}

		
	}
	
	
	

}
