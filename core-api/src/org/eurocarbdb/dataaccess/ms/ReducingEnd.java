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
import java.util.Set;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
 *  eurocarb_devel.ReducingEnd
 *  06/03/2010 20:22:50
 * 
 */
public class ReducingEnd implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( ReducingEnd.class );


    private Integer reducingEndId;
    private String abbreviation;
    private String name;
    private String uri;
    private Set<org.eurocarbdb.dataaccess.ms.PeakAnnotated> peakAnnotateds = new HashSet<org.eurocarbdb.dataaccess.ms.PeakAnnotated>();

    public ReducingEnd() {
    }

    public ReducingEnd(Integer reducingEndId, String abbreviation, String name, String uri) {
        this.reducingEndId = reducingEndId;
        this.abbreviation = abbreviation;
        this.name = name;
        this.uri = uri;
    }

    public ReducingEnd(Integer reducingEndId, String abbreviation, String name, String uri, Set<org.eurocarbdb.dataaccess.ms.PeakAnnotated> peakAnnotateds) {
        this.reducingEndId = reducingEndId;
        this.abbreviation = abbreviation;
        this.name = name;
        this.uri = uri;
        this.peakAnnotateds = peakAnnotateds;
    }

    public Integer getReducingEndId() {
        return reducingEndId;
    }

    public void setReducingEndId(Integer reducingEndId) {
        this.reducingEndId = reducingEndId;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Set<org.eurocarbdb.dataaccess.ms.PeakAnnotated> getPeakAnnotateds() {
        return peakAnnotateds;
    }

    public void setPeakAnnotateds(Set<org.eurocarbdb.dataaccess.ms.PeakAnnotated> peakAnnotateds) {
        this.peakAnnotateds = peakAnnotateds;
    }
    public static ReducingEnd getReducingEndByAbbr(String abb)
    {
    	return (ReducingEnd)getEntityManager().getQuery("org.eurocarbdb.dataaccess.ms.ReducingEnd.BY_ABBREVIATION")
    	                                      .setParameter("abbreviation", abb)
    	                                      .uniqueResult();
    }

}
