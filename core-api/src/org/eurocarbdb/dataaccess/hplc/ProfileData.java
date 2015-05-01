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
// Generated Jun 21, 2007 2:07:00 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class ProfileData  *//**********************************************
*
*
*/ 
public class ProfileData extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int profileDataId;
      
    private Profile profile;
      
    private Digest digest;
      
    private Double XCoord;
      
    private Double YCoord;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public ProfileData() {}

    /** Minimal constructor */
    public ProfileData( Profile profile, Digest digest ) 
    {
        this.profile = profile;
        this.digest = digest;
    }
    
    /** full constructor */
    public ProfileData( Profile profile, Digest digest, Double XCoord, Double YCoord ) 
    {
        this.profile = profile;
        this.digest = digest;
        this.XCoord = XCoord;
        this.YCoord = YCoord;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getProfileDataId  *//******************************** 
    *
    */ 
    public int getProfileDataId() 
    {
        return this.profileDataId;
    }
    
    
    /*  setProfileDataId  *//******************************** 
    *
    */
    public void setProfileDataId( int profileDataId ) 
    {
        this.profileDataId = profileDataId;
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
    

    /*  getDigest  *//******************************** 
    *
    */ 
    public Digest getDigest() 
    {
        return this.digest;
    }
    
    
    /*  setDigest  *//******************************** 
    *
    */
    public void setDigest( Digest digest ) 
    {
        this.digest = digest;
    }
    

    /*  getXCoord  *//******************************** 
    *
    */ 
    public Double getXCoord() 
    {
        return this.XCoord;
    }
    
    
    /*  setXCoord  *//******************************** 
    *
    */
    public void setXCoord( Double XCoord ) 
    {
        this.XCoord = XCoord;
    }
    

    /*  getYCoord  *//******************************** 
    *
    */ 
    public Double getYCoord() 
    {
        return this.YCoord;
    }
    
    
    /*  setYCoord  *//******************************** 
    *
    */
    public void setYCoord( Double YCoord ) 
    {
        this.YCoord = YCoord;
    }
    
       

    







} // end class
