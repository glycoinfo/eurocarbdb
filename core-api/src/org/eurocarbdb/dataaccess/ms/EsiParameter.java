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
// Generated Apr 3, 2007 6:49:18 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.ms;

//  stdlib imports

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class EsiParameter  *//**********************************************
*
*
*/ 
public class EsiParameter extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int esiParameterId;
      
    private DeviceSettings deviceSettings;
      
    private String dryGas;
      
    private Double flowRate;
      
    private Double temperatur;
      
    private Double voltageCapillary;
      
    private Double voltageEndPlate;
      
    private String solvent;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public EsiParameter() {}

    /** Minimal constructor */
    public EsiParameter( DeviceSettings deviceSettings, String solvent ) 
    {
        this.deviceSettings = deviceSettings;
        this.solvent = solvent;
    }
    
    /** full constructor */
    public EsiParameter( DeviceSettings deviceSettings, String dryGas, Double flowRate, Double temperatur, Double voltageCapillary, Double voltageEndPlate, String solvent ) 
    {
        this.deviceSettings = deviceSettings;
        this.dryGas = dryGas;
        this.flowRate = flowRate;
        this.temperatur = temperatur;
        this.voltageCapillary = voltageCapillary;
        this.voltageEndPlate = voltageEndPlate;
        this.solvent = solvent;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getEsiParameterId  *//******************************** 
    *
    */ 
    public int getEsiParameterId() 
    {
        return this.esiParameterId;
    }
    
    
    /*  setEsiParameterId  *//******************************** 
    *
    */
    public void setEsiParameterId( int esiParameterId ) 
    {
        this.esiParameterId = esiParameterId;
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
    

    /*  getDryGas  *//******************************** 
    *
    */ 
    public String getDryGas() 
    {
        return this.dryGas;
    }
    
    
    /*  setDryGas  *//******************************** 
    *
    */
    public void setDryGas( String dryGas ) 
    {
        this.dryGas = dryGas;
    }
    

    /*  getFlowRate  *//******************************** 
    *
    */ 
    public Double getFlowRate() 
    {
        return this.flowRate;
    }
    
    
    /*  setFlowRate  *//******************************** 
    *
    */
    public void setFlowRate( Double flowRate ) 
    {
        this.flowRate = flowRate;
    }
    

    /*  getTemperatur  *//******************************** 
    *
    */ 
    public Double getTemperatur() 
    {
        return this.temperatur;
    }
    
    
    /*  setTemperatur  *//******************************** 
    *
    */
    public void setTemperatur( Double temperatur ) 
    {
        this.temperatur = temperatur;
    }
    

    /*  getVoltageCapillary  *//******************************** 
    *
    */ 
    public Double getVoltageCapillary() 
    {
        return this.voltageCapillary;
    }
    
    
    /*  setVoltageCapillary  *//******************************** 
    *
    */
    public void setVoltageCapillary( Double voltageCapillary ) 
    {
        this.voltageCapillary = voltageCapillary;
    }
    

    /*  getVoltageEndPlate  *//******************************** 
    *
    */ 
    public Double getVoltageEndPlate() 
    {
        return this.voltageEndPlate;
    }
    
    
    /*  setVoltageEndPlate  *//******************************** 
    *
    */
    public void setVoltageEndPlate( Double voltageEndPlate ) 
    {
        this.voltageEndPlate = voltageEndPlate;
    }
    

    /*  getSolvent  *//******************************** 
    *
    */ 
    public String getSolvent() 
    {
        return this.solvent;
    }
    
    
    /*  setSolvent  *//******************************** 
    *
    */
    public void setSolvent( String solvent ) 
    {
        this.solvent = solvent;
    }
    
       

    







} // end class
