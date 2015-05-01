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
*   Last commit: $Rev: 1995 $ by $Author: khaleefah $ on $Date:: 2010-10-27 #$  
*/


/**
 *  eurocarb_devel.AcquisitionToPersubstitution
 *  06/10/2010 00:59:00
 * 
 */
package org.eurocarbdb.dataaccess.ms;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
 *  eurocarb_devel.PeakLabeled
 *  06/03/2010 20:22:49
 * 
 */
public class PeakLabeled implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( PeakLabeled.class );

    private Integer peakLabeledId;
    private PeakList peakList;
    private Double mzValue;
    private Double intensityValue;
    private Boolean monoisotopic;
    private Integer chargeCount;
    private Double fwhm;
    private Double signalToNoise;
    private Set<org.eurocarbdb.dataaccess.ms.PeakAnnotated> peakAnnotateds = new HashSet<org.eurocarbdb.dataaccess.ms.PeakAnnotated>();

    public PeakLabeled() {
    }

    public PeakLabeled(Integer peakLabeledId, Double mzValue, Double intensityValue, Boolean monoisotopic, Integer chargeCount, Double fwhm, Double signalToNoise) {
        this.peakLabeledId = peakLabeledId;
        this.mzValue = mzValue;
        this.intensityValue = intensityValue;
        this.monoisotopic = monoisotopic;
        this.chargeCount = chargeCount;
        this.fwhm = fwhm;
        this.signalToNoise = signalToNoise;
    }

    public PeakLabeled(Integer peakLabeledId, PeakList peakList, Double mzValue, Double intensityValue, Boolean monoisotopic, Integer chargeCount, Double fwhm, Double signalToNoise, Set<org.eurocarbdb.dataaccess.ms.PeakAnnotated> peakAnnotateds) {
        this.peakLabeledId = peakLabeledId;
        this.peakList = peakList;
        this.mzValue = mzValue;
        this.intensityValue = intensityValue;
        this.monoisotopic = monoisotopic;
        this.chargeCount = chargeCount;
        this.fwhm = fwhm;
        this.signalToNoise = signalToNoise;
        this.peakAnnotateds = peakAnnotateds;
    }

    public Integer getPeakLabeledId() {
        return peakLabeledId;
    }

    public void setPeakLabeledId(Integer peakLabeledId) {
        this.peakLabeledId = peakLabeledId;
    }

    public PeakList getPeakList() {
        return peakList;
    }

    public void setPeakList(PeakList peakList) {
        this.peakList = peakList;
    }

    public Double getMzValue() {
        return mzValue;
    }

    public void setMzValue(Double mzValue) {
        this.mzValue = mzValue;
    }

    public Double getIntensityValue() {
        return intensityValue;
    }

    public void setIntensityValue(Double intensityValue) {
        this.intensityValue = intensityValue;
    }

    public Boolean getMonoisotopic() {
        return monoisotopic;
    }

    public void setMonoisotopic(Boolean monoisotopic) {
        this.monoisotopic = monoisotopic;
    }

    public Integer getChargeCount() {
        return chargeCount;
    }

    public void setChargeCount(Integer chargeCount) {
        this.chargeCount = chargeCount;
    }

    public Double getFwhm() {
        return fwhm;
    }

    public void setFwhm(Double fwhm) {
        this.fwhm = fwhm;
    }

    public Double getSignalToNoise() {
        return signalToNoise;
    }

    public void setSignalToNoise(Double signalToNoise) {
        this.signalToNoise = signalToNoise;
    }

    public Set<org.eurocarbdb.dataaccess.ms.PeakAnnotated> getPeakAnnotateds() {
        return peakAnnotateds;
    }

    public void setPeakAnnotateds(Set<org.eurocarbdb.dataaccess.ms.PeakAnnotated> peakAnnotateds) {
        this.peakAnnotateds = peakAnnotateds;
    }
    @SuppressWarnings("unchecked")
	public static Long getNumberOfPeaks(int scanId,Date dateEntered,String contributorName)
    {
//    	 getEntityManager()
//        .getQuery("org.eurocarbdb.dataaccess.ms.PeakLabeled.GET_Number_OF_PEAKS")
//        .setParameter("scanId", scanId)
//        .setParameter("dateEntered",dateEntered)
//        .setParameter("contributorName", contributorName).list().size();
    	Iterator<Long> it  = (Iterator<Long>) getEntityManager()
        .getQuery("org.eurocarbdb.dataaccess.ms.PeakLabeled.GET_Number_OF_PEAKS")
        .setParameter("scanId", scanId)
        .setParameter("dateEntered",dateEntered)
        .setParameter("contributorName", contributorName).iterate();
    	if(it.hasNext())
    		return it.next();
    	else
    		return null;
        
    }

}
