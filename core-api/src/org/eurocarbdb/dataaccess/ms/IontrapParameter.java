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
// Generated Apr 3, 2007 6:49:18 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.ms;

//  stdlib imports

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class IontrapParameter  *//**********************************************
*
*
*/ 
public class IontrapParameter extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int iontrapParameterId;
      
    private DeviceSettings deviceSettings;
      
    private int ionCount;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public IontrapParameter() {}

    
    /** full constructor */
    public IontrapParameter( DeviceSettings deviceSettings, int ionCount ) 
    {
        this.deviceSettings = deviceSettings;
        this.ionCount = ionCount;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getIontrapParameterId  *//******************************** 
    *
    */ 
    public int getIontrapParameterId() 
    {
        return this.iontrapParameterId;
    }
    
    
    /*  setIontrapParameterId  *//******************************** 
    *
    */
    public void setIontrapParameterId( int iontrapParameterId ) 
    {
        this.iontrapParameterId = iontrapParameterId;
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
    

    /*  getIonCount  *//******************************** 
    *
    */ 
    public int getIonCount() 
    {
        return this.ionCount;
    }
    
    
    /*  setIonCount  *//******************************** 
    *
    */
    public void setIonCount( int ionCount ) 
    {
        this.ionCount = ionCount;
    }
    
       

    







} // end class
