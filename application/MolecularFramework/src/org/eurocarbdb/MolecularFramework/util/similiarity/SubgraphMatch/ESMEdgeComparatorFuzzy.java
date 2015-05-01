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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


import org.eurocarbdb.MolecularFramework.io.GlycoCT.GlycoCTLinkageComparator;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.LinkageType;

/**
* @author sherget
*
*/
public class ESMEdgeComparatorFuzzy implements Comparator<GlycoEdge> {
    
    private ESMFuzzy m_oMCSFuzzy = null;
    
    public ESMEdgeComparatorFuzzy (ESMFuzzy m){
        this.m_oMCSFuzzy=m;
    }
    
    public int compare(GlycoEdge arg0, GlycoEdge arg1) 
    {
        ArrayList <Linkage> t_aLinkages0 = arg0.getGlycosidicLinkages();
        ArrayList <Linkage> t_aLinkages1 = arg1.getGlycosidicLinkages();
        
        GlycoCTLinkageComparator t_oLinComp = new GlycoCTLinkageComparator ();
        Collections.sort(t_aLinkages0,t_oLinComp);
        Collections.sort(t_aLinkages1,t_oLinComp);
        
        if (this.m_oMCSFuzzy.m_BLinkageExact){
            
            if (t_aLinkages0.size()!=t_aLinkages1.size()){
                return 1;
            }
            
            // Linkage comparison. Ordered Lists compared, exact match.
            Integer result = 0;
            if (t_aLinkages0.size()==t_aLinkages1.size())
            {            
                for (int i = 0; i < t_aLinkages0.size(); i++) {
                    if (t_oLinComp.compare(t_aLinkages0.get(i),t_aLinkages1.get(i))!=0){
                        // All linkages have to be identical in ordered list
                        result = t_oLinComp.compare(t_aLinkages0.get(i),t_aLinkages1.get(i));
                    }
                }    
            }    
            return result;
        }
        
        // Fuzzy Linkage comparison
        else {
            
            // make simple datastructure for single linkage pos + type
            ArrayList <SummarizedLinkage> edge1 = new ArrayList <SummarizedLinkage>();            
            for (int i = 0; i < t_aLinkages0.size(); i++){
                for (int j = 0; j < t_aLinkages0.get(i).getParentLinkages().size(); j++){
                    for (int g = 0; g < t_aLinkages0.get(i).getChildLinkages().size(); g++){
                    SummarizedLinkage t_oSum = new SummarizedLinkage();
                    t_oSum.m_aPosParent=t_aLinkages0.get(i).getParentLinkages().get(j);
                    t_oSum.m_oTypeParent=t_aLinkages0.get(i).getParentLinkageType();
                    t_oSum.m_aPosChild=t_aLinkages0.get(i).getParentLinkages().get(g);
                    t_oSum.m_oTypeChild=t_aLinkages0.get(i).getChildLinkageType();
                    edge1.add(t_oSum);
                    }
                }
                }
            // make simple datastructure for single linkage pos + type
            ArrayList <SummarizedLinkage> edge2 = new ArrayList <SummarizedLinkage>();            
            for (int i = 0; i < t_aLinkages1.size(); i++){
                for (int j = 0; j < t_aLinkages1.get(i).getParentLinkages().size(); j++){
                    for (int g = 0; g < t_aLinkages1.get(i).getChildLinkages().size(); g++){
                    SummarizedLinkage t_oSum = new SummarizedLinkage();
                    t_oSum.m_aPosParent=t_aLinkages1.get(i).getParentLinkages().get(j);
                    t_oSum.m_oTypeParent=t_aLinkages1.get(i).getParentLinkageType();
                    t_oSum.m_aPosChild=t_aLinkages1.get(i).getParentLinkages().get(g);
                    t_oSum.m_oTypeChild=t_aLinkages1.get(i).getChildLinkageType();
                    edge2.add(t_oSum);
                    }
                }
                }
            Integer result = 1;
            for (SummarizedLinkage t_edge1 : edge1){
                for (SummarizedLinkage t_edge2 : edge2){
                    if (     t_edge1.m_aPosChild.equals(t_edge2.m_aPosChild) &&
                            t_edge1.m_aPosParent.equals(t_edge2.m_aPosParent) &&
                            t_edge1.m_oTypeChild.equals(t_edge2.m_oTypeChild) &&
                            t_edge1.m_oTypeParent.equals(t_edge2.m_oTypeParent)){
                            result = 0;
                    }
                }
            }
            
            return result;
            
        }
        
    }
    
    class SummarizedLinkage {
        Integer m_aPosChild = null;
        Integer m_aPosParent = null;
        LinkageType m_oTypeParent = LinkageType.UNVALIDATED;
        LinkageType m_oTypeChild = LinkageType.UNVALIDATED;
    }
}
