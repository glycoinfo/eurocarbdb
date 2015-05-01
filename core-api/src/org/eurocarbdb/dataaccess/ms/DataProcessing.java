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
//static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
 *  eurocarb_devel.DataProcessing
 *  06/03/2010 20:22:48
 * 
 */
public class DataProcessing implements Serializable{
/** Logging handle. */
    static final Logger log = Logger.getLogger( DataProcessing.class );


    private Integer dataProcessingId;
    private SoftwareType softwareType;
    private Software software;
    private Double intensityCutoff;
    private String format;
    private Set<org.eurocarbdb.dataaccess.ms.ScanToDataProcessing> ScanToDataProcessings = new HashSet<org.eurocarbdb.dataaccess.ms.ScanToDataProcessing>();
    private Set<org.eurocarbdb.dataaccess.ms.PeakListToDataProcessing> PeakListToDataProcessings = new HashSet<org.eurocarbdb.dataaccess.ms.PeakListToDataProcessing>();

    public DataProcessing() {
    }

    public DataProcessing(Integer dataProcessingId, Double intensityCutoff, String format) {
        this.dataProcessingId = dataProcessingId;
        this.intensityCutoff = intensityCutoff;
        this.format = format;
    }

    public DataProcessing(Integer dataProcessingId, SoftwareType softwareType, Software software, Double intensityCutoff, String format, Set<org.eurocarbdb.dataaccess.ms.ScanToDataProcessing> ScanToDataProcessings, Set<org.eurocarbdb.dataaccess.ms.PeakListToDataProcessing> PeakListToDataProcessings) {
        this.dataProcessingId = dataProcessingId;
        this.softwareType = softwareType;
        this.software = software;
        this.intensityCutoff = intensityCutoff;
        this.format = format;
        this.ScanToDataProcessings = ScanToDataProcessings;
        this.PeakListToDataProcessings = PeakListToDataProcessings;
    }

    public Integer getDataProcessingId() {
        return dataProcessingId;
    }

    public void setDataProcessingId(Integer dataProcessingId) {
        this.dataProcessingId = dataProcessingId;
    }

    public SoftwareType getSoftwareType() {
        return softwareType;
    }

    public void setSoftwareType(SoftwareType softwareType) {
        this.softwareType = softwareType;
    }

    public Software getSoftware() {
        return software;
    }

    public void setSoftware(Software software) {
        this.software = software;
    }

    public Double getIntensityCutoff() {
        return intensityCutoff;
    }

    public void setIntensityCutoff(Double intensityCutoff) {
        this.intensityCutoff = intensityCutoff;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Set<org.eurocarbdb.dataaccess.ms.ScanToDataProcessing> getScanToDataProcessings() {
        return ScanToDataProcessings;
    }

    public void setScanToDataProcessings(Set<org.eurocarbdb.dataaccess.ms.ScanToDataProcessing> ScanToDataProcessings) {
        this.ScanToDataProcessings = ScanToDataProcessings;
    }

    public Set<org.eurocarbdb.dataaccess.ms.PeakListToDataProcessing> getPeakListToDataProcessings() {
        return PeakListToDataProcessings;
    }

    public void setPeakListToDataProcessings(Set<org.eurocarbdb.dataaccess.ms.PeakListToDataProcessing> PeakListToDataProcessings) {
        this.PeakListToDataProcessings = PeakListToDataProcessings;
    }
    //Queries
    public static DataProcessing getBySoftwareTypeIdAndSoftwareId(int softwareTypeId, int softwareId)
    {
    	return (DataProcessing) getEntityManager().getQuery("org.eurocarbdb.dataaccess.ms.DataProcessing.GET_BY_swTypeID_swID")
    	                                          .setParameter("softwareTypeId", softwareTypeId) 
    	                                          .setParameter("softwareId", softwareId)
    	                                          .uniqueResult();
    	
    }

}
