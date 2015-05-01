package org.eurocarbdb.util.glycomedb;

public class CrossReferenceToStructure {
	private boolean used = false;
	private Integer id = null;
	public boolean isUsed() {
		return used;
	}
	public void setUsed(boolean used) {
		this.used = used;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
}
