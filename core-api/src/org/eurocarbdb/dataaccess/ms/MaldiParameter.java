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

/*  class MaldiParameter  *//**********************************************
*
*
*/ 
public class MaldiParameter extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int maldiParameterId;
      
    private DeviceSettings deviceSettings;
      
    private MaldiMatrix maldiMatrix;
      
    private double spotDiameter;
      
    private String spotType;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public MaldiParameter() {}

    
    /** full constructor */
    public MaldiParameter( DeviceSettings deviceSettings, MaldiMatrix maldiMatrix, double spotDiameter, String spotType ) 
    {
        this.deviceSettings = deviceSettings;
        this.maldiMatrix = maldiMatrix;
        this.spotDiameter = spotDiameter;
        this.spotType = spotType;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getMaldiParameterId  *//******************************** 
    *
    */ 
    public int getMaldiParameterId() 
    {
        return this.maldiParameterId;
    }
    
    
    /*  setMaldiParameterId  *//******************************** 
    *
    */
    public void setMaldiParameterId( int maldiParameterId ) 
    {
        this.maldiParameterId = maldiParameterId;
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
    

    /*  getMaldiMatrix  *//******************************** 
    *
    */ 
    public MaldiMatrix getMaldiMatrix() 
    {
        return this.maldiMatrix;
    }
    
    
    /*  setMaldiMatrix  *//******************************** 
    *
    */
    public void setMaldiMatrix( MaldiMatrix maldiMatrix ) 
    {
        this.maldiMatrix = maldiMatrix;
    }
    

    /*  getSpotDiameter  *//******************************** 
    *
    */ 
    public double getSpotDiameter() 
    {
        return this.spotDiameter;
    }
    
    
    /*  setSpotDiameter  *//******************************** 
    *
    */
    public void setSpotDiameter( double spotDiameter ) 
    {
        this.spotDiameter = spotDiameter;
    }
    

    /*  getSpotType  *//******************************** 
    *
    */ 
    public String getSpotType() 
    {
        return this.spotType;
    }
    
    
    /*  setSpotType  *//******************************** 
    *
    */
    public void setSpotType( String spotType ) 
    {
        this.spotType = spotType;
    }
    
       

    







} // end class
