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
*   Last commit: $Rev: 1916 $ by $Author: Khalifeh $ on $Date:: 2010-06-16 #$  
*/
package org.eurocarbdb.dataaccess.ms;

import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
 *  eurocarb_devel.PeakListToDataProcessing
 *  06/03/2010 20:22:50
 * 
 */
public class PeakListToDataProcessing implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( PeakListToDataProcessing.class );


    private Integer PeakListToDataProcessingId;
    private DataProcessing dataProcessing;
    private PeakList peakList;
    private Integer softwareOrder;

    public PeakListToDataProcessing() {
    }

    public PeakListToDataProcessing(Integer PeakListToDataProcessingId, Integer softwareOrder) {
        this.PeakListToDataProcessingId = PeakListToDataProcessingId;
        this.softwareOrder = softwareOrder;
    }

    public PeakListToDataProcessing(Integer PeakListToDataProcessingId, DataProcessing dataProcessing, PeakList peakList, Integer softwareOrder) {
        this.PeakListToDataProcessingId = PeakListToDataProcessingId;
        this.dataProcessing = dataProcessing;
        this.peakList = peakList;
        this.softwareOrder = softwareOrder;
    }

    public Integer getPeakListToDataProcessingId() {
        return PeakListToDataProcessingId;
    }

    public void setPeakListToDataProcessingId(Integer PeakListToDataProcessingId) {
        this.PeakListToDataProcessingId = PeakListToDataProcessingId;
    }

    public DataProcessing getDataProcessing() {
        return dataProcessing;
    }

    public void setDataProcessing(DataProcessing dataProcessing) {
        this.dataProcessing = dataProcessing;
    }

    public PeakList getPeakList() {
        return peakList;
    }

    public void setPeakList(PeakList peakList) {
        this.peakList = peakList;
    }

    public Integer getSoftwareOrder() {
        return softwareOrder;
    }

    public void setSoftwareOrder(Integer softwareOrder) {
        this.softwareOrder = softwareOrder;
    }

}
