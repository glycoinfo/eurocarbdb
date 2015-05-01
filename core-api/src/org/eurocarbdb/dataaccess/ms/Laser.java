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
// Generated Apr 3, 2007 6:49:19 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.ms;

//  stdlib imports
import java.util.HashSet;
import java.util.Set;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Laser  *//**********************************************
*
*
*/ 
public class Laser extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int laserId;
      
    private Device device;
      
    private String model;
      
    private String laserType;
      
    private double focus;
      
    private Double energy;
      
    private Double frequency;
      
    private Double waveLength;
      
    private Set<LaserParameter> laserParameters = new HashSet<LaserParameter>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public Laser() {}

    /** Minimal constructor */
    public Laser( Device device, String laserType, double focus ) 
    {
        this.device = device;
        this.laserType = laserType;
        this.focus = focus;
    }
    
    /** full constructor */
    public Laser( Device device, String model, String laserType, double focus, Double energy, Double frequency, Double waveLength, Set<LaserParameter> laserParameters ) 
    {
        this.device = device;
        this.model = model;
        this.laserType = laserType;
        this.focus = focus;
        this.energy = energy;
        this.frequency = frequency;
        this.waveLength = waveLength;
        this.laserParameters = laserParameters;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getLaserId  *//******************************** 
    *
    */ 
    public int getLaserId() 
    {
        return this.laserId;
    }
    
    
    /*  setLaserId  *//******************************** 
    *
    */
    public void setLaserId( int laserId ) 
    {
        this.laserId = laserId;
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
    

    /*  getLaserType  *//******************************** 
    *
    */ 
    public String getLaserType() 
    {
        return this.laserType;
    }
    
    
    /*  setLaserType  *//******************************** 
    *
    */
    public void setLaserType( String laserType ) 
    {
        this.laserType = laserType;
    }
    

    /*  getFocus  *//******************************** 
    *
    */ 
    public double getFocus() 
    {
        return this.focus;
    }
    
    
    /*  setFocus  *//******************************** 
    *
    */
    public void setFocus( double focus ) 
    {
        this.focus = focus;
    }
    

    /*  getEnergy  *//******************************** 
    *
    */ 
    public Double getEnergy() 
    {
        return this.energy;
    }
    
    
    /*  setEnergy  *//******************************** 
    *
    */
    public void setEnergy( Double energy ) 
    {
        this.energy = energy;
    }
    

    /*  getFrequency  *//******************************** 
    *
    */ 
    public Double getFrequency() 
    {
        return this.frequency;
    }
    
    
    /*  setFrequency  *//******************************** 
    *
    */
    public void setFrequency( Double frequency ) 
    {
        this.frequency = frequency;
    }
    

    /*  getWaveLength  *//******************************** 
    *
    */ 
    public Double getWaveLength() 
    {
        return this.waveLength;
    }
    
    
    /*  setWaveLength  *//******************************** 
    *
    */
    public void setWaveLength( Double waveLength ) 
    {
        this.waveLength = waveLength;
    }
    

    /*  getLaserParameters  *//******************************** 
    *
    */ 
    public Set<LaserParameter> getLaserParameters() 
    {
        return this.laserParameters;
    }
    
    
    /*  setLaserParameters  *//******************************** 
    *
    */
    public void setLaserParameters( Set<LaserParameter> laserParameters ) 
    {
        this.laserParameters = laserParameters;
    }
    
       

    







} // end class
