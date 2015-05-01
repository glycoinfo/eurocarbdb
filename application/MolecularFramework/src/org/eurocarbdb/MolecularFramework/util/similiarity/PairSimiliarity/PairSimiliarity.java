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
package org.eurocarbdb.MolecularFramework.util.similiarity.PairSimiliarity;


import java.util.ArrayList;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.similiarity.MaximumCommonSubgraph.MCSEdgeComparatorExact;
import org.eurocarbdb.MolecularFramework.util.similiarity.MaximumCommonSubgraph.MCSNodeComparatorExact;


/**
* @author sherget
*
*/
public class PairSimiliarity {    
    
    private ArrayList <PairData> m_aG1Pairs = new ArrayList <PairData> ();
    private ArrayList <PairData> m_aG2Pairs = new ArrayList <PairData> ();
    private ArrayList <PairData> m_aResult = new ArrayList <PairData> ();
    private int score;
    private int size;
    
    public PairSimiliarity (Sugar g1, Sugar g2){
        this.clear();
        
        // fill with new modified nodes
        try {
            for (GlycoNode t_oNode : g1.getRootNodes()){
                recursiveG1(t_oNode);
            }
        } catch (GlycoconjugateException e) {
            
        }
        
        try {
            for (GlycoNode t_oNode : g2.getRootNodes()){
                recursiveG2(t_oNode);
            }
        } catch (GlycoconjugateException e) {
            
        }
        
        //Size fun
        if (this.m_aG1Pairs.size()>this.m_aG2Pairs.size()){
            this.size = this.m_aG1Pairs.size();
        }
        else {this.size = this.m_aG2Pairs.size();}
        
        // compare arrays and compose result        
        for (PairData p1 : this.m_aG1Pairs){
            for (PairData p2 : this.m_aG2Pairs){
                
                MCSNodeComparatorExact t_oNodeComparator = new MCSNodeComparatorExact();
                MCSEdgeComparatorExact t_oCompEdge = new MCSEdgeComparatorExact();
                //GlycoVisitorNodeType t_oCompNodeType = new GlycoVisitorNodeType();
                //&&    !t_oCompNodeType.isSubstituent(p2.getChild())
                
                    if (t_oNodeComparator.compare(p1.getParent(),p2.getParent()) &&
                        t_oNodeComparator.compare(p1.getChild(),p2.getChild()) &&
                        t_oCompEdge.compare(p1.getEdge(),p2.getEdge())==0 ){
                            
                                this.m_aResult.add(p2);
                                this.score++;
                        
                        }
                
                
            }
        }
        
        
        
    }
    
    
    
    public Integer getScore (){
        return this.score;
    }
    
    public Double getNormalizedScore (){
        Double f_temp;
        f_temp = this.score /Double.valueOf(this.size);
        return (f_temp);
    }
    
    
    public ArrayList <PairData> getPairs (){
        return this.m_aResult;
    }
    
    
    
    private void clear(){
        score=0;
        size=0;
        m_aG1Pairs.clear();
        m_aG2Pairs.clear();
        m_aResult.clear();
    }    
    
    private void recursiveG1 (GlycoNode a_Node){                
        for (GlycoNode t_oChild : a_Node.getChildNodes()){
            PairData t_oPairInfo = new PairData();
            t_oPairInfo.clear();        
            
            
            
            try {
                GlycoNode child;
                child = t_oChild.copy();
                child.removeAllEdges();
                t_oPairInfo.setChild(child);
                
                GlycoEdge t_edge = t_oChild.getParentEdge().copy();
                t_oPairInfo.setEdge(t_edge);
                
                GlycoNode parent = t_oChild.getParentNode().copy();
                parent.removeAllEdges();
                t_oPairInfo.setParent(parent);
                
                this.m_aG1Pairs.add(t_oPairInfo);
                
                
            } catch (GlycoconjugateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            this.recursiveG1(t_oChild);
            
        }    
    }
    
    private void recursiveG2 (GlycoNode a_Node){                
        for (GlycoNode t_oChild : a_Node.getChildNodes()){
            PairData t_oPairInfo = new PairData();
            t_oPairInfo.clear();        
            
            
            
            try {
                GlycoNode child;
                child = t_oChild.copy();
                child.removeAllEdges();
                t_oPairInfo.setChild(child);
                
                GlycoEdge t_edge = t_oChild.getParentEdge().copy();
                t_oPairInfo.setEdge(t_edge);
                
                GlycoNode parent = t_oChild.getParentNode().copy();
                parent.removeAllEdges();
                t_oPairInfo.setParent(parent);
                
                this.m_aG2Pairs.add(t_oPairInfo);
                
                
            } catch (GlycoconjugateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            this.recursiveG2(t_oChild);
            
        }    
    }
}
