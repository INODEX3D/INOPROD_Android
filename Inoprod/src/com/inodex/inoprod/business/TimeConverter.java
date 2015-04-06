package com.inodex.inoprod.business;

public class TimeConverter {
	
	public static long convert(String time) {
		try {
			long t = 0;
			int taille = time.length();
			//secondes
			t+= Integer.parseInt(time.substring(6, 8));
			//minutes
			t+= 60*Integer.parseInt(time.substring(3, 5));
			//heures
			t+= 3600*Integer.parseInt(time.substring(0, 2 ));
			return t;
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static String display(long time) {
		if (time/3600 >0 ) {
			return time/3600 + "h " + (time%3600)/60 +"min " ;
		} else if (time % 60 == 0){
			return time/60 + "min";
		} else {
			return time/60 +"min " + time%60 + "sec";
		}
	}

}
