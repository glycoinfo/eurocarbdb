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
import java.util.HashMap;



import org.eurocarbdb.MolecularFramework.sugar.GlycoGraphAlternative;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;



/*
* @author sherget 
*/
public class GlycoCTGraphAlternativeComparator implements  Comparator<GlycoGraphAlternative> {
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(T, T)
     */
    public int compare(GlycoGraphAlternative arg0, GlycoGraphAlternative arg1) {        
        // Attach Lead-Out nodes to subgraphs        
        HashMap<GlycoNode,GlycoNode> t_hMapping = arg0.getLeadOutNodeToNode();
        if (!t_hMapping.isEmpty()){
            ArrayList <GlycoNode> t_aTempNodes = new ArrayList <GlycoNode> ();
            for (GlycoNode t_node: t_hMapping.keySet()){
                t_aTempNodes.add(t_node);
            }                            
            for (GlycoNode t_oNodeMain : t_aTempNodes){
                if (arg0.containsNode(t_hMapping.get(t_oNodeMain))){
                    //Parent
                    try {
                        // TODO : das kann nicht klappen. der parentnode ist nicht teil des baums
                        arg0.addEdge(t_hMapping.get(t_oNodeMain),t_oNodeMain,t_oNodeMain.getParentEdge());
                    } catch (GlycoconjugateException e) {
                        
                    }                    
                }
            }
            
        }
        t_hMapping = arg1.getLeadOutNodeToNode();
        if (!t_hMapping.isEmpty()){
            ArrayList <GlycoNode> t_aTempNodes = new ArrayList <GlycoNode> ();
            for (GlycoNode t_node: t_hMapping.keySet()){
                t_aTempNodes.add(t_node);
            }                            
            for (GlycoNode t_oNodeMain : t_aTempNodes){
                if (arg1.containsNode(t_hMapping.get(t_oNodeMain))){
                    //Parent
                    try {
                        arg1.addEdge(t_hMapping.get(t_oNodeMain),t_oNodeMain,t_oNodeMain.getParentEdge());
                    } catch (GlycoconjugateException e) {
                        
                    }                    
                }
            }
            
        }
        
        
        // compare subgraphs
        
        ArrayList <GlycoNode> t_oArg0Root = null;
        ArrayList <GlycoNode> t_oArg1Root = null;
        
        try 
        {
            t_oArg0Root = arg0.getRootNodes();
            t_oArg1Root = arg1.getRootNodes();
        } catch (GlycoconjugateException e) {
            e.printStackTrace();
        }
        
        GlycoCTGlycoNodeComparator t_oNodeComparator = new GlycoCTGlycoNodeComparator();
        Collections.sort(t_oArg0Root,t_oNodeComparator);
        Collections.sort(t_oArg1Root,t_oNodeComparator);        
                    
        if ( t_oArg0Root.size() > t_oArg1Root.size() )
        {
            for (int i = 0; i < t_oArg1Root.size(); i++) 
            {
                if (t_oNodeComparator.compare(t_oArg0Root.get(i),t_oArg1Root.get(i))==1){
                    return 1;
                }
                if (t_oNodeComparator.compare(t_oArg0Root.get(i),t_oArg1Root.get(i))==-1){
                    return -1;
                }
            }    
            
        }
        if ( t_oArg0Root.size() < t_oArg1Root.size() )
        {
            for (int i = 0; i < t_oArg0Root.size(); i++) 
            {
                if (t_oNodeComparator.compare(t_oArg0Root.get(i),t_oArg1Root.get(i))==1){
                    return 1;
                }
                if (t_oNodeComparator.compare(t_oArg0Root.get(i),t_oArg1Root.get(i))==-1){
                    return -1;
                }
            }    
            
        }
        if ( t_oArg0Root.size() == t_oArg1Root.size() )
        {
            for (int i = 0; i < t_oArg0Root.size(); i++) 
            {
                if (t_oNodeComparator.compare(t_oArg0Root.get(i),t_oArg1Root.get(i))==1){
                    return 1;
                }
                if (t_oNodeComparator.compare(t_oArg0Root.get(i),t_oArg1Root.get(i))==-1){
                    return -1;
                }
            }    
            
        }
        
        
        // compare LEAD-IN nodes
        GlycoCTGlycoNodeComparator t_comp = new GlycoCTGlycoNodeComparator();
        return t_comp.compare(arg0.getLeadInNode(),arg1.getLeadInNode());
        
    }
    
}
