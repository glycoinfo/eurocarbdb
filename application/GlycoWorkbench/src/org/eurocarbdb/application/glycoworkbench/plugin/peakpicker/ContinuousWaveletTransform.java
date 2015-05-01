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
   This class is the base class of the continuous wavelet
   transformation.
*/



public class ContinuousWaveletTransform
{
    
    /// The transformed signal
    protected Peak[] signal_;

    /// The pretabulated wavelet used for the transform
    protected double[] wavelet_;

    /// Spacing and scale of the wavelet and length of the signal.
    protected double scale_;
    protected double spacing_;
    protected int signal_length_;

    /// We often have to pad the transform at the left and right with
    /// zeros. Since we don't want to iterate over those as well, we
    /// have to store their positions.
    protected int end_left_padding_;
    protected int begin_right_padding_;

    // ----

    public ContinuousWaveletTransform() {
    scale_ = 0;
    spacing_ = 0;
    signal_length_ = 0;
    end_left_padding_ = 0;
    begin_right_padding_ = 0;
    }

    /// Non-mutable access to the wavelet transform of the signal
    public Peak[] getSignal() 
    {
    return signal_;
    }
    
    /// Mutable access to the wavelet transform of the signal
    public void setSignal( Peak[] signal)
    {
    signal_ = signal;
    }

    /// Non-mutable access to the wavelet
    public double[] getWavelet() 
    {
    return wavelet_;
    }

    /// Mutable access to the signal
    public void setWavelet( double[] wavelet)
    {
    wavelet_ = wavelet;
    }

    // Non-mutable access to the scale of the wavelet
    public double getScale() 
    {
    return scale_;
    }
           
    /// Mutable access to the spacing of raw data
    public void setScale(double scale)
    {
    scale_ = scale;
    }

    // Non-mutable access to the spacing of raw data
    public double getSpacing() 
    {
    return spacing_;
    }

    /// Mutable access to the spacing of raw data
    public void setSpacing(double spacing)
    {
    spacing_ = spacing;
    }

    /// Non-mutable access to the position where the signal starts (in the intervall [0,end_left_padding_) are the padded zeros)
    public int getLeftPaddingIndex() 
    {
    return end_left_padding_;
    }

    /// Mutable access to position where the signal starts
    public void setLeftPaddingIndex( int end_left_padding)
    {
    end_left_padding_ = end_left_padding;
    }

    /// Non-mutable access to the position where the signal ends (in the intervall (begin_right_padding_,end] are the padded zeros)
    public int getRightPaddingIndex() 
    {
    return begin_right_padding_;
    }
    
    /// Mutable access to position where the signal starts
    public void setRightPaddingIndex( int begin_right_padding)
    {
    begin_right_padding_ = begin_right_padding;
    }

    /// Non-mutable access to signal length [end_left_padding,begin_right_padding]
    public int getSignalLength() 
    {
    return signal_length_;
    }
    

    /// Mutable access to signal length [end_left_padding,begin_right_padding]
    public void setSignalLength( int signal_length)
    {
    signal_length_ = signal_length;
    }

    /// Non-mutable access to signal length including padded zeros [0,end]
    public int getSize() 
    {
    return signal_.length;
    }


    /**
       Perform possibly necessary preprocessing steps, like tabulating the Wavelet.
    */
    public void init(double scale, double spacing) {
    scale_ = scale;
    spacing_=spacing;
    }

    /// Yields the signal (intensity) at position i    
    public double get(int i)
    {
    return signal_[i].getIntensity();
    }
    
    
    // -------

    /// Computes the interpolated value at position x (mz) given the iterator it_left, which points
    /// to the left neighbour raw data point of x in the original data
    protected double getInterpolatedValue_(Peak[] data, double x, int it_left) {

        // Interpolate between the point to the left and the point to the right.
        double left_position = data[it_left].getMZ();
        double right_position = data[it_left+1].getMZ();
        double d = (x-left_position)/(right_position-left_position);

        return (data[it_left+1].getIntensity()*d+data[it_left].getIntensity()*(1-d));
    }

}

