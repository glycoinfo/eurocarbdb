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
package org.eurocarbdb.MolecularFramework.util.similiarity.MaximumCommonSubgraph;


import java.util.ArrayList;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;

/**
* @author sherget
*
*/
public class MCSExactMatch {
    private ArrayList <GlycoNode> m_aG2Nodes = new ArrayList <GlycoNode> ();    
    private ArrayList <GlycoNode> m_aG1Nodes = new ArrayList <GlycoNode> ();
    private GlycoNode m_oG1Max;
    private GlycoNode m_oG2Max;
    private Sugar m_oSugarResult = new Sugar();
    private int[][] m_aMatrix;
    private int score;
    
    public MCSExactMatch (Sugar g1, Sugar g2){
        if (g1.getNodes().size()<g2.getNodes().size()){
        this.m_aG1Nodes = g1.getNodes();        
        this.m_aG2Nodes= g2.getNodes();            
        }
        
        else {
            this.m_aG2Nodes = g1.getNodes();        
            this.m_aG1Nodes= g2.getNodes();    
        }
        //size determination
        this.m_aMatrix = new int[m_aG1Nodes.size()][m_aG2Nodes.size()] ;        
    }
    
    
    
    public Sugar compareGraph (){
        
        this.clear();
        
        Integer t_counterG1 = 0;
        for (GlycoNode t_rG1 : this.m_aG1Nodes){
            Integer t_counterG2 = 0;
            for (GlycoNode t_rG2 : this.m_aG2Nodes){                
                this.m_aMatrix[t_counterG1][t_counterG2] = recursive (t_rG1,t_rG2);
                this.score=0;
                t_counterG2++;
            }            
            t_counterG1++;
        }    
        
        
        getMax();
        makeSugar();        
        
        return this.m_oSugarResult;
        
    }
    
    void recursionMakeSugar (GlycoNode root, ArrayList<GlycoNode> a_Nodes1, ArrayList<GlycoNode> a_Nodes2){
        // Überprüfe Listen + ParentLinkages, wenn Identität, an Resultsugar anhängen + Recursion
        
        MCSNodeComparatorExact t_oNodeComparator = new MCSNodeComparatorExact();
        MCSEdgeComparatorExact t_oCompEdge = new MCSEdgeComparatorExact();    
        
        for (GlycoNode node1 : a_Nodes1){
            for (GlycoNode node2 : a_Nodes2){
                
                if (t_oNodeComparator.compare(node1,node2) &&
                        t_oCompEdge.compare(node1.getParentEdge(),node2.getParentEdge())==0){
                    
                    GlycoNode child=null;
                    try {
                        child = node1.copy();
                        child.removeAllEdges();
                        GlycoEdge t_edge = node1.getParentEdge().copy();
                        
                        this.m_oSugarResult.addEdge(root,
                                child,
                                t_edge);    
                    } catch (GlycoconjugateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    // recursion 
                    if (child!=null){
                        recursionMakeSugar(child,node1.getChildNodes(),node2.getChildNodes());
                    }                
                }            
            }
        }    
    }
    
    /**
     * @return
     */
    private void makeSugar() {
        GlycoNode root;
        try {
            if (this.m_oG1Max!=null){
            root = this.m_oG1Max.copy();
            this.m_oSugarResult.addNode(root);
            
            recursionMakeSugar(root,this.m_oG1Max.getChildNodes(),this.m_oG2Max.getChildNodes());
            }
        } catch (GlycoconjugateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
    }
    
    private void clear(){
        m_oG1Max=null;
        m_oG2Max=null;
        m_oSugarResult = new Sugar();
        score=0;
    }
    
    /**
     * @return
     */
    private void getMax() {
        // get max res from matrix
        Integer temp=0;        
        for (int t_counterG1 = 0; t_counterG1 < this.m_aG1Nodes.size(); t_counterG1++) {            
            for (int t_counterG2 = 0; t_counterG2 < this.m_aG2Nodes.size(); t_counterG2++){                            
                if (this.m_aMatrix[t_counterG1][t_counterG2]>temp){                    
                    temp=this.m_aMatrix[t_counterG1][t_counterG2];
                    this.score=temp;
                    this.m_oG1Max=this.m_aG1Nodes.get(t_counterG1);
                    this.m_oG2Max=this.m_aG2Nodes.get(t_counterG2);
                }                    
            }                
        }
    }
    
    
    
    private Integer recursive (GlycoNode a_Node1, GlycoNode a_Node2){    
        MCSNodeComparatorExact t_oNodeComparator = new MCSNodeComparatorExact();
        if (t_oNodeComparator.compare(a_Node1,a_Node2)){
            this.score++;
            for (GlycoNode t_oChildOf1: a_Node1.getChildNodes()){
                for (GlycoNode t_oChildOf2:a_Node2.getChildNodes()){
                    
                    MCSEdgeComparatorExact t_oCompEdge = new MCSEdgeComparatorExact();                    
                    if (t_oCompEdge.compare(t_oChildOf1.getParentEdge(),t_oChildOf2.getParentEdge())==0){                    
                        
                        recursive(t_oChildOf1,t_oChildOf2);                        
                    }
                }                
            }            
        }
        return this.score;
    }
    
    public float getScore (){
        this.clear();
        compareGraph();
        return Float.valueOf(this.score);
    }
    public int[][]  getMatrix(){
        return this.m_aMatrix;
    }
    
    
    public void plotMatrix(){
        this.clear();
        compareGraph();
        for (int t_counterG1 = 0; t_counterG1 < this.m_aG1Nodes.size(); t_counterG1++) {            
            for (int t_counterG2 = 0; t_counterG2 < this.m_aG2Nodes.size(); t_counterG2++){                            
                System.out.print(this.m_aMatrix[t_counterG1][t_counterG2] +" ");                
            }    
            System.out.print("\n");
        }    
    }    
}
