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
package org.eurocarbdb.MolecularFramework.util.analytical.misc;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserTree;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;



/**
* @author sahnemann
*
*/
public class GlycoVisitorCountNodeType implements GlycoVisitor
{
    private int m_iSubstituentCount;
    private int m_iNonMonosaccharideCount;
    private int m_iCyclicCount;
    private int m_iMonosaccharideCount;
    private int m_iRepeatCount;
    private int m_iAlternativeCount;
    private int m_iUnvalidatedGlycoNode;
    
    private GlycoTraverser m_objTraverser;
    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.Monosaccharide)
     */
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            this.m_iMonosaccharideCount++;
        }
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.NonMonosaccharide)
     */
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            this.m_iNonMonosaccharideCount++;
        }
    }

   

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.GlycosidicLinkage)
     */
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException
    {
        // Nothing to do        
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.SugarRepeatingUnit)
     */
    public void visit(SugarUnitRepeat a_objRepeate) throws GlycoVisitorException
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            GlycoTraverser t_trav = this.m_objTraverser;
            this.m_objTraverser = this.getTraverser(this);
            this.m_objTraverser.traverseGraph(a_objRepeate);
            this.m_objTraverser= t_trav;  
            
            this.m_iRepeatCount++;
        }
    }

    /**
     * @throws GlycoVisitorException 
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#getTraverser(de.glycosciences.MolecularFrameWork.util.SugarVisitor)
     */
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException
    {
        return new GlycoTraverserTree(a_objVisitor);
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.Substituent)
     */
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException {
         if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
            {
                this.m_iSubstituentCount++;
            }
        
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.SugarUnitCyclic)
     */
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            this.m_iCyclicCount++;
        }
        
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            this.m_iAlternativeCount++;
        }
        
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.UnvalidatedGlycoNode)
     */
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            this.m_iUnvalidatedGlycoNode++;
        }
        
    }

    
    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#clear()
     */
    public void clear()
    {
        this.m_iRepeatCount = 0;
        this.m_iMonosaccharideCount = 0;
        this.m_iNonMonosaccharideCount = 0;
        this.m_iCyclicCount = 0;
        this.m_iSubstituentCount = 0;
        this.m_iAlternativeCount = 0;
        this.m_iUnvalidatedGlycoNode = 0;
    }
    
    public int getNonMonosaccharideCount()
    {
        return this.m_iNonMonosaccharideCount;
    }
    
    public int getMonosaccharideCount()
    {
        return this.m_iMonosaccharideCount;
    }
    
    public int getSubstituentCount()
    {
        return this.m_iSubstituentCount;
    }
    
    
    public int getRepeatCount()
    {
        return this.m_iRepeatCount;
    }
    
    public int getAlternativeNodeCount ()
    {
        return this.m_iAlternativeCount;
    }
    
    public int getCyclicCount ()
    {
        return this.m_iCyclicCount;
    }
    
    public int getUnvalidatedNodeCount ()
    {
        return this.m_iUnvalidatedGlycoNode;
    }
    
    
    public void start(Sugar a_objSugar) throws GlycoVisitorException
    {
        this.clear();
        this.m_objTraverser = this.getTraverser(this);
        this.m_objTraverser.traverseGraph(a_objSugar);
    }

    public void start(GlycoNode a_objResidue) throws GlycoVisitorException 
    {
        this.clear();
        this.m_objTraverser = this.getTraverser(this);
        this.m_objTraverser.traverse(a_objResidue);
    }
}