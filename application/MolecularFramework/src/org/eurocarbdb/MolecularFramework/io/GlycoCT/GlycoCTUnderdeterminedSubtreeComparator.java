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
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;

/**
* @author sherget 
*/
public class GlycoCTUnderdeterminedSubtreeComparator implements  Comparator<UnderdeterminedSubTree> {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(T, T)
     */
    public int compare(UnderdeterminedSubTree arg0, UnderdeterminedSubTree arg1) {
        
        // compare roots of undet. subtree    
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
                    
        // TODO : wenn anzahl ungleich ist, sofort entscheidung fällen
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
        
        // compare parent nodes        
        ArrayList <GlycoNode> t_arg0Parents = arg0.getParents();
        Collections.sort(t_arg0Parents,t_oNodeComparator);
        ArrayList <GlycoNode> t_arg1Parents = arg1.getParents();
        Collections.sort(t_arg1Parents,t_oNodeComparator);        
        
        if ( t_arg0Parents.size() > t_arg1Parents.size() )
        {
            for (int i = 0; i < t_arg1Parents.size(); i++) 
            {
                if (t_oNodeComparator.compare(t_arg0Parents.get(i),t_arg1Parents.get(i))==1){
                    return 1;
                }
                if (t_oNodeComparator.compare(t_arg0Parents.get(i),t_arg1Parents.get(i))==-1){
                    return -1;
                }
            }    
            
        }
        if ( t_arg0Parents.size() < t_arg1Parents.size() )
        {
            for (int i = 0; i < t_arg0Parents.size(); i++) 
            {
                if (t_oNodeComparator.compare(t_arg0Parents.get(i),t_arg1Parents.get(i))==1){
                    return 1;
                }
                if (t_oNodeComparator.compare(t_arg0Parents.get(i),t_arg1Parents.get(i))==-1){
                    return -1;
                }
            }    
            
        }
        if ( t_arg0Parents.size() == t_arg1Parents.size() )
        {
            for (int i = 0; i < t_arg0Parents.size(); i++) 
            {
                if (t_oNodeComparator.compare(t_arg0Parents.get(i),t_arg1Parents.get(i))==1){
                    return 1;
                }
                if (t_oNodeComparator.compare(t_arg0Parents.get(i),t_arg1Parents.get(i))==-1){
                    return -1;
                }
            }    
            
        }
        
        // compare parent-linkages
        GlycoCTGlycoEdgeComparator t_oEdgeComparator = new GlycoCTGlycoEdgeComparator();
        GlycoEdge t_arg0ParentLin = arg0.getConnection();
        GlycoEdge t_arg1ParentLin = arg1.getConnection();
        if (t_oEdgeComparator.compare(t_arg0ParentLin,t_arg1ParentLin)!=0){
            return     t_oEdgeComparator.compare(t_arg0ParentLin,t_arg1ParentLin);        
        }        
        
        // compare aufenthaltswahrscheinlichkeit        
        if (arg0.getProbabilityLower()<arg1.getProbabilityLower()){
            return -1;
        }
        else if (arg0.getProbabilityLower()>arg1.getProbabilityLower()){
            return 1;
        }
        
        if (arg0.getProbabilityUpper()<arg1.getProbabilityUpper()){
            return -1;
        }
        else if (arg0.getProbabilityUpper()>arg1.getProbabilityUpper()){
            return 1;
        }        
        
        return 0;
    }

}
