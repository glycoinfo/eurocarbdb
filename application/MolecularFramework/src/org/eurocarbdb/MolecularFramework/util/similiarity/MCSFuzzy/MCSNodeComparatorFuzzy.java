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
package org.eurocarbdb.MolecularFramework.util.similiarity.MCSFuzzy;


import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

public class MCSNodeComparatorFuzzy {

    MCSFuzzy m_oMCSFuzzy = null;
    
    public MCSNodeComparatorFuzzy (MCSFuzzy m){
        this.m_oMCSFuzzy=m;
    }
    
    
    public boolean compare(GlycoNode r1, GlycoNode r2) {
        
        MCSVisitorFuzzy v = new MCSVisitorFuzzy(this.m_oMCSFuzzy);
        
        try {
            r1.accept(v);
            String r1Name=v.getName();
            v.clear();
            r2.accept(v);
            String r2Name=v.getName();
            v.clear();
            
            if (r2Name.equals(r1Name)){                
                return true;                
            }
        } catch (GlycoVisitorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
        
        
        return false;
        
    }    
}
