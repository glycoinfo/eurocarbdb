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
package org.eurocarbdb.MolecularFramework.util.visitor;

import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraphAlternative;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
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
* Takes an unvalidated sugar and replaces all occurence of a pattern in the names of
* UnvalidatedGlycoNode. Case sensitive.
* 
* @author rene
*
*/
public class GlycoVisitorReplaceName implements GlycoVisitor
{
    private String m_strPattern = "";
    private String m_strName = "";
    
    /**
     * @param string
     */
    public GlycoVisitorReplaceName(String a_strPattern,String a_strName)
    {
        this.m_strPattern = a_strPattern;
        this.m_strName = a_strName;
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.Monosaccharide)
     */
    public void visit(Monosaccharide arg0) throws GlycoVisitorException
    {
        // do nothing
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.NonMonosaccharide)
     */
    public void visit(UnvalidatedGlycoNode a_objNonMonosaccharide) throws GlycoVisitorException
    {
        String t_strName = a_objNonMonosaccharide.getName();
        int t_iPos = t_strName.indexOf(this.m_strPattern);
        if ( t_iPos != -1 )
        {
            String t_strNameOld = t_strName.substring(t_iPos,t_iPos+this.m_strPattern.length());
            try 
            {
                a_objNonMonosaccharide.setName(t_strName.replaceAll(t_strNameOld, this.m_strName ));
            } 
            catch (GlycoconjugateException e) 
            {
                throw new GlycoVisitorException(e.getMessage(),e);
            }            
        }
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.Sugar)
     */
    public void visit(Sugar a_objSugar) throws GlycoVisitorException
    {
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.SimpleGlycosidicLinkage)
     */
    public void visit(GlycoEdge arg0) throws GlycoVisitorException
    {
        // nothing to do        
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.SugarRepeatingUnit)
     */
    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException
    {
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objRepeat);
        for (UnderdeterminedSubTree t_oSubtree : a_objRepeat.getUndeterminedSubTrees())
        {
            t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(t_oSubtree);
        }
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#start(de.glycosciences.MolecularFrameWork.sugar.Sugar)
     */
    public void start(Sugar a_objSugar) throws GlycoVisitorException
    {
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        for (UnderdeterminedSubTree t_oSubtree : a_objSugar.getUndeterminedSubTrees())
        {
            t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(t_oSubtree);
        }
    }

    /**
     * @throws GlycoVisitorException 
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#getTraverser(de.glycosciences.MolecularFrameWork.util.SugarVisitor)
     */
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException
    {
        return new GlycoTraverserNodes(a_objVisitor);
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#clear()
     */
    public void clear()
    {
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide)
     */
    public void visit(NonMonosaccharide arg0) throws GlycoVisitorException 
    {
        // do nothing        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Substituent)
     */
    public void visit(Substituent arg0) throws GlycoVisitorException 
    {
        // do nothing        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic)
     */
    public void visit(SugarUnitCyclic arg0) throws GlycoVisitorException 
    {
        // do nothing        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException 
    {
        for (Iterator<GlycoGraphAlternative> t_iterGraphs = a_objAlternative.getAlternatives().iterator(); t_iterGraphs.hasNext();)
        {
            GlycoGraphAlternative t_objGraph = t_iterGraphs.next();
            GlycoTraverser t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(t_objGraph);
        }            
    }
}
