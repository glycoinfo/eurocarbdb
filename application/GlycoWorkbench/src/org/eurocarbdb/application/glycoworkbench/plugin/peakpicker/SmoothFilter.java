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
//
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
// $Maintainer: Eva Lange $
// --------------------------------------------------------------------------
//

package org.eurocarbdb.application.glycoworkbench.plugin.peakpicker;

/**
   Base class for all noise filter implementations
*/
public class SmoothFilter extends DefaultParamHandler
{
    protected double[] coeffs_;
    
    
    /// Constructor
    public SmoothFilter() {
    super("SmoothFilter");
    coeffs_ = new double[0];
    }
    
    /// Mutable access to the coefficients of the filter
    public double[] getCoeffs() {
        return coeffs_;
    }

    
    /// Mutable access to the coefficients of the filter
    public void setCoeffs(double[] coeffs) {
        coeffs_ = coeffs;
    }


    /** 
    Applies the convolution with the filter coefficients to an given iterator range.

    Convolutes the filter and the raw data in the iterator intervall [first,last) and writes the
    resulting data to the smoothed_data_container.
    
    This method assumes that the InputPeakIterator (e.g. of type MSSpectrum<DRawDataPoint<1> >::const_iterator)
    points to a data point of type DRawDataPoint<1> or any other class derived from DRawDataPoint<1>.
    
    The resulting peaks in the smoothed_data_container (e.g. of type MSSpectrum<DRawDataPoint<1> >)
    can be of type DRawDataPoint<1> or any other class derived from DRawDataPoint. 
    
    If you use MSSpectrum iterators you have to set the SpectrumSettings by your own.
    */
        
    public double[][] filter(double[][] data, int first, int last) {
      
    double[][] ret = new double[2][];
    ret[0] = new double[last-first];
    ret[1] = new double[last-first];

        // needed for multiply the signal with the filter coefficients
        int it_back;
    int out_it = 0;
        int m,i,j;
        float help;

        int frame_size = coeffs_.length;

        // compute the transient on
        for(i=0; i<frame_size;++i)
        {
        it_back=first;
        help=0;
        m=0;

        for (j=i; j>=0; --j) {
        help += data[1][it_back]*coeffs_[m];
        --it_back;
        ++m;
        }

        ret[0][out_it] = data[0][first];
        ret[1][out_it] = help;
        ++out_it;
        ++first;
        }

        // compute the steady state output
        while (first!=last) {
        it_back=first;
        help=0;

        for (j=0; j<frame_size; ++j) {
        help+=data[0][it_back]*coeffs_[j];
        --it_back;
        }

        ret[0][out_it] = data[0][first];
        ret[1][out_it] = help;
        ++out_it;
        ++first;
        }

    return ret;
    }


    /** 
    Convolutes the filter coefficients and the input raw data.

    Convolutes the filter and the raw data in the input_peak_container and writes the
    resulting data to the smoothed_data_container.
    
    This method assumes that the elements of the InputPeakContainer (e.g. of type MSSpectrum<DRawDataPoint<1> >)
    are of type DRawDataPoint<1> or any other class derived from DRawDataPoint<1>.
    
    The resulting peaks in the smoothed_data_container (e.g. of type MSSpectrum<DRawDataPoint<1> >)
    can be of type DRawDataPoint<1> or any other class derived from DRawDataPoint. 
    
    If you use MSSpectrum iterators you have to set the SpectrumSettings by your own.
    */

    public double[][] filter(double[][] data) {
    return filter(data,0,data[0].length);
    }

}
