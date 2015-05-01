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
// Generated Apr 3, 2007 6:49:20 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.ms;

//  stdlib imports

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class MassDetectorParameter  *//**********************************************
*
*
*/ 
public class MassDetectorParameter extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int massDetectorParameterId;
      
    private DeviceSettings deviceSettings;
      
    private MassDetector massDetector;
      
    private int digitalResolution;
      
    private int samplingFrequency;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public MassDetectorParameter() {}

    
    /** full constructor */
    public MassDetectorParameter( DeviceSettings deviceSettings, MassDetector massDetector, int digitalResolution, int samplingFrequency ) 
    {
        this.deviceSettings = deviceSettings;
        this.massDetector = massDetector;
        this.digitalResolution = digitalResolution;
        this.samplingFrequency = samplingFrequency;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getMassDetectorParameterId  *//******************************** 
    *
    */ 
    public int getMassDetectorParameterId() 
    {
        return this.massDetectorParameterId;
    }
    
    
    /*  setMassDetectorParameterId  *//******************************** 
    *
    */
    public void setMassDetectorParameterId( int massDetectorParameterId ) 
    {
        this.massDetectorParameterId = massDetectorParameterId;
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
    

    /*  getMassDetector  *//******************************** 
    *
    */ 
    public MassDetector getMassDetector() 
    {
        return this.massDetector;
    }
    
    
    /*  setMassDetector  *//******************************** 
    *
    */
    public void setMassDetector( MassDetector massDetector ) 
    {
        this.massDetector = massDetector;
    }
    

    /*  getDigitalResolution  *//******************************** 
    *
    */ 
    public int getDigitalResolution() 
    {
        return this.digitalResolution;
    }
    
    
    /*  setDigitalResolution  *//******************************** 
    *
    */
    public void setDigitalResolution( int digitalResolution ) 
    {
        this.digitalResolution = digitalResolution;
    }
    

    /*  getSamplingFrequency  *//******************************** 
    *
    */ 
    public int getSamplingFrequency() 
    {
        return this.samplingFrequency;
    }
    
    
    /*  setSamplingFrequency  *//******************************** 
    *
    */
    public void setSamplingFrequency( int samplingFrequency ) 
    {
        this.samplingFrequency = samplingFrequency;
    }
    
       

    







} // end class
