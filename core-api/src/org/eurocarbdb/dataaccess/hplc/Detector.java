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
// Generated Jun 21, 2007 2:07:00 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports
import java.util.HashSet;
import java.util.Set;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Detector  *//**********************************************
*
*
*/ 
public class Detector extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int detectorId;
      
    private String manufacturer;
      
    private String model;
      
    private int manufacturerId;
      
    private int modelId;
      
    private Short excitation;
      
    private Short emission;
      
    private Double bandwidth;
      
    private Short samplingRate;
      
    private Set<Profile> profiles = new HashSet<Profile>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public Detector() {}

    /** Minimal constructor */
    public Detector( int manufacturerId, int modelId ) 
    {
        this.manufacturerId = manufacturerId;
        this.modelId = modelId;
    }
    
    /** full constructor */
    public Detector( String manufacturer, String model, int manufacturerId, int modelId, Short excitation, Short emission, Double bandwidth, Short samplingRate, Set<Profile> profiles ) 
    {
        this.manufacturer = manufacturer;
        this.model = model;
        this.manufacturerId = manufacturerId;
        this.modelId = modelId;
        this.excitation = excitation;
        this.emission = emission;
        this.bandwidth = bandwidth;
        this.samplingRate = samplingRate;
        this.profiles = profiles;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getDetectorId  *//******************************** 
    *
    */ 
    public int getDetectorId() 
    {
        return this.detectorId;
    }
    
    
    /*  setDetectorId  *//******************************** 
    *
    */
    public void setDetectorId( int detectorId ) 
    {
        this.detectorId = detectorId;
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
    

    /*  getManufacturerId  *//******************************** 
    *
    */ 
    public int getManufacturerId() 
    {
        return this.manufacturerId;
    }
    
    
    /*  setManufacturerId  *//******************************** 
    *
    */
    public void setManufacturerId( int manufacturerId ) 
    {
        this.manufacturerId = manufacturerId;
    }
    

    /*  getModelId  *//******************************** 
    *
    */ 
    public int getModelId() 
    {
        return this.modelId;
    }
    
    
    /*  setModelId  *//******************************** 
    *
    */
    public void setModelId( int modelId ) 
    {
        this.modelId = modelId;
    }
    

    /*  getExcitation  *//******************************** 
    *
    */ 
    public Short getExcitation() 
    {
        return this.excitation;
    }
    
    
    /*  setExcitation  *//******************************** 
    *
    */
    public void setExcitation( Short excitation ) 
    {
        this.excitation = excitation;
    }
    

    /*  getEmission  *//******************************** 
    *
    */ 
    public Short getEmission() 
    {
        return this.emission;
    }
    
    
    /*  setEmission  *//******************************** 
    *
    */
    public void setEmission( Short emission ) 
    {
        this.emission = emission;
    }
    

    /*  getBandwidth  *//******************************** 
    *
    */ 
    public Double getBandwidth() 
    {
        return this.bandwidth;
    }
    
    
    /*  setBandwidth  *//******************************** 
    *
    */
    public void setBandwidth( Double bandwidth ) 
    {
        this.bandwidth = bandwidth;
    }
    

    /*  getSamplingRate  *//******************************** 
    *
    */ 
    public Short getSamplingRate() 
    {
        return this.samplingRate;
    }
    
    
    /*  setSamplingRate  *//******************************** 
    *
    */
    public void setSamplingRate( Short samplingRate ) 
    {
        this.samplingRate = samplingRate;
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
