package com.inodex.inoprod.business;

import android.provider.BaseColumns;

public class TableChariots {
	
	public TableChariots() {} 
	
	public static final class Chariot implements BaseColumns {
		
		public static final String _id = "_id";
		public static final String NUMERO_CHARIOT = "NUMERO_CHARIOT";
		public static final String FACE_CHARIOT = "FACE_CHARIOT";
		public static final String POSITION_NUMERO = "POSITION_NUMERO";
		public static final String CODE_TAG = "CODE_TAG";
		public static final String CONNECTEUR_POSITIONNE = "CONNECTEUR_POSITIONNE";
		
	}

}
