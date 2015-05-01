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
// Generated Jun 21, 2007 2:07:01 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class IntegrationMethod  *//**********************************************
*
*
*/ 
public class IntegrationMethod extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int integrationMethod;
      
    private Profile profile;
      
    private Digest digest;
      
    private short rtRangeEnd;
      
    private short rtRangeStart;
      
    private double peakWidth;
      
    private Double peakThreshold;
      
    private double peakMinHeight;
      
    private double peakMinArea;
      
    private String calibrationCurveType;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public IntegrationMethod() {}

    /** Minimal constructor */
    public IntegrationMethod( Profile profile, Digest digest, short rtRangeEnd, short rtRangeStart, double peakWidth, double peakMinHeight, double peakMinArea, String calibrationCurveType ) 
    {
        this.profile = profile;
        this.digest = digest;
        this.rtRangeEnd = rtRangeEnd;
        this.rtRangeStart = rtRangeStart;
        this.peakWidth = peakWidth;
        this.peakMinHeight = peakMinHeight;
        this.peakMinArea = peakMinArea;
        this.calibrationCurveType = calibrationCurveType;
    }
    
    /** full constructor */
    public IntegrationMethod( Profile profile, Digest digest, short rtRangeEnd, short rtRangeStart, double peakWidth, Double peakThreshold, double peakMinHeight, double peakMinArea, String calibrationCurveType ) 
    {
        this.profile = profile;
        this.digest = digest;
        this.rtRangeEnd = rtRangeEnd;
        this.rtRangeStart = rtRangeStart;
        this.peakWidth = peakWidth;
        this.peakThreshold = peakThreshold;
        this.peakMinHeight = peakMinHeight;
        this.peakMinArea = peakMinArea;
        this.calibrationCurveType = calibrationCurveType;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getIntegrationMethod  *//******************************** 
    *
    */ 
    public int getIntegrationMethod() 
    {
        return this.integrationMethod;
    }
    
    
    /*  setIntegrationMethod  *//******************************** 
    *
    */
    public void setIntegrationMethod( int integrationMethod ) 
    {
        this.integrationMethod = integrationMethod;
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
    

    /*  getRtRangeEnd  *//******************************** 
    *
    */ 
    public short getRtRangeEnd() 
    {
        return this.rtRangeEnd;
    }
    
    
    /*  setRtRangeEnd  *//******************************** 
    *
    */
    public void setRtRangeEnd( short rtRangeEnd ) 
    {
        this.rtRangeEnd = rtRangeEnd;
    }
    

    /*  getRtRangeStart  *//******************************** 
    *
    */ 
    public short getRtRangeStart() 
    {
        return this.rtRangeStart;
    }
    
    
    /*  setRtRangeStart  *//******************************** 
    *
    */
    public void setRtRangeStart( short rtRangeStart ) 
    {
        this.rtRangeStart = rtRangeStart;
    }
    

    /*  getPeakWidth  *//******************************** 
    *
    */ 
    public double getPeakWidth() 
    {
        return this.peakWidth;
    }
    
    
    /*  setPeakWidth  *//******************************** 
    *
    */
    public void setPeakWidth( double peakWidth ) 
    {
        this.peakWidth = peakWidth;
    }
    

    /*  getPeakThreshold  *//******************************** 
    *
    */ 
    public Double getPeakThreshold() 
    {
        return this.peakThreshold;
    }
    
    
    /*  setPeakThreshold  *//******************************** 
    *
    */
    public void setPeakThreshold( Double peakThreshold ) 
    {
        this.peakThreshold = peakThreshold;
    }
    

    /*  getPeakMinHeight  *//******************************** 
    *
    */ 
    public double getPeakMinHeight() 
    {
        return this.peakMinHeight;
    }
    
    
    /*  setPeakMinHeight  *//******************************** 
    *
    */
    public void setPeakMinHeight( double peakMinHeight ) 
    {
        this.peakMinHeight = peakMinHeight;
    }
    

    /*  getPeakMinArea  *//******************************** 
    *
    */ 
    public double getPeakMinArea() 
    {
        return this.peakMinArea;
    }
    
    
    /*  setPeakMinArea  *//******************************** 
    *
    */
    public void setPeakMinArea( double peakMinArea ) 
    {
        this.peakMinArea = peakMinArea;
    }
    

    /*  getCalibrationCurveType  *//******************************** 
    *
    */ 
    public String getCalibrationCurveType() 
    {
        return this.calibrationCurveType;
    }
    
    
    /*  setCalibrationCurveType  *//******************************** 
    *
    */
    public void setCalibrationCurveType( String calibrationCurveType ) 
    {
        this.calibrationCurveType = calibrationCurveType;
    }
    
       

    







} // end class
