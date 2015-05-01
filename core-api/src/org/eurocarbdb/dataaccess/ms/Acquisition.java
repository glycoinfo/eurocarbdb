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
*   Last commit: $Rev: 1980 $ by $Author: khaleefah $ on $Date:: 2010-08-26 #$  
*/

package org.eurocarbdb.dataaccess.ms;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Criteria;
import org.hibernate.criterion.*;

//  eurocarb imports
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.Contributed;
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import org.eurocarbdb.dataaccess.core.Evidence;
import org.eurocarbdb.dataaccess.core.Contributor;

//  static imports
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


//EuroCarb imports




/**
 *  eurocarb_devel.Acquisition
 *  06/10/2010 00:59:00
 * 
 */
public class Acquisition extends Evidence implements Serializable{

    static final Logger log = Logger.getLogger( Acquisition.class );

   // private Integer acquisitionId;
    private Device device;
   // private Integer evidenceId;
    private String filename;
    private String filetype;
    private Date dateObtained;
    private Double contributorQuality;
    private Set<org.eurocarbdb.dataaccess.ms.AcquisitionToPersubstitution> AcquisitionToPersubstitutions = new HashSet<org.eurocarbdb.dataaccess.ms.AcquisitionToPersubstitution>();
    private Set<org.eurocarbdb.dataaccess.ms.Scan> scans = new HashSet<org.eurocarbdb.dataaccess.ms.Scan>();
    private Set<org.eurocarbdb.dataaccess.ms.DeviceSettings> deviceSettingses = new HashSet<org.eurocarbdb.dataaccess.ms.DeviceSettings>();

    public Acquisition() {
        setEvidenceType( Evidence.Type.MS );
    }

    public Acquisition(Device device,String filename, String filetype, Date dateObtained, Double contributorQuality) {
        this();
        this.device = device;
        this.filename = filename;
        this.filetype = filetype;
        this.dateObtained = dateObtained;
        this.contributorQuality = contributorQuality;
    }

    public Acquisition(Integer acquisitionId, Device device, Integer evidenceId, String filename, String filetype, Date dateObtained, Double contributorQuality, Set<org.eurocarbdb.dataaccess.ms.AcquisitionToPersubstitution> AcquisitionToPersubstitutions, Set<org.eurocarbdb.dataaccess.ms.Scan> scans, Set<org.eurocarbdb.dataaccess.ms.DeviceSettings> deviceSettingses) {
      //  this.acquisitionId = acquisitionId;
        this.device = device;
     //   this.evidenceId = evidenceId;
        this.filename = filename;
        this.filetype = filetype;
        this.dateObtained = dateObtained;
        this.contributorQuality = contributorQuality;
        this.AcquisitionToPersubstitutions = AcquisitionToPersubstitutions;
        this.scans = scans;
        this.deviceSettingses = deviceSettingses;
    }

    public Integer getAcquisitionId() {
        return getEvidenceId();
    }

   
    public void setAquesitionEvidenceId(Integer id)
    {
    	this.setEvidenceId(id);
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

/*    public Integer getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(Integer evidenceId) {
        this.evidenceId = evidenceId;
    }*/

/** 
    *   Always returns {@link Evidence.Type.MS}. 
    *   @see Evidence.Type
    */
    public Type getEvidenceType()
    {
        return Evidence.Type.MS;  
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public Date getDateObtained() {
        return dateObtained;
    }

    public void setDateObtained(Date dateObtained) {
        this.dateObtained = dateObtained;
    }

    public Double getContributorQuality() {
        return contributorQuality;
    }

    public void setContributorQuality(Double contributorQuality) {
        this.contributorQuality = contributorQuality;
    }

    public Set<org.eurocarbdb.dataaccess.ms.AcquisitionToPersubstitution> getAcquisitionToPersubstitutions() {
        return AcquisitionToPersubstitutions;
    }

    public void setAcquisitionToPersubstitutions(Set<org.eurocarbdb.dataaccess.ms.AcquisitionToPersubstitution> AcquisitionToPersubstitutions) {
        this.AcquisitionToPersubstitutions = AcquisitionToPersubstitutions;
    }

    public Set<org.eurocarbdb.dataaccess.ms.Scan> getScans() {
        return scans;
    }

    public void setScans(Set<org.eurocarbdb.dataaccess.ms.Scan> scans) {
        this.scans = scans;
    }

    public Set<org.eurocarbdb.dataaccess.ms.DeviceSettings> getDeviceSettingses() {
        return deviceSettingses;
    }

    public void setDeviceSettingses(Set<org.eurocarbdb.dataaccess.ms.DeviceSettings> deviceSettingses) {
        this.deviceSettingses = deviceSettingses;
    }
    //Quiries
    //Author: Khalifeh Al-Jadda
    
    /**
    Get top level scans
 */
 public Set<Scan> getRootScans() 
 {
     HashSet<Scan> ret = new HashSet<Scan>();
     for( Scan s : getScans() ) 
     {
         if( s.getParentScan() == null )
             ret.add(s);
     }
     return ret;
 }

    
    public static Acquisition lookupById( int id )
    {
        log.debug("looking up acquisition by acquisitionId");
        Object i = getEntityManager()
                  .getQuery( "org.eurocarbdb.dataaccess.ms.Acquisition.BY_ID" )
                  .setParameter("acquisitionId", id )
                  .uniqueResult();

        assert i instanceof Acquisition;
        
        return (Acquisition) i;
    }
    
    @SuppressWarnings("unchecked")
	public static List<Acquisition> ownedAcquisitions()
    {
    	Contributor c = Contributor.getCurrentContributor();
    	List <Acquisition> list = (List<Acquisition>)getEntityManager()
    	                         .getQuery("org.eurocarbdb.dataaccess.ms.Acquisition.OwnedAcquisitions")
    	                         .setParameter("contributorName",c.getContributorName())
    	                         .list();
    	if(list == null)
    		return emptyList();
    	
    	return list;
    	                         
    }
    
    @SuppressWarnings("unchecked")
	public static List<Acquisition> othersAcquisitions()
    {
    	Contributor c = Contributor.getCurrentContributor();
    	List <Acquisition>list = (List<Acquisition>)getEntityManager()
    	                         .getQuery("org.eurocarbdb.dataaccess.ms.Acquisition.OthersAcquisitions")
    	                         .setParameter("contributorName",c.getContributorName())
    	                         .list();
    	if(list == null)
    		return emptyList();
    	
    	return list;
    	                         
    }



}
