package org.eurocarbdb.action.ms;
import org.eurocarbdb.application.glycoworkbench.*;

public class SimpleScan extends Scan{

	public SimpleScan(GlycanWorkspace ws) {
		super(ws);
		// TODO Auto-generated constructor stub
	}
	 public void setPeakList(PeakList _theList){
		    thePeakList=_theList;
		  }

}
