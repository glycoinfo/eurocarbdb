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
 *  eurocarb_devel.ScanToDataProcessing
 *  06/03/2010 20:22:49
 * 
 */
public class ScanToDataProcessing implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( Acquisition.class );

    private Integer ScanToDataProcessingId;
    private Scan scan;
    private DataProcessing dataProcessing;
    private Boolean spotIntegration;
    private Integer softwareOrder;

    public ScanToDataProcessing() {
    }

    public ScanToDataProcessing(Integer ScanToDataProcessingId, Boolean spotIntegration, Integer softwareOrder) {
        this.ScanToDataProcessingId = ScanToDataProcessingId;
        this.spotIntegration = spotIntegration;
        this.softwareOrder = softwareOrder;
    }

    public ScanToDataProcessing(Integer ScanToDataProcessingId, Scan scan, DataProcessing dataProcessing, Boolean spotIntegration, Integer softwareOrder) {
        this.ScanToDataProcessingId = ScanToDataProcessingId;
        this.scan = scan;
        this.dataProcessing = dataProcessing;
        this.spotIntegration = spotIntegration;
        this.softwareOrder = softwareOrder;
    }

    public Integer getScanToDataProcessingId() {
        return ScanToDataProcessingId;
    }

    public void setScanToDataProcessingId(Integer ScanToDataProcessingId) {
        this.ScanToDataProcessingId = ScanToDataProcessingId;
    }

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public DataProcessing getDataProcessing() {
        return dataProcessing;
    }

    public void setDataProcessing(DataProcessing dataProcessing) {
        this.dataProcessing = dataProcessing;
    }

    public Boolean getSpotIntegration() {
        return spotIntegration;
    }

    public void setSpotIntegration(Boolean spotIntegration) {
        this.spotIntegration = spotIntegration;
    }

    public Integer getSoftwareOrder() {
        return softwareOrder;
    }

    public void setSoftwareOrder(Integer softwareOrder) {
        this.softwareOrder = softwareOrder;
    }

}
