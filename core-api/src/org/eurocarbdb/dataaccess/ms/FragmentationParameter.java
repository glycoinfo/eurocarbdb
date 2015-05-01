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

/*  class FragmentationParameter  *//**********************************************
*
*
*/ 
public class FragmentationParameter extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int fragmentationParameterId;
      
    private DeviceSettings deviceSettings;
      
    private FragmentationType fragmentationType;
      
    private String collisionGas;
      
    private double pressure;
      
    private double collisionEnergie;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public FragmentationParameter() {}

    
    /** full constructor */
    public FragmentationParameter( DeviceSettings deviceSettings, FragmentationType fragmentationType, String collisionGas, double pressure, double collisionEnergie ) 
    {
        this.deviceSettings = deviceSettings;
        this.fragmentationType = fragmentationType;
        this.collisionGas = collisionGas;
        this.pressure = pressure;
        this.collisionEnergie = collisionEnergie;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getFragmentationParameterId  *//******************************** 
    *
    */ 
    public int getFragmentationParameterId() 
    {
        return this.fragmentationParameterId;
    }
    
    
    /*  setFragmentationParameterId  *//******************************** 
    *
    */
    public void setFragmentationParameterId( int fragmentationParameterId ) 
    {
        this.fragmentationParameterId = fragmentationParameterId;
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
    

    /*  getFragmentationType  *//******************************** 
    *
    */ 
    public FragmentationType getFragmentationType() 
    {
        return this.fragmentationType;
    }
    
    
    /*  setFragmentationType  *//******************************** 
    *
    */
    public void setFragmentationType( FragmentationType fragmentationType ) 
    {
        this.fragmentationType = fragmentationType;
    }
    

    /*  getCollisionGas  *//******************************** 
    *
    */ 
    public String getCollisionGas() 
    {
        return this.collisionGas;
    }
    
    
    /*  setCollisionGas  *//******************************** 
    *
    */
    public void setCollisionGas( String collisionGas ) 
    {
        this.collisionGas = collisionGas;
    }
    

    /*  getPressure  *//******************************** 
    *
    */ 
    public double getPressure() 
    {
        return this.pressure;
    }
    
    
    /*  setPressure  *//******************************** 
    *
    */
    public void setPressure( double pressure ) 
    {
        this.pressure = pressure;
    }
    

    /*  getCollisionEnergie  *//******************************** 
    *
    */ 
    public double getCollisionEnergie() 
    {
        return this.collisionEnergie;
    }
    
    
    /*  setCollisionEnergie  *//******************************** 
    *
    */
    public void setCollisionEnergie( double collisionEnergie ) 
    {
        this.collisionEnergie = collisionEnergie;
    }
    
       

    







} // end class
