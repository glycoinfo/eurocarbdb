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

import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
* @author rene
*
*/
public class SugarUnitCyclic extends GlycoNode
{
    private GlycoNode m_objCyclicStart = null;    
    
    protected SugarUnitCyclic(GlycoNode a_objStart)
    {
        this.m_objCyclicStart = a_objStart;
    }
    
    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.Visitable#accept(org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor)
     */
    public void accept(GlycoVisitor a_objVisitor) throws GlycoVisitorException
    {
        a_objVisitor.visit(this);        
    }

    public GlycoNode getCyclicStart()
    {
        return this.m_objCyclicStart;
    }
    
    public void setCyclicStart(GlycoNode a_objNode) throws GlycoconjugateException
    {
        if ( a_objNode == null )
        {
            throw new GlycoconjugateException("Invalide value for cyclic residue.");
        }
        this.m_objCyclicStart = a_objNode;
    }
    
    protected void setChildEdge(ArrayList<GlycoEdge> a_aChilds) throws GlycoconjugateException
    {
        throw new GlycoconjugateException("Cyclic objects can not have childs.");
    }
    
    protected boolean addChildEdge(GlycoEdge a_linkSubStructure) throws GlycoconjugateException 
    {
        throw new GlycoconjugateException("Cyclic objects can not have childs.");
    }

    /**
     * Copy the SugarUnitCyclic. Start residue is the SAME residue
     */
    public SugarUnitCyclic copy() throws GlycoconjugateException
    {
        return new SugarUnitCyclic( this.m_objCyclicStart );
    }    
}
