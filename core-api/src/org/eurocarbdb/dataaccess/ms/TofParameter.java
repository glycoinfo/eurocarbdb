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
// Generated Apr 3, 2007 6:49:20 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.ms;

//  stdlib imports

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class TofParameter  *//**********************************************
*
*
*/ 
public class TofParameter extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int tofParameterId;
      
    private DeviceSettings deviceSettings;
      
    private Boolean reflectorState;
      
    private Double acceleratorGridVoltage;
      
    private Double delayExtrationTime;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public TofParameter() {}

    /** Minimal constructor */
    public TofParameter( DeviceSettings deviceSettings ) 
    {
        this.deviceSettings = deviceSettings;
    }
    
    /** full constructor */
    public TofParameter( DeviceSettings deviceSettings, Boolean reflectorState, Double acceleratorGridVoltage, Double delayExtrationTime ) 
    {
        this.deviceSettings = deviceSettings;
        this.reflectorState = reflectorState;
        this.acceleratorGridVoltage = acceleratorGridVoltage;
        this.delayExtrationTime = delayExtrationTime;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getTofParameterId  *//******************************** 
    *
    */ 
    public int getTofParameterId() 
    {
        return this.tofParameterId;
    }
    
    
    /*  setTofParameterId  *//******************************** 
    *
    */
    public void setTofParameterId( int tofParameterId ) 
    {
        this.tofParameterId = tofParameterId;
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
    

    /*  getReflectorState  *//******************************** 
    *
    */ 
    public Boolean getReflectorState() 
    {
        return this.reflectorState;
    }
    
    
    /*  setReflectorState  *//******************************** 
    *
    */
    public void setReflectorState( Boolean reflectorState ) 
    {
        this.reflectorState = reflectorState;
    }
    

    /*  getAcceleratorGridVoltage  *//******************************** 
    *
    */ 
    public Double getAcceleratorGridVoltage() 
    {
        return this.acceleratorGridVoltage;
    }
    
    
    /*  setAcceleratorGridVoltage  *//******************************** 
    *
    */
    public void setAcceleratorGridVoltage( Double acceleratorGridVoltage ) 
    {
        this.acceleratorGridVoltage = acceleratorGridVoltage;
    }
    

    /*  getDelayExtrationTime  *//******************************** 
    *
    */ 
    public Double getDelayExtrationTime() 
    {
        return this.delayExtrationTime;
    }
    
    
    /*  setDelayExtrationTime  *//******************************** 
    *
    */
    public void setDelayExtrationTime( Double delayExtrationTime ) 
    {
        this.delayExtrationTime = delayExtrationTime;
    }
    
       

    







} // end class
