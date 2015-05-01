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
// --------------------------------------------------------------------------
//                   OpenMS Mass Spectrometry Framework
// --------------------------------------------------------------------------
//  Copyright (C) 2003-2007 -- Oliver Kohlbacher, Knut Reinert
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// --------------------------------------------------------------------------
// $Maintainer: Eva Lange  $
// --------------------------------------------------------------------------


package org.eurocarbdb.application.glycoworkbench.plugin.peakpicker;

/**
   This class represents a Gaussian lowpass-filter which works on uniform as well as on non-uniform raw data.
   
   Gaussian filters are important in many signal processing,
   image processing, and communication applications. These filters are characterized by narrow bandwidths,
   sharp cutoffs, and low passband ripple. A key feature of Gaussian filters is that the Fourier transform of a
   Gaussian is also a Gaussian, so the filter has the same response shape in both the time and frequency domains.
   The coefficients \f$ \emph{coeffs} \f$ of the Gaussian-window with length \f$ \emph{frameSize} \f$ are calculated
   from the gaussian distribution
   \f[ \emph{coeff}(x) = \frac{1}{\sigma \sqrt{2\pi}} e^{\frac{-x^2}{2\sigma^2}} \f]
   where \f$ x=[-\frac{frameSize}{2},...,\frac{frameSize}{2}] \f$ represents the window area and \f$ \sigma \f$
   is the standard derivation.
   
   The wider the kernel width the smoother the signal (the more detail information get lost!).
   Use a gaussian filter kernel which has approximately the same width as your mass peaks,
   whereas the gaussian peak width corresponds approximately to 8*sigma.
   
   GaussFilter_Parameters are explained on a separate page.
*/

public class GaussFilter extends SmoothFilter 
{
    /// The standard derivation  \f$ \sigma \f$.
    protected double sigma_;

    /// The spacing of the pre-tabulated kernel coefficients
    protected double spacing_;
 
    /// Constructor
    public GaussFilter() {
    super();
    
    setName("GaussFilter");
      
          //Parameter settings
          defaults_.setValue("gaussian_width",0.8,"Use a gaussian filter kernel which has approximately the same width as your mass peaks");
        
    //members
        sigma_ = .1;
        spacing_ = 0.01;

        //compute the filter kernel coefficients
        init(sigma_,spacing_);
        
        defaultsToParam_();
    }

    /// Non-mutable access to the sigma
    public double getSigma() 
    {
        return sigma_;
    }

    
    /// Mutable access to the sigma
    public void setSigma(double sigma)
    {
        sigma_ = sigma;
        spacing_ = 4*sigma_ / 50;
        init(sigma_,spacing_);
        
        param_.setValue("gaussian_width",8*sigma_);
    }
    
    /// Non-mutable access to the kernel width
    public double getKernelWidth()
    {
        return (sigma_ * 8.);
    }
     
    /// Mutable access to the kernel width
    public void setKernelWidth(double kernel_width) throws Exception
    {
        if (kernel_width <= 0)        
        throw new Exception("The kernel width should be greater than zero! " + kernel_width);    
        
        sigma_ = kernel_width / 8.;
        init(sigma_,spacing_);
        param_.setValue("gaussian_width",kernel_width);
    }
    
    /// Non-mutable access to the spacing
    public double getSpacing()
    {
        return spacing_;
    }
    
    /// Mutable access to the spacing
    public void setSpacing(double spacing)
    {
    spacing_=spacing;
        //OPENMS_PRECONDITION((4*sigma_ > spacing), "You have to choose a smaller spacing for the kernel coefficients!" );
        init(sigma_,spacing_);
    }
      
    /** 
    Build a gaussian distribution for the current spacing and standard deviation.
    
    We store the coefficiens of gaussian in the vector<double> coeffs_;
    
    We only need a finite amount of points since the gaussian distribution
    decays fast. We take 4*sigma (99.993666% of the area is within four standard deviations), since at that point the function
    has dropped to ~ -10^-4
    */
    public void init(double sigma, double spacing) {
    sigma_= sigma;
    spacing_=spacing;
    
    int number_of_points_right = (int)(Math.ceil(4*sigma_ / spacing_))+1;
    coeffs_ = new double[number_of_points_right];
    coeffs_[0] = 1.0/(sigma_ * Math.sqrt(2.0 * Math.PI));
    
    for (int i=1; i < number_of_points_right; i++)        
        coeffs_[i] = gauss_(i*spacing_);    
    }

    
    /** 
    Applies the convolution with the filter coefficients to an given iterator range.
    
    Convolutes the filter and the raw data in the iterator intervall [first,last) and writes the
    resulting data to the smoothed_data_container.
    
    This method assumes that the InputPeakIterator (e.g. of type MSSpectrum<RawDataPoint1D >::const_iterator)
    points to a data point of type RawDataPoint1D or any other class derived from RawDataPoint1D.
    
    The resulting peaks in the smoothed_data_container (e.g. of type MSSpectrum<RawDataPoint1D >)
    can be of type RawDataPoint1D or any other class derived from DRawDataPoint. 
       
    If you use MSSpectrum iterators you have to set the SpectrumSettings by your own.
    */
    
    public double[][] filter(double[][] data, int first, int last)
    {
    double[][] ret = new double[2][];
    ret[0] = new double[last-first];
    ret[1] = new double[last-first];

        int help = first;
    int out_it = 0;
        while (help != last) {
        ret[0][out_it] = data[0][help];
        ret[1][out_it] = integrate_(data,help,first,last);
        ++out_it;
        ++help;
        }

    return ret;
    }


    /** 
    Convolutes the filter coefficients and the input raw data.

    Convolutes the filter and the raw data in the input_peak_container and writes the
    resulting data to the smoothed_data_container.
    
    This method assumes that the elements of the InputPeakContainer (e.g. of type MSSpectrum<RawDataPoint1D >)
    are of type RawDataPoint1D or any other class derived from RawDataPoint1D.
    
    The resulting peaks in the smoothed_data_container (e.g. of type MSSpectrum<RawDataPoint1D >)
    can be of type RawDataPoint1D or any other class derived from DRawDataPoint. 
    
    If you use MSSpectrum iterators you have to set the SpectrumSettings by your own.
    */
    public double[][] filter(double[][] data) {
    return filter(data,0,data[0].length);
    }

    
    // ------------

    protected void updateMembers_() 
    {
        double kernel_width = (Double)param_.getValue("gaussian_width"); 
        
        sigma_ = kernel_width / 8.;
        init(sigma_,spacing_);
    }


    /// Computes the value of the gaussian distribution (mean=0 and standard deviation=sigma) at position x
    private double gauss_(double x)
    {
        return (1.0/(sigma_ * Math.sqrt(2.0 * Math.PI)) * Math.exp(-(x*x) / (2 * sigma_ * sigma_)));
    }

    /// Computes the convolution of the raw data at position x and the gaussian kernel    
    private double integrate_(double[][] data, int x, int first, int last)
    {
    double v = 0.;

        // norm the gaussian kernel area to one
        double norm = 0.;
        int middle = coeffs_.length;

        double start_pos = ((data[0][x]-(middle*spacing_)) > data[0][first]) ? (data[0][x]-(middle*spacing_)) : data[0][first];
        double end_pos = ((data[0][x]+(middle*spacing_)) < data[0][last-1]) ? (data[0][x]+(middle*spacing_)) : data[0][last-1];

        int help = x;

        //integrate from middle to start_pos
        while ((help != first) && (data[0][help-1] > start_pos)) {
        
        // search for the corresponding datapoint of help in the gaussian (take the left most adjacent point)
        double distance_in_gaussian = Math.abs(data[0][x] - data[0][help]);
        int left_position = (int)Math.floor(distance_in_gaussian / spacing_);

        // search for the true left adjacent data point (because of rounding errors)
        for (int j=0; ((j<3) &&  ((help-j-first) >= 0)); ++j) {
        
        if (((left_position-j)*spacing_ <= distance_in_gaussian) && ((left_position-j+1)*spacing_ >= distance_in_gaussian)) {
            left_position -= j;
            break;
        }

        if (((left_position+j)*spacing_ < distance_in_gaussian) && ((left_position+j+1)*spacing_ < distance_in_gaussian)) {
            left_position +=j;
            break;
        }
        }

        // interpolate between the left and right data points in the gaussian to get the true value at position distance_in_gaussian
        int right_position = left_position+1;
        double d = Math.abs((left_position*spacing_)-distance_in_gaussian) / spacing_;

        // check if the right data point in the gaussian exists
        double coeffs_right = (right_position < middle) ? (1-d)*coeffs_[left_position]+d*coeffs_[right_position] : coeffs_[left_position];

        
        // search for the corresponding datapoint for (help-1) in the gaussian (take the left most adjacent point)
        distance_in_gaussian = Math.abs(data[0][x] - data[0][help-1]);
        left_position = (int)Math.floor(distance_in_gaussian / spacing_);

        // search for the true left adjacent data point (because of rounding errors)
        for (int j=0; ((j<3) && ((help-j-first) >= 0)); ++j) {
        
        if (((left_position-j)*spacing_ <= distance_in_gaussian) && ((left_position-j+1)*spacing_ >= distance_in_gaussian)) {
            left_position -= j;
            break;
        }
        
        if (((left_position+j)*spacing_ < distance_in_gaussian) && ((left_position+j+1)*spacing_ < distance_in_gaussian)) {
            left_position +=j;
            break;
        }
        }

        // start the interpolation for the true value in the gaussian
        right_position = left_position+1;
        d = Math.abs((left_position*spacing_)-distance_in_gaussian) / spacing_;
        double coeffs_left= (right_position < middle) ? (1-d)*coeffs_[left_position]+d*coeffs_[right_position] : coeffs_[left_position];


        norm += Math.abs(data[0][help-1]-data[0][help]) / 2. * (coeffs_left + coeffs_right);

        v += Math.abs(data[0][help-1]-data[0][help]) / 2. * (data[1][help-1]*coeffs_left + data[1][help]*coeffs_right);
        --help;
        }


        //integrate from middle to end_pos
        help = x;

        while ((help != (last-1)) && (data[0][help+1] < end_pos))  {
        
        // search for the corresponding datapoint for help in the gaussian (take the left most adjacent point)
        double distance_in_gaussian = Math.abs(data[0][x] - data[0][help]);
        int left_position = (int)Math.floor(distance_in_gaussian / spacing_);

        // search for the true left adjacent data point (because of rounding errors)
        for (int j=0; ((j<3) && ((last-1-help-j) >= 0)); ++j) {
        
        if (((left_position-j)*spacing_ <= distance_in_gaussian) && ((left_position-j+1)*spacing_ >= distance_in_gaussian)) {
            left_position -= j;
            break;
        }

        if (((left_position+j)*spacing_ < distance_in_gaussian) && ((left_position+j+1)*spacing_ < distance_in_gaussian)) {
            left_position +=j;
            break;
        }
        }

        // start the interpolation for the true value in the gaussian
        int right_position = left_position+1;
        double d = Math.abs((left_position*spacing_)-distance_in_gaussian) / spacing_;
        double coeffs_left= (right_position < middle) ? (1-d)*coeffs_[left_position]+d*coeffs_[right_position] : coeffs_[left_position];

        // search for the corresponding datapoint for (help+1) in the gaussian (take the left most adjacent point)
        distance_in_gaussian = Math.abs(data[0][x] - data[0][help+1]);
        left_position = (int)Math.floor(distance_in_gaussian / spacing_);

        // search for the true left adjacent data point (because of rounding errors)
        for (int j=0; ((j<3) && ((last-1-help-j) >= 0)); ++j) {
        
        if (((left_position-j)*spacing_ <= distance_in_gaussian) && ((left_position-j+1)*spacing_ >= distance_in_gaussian)) {
            left_position -= j;
            break;
        }
        
        if (((left_position+j)*spacing_ < distance_in_gaussian) && ((left_position+j+1)*spacing_ < distance_in_gaussian)) {
            left_position +=j;
            break;
        }
        }
        
        // start the interpolation for the true value in the gaussian
        right_position = left_position+1;
        d = Math.abs((left_position*spacing_)-distance_in_gaussian) / spacing_;
        double coeffs_right = (right_position < middle) ? (1-d)*coeffs_[left_position]+d*coeffs_[right_position] : coeffs_[left_position];
        
        norm += Math.abs(data[0][help] - data[0][help+1]) / 2. * (coeffs_left + coeffs_right);

        v += Math.abs(data[0][help] - data[0][help+1]) / 2. * (data[1][help]*coeffs_left + data[1][help+1]*coeffs_right);
        ++help;
        }
    
        if (v > 0)        
        return v / norm;        
        else        
        return 0;        
    }  

} 

