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
package org.eurocarbdb.MolecularFramework.util.validation;

import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraphAlternative;
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
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserNodes;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
* Count all types of residues. 
* 
* @author rene
*/
public class GlycoVisitorContainsNode implements GlycoVisitor
{
    private int m_iMonosaccharide = 0;
    private int m_iNonMonosaccharide = 0;
    private int m_iSubstituent = 0;
    private int m_iRepeat = 0;
    private int m_iUnvalidated = 0;
    private int m_iUnderdetermined = 0;
    private int m_iAlternative = 0;
    private int m_iCyclic = 0;
    private boolean m_bDescent = true;
    private boolean m_bDescentUnderdeterminded = true;
    private String m_strNonMSnames = "";
    
    public String getNonMsNames()
    {
        return this.m_strNonMSnames;
    }
    
    public void setDescent(boolean a_bDescent)
    {
        this.m_bDescent = a_bDescent;
    }
    
    public void setDescentUnderdeterminded(boolean a_bDescent)
    {
        this.m_bDescentUnderdeterminded = a_bDescent;
    }

    public int getMonosaccharideCount()
    {
        return this.m_iMonosaccharide;
    }
    
    public int getNonMonosaccharideCount()
    {
        return this.m_iNonMonosaccharide;
    }
    
    public int getSubstituentCount()
    {
        return this.m_iSubstituent;
    }
    
    public int getRepeatCount()
    {
        return this.m_iRepeat;
    }
    
    public int getUnvalidatedCount()
    {
        return this.m_iUnvalidated;
    }
    
    public int getUnderdetermindedCount()
    {
        return this.m_iUnderdetermined;
    }
    
    public int getAlternativeCount()
    {
        return this.m_iAlternative;
    }
    
    public int getCyclicCount()
    {
        return this.m_iCyclic;
    }
    
    public void visit(Monosaccharide arg0) throws GlycoVisitorException
    {
        this.m_iMonosaccharide++;
    }

    public void visit(NonMonosaccharide arg0) throws GlycoVisitorException
    {
        this.m_iNonMonosaccharide++;
    }

    public void visit(GlycoEdge arg0) throws GlycoVisitorException
    {
        // do nothing
    }

    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException
    {
        this.m_iRepeat++;
        this.m_iUnderdetermined += a_objRepeat.getUndeterminedSubTrees().size();
        if ( this.m_bDescent )
        {
            GlycoTraverser t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(a_objRepeat);
            if ( this.m_bDescentUnderdeterminded )
            {
                for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objRepeat.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();)
                {
                    t_objTraverser = this.getTraverser(this);
                    t_objTraverser.traverseGraph(t_iterUnder.next());
                }
            }
        }
    }

    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException
    {
        return new GlycoTraverserNodes(a_objVisitor);
    }

    public void clear()
    {
        this.m_iMonosaccharide = 0;
        this.m_iNonMonosaccharide = 0;
        this.m_iRepeat = 0;
        this.m_iSubstituent = 0;
        this.m_iUnvalidated = 0;
        this.m_iUnderdetermined = 0;
        this.m_iAlternative = 0;
        this.m_iCyclic = 0;
        this.m_strNonMSnames = "";
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Substituent)
     */
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException 
    {
        this.m_iSubstituent++;
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic)
     */
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException 
    {
        this.m_iCyclic++;
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException 
    {
        this.m_iAlternative++;
        if ( this.m_bDescent )
        {
            GlycoTraverser t_objTraverser;
            for (Iterator<GlycoGraphAlternative> t_iterAlt = a_objAlternative.getAlternatives().iterator(); t_iterAlt.hasNext();)
            {
                GlycoGraphAlternative t_objAlternative = t_iterAlt.next();
                t_objTraverser = this.getTraverser(this);
                t_objTraverser.traverseGraph(t_objAlternative);        
            }
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode)
     */
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException 
    {
        this.m_iUnvalidated++;
    }
    
    public void start(Sugar a_objSugar) throws GlycoVisitorException
    {
        this.clear();
        this.m_iUnderdetermined += a_objSugar.getUndeterminedSubTrees().size();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);       
        if ( this.m_bDescentUnderdeterminded )
        {
            for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();)
            {
                t_objTraverser = this.getTraverser(this);
                t_objTraverser.traverseGraph(t_iterUnder.next());
            }
        }
    }
    
    public void start(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException
    {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objRepeat);        
        this.m_iUnderdetermined += a_objRepeat.getUndeterminedSubTrees().size();
        if ( this.m_bDescentUnderdeterminded )
        {
            for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objRepeat.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();)
            {
                t_objTraverser = this.getTraverser(this);
                t_objTraverser.traverseGraph(t_iterUnder.next());
            }
        }
    }

    public void start(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException
    {
        GlycoTraverser t_objTraverser;
        this.clear();
        for (Iterator<GlycoGraphAlternative> t_iterAlt = a_objAlternative.getAlternatives().iterator(); t_iterAlt.hasNext();)
        {
            GlycoGraphAlternative t_objAlternative = t_iterAlt.next();
            t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(t_objAlternative);        
        }
    }
}