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
 *  eurocarb_devel.PeakAnnotatedToSmallMolecule
 *  06/03/2010 20:22:49
 * 
 */
public class PeakAnnotatedToSmallMolecule implements Serializable{
 /** Logging handle. */
    static final Logger log = Logger.getLogger( PeakAnnotatedToSmallMolecule.class );

    private Integer peakAnnotatedToSmallMoleculeId;
    private SmallMolecule smallMolecule;
    private PeakAnnotated peakAnnotated;
    private Boolean gain;
    private Integer number;

    public PeakAnnotatedToSmallMolecule() {
    }

    public PeakAnnotatedToSmallMolecule(Integer peakAnnotatedToSmallMoleculeId, Boolean gain, Integer number) {
        this.peakAnnotatedToSmallMoleculeId = peakAnnotatedToSmallMoleculeId;
        this.gain = gain;
        this.number = number;
    }

    public PeakAnnotatedToSmallMolecule(Integer peakAnnotatedToSmallMoleculeId, SmallMolecule smallMolecule, PeakAnnotated peakAnnotated, Boolean gain, Integer number) {
        this.peakAnnotatedToSmallMoleculeId = peakAnnotatedToSmallMoleculeId;
        this.smallMolecule = smallMolecule;
        this.peakAnnotated = peakAnnotated;
        this.gain = gain;
        this.number = number;
    }

    public Integer getPeakAnnotatedToSmallMoleculeId() {
        return peakAnnotatedToSmallMoleculeId;
    }

    public void setPeakAnnotatedToSmallMoleculeId(Integer peakAnnotatedToSmallMoleculeId) {
        this.peakAnnotatedToSmallMoleculeId = peakAnnotatedToSmallMoleculeId;
    }

    public SmallMolecule getSmallMolecule() {
        return smallMolecule;
    }

    public void setSmallMolecule(SmallMolecule smallMolecule) {
        this.smallMolecule = smallMolecule;
    }

    public PeakAnnotated getPeakAnnotated() {
        return peakAnnotated;
    }

    public void setPeakAnnotated(PeakAnnotated peakAnnotated) {
        this.peakAnnotated = peakAnnotated;
    }

    public Boolean getGain() {
        return gain;
    }

    public void setGain(Boolean gain) {
        this.gain = gain;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

}
