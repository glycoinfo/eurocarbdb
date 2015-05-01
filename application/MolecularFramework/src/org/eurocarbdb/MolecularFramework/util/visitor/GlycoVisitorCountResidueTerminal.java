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


/**
* will ignore underdeterminded subtrees
* @author Logan
*
*/
public class GlycoVisitorCountResidueTerminal implements GlycoVisitor
{
    private int m_iTerminalResidue;
    private int m_iTerminalBasetype;
    private int m_iTerminalMonosaccharide;
    private int m_iTerminalSubstituents;
    
    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.Monosaccharide)
     */
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException
    {
        ArrayList<GlycoEdge> t_objLinkages = a_objMonosaccharid.getChildEdges();

        if ( t_objLinkages.size() == 0 )
        {
            this.m_iTerminalResidue++;
            this.m_iTerminalBasetype++;
            this.m_iTerminalMonosaccharide++;
        }          
        else
        {
            boolean t_bTerminal = true;
            GlycoVisitorNodeType t_objType = new GlycoVisitorNodeType();
            for (Iterator<GlycoNode> t_iterChild = a_objMonosaccharid.getChildNodes().iterator(); t_iterChild.hasNext();) 
            {
                GlycoNode t_objChild = t_iterChild.next();
                if ( t_objType.isMonosaccharide(t_objChild))
                {
                    t_bTerminal = false;
                }
                else if ( t_objType.isSubstituent(t_objChild))
                {
                    if ( t_objChild.getChildEdges().size() != 0 )
                    {
                        t_bTerminal = false;
                    }
                }
                else if ( t_objType.isSugarUnitAlternative(t_objChild) )
                {
                    t_bTerminal = false;
                }
                else if ( t_objType.isSugarUnitCyclic(t_objChild) )
                {
                    t_bTerminal = false;
                }
                else if ( t_objType.isSugarUnitRepeat(t_objChild) )
                {
                    t_bTerminal = false;
                }
            }
            if ( t_bTerminal )
            {
                this.m_iTerminalMonosaccharide++;
                this.m_iTerminalResidue++;
            }
        }
    }

    /**
     * @throws GlycoVisitorException 
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.NonMonosaccharide)
     */
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException
    {
        throw new GlycoVisitorException("NonMonosaccharides are not allowed.");
    }

   

    /**
     * @throws GlycoVisitorException 
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.SugarRepeatingUnit)
     */
    public void visit(SugarUnitRepeat a_objRepeate) throws GlycoVisitorException 
    {
        GlycoTraverser t_trav = this.getTraverser(this);
        t_trav.traverseGraph(a_objRepeate);
        GlycoNode t_objNode = a_objRepeate.getRepeatLinkage().getParent();
        GlycoVisitorNodeType t_visType = new GlycoVisitorNodeType();
        if ( t_objNode.getChildEdges().size() == 0 )
        {
            if ( t_visType.isMonosaccharide(t_objNode))
            {
                this.m_iTerminalMonosaccharide--;
                this.m_iTerminalResidue--;
                this.m_iTerminalBasetype--;
            }
            else
            {
                this.m_iTerminalSubstituents--;
                this.m_iTerminalResidue--;
            }
        }
        else
        {
            if ( t_visType.isMonosaccharide(t_objNode))
            {
                boolean t_bTerminal = true;
                for (Iterator<GlycoNode> t_iterChild = t_objNode.getChildNodes().iterator(); t_iterChild.hasNext();) 
                {
                    GlycoNode t_objChild = t_iterChild.next();
                    if ( t_visType.isMonosaccharide(t_objChild))
                    {
                        t_bTerminal = false;
                    }
                    else if ( t_visType.isSubstituent(t_objChild))
                    {
                        if ( t_objChild.getChildEdges().size() != 0 )
                        {
                            t_bTerminal = false;
                        }
                    }
                    else if ( t_visType.isSugarUnitAlternative(t_objChild) )
                    {
                        t_bTerminal = false;
                    }
                    else if ( t_visType.isSugarUnitCyclic(t_objChild) )
                    {
                        t_bTerminal = false;
                    }
                    else if ( t_visType.isSugarUnitRepeat(t_objChild) )
                    {
                        t_bTerminal = false;
                    }
                }
                if ( t_bTerminal )
                {
                    this.m_iTerminalMonosaccharide--;
                    this.m_iTerminalResidue--;                    
                }                
            }
        }
        for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objRepeate.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();) 
        {
            t_trav = this.getTraverser(this);
            t_trav.traverseGraph(t_iterUnder.next());            
        }
    }

    /**
     * @throws GlycoVisitorException 
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#getTraverser(de.glycosciences.MolecularFrameWork.util.SugarVisitor)
     */
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException
    {
        return new GlycoTraverserSimple(a_objVisitor);
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#clear()
     */
    public void clear()
    {
        this.m_iTerminalMonosaccharide = 0;   
        this.m_iTerminalResidue = 0;
        this.m_iTerminalSubstituents = 0;
        this.m_iTerminalBasetype = 0;
    }

    public int getTerminalCountResidue()
    {
        return this.m_iTerminalResidue;
    }
    
    public int getTerminalMonosaccharide()
    {
        return this.m_iTerminalMonosaccharide;
    }    

    public int getTerminalBasetype()
    {
        return this.m_iTerminalBasetype;
    }
    
    public int getTerminalSubstituent()
    {
        return this.m_iTerminalSubstituents;
    }

    public void start(Sugar a_objSugar) throws GlycoVisitorException  
    {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();) 
        {
            t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(t_iterUnder.next());            
        }
    }

    public void start(GlycoNode a_objResidue) throws GlycoVisitorException 
    {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverse(a_objResidue);
    }

    /**
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.Substituent)
     */
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException 
    {
        ArrayList<GlycoEdge> t_objLinkages = a_objSubstituent.getChildEdges();

        if ( t_objLinkages.size() == 0 )
        {
            this.m_iTerminalResidue++;
            this.m_iTerminalSubstituents++;
        }
    }

    /**
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.SugarUnitCyclic)
     */
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException 
    {}

    /**
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("SugarUnitAlternative are not allowed.");
    }

    /**
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.UnvalidatedGlycoNode)
     */
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("UnvalidatedGlycoNode are not allowed.");
    }

    /**
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.GlycoEdge)
     */
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException 
    {
        // nothing to do
    }

}
