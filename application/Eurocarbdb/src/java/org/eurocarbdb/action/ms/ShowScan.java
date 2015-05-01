/*
*   EuroCarbDB, a framework for carbohydrate bioinformatics
*
*   Copyright (c) 2006-2009, Eurocarb project, or third-party contributors as
*   indicated by the @author tags or express copyright attribution
*   statements applied by the authors.  
*
*   This copyrighted material is made available to anyone wishing to use, modify,
*   copy, or redistribute it subject to the terms and conditions of the GNU
*   Lesser General Public License, as published by the Free Software Foundation.
*   A copy of this license accompanies this distribution in the file LICENSE.txt.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
*   or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
*   for more details.
*
*   Last commit: $Rev: 1995 $ by $Author: khaleefah $ on $Date:: 2010-10-27 #$  
*/
/**
* $Id: ShowScan.java 1995 2010-10-26 20:06:16Z khaleefah $
* Last changed $Author: khaleefah $
* EUROCarbDB Project
*/

package org.eurocarbdb.action.ms;

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
* @author                 Khalifeh AlJadda
* @version                $Rev: 1995 $
*/

public class ShowScan extends EurocarbAction {

    protected static final Logger log = Logger.getLogger( ShowScan.class.getName() );

    private Scan scan = null;

    private int scan_id = -1;
    private Date dateEntered;
    private String contributorName = null;
    private PeakAnnotated annotation = null;
    private int annotation_index = -1;
    private List<PeakAnnotated> peakAnnotateds = null;
    
    public Scan getScan() {      
        return scan;  
    }

    public void setScan(Scan s) {      
        scan = s;  
    }

    public int getScanId() {
        return scan_id;
    }

    public void setScanId(int _id) {
        this.scan_id = _id;
    }

    public PeakAnnotated getAnnotation() {
        return annotation;
    }

    public void setAnnotation(PeakAnnotated annotation) {
        this.annotation = annotation;
    }

//    public void setParameters(Map params)
//    {   
//
//        annotation = getObjectFromParams(PeakAnnotated.class, params, "annotation.peakAnnotatedId");
//        
//        if (params.get("scan.scanId") == null && params.get("scanId") != null) {
//            params.put("scan.scanId",params.get("scanId"));
//        }
//
//        if ( annotation == null ) {
// //           scan = getObjectFromParams(Scan.class, params);
//        }
//                        
//        super.setParameters(params);
//    }

    public String execute() throws Exception {
    	System.out.println("Date entered: " + dateEntered);
    	Date a = new Date();
    	
    	
    	Scan temp = Scan.getScanById(this.scan_id);
    	setScan(temp);
    	setPeakAnnotateds(PeakAnnotated.getScanPeakAnnotateds(scan_id, null, contributorName));
    	System.out.println("Peak Annotateds size: " + peakAnnotateds.size());
    	filterPeakAnnotateds();
    	System.out.println("Peak Annotateds size after filtering: " + peakAnnotateds.size());
 
        return SUCCESS;
    }
    public void filterPeakAnnotateds()
    {
    	for(int i=0;i<peakAnnotateds.size();i++)
    		if(!peakAnnotateds.get(i).getPeakLabeled().getPeakList().getDateEntered().equals(dateEntered))
    			peakAnnotateds.remove(i);
    }

	public void setDateEntered(String dateEntered) throws ParseException {
		Date d;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		d =  df.parse(dateEntered);
		//items[i][1] = d.getTime();
		System.out.println("d.getTime = " + d.getTime());
		System.out.println("in set dateEntered");
		//long test = 
		
		//this.dateEntered = new Date(d.getTime());
		this.dateEntered = df.parse(dateEntered);
	}

	public Date getDateEntered() {
		return dateEntered;
	}

	public void setContributorName(String contributorName) {
		this.contributorName = contributorName;
	}

	public String getContributorName() {
		return contributorName;
	}

	public void setPeakAnnotateds(List<PeakAnnotated> peakAnnotateds) {
		this.peakAnnotateds = peakAnnotateds;
	}

	public List<PeakAnnotated> getPeakAnnotateds() {
		return peakAnnotateds;
	}

}
