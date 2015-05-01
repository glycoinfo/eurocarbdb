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
*   Last commit: $Rev: 1947 $ by $Author: khaleefah $ on $Date:: 2010-08-18 #$  
*/


/**
 *  eurocarb_devel.AcquisitionToPersubstitution
 *  06/10/2010 00:59:00
 * 
 */
package org.eurocarbdb.dataaccess.ms;

import java.io.Serializable;
import java.util.List;
//  3rd party imports
import org.apache.log4j.Logger;

//eurocarb
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
 *  eurocarb_devel.MsMsRelationship
 *  06/03/2010 20:22:49
 * 
 */
public class MsMsRelationship implements Serializable{
  /** Logging handle. */
    static final Logger log = Logger.getLogger( MsMsRelationship.class );


    private Integer msMsRelationshipId;
    private org.eurocarbdb.dataaccess.ms.Scan scanByScanId;
    private org.eurocarbdb.dataaccess.ms.Scan scanByParentId;
    private Double precursorMz;
    private Double precursorIntensity;
    private Double precursorMassWindowLow;
    private Double precursorMassWindowHigh;
    private Integer precursorCharge;
    private String msMsMethode;

    public MsMsRelationship() {
    }

    public MsMsRelationship(Integer msMsRelationshipId, Double precursorMz, Double precursorIntensity, Double precursorMassWindowLow, Double precursorMassWindowHigh, Integer precursorCharge, String msMsMethode) {
        this.msMsRelationshipId = msMsRelationshipId;
        this.precursorMz = precursorMz;
        this.precursorIntensity = precursorIntensity;
        this.precursorMassWindowLow = precursorMassWindowLow;
        this.precursorMassWindowHigh = precursorMassWindowHigh;
        this.precursorCharge = precursorCharge;
        this.msMsMethode = msMsMethode;
    }

    public MsMsRelationship(Integer msMsRelationshipId, org.eurocarbdb.dataaccess.ms.Scan scanByScanId, org.eurocarbdb.dataaccess.ms.Scan scanByParentId, Double precursorMz, Double precursorIntensity, Double precursorMassWindowLow, Double precursorMassWindowHigh, Integer precursorCharge, String msMsMethode) {
        this.msMsRelationshipId = msMsRelationshipId;
        this.scanByScanId = scanByScanId;
        this.scanByParentId = scanByParentId;
        this.precursorMz = precursorMz;
        this.precursorIntensity = precursorIntensity;
        this.precursorMassWindowLow = precursorMassWindowLow;
        this.precursorMassWindowHigh = precursorMassWindowHigh;
        this.precursorCharge = precursorCharge;
        this.msMsMethode = msMsMethode;
    }

    public Integer getMsMsRelationshipId() {
        return msMsRelationshipId;
    }

    public void setMsMsRelationshipId(Integer msMsRelationshipId) {
        this.msMsRelationshipId = msMsRelationshipId;
    }

    public org.eurocarbdb.dataaccess.ms.Scan getScanByScanId() {
        return scanByScanId;
    }

    public void setScanByScanId(org.eurocarbdb.dataaccess.ms.Scan scanByScanId) {
        this.scanByScanId = scanByScanId;
    }

    public org.eurocarbdb.dataaccess.ms.Scan getScanByParentId() {
        return scanByParentId;
    }

    public void setScanByParentId(org.eurocarbdb.dataaccess.ms.Scan scanByParentId) {
        this.scanByParentId = scanByParentId;
    }

    public Double getPrecursorMz() {
        return precursorMz;
    }

    public void setPrecursorMz(Double precursorMz) {
        this.precursorMz = precursorMz;
    }

    public Double getPrecursorIntensity() {
        return precursorIntensity;
    }

    public void setPrecursorIntensity(Double precursorIntensity) {
        this.precursorIntensity = precursorIntensity;
    }

    public Double getPrecursorMassWindowLow() {
        return precursorMassWindowLow;
    }

    public void setPrecursorMassWindowLow(Double precursorMassWindowLow) {
        this.precursorMassWindowLow = precursorMassWindowLow;
    }

    public Double getPrecursorMassWindowHigh() {
        return precursorMassWindowHigh;
    }

    public void setPrecursorMassWindowHigh(Double precursorMassWindowHigh) {
        this.precursorMassWindowHigh = precursorMassWindowHigh;
    }

    public Integer getPrecursorCharge() {
        return precursorCharge;
    }

    public void setPrecursorCharge(Integer precursorCharge) {
        this.precursorCharge = precursorCharge;
    }

    public String getMsMsMethode() {
        return msMsMethode;
    }

    public void setMsMsMethode(String msMsMethode) {
        this.msMsMethode = msMsMethode;
    }
    public static MsMsRelationship getParentByScanId(int scanId)
    {
    	MsMsRelationship temp = (MsMsRelationship) getEntityManager()
        .getQuery("org.eurocarbdb.dataaccess.ms.MsMsRelationship.GET_BY_SCAN_ID")
        .setParameter("scanId", scanId)
        .uniqueResult();
    	return temp;
    }
    @SuppressWarnings("unchecked")
	public static List<Scan> getByParentId(int parentId)
    {
    	return (List<Scan>) getEntityManager()
        .getQuery("org.eurocarbdb.dataaccess.ms.MsMsRelationship.GET_BY_PARENT_ID")
        .setParameter("parentId", parentId)
        .list();
    	
    }
    public static double getPrecursorByScanId(int scanId)
    {
    	MsMsRelationship temp = (MsMsRelationship) getEntityManager()
        .getQuery("org.eurocarbdb.dataaccess.ms.MsMsRelationship.GET_BY_SCAN_ID")
        .setParameter("scanId", scanId)
        .uniqueResult();
    	return temp.getPrecursorMz();
    }

}
