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
   This class implements a peak picking algorithm using wavelet techniques (as described by Lange et al. (2006) Proc. PSB-06).
     
   This peak picking algorithm uses the continuous wavelet transform of a raw data signal to detect mass peaks.
   Afterwards a given asymmetric peak function is fitted to the raw data and important peak parameters (e.g. fwhm)
   are extracted.
   In an optional step these parameters can be optimized using a non-linear opimization method.
   
   PeakPickerCWT_Parameters are explained on a separate page.
   
*/

import java.util.*;
 
public class PeakPickerCWT extends PeakPicker
{

    /** 
    Class for the internal peak representation        
        A regularData-Object which contents some additional useful informations
        for analysing peaks and their properties
    */
    private class PeakArea_
    {

    public PeakArea_() {
        left = 0;
        max = 0;
        right = 0;
        left_behind_centroid = 0;
    }

    /** 
        Iterator defining a raw data peak.
        
        The left and right iterators delimit a range in the raw data which represents a raw peak.
        They define the raw peak endpoints. Max points to the raw data point in [left, right] with the highest intensity, the 
        maximum of the raw peak. 
        
        Left_behind_centroid points to the raw data point next to the estimates centroid position.
    */
    public int left;
    public int max;
    public int right;
    public int left_behind_centroid;
    
    /// The estimated centroid position.
    public double centroid_position;
    }


    /// Container the determined peak shapes
    protected Vector<PeakShape> peak_shapes_;

    /// The continuous wavelet "transformer"
    protected ContinuousWaveletTransformNumIntegration wt_;

    /// The continuous wavelet "transformer" for the deconvolution
    protected ContinuousWaveletTransformNumIntegration wtDC_;

    /// The search radius for the determination of a peak's maximum position
    protected int radius_;

    /// The dilation of the wavelet
    protected double scale_;

    /// The minimal height which defines a peak in the CWT (MS 1 level)
    protected double peak_bound_cwt_;

    /// The minimal height which defines a peak in the CWT (MS 2 level)
    protected double peak_bound_ms2_level_cwt_;

    /// The threshold for correlation
    protected double peak_corr_bound_;

    /// The threshold for the noise level (TODO: Use the information of the signal to noise estimator)
    protected double noise_level_;

    /// Switch for the optimization of peak parameters
    protected boolean optimization_;

    /// Switch for the deconvolution of peak parameters
    protected boolean deconvolution_;

    /// Switch for the 2D optimization of peak parameters
    protected boolean two_d_optimization_;

    /// Constructor
    public PeakPickerCWT() {
    super();
    
    /*radius_ = 0;
    scale_ = 0.0;
    peak_bound_cwt_ = 0.0;
    peak_bound_ms2_level_cwt_ = 0.0;
    peak_corr_bound_ = 0.0;
    noise_level_ = 0.0;
    optimization_ = false;*/

    peak_shapes_ = new Vector<PeakShape>();
    
    wt_ = new ContinuousWaveletTransformNumIntegration();
    wtDC_ = new ContinuousWaveletTransformNumIntegration();
    }

    protected void setDefaults_() {
      
    super.setDefaults_();
    
    // if a peak picking parameter is missed in the param object the value should be substituted by a default value
      defaults_.setValue("thresholds:correlation",0.5,"minimal correlation of a peak and the raw signal. " +
               "If a peak has a lower correlation it is skipped.");
      defaults_.setValue("wavelet_transform:scale",0.15,"Width of the used wavelet. "    +
               "Should correspond approx. to the fwhm of the peaks.");
      defaults_.setValue("wavelet_transform:spacing",0.001,"spacing of the cwt.");
      defaults_.setValue("thresholds:noise_level",0.1,"noise level for the search of the peak endpoints.");
       defaults_.setValue("thresholds:search_radius",3,"search radius for the search of the maximum in the signal after a maximum in the cwt was found");     
    defaults_.setValue("thresholds:signal_to_noise",2.,"minimal signal to noise value." +
               "If a peak has a s/n value it is skipped.");
        
    //Optimization parameters
      defaults_.setValue("Optimization:optimization","no","If the peak parameters position, intensity and left/right width" +
               "shall be optimized set optimization to yes.");
    defaults_.setValue("Optimization:penalties:position",0.0,"penalty term for the fitting of the position:" +
               "If it differs too much from the initial one it can be penalized ");
    defaults_.setValue("Optimization:penalties:left_width",1.0,"penalty term for the fitting of the left width:" +
               "If the left width differs too much from the initial one during the fitting it can be penalized.");     
    defaults_.setValue("Optimization:penalties:right_width",1.0,"penalty term for the fitting of the right width:" +
               "If the right width differs too much from the initial one during the fitting it can be penalized.");     
    defaults_.setValue("Optimization:iterations",15,"maximal number of iterations for the fitting step");     
    defaults_.setValue("Optimization:delta_abs_error",1e-04,"if the absolute error gets smaller than this value the fitting is stopped.");     
    defaults_.setValue("Optimization:delta_rel_error",1e-04,"if the relative error gets smaller than this value the fitting is stopped");

    // deconvolution parameters
    defaults_.setValue("deconvolution:skip_deconvolution","yes","If you want heavily overlapping peaks to be separated set this value to \"no\"");
    defaults_.setValue("deconvolution:asym_threshold",0.3,"If the symmetry of a peak is smaller than asym_thresholds it is assumed that it consists of more than one peak and the deconvolution procedure is started.");
    defaults_.setValue("deconvolution:left_width",2,"1/left_width is the initial value for the left width of the peaks found in the deconvolution step.");
    defaults_.setValue("deconvolution:right_width",2,"1/right_width is the initial value for the right width of the peaks found in the deconvolution step.");
    defaults_.setValue("deconvolution:scaling",0.12,"Initial scaling of the cwt used in the seperation of heavily overlapping peaks. The initial value is used for charge 1, for higher charges it is adapted to scaling/charge.");
    defaults_.setValue("deconvolution:fitting:penalties:position",0.0,"penalty term for the fitting of the peak position:" +
               "If the position changes more than 0.5Da during the fitting it can be penalized as well as "    +
               "discrepancies of the peptide mass rule.");
    defaults_.setValue("deconvolution:fitting:penalties:height",1.0,"penalty term for the fitting of the intensity:" +
               "If it gets negative during the fitting it can be penalized.");
    defaults_.setValue("deconvolution:fitting:penalties:left_width",0.0,"penalty term for the fitting of the left width:" +
               "If the left width gets too broad or negative during the fitting it can be penalized.");
    defaults_.setValue("deconvolution:fitting:penalties:right_width",0.0,"penalty term for the fitting of the right width:"    +
               "If the right width gets too broad or negative during the fitting it can be penalized.");
    defaults_.setValue("deconvolution:fitting:fwhm_threshold",0.7,"If the fwhm of a peak is higher than fwhm_thresholds it is assumed that it consists of more than one peak and the deconvolution procedure is started.");
    defaults_.setValue("deconvolution:fitting:eps_abs",1e-05,"if the absolute error gets smaller than this value the fitting is stopped.");
    defaults_.setValue("deconvolution:fitting:eps_rel",1e-05,"if the relative error gets smaller than this value the fitting is stopped.");
    defaults_.setValue("deconvolution:fitting:max_iteration",10,"maximal number of iterations for the fitting step");
    
    defaults_.setValue("WinLen", 200.0, "window length in Thomson"); 

    subsections_.add("SignalToNoiseEstimationParameter");
    subsections_.add("2D_optimization");
    
    }

    protected void updateMembers_() {
    super.updateMembers_();
    
    signal_to_noise_ = (Double)param_.getValue("thresholds:signal_to_noise");
    peak_bound_ = (Double)param_.getValue("thresholds:peak_bound");
    peak_bound_ms2_level_ = (Double)param_.getValue("thresholds:peak_bound_ms2_level");
    fwhm_bound_ = (Double)param_.getValue("thresholds:fwhm_bound");
    peak_corr_bound_ = (Double)param_.getValue("thresholds:correlation");
    String opt = param_.getValue("Optimization:optimization").toString();
    if (opt=="one_dimensional") {
        optimization_ = true;
        two_d_optimization_ = false;
    }
    else if (opt=="two_dimensional") {
        two_d_optimization_ = true;
        optimization_ = false;
    }
    else {
        optimization_ = false;
        two_d_optimization_ = false;        
    }
    scale_ = (Double)param_.getValue("wavelet_transform:scale");
    noise_level_ = (Double)param_.getValue("thresholds:noise_level");
    radius_ = (Integer)param_.getValue("thresholds:search_radius");
    signal_to_noise_ = (Double)param_.getValue("thresholds:signal_to_noise");
    opt = param_.getValue("deconvolution:skip_deconvolution").toString();
    if (opt.equals("yes")) {
        deconvolution_ = false;
    }
    else if (opt.equals("no")) {
        deconvolution_ = true;
    }
    else {
        System.err.println("Warning: PeakPickerCWT option 'deconvolution:skip_deconvolution' should be 'yes' or 'no'!"
                   + " It is set to '" + opt + "'");
    }
    }
    
   
    /** Accessor methods
     */

    /// Non-mutable access to the vector of peak shapes
    public Vector<PeakShape> getPeakShapes() 
    {
    return peak_shapes_;
    }

    /// Non-mutable access to the wavelet transform
    public ContinuousWaveletTransformNumIntegration getWaveletTransform() 
    {
    return wt_;
    }

    /// Non-mutable access to the search radius for the peak maximum
    public int getSearchRadius() 
    {
    return radius_;
    }
    
    /// Mutable access to the search radius for the peak maximum
    public void setSearchRadius(int radius)
    {
    radius_ = radius;
    param_.setValue("thresholds:search_radius",radius);
    }

    /// Non-mutable access to the scale of the wavelet transform
    public double getWaveletScale() 
    {
    return scale_;
    }

    /// Mutable access to the scale of the wavelet transform
    public void setWaveletScale(double scale)
    {
    scale_ = scale;
    param_.setValue("wavelet_transform:scale",scale);
    }

    /// Non-mutable access to the threshold of the height
    public double getPeakBound() 
    {
    return peak_bound_;
    }

    /// Non-mutable access to the peak bound in the wavelet transform for the MS 1 level
    public double getPeakBoundCWT() 
    {
    return peak_bound_cwt_;
    }

    /// Non-mutable access to the peak bound in the wavelet transform for the MS 2 level
    public double getPeakBoundMs2LevelCWT() 
    {
    return peak_bound_ms2_level_cwt_;
    }

    /// Non-mutable access to the minimum peak correlation coefficient
    public double getPeakCorrBound() 
    {
    return peak_corr_bound_;
    }

    /// Mutable access to the minimum peak correlation coefficient
    public void setPeakCorrBound(double peak_corr_bound)
    {
    peak_corr_bound_ = peak_corr_bound;
    param_.setValue("thresholds:correlation",peak_corr_bound);
    }

    /// Non-mutable access to the noise level
    public double getNoiseLevel() 
    {
    return noise_level_;
    }
    
    /// Mutable access to the noise level
    public void setNoiseLevel(double noise_level)
    {
    noise_level_ = noise_level;    
    param_.setValue("thresholds:noise_level",noise_level);
    }

    /// Non-mutable access to the optimization switch
    public  boolean getOptimizationFlag() 
    {
      return optimization_;
    }

    /// Mutable access to the optimization switch
    public void setOptimizationFlag( boolean optimization)
    {
    optimization_ = optimization;
    if (optimization)        
        param_.setValue("Optimization:optimization","one_dimensional");      
    else      
        param_.setValue("Optimization:optimization","no");      
    }

    /// Non-mutable access to the deconvolution switch
    public  boolean getDeconvolutionFlag() 
    {
    return deconvolution_;
    }

    /// Mutable access to the deconvolution switch
    public void setDeconvolutionFlag( boolean deconvolution)
    {
    deconvolution_ = deconvolution;
    if (deconvolution)      
        param_.setValue("deconvolution:skip_deconvolution","no");      
    else      
        param_.setValue("deconvolution:skip_deconvolution","yes");      
    }

    /// Non-mutable access to the optimization switch
    public  boolean get2DOptimizationFlag() 
    {
    return two_d_optimization_;
    }
    
    /// Mutable access to the optimization switch
    public void set2DOptimizationFlag( boolean two_d_optimization)
    {
    two_d_optimization_ = two_d_optimization;
    if (two_d_optimization)      
        param_.setValue("Optimization:optimization","two_dimensional");      
    }

    /// Non-mutable access to the window length (in Thomson)
    public double getWinLen() {
    return (Double)param_.getValue("WinLen");
    }
    
    /// Mutable access to the window length (in Thomson)
    public void setWinLen(double win_len)
    {
    param_.setValue("WinLen", win_len);      
    }

    public double getWaveletSpacing() {
    return (Double)param_.getValue("wavelet_transform:spacing");
    }
    
    /// Mutable access to the window length (in Thomson)
    public void setWaveletSpacing(double spacing)
    {
    param_.setValue("wavelet_transform:spacing", spacing);      
    }

    // ------

    /** 
    Applies the peak picking algorithm to an given iterator range.
        
    Picks the peaks in the given iterator intervall [first,last) and writes the
    resulting peaks to the picked_peak_container.
        The ms_level should be one if the spectrum is a normal mass spectrum, or two if it is a tandem mass spectrum.
        
    This method assumes that the InputPeakIterator (e.g. of type MSSpectrum<RawDataPoint1D >::const_iterator)
    points to a data point of type RawDataPoint1D or any other class derived from RawDataPoint1D.
        
    The resulting peaks in the picked_peak_container (e.g. of type MSSpectrum<PickedPeak1D>)
    can be of type RawDataPoint1D or any other class derived from DRawDataPoint. 
    We recommend to use the PickedPeak1Dbecause it stores important information gained during
    the peak picking algorithm.
        
    If you use MSSpectrum iterators you have to set the SpectrumSettings on your own.              
    */

    public Vector<Peak> pick(double[][] data, int ms_level) throws Exception  {
    return pick(data,0,data[0].length,ms_level);
    }

    public Vector<Peak> pick(double[][] data) throws Exception   {
    return pick(data,0,data[0].length,1);
    }

    public Vector<Peak> pick(double[][] data, int first, int last) throws Exception   {
    return pick(data,first,last,1);
    }

    public Vector<Peak> pick(double[][] data, int first, int last, int ms_level) throws Exception  {
    
    if( deconvolution_ == true )
        throw new Exception("deconvolution is not yet supported");

    Vector<Peak> picked_peak_container = new Vector<Peak>();
        
    if (peak_bound_cwt_==0.0 || peak_bound_ms2_level_cwt_==0.0) {
        initializeWT_();
    }

    // empty spectra shouldn't be picked
    if(first == last)
        return picked_peak_container;    
    else {
        // only one "peak"
        if (last - first == 1) {
        // @todo handle spectra with only one raw data point (seg. faulted) (Chris Bielow)
        return picked_peak_container;
        }
    }

    //clear the peak shapes vector
    peak_shapes_.clear();

    // vector of peak endpoint positions
    Vector<Double> peak_endpoints = new Vector<Double>();

    // signal to noise estimator
    SignalToNoiseEstimatorMedian sne = new SignalToNoiseEstimatorMedian();
    Param sne_param = param_.copy("SignalToNoiseEstimationParameter:",true);
    if(sne_param.empty()) 
        sne.setParameters(new Param());
    else 
        sne.setParameters(sne_param);

    // copy the raw data into a DPeakArray<DRawDataPoint<D> >
    int n = last-first;
    Peak[] raw_peak_array = new Peak[n];
    for (int i = 0; i < n; ++i) {        
        raw_peak_array[i] = new Peak();
        raw_peak_array[i].setIntensity(data[1][first + i]);
        raw_peak_array[i].setMZ(data[0][first + i]);
    }


    int it_pick_begin = 0;
    int it_pick_end   = n;
    if(it_pick_begin == it_pick_end)
        return picked_peak_container;

    sne.init(raw_peak_array,it_pick_begin,it_pick_end);
    
    // thresholds for deconvolution
    double fwhm_threshold = (Double)param_.getValue("deconvolution:fitting:fwhm_threshold");
    double symm_threshold = (Double)param_.getValue("deconvolution:asym_threshold");

    // Points to the actual maximum position in the raw data
    int it_max_pos;

    // start the peak picking until no more maxima can be found in the wavelet transform
    int number_of_peaks = 0;

    do {
        number_of_peaks = 0;
        Pair<Integer,Integer> peak_indexes = new Pair<Integer,Integer>();

        // compute the continious wavelet transform with resolution 1
        double resolution = 1.;
        wt_.transform(raw_peak_array,it_pick_begin,it_pick_end,resolution);

        PeakArea_ area = new PeakArea_();
        boolean centroid_fit=false;
        boolean regular_endpoints=true;

        // search for maximum positions in the cwt and extract potential peaks
        int direction=1;
        int distance_from_scan_border = 0;
        while( (it_pick_end - it_pick_begin) > 3
           && getMaxPosition_(raw_peak_array,
                      it_pick_begin,
                      it_pick_end,
                      wt_,
                      area,
                      distance_from_scan_border,
                      ms_level,
                      direction)) {
        // if the signal to noise ratio at the max position is too small
        // the peak isn't considered

        if((area.max  != it_pick_end) && (sne.getSignalToNoise(area.max) < signal_to_noise_) ) {
            it_pick_begin = area.max;
            distance_from_scan_border = it_pick_begin;
            continue;
        }
        else if(area.max >= it_pick_end) 
            break;

        //search for the endpoints of the peak
        regular_endpoints = getPeakEndPoints_(raw_peak_array,
                              it_pick_begin,
                              it_pick_end,
                              area,
                              distance_from_scan_border,
                              peak_indexes);

        // compute the centroid position
        getPeakCentroid_(raw_peak_array,area);

        // if the peak achieves a minimal width, start the peak fitting
        if (regular_endpoints) {

            // determine the best fitting lorezian or sech2 function
            PeakShape shape = fitPeakShape_(raw_peak_array,area,centroid_fit);

            // Use the centroid for Optimization
            shape.mz_position=area.centroid_position;

            if ( (shape.r_value > peak_corr_bound_)
             && (shape.getFWHM() >= fwhm_bound_)) {
            shape.signal_to_noise = sne.getSignalToNoise(area.max);
            // if peak is too broad or asymmetric it needs to be deconvoluted
            if( deconvolution_  &&
                ((shape.getFWHM() > fwhm_threshold) ||
                 (shape.getSymmetricMeasure() < symm_threshold)) ) {
                deconvolutePeak_(raw_peak_array, shape, area,peak_endpoints);
            }
            else {            
                peak_shapes_.add(shape);
                peak_endpoints.add(raw_peak_array[area.left].getMZ());
                peak_endpoints.add(raw_peak_array[area.right].getMZ());
            }
            ++number_of_peaks;
            }            
        }

        // remove the peak from the signal
        // TODO: does this work as expected???
        for (int pi=area.left; pi!=area.right+1; pi++) {
            raw_peak_array[pi].setIntensity(0.);
        }

        // search for the next peak
        it_pick_begin = area.right;
        distance_from_scan_border = it_pick_begin;

        } //end while (getMaxPosition_(it_pick_begin, it_pick_end, wt_, area, distance_from_scan_border, ms_level, direction))
        it_pick_begin = 0;
    }
    while (number_of_peaks != 0);

    // start the nonlinear optimization for all peaks in split
    if (peak_shapes_.size() > 0) {

        // write the picked peaks to the outputcontainer
        for (int i = 0; i < peak_shapes_.size(); ++i) {
        Peak picked_peak = new Peak();
        picked_peak.setIntensity(peak_shapes_.get(i).height);
        picked_peak.setMZ(peak_shapes_.get(i).mz_position);
        
        fillPeak(peak_shapes_.get(i),picked_peak);
        picked_peak_container.add(picked_peak);
        }
    } // if (peak_shapes_.size() > 0)

    return picked_peak_container;
    }    

    
    private void fillPeak(PeakShape peak_shape, Peak picked_peak) {
    /*
    picked_peak.setRValue(peak_shape.r_value);
    picked_peak.setArea(peak_shape.area);
    picked_peak.setFWHM(peak_shape.getFWHM());
    picked_peak.setLeftWidthParameter(peak_shape.left_width);
    picked_peak.setRightWidthParameter(peak_shape.right_width);
    picked_peak.setPeakShape(peak_shape.type);
    picked_peak.setSN(peak_shape.signal_to_noise);
    */
    }    
 
   
    /// Computes the peak's left and right area
    private void getPeakArea_( Peak[] data, PeakArea_ area, Pair<Double,Double> area_ret ) {
    double area_left = area_ret.getFirst();
    double area_right = area_ret.getFirst();
    
    area_left += data[area.left].getIntensity() * (data[area.left+1].getMZ() - data[area.left].getMZ()) * 0.5;
    area_left += data[area.max].getIntensity() *  (data[area.max].getMZ() - data[area.max-1].getMZ()) * 0.5;

    for (int pi=area.left+1; pi<area.max; pi++) {
        double step = (data[pi].getMZ() - data[pi-1].getMZ());
        area_left += step * data[pi].getIntensity();
    }

    area_right += data[area.right].getIntensity() * (data[area.right].getMZ() - data[area.right-1].getMZ()) * 0.5;
    area_right += data[area.max+1].getIntensity() *  (data[area.max+2].getMZ() - data[area.max+1].getMZ()) * 0.5;

    for (int pi=area.max+2; pi<area.right; pi++) {
        double step = (data[pi].getMZ() - data[pi-1].getMZ());
        area_right += step * data[pi].getIntensity();
    }
    }

    /// Returns the best fitting peakshape

    private PeakShape fitPeakShape_( Peak[] data, PeakArea_ area, boolean enable_centroid_fit) {

    double max_intensity   =   data[area.max].getIntensity();
    double left_intensity  =  data[area.left].getIntensity();
    double right_intensity = data[area.right].getIntensity();

    if (enable_centroid_fit) {
        //avoid zero width
        double minimal_endpoint_centroid_distance=0.01;
        if (  (Math.abs( data[area.left].getMZ()-area.centroid_position) < minimal_endpoint_centroid_distance)
          ||(Math.abs(data[area.right].getMZ()-area.centroid_position) < minimal_endpoint_centroid_distance) ) {
        return new PeakShape();
        }
        
        // the maximal position was taken directly from the cwt.
        // first we do a "regular" fit of the left half
        // TODO: avoid zero width!

        // lorentzian fit

        // estimate the width parameter of the left peak side
        int left_it=area.left_behind_centroid;
        double x0=area.centroid_position;
        double l_sqrd=0.;
        int n=0;
        while(left_it-1 >= area.left) {
        double x1=data[left_it].getMZ();
        double x2=data[left_it-1].getMZ();
        double c=data[left_it-1].getIntensity()/data[left_it].getIntensity();
        l_sqrd+=(1-c)/(c*(Math.pow((x2-x0),2))-Math.pow((x1-x0),2));
        --left_it;
        ++n;
        }

        double left_heigth=data[area.left_behind_centroid].getIntensity()/(1+l_sqrd*Math.pow(data[area.left_behind_centroid].getMZ()-area.centroid_position,2));

        // estimate the width parameter of the right peak side
        int right_it=area.left_behind_centroid+1;
        l_sqrd=0.;
        n=0;
        while(right_it+1 <= area.right) {
        double x1=data[right_it].getMZ();
        double x2=data[right_it+1].getMZ();
        double c=data[right_it+1].getIntensity()/data[right_it].getIntensity();
        l_sqrd+=(1-c)/(c*(Math.pow((x1-x0),2))-Math.pow((x2-x0),2));
        ++right_it;
        ++n;
        }

        //estimate the heigth
        double right_heigth=data[area.left_behind_centroid+1].getIntensity()/(1+l_sqrd*Math.pow(data[area.left_behind_centroid+1].getMZ()-area.centroid_position,2));
        
        double height=Math.min(left_heigth,right_heigth);

        // compute the left and right area
        double peak_area_left = 0.;
        peak_area_left += data[area.left].getIntensity() * (data[area.left+1].getMZ() - data[area.left].getMZ()) * 0.5;
        peak_area_left += height * (area.centroid_position-data[area.left_behind_centroid].getMZ()) * 0.5;

        for (int pi=area.left+1; pi <= area.left_behind_centroid; pi++) {
        double step = (data[pi].getMZ() - data[pi-1].getMZ());
        peak_area_left += step * data[pi].getIntensity();
        }

        double peak_area_right = 0.;
        peak_area_right += data[area.right].getIntensity() * (data[area.right].getMZ() - data[area.right-1].getMZ()) * 0.5;
        peak_area_right += height * (data[area.left_behind_centroid+1].getMZ()-area.centroid_position) * 0.5;
        
        for (int pi=area.left_behind_centroid+1; pi < area.right; pi++) {
        double step = (data[pi].getMZ() - data[pi-1].getMZ());
        peak_area_right += step * data[pi].getIntensity();
        }

        double left_width =    height/peak_area_left * Math.atan( Math.sqrt( height/data[area.left].getIntensity() - 1. ) );
        double right_width =  height/peak_area_right * Math.atan( Math.sqrt( height/data[area.right].getIntensity() - 1. ) );

        
        // TODO: test different heights; recompute widths; compute area
        PeakShape lorentz = new PeakShape(height, area.centroid_position, left_width, right_width,
                          peak_area_left + peak_area_right, PeakShape.Type.LORENTZ_PEAK);

        lorentz.r_value = correlate_(data, lorentz, area);

        return lorentz;
    }

    else { 
        // no fitting on centroids
        
        // determine the left half of the area of the PeakArea_...
        double peak_area_left = 0.;
        peak_area_left += data[area.left].getIntensity() * (data[area.left+1].getMZ() - data[area.left].getMZ()) * 0.5;
        peak_area_left += data[area.max].getIntensity() *  (data[area.max].getMZ() - data[area.max-1].getMZ()) * 0.5;

        for (int pi=area.left+1; pi<area.max; pi++) {
        double step = (data[pi].getMZ() - data[pi-1].getMZ());
        peak_area_left += step * data[pi].getIntensity();
        }

        double peak_area_right = 0.;
        peak_area_right += data[area.right].getIntensity() * (data[area.right].getMZ() - data[area.right-1].getMZ()) * 0.5;
        peak_area_right += data[area.max].getIntensity() *  (data[area.max+1].getMZ() - data[area.max].getMZ()) * 0.5;

        for (int pi=area.max+1; pi<area.right; pi++) {
        double step = (data[pi].getMZ() - data[pi-1].getMZ());
        peak_area_right += step * data[pi].getIntensity();
        }

        // first the lorentz-peak...
        
        double left_width = max_intensity / peak_area_left * Math.atan(Math.sqrt(max_intensity / left_intensity - 1.));
        double right_width = max_intensity / peak_area_right * Math.atan(Math.sqrt(max_intensity / right_intensity - 1.));
        
        
        
        PeakShape lorentz = new PeakShape(max_intensity, data[area.max].getMZ(),
                          left_width, right_width, peak_area_left + peak_area_right,
                          PeakShape.Type.LORENTZ_PEAK);
        
        lorentz.r_value = correlate_(data, lorentz, area);
        
        // now the sech-peak...
        left_width  = max_intensity /peak_area_left * Math.sqrt(1. - left_intensity / max_intensity);
        right_width  = max_intensity /peak_area_right * Math.sqrt(1. - right_intensity / max_intensity);


        PeakShape sech = new PeakShape(max_intensity, data[area.max].getMZ(),
                       left_width, right_width,
                       peak_area_left + peak_area_right,
                       PeakShape.Type.SECH_PEAK);

        sech.r_value = correlate_(data, sech, area);

        if ((lorentz.r_value > sech.r_value) && Double.isNaN(sech.r_value))        
        return lorentz;    
        else        
        return sech;    
    }
    } 

    /** 
    Returns the squared pearson coefficient.

        Computes the correlation of the peak and the original data given by the peak enpoints area.left and area.right.
        If the value is near 1, the fitted peakshape and the raw data are expected to be very similar. 
    */

    private double correlate_( Peak[] data, PeakShape peak,  PeakArea_ area)  {
    return correlate_(data,peak,area,0);
    }

    private double correlate_( Peak[] data, PeakShape peak,  PeakArea_ area, int direction)  {
    
    double SSxx = 0., SSyy = 0., SSxy = 0.;
    
    // compute the averages
    double data_average=0., fit_average=0.;
    double data_sqr=0., fit_sqr=0.;
    double cross=0.;
    
    int number_of_points = 0;
    int corr_begin=area.left;
    int corr_end=area.right;
    
    // for separate overlapping peak correlate until the max position...
    if (direction > 0)
        corr_end=area.max;
    else if (direction < 0)
        corr_begin=area.max;

    for (int pi = corr_begin; pi<=corr_end; pi++){
        double data_val = data[pi].getIntensity();
        double peak_val = peak.get(data[pi].getMZ());

        data_average += data_val;
        fit_average  += peak_val;
        
        data_sqr += data_val * data_val;
        fit_sqr  += peak_val * peak_val;

        cross += data_val * peak_val;
        
        number_of_points++;
    }

    if (number_of_points == 0)
        return 0.;

    data_average /= number_of_points;
    fit_average  /= number_of_points;

    SSxx = data_sqr - number_of_points * (data_average * data_average);
    SSyy = fit_sqr - number_of_points * (fit_average * fit_average);
    SSxy = cross - number_of_points * (data_average * fit_average);

    return (SSxy * SSxy) / (SSxx * SSyy);
    }



    /** 
    Finds the next maximum position in the wavelet transform wt.
        
        If the maximum is greater than peak_bound_cwt we search for the corresponding maximum in the raw data interval [first,last)
      given a predefined search radius radius. Only peaks with intensities greater than peak_bound_ 
      are relevant. If no peak is detected the method return false.
      For direction=1, the method runs from first to last given direction=-1 it runs the other way around.
    */
    private boolean getMaxPosition_(Peak[] data, int first, int last,  ContinuousWaveletTransform wt, PeakArea_ area, int distance_from_scan_border, int ms_level) {
    return getMaxPosition_(data,first,last,wt,area,distance_from_scan_border,ms_level,1);
    }

    private boolean getMaxPosition_(Peak[] data, int first, int last,  ContinuousWaveletTransform wt, PeakArea_ area, int distance_from_scan_border, int ms_level, int direction) {

    // ATTENTION! It is assumed that the resolution==1 (no resolution higher than 1).
    // Comment: Who cares ??
    double noise_level=0.;
    double noise_level_cwt=0.;
    if (ms_level==1) {
        noise_level = peak_bound_;
        noise_level_cwt = peak_bound_cwt_;
    }
    else {
        noise_level = peak_bound_ms2_level_;
        noise_level_cwt = peak_bound_ms2_level_cwt_;
    }

    int zeros_left_index  = wt.getLeftPaddingIndex();
    int zeros_right_index = wt.getRightPaddingIndex();

    // Points to most intensive data point in the signal
    int it_max_pos;
    double max_value;

    // Given direction, start the search from left or right
    int start = (direction > 0) ? ((zeros_left_index + 2) + distance_from_scan_border) : ((zeros_right_index - 2) - distance_from_scan_border) ;
    int end   = (direction > 0) ? (zeros_right_index - 1)  : zeros_left_index+1;
    
    int i=0, j=0, k, max_pos;
    for(i=start, k=0; i!=end; i+=direction, ++k) {
        // Check for maximum in cwt at position i
        if(((wt.get(i-1) - wt.get(i)  ) < 0)
           && ((wt.get(i) - wt.get(i+1)) > 0)
           && ( wt.get(i)  >  noise_level_cwt)) {
        max_pos = (direction > 0) ? (i - distance_from_scan_border)  : i;
        if(first+max_pos < first ||first +max_pos >=last ) 
            break;

        max_value=data[first + max_pos].getIntensity();


        // search for the corresponding maximum in the signal (consider the radius left and right adjacent points)
        int start_intervall = ((max_pos - (int)radius_) < 0 ) ? 0 : (max_pos - (int)radius_);
        int end_intervall= ((max_pos + (int)radius_) >= (last-first)) ? 0 : (max_pos + (int)radius_);

        for(j = start_intervall; j <= end_intervall; ++j) {
            if(data[first + j].getIntensity() > max_value) {
            max_pos = j;
            max_value = data[first + j].getIntensity();
            }
        }

        // if the maximum position is high enough and isn't one of the border points, we return it
        if((data[first + max_pos].getIntensity() >= noise_level)
           && (((first + max_pos) != first)
               && (first + max_pos)   != (last-1))) {
            area.max = first + max_pos;
            return true;
        }

        }
    }

    // No relevant peak was found
    return false;
    }


    /** 
    Determines a peaks's endpoints.
      
      The algorithm does the following:
          - let x_m be the position of the maximum in the data and let (x_l, x_r) be
            the left and right neighbours
      
      
        (1) starting from x_l', walk left until one of the following happens
               - the new point is lower than the original bound
                    => we found our left endpoint
      
               - the new point is larger than the last, but the point left from
                 the new point is smaller. In that case, we either ran into another
                 peak, or we encounter some noise. Therefore we now look in the cwt
                 at the position corresponding to this value. If the cwt here is
                 monotonous, we consider the point as noise and continue further to the
                 left. Otherwise, we probably found the beginning of a new peak and
                 therefore stop here.
      
        (2) analogous procedure to the right of x_r
    */
    private boolean getPeakEndPoints_(Peak[] data, int first, int last,  PeakArea_ area, int distance_from_scan_border, Pair<Integer,Integer> peak_indexes) {
  
    // the Maximum may neither be the first or last point in the signal
    if ((area.max <= first) || (area.max >= last-1))        
        return false;
    
    int it_help=area.max-1;
    double vec_pos;
    int cwt_pos;
    int ep_radius=2;
    int start;
    int stop;
    boolean monoton;

    int zeros_left_index  = wt_.getLeftPaddingIndex();

    // search for the left endpoint
    while (((it_help-1) > first) && (data[it_help].getIntensity() > noise_level_)) {
    
        // if the values are still falling to the left, everything is ok.
        if (data[it_help-1].getIntensity() < data[it_help].getIntensity()) {
        --it_help;        
        }
        // if the values are _rising_, we have to check the cwt
        else {
        if ((it_help-2) <= first) {
            break;
        }
        // now check the value to the left of the problematic value
        if (data[it_help-2].getIntensity() > data[it_help-1].getIntensity()) {
            // we probably ran into another peak
            break;
        }


        // to the left, the values are falling again => let the cwt decide if we
        // are seeing a new peak or just noise

        // compute the position of the corresponding point in the cwt
        cwt_pos = it_help-first;
        vec_pos = data[it_help].getMZ();

        // since the cwt is pretty smooth usually, we consider the point as noise
        // if the cwt is monotonous in this region
        // TODO: better monotonicity test... say two or three points more
        monoton=true;

        start   =   (cwt_pos < ep_radius)
            ? (distance_from_scan_border + zeros_left_index) + 2
            : cwt_pos - ep_radius + (distance_from_scan_border + zeros_left_index + 2);
        stop    =   ((cwt_pos + ep_radius) > (last-it_help))
            ?  (wt_.getSize() - 2)
            : cwt_pos + ep_radius + (distance_from_scan_border + zeros_left_index + 2);

        for (; start < stop; ++start) {
            if (   (wt_.get(start-1) - wt_.get(start)  )
               * (wt_.get(start)   - wt_.get(start+1)) < 0 ) {
            // different slopes at the sides => stop here
            monoton=false;
            break;
            }
        }
        
        if (!monoton)  {
            break;
        }
        --it_help;
        }
    }
    area.left=it_help;

    it_help=area.max+1;
    // search for the right endpoint ???
    while (((it_help+1) < last) && (data[it_help].getIntensity() > noise_level_)) {
        //      if the values are still falling to the right, everything is ok.
        if (data[it_help].getIntensity() > data[it_help+1].getIntensity()) {
        ++it_help;
        }
        // if the values are _rising_, we have to check the cwt
        else {
        if ((it_help+2) >= last) {
            break;
        }
        // now check the value to the right of the problematic value
        if (data[it_help+2].getIntensity() > data[it_help+1].getIntensity()) {
            // we probably ran into another peak
            break;
        }
        
        // to the left, the values are falling again => let the cwt decide if we
        // are seeing a new peak or just noise
        // compute the position of the corresponding point in the cwt
        cwt_pos = it_help - first;
        //cwt_pos = distance(first, it_help);
        vec_pos=data[it_help].getMZ();

        // since the cwt is pretty smooth usually, we consider the point as noise
        // if the cwt is monotonous in this region
        // TODO: better monotonicity test... say two or three points more
        monoton = true;

        start   =   (cwt_pos < ep_radius)
            ? (distance_from_scan_border + zeros_left_index) + 2
            : cwt_pos - ep_radius + (distance_from_scan_border + zeros_left_index + 2);
        stop    =   ((cwt_pos + ep_radius) > (last-it_help))
            ?  (wt_.getSize() - 2)
            : cwt_pos + ep_radius + (distance_from_scan_border + zeros_left_index + 2);
        
        for (; start < stop; ++start) {
            if (   (wt_.get(start-1) - wt_.get(start))
               * (wt_.get(start)  - wt_.get(start+1)) < 0 )    {
            // different slopes at the sides => stop here
            monoton=false;
            break;
            }
        }
        
        if (!monoton) {
            break;
        }
        ++it_help;
        }
    }
    area.right=it_help;

    peak_indexes.setFirst(area.left-first);
    peak_indexes.setSecond(area.right-first);

    // The minimal raw data points per peak should be 2
    if (((area.max-area.left) > 0) && ((area.right-area.max) > 0)) {
        return true;
    }
    return false;
    }

    /** 
    Estimates a peak's centroid position.

       Computes the centroid position of the peak using all raw data points which are greater than 
       60% of the most intensive raw data point.
    */
    private void getPeakCentroid_(Peak[] data, PeakArea_ area) {
    
    int left_it=area.max-1, right_it=area.max;
    double max_intensity=data[area.max].getIntensity();
    double rel_peak_height=max_intensity*0.6;
    double sum=0., w=0.;
    area.centroid_position=data[area.max].getMZ();

    // compute the centroid position (use weighted mean)
    while ((left_it >= area.left) && (data[left_it].getIntensity() >=rel_peak_height) ) {
        if (data[left_it].getIntensity() >=rel_peak_height) {
        w+=data[left_it].getIntensity()*data[left_it].getMZ();
        sum+=data[left_it].getIntensity();
        --left_it;
        }
    }

    while ((right_it < area.right) && (data[right_it].getIntensity() >=rel_peak_height) ) {
        if (data[right_it].getIntensity() >=rel_peak_height) {
        w+=data[right_it].getIntensity()*data[right_it].getMZ();
        sum+=data[right_it].getIntensity();
        ++right_it;
        }
    }

    area.centroid_position = w / sum;
    }



    /// Computes the value of a theroretical lorentz peak at position x
    private double lorentz_(double height, double lambda, double pos, double x) {
    return height/(1+Math.pow(lambda*(x-pos),2));
    }

    /** 
    Computes the threshold for the peak height in the wavelet transform and initializes the wavelet transform.

        Given the threshold for the peak height a corresponding value peak_bound_cwt can be computed
      for the continious wavelet transform. 
      Therefore we compute a theoretical lorentzian peakshape with height=peak_bound_ and a width which 
      is similar to the width of the wavelet. Taking the maximum in the wavelet transform of the
      lorentzian peak we have a peak bound in the wavelet transform. 
    */
    private void initializeWT_() {
    
    //initialize wavelet transformer
    wt_.init(scale_, (Double)param_.getValue("wavelet_transform:spacing"));

    //calculate peak bound in CWT

    // build a lorentz peak of height peak_bound_
    // compute its cwt, and compute the resulting height
    // of the transformed peak
    
    //compute the peak in the intervall [-2*scale,2*scale]
    double spacing=0.001;
    int n = (int)((4*scale_)/spacing)+1;

    double lambda = 2. / scale_;
    // compute the width parameter using height=peak_bound_ and the peak endpoints should be -scale and +scale, so at
    // positions -scale and +scale the peak value should correspond to the noise_level_
    //double lambda = sqrt((-noise_level_*(-peak_bound_+noise_level_)))/(noise_level_*scale_);

    Peak[] lorentz_peak = new Peak[n];
    Peak[] lorentz_peak2 = new Peak[n];
    
    // TODO: switch the type of the transform
    
    ContinuousWaveletTransformNumIntegration lorentz_cwt = new ContinuousWaveletTransformNumIntegration();
    ContinuousWaveletTransformNumIntegration lorentz_ms2_cwt = new ContinuousWaveletTransformNumIntegration();
    
    lorentz_cwt.init(scale_, spacing);
    lorentz_ms2_cwt.init(scale_, spacing);
    double start = -2*scale_;
    for (int i=0; i < n; ++i) {
        double p = i*spacing + start;
        
        lorentz_peak[i] = new Peak();
        lorentz_peak[i].setMZ(p);
        lorentz_peak[i].setIntensity(lorentz_(peak_bound_,lambda,0,i*spacing + start));

        lorentz_peak2[i] = new Peak();
        lorentz_peak2[i].setMZ(p);
        lorentz_peak2[i].setIntensity(lorentz_(peak_bound_ms2_level_,lambda,0,i*spacing + start));
    }

    double resolution = 1.;
    lorentz_cwt.transform(lorentz_peak,0,n,resolution);
    lorentz_ms2_cwt.transform(lorentz_peak2,0,n,resolution);

    double peak_max=0;
    double peak_max2=0;
    for (int i=0; i < lorentz_cwt.getSignalLength(); i++) {
        if (lorentz_cwt.get(i) > peak_max) {
        peak_max = lorentz_cwt.get(i);
        }
        if (lorentz_ms2_cwt.get(i) > peak_max2) {
        peak_max2 = lorentz_ms2_cwt.get(i);
        }
    }
    
    peak_bound_cwt_ = peak_max;
    peak_bound_ms2_level_cwt_ = peak_max2;
    }

    /** Methods needed for separation of overlapping peaks
    */

        
    /** 
    Separates overlapping peaks.
    
    It determines the number of peaks lying underneath the initial peak using the cwt with different scales.
    Then a nonlinear optimzation procedure is applied to optimize the peak parameters.
    
    */
    private void deconvolutePeak_(Peak[] data, PeakShape shape, PeakArea_ area, Vector<Double> peak_endpoints) {
    // scaling for charge one
    double scaling_DC = (Double) param_.getValue("deconvolution:scaling");
        
    // init and calculate the transform of the signal in the convoluted region
    // first take the scaling for charge 2
    wtDC_.init(scaling_DC/2,  wt_.getSpacing());
    wtDC_.transform(data,area.left,area.right,2);

        
    int charge=2,old_peaks;
    Vector<Double> peak_values = new Vector<Double>();
    Vector<Double> old_peak_values;
    Vector<PeakShape> peaks_DC = new Vector<PeakShape>();
    int peaks = getNumberOfPeaks_(data,area.left,area.right,peak_values,/*charge*/1,2,wtDC_);    
    boolean correct_scale = false;

    //    if(peaks > 1) correct_scale = true;
    Vector<Double> distances = new Vector<Double>();
    while(!correct_scale) {
        old_peaks = peaks;
        old_peak_values = peak_values;

        correct_scale = true;
        distances.clear();
        //check distances between the peaks, if there is a larger one than try a higher charge
        for( int i=1; i<peak_values.size()/2;++i) {
        distances.add(peak_values.get(2*i+1) - peak_values.get(2*i-1));
        }
        if(peaks <=1) {
        //std::cout << "peaks "<< peaks << "\told_peaks "<< old_peaks << std::endl;
                
        ++charge;
        wtDC_.init(scaling_DC/charge,  wt_.getSpacing());
        wtDC_.transform(data,area.left,area.right,2);
        peak_values.clear();
        peaks = getNumberOfPeaks_(data,area.left,area.right,peak_values,/*charge,*/1,2,wtDC_);
        //    std::cout << "peaks "<< peaks << "\told_peaks "<< old_peaks << std::endl;
        
        if(peaks <= old_peaks) {
            //    std::cout << "peaks "<< peaks << "\told_peaks "<< old_peaks << std::endl;
            peaks = old_peaks;
            peak_values = old_peak_values;
            correct_scale = true;
        }
        continue;
        }
        Collections.sort(distances);
        int index = (int)Math.floor((distances.size()-1)/2);
        double median = distances.get(index);

        for(;index<distances.size();++index) {
        // if there is one differing distance try next scale
        if(distances.get(index) - median > 0.2 || distances.get(index) > 1.003/charge+0.15) {
            correct_scale = false;
            break;
        }
        }
        if(!correct_scale)    {
        ++charge;
        wtDC_.init(scaling_DC/charge,  wt_.getSpacing());
        wtDC_.transform(data,area.left,area.right,2);
        peak_values.clear();
        peaks = getNumberOfPeaks_(data,area.left,area.right,peak_values,/*charge,*/1,2,wtDC_);
        if(peaks <= old_peaks) {
            peaks = old_peaks;
            peak_values = old_peak_values;
            correct_scale = true;
        }
        
        }
    }

    // determine the probable charge state
    // the best result from [charge-1,charge,charge+1] will be taken
    // if charge equals zero, something bad happened
    charge = determineChargeState_(peak_values);

    // one peak needn't be deconvoluted
    if (peaks > 1 && charge >0) {
                
        OptimizePeakDeconvolution opt = new OptimizePeakDeconvolution();
     
        opt.positions_DC_.clear();
        opt.signal_DC_.clear();
        opt.peaks_DC_.clear();

        // enter zero-intensity at the left margin
        opt.positions_DC_.add(data[area.left].getMZ()-0.2);
        opt.signal_DC_.add(0.);    
                
        for (int i = 0; area.left+i != area.right ;++i) {
        opt.positions_DC_.add(data[area.left+i].getMZ());
        opt.signal_DC_.add(data[area.left+i].getIntensity());    
        }
        opt.positions_DC_.add(data[area.right].getMZ());
        opt.signal_DC_.add(data[area.right].getIntensity());    

        opt.positions_DC_.add(data[area.right].getMZ()+0.2);
        opt.signal_DC_.add(0.);    
                
                            
        // initial parameters for the optimization
        double leftwidth = (Double)param_.getValue("deconvolution:left_width");
        double rightwidth = (Double)param_.getValue("deconvolution:right_width");
            
        double dist = 1.003 / charge;

                
        PeakShape peak = new PeakShape(peak_values.get(0),peak_values.get(1),leftwidth,rightwidth,0,PeakShape.Type.SECH_PEAK);
        peaks_DC.add(peak);
        //    std::cout<<"peak.mz_position "<<peak.mz_position<<std::endl;
        for(int i=1;i<peaks;++i) {
        peak = new PeakShape(peak_values.get(2*i),peak_values.get(1)+i*dist,leftwidth,rightwidth,0,PeakShape.Type.SECH_PEAK);
        peaks_DC.add(peak);        
        }

        opt.setParameters(param_.copy("deconvolution:fitting:",true));
        opt.setCharge(charge);
                
        int runs=0;    
                        
        // if the optimization fails (peaks are too broad) try entering an additional peak
        while(!opt.optimize(peaks_DC,runs)) {
        ++runs;
        addPeak_(opt,data,peaks_DC,area,leftwidth,rightwidth);                    
        }                        
                                  
        double left_endpoint,right_endpoint;
        for( int curr_peak=0;curr_peak<peaks_DC.size();++curr_peak) {
        peak_shapes_.add(peaks_DC.get(curr_peak));
        
        PeakShape p = peaks_DC.get(curr_peak);
        if (peaks_DC.get(curr_peak).type == PeakShape.Type.LORENTZ_PEAK) {
            left_endpoint=p.mz_position+1/p.left_width*Math.sqrt(p.height/1-1);
            right_endpoint=p.mz_position+1/p.right_width*Math.sqrt(p.height/1-1);
        }
        else {
            left_endpoint=p.mz_position+1/p.left_width*MathH.acosh(Math.sqrt(p.height/0.001));
            right_endpoint=p.mz_position+1/p.right_width*MathH.acosh(Math.sqrt(p.height/0.001));
        }
        peak_endpoints.add(left_endpoint);
        peak_endpoints.add(right_endpoint);
        }
        
        opt.peaks_DC_.clear();
        opt.signal_DC_.clear();
        opt.positions_DC_.clear();
        peaks_DC.clear();
    }
    else {
        peak_shapes_.add(shape);
        peak_endpoints.add(data[area.left].getMZ());
        peak_endpoints.add(data[area.right].getMZ());
    }
            
    }
    
    /// Determines the number of peaks in the given mass range using the cwt
    private int getNumberOfPeaks_(Peak[] data, int first, int last, Vector<Double> peak_values,
                  int direction,int resolution, ContinuousWaveletTransformNumIntegration wt) {
    
    
    double noise_level=0.;
    double noise_level_cwt=0.;
    
    noise_level = peak_bound_;
    noise_level_cwt = peak_bound_cwt_;
    
    int found = 0;
    
    int zeros_left_index  = wt.getLeftPaddingIndex();
    int zeros_right_index = wt.getRightPaddingIndex();
    
    // The maximum intensity in the signal
    int it_max_pos;
    //double max_value;T
    int start = (direction>0) ? zeros_left_index+2 : zeros_right_index-2;
    int end   = (direction>0) ? zeros_right_index-1  : zeros_left_index+1;
    
    int i=0, max_pos;
    int k=0;
    
    int checker;
    while(wt.getSignal()[start+1].getMZ() <= data[first].getMZ())     
        ++start;
    //k=i;
    int offset = start;
    while(wt.getSignal()[end].getMZ() > data[last].getMZ())     
        --end;
    for(i=start; i!=end; i+=direction,k+=direction) {

        // Does a maximum in the wavelet transform at position i exists?
        if(((wt.get(i-1) - wt.get(i)  ) < 0) 
           && ((wt.get(i)   - wt.get(i+1)) > 0) 
           && ( wt.get(i)   >  noise_level_cwt)) {

        max_pos=(i-offset)/resolution;

        // if the maximum position is high enough and isn't one of the border points, we return it
        if((data[first+max_pos].getIntensity() >= noise_level) 
           && (((first+max_pos) != first) 
               && (first+max_pos)   != (last-1))
           ) {
            peak_values.add(data[first+max_pos].getIntensity());
            peak_values.add(data[first+max_pos].getMZ());
                                
            ++found;
        }
        
        }
    }
    return found;
    }

    /// Estimate the charge state of the peaks
    private int determineChargeState_(Vector<Double> peak_values) {

    int charge;
    int peaks = (int)peak_values.size() / 2;
    if(peaks>1) {
        double dif = 0;
        int i=peaks-1;
        while(i>0) {
        dif += Math.abs(peak_values.get(2*i+1) - peak_values.get(2*(i-1)+1));
        --i;
        }
        dif /= peaks-1;
        charge = (int)Math.round(1/dif);
    }
    else 
        charge = 1;
    
    return charge;
    }


    /// Add a peak
    private void addPeak_(OptimizePeakDeconvolution opt, Peak[] data, Vector<PeakShape> peaks_DC, PeakArea_ area, double left_width, double right_width) {

    // just enter a peak using equally spaced peak positions

    double peak_width = data[area.right].getMZ() - data[area.left].getMZ();
    int num_peaks = peaks_DC.size()+1;
    
    double dist = peak_width / (num_peaks+1);
    
    // put peak into peak vector using default values for the widths and peak type
    peaks_DC.add(new PeakShape(0,0,left_width,right_width,0,PeakShape.Type.SECH_PEAK));

    // adjust the positions and get their initial intensities from the raw data
    for(int i=0; i < num_peaks; ++i) {
        peaks_DC.get(i).mz_position = data[area.left].getMZ() + dist/2 + i*dist;

        int it_help = lower_bound(opt.positions_DC_,
                      0,opt.positions_DC_.size(),
                      peaks_DC.get(i).mz_position);

        if(it_help != opt.positions_DC_.size()) {                    
        peaks_DC.get(i).height = opt.signal_DC_.get(it_help)/10;
        }
        else {
        peaks_DC.get(i).height = opt.signal_DC_.get(opt.positions_DC_.size()-1);
        }
    }                
    }

    public int lower_bound(Vector<Double> data, int first, int last, double position) {
    for( int i=first; i<last; i++ ) {
        if( data.get(i)>position) {
        if( i==first )
            return first;
        return i-1;
        }
        if( data.get(i)==position) 
        return i;
    }
    return last;    
    }

}

