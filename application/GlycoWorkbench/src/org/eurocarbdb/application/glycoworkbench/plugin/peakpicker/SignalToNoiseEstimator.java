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
   This class represents the abstract base class of a signal to noise estimator.
        
    A signal to noise estimator should provide the signal to noise ratio of all raw data points
    in a given intervall [first_,last_).
*/
  
import java.util.*;

public abstract class SignalToNoiseEstimator extends DefaultParamHandler {
    
    protected Peak[] data_ = null;
    protected int first_ = 0;    
    protected int last_ = 0;    
    protected boolean is_result_valid_ = false;
    protected HashMap<Peak,Double> stn_estimates_ = new HashMap<Peak,Double>();
    
    // --------------
    // Accessors

    public SignalToNoiseEstimator() {
    super("SignalToNoiseEstimator");
    }
    
    /// Non-mutable access to the first raw data point
    public int getFirstDataPoint() { 
    return first_; 
    }
    
    /// Mutable access to the first raw data point
    public void setFirstDataPoint(int first) { 
    is_result_valid_=false; 
    first_ = first; 
    }

    /// Non-mutable access to the last raw data point
    public int getLastDataPoint() { 
    return last_; 
    }

    /// Mutable access to the last raw data point
    public void setLastDataPoint(int last) { 
    is_result_valid_=false; 
    last_ = last; 
    }
    
    /// Non-mutable access to the maximal intensity that is included in the histogram (higher values get discarded)
    abstract public double getMaxIntensity();

    /// Mutable access to the maximal intensity that is included in the histogram (higher values get discarded)
    abstract public void setMaxIntensity(double max_intensity);
    
    /// Non-Mutable access to the AutoMaxStdevFactor-Param, which holds a factor for stddev (only used if autoMode=1)
    abstract public double getAutoMaxStdevFactor();
    
    /// Mutable access to the AutoMaxStdevFactor-Param, which holds a factor for stddev (only used if autoMode=1)
    abstract public void setAutoMaxStdevFactor(double value);  

    /// get the AutoMaxPercentile-Param, which holds a percentile (only used if autoMode=2)
    abstract public double getAutoMaxPercentile();
    
    /// Mutable access to the AutoMaxPercentile-Param, which holds a percentile (only used if autoMode=2)
    abstract public void setAutoMaxPercentile(double value);

    /// @brief -1 will disable it. 0 is default. 1 is alternative method
    /// Non-mutable access to AutoMode, which determines the heuristic to find MaxIntensity. See Class description.
    abstract public int getAutoMode();
    
    /// @brief -1 will disable it. 0 is default. 1 is alternative method
    /// Mutable access to AutoMode, which determines the heuristic to find MaxIntensity. See Class description.
    abstract public void setAutoMode(int auto_mode);

    /// Non-mutable access to the window length (in Thomson)
    abstract public double getWinLen();

    /// Mutable access to the window length (in Thomson)
    abstract public void setWinLen(double win_len);

    /// Non-mutable access to the number of bins used for the histogram (the more bins, the better the approximation, but longer runtime)
    abstract public int getBinCount();

    /// Mutable access to the number of bins used for the histogram
    abstract public void setBinCount(int bin_count);

    /// Non-mutable access to the minimum required elements in a window, to be evaluated.
    abstract public int getMinReqElements();

    /// Mutable access to the minimum required elements in a window, to be evaluated.
    abstract public void setMinReqElements(int min_required_elements);

    /// Non-mutable access to the noise value that is used if a window contains not enough elements
    abstract public double getNoiseForEmptyWindow();
    
    /// Mutable access to the noise value that is used if a window contains not enough elements
    abstract public void setNoiseForEmptyWindow(double noise_for_empty_window);
     
    /// Set the start and endpoint of the raw data intervall, for which signal to noise ratios will be estimated immediately
    public void init(Peak[] data, int it_begin, int it_end)
    {
    data_ = data;
    first_ = it_begin;
    last_ = it_end;
    try {
        computeSTN_(data_, first_, last_);
        is_result_valid_ = true;
    }
    catch(Exception e) {
        System.err.println(e);
        is_result_valid_ = false;
    }
    }
      
          
    /// Set the start and endpoint of the raw data intervall, for which signal to noise ratios will be estimated immediately
    public void init(Peak[] data)
    {
    init(data,0,data.length); 
    }
    
    /// Return to signal/noise estimate for data point @p data_point
    /// @note the first query to this function will take longer, as
    ///       all SignalToNoise values are calculated
    /// @note you will get a warning to stderr if more than 20% of the
    ///       noise estimates used sparse windows
    public double getSignalToNoise(int data_point)
    {
    if (!is_result_valid_) { 
        // recompute ...
        init(data_,first_, last_);
    }
    return stn_estimates_.get(data_[data_point]);
    }


    // ----------------

    abstract protected  void computeSTN_(Peak[] data_, int scan_first_, int scan_last_) throws Exception;
        

    /** 
    protected struct to store parameters my, sigma for a gaussian distribution
    
      Accessors are : mean and variance      
    */ 
    protected static final class GaussianEstimate
    {
    public GaussianEstimate(double m, double v) {
        mean = m;
        variance = v;
    }

    public double mean = 0.;   ///mean of estimated gaussian
    public double variance = 0.; ///variance of estimated gaussian
    };  


    /// calculate mean & stdev of intensities of a DPeakArray
    static protected GaussianEstimate estimate_(Peak[] data_, int scan_first_, int scan_last_) {
    int size = 0;

    // add up
    double v = 0;
    double m = 0;
    int run = scan_first_;
    while (run != scan_last_) {
        m += data_[run].getIntensity();
        ++size;
        ++run;
    }
    
    //average
    m = m/size;

    //determine variance
    run = scan_first_;
    while (run != scan_last_) {
        v += Math.pow(m - data_[run].getIntensity(), 2);
        ++run;
    }
    v = v / ((double)size); // divide by n

    return new GaussianEstimate(m, v);
    }
    
}
