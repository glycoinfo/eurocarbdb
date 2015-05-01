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
*   Last commit: $Rev: 1940 $ by $Author: khaleefah $ on $Date:: 2010-08-10 #$  
*/
package org.eurocarbdb.dataaccess.ms;

import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import static java.util.Collections.emptyList;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
 *  eurocarb_devel.PeakProcessing
 *  06/03/2010 20:22:49
 * 
 */
public class PeakProcessing implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( PeakProcessing.class );

    private Integer peakProcessingId;
    private String peakProcessingType;
    private Set<org.eurocarbdb.dataaccess.ms.PeakList> peakLists = new HashSet<org.eurocarbdb.dataaccess.ms.PeakList>();

    public PeakProcessing() {
    }

    public PeakProcessing(Integer peakProcessingId, String peakProcessingType) {
        this.peakProcessingId = peakProcessingId;
        this.peakProcessingType = peakProcessingType;
    }

    public PeakProcessing(Integer peakProcessingId, String peakProcessingType, Set<org.eurocarbdb.dataaccess.ms.PeakList> peakLists) {
        this.peakProcessingId = peakProcessingId;
        this.peakProcessingType = peakProcessingType;
        this.peakLists = peakLists;
    }

    public Integer getPeakProcessingId() {
        return peakProcessingId;
    }

    public void setPeakProcessingId(Integer peakProcessingId) {
        this.peakProcessingId = peakProcessingId;
    }

    public String getPeakProcessingType() {
        return peakProcessingType;
    }

    public void setPeakProcessingType(String peakProcessingType) {
        this.peakProcessingType = peakProcessingType;
    }

    public Set<org.eurocarbdb.dataaccess.ms.PeakList> getPeakLists() {
        return peakLists;
    }

    public void setPeakLists(Set<org.eurocarbdb.dataaccess.ms.PeakList> peakLists) {
        this.peakLists = peakLists;
    }
//Queries
    public static List<String> getAllPeakProcessingTypes()
    {
      List<String> list = (List<String>) getEntityManager()
    	                         .getQuery("org.eurocarbdb.dataaccess.ms.PeakProcessing.GET_ALL_TYPES")
    	                         .list();
      if(list == null)
    		return emptyList();
    	
    	return list;
    }
    public static PeakProcessing getPeakProcessingByType(String type)
    {
    	return (PeakProcessing)getEntityManager()
        .getQuery("org.eurocarbdb.dataaccess.ms.PeakProcessing.BY_TYPE")
        .setParameter("type", type)
        .uniqueResult();
    	
    }
    	             
}
