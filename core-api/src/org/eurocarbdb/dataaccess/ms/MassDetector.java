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
import java.util.HashSet;
import java.util.Set;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class MassDetector  *//**********************************************
*
*
*/ 
public class MassDetector extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int massDetectorId;
      
    private Device device;
      
    private String model;
      
    private String massDetectorType;
      
    private double massDetectorResolution;
      
    private double digitalResolution;
      
    private int samplingFrequency;
      
    private Set<MassDetectorParameter> massDetectorParameters = new HashSet<MassDetectorParameter>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public MassDetector() {}

    /** Minimal constructor */
    public MassDetector( Device device, String massDetectorType, double massDetectorResolution, double digitalResolution, int samplingFrequency ) 
    {
        this.device = device;
        this.massDetectorType = massDetectorType;
        this.massDetectorResolution = massDetectorResolution;
        this.digitalResolution = digitalResolution;
        this.samplingFrequency = samplingFrequency;
    }
    
    /** full constructor */
    public MassDetector( Device device, String model, String massDetectorType, double massDetectorResolution, double digitalResolution, int samplingFrequency, Set<MassDetectorParameter> massDetectorParameters ) 
    {
        this.device = device;
        this.model = model;
        this.massDetectorType = massDetectorType;
        this.massDetectorResolution = massDetectorResolution;
        this.digitalResolution = digitalResolution;
        this.samplingFrequency = samplingFrequency;
        this.massDetectorParameters = massDetectorParameters;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getMassDetectorId  *//******************************** 
    *
    */ 
    public int getMassDetectorId() 
    {
        return this.massDetectorId;
    }
    
    
    /*  setMassDetectorId  *//******************************** 
    *
    */
    public void setMassDetectorId( int massDetectorId ) 
    {
        this.massDetectorId = massDetectorId;
    }
    

    /*  getDevice  *//******************************** 
    *
    */ 
    public Device getDevice() 
    {
        return this.device;
    }
    
    
    /*  setDevice  *//******************************** 
    *
    */
    public void setDevice( Device device ) 
    {
        this.device = device;
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
    

    /*  getMassDetectorType  *//******************************** 
    *
    */ 
    public String getMassDetectorType() 
    {
        return this.massDetectorType;
    }
    
    
    /*  setMassDetectorType  *//******************************** 
    *
    */
    public void setMassDetectorType( String massDetectorType ) 
    {
        this.massDetectorType = massDetectorType;
    }
    

    /*  getMassDetectorResolution  *//******************************** 
    *
    */ 
    public double getMassDetectorResolution() 
    {
        return this.massDetectorResolution;
    }
    
    
    /*  setMassDetectorResolution  *//******************************** 
    *
    */
    public void setMassDetectorResolution( double massDetectorResolution ) 
    {
        this.massDetectorResolution = massDetectorResolution;
    }
    

    /*  getDigitalResolution  *//******************************** 
    *
    */ 
    public double getDigitalResolution() 
    {
        return this.digitalResolution;
    }
    
    
    /*  setDigitalResolution  *//******************************** 
    *
    */
    public void setDigitalResolution( double digitalResolution ) 
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
    

    /*  getMassDetectorParameters  *//******************************** 
    *
    */ 
    public Set<MassDetectorParameter> getMassDetectorParameters() 
    {
        return this.massDetectorParameters;
    }
    
    
    /*  setMassDetectorParameters  *//******************************** 
    *
    */
    public void setMassDetectorParameters( Set<MassDetectorParameter> massDetectorParameters ) 
    {
        this.massDetectorParameters = massDetectorParameters;
    }
    
       

    







} // end class
