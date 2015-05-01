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
*   Last commit: $Rev: 1549 $ by $Author: glycoslave $ on $Date:: 2009-07-19 #$  
*/

package org.eurocarbdb.action.hplc;

import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.core.Reference;

//3rd party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 

/**
*   @author     hirenj
*   @version    $Rev: 1549 $
*/
public class DetectorHplc extends EurocarbAction 
{

    /** Logging handle. */
    protected static final Log log = LogFactory.getLog( DetectorHplc.class );
   

    private Long detectorId;
    private String manufacturer;
    private String model;
    private String excitation;
    private String emission;
    private float bandwidth;
    private String samplingRate;



    public float getBandwidth() {
        return bandwidth;
    }


    public void setBandwidth(float bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Long getDetectorId() {
        return detectorId;
    }


    public void setDetectorId(Long detectorId) {
        this.detectorId = detectorId;
    }

    public String getEmission() {
        return emission;
    }

    public void setEmission(String emission) {
        this.emission = emission;
    }

    public String getExcitation() {
        return excitation;
    }


    public void setExcitation(String excitation) {
        this.excitation = excitation;
    }


    public String getManufacturer() {

        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(String samplingRate) {
        this.samplingRate = samplingRate;
    }

    
    public String execute() {
        
        if (this.getManufacturer().equals("hello")) {
            return SUCCESS; 
        } else {
        return INPUT;
         }
}
}
