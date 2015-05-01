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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
// Generated Apr 18, 2007 5:02:25 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import org.eurocarbdb.dataaccess.core.Experiment;
import org.eurocarbdb.dataaccess.core.ExperimentStep;
import org.eurocarbdb.dataaccess.core.Evidence;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Profile  *//**********************************************
*
*
*/ 
public class Profile extends Evidence implements Serializable 
                         //extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int profileId;
      
    private Profile parentProfile;
      
    private Detector detector;
      
    private Column column;
      
    private Instrument instrument;
      
    private String acqSwVersion;
      
    private String operator;
      
    private Date dateAcquired;
      
    private String dextranStandard;
      
    private String sequentialDigest;
      
    private String userComments;
      
    private String waxUndigested;
      
    private Set<Profile> childProfiles = new HashSet<Profile>(0);
      
    private Set<HplcPeaksIntegrated> hplcPeaksIntegrateds = new HashSet<HplcPeaksIntegrated>(0);
      
    private Set<DigestProfile> digestProfiles = new HashSet<DigestProfile>(0);
      
    private Set<IntegrationMethod> integrationMethods = new HashSet<IntegrationMethod>(0);
      
    private Set<ProfileData> profileDatas = new HashSet<ProfileData>(0);
      
    private Set<HplcPeaksAnnotated> hplcPeaksAnnotateds = new HashSet<HplcPeaksAnnotated>(0);

    private Set contents = new HashSet();

    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public Profile() {
    setEvidenceType( Evidence.Type.HPLC );
    }

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /** 
    *   Always returns {@link Evidence.Type.HPLC}. 
    *   @see Evidence.Type
    */
    public Type getEvidenceType()
    {
        return Evidence.Type.HPLC;   
    }

    /*  getProfileId  *//******************************** 
    *
    */ 
    public int getProfileId() 
    {
        return this.profileId;
    }
    
    
    /*  setProfileId  *//******************************** 
    *
    */
    public void setProfileId( int profileId ) 
    {
        this.profileId = profileId;
    }
    

    /*  getProfile  *//******************************** 
    *
    */ 
    public Profile getParentProfile() 
    {
        return this.parentProfile;
    }
    
    
    /*  setProfile  *//******************************** 
    *
    */
    public void setParentProfile( Profile parentProfile ) 
    {
        this.parentProfile = parentProfile;
    }
    

    /*  getDetector  *//******************************** 
    *
    */ 
    public Detector getDetector() 
    {
        return this.detector;
    }
    
    
    /*  setDetector  *//******************************** 
    *
    */
    public void setDetector( Detector detector ) 
    {
        this.detector = detector;
    }
    

    /*  getColumn  *//******************************** 
    *
    */ 
    public Column getColumn() 
    {
        return this.column;
    }
    
    
    /*  setColumn  *//******************************** 
    *
    */
    public void setColumn( Column column ) 
    {
        this.column = column;
    }
    

    /*  getInstrument  *//******************************** 
    *
    */ 
    public Instrument getInstrument() 
    {
        return this.instrument;
    }
    
    
    /*  setInstrument  *//******************************** 
    *
    */
    public void setInstrument( Instrument instrument ) 
    {
        this.instrument = instrument;
    }
    

    /*  getAcqSwVersion  *//******************************** 
    *
    */ 
    public String getAcqSwVersion() 
    {
        return this.acqSwVersion;
    }
    
    
    /*  setAcqSwVersion  *//******************************** 
    *
    */
    public void setAcqSwVersion( String acqSwVersion ) 
    {
        this.acqSwVersion = acqSwVersion;
    }
    

    /*  getOperator  *//******************************** 
    *
    */ 
    public String getOperator() 
    {
        return this.operator;
    }
    
    
    /*  setOperator  *//******************************** 
    *
    */
    public void setOperator( String operator ) 
    {
        this.operator = operator;
    }
    

    /*  getDateAcquired  *//******************************** 
    *
    */ 
    public Date getDateAcquired() 
    {
        return this.dateAcquired;
    }
    
    
    /*  setDateAcquired  *//******************************** 
    *
    */
    public void setDateAcquired( Date dateAcquired ) 
    {
        this.dateAcquired = dateAcquired;
    }
    

    /*  getDextranStandard  *//******************************** 
    *
    */ 
    public String getDextranStandard() 
    {
        return this.dextranStandard;
    }
    
    
    /*  setDextranStandard  *//******************************** 
    *
    */
    public void setDextranStandard( String dextranStandard ) 
    {
        this.dextranStandard = dextranStandard;
    }
    

    /*  getSequentialDigest  *//******************************** 
    *
    */ 
    public String getSequentialDigest() 
    {
        return this.sequentialDigest;
    }
    
    
    /*  setSequentialDigest  *//******************************** 
    *
    */
    public void setSequentialDigest( String sequentialDigest ) 
    {
        this.sequentialDigest = sequentialDigest;
    }
    

    /*  getUserComments  *//******************************** 
    *
    */ 
    public String getUserComments() 
    {
        return this.userComments;
    }
    
    
    /*  setUserComments  *//******************************** 
    *
    */
    public void setUserComments( String userComments ) 
    {
        this.userComments = userComments;
    }
    

    /*  getWaxUndigested  *//******************************** 
    *
    */ 
    public String getWaxUndigested() 
    {
        return this.waxUndigested;
    }
    
    
    /*  setWaxUndigested  *//******************************** 
    *
    */
    public void setWaxUndigested( String waxUndigested ) 
    {
        this.waxUndigested = waxUndigested;
    }
    

    /*  getChildProfiles  *//******************************** 
    *
    */ 
    public Set<Profile> getChildProfiles() 
    {
        return this.childProfiles;
    }
    
    
    /*  setChildProfiles  *//******************************** 
    *
    */
    public void setChildProfiles( Set<Profile> childProfiles ) 
    {
        this.childProfiles = childProfiles;
    }
    

    /*  getHplcPeaksIntegrateds  *//******************************** 
    *
    */ 
    public Set<HplcPeaksIntegrated> getHplcPeaksIntegrateds() 
    {
        return this.hplcPeaksIntegrateds;
    }
    
    
    /*  setHplcPeaksIntegrateds  *//******************************** 
    *
    */
    public void setHplcPeaksIntegrateds( Set<HplcPeaksIntegrated> hplcPeaksIntegrateds ) 
    {
        this.hplcPeaksIntegrateds = hplcPeaksIntegrateds;
    }
    

    /*  getDigestProfiles  *//******************************** 
    *
    */ 
    public Set<DigestProfile> getDigestProfiles() 
    {
        return this.digestProfiles;
    }
    
    
    /*  setDigestProfiles  *//******************************** 
    *
    */
    public void setDigestProfiles( Set<DigestProfile> digestProfiles ) 
    {
        this.digestProfiles = digestProfiles;
    }
    

    /*  getIntegrationMethods  *//******************************** 
    *
    */ 
    public Set<IntegrationMethod> getIntegrationMethods() 
    {
        return this.integrationMethods;
    }
    
    
    /*  setIntegrationMethods  *//******************************** 
    *
    */
    public void setIntegrationMethods( Set<IntegrationMethod> integrationMethods ) 
    {
        this.integrationMethods = integrationMethods;
    }
    

    /*  getProfileDatas  *//******************************** 
    *
    */ 
    public Set<ProfileData> getProfileDatas() 
    {
        return this.profileDatas;
    }
    
    
    /*  setProfileDatas  *//******************************** 
    *
    */
    public void setProfileDatas( Set<ProfileData> profileDatas ) 
    {
        this.profileDatas = profileDatas;
    }
    

    /*  getHplcPeaksAnnotateds  *//******************************** 
    *
    */ 
    public Set<HplcPeaksAnnotated> getHplcPeaksAnnotateds() 
    {
        return this.hplcPeaksAnnotateds;
    }
    
    
    /*  setHplcPeaksAnnotateds  *//******************************** 
    *
    */
    public void setHplcPeaksAnnotateds( Set<HplcPeaksAnnotated> hplcPeaksAnnotateds ) 
    {
        this.hplcPeaksAnnotateds = hplcPeaksAnnotateds;
    }
    
    public Set<Content> getContents()
    {
    return this.contents;
    }

    public void setContents(Set<Profile> contents)
    {
    this.contents = contents;
    }

    
    public static Profile lookupById( int id )
 
    {
        //log.debug("looking up profile by profileId");
        Object i = getEntityManager()
                  .getQuery( "org.eurocarbdb.dataaccess.hplc.Profile.BY_ID" )
                  .setParameter("profileId", id )
                  .uniqueResult();

        assert i instanceof Profile;
        
        return (Profile) i;
    }

     public static Profile test( int id )
     {
     //log.debug("looking up profile by profileId");
        Object test = getEntityManager()
                  .getQuery( "org.eurocarbdb.dataaccess.hplc.Profile.SELECT_ALL" )
                  .setParameter("profileId", id )
                  .uniqueResult();

        assert test instanceof Profile;

       return (Profile) test;
       }

      public static Profile lookupByEvidence (int id)
      {
    Object i = getEntityManager()
           .getQuery( "org.eurocarbdb.dataaccess.hplc.Profile.BY_EVIDENCE")
           .setParameter("evidId", id)
           .uniqueResult();
                
    assert i instanceof Profile;
        
    return (Profile) i;
      }    








} // end class
