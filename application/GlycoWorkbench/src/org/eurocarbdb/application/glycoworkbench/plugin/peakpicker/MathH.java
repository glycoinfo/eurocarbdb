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

public class MathH {

    static double sinh(double x) {
    return (Math.exp(x)-Math.exp(-x))/2.;
    }

    static double cosh(double x) {
    return (Math.exp(x)+Math.exp(-x))/2.;
    }

    static double tanh(double x) {
    return (Math.exp(x)-Math.exp(-x))/(Math.exp(x)+Math.exp(-x));
    }
    
    static double asinh(double x) {    
     return Math.log(x + Math.sqrt(1. + x*x));
    }
    
    static double acosh(double x) {    
    return 2. * Math.log(Math.sqrt((x+1)/2.) + Math.sqrt((x-1)/2));
    }

    static double atanh(double x) {    
    return (Math.log(1+x) - Math.log(1-x))/2.;
    }

}