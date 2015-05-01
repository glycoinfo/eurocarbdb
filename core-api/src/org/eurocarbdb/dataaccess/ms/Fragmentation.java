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
 *  eurocarb_devel.Fragmentation
 *  06/03/2010 20:22:49
 * 
 */
public class Fragmentation implements Serializable {

    /** Logging handle. */
    static final Logger log = Logger.getLogger( Fragmentation.class );


    private Integer fragmentationId;
    private PeakAnnotated peakAnnotated;
    private String fragmentType;
    private String fragmentDc;
    private String fragmentAlt;
    private Integer fragmentPosition;
    private Integer cleavageOne;
    private Integer cleavageTwo;

    public Fragmentation() {
    }

    public Fragmentation(Integer fragmentationId, String fragmentType, String fragmentDc, String fragmentAlt, Integer fragmentPosition, Integer cleavageOne, Integer cleavageTwo) {
        this.fragmentationId = fragmentationId;
        this.fragmentType = fragmentType;
        this.fragmentDc = fragmentDc;
        this.fragmentAlt = fragmentAlt;
        this.fragmentPosition = fragmentPosition;
        this.cleavageOne = cleavageOne;
        this.cleavageTwo = cleavageTwo;
    }

    public Fragmentation(Integer fragmentationId, PeakAnnotated peakAnnotated, String fragmentType, String fragmentDc, String fragmentAlt, Integer fragmentPosition, Integer cleavageOne, Integer cleavageTwo) {
        this.fragmentationId = fragmentationId;
        this.peakAnnotated = peakAnnotated;
        this.fragmentType = fragmentType;
        this.fragmentDc = fragmentDc;
        this.fragmentAlt = fragmentAlt;
        this.fragmentPosition = fragmentPosition;
        this.cleavageOne = cleavageOne;
        this.cleavageTwo = cleavageTwo;
    }

    public Integer getFragmentationId() {
        return fragmentationId;
    }

    public void setFragmentationId(Integer fragmentationId) {
        this.fragmentationId = fragmentationId;
    }

    public PeakAnnotated getPeakAnnotated() {
        return peakAnnotated;
    }

    public void setPeakAnnotated(PeakAnnotated peakAnnotated) {
        this.peakAnnotated = peakAnnotated;
    }

    public String getFragmentType() {
        return fragmentType;
    }

    public void setFragmentType(String fragmentType) {
        this.fragmentType = fragmentType;
    }

    public String getFragmentDc() {
        return fragmentDc;
    }

    public void setFragmentDc(String fragmentDc) {
        this.fragmentDc = fragmentDc;
    }

    public String getFragmentAlt() {
        return fragmentAlt;
    }

    public void setFragmentAlt(String fragmentAlt) {
        this.fragmentAlt = fragmentAlt;
    }

    public Integer getFragmentPosition() {
        return fragmentPosition;
    }

    public void setFragmentPosition(Integer fragmentPosition) {
        this.fragmentPosition = fragmentPosition;
    }

    public Integer getCleavageOne() {
        return cleavageOne;
    }

    public void setCleavageOne(Integer cleavageOne) {
        this.cleavageOne = cleavageOne;
    }

    public Integer getCleavageTwo() {
        return cleavageTwo;
    }

    public void setCleavageTwo(Integer cleavageTwo) {
        this.cleavageTwo = cleavageTwo;
    }

}
