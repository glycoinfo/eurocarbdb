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
// Generated Jun 21, 2007 2:07:02 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports
import java.util.Date;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class DigestProfile  *//**********************************************
*
*
*/ 
public class DigestProfile extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int digestProfileId;
      
    private Profile profile;
      
    private Integer digestId;
      
    private String acqSwVersion;
      
    private String operator;
      
    private Date dateAcquired;
      
    private String dextranStandard;
      
    private String sequentialDigest;
      
    private String waxDigested;
      
    private String waxUndigested;
      
    private String neutralSeparation;
      
    private String monoSeparation;
      
    private String diSeparation;
      
    private String triSeparation;
      
    private String tetraSeparation;
      
    private String userComments;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public DigestProfile() {}

    
    /** full constructor */
    public DigestProfile( Profile profile, 
                          Integer digestId, 
                          String acqSwVersion, 
                          String operator, 
                          Date dateAcquired, 
                          String dextranStandard, 
                          String sequentialDigest, 
                          String waxDigested, 
                          String waxUndigested, 
                          String neutralSeparation, 
                          String monoSeparation, 
                          String diSeparation, 
                          String triSeparation, 
                          String tetraSeparation, 
                          String userComments ) 
    {
        this.profile = profile;
        this.digestId = digestId;
        this.acqSwVersion = acqSwVersion;
        this.operator = operator;
        this.dateAcquired = dateAcquired;
        this.dextranStandard = dextranStandard;
        this.sequentialDigest = sequentialDigest;
        this.waxDigested = waxDigested;
        this.waxUndigested = waxUndigested;
        this.neutralSeparation = neutralSeparation;
        this.monoSeparation = monoSeparation;
        this.diSeparation = diSeparation;
        this.triSeparation = triSeparation;
        this.tetraSeparation = tetraSeparation;
        this.userComments = userComments;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getDigestProfileId  *//******************************** 
    *
    */ 
    public int getDigestProfileId() 
    {
        return this.digestProfileId;
    }
    
    
    /*  setDigestProfileId  *//******************************** 
    *
    */
    public void setDigestProfileId( int digestProfileId ) 
    {
        this.digestProfileId = digestProfileId;
    }
    

    /*  getProfile  *//******************************** 
    *
    */ 
    public Profile getProfile() 
    {
        return this.profile;
    }
    
    
    /*  setProfile  *//******************************** 
    *
    */
    public void setProfile( Profile profile ) 
    {
        this.profile = profile;
    }
    

    /*  getDigestId  *//******************************** 
    *
    */ 
    public Integer getDigestId() 
    {
        return this.digestId;
    }
    
    
    /*  setDigestId  *//******************************** 
    *
    */
    public void setDigestId( Integer digestId ) 
    {
        this.digestId = digestId;
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
    

    /*  getWaxDigested  *//******************************** 
    *
    */ 
    public String getWaxDigested() 
    {
        return this.waxDigested;
    }
    
    
    /*  setWaxDigested  *//******************************** 
    *
    */
    public void setWaxDigested( String waxDigested ) 
    {
        this.waxDigested = waxDigested;
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
    

    /*  getNeutralSeparation  *//******************************** 
    *
    */ 
    public String getNeutralSeparation() 
    {
        return this.neutralSeparation;
    }
    
    
    /*  setNeutralSeparation  *//******************************** 
    *
    */
    public void setNeutralSeparation( String neutralSeparation ) 
    {
        this.neutralSeparation = neutralSeparation;
    }
    

    /*  getMonoSeparation  *//******************************** 
    *
    */ 
    public String getMonoSeparation() 
    {
        return this.monoSeparation;
    }
    
    
    /*  setMonoSeparation  *//******************************** 
    *
    */
    public void setMonoSeparation( String monoSeparation ) 
    {
        this.monoSeparation = monoSeparation;
    }
    

    /*  getDiSeparation  *//******************************** 
    *
    */ 
    public String getDiSeparation() 
    {
        return this.diSeparation;
    }
    
    
    /*  setDiSeparation  *//******************************** 
    *
    */
    public void setDiSeparation( String diSeparation ) 
    {
        this.diSeparation = diSeparation;
    }
    

    /*  getTriSeparation  *//******************************** 
    *
    */ 
    public String getTriSeparation() 
    {
        return this.triSeparation;
    }
    
    
    /*  setTriSeparation  *//******************************** 
    *
    */
    public void setTriSeparation( String triSeparation ) 
    {
        this.triSeparation = triSeparation;
    }
    

    /*  getTetraSeparation  *//******************************** 
    *
    */ 
    public String getTetraSeparation() 
    {
        return this.tetraSeparation;
    }
    
    
    /*  setTetraSeparation  *//******************************** 
    *
    */
    public void setTetraSeparation( String tetraSeparation ) 
    {
        this.tetraSeparation = tetraSeparation;
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

    public static DigestProfile lookupById( int profile_id )
    {
        //log.debug("looking up profile by profileId testing area");
        Object i = getEntityManager()
                  .getQuery( "org.eurocarbdb.dataaccess.hplc.DigestProfile.BY_ID" )
                  .setParameter("profile_id", profile_id );
                  
    
        assert i instanceof DigestProfile;

        return (DigestProfile) i;
    }

    
} // end class
