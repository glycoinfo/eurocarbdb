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
package org.eurocarbdb.MolecularFramework.sugar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

public class GlycoGraphAlternative implements GlycoGraph 
{
    private ArrayList<GlycoNode> m_aResidues = new ArrayList<GlycoNode>();
    private GlycoNode m_objLeadInNode = null;
    private HashMap<GlycoNode,GlycoNode> m_hLeadOutNodeToNode = new HashMap<GlycoNode,GlycoNode>(); 
    
    /**
     * Delivers all Residues that does not have a parent residue.
     * @throws GlycoconjugateException if the structure contain a cyclic part or if the sugar contain no residue with a parent 
     * @see de.glycosciences.glycoconjugate.GlycoGraph#getRootResiduesForNonCyclicSugars()
     */
    public ArrayList<GlycoNode> getRootNodes() throws GlycoconjugateException 
    {
        ArrayList<GlycoNode> t_aResult = new ArrayList<GlycoNode>();
        GlycoNode t_objResidue;
        // for all residues of the sugar
        Iterator<GlycoNode> t_iterResidue = this.getNodeIterator();
        while (t_iterResidue.hasNext())
        {
            t_objResidue = t_iterResidue.next();
            GlycoEdge t_objParents = t_objResidue.getParentEdge();
            if (  t_objParents == null )
            {
                t_aResult.add(t_objResidue);
            }
        }
        if ( t_aResult.size() < 1 )
        {
            throw new GlycoconjugateException("Sugar seems not to have at least one root residue");
        }
        return t_aResult;
    }

    /**
     * @see de.glycosciences.glycoconjugate.GlycoGraph#getResidueIterator()
     */
    public Iterator<GlycoNode> getNodeIterator()
    {
        return this.m_aResidues.iterator();
    }
    
    /**
     * @see de.glycosciences.MolecularFrameWork.sugar.GlycoGraph#isConnected()
     */
    public boolean isConnected() throws GlycoconjugateException
    {
        ArrayList<GlycoNode> t_objRoots = this.getRootNodes();
        if ( t_objRoots.size() > 1 )
        {
            return false;
        }
        return true;
    }
    
    public boolean removeNode(GlycoNode a_objResidue) throws GlycoconjugateException
    {
        GlycoEdge t_objLinkage;
        GlycoNode t_objResidue;
        if ( a_objResidue == null )
        {
            throw new GlycoconjugateException("Invalide residue.");
        }
        t_objLinkage = a_objResidue.getParentEdge();
        if ( t_objLinkage != null )
        {
            t_objResidue = t_objLinkage.getParent();
            if ( t_objResidue == null )
            {
                throw new GlycoconjugateException("A linkage with a null parent exists.");
            }
            t_objResidue.removeChildEdge(t_objLinkage);
        }
        for (Iterator<GlycoEdge> t_iterEdges = a_objResidue.getChildEdges().iterator(); t_iterEdges.hasNext();)
        {
            t_objLinkage = t_iterEdges.next();
            t_objResidue = t_objLinkage.getChild();
            if ( t_objResidue == null )
            {
                throw new GlycoconjugateException("A linkage with a null child exists.");
            }
            t_objResidue.removeParentEdge(t_objLinkage);
        }
        return this.m_aResidues.remove(a_objResidue);
    }

    public ArrayList<GlycoNode> getNodes()
    {
        return this.m_aResidues;
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.sugar.GlycoGraph#addNode(org.eurocarbdb.MolecularFramework.sugar.GlycoNode)
     */
    public boolean addNode(GlycoNode a_objResidue) throws GlycoconjugateException
    {
        if ( a_objResidue == null )
        {
            throw new GlycoconjugateException("Invalide residue.");
        }
        GlycoVisitorNodeType t_objNodeType = new GlycoVisitorNodeType();
        if ( !this.m_aResidues.contains(a_objResidue) )
        {
            try 
            {
                if ( t_objNodeType.isSugarUnitCyclic(a_objResidue) )
                {
                    throw new GlycoconjugateException("Cyclic unit are not allowed in alternative graphs.");
                }
                if ( t_objNodeType.isSugarUnitRepeat(a_objResidue) )
                {
                    throw new GlycoconjugateException("repeat unit are not allowed in alternative graphs.");
                }
                if ( t_objNodeType.isSugarUnitAlternative(a_objResidue) )
                {
                    throw new GlycoconjugateException("Alternative units are not allowed in alternative graphs.");
                }
            }
            catch (GlycoVisitorException e) 
            {
                throw new GlycoconjugateException(e.getMessage(),e);
            } 
            a_objResidue.removeAllEdges();
            return this.m_aResidues.add(a_objResidue);
        }
        return false;
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.sugar.GlycoGraph#addNode(org.eurocarbdb.MolecularFramework.sugar.GlycoNode, org.eurocarbdb.MolecularFramework.sugar.GlycoEdge, org.eurocarbdb.MolecularFramework.sugar.GlycoNode)
     */
    public boolean addNode(GlycoNode a_objParent, GlycoEdge a_objLinkage, GlycoNode a_objChild) throws GlycoconjugateException
    {
        if ( a_objParent == null || a_objChild == null )
        {
            throw new GlycoconjugateException("Invalide residue.");
        }
        if ( a_objLinkage == null )
        {
            throw new GlycoconjugateException("Invalide linkage.");
        }
        if ( a_objChild.getParentEdge() !=  null )
        {
            throw new GlycoconjugateException("The child residue has a parent residue.");
        }
        this.addNode(a_objParent);
        this.addNode(a_objChild);
        if ( this.m_aResidues.contains(a_objChild) && this.m_aResidues.contains(a_objChild) )
        {
            // test for indirect cyclic structures
            if ( this.isParent(a_objChild,a_objParent) || a_objChild == a_objParent )
            {
                throw new GlycoconjugateException("Cyclic structures are not allowed in alternative glyco graphs.");
            }
        }
        else
        {
            throw new GlycoconjugateException("Critical error imposible to add residue.");
        }
        a_objChild.setParentEdge(a_objLinkage);
        a_objParent.addChildEdge(a_objLinkage);
        a_objLinkage.setChild(a_objChild);
        a_objLinkage.setParent(a_objParent);
        return true;
    }
    
    /**
     * @see org.eurocarbdb.MolecularFramework.sugar.GlycoGraph#addEdge(org.eurocarbdb.MolecularFramework.sugar.GlycoNode, org.eurocarbdb.MolecularFramework.sugar.GlycoNode, org.eurocarbdb.MolecularFramework.sugar.GlycoEdge)
     */
    public boolean addEdge(GlycoNode a_objParent, GlycoNode a_objChild, GlycoEdge a_objLinkage) throws GlycoconjugateException
    {
        return this.addNode(a_objParent,a_objLinkage,a_objChild);        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.sugar.GlycoGraph#containsNode(org.eurocarbdb.MolecularFramework.sugar.GlycoNode)
     */
    public boolean containsNode(GlycoNode a_objNode)
    {
        return this.m_aResidues.contains(a_objNode);
    }
    
    /**Recursive check if query node has specified parent
     * @param child
     * @param parent
     * @return
     */
    public boolean isParent(GlycoNode a_objParent, GlycoNode a_objNode)
    {
        GlycoNode t_objParent = a_objNode.getParentNode();
        if ( t_objParent == null )
        {
            return false;
        }
        if ( t_objParent == a_objParent )
        {
            return true;
        }
        return this.isParent(a_objParent,t_objParent);
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.sugar.GlycoGraph#removeEdge(org.eurocarbdb.MolecularFramework.sugar.GlycoEdge)
     */
    public boolean removeEdge(GlycoEdge a_objEdge) throws GlycoconjugateException
    {
        GlycoNode t_objChild = a_objEdge.getChild();
        GlycoNode t_objParent = a_objEdge.getParent();
        if ( a_objEdge == null )
        {
            return false;
        }
        if ( t_objChild == null || t_objParent == null )
        {
            throw new GlycoconjugateException("The edge contains null values.");
        }
        if ( t_objChild.getParentEdge() != a_objEdge )
        {
            throw new GlycoconjugateException("The child attachment is not correct");
        }
        ArrayList<GlycoEdge> t_aEdges = t_objParent.getChildEdges();
        if ( !t_aEdges.contains(a_objEdge) )
        {
            throw new GlycoconjugateException("The parent attachment is not correct");
        }
        t_objChild.removeParentEdge(a_objEdge);
        t_objParent.removeChildEdge(a_objEdge);
        return true;
    }
    
    public GlycoGraphAlternative copy() throws GlycoconjugateException 
    {
        HashMap<GlycoNode,GlycoNode> t_hashResidues = new HashMap<GlycoNode,GlycoNode>();
        GlycoGraphAlternative t_objCopy = new GlycoGraphAlternative();
        GlycoNode t_objNodeOne;
        GlycoNode t_objNodeTwo;
        GlycoEdge t_objLinkOriginal;
        GlycoEdge t_objLinkCopy;
        ArrayList<GlycoEdge> t_aLinkages;
        // copy all nodes
        for (Iterator<GlycoNode> t_iterNode = this.m_aResidues.iterator(); t_iterNode.hasNext();) 
        {
            t_objNodeOne = t_iterNode.next();
            t_objNodeTwo = t_objNodeOne.copy();
            t_hashResidues.put(t_objNodeOne,t_objNodeTwo);
            t_objCopy.addNode(t_objNodeTwo);
        }
        // copy linkages
        for (Iterator<GlycoNode> t_iterNode = this.m_aResidues.iterator(); t_iterNode.hasNext();) 
        {
            t_objNodeOne = t_iterNode.next();
            t_aLinkages = t_objNodeOne.getChildEdges();
            for (Iterator<GlycoEdge> t_iterLinkages = t_aLinkages.iterator(); t_iterLinkages.hasNext();) 
            {
                t_objLinkOriginal = t_iterLinkages.next();
                t_objLinkCopy = t_objLinkOriginal.copy();
                t_objNodeOne = t_hashResidues.get(t_objLinkOriginal.getParent());
                t_objNodeTwo = t_hashResidues.get(t_objLinkOriginal.getChild());
                if ( t_objNodeOne == null || t_objNodeTwo == null )
                {
                    throw new GlycoconjugateException("Impossible to copy alternative glyco graph. Null values in copy.");
                }
                t_objCopy.addEdge( t_objNodeOne, t_objNodeTwo, t_objLinkCopy);
            }
        }
        // copy parent connection information
        if ( this.m_objLeadInNode != null )
        {
            t_objCopy.setLeadInNode( t_hashResidues.get(this.m_objLeadInNode) );
        }
        // copy child connection information
        for (Iterator<GlycoNode> t_iterChilds = this.m_hLeadOutNodeToNode.keySet().iterator(); t_iterChilds.hasNext();)
        {
            GlycoNode t_objChild = t_iterChilds.next();
            t_objNodeOne = t_hashResidues.get(this.m_hLeadOutNodeToNode.get(t_objChild));
            if ( t_objNodeOne == null )
            {
                throw new GlycoconjugateException("Impossible to copy child connection. Null values in copy.");
            }
            t_objCopy.addLeadOutNodeToNode(t_objChild,t_objNodeOne);
        }
        return t_objCopy;
    }
    
    protected void setLeadInNode(GlycoNode a_objNode) throws GlycoconjugateException
    {
        if ( !this.m_aResidues.contains(a_objNode) )
        {
            throw new GlycoconjugateException("Parent residue is not part of the glyco graph.");
        }
        this.m_objLeadInNode = a_objNode;
    }
    
    public GlycoNode getLeadInNode()
    {
        return this.m_objLeadInNode;
    }
    
    protected void addLeadOutNodeToNode(GlycoNode a_objChildResidue, GlycoNode a_objNode) throws GlycoconjugateException
    {
        if ( !this.m_aResidues.contains(a_objNode) )
        {
            throw new GlycoconjugateException("Child residue is not part of the glyco graph.");
        }
        this.m_hLeadOutNodeToNode.put(a_objChildResidue,a_objNode);
    }
    
    public HashMap<GlycoNode,GlycoNode> getLeadOutNodeToNode()
    {
        return this.m_hLeadOutNodeToNode;
    }
    
    public boolean removeLeadOutNodeToNode(GlycoEdge a_objEdge, GlycoNode a_objNode)
    {
        if ( !this.m_hLeadOutNodeToNode.containsKey(a_objEdge) )
        {
            return false;
        }
        if ( this.m_hLeadOutNodeToNode.get(a_objEdge) != a_objNode )
        {
            return false;
        }
        this.m_hLeadOutNodeToNode.remove(a_objEdge);
        return true;
    }

    /**
     * 
     */
    public void removeAllLeadOutNodes()
    {
        this.m_hLeadOutNodeToNode.clear();        
    }
}