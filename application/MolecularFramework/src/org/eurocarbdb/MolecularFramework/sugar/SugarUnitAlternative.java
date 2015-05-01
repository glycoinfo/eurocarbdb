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
package org.eurocarbdb.MolecularFramework.sugar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
* @author rene
*
*/
public class SugarUnitAlternative extends GlycoNode
{
    private ArrayList<GlycoGraphAlternative> m_aAlternatives = new ArrayList<GlycoGraphAlternative>(); 
    
    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.Visitable#accept(org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor)
     */
    public void accept(GlycoVisitor a_objVisitor) throws GlycoVisitorException
    {
        a_objVisitor.visit(this);
    }

    public void setAlternatives(ArrayList<GlycoGraphAlternative> a_aList) throws GlycoconjugateException
    {
        if ( a_aList == null )
        {
            throw new GlycoconjugateException("null is not a valid set of alternatives");
        }
        this.m_aAlternatives.clear();
        for (Iterator<GlycoGraphAlternative> t_iterAlter = a_aList.iterator(); t_iterAlter.hasNext();)
        {
            this.addAlternative(t_iterAlter.next());
        }
    }
    
    public ArrayList<GlycoGraphAlternative> getAlternatives()
    {
        return this.m_aAlternatives;
    }
    
    public void addAlternative(GlycoGraphAlternative a_objAlternative) throws GlycoconjugateException
    {
        if ( a_objAlternative == null )
        {
            throw new GlycoconjugateException("null is not a valid residue.");
        }
        if ( !this.m_aAlternatives.contains(a_objAlternative) )
        {
            this.m_aAlternatives.add(a_objAlternative);
        }
    }
    
    public void removeAlternative(GlycoNode a_objAlternative) throws GlycoconjugateException
    {
        if ( !this.m_aAlternatives.contains(a_objAlternative) )
        {
            throw new GlycoconjugateException("Can't remove invalid alternative residue.");
        }
        this.m_aAlternatives.remove(a_objAlternative);
    }

    /**
     * Copies the alternative resiude. All alternative graphs are copied. For the child connections the tuple "old egde" "new glyconode" is set. 
     * @see org.eurocarbdb.MolecularFramework.sugar.GlycoNode#copy()
     */
    public SugarUnitAlternative copy() throws GlycoconjugateException
    {
        SugarUnitAlternative t_objCopy = new SugarUnitAlternative();
        for (Iterator<GlycoGraphAlternative> t_iterNodes = this.m_aAlternatives.iterator(); t_iterNodes.hasNext();)
        {
            t_objCopy.addAlternative(t_iterNodes.next().copy());            
        }
        return t_objCopy;
    }
    
    public void setLeadInNode(GlycoNode a_objParent,GlycoGraphAlternative a_objAlternative) throws GlycoconjugateException
    {
        if ( !this.m_aAlternatives.contains(a_objAlternative) )
        {
            throw new GlycoconjugateException("GlycoGraphAlternative is not part of this sugar unit alternative.");
        }
        if ( this.m_objParentLinkage == null )
        {
            throw new GlycoconjugateException("This sugar unit alternative does not have a parent linkage.");
        }
        a_objAlternative.setLeadInNode(a_objParent);
    }

    public void addLeadOutNodeToNode(GlycoNode a_objParent,GlycoGraphAlternative a_objAlternative,GlycoNode a_objChild) throws GlycoconjugateException
    {
        if ( !this.m_aAlternatives.contains(a_objAlternative) )
        {
            throw new GlycoconjugateException("GlycoGraphAlternative is not part of this sugar unit alternative.");
        }
        boolean t_bFound = false;
        for (Iterator<GlycoEdge> t_iterLinkages = this.m_aChildLinkages.iterator(); t_iterLinkages.hasNext();) 
        {
            if ( a_objChild == t_iterLinkages.next().getChild() )
            {
                t_bFound = true;
            }            
        }
        if ( !t_bFound )
        {
            throw new GlycoconjugateException("This sugar unit alternative does not have this child linkage.");
        }
        a_objAlternative.addLeadOutNodeToNode(a_objChild,a_objParent);
    }

    /**
     * @param mapNew
     * @param altGraph
     * @throws GlycoconjugateException 
     */
    public void setLeadOutNodeToNode(HashMap<GlycoNode, GlycoNode> a_hNodeToNode, GlycoGraphAlternative a_objAltGraph) throws GlycoconjugateException
    {
        a_objAltGraph.removeAllLeadOutNodes();
        for (Iterator<GlycoNode> t_iterKEy = a_hNodeToNode.keySet().iterator(); t_iterKEy.hasNext();)
        {
            GlycoNode t_objKEy = t_iterKEy.next();
            this.addLeadOutNodeToNode(a_hNodeToNode.get(t_objKEy),a_objAltGraph,t_objKEy);
        }
    }
}
