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
   This class computes the continuous wavelet transformation using a marr wavelet.
     
   The convolution of the signal and the wavelet is computed by numerical integration.
*/

public class ContinuousWaveletTransformNumIntegration extends ContinuousWaveletTransform
{
    /**
       Computes the wavelet transform of a given raw data intervall [begin_input,end_input)

       Resolution = 1: the wavelet transform will be computed at every position of the raw data,
       Resolution = 2: the wavelet transform will be computed at 2x(number of raw data positions) positions
       (the raw data are interpolated to get the intensity for missing positions)
       The InputPeakIterator should point to a DRawDataPoint<1> or any other one dimensional class derived from DRawDataPoint.
       Before starting the transformation you have to call the init function
    */

    public void transform(Peak[] data, int begin_input, int end_input, double resolution) {
    transform(data,begin_input,end_input,resolution,0);
    }

    public void transform(Peak[] data, int begin_input, int end_input, double resolution, int zeros) // zeros=0
    {
    
    if( Math.abs(resolution-1) < 0.0001) {
        // resolution = 1 corresponds to the cwt at supporting points which have a distance corresponding to the minimal spacing in [begin_input,end_input)
        int n = end_input - begin_input;
        signal_length_ = n;

        int i;

        signal_ = new Peak[n];
        int help = begin_input;
        for (i=0; i < n; ++i) {
        signal_[i] = new Peak();
        signal_[i].setMZ(data[help].getMZ());
        signal_[i].setIntensity(integrate_(data,help,begin_input,end_input));
        ++help;
        }

        // no zeropadding
        begin_right_padding_=n;
        end_left_padding_=-1;
    }
    else {
        int n = (int) resolution * (end_input - begin_input);
        double origin  = data[begin_input].getMZ();
        double spacing = (data[end_input-1].getMZ()-origin)/(n-1);
                
        // zero-padding at the ends?
        if(zeros > 0)        
        n += (2*zeros);            

        double[] processed_input = new double[n];
        signal_ = new Peak[n];

        int it_help = begin_input;
        if(zeros >0) {
        processed_input[0] = data[it_help].getMZ() - zeros*spacing;
        for( int i = 0; i < zeros; ++i) 
            processed_input[i]=0;
        }
        else 
        processed_input[0] = data[it_help].getIntensity();
                
        double x;
        for (int k=1; k < n-zeros; ++k) {
        x = origin + k*spacing;
        
        // go to the real data point next to x
        while (((it_help+1) < end_input) && (data[it_help+1].getMZ() < x)) {
            ++it_help;
        }
        processed_input[k] = getInterpolatedValue_(data,x,it_help);
        }

        if(zeros >0) {        
        for(int i = 0; i < zeros; ++i) 
            processed_input[n-zeros+i]=0;
        }
        
        // TODO avoid to compute the cwt for the zeros in signal
        for (int i=0; i < n; ++i) {
        signal_[i].setMZ(origin + i*spacing);
        signal_[i].setIntensity(integrate_(processed_input,spacing,i));
        }
        
        if(zeros == 0) {
        begin_right_padding_=n;
        end_left_padding_=-1;
        }
        else {
        begin_right_padding_=n-zeros;
        end_left_padding_=zeros-1;
        }
    }
    }


    /**
       Perform necessary preprocessing steps like tabulating the Wavelet.
       
       Build a Marr-Wavelet for the current spacing and scale.
       We store the wavelet in the vector<double> wavelet_;
       
       We only need a finite amount of points since the Marr function
       decays fast. We take 5*scale, since at that point the wavelet
       has dropped to ~ -10^-4
    */
    public void init(double scale, double spacing) {
    super.init(scale, spacing);
    int number_of_points_right = (int)(Math.ceil(5*scale_/spacing_));
    int number_of_points = number_of_points_right + 1;
    wavelet_ = new double[number_of_points];
    wavelet_[0] = 1.;

    for (int i=1; i<number_of_points; i++) {
        wavelet_[i] = marr_(i*spacing_/scale_ );
    }
    }

    // ------------

    /// Computes the convolution of the wavelet and the raw data at position x with resolution = 1
    protected double integrate_(Peak[] data, int x, int first, int last)
    {

    double v=0.;
    int middle = wavelet_.length;
    
    double start_pos = ((data[x].getMZ()-(middle*spacing_)) > data[first].getMZ()) ? (data[x].getMZ()-(middle*spacing_)) : data[first].getMZ();
    double end_pos = ((data[x].getMZ()+(middle*spacing_)) < data[last-1].getMZ()) ? (data[x].getMZ()+(middle*spacing_)) : data[last-1].getMZ();

    int help = x;

    //integrate from middle to start_pos
    while ((help != first) && (data[help-1].getMZ() > start_pos)) {
        // search for the corresponding datapoint of help in the wavelet (take the left most adjacent point)
        double distance = Math.abs(data[x].getMZ() - data[help].getMZ());
        int index_w_r = (int)Math.floor(distance / spacing_);
        double wavelet_right =  wavelet_[index_w_r];

        // search for the corresponding datapoint for (help-1) in the wavelet (take the left most adjacent point)
        distance = Math.abs(data[x].getMZ() - data[help-1].getMZ());
        int index_w_l = (int)Math.floor(distance / spacing_);
        double wavelet_left =  wavelet_[index_w_l];

        // start the interpolation for the true value in the wavelet
        v+= Math.abs(data[help-1].getMZ()-data[help].getMZ()) / 2. * (data[help-1].getIntensity()*wavelet_left + data[help].getIntensity()*wavelet_right);
        --help;
    }


    //integrate from middle to end_pos
    help = x;
    while ((help != (last-1)) && (data[help+1].getMZ() < end_pos)) {
        // search for the corresponding datapoint for help in the wavelet (take the left most adjacent point)
        double distance = Math.abs(data[x].getMZ() - data[help].getMZ());
        int index_w_l = (int)Math.floor(distance / spacing_);
        double wavelet_left =  wavelet_[index_w_l];

        // search for the corresponding datapoint for (help+1) in the wavelet (take the left most adjacent point)
        distance = Math.abs(data[x].getMZ() - data[help+1].getMZ());
        int index_w_r = (int)Math.floor(distance / spacing_);
        double wavelet_right =  wavelet_[index_w_r];

        v+= Math.abs(data[help].getMZ() - data[help+1].getMZ()) / 2. * (data[help].getIntensity()*wavelet_left + data[help+1].getIntensity()*wavelet_right);
        ++help;
    }

    return v / Math.sqrt(scale_);
    }

    /// Computes the convolution of the wavelet and the raw data at position x with resolution > 1
    protected double integrate_(double[] processed_input, double spacing_data, int index) {
    double v = 0.;
    int half_width = wavelet_.length;
    int index_in_data = (int)Math.floor((half_width*spacing_) / spacing_data);
    int offset_data_left = ((index - index_in_data) < 0) ? 0 : (index-index_in_data);
    int offset_data_right = ((index + index_in_data) > (int)processed_input.length-1) ? processed_input.length-2 : (index+index_in_data);

    // integrate from i until offset_data_left
    for (int i = index; i > offset_data_left; --i) {
        int index_w_r = (int)Math.round(((index-i)*spacing_data)/spacing_);
        int index_w_l = (int)Math.round(((index-(i-1))*spacing_data)/spacing_);

        v += spacing_data / 2.*( processed_input[i]*wavelet_[index_w_r] + processed_input[i-1]*wavelet_[index_w_l] );
    }

    // integrate from i+1 until offset_data_right
    for (int i = index; i < offset_data_right; ++i)    {
        int index_w_r = (int)Math.round((((i+1)-index)*spacing_data)/spacing_);
        int index_w_l = (int)Math.round(((i-index)*spacing_data)/spacing_);
            
        v += spacing_data / 2.*( processed_input[i+1]*wavelet_[index_w_r] + processed_input[i]*wavelet_[index_w_l]);
    }
    
    return v / Math.sqrt(scale_);
    }


    /// Computes the marr wavelet at position x
    private double marr_(double x)
    {
    return (1-x*x)*Math.exp(-x*x/2);
    }

}

