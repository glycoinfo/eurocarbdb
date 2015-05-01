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
package org.eurocarbdb.MolecularFramework.util.visitor;



import java.util.ArrayList;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserSimple;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

public class GlycoVisitorCountBranchingPoints implements GlycoVisitor
{
    private int m_iBranchingPointsAllResidues;
    private int m_iBranchingPointsOnlyMonosaccharide;
    private ArrayList<GlycoNode> m_aBranchingPointResidue = new ArrayList<GlycoNode>();
    private ArrayList<GlycoNode> m_aBranchingPointMonosaccharide = new ArrayList<GlycoNode>();    
    
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException
    {
        ArrayList<GlycoEdge> t_objLinkages = a_objMonosaccharid.getChildEdges();

        if ( t_objLinkages.size() > 1 )
        {
            this.m_iBranchingPointsAllResidues++;
            this.m_aBranchingPointResidue.add(a_objMonosaccharid);
        }
        // monosaccharide branching count
        Integer t_iMonosaccharideCount=0;
        for (GlycoEdge t_edge : t_objLinkages)
        {
            GlycoVisitorNodeType o_tVisitor = new GlycoVisitorNodeType ();                               
            if (o_tVisitor.isMonosaccharide(t_edge.getChild()))
            {
                t_iMonosaccharideCount++;
            }        
            else if ( o_tVisitor.isSugarUnitRepeat(t_edge.getChild()))
            {
                t_iMonosaccharideCount++;
            }
        }
        if (t_iMonosaccharideCount>1)
        {
            this.m_iBranchingPointsOnlyMonosaccharide++;
            this.m_aBranchingPointMonosaccharide.add(a_objMonosaccharid);
        }
    }

    
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("NonMonosaccharides are not allowed.");
    }

    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException
    {
        return new GlycoTraverserSimple(a_objVisitor);
    }

    public void clear()
    {
        this.m_iBranchingPointsAllResidues = 0;   
        this.m_iBranchingPointsOnlyMonosaccharide = 0;
        this.m_aBranchingPointMonosaccharide.clear();
        this.m_aBranchingPointResidue.clear();
    }

    public int getBranchingPointsCountResidue()
    {
        return this.m_iBranchingPointsAllResidues;
    }
    
    public int getBranchingPointsCountMonosaccharide()
    {
        return this.m_iBranchingPointsOnlyMonosaccharide;
    }

    public void start(GlycoNode a_objResidue) throws GlycoVisitorException 
    {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverse(a_objResidue);
    }

    public void start(Sugar a_objSugar) throws GlycoVisitorException 
    {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();) 
        {
            UnderdeterminedSubTree t_objSubTree = t_iterUnder.next();
            t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(t_objSubTree);            
            boolean t_bNewResidue = false;
            boolean t_bNewMonosaccharide = false;
            for (Iterator<GlycoNode> t_iterParents = t_objSubTree.getParents().iterator(); t_iterParents.hasNext();) 
            {
                GlycoNode t_objNode = t_iterParents.next();
                GlycoVisitorNodeType t_visType = new GlycoVisitorNodeType();
                if ( t_visType.isMonosaccharide(t_objNode ))
                {
                    if ( t_objNode.getChildEdges().size() == 1 )
                    {
                        if ( !this.m_aBranchingPointResidue.contains(t_objNode) )
                        {
                            this.m_aBranchingPointResidue.add(t_objNode);
                            t_bNewResidue = true;
                        }
                    }
                    int t_iMonosaccharideCount = 0;
                    for (GlycoEdge t_edge : t_objNode.getChildEdges())
                    {
                        GlycoVisitorNodeType o_tVisitor = new GlycoVisitorNodeType ();                               
                        if (o_tVisitor.isMonosaccharide(t_edge.getChild()))
                        {
                            t_iMonosaccharideCount++;
                        }        
                        else if ( o_tVisitor.isSugarUnitRepeat(t_edge.getChild()))
                        {
                            t_iMonosaccharideCount++;
                        }
                    }
                    if ( t_iMonosaccharideCount == 1 )
                    {
                        if ( !this.m_aBranchingPointMonosaccharide.contains(t_objNode) )
                        {
                            this.m_aBranchingPointMonosaccharide.add(t_objNode);
                            t_bNewMonosaccharide = true;
                        }
                    }
                }
                else
                {
                    if ( t_objNode.getChildEdges().size() == 1 )
                    {
                        if ( !this.m_aBranchingPointResidue.contains(t_objNode) )
                        {
                            this.m_aBranchingPointResidue.add(t_objNode);
                            t_bNewResidue = true;
                        }
                    }
                }                
            }
            if ( t_bNewResidue )
            {
                this.m_iBranchingPointsAllResidues++;
            }
            if ( t_bNewMonosaccharide )
            {
                this.m_iBranchingPointsOnlyMonosaccharide++;
            }
        }
    }


    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException 
    {
        ArrayList<GlycoEdge> t_objLinkages = a_objSubstituent.getChildEdges();

        if ( t_objLinkages.size() > 1 )
        {
            this.m_iBranchingPointsAllResidues++;  
            this.m_iBranchingPointsOnlyMonosaccharide++;
            this.m_aBranchingPointResidue.add(a_objSubstituent);
        }
    }


    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException 
    {
        // nothing to do, cyclic can not have childs        
    }


    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("SugarUnitAlternative are not allowed.");
    }

    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("UnvalidatedGlycoNodes are not allowed.");
    }

    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException 
    {
        // nothing to do        
    }
    
    public void visit(SugarUnitRepeat a_objRepeate) throws GlycoVisitorException 
    {
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objRepeate);
        GlycoNode t_objNode = a_objRepeate.getRepeatLinkage().getParent();
        GlycoVisitorNodeType t_visType = new GlycoVisitorNodeType();
        if ( t_visType.isMonosaccharide(t_objNode))
        {
            if ( t_objNode.getChildEdges().size() == 1 )
            {
                this.m_iBranchingPointsAllResidues++;
                this.m_aBranchingPointResidue.add(t_objNode);
            }
            Integer t_iMonosaccharideCount=0;
            for (GlycoEdge t_edge : t_objNode.getChildEdges())
            {
                if (t_visType.isMonosaccharide(t_edge.getChild()))
                {
                    t_iMonosaccharideCount++;
                }                
            }
            if (t_iMonosaccharideCount==1)
            {
                this.m_iBranchingPointsOnlyMonosaccharide++;
                this.m_aBranchingPointMonosaccharide.add(t_objNode);
            }
        }
        else
        {
            if ( t_objNode.getChildEdges().size() == 1 )
            {
                this.m_iBranchingPointsAllResidues++;        
                this.m_iBranchingPointsOnlyMonosaccharide++;
                this.m_aBranchingPointResidue.add(t_objNode);
            }
        }
        for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objRepeate.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();) 
        {
            t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(t_iterUnder.next());            
        }
    }
}