package org.eurocarbdb.application.glycanbuilder;

public enum GlycanFormat {
	GWS("gws"),
	GW_LINUCS("gwlinucs"),
	GLYCOMINDS("glycominds"),
	GLYCOCT("glycoct"),
	GLYCOCT_CONDENSED("glycoct_condensed");
	
	
	String format;
	GlycanFormat(String _format){
		format=_format;
	}
	
	public String toString(){
		return format;
	}
}
