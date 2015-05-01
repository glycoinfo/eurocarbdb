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
package org.eurocarbdb.MolecularFramework.util.similiarity.SubgraphMatch;


import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

public class ESMNodeComparatorFuzzy {

    ESMFuzzy m_oMCSFuzzy = null;
    
    public ESMNodeComparatorFuzzy (ESMFuzzy m){
        this.m_oMCSFuzzy=m;
    }
    
    
    public boolean compare(GlycoNode r1, GlycoNode r2) {
        
        ESMVisitorFuzzy v = new ESMVisitorFuzzy(this.m_oMCSFuzzy);
        
        try {
            
            Boolean oldAnomer=this.m_oMCSFuzzy.m_BAnomer;
            Boolean oldRingsize=this.m_oMCSFuzzy.m_BRingsize;
            Boolean oldConfiguration=this.m_oMCSFuzzy.m_bConfiguration;
            Boolean oldModification=this.m_oMCSFuzzy.m_BModifications;
            
            if (this.m_oMCSFuzzy.m_aResIgnoreAnomer.contains(r1) ||
                this.m_oMCSFuzzy.m_aResIgnoreAnomer.contains(r2)){                
                this.m_oMCSFuzzy.m_BAnomer=false;                
            }
            if (this.m_oMCSFuzzy.m_aResIgnoreConfiguration.contains(r1) ||
                    this.m_oMCSFuzzy.m_aResIgnoreConfiguration.contains(r2)){                
                    this.m_oMCSFuzzy.m_bConfiguration=false;                
                }    
            if (this.m_oMCSFuzzy.m_aResIgnoreRingsize.contains(r1) ||
                    this.m_oMCSFuzzy.m_aResIgnoreRingsize.contains(r2)){                
                    this.m_oMCSFuzzy.m_BRingsize=false;                
                }    
            if (this.m_oMCSFuzzy.m_aResIgnoreModification.contains(r1) ||
                    this.m_oMCSFuzzy.m_aResIgnoreModification.contains(r2)){                
                    this.m_oMCSFuzzy.m_BModifications=false;                
                }    
            
            r1.accept(v);
            String r1Name=v.getName();
            v.clear();
            r2.accept(v);
            String r2Name=v.getName();
            v.clear();
            
            this.m_oMCSFuzzy.m_BAnomer=oldAnomer;
            this.m_oMCSFuzzy.m_BRingsize=oldRingsize;
            this.m_oMCSFuzzy.m_bConfiguration=oldConfiguration;
            this.m_oMCSFuzzy.m_BModifications=oldModification;
            
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
