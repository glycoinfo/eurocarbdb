package org.eurocarbdb.util.glycomedb;

import java.util.HashMap;

public class CrossReference {
	private Integer ID = null;
	private boolean used =false;
	private HashMap<Integer,CrossReferenceToStructure> seqeunces = new HashMap<Integer, CrossReferenceToStructure>();
	
	public Integer getID() {
		return ID;
	}
	public void setID(Integer iD) {
		ID = iD;
	}
	public boolean isUsed() {
		return used;
	}
	public void setUsed(boolean used) {
		this.used = used;
	}
	public HashMap<Integer, CrossReferenceToStructure> getSeqeunces() {
		return seqeunces;
	}
	public void setSeqeunces(HashMap<Integer, CrossReferenceToStructure> seqeunces) {
		this.seqeunces = seqeunces;
	}
	
	
}
