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
   Placeholder for the class doing the deconvolution optimization
*/

import java.util.*;

public class OptimizePeakDeconvolution extends DefaultParamHandler {

    public Vector<PeakShape> peaks_DC_;
    public Vector<Double> positions_DC_;
    public Vector<Double> signal_DC_;

    private int charge_;
     
    /** @name Constructors and Destructor
     */
    ///Constructor
    OptimizePeakDeconvolution( ) {
    super("OptimizePeakDeconvolution");

    peaks_DC_ = new Vector<PeakShape>();
    positions_DC_ = new Vector<Double>();
    signal_DC_ = new Vector<Double>(); 

    charge_ = 1;
    }        
    
    /// Non-mutable access to the charge
    public int getCharge() { 
    return charge_; 
    }
    
    /// Mutable access to the charge
    public void setCharge(int charge) { 
    charge_ = charge; 
    }

    /// Performs a nonlinear optimization of the peaks that belong to the current isotope pattern
    public boolean optimize(Vector<PeakShape> peaks,int failure) {
    return false;
    }
 
}

