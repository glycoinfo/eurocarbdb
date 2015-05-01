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
package org.eurocarbdb.dataaccess.ms;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.io.Serializable;
import java.text.*;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
 *  eurocarb_devel.Scan
 *  06/10/2010 00:59:00
 * 
 */
public class Scan implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( Scan.class );

    private Integer scanId;
    private Acquisition acquisition;
    private Integer msExponent;
    private Boolean polarity;
    private Double startMz;
    private Double endMz;
    private Double contributorQuality;
    private Integer originalScanId;
    private Set<org.eurocarbdb.dataaccess.ms.SumAverageRelationship> sumAverageRelationshipsForScanId = new HashSet<org.eurocarbdb.dataaccess.ms.SumAverageRelationship>();
    private Set<org.eurocarbdb.dataaccess.ms.PeakList> peakLists = new HashSet<org.eurocarbdb.dataaccess.ms.PeakList>();
    private Set<org.eurocarbdb.dataaccess.ms.MsMsRelationship> msMsRelationshipsForParentId = new HashSet<org.eurocarbdb.dataaccess.ms.MsMsRelationship>();
    private Set<org.eurocarbdb.dataaccess.ms.ScanToDataProcessing> ScanToDataProcessings = new HashSet<org.eurocarbdb.dataaccess.ms.ScanToDataProcessing>();
    private Set<org.eurocarbdb.dataaccess.ms.SumAverageRelationship> sumAverageRelationshipsForSubsetScanId = new HashSet<org.eurocarbdb.dataaccess.ms.SumAverageRelationship>();
    private Set<org.eurocarbdb.dataaccess.ms.ScanImage> scanImages = new HashSet<org.eurocarbdb.dataaccess.ms.ScanImage>();
    private Set<org.eurocarbdb.dataaccess.ms.MsMsRelationship> msMsRelationshipsForScanId = new HashSet<org.eurocarbdb.dataaccess.ms.MsMsRelationship>();
    //private List<Scan> childScans = null;

    public Scan() {
    }

    public Scan(Integer scanId, Integer msExponent, Boolean polarity, Double startMz, Double endMz, Double contributorQuality, Integer originalScanId) {
        this.scanId = scanId;
        this.msExponent = msExponent;
        this.polarity = polarity;
        this.startMz = startMz;
        this.endMz = endMz;
        this.contributorQuality = contributorQuality;
        this.originalScanId = originalScanId;
    }

    public Scan(Integer scanId, Acquisition acquisition, Integer msExponent, Boolean polarity, Double startMz, Double endMz, Double contributorQuality, Integer originalScanId, Set<org.eurocarbdb.dataaccess.ms.SumAverageRelationship> sumAverageRelationshipsForScanId, Set<org.eurocarbdb.dataaccess.ms.PeakList> peakLists, Set<org.eurocarbdb.dataaccess.ms.MsMsRelationship> msMsRelationshipsForParentId, Set<org.eurocarbdb.dataaccess.ms.ScanToDataProcessing> ScanToDataProcessings, Set<org.eurocarbdb.dataaccess.ms.SumAverageRelationship> sumAverageRelationshipsForSubsetScanId, Set<org.eurocarbdb.dataaccess.ms.ScanImage> scanImages, Set<org.eurocarbdb.dataaccess.ms.MsMsRelationship> msMsRelationshipsForScanId) {
        this.scanId = scanId;
        this.acquisition = acquisition;
        this.msExponent = msExponent;
        this.polarity = polarity;
        this.startMz = startMz;
        this.endMz = endMz;
        this.contributorQuality = contributorQuality;
        this.originalScanId = originalScanId;
        this.sumAverageRelationshipsForScanId = sumAverageRelationshipsForScanId;
        this.peakLists = peakLists;
        this.msMsRelationshipsForParentId = msMsRelationshipsForParentId;
        this.ScanToDataProcessings = ScanToDataProcessings;
        this.sumAverageRelationshipsForSubsetScanId = sumAverageRelationshipsForSubsetScanId;
        this.scanImages = scanImages;
        this.msMsRelationshipsForScanId = msMsRelationshipsForScanId;
    }

    public Integer getScanId() {
        return scanId;
    }

    public void setScanId(Integer scanId) {
        this.scanId = scanId;
    }

    public Acquisition getAcquisition() {
        return acquisition;
    }

    public void setAcquisition(Acquisition acquisition) {
        this.acquisition = acquisition;
    }

    public Integer getMsExponent() {
        return msExponent;
    }

    public void setMsExponent(Integer msExponent) {
        this.msExponent = msExponent;
    }

    public Boolean getPolarity() {
        return polarity;
    }

    public void setPolarity(Boolean polarity) {
        this.polarity = polarity;
    }

    public Double getStartMz() {
        return startMz;
    }

    public void setStartMz(Double startMz) {
        this.startMz = startMz;
    }

    public Double getEndMz() {
        return endMz;
    }

    public void setEndMz(Double endMz) {
        this.endMz = endMz;
    }

    public Double getContributorQuality() {
        return contributorQuality;
    }

    public void setContributorQuality(Double contributorQuality) {
        this.contributorQuality = contributorQuality;
    }

    public Integer getOriginalScanId() {
        return originalScanId;
    }

    public void setOriginalScanId(Integer originalScanId) {
        this.originalScanId = originalScanId;
    }

    public Set<org.eurocarbdb.dataaccess.ms.SumAverageRelationship> getSumAverageRelationshipsForScanId() {
        return sumAverageRelationshipsForScanId;
    }

    public void setSumAverageRelationshipsForScanId(Set<org.eurocarbdb.dataaccess.ms.SumAverageRelationship> sumAverageRelationshipsForScanId) {
        this.sumAverageRelationshipsForScanId = sumAverageRelationshipsForScanId;
    }

    public Set<org.eurocarbdb.dataaccess.ms.PeakList> getPeakLists() {
        return peakLists;
    }

    public void setPeakLists(Set<org.eurocarbdb.dataaccess.ms.PeakList> peakLists) {
        this.peakLists = peakLists;
    }

    public Set<org.eurocarbdb.dataaccess.ms.MsMsRelationship> getMsMsRelationshipsForParentId() {
        return msMsRelationshipsForParentId;
    }

    public void setMsMsRelationshipsForParentId(Set<org.eurocarbdb.dataaccess.ms.MsMsRelationship> msMsRelationshipsForParentId) {
        this.msMsRelationshipsForParentId = msMsRelationshipsForParentId;
    }

    public Set<org.eurocarbdb.dataaccess.ms.ScanToDataProcessing> getScanToDataProcessings() {
        return ScanToDataProcessings;
    }

    public void setScanToDataProcessings(Set<org.eurocarbdb.dataaccess.ms.ScanToDataProcessing> ScanToDataProcessings) {
        this.ScanToDataProcessings = ScanToDataProcessings;
    }

    public Set<org.eurocarbdb.dataaccess.ms.SumAverageRelationship> getSumAverageRelationshipsForSubsetScanId() {
        return sumAverageRelationshipsForSubsetScanId;
    }

    public void setSumAverageRelationshipsForSubsetScanId(Set<org.eurocarbdb.dataaccess.ms.SumAverageRelationship> sumAverageRelationshipsForSubsetScanId) {
        this.sumAverageRelationshipsForSubsetScanId = sumAverageRelationshipsForSubsetScanId;
    }

    public Set<org.eurocarbdb.dataaccess.ms.ScanImage> getScanImages() {
        return scanImages;
    }

    public void setScanImages(Set<org.eurocarbdb.dataaccess.ms.ScanImage> scanImages) {
        this.scanImages = scanImages;
    }

    public Set<org.eurocarbdb.dataaccess.ms.MsMsRelationship> getMsMsRelationshipsForScanId() {
        return msMsRelationshipsForScanId;
    }
    public void setMsMsRelationshipsForScanId(Set<org.eurocarbdb.dataaccess.ms.MsMsRelationship> msMsRelationshipsForScanId) {
        this.msMsRelationshipsForScanId = msMsRelationshipsForScanId;
    }

    
    
    public Scan getParentScan() {
        /*HashSet<Scan> ret = new HashSet<Scan>();
        for( MsMsRelationship mmr : getMsMsRelationshipsWithParents() )
        ret.add(mmr.getParentScan());
        return ret;*/
//        for( MsMsRelationship mmr : getMsMsRelationshipsForScanId())
//            return mmr.getScanByParentId(); // return first match
//        return null;
    	 if(MsMsRelationship.getParentByScanId(this.scanId) != null)
    		 return MsMsRelationship.getParentByScanId(this.scanId).getScanByParentId();
    	 return null;
        }
    public List<Scan> getChildScans()
    {
    	return MsMsRelationship.getByParentId(this.scanId);
    }
    
    public static Scan getScanByOriginalId(int originalScanId,int acquisitionId)
    {
    	return (Scan)getEntityManager()
        .getQuery("org.eurocarbdb.dataaccess.ms.Scan.GET_SCAN_BY_ORIGINAL_ID")
        .setParameter("originalScanId", originalScanId)
        .setParameter("acquisitionId", acquisitionId)
        .uniqueResult();
    }
    
    public static Scan getScanById(int scanId)
    {
    	return (Scan)getEntityManager()
        .getQuery("org.eurocarbdb.dataaccess.ms.Scan.GET_SCAN_BY_ID")
        .setParameter("scanId", scanId)
        .uniqueResult();
    }
    
    @SuppressWarnings("unchecked")
	public static List<Scan> getAllScans(int acquisitionId)
    {
    	return (List<Scan>)getEntityManager()
        .getQuery("org.eurocarbdb.dataaccess.ms.Scan.GET_ALL")
        .setParameter("acquisitionId", acquisitionId)
        .list();
    }
    public String test()
    {
    	return "Can Access Scan";
    }
    public Long peaksNum(String contributorName,Date dateEntered) throws ParseException
    {
//    	String contributorName = "admin";
//    	String dateEntered = "08/26/10"; 
//    	DateFormat df = new SimpleDateFormat("mm/dd/yy");
    	Long temp = null;
//    	temp = PeakLabeled.getNumberOfPeaks(this.scanId,df.parse(dateEntered),contributorName);
    	temp = PeakLabeled.getNumberOfPeaks(this.scanId,dateEntered,contributorName);
    	System.out.println("temp: " + temp);
    	if(temp == null)
    		return 0l;
    	else
    	return temp;
    	
    }
    
//    public Long getPeaksNum()
//    {
//    	
//    	
//    	try {
//			return PeakLabeled.getNumberOfPeaks(this.scanId,df.parse(dateEntered),contributorName);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return 0l;
//    }
    
    public Double getPrecursorMz()
    {
    	return MsMsRelationship.getPrecursorByScanId(this.scanId);
    }
   

}
