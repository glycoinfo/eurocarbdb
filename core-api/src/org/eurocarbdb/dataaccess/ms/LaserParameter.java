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

/*  class LaserParameter  *//**********************************************
*
*
*/ 
public class LaserParameter extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int laserParameterId;
      
    private DeviceSettings deviceSettings;
      
    private Laser laser;
      
    private int laserShootCount;
      
    private double laserFrequency;
      
    private double laserIntensity;
      
    private double laserFocus;
      
    private Double ionisationEnergy;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public LaserParameter() {}

    /** Minimal constructor */
    public LaserParameter( DeviceSettings deviceSettings, Laser laser, int laserShootCount, double laserFrequency, double laserIntensity, double laserFocus ) 
    {
        this.deviceSettings = deviceSettings;
        this.laser = laser;
        this.laserShootCount = laserShootCount;
        this.laserFrequency = laserFrequency;
        this.laserIntensity = laserIntensity;
        this.laserFocus = laserFocus;
    }
    
    /** full constructor */
    public LaserParameter( DeviceSettings deviceSettings, Laser laser, int laserShootCount, double laserFrequency, double laserIntensity, double laserFocus, Double ionisationEnergy ) 
    {
        this.deviceSettings = deviceSettings;
        this.laser = laser;
        this.laserShootCount = laserShootCount;
        this.laserFrequency = laserFrequency;
        this.laserIntensity = laserIntensity;
        this.laserFocus = laserFocus;
        this.ionisationEnergy = ionisationEnergy;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getLaserParameterId  *//******************************** 
    *
    */ 
    public int getLaserParameterId() 
    {
        return this.laserParameterId;
    }
    
    
    /*  setLaserParameterId  *//******************************** 
    *
    */
    public void setLaserParameterId( int laserParameterId ) 
    {
        this.laserParameterId = laserParameterId;
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
    

    /*  getLaser  *//******************************** 
    *
    */ 
    public Laser getLaser() 
    {
        return this.laser;
    }
    
    
    /*  setLaser  *//******************************** 
    *
    */
    public void setLaser( Laser laser ) 
    {
        this.laser = laser;
    }
    

    /*  getLaserShootCount  *//******************************** 
    *
    */ 
    public int getLaserShootCount() 
    {
        return this.laserShootCount;
    }
    
    
    /*  setLaserShootCount  *//******************************** 
    *
    */
    public void setLaserShootCount( int laserShootCount ) 
    {
        this.laserShootCount = laserShootCount;
    }
    

    /*  getLaserFrequency  *//******************************** 
    *
    */ 
    public double getLaserFrequency() 
    {
        return this.laserFrequency;
    }
    
    
    /*  setLaserFrequency  *//******************************** 
    *
    */
    public void setLaserFrequency( double laserFrequency ) 
    {
        this.laserFrequency = laserFrequency;
    }
    

    /*  getLaserIntensity  *//******************************** 
    *
    */ 
    public double getLaserIntensity() 
    {
        return this.laserIntensity;
    }
    
    
    /*  setLaserIntensity  *//******************************** 
    *
    */
    public void setLaserIntensity( double laserIntensity ) 
    {
        this.laserIntensity = laserIntensity;
    }
    

    /*  getLaserFocus  *//******************************** 
    *
    */ 
    public double getLaserFocus() 
    {
        return this.laserFocus;
    }
    
    
    /*  setLaserFocus  *//******************************** 
    *
    */
    public void setLaserFocus( double laserFocus ) 
    {
        this.laserFocus = laserFocus;
    }
    

    /*  getIonisationEnergy  *//******************************** 
    *
    */ 
    public Double getIonisationEnergy() 
    {
        return this.ionisationEnergy;
    }
    
    
    /*  setIonisationEnergy  *//******************************** 
    *
    */
    public void setIonisationEnergy( Double ionisationEnergy ) 
    {
        this.ionisationEnergy = ionisationEnergy;
    }
    
       

    







} // end class
