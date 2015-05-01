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
package org.eurocarbdb.MolecularFramework.io.glycobase;

import java.util.Comparator;


/**
* @author rene
*
*/
public class GlycobaseResidueComparator implements Comparator<GlycobaseResidue> 
{
    public int compare(GlycobaseResidue arg0, GlycobaseResidue arg1) 
    {
        int t_iLength0 = arg0.m_strPosition.split(",").length;
        int t_iLength1 = arg1.m_strPosition.split(",").length;
        if ( t_iLength0 > t_iLength1 )
        {
            return 1;
        }
        if ( t_iLength0 < t_iLength1 )
        {
            return -1;
        }
        return 0;
    }
}
