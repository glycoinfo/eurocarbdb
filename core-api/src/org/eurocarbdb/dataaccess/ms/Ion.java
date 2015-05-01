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


/**
 *  eurocarb_devel.AcquisitionToPersubstitution
 *  06/10/2010 00:59:00
 * 
 */
package org.eurocarbdb.dataaccess.ms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//eurocarb imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
 *  eurocarb_devel.Ion
 *  06/03/2010 20:22:49
 * 
 */
public class Ion implements Serializable{
/** Logging handle. */
    static final Logger log = Logger.getLogger( Ion.class );

    private Integer ionId;
    private String ionType;
    private Integer charge;
    private Boolean positive;
    private Boolean atomer;
    private Set<org.eurocarbdb.dataaccess.ms.IonComposition> ionCompositions = new HashSet<org.eurocarbdb.dataaccess.ms.IonComposition>();
    private Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToIon> peakAnnotatedToIons = new HashSet<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToIon>();

    public Ion() {
    }

    public Ion(Integer ionId, String ionType, Integer charge, Boolean positive, Boolean atomer) {
        this.ionId = ionId;
        this.ionType = ionType;
        this.charge = charge;
        this.positive = positive;
        this.atomer = atomer;
    }

    public Ion(Integer ionId, String ionType, Integer charge, Boolean positive, Boolean atomer, Set<org.eurocarbdb.dataaccess.ms.IonComposition> ionCompositions, Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToIon> peakAnnotatedToIons) {
        this.ionId = ionId;
        this.ionType = ionType;
        this.charge = charge;
        this.positive = positive;
        this.atomer = atomer;
        this.ionCompositions = ionCompositions;
        this.peakAnnotatedToIons = peakAnnotatedToIons;
    }

    public Integer getIonId() {
        return ionId;
    }

    public void setIonId(Integer ionId) {
        this.ionId = ionId;
    }

    public String getIonType() {
        return ionType;
    }

    public void setIonType(String ionType) {
        this.ionType = ionType;
    }

    public Integer getCharge() {
        return charge;
    }

    public void setCharge(Integer charge) {
        this.charge = charge;
    }

    public Boolean getPositive() {
        return positive;
    }

    public void setPositive(Boolean positive) {
        this.positive = positive;
    }

    public Boolean getAtomer() {
        return atomer;
    }

    public void setAtomer(Boolean atomer) {
        this.atomer = atomer;
    }

    public Set<org.eurocarbdb.dataaccess.ms.IonComposition> getIonCompositions() {
        return ionCompositions;
    }

    public void setIonCompositions(Set<org.eurocarbdb.dataaccess.ms.IonComposition> ionCompositions) {
        this.ionCompositions = ionCompositions;
    }

    public Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToIon> getPeakAnnotatedToIons() {
        return peakAnnotatedToIons;
    }

    public void setPeakAnnotatedToIons(Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToIon> peakAnnotatedToIons) {
        this.peakAnnotatedToIons = peakAnnotatedToIons;
    }
    //Queries
    public static Ion checkExistance(String type, int charge, boolean positive)
    {
    	return (Ion) getEntityManager().getQuery("org.eurocarbdb.dataaccess.ms.Ion.BY_TYPE_CHARGE_POSITIVE")
    	                               .setParameter("type",type)
    	                               .setParameter("charge",charge)
    	                               .setParameter("positive",positive)
    	                               .uniqueResult();
    }
    @SuppressWarnings("unchecked")
	public static List<Ion> getAll()
    {
    	List<Ion> temp = (List<Ion>) getEntityManager().getQuery("org.eurocarbdb.dataaccess.ms.Ion.GET_ALL")
        .list();
    	return temp;
    }

}
