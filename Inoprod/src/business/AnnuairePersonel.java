package business;

import android.provider.BaseColumns;


public class AnnuairePersonel  {
	
	public AnnuairePersonel() {
		
	}
	
	public static final class Employe implements BaseColumns {
		
		private Employe() {}
		
		public static final String EMPLOYE_NOM = "EMPLOYE_NOM";
		public static final String EMPLOYE_PRENOM = "EMPLOYE_PRENOM"; 
		public static final String EMPLOYE_METIER = "EMPLOYE_METIER"; 

		
	}
	
	
	

}
