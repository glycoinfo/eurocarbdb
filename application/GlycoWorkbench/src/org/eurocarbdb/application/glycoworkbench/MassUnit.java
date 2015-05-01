package org.eurocarbdb.application.glycoworkbench;

public enum MassUnit {
	PPM(),
	Da();
	
	public static MassUnit valueOfCompat(String value){
		if(value.equals("ppm")){
			return MassUnit.PPM;
		}else{
			return MassUnit.valueOf(value);
		}
	}
	
}
