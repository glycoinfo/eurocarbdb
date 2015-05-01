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
/**
* 
*/
package org.eurocarbdb.applications.ms.glycopeakfinder.calculation.util;

import java.util.Comparator;

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationMolecule;


/**
* @author Logan
*
*/
public class ComparatorMolecule implements Comparator<CalculationMolecule>
{
    /**
     * @see java.util.Comparator#compare(T, T)
     */
    public int compare(CalculationMolecule a_objPeakOne, CalculationMolecule a_objPeakTwo) 
    {
        double t_dDelta = a_objPeakOne.getMass() - a_objPeakTwo.getMass(); 
        if ( t_dDelta < 0 )
        {
            return -1;
        }
        if ( t_dDelta > 0 )
        {
            return 1;
        }        
        return 0;
    }
    
}
