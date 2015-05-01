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
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

/** 
    This class is a internal representation (used by the PeakPickerCWT) of a peak shape.    
    It defines an asymmetric lorentzian and asymmetric hyperbolic squared secan function. 
*/

public class PeakShape
{

    /**
       Comparator for the width.
       Lexicographical comparison from dimension 0 to dimension D is done.
    */
    public class PositionLess implements java.util.Comparator<PeakShape>
    {
    private int dimension_;

    public PositionLess(int i) {
        dimension_ = i; 
    }
    
    public PositionLess() {
        dimension_ = -1;
    }

    public int compare(PeakShape a, PeakShape b) {
        if(a.mz_position==b.mz_position) 
        return 0;
        if(a.mz_position<b.mz_position) 
        return -1;
        return 1;
    }

    };


    /** 
    Peak shape type (asymmetric lorentzian or asymmetric hyperbolic secans squared).

    *    The peak shape can represent an asymmetric lorentzian function, given by 
    
    *    l(x) = height/(1.+pow(left_width*(x - mz_position), 2)) (x<=mz_position) 
    
    *    l(x) = height/(1.+pow(right_width*(x - mz_position), 2)) (x>mz_position)
        
    *    or an asymmetric hyperbolic secans squared function 
    
    *   s(x) = height/pow(cosh(left_width*(x-mz_position)), 2) (x<=mz_position)
    
    *   s(x) = height/pow(cosh(right_width*(x-mz_position)), 2) (x>mz_position)
    */

    public enum Type { LORENTZ_PEAK, SECH_PEAK, UNDEFINED };


    // -------------------

    /// Maximum intensity of the peak shape
    public double height;

    /// Centroid position
    public double mz_position;

    /// Left width parameter
    public double left_width;

    /// Right width parameter
    public double right_width;

    /// Area of the peak shape
    public double area;

    /// It represents the squared pearson correlation coefficient with the original data (0 <= r_value <= 1)
    public double r_value;

    /// The signal to noise ratio at the mz_position
    public double signal_to_noise;

    /// Peak shape type 
    public Type type;
    
    // -------------------

    /// Constructor
    public PeakShape() {

        height = 0.;
        mz_position = 0.;
        left_width = 0.;
        right_width = 0.;
        area = 0.;
        r_value = 0.;
        signal_to_noise = 0.;
        type = Type.UNDEFINED;
    }
    
    /// Constructor
    public PeakShape(double height_,
             double mz_position_,
             double left_width_,
             double right_width_,
             double area_,
             Type type_) {
    height = height_;
    mz_position = mz_position_;    
    left_width = left_width_;
    right_width = right_width_;
    area = area_;
    r_value = 0;
    signal_to_noise = 0;
    type = type_;
    }

    /// Copy constructor
    public PeakShape clone() {
    PeakShape ret = new PeakShape();
    ret.copy(this);
    return ret;
    }
    
    /// Assignment operator
    public void copy(PeakShape peakshape) {
    this.mz_position = peakshape.mz_position;
    this.left_width = peakshape.left_width;
    this.right_width = peakshape.right_width;
    this.area = peakshape.area;
    this.r_value = peakshape.r_value;
    this.signal_to_noise = peakshape.signal_to_noise;
    this.type = peakshape.type;
    }

    /// Compute the intensity of the peaks shape at position x
    public double get(double x) {
    double value;

    if( type==Type.LORENTZ_PEAK ) {
        if (x<=mz_position)
        value = height/(1.+Math.pow(left_width*(x - mz_position), 2));
        else
        value = height/(1.+Math.pow(right_width*(x - mz_position), 2));
    }
    else if( type==Type.SECH_PEAK ) {
        if (x<=mz_position)
        value = height/Math.pow(Math.cosh(left_width*(x-mz_position)), 2);
        else
        value = height/Math.pow(Math.cosh(right_width*(x-mz_position)), 2);
    }
    else 
        value = -1.;   
    
    return value;
    }


    /// Computes symmetry measure of the peak shape, which is corresponds to th ratio of the left and right width parameters.
    public double getSymmetricMeasure() {
    double value;

    if (left_width < right_width)
        value = left_width/right_width;
    else
        value = right_width/left_width;
    
    return value;
    }

    /// Estimates the full width at half maximum.
    public double getFWHM() {
    double fwhm=0;

    if( type==Type.LORENTZ_PEAK ) {
        fwhm = 1/right_width;
        fwhm += 1/left_width;
    }
    else if( type==Type.SECH_PEAK ) {      
        double m = Math.log(Math.sqrt(2.0)+1);
        fwhm = m/left_width;
        fwhm += m/right_width;
    }
    else 
        fwhm = -1.;      
    
    return fwhm;
    }

    

    public void output() {
    System.out.println("PeakShape");
    System.out.println("\theight = " + height);
    System.out.println("\tmz = " + mz_position);
    System.out.println("\tleft_width = " + left_width);
    System.out.println("\tright_width = " + right_width);
    System.out.println("\tarea = " + area);
    System.out.println("\ttype = " + type);

    }
}