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
import java.util.HashSet;
import java.util.Set;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Instrument  *//**********************************************
*
*
*/ 
public class Instrument extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int instrumentId;
      
    private String manufacturer;
      
    private String model;
      
    private Double temperature;
      
    private String solventA;
      
    private String solventB;
      
    private String solventC;
      
    private String solventD;
      
    private Double flowRate;
      
    private String flowGradient;
      
    private Set<Profile> profiles = new HashSet<Profile>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public Instrument() {}

    /** Minimal constructor */
    public Instrument( String manufacturer, String model ) 
    {
        this.manufacturer = manufacturer;
        this.model = model;
    }
    
    /** full constructor */
    public Instrument( String manufacturer, String model, Double temperature, String solventA, String solventB, String solventC, String solventD, Double flowRate, String flowGradient, Set<Profile> profiles ) 
    {
        this.manufacturer = manufacturer;
        this.model = model;
        this.temperature = temperature;
        this.solventA = solventA;
        this.solventB = solventB;
        this.solventC = solventC;
        this.solventD = solventD;
        this.flowRate = flowRate;
        this.flowGradient = flowGradient;
        this.profiles = profiles;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getInstrumentId  *//******************************** 
    *
    */ 
    public int getInstrumentId() 
    {
        return this.instrumentId;
    }
    
    
    /*  setInstrumentId  *//******************************** 
    *
    */
    public void setInstrumentId( int instrumentId ) 
    {
        this.instrumentId = instrumentId;
    }
    

    /*  getManufacturer  *//******************************** 
    *
    */ 
    public String getManufacturer() 
    {
        return this.manufacturer;
    }
    
    
    /*  setManufacturer  *//******************************** 
    *
    */
    public void setManufacturer( String manufacturer ) 
    {
        this.manufacturer = manufacturer;
    }
    

    /*  getModel  *//******************************** 
    *
    */ 
    public String getModel() 
    {
        return this.model;
    }
    
    
    /*  setModel  *//******************************** 
    *
    */
    public void setModel( String model ) 
    {
        this.model = model;
    }
    

    /*  getTemperature  *//******************************** 
    *
    */ 
    public Double getTemperature() 
    {
        return this.temperature;
    }
    
    
    /*  setTemperature  *//******************************** 
    *
    */
    public void setTemperature( Double temperature ) 
    {
        this.temperature = temperature;
    }
    

    /*  getSolventA  *//******************************** 
    *
    */ 
    public String getSolventA() 
    {
        return this.solventA;
    }
    
    
    /*  setSolventA  *//******************************** 
    *
    */
    public void setSolventA( String solventA ) 
    {
        this.solventA = solventA;
    }
    

    /*  getSolventB  *//******************************** 
    *
    */ 
    public String getSolventB() 
    {
        return this.solventB;
    }
    
    
    /*  setSolventB  *//******************************** 
    *
    */
    public void setSolventB( String solventB ) 
    {
        this.solventB = solventB;
    }
    

    /*  getSolventC  *//******************************** 
    *
    */ 
    public String getSolventC() 
    {
        return this.solventC;
    }
    
    
    /*  setSolventC  *//******************************** 
    *
    */
    public void setSolventC( String solventC ) 
    {
        this.solventC = solventC;
    }
    

    /*  getSolventD  *//******************************** 
    *
    */ 
    public String getSolventD() 
    {
        return this.solventD;
    }
    
    
    /*  setSolventD  *//******************************** 
    *
    */
    public void setSolventD( String solventD ) 
    {
        this.solventD = solventD;
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
    

    /*  getFlowGradient  *//******************************** 
    *
    */ 
    public String getFlowGradient() 
    {
        return this.flowGradient;
    }
    
    
    /*  setFlowGradient  *//******************************** 
    *
    */
    public void setFlowGradient( String flowGradient ) 
    {
        this.flowGradient = flowGradient;
    }
    

    /*  getProfiles  *//******************************** 
    *
    */ 
    public Set<Profile> getProfiles() 
    {
        return this.profiles;
    }
    
    
    /*  setProfiles  *//******************************** 
    *
    */
    public void setProfiles( Set<Profile> profiles ) 
    {
        this.profiles = profiles;
    }
    
       

    







} // end class
