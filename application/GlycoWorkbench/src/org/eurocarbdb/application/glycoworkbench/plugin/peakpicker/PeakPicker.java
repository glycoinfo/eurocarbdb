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

package org.eurocarbdb.application.glycoworkbench.plugin.peakpicker;
import org.eurocarbdb.application.glycanbuilder.*;
import org.eurocarbdb.application.glycoworkbench.*;

/**
   This class is the base class for every peak picker.
     
   PeakPicker_Parameters are explained on a separate page.
*/
public class PeakPicker extends DefaultParamHandler
{

    /// Threshold for the peak height in the MS 1 level
    protected double peak_bound_;
 
    /// Threshold for the peak height in the MS 2 level
    protected double peak_bound_ms2_level_;

    /// Signal to noise threshold
    protected double signal_to_noise_;

    /// The minimal full width at half maximum
    protected double fwhm_bound_;

    //-------------


    /// Constructor
    public PeakPicker() {
    super("PeakPicker");
    setDefaults_();
    defaultsToParam_();
    }

    protected void setDefaults_() {
    defaults_.setValue("thresholds:signal_to_noise",3.0,"Minimal signal to noise ratio for a peak to be picked.");
      defaults_.setValue("thresholds:peak_bound",200.0,"Minimal peak intensity.");
      defaults_.setValue("thresholds:peak_bound_ms2_level",50.0,"Minimal peak intensity for MSMS peaks.");
      defaults_.setValue("thresholds:fwhm_bound",0.2,"Minimal peak width");
    }
    
    protected void updateMembers_()
    {
    signal_to_noise_ = (Double)param_.getValue("thresholds:signal_to_noise");
    peak_bound_ = (Double)param_.getValue("thresholds:peak_bound");
    peak_bound_ms2_level_ = (Double)param_.getValue("thresholds:peak_bound_ms2_level");
    fwhm_bound_ = (Double)param_.getValue("thresholds:fwhm_bound");
    }


    /// Non-mutable access to the threshold of the height
    public double getPeakBound()  
    { 
        return peak_bound_; 
    }

    /// Mutable access to the threshold of the height
    public void setPeakBound(double peak_bound) 
    { 
        peak_bound_ = peak_bound;
      param_.setValue("thresholds:peak_bound",peak_bound);
    }

    /// Non-mutable access to the threshold of the peak height in the MS 2 level
    public double getPeakBoundMs2Level()  
    {
        return peak_bound_ms2_level_;
    }

    /// Mutable access to the threshold of the peak height in the MS 2 level
    public void setPeakBoundMs2Level(double peak_bound_ms2_level) 
    { 
        peak_bound_ms2_level_ = peak_bound_ms2_level;
      param_.setValue("thresholds:peak_bound_ms2_level",peak_bound_ms2_level); 
    }

    /// Non-mutable access to the signal to noise threshold
    public double getSignalToNoiseLevel()  
    { 
        return signal_to_noise_; 
    }
    
    /// Mutable access to the signal to noise threshold
    public void setSignalToNoiseLevel(double signal_to_noise) 
    { 
        signal_to_noise_ = signal_to_noise;
       param_.setValue("thresholds:signal_to_noise",signal_to_noise); 
    }

    /// Non-mutable access to the fwhm threshold
    public double getFwhmBound()  
    { 
        return fwhm_bound_; 
    }
    
    /// Mutable access to the fwhm threshold
    public void setFwhmBound(double fwhm) 
    {
        fwhm_bound_ = fwhm; 
       param_.setValue("thresholds:fwhm_bound",fwhm); 
    }

        
}
