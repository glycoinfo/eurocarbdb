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
*   Last commit: $Rev: 1922 $ by $Author: khaleefah $ on $Date:: 2010-06-18 #$  
*/


/**
 *  eurocarb_devel.AcquisitionToPersubstitution
 *  06/10/2010 00:59:00
 * 
 */
package org.eurocarbdb.dataaccess.ms;

import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//eurocarb imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
 *  eurocarb_devel.PeakAnnotatedToIon
 *  06/03/2010 20:22:48
 * 
 */
public class PeakAnnotatedToIon implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( PeakAnnotatedToIon.class );


    private Integer peakAnnotatedToIonId;
    private Ion ion;
    private PeakAnnotated peakAnnotated;
    private Integer number;
    private Boolean gain;

    public PeakAnnotatedToIon() {
    }

    public PeakAnnotatedToIon(Integer peakAnnotatedToIonId, Integer number, Boolean gain) {
        this.peakAnnotatedToIonId = peakAnnotatedToIonId;
        this.number = number;
        this.gain = gain;
    }

    public PeakAnnotatedToIon(Integer peakAnnotatedToIonId, Ion ion, PeakAnnotated peakAnnotated, Integer number, Boolean gain) {
        this.peakAnnotatedToIonId = peakAnnotatedToIonId;
        this.ion = ion;
        this.peakAnnotated = peakAnnotated;
        this.number = number;
        this.gain = gain;
    }

    public Integer getPeakAnnotatedToIonId() {
        return peakAnnotatedToIonId;
    }

    public void setPeakAnnotatedToIonId(Integer peakAnnotatedToIonId) {
        this.peakAnnotatedToIonId = peakAnnotatedToIonId;
    }

    public Ion getIon() {
        return ion;
    }

    public void setIon(Ion ion) {
        this.ion = ion;
    }

    public PeakAnnotated getPeakAnnotated() {
        return peakAnnotated;
    }

    public void setPeakAnnotated(PeakAnnotated peakAnnotated) {
        this.peakAnnotated = peakAnnotated;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Boolean getGain() {
        return gain;
    }

    public void setGain(Boolean gain) {
        this.gain = gain;
    }

}
