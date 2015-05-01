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

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;

/**
* @author sherget
*
*/
public class ESMFuzzy {
    private ArrayList <GlycoNode> m_aQuery = new ArrayList <GlycoNode> ();    
    private ArrayList <GlycoNode> m_aTarget = new ArrayList <GlycoNode> ();
    private Sugar m_oSugarResult = new Sugar();
    private int[][] m_aMatrix;
    private int score;
    
    
    protected Boolean m_BAnomer = true;
    protected Boolean m_BModifications = true;
    protected Boolean m_BSuperclass = true;
    protected Boolean m_BRingsize = true;
    protected Boolean m_BStereochemistry = true;
    protected Boolean m_BLinkageExact = true;
    protected Boolean m_BOnlyTopology = false;
    protected Boolean m_bConfiguration = true;
    
    protected ArrayList <GlycoNode> m_aResIgnoreAnomer = new ArrayList <GlycoNode> ();
    protected ArrayList <GlycoNode> m_aResIgnoreConfiguration = new ArrayList <GlycoNode> ();
    protected ArrayList <GlycoNode> m_aResIgnoreRingsize = new ArrayList <GlycoNode> ();
    protected ArrayList <GlycoNode> m_aResIgnoreModification = new ArrayList <GlycoNode> ();
    
    
    public ESMFuzzy (Sugar target, Sugar query){
        
        
        
        
        this.m_aTarget = target.getNodes();        
        this.m_aQuery= query.getNodes();            
        
        //size determination
        this.m_aMatrix = new int[m_aTarget.size()][m_aQuery.size()] ;        
    }
    
    
    
    public Boolean compareGraph (){
        
        this.clear();
        
        Integer t_counterG1 = 0;
        for (GlycoNode t_rG1 : this.m_aTarget){
            Integer t_counterG2 = 0;
            for (GlycoNode t_rG2 : this.m_aQuery){                
                this.m_aMatrix[t_counterG1][t_counterG2] = recursive (t_rG1,t_rG2);
                this.score=0;
                t_counterG2++;
            }            
            t_counterG1++;
        }    
        
        
        getMax();
            
        
        if (this.score>=m_aQuery.size()){
            return true;
        }
        
        return false;
        
    }
    
    void recursionMakeSugar (GlycoNode root, ArrayList<GlycoNode> a_Nodes1, ArrayList<GlycoNode> a_Nodes2){
        // Überprüfe Listen + ParentLinkages, wenn Identität, an Resultsugar anhängen + Recursion
        
        ESMNodeComparatorFuzzy t_oNodeComparator = new ESMNodeComparatorFuzzy(this);
        ESMEdgeComparatorFuzzy t_oCompEdge = new ESMEdgeComparatorFuzzy(this);    
        
        for (GlycoNode node1 : a_Nodes1){
            for (GlycoNode node2 : a_Nodes2){
                if (this.m_BOnlyTopology){
                    if (t_oNodeComparator.compare(node1,node2)){
                        
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
                else{
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
    }
    
    /**
     * @return
     */
    
    
    private void clear(){
        m_oSugarResult = new Sugar();
        score=0;
    }
    
    /**
     * @return
     */
    private void getMax() {
        // get max res from matrix
        Integer temp=0;        
        for (int t_counterG1 = 0; t_counterG1 < this.m_aTarget.size(); t_counterG1++) {            
            for (int t_counterG2 = 0; t_counterG2 < this.m_aQuery.size(); t_counterG2++){                            
                if (this.m_aMatrix[t_counterG1][t_counterG2]>temp){                    
                    temp=this.m_aMatrix[t_counterG1][t_counterG2];
                    this.score=temp;
                    
                }                    
            }                
        }
    }
    
    
    
    private Integer recursive (GlycoNode a_Node1, GlycoNode a_Node2){    
        ESMNodeComparatorFuzzy t_oNodeComparator = new ESMNodeComparatorFuzzy(this);
        if (t_oNodeComparator.compare(a_Node1,a_Node2)){
            this.score++;
            for (GlycoNode t_oChildOf1: a_Node1.getChildNodes()){
                for (GlycoNode t_oChildOf2:a_Node2.getChildNodes()){
                    if (this.m_BOnlyTopology){
                        
                            recursive(t_oChildOf1,t_oChildOf2);                        
                    
                    }
                    else {
                        ESMEdgeComparatorFuzzy t_oCompEdge = new ESMEdgeComparatorFuzzy(this);                    
                        if (t_oCompEdge.compare(t_oChildOf1.getParentEdge(),t_oChildOf2.getParentEdge())==0){                    
                            
                            recursive(t_oChildOf1,t_oChildOf2);                        
                        }
                    }
                    
                }                
            }            
        }
        return this.score;
    }
    
    
    public Boolean isContained (){
        this.clear();
        compareGraph();
        
        if (this.score>=m_aQuery.size()){
            return true;
        }
        
        return false;
    }
    public int[][]  getMatrix(){
        return this.m_aMatrix;
    }
    
    
    public void plotMatrix(){
        this.clear();
        compareGraph();
        for (int t_counterG1 = 0; t_counterG1 < this.m_aTarget.size(); t_counterG1++) {            
            for (int t_counterG2 = 0; t_counterG2 < this.m_aQuery.size(); t_counterG2++){                            
                System.out.print(this.m_aMatrix[t_counterG1][t_counterG2] +" ");                
            }    
            System.out.print("\n");
        }    
    }



    public void setExactLinkageMatch (Boolean linkageExact) {
        m_BLinkageExact = linkageExact;
    }



    public void setAnomerSensitivity(Boolean anomer) {
        m_BAnomer = anomer;
    }



    public void setModificationSensitivity(Boolean modifications) {
        m_BModifications = modifications;
    }



    public void setRingsizeSensitivity(Boolean ringsize) {
        m_BRingsize = ringsize;
    }



    public void setStereochemistrySensitivity(Boolean stereochemistry) {
        m_BStereochemistry = stereochemistry;
    }



    public void setSuperclassSensitivity(Boolean superclass) {
        m_BSuperclass = superclass;
    }    
    public void setOnlyTopology(Boolean onlyTopology) {
        m_BOnlyTopology = onlyTopology;
    }



    public ArrayList<GlycoNode> getM_aResIgnoreAnomer() {
        return m_aResIgnoreAnomer;
    }



    public void setResidueAnomerIgnorable (ArrayList<GlycoNode> resIgnoreAnomer) {
        m_aResIgnoreAnomer = resIgnoreAnomer;
    }


    public void setResiduesConfigurationIgnorable (    ArrayList<GlycoNode> resIgnoreConfiguration) {
        m_aResIgnoreConfiguration = resIgnoreConfiguration;
    }

    public void setResiduesRingsizeIgnorable(ArrayList<GlycoNode> resRingsize) {
        m_aResIgnoreRingsize = resRingsize;
    }    
    public void setResiduesModificationIgnorable(ArrayList<GlycoNode> resModification) {
        m_aResIgnoreModification = resModification;
    }    
}
