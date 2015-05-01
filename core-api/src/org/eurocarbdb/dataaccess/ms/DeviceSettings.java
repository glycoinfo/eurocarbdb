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

/*  class DeviceSettings  *//**********************************************
*
*
*/ 
public class DeviceSettings extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int deviceSettingsId;
      
    private Acquisition acquisition;
      
    private double contributorQuality;
      
    private Set<IontrapParameter> iontrapParameters = new HashSet<IontrapParameter>(0);
      
    private Set<SourceParameter> sourceParameters = new HashSet<SourceParameter>(0);
      
    private Set<EsiParameter> esiParameters = new HashSet<EsiParameter>(0);
      
    private Set<MassDetectorParameter> massDetectorParameters = new HashSet<MassDetectorParameter>(0);
      
    private Set<LaserParameter> laserParameters = new HashSet<LaserParameter>(0);
      
    private Set<FragmentationParameter> fragmentationParameters = new HashSet<FragmentationParameter>(0);
      
    private Set<AnalyserParameter> analyserParameters = new HashSet<AnalyserParameter>(0);
      
    private Set<TofParameter> tofParameters = new HashSet<TofParameter>(0);
      
    private Set<MaldiParameter> maldiParameters = new HashSet<MaldiParameter>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public DeviceSettings() {}

    /** Minimal constructor */
    public DeviceSettings( Acquisition acquisition, double contributorQuality ) 
    {
        this.acquisition = acquisition;
        this.contributorQuality = contributorQuality;
    }
    
    /** full constructor */
    public DeviceSettings( Acquisition acquisition, double contributorQuality, Set<IontrapParameter> iontrapParameters, Set<SourceParameter> sourceParameters, Set<EsiParameter> esiParameters, Set<MassDetectorParameter> massDetectorParameters, Set<LaserParameter> laserParameters, Set<FragmentationParameter> fragmentationParameters, Set<AnalyserParameter> analyserParameters, Set<TofParameter> tofParameters, Set<MaldiParameter> maldiParameters ) 
    {
        this.acquisition = acquisition;
        this.contributorQuality = contributorQuality;
        this.iontrapParameters = iontrapParameters;
        this.sourceParameters = sourceParameters;
        this.esiParameters = esiParameters;
        this.massDetectorParameters = massDetectorParameters;
        this.laserParameters = laserParameters;
        this.fragmentationParameters = fragmentationParameters;
        this.analyserParameters = analyserParameters;
        this.tofParameters = tofParameters;
        this.maldiParameters = maldiParameters;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getDeviceSettingsId  *//******************************** 
    *
    */ 
    public int getDeviceSettingsId() 
    {
        return this.deviceSettingsId;
    }
    
    
    /*  setDeviceSettingsId  *//******************************** 
    *
    */
    public void setDeviceSettingsId( int deviceSettingsId ) 
    {
        this.deviceSettingsId = deviceSettingsId;
    }
    

    /*  getAcquisition  *//******************************** 
    *
    */ 
    public Acquisition getAcquisition() 
    {
        return this.acquisition;
    }
    
    
    /*  setAcquisition  *//******************************** 
    *
    */
    public void setAcquisition( Acquisition acquisition ) 
    {
        this.acquisition = acquisition;
    }
    

    /*  getContributorQuality  *//******************************** 
    *
    */ 
    public double getContributorQuality() 
    {
        return this.contributorQuality;
    }
    
    
    /*  setContributorQuality  *//******************************** 
    *
    */
    public void setContributorQuality( double contributorQuality ) 
    {
        this.contributorQuality = contributorQuality;
    }
    

    /*  getIontrapParameters  *//******************************** 
    *
    */ 
    public Set<IontrapParameter> getIontrapParameters() 
    {
        return this.iontrapParameters;
    }
    
    
    /*  setIontrapParameters  *//******************************** 
    *
    */
    public void setIontrapParameters( Set<IontrapParameter> iontrapParameters ) 
    {
        this.iontrapParameters = iontrapParameters;
    }
    

    /*  getSourceParameters  *//******************************** 
    *
    */ 
    public Set<SourceParameter> getSourceParameters() 
    {
        return this.sourceParameters;
    }
    
    
    /*  setSourceParameters  *//******************************** 
    *
    */
    public void setSourceParameters( Set<SourceParameter> sourceParameters ) 
    {
        this.sourceParameters = sourceParameters;
    }
    

    /*  getEsiParameters  *//******************************** 
    *
    */ 
    public Set<EsiParameter> getEsiParameters() 
    {
        return this.esiParameters;
    }
    
    
    /*  setEsiParameters  *//******************************** 
    *
    */
    public void setEsiParameters( Set<EsiParameter> esiParameters ) 
    {
        this.esiParameters = esiParameters;
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
    

    /*  getFragmentationParameters  *//******************************** 
    *
    */ 
    public Set<FragmentationParameter> getFragmentationParameters() 
    {
        return this.fragmentationParameters;
    }
    
    
    /*  setFragmentationParameters  *//******************************** 
    *
    */
    public void setFragmentationParameters( Set<FragmentationParameter> fragmentationParameters ) 
    {
        this.fragmentationParameters = fragmentationParameters;
    }
    

    /*  getAnalyserParameters  *//******************************** 
    *
    */ 
    public Set<AnalyserParameter> getAnalyserParameters() 
    {
        return this.analyserParameters;
    }
    
    
    /*  setAnalyserParameters  *//******************************** 
    *
    */
    public void setAnalyserParameters( Set<AnalyserParameter> analyserParameters ) 
    {
        this.analyserParameters = analyserParameters;
    }
    

    /*  getTofParameters  *//******************************** 
    *
    */ 
    public Set<TofParameter> getTofParameters() 
    {
        return this.tofParameters;
    }
    
    
    /*  setTofParameters  *//******************************** 
    *
    */
    public void setTofParameters( Set<TofParameter> tofParameters ) 
    {
        this.tofParameters = tofParameters;
    }
    

    /*  getMaldiParameters  *//******************************** 
    *
    */ 
    public Set<MaldiParameter> getMaldiParameters() 
    {
        return this.maldiParameters;
    }
    
    
    /*  setMaldiParameters  *//******************************** 
    *
    */
    public void setMaldiParameters( Set<MaldiParameter> maldiParameters ) 
    {
        this.maldiParameters = maldiParameters;
    }
    
       

    







} // end class
