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
package org.eurocarbdb.MolecularFramework.io.GlycoCT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;

/**
* @author sherget
*
*/
public class GlycoCTGlycoEdgeComparator implements Comparator<GlycoEdge> {
    
    public int compare(GlycoEdge arg0, GlycoEdge arg1) 
    {
        
        // compare size of GlycoEdge, bigger comes first (Maechtigkeit)        
        if (arg0.getGlycosidicLinkages().size() > arg1.getGlycosidicLinkages().size()){
            return 1;
        }
        if (arg0.getGlycosidicLinkages().size() < arg1.getGlycosidicLinkages().size()){
            return -1;
        }
        
        // if of equal size, compare linkages inside GlycoEdges        
        ArrayList <Linkage> t_aLinkages0 = arg0.getGlycosidicLinkages();
        ArrayList <Linkage> t_aLinkages1 = arg1.getGlycosidicLinkages();        
        
        // compare LinkageList
        GlycoCTLinkageComparator t_oLinComp = new GlycoCTLinkageComparator ();
        Collections.sort(t_aLinkages0,t_oLinComp);
        Collections.sort(t_aLinkages1,t_oLinComp);
        
        // Linkage comparison
                
        
        if (t_aLinkages0.size()==t_aLinkages1.size())
        {
            for (int i = 0; i < t_aLinkages0.size(); i++) {
                if (t_oLinComp.compare(t_aLinkages0.get(i),t_aLinkages1.get(i))!=0){
                    return t_oLinComp.compare(t_aLinkages0.get(i),t_aLinkages1.get(i));
                }
                
                
            }
            
        }
        else if (t_aLinkages0.size()<t_aLinkages1.size())
        {
            for (int i = 0; i < t_aLinkages0.size(); i++) {
                return (t_oLinComp.compare(t_aLinkages0.get(i),t_aLinkages1.get(i)));                
            }
        }
        else if (t_aLinkages0.size()>t_aLinkages1.size())
        {
            for (int i = 0; i < t_aLinkages1.size(); i++) {
                return (t_oLinComp.compare(t_aLinkages0.get(i),t_aLinkages1.get(i)));                
            }
        }        
        if ( arg0.getChild() == arg1.getChild() )
        {
            return 0;
        }
        // compare childs: CT - Logic from Node Comparator
        GlycoCTGlycoNodeComparator t_comp = new GlycoCTGlycoNodeComparator ();
        return t_comp.compare(arg0.getChild(),arg1.getChild());
}
}
