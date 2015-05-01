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
package org.eurocarbdb.resourcesdb.glycoconjugate_derived;

import java.util.Comparator;

import org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification;


public class ComparatorModification  implements Comparator<EcdbModification>
{
    public int compare(EcdbModification arg0, EcdbModification arg1) 
    {
        int t_iResult = 0;
        
        if (arg0.getPositionOne()<arg1.getPositionOne()){
            t_iResult=-1;
        }
        if (arg0.getPositionOne()>arg1.getPositionOne()){
            t_iResult=1;
        }
        if (arg0.getPositionOne()==arg1.getPositionOne()){
            t_iResult=0;
        }
        
        return t_iResult;
    }
}
