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
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/
// Generated Apr 3, 2007 6:49:19 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.ms;

//  stdlib imports

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class AnalyserParameter  *//**********************************************
*
*
*/ 
public class AnalyserParameter extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int analyserParameterId;
      
    private DeviceSettings deviceSettings;
      
    private Analyser analyser;
      
    private TandemScanMethod tandemScanMethod;
      
    private String resolutionType;
      
    private String resolutionMethod;
      
    private double resolution;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public AnalyserParameter() {}

    
    /** full constructor */
    public AnalyserParameter( DeviceSettings deviceSettings, Analyser analyser, TandemScanMethod tandemScanMethod, String resolutionType, String resolutionMethod, double resolution ) 
    {
        this.deviceSettings = deviceSettings;
        this.analyser = analyser;
        this.tandemScanMethod = tandemScanMethod;
        this.resolutionType = resolutionType;
        this.resolutionMethod = resolutionMethod;
        this.resolution = resolution;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getAnalyserParameterId  *//******************************** 
    *
    */ 
    public int getAnalyserParameterId() 
    {
        return this.analyserParameterId;
    }
    
    
    /*  setAnalyserParameterId  *//******************************** 
    *
    */
    public void setAnalyserParameterId( int analyserParameterId ) 
    {
        this.analyserParameterId = analyserParameterId;
    }
    

    /*  getDeviceSettings  *//******************************** 
    *
    */ 
    public DeviceSettings getDeviceSettings() 
    {
        return this.deviceSettings;
    }
    
    
    /*  setDeviceSettings  *//******************************** 
    *
    */
    public void setDeviceSettings( DeviceSettings deviceSettings ) 
    {
        this.deviceSettings = deviceSettings;
    }
    

    /*  getAnalyser  *//******************************** 
    *
    */ 
    public Analyser getAnalyser() 
    {
        return this.analyser;
    }
    
    
    /*  setAnalyser  *//******************************** 
    *
    */
    public void setAnalyser( Analyser analyser ) 
    {
        this.analyser = analyser;
    }
    

    /*  getTandemScanMethod  *//******************************** 
    *
    */ 
    public TandemScanMethod getTandemScanMethod() 
    {
        return this.tandemScanMethod;
    }
    
    
    /*  setTandemScanMethod  *//******************************** 
    *
    */
    public void setTandemScanMethod( TandemScanMethod tandemScanMethod ) 
    {
        this.tandemScanMethod = tandemScanMethod;
    }
    

    /*  getResolutionType  *//******************************** 
    *
    */ 
    public String getResolutionType() 
    {
        return this.resolutionType;
    }
    
    
    /*  setResolutionType  *//******************************** 
    *
    */
    public void setResolutionType( String resolutionType ) 
    {
        this.resolutionType = resolutionType;
    }
    

    /*  getResolutionMethod  *//******************************** 
    *
    */ 
    public String getResolutionMethod() 
    {
        return this.resolutionMethod;
    }
    
    
    /*  setResolutionMethod  *//******************************** 
    *
    */
    public void setResolutionMethod( String resolutionMethod ) 
    {
        this.resolutionMethod = resolutionMethod;
    }
    

    /*  getResolution  *//******************************** 
    *
    */ 
    public double getResolution() 
    {
        return this.resolution;
    }
    
    
    /*  setResolution  *//******************************** 
    *
    */
    public void setResolution( double resolution ) 
    {
        this.resolution = resolution;
    }
    
       

    







} // end class
