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
package org.eurocarbdb.MolecularFramework.io.namespace;

import java.util.ArrayList;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
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
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserTreeSingle;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
* @author Logan
*
*/
public class GlycoVisitorSubstituentUnknownPosition implements GlycoVisitor 
{
    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Monosaccharide)
     */
    public void visit(Monosaccharide a_objMonosaccharide) throws GlycoVisitorException 
    {
        // nothing to do
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide)
     */
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException 
    {
        // do nothing        
    }

    /* (non-Javadoc)
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat)
     */
    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException
    {
        GlycoVisitorSubstituentUnknownPosition t_objVisitor = new GlycoVisitorSubstituentUnknownPosition();
        t_objVisitor.start(a_objRepeat);
    }

    public void start(SugarUnitRepeat a_objSugar) throws GlycoVisitorException
    {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();) 
        {
            GlycoVisitorSubstituentUnknownPosition t_objNN = new GlycoVisitorSubstituentUnknownPosition();
            t_objNN.start(t_iterUnder.next());
        }
    }

    public void start(UnderdeterminedSubTree a_objSugar) throws GlycoVisitorException
    {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Substituent)
     */
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException 
    {
        try 
        {
            if ( !a_objSubstituent.getSubstituentType().getComplexType() )
            {
                // simple type ==> all linkage positions have to be 1
                ArrayList<Integer> t_aPositions;
                GlycoEdge t_objEdge = a_objSubstituent.getParentEdge();
                if ( t_objEdge != null )
                {
                    for ( Linkage t_objLinkage : t_objEdge.getGlycosidicLinkages() )
                    {
                        t_aPositions = t_objLinkage.getChildLinkages();
                        if ( t_aPositions.contains(Linkage.UNKNOWN_POSITION) && t_aPositions.size() == 1 )
                        {
                            t_aPositions = new ArrayList<Integer>();
                            t_aPositions.add(1);
                            t_objLinkage.setChildLinkages(t_aPositions);
                        }
                    }
                }
                for ( GlycoEdge t_objChildEdge : a_objSubstituent.getChildEdges() )
                {
                    for ( Linkage t_objLinkage : t_objChildEdge.getGlycosidicLinkages() )
                    {
                        t_aPositions = t_objLinkage.getParentLinkages();
                        if ( t_aPositions.contains(Linkage.UNKNOWN_POSITION) && t_aPositions.size() == 1 )
                        {
                            t_aPositions = new ArrayList<Integer>();
                            t_aPositions.add(1);
                            t_objLinkage.setParentLinkages(t_aPositions);
                        }
                    }
                }
            }        
        } 
        catch (GlycoconjugateException e) 
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic)
     */
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException 
    {
        // do nothing        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException 
    {
        // TODO        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode)
     */
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException 
    {
        // do nothing
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.GlycoEdge)
     */
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException 
    {
        // do nothing        
    }

    /* (non-Javadoc)
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#start(org.eurocarbdb.MolecularFramework.sugar.Sugar)
     */
    public void start(Sugar a_objSugar) throws GlycoVisitorException 
    {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();) 
        {
            GlycoVisitorSubstituentUnknownPosition t_objNN = new GlycoVisitorSubstituentUnknownPosition();
            t_objNN.start(t_iterUnder.next());
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#getTraverser(org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor)
     */
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException 
    {
        return new GlycoTraverserTreeSingle(a_objVisitor);
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#clear()
     */
    public void clear() 
    {
    }

}