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
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/
package org.eurocarbdb.MolecularFramework.sugar;

import java.util.ArrayList;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.util.visitor.Visitable;

public abstract class GlycoNode implements Visitable
{
    protected GlycoEdge m_objParentLinkage = null;
    protected ArrayList<GlycoEdge> m_aChildLinkages = new ArrayList<GlycoEdge>();
    
    /**
     * Copies the properties of a node. Does not copy the GlycoEdges from and to the node.
     * @return copy of the node
     * @throws GlycoconjugateException
     */
    public abstract GlycoNode copy() throws GlycoconjugateException;
    
    protected void setChildEdge(ArrayList<GlycoEdge> a_aChilds) throws GlycoconjugateException
    {
        if ( a_aChilds == null )
        {
            throw new GlycoconjugateException("Null is not a valid set of edges.");
        }
        this.m_aChildLinkages.clear();
        for (Iterator<GlycoEdge> t_iterEdge = a_aChilds.iterator(); t_iterEdge.hasNext();)
        {
            this.addChildEdge(t_iterEdge.next());            
        }
    }
    
    public ArrayList<GlycoEdge> getChildEdges()
    {
        return this.m_aChildLinkages;
    }
    
    protected void setParentEdge(GlycoEdge a_objLinkage)
    {
        this.m_objParentLinkage = a_objLinkage;
    }
    
    public GlycoEdge getParentEdge()
    {
        return this.m_objParentLinkage;
    }
    
    /**
     * @return ArrayList of all child residues of this residue ( can contain null )
     */
    public ArrayList<GlycoNode> getChildNodes() 
    {
        ArrayList<GlycoNode> t_aResult = new ArrayList<GlycoNode>();
        for (Iterator<GlycoEdge> t_iterLinakges = this.m_aChildLinkages.iterator(); t_iterLinakges.hasNext();)
        {
            GlycoNode t_objResidue = t_iterLinakges.next().getChild();
            if ( !t_aResult.contains(t_objResidue) )
            {
                t_aResult.add( t_objResidue );
            }
        }
        return t_aResult;
    }
    
    /**
     * @return ArrayList of all Parent residues of this residue ( can contain null )
     */
    public GlycoNode getParentNode() 
    {
        if ( this.m_objParentLinkage == null )
        {
            return null;
        }
        return this.m_objParentLinkage.getParent();
    }
    
    /**
     * Adds a Child residue to this residue
     * @param a_linkSubStructure
     * @throws GlycoconjugateException 
     */
    protected boolean addChildEdge(GlycoEdge a_linkSubStructure) throws GlycoconjugateException 
    {
        if ( a_linkSubStructure == null )
        {
            throw new GlycoconjugateException("null edge is not allowed.");
        }
        if ( !this.m_aChildLinkages.contains(a_linkSubStructure) )
        {
            return this.m_aChildLinkages.add(a_linkSubStructure);
        }
        return false;
    }

    /**
     * @param linkage
     * @throws GlycoconjugateException 
     */
    protected boolean removeParentEdge(GlycoEdge a_objLinkage) throws GlycoconjugateException
    {
        if ( a_objLinkage != this.m_objParentLinkage )
        {
            throw new GlycoconjugateException("Cant delete invalid parent edge.");
        }
        this.m_objParentLinkage = null;
        return true;
    }

    /**
     * @param linkage
     */
    protected boolean removeChildEdge(GlycoEdge a_objLinkage) throws GlycoconjugateException
    {
        if ( a_objLinkage == null )
        {
            throw new GlycoconjugateException("Cant delete null linkage.");
        }
        if ( !this.m_aChildLinkages.contains(a_objLinkage) )
        {
            return false;
        }
        return this.m_aChildLinkages.remove(a_objLinkage);
    }

    /**
     * 
     */
    public void removeAllEdges()
    {
        this.m_objParentLinkage = null;
        this.m_aChildLinkages = new ArrayList<GlycoEdge>();        
    }
}