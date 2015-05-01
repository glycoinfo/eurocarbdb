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


/**
 *  eurocarb_devel.AcquisitionToPersubstitution
 *  06/10/2010 00:59:00
 * 
 */
package org.eurocarbdb.dataaccess.ms;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;
import org.eurocarbdb.dataaccess.Eurocarb;

//  eurocarb imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
 *  eurocarb_devel.PeakList
 *  06/03/2010 20:22:50
 * 
 */
public class PeakList implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( PeakList.class );



    private Integer peakListId;
    private PeakProcessing peakProcessing;
    private Scan scan;
    private Date dateEntered;
    private Boolean deisotoped;
    private Boolean chargeDeconvoluted;
    private Double basePeakMz;
    private Double basePeakIntensity;
    private Double lowMz;
    private Double highMz;
    private Integer contributorId;
    private Double contributorQuality;
    private Set<org.eurocarbdb.dataaccess.ms.PeakLabeled> peakLabeleds = new HashSet<org.eurocarbdb.dataaccess.ms.PeakLabeled>();
    private Set<org.eurocarbdb.dataaccess.ms.PeakListToDataProcessing> PeakListToDataProcessings = new HashSet<org.eurocarbdb.dataaccess.ms.PeakListToDataProcessing>();

    public PeakList() {
    }

    public PeakList(Integer peakListId, Date dateEntered, Boolean deisotoped, Boolean chargeDeconvoluted, Double basePeakMz, Double basePeakIntensity, Double lowMz, Double highMz, Integer contributorId, Double contributorQuality) {
        this.peakListId = peakListId;
        this.dateEntered = dateEntered;
        this.deisotoped = deisotoped;
        this.chargeDeconvoluted = chargeDeconvoluted;
        this.basePeakMz = basePeakMz;
        this.basePeakIntensity = basePeakIntensity;
        this.lowMz = lowMz;
        this.highMz = highMz;
        this.contributorId = contributorId;
        this.contributorQuality = contributorQuality;
    }

    public PeakList(Integer peakListId, PeakProcessing peakProcessing, Scan scan, Date dateEntered, Boolean deisotoped, Boolean chargeDeconvoluted, Double basePeakMz, Double basePeakIntensity, Double lowMz, Double highMz, Integer contributorId, Double contributorQuality, Set<org.eurocarbdb.dataaccess.ms.PeakLabeled> peakLabeleds, Set<org.eurocarbdb.dataaccess.ms.PeakListToDataProcessing> PeakListToDataProcessings) {
        this.peakListId = peakListId;
        this.peakProcessing = peakProcessing;
        this.scan = scan;
        this.dateEntered = dateEntered;
        this.deisotoped = deisotoped;
        this.chargeDeconvoluted = chargeDeconvoluted;
        this.basePeakMz = basePeakMz;
        this.basePeakIntensity = basePeakIntensity;
        this.lowMz = lowMz;
        this.highMz = highMz;
        this.contributorId = contributorId;
        this.contributorQuality = contributorQuality;
        this.peakLabeleds = peakLabeleds;
        this.PeakListToDataProcessings = PeakListToDataProcessings;
    }

    public Integer getPeakListId() {
        return peakListId;
    }

    public void setPeakListId(Integer peakListId) {
        this.peakListId = peakListId;
    }

    public PeakProcessing getPeakProcessing() {
        return peakProcessing;
    }

    public void setPeakProcessing(PeakProcessing peakProcessing) {
        this.peakProcessing = peakProcessing;
    }

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public Date getDateEntered() {
        return dateEntered;
    }

    public void setDateEntered(Date dateEntered) {
        this.dateEntered = dateEntered;
    }

    public Boolean getDeisotoped() {
        return deisotoped;
    }

    public void setDeisotoped(Boolean deisotoped) {
        this.deisotoped = deisotoped;
    }

    public Boolean getChargeDeconvoluted() {
        return chargeDeconvoluted;
    }

    public void setChargeDeconvoluted(Boolean chargeDeconvoluted) {
        this.chargeDeconvoluted = chargeDeconvoluted;
    }

    public Double getBasePeakMz() {
        return basePeakMz;
    }

    public void setBasePeakMz(Double basePeakMz) {
        this.basePeakMz = basePeakMz;
    }

    public Double getBasePeakIntensity() {
        return basePeakIntensity;
    }

    public void setBasePeakIntensity(Double basePeakIntensity) {
        this.basePeakIntensity = basePeakIntensity;
    }

    public Double getLowMz() {
        return lowMz;
    }

    public void setLowMz(Double lowMz) {
        this.lowMz = lowMz;
    }

    public Double getHighMz() {
        return highMz;
    }

    public void setHighMz(Double highMz) {
        this.highMz = highMz;
    }

    public Integer getContributorId() {
        return contributorId;
    }

    public void setContributorId(Integer contributorId) {
        this.contributorId = contributorId;
    }

    public Double getContributorQuality() {
        return contributorQuality;
    }

    public void setContributorQuality(Double contributorQuality) {
        this.contributorQuality = contributorQuality;
    }

    public Set<org.eurocarbdb.dataaccess.ms.PeakLabeled> getPeakLabeleds() {
        return peakLabeleds;
    }

    public void setPeakLabeleds(Set<org.eurocarbdb.dataaccess.ms.PeakLabeled> peakLabeleds) {
        this.peakLabeleds = peakLabeleds;
    }

    public Set<org.eurocarbdb.dataaccess.ms.PeakListToDataProcessing> getPeakListToDataProcessings() {
        return PeakListToDataProcessings;
    }

    public void setPeakListToDataProcessings(Set<org.eurocarbdb.dataaccess.ms.PeakListToDataProcessing> PeakListToDataProcessings) {
        this.PeakListToDataProcessings = PeakListToDataProcessings;
    }
    @SuppressWarnings("unchecked")
	public static List<Object> getPeaklistsAcquisitions()
    {
    	return (List<Object>)Eurocarb.getEntityManager()
    	                             .getQuery("org.eurocarbdb.dataaccess.ms.PeakList.GET_AQUISITIONS_AND_PEAKLISTS")
    	                             .list();
    }

}
