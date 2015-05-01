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

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class HplcPeaksIntegrated  *//**********************************************
*
*
*/ 
public class HplcPeaksIntegrated extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int hplcPeaksIntegratedId;
      
    private Profile profile;
      
    private Digest digest;
      
    private Integer assignedPeak;
      
    private double peakArea;
      
    private int gu;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public HplcPeaksIntegrated() {}

    /** Minimal constructor */
    public HplcPeaksIntegrated( Profile profile, double peakArea, int gu ) 
    {
        this.profile = profile;
        this.peakArea = peakArea;
        this.gu = gu;
    }
    
    /** full constructor */
    public HplcPeaksIntegrated( Profile profile, Digest digest, Integer assignedPeak, double peakArea, int gu ) 
    {
        this.profile = profile;
        this.digest = digest;
        this.assignedPeak = assignedPeak;
        this.peakArea = peakArea;
        this.gu = gu;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getHplcPeaksIntegratedId  *//******************************** 
    *
    */ 
    public int getHplcPeaksIntegratedId() 
    {
        return this.hplcPeaksIntegratedId;
    }
    
    
    /*  setHplcPeaksIntegratedId  *//******************************** 
    *
    */
    public void setHplcPeaksIntegratedId( int hplcPeaksIntegratedId ) 
    {
        this.hplcPeaksIntegratedId = hplcPeaksIntegratedId;
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
    

    /*  getAssignedPeak  *//******************************** 
    *
    */ 
    public Integer getAssignedPeak() 
    {
        return this.assignedPeak;
    }
    
    
    /*  setAssignedPeak  *//******************************** 
    *
    */
    public void setAssignedPeak( Integer assignedPeak ) 
    {
        this.assignedPeak = assignedPeak;
    }
    

    /*  getPeakArea  *//******************************** 
    *
    */ 
    public double getPeakArea() 
    {
        return this.peakArea;
    }
    
    
    /*  setPeakArea  *//******************************** 
    *
    */
    public void setPeakArea( double peakArea ) 
    {
        this.peakArea = peakArea;
    }
    

    /*  getGu  *//******************************** 
    *
    */ 
    public int getGu() 
    {
        return this.gu;
    }
    
    
    /*  setGu  *//******************************** 
    *
    */
    public void setGu( int gu ) 
    {
        this.gu = gu;
    }
    
       

    







} // end class
