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
import java.util.Set;
import java.io.Serializable;
import java.util.List;

//  3rd party imports
import org.apache.log4j.Logger;

//eurpcarb imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
 *  eurocarb_devel.PeakAnnotated
 *  06/10/2010 00:58:58
 * 
 */
public class PeakAnnotated implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( PeakAnnotated.class );

    private Integer peakAnnotatedId;
    private PeakLabeled peakLabeled;
    private ReducingEnd reducingEnd;
    private Persubstitution persubstitution;
    private Integer glycoCtId;
    private String sequenceGws;
    private String formula;
    private Double calculatedMass;
    private Double contributorQuality;
    private Date dateEntered;
    private Integer contributorId;
    private Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToSmallMolecule> peakAnnotatedToSmallMolecules = new HashSet<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToSmallMolecule>();
    private Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToIon> peakAnnotatedToIons = new HashSet<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToIon>();
    private Set<org.eurocarbdb.dataaccess.ms.Fragmentation> fragmentations = new HashSet<org.eurocarbdb.dataaccess.ms.Fragmentation>();

    public PeakAnnotated() {
    }

    public PeakAnnotated(Integer peakAnnotatedId, Integer glycoCtId, String sequenceGws, String formula, Double calculatedMass, Double contributorQuality, Date dateEntered, Integer contributorId) {
        this.peakAnnotatedId = peakAnnotatedId;
        this.glycoCtId = glycoCtId;
        this.sequenceGws = sequenceGws;
        this.formula = formula;
        this.calculatedMass = calculatedMass;
        this.contributorQuality = contributorQuality;
        this.dateEntered = dateEntered;
        this.contributorId = contributorId;
    }

    public PeakAnnotated(Integer peakAnnotatedId, PeakLabeled peakLabeled, ReducingEnd reducingEnd, Persubstitution persubstitution, Integer glycoCtId, String sequenceGws, String formula, Double calculatedMass, Double contributorQuality, Date dateEntered, Integer contributorId, Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToSmallMolecule> peakAnnotatedToSmallMolecules, Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToIon> peakAnnotatedToIons, Set<org.eurocarbdb.dataaccess.ms.Fragmentation> fragmentations) {
        this.peakAnnotatedId = peakAnnotatedId;
        this.peakLabeled = peakLabeled;
        this.reducingEnd = reducingEnd;
        this.persubstitution = persubstitution;
        this.glycoCtId = glycoCtId;
        this.sequenceGws = sequenceGws;
        this.formula = formula;
        this.calculatedMass = calculatedMass;
        this.contributorQuality = contributorQuality;
        this.dateEntered = dateEntered;
        this.contributorId = contributorId;
        this.peakAnnotatedToSmallMolecules = peakAnnotatedToSmallMolecules;
        this.peakAnnotatedToIons = peakAnnotatedToIons;
        this.fragmentations = fragmentations;
    }

    public Integer getPeakAnnotatedId() {
        return peakAnnotatedId;
    }

    public void setPeakAnnotatedId(Integer peakAnnotatedId) {
        this.peakAnnotatedId = peakAnnotatedId;
    }

    public PeakLabeled getPeakLabeled() {
        return peakLabeled;
    }

    public void setPeakLabeled(PeakLabeled peakLabeled) {
        this.peakLabeled = peakLabeled;
    }

    public ReducingEnd getReducingEnd() {
        return reducingEnd;
    }

    public void setReducingEnd(ReducingEnd reducingEnd) {
        this.reducingEnd = reducingEnd;
    }

    public Persubstitution getPersubstitution() {
        return persubstitution;
    }

    public void setPersubstitution(Persubstitution persubstitution) {
        this.persubstitution = persubstitution;
    }

    public Integer getGlycoCtId() {
        return glycoCtId;
    }

    public void setGlycoCtId(Integer glycoCtId) {
        this.glycoCtId = glycoCtId;
    }

    public String getSequenceGws() {
        return sequenceGws;
    }

    public void setSequenceGws(String sequenceGws) {
        this.sequenceGws = sequenceGws;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public Double getCalculatedMass() {
        return calculatedMass;
    }

    public void setCalculatedMass(Double calculatedMass) {
        this.calculatedMass = calculatedMass;
    }

    public Double getContributorQuality() {
        return contributorQuality;
    }

    public void setContributorQuality(Double contributorQuality) {
        this.contributorQuality = contributorQuality;
    }

    public Date getDateEntered() {
        return dateEntered;
    }

    public void setDateEntered(Date dateEntered) {
        this.dateEntered = dateEntered;
    }

    public Integer getContributorId() {
        return contributorId;
    }

    public void setContributorId(Integer contributorId) {
        this.contributorId = contributorId;
    }

    public Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToSmallMolecule> getPeakAnnotatedToSmallMolecules() {
        return peakAnnotatedToSmallMolecules;
    }

    public void setPeakAnnotatedToSmallMolecules(Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToSmallMolecule> peakAnnotatedToSmallMolecules) {
        this.peakAnnotatedToSmallMolecules = peakAnnotatedToSmallMolecules;
    }

    public Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToIon> getPeakAnnotatedToIons() {
        return peakAnnotatedToIons;
    }

    public void setPeakAnnotatedToIons(Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToIon> peakAnnotatedToIons) {
        this.peakAnnotatedToIons = peakAnnotatedToIons;
    }

    public Set<org.eurocarbdb.dataaccess.ms.Fragmentation> getFragmentations() {
        return fragmentations;
    }

    public void setFragmentations(Set<org.eurocarbdb.dataaccess.ms.Fragmentation> fragmentations) {
        this.fragmentations = fragmentations;
    }
    @SuppressWarnings("unchecked")
	public static List<Object> getScanAnnotations(int scanId)
    {
    	return (List<Object>)getEntityManager()
        .getQuery("org.eurocarbdb.dataaccess.ms.PeakAnnotated.GET_SCAN_ANNOTATIONS")
        .setParameter("scanId", scanId)
        .list();
    }
    
    @SuppressWarnings("unchecked")
	public static List<PeakAnnotated> getScanPeakAnnotateds(int scanId, Date dateEntered, String contributorName)
    {
//    	System.out.println("The peak annotateds list with date " + dateEntered);
//    	System.out.println(getEntityManager()
//        .getQuery("org.eurocarbdb.dataaccess.ms.PeakAnnotated.GET_SCAN_PeakAnnotateds_Date")
//        .setParameter("scanId", scanId)
//        .setParameter("dateEntered",dateEntered)
//        .setParameter("contributorName", contributorName).getQueryString());
    	
    	return (List<PeakAnnotated>)getEntityManager()
        .getQuery("org.eurocarbdb.dataaccess.ms.PeakAnnotated.GET_SCAN_PeakAnnotateds")
        .setParameter("scanId", scanId)
        .setParameter("contributorName", contributorName)
        .list();
    	
    }
   

}
