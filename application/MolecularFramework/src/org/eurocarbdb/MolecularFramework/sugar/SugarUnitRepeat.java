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

import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

public class SugarUnitRepeat extends GlycoNode implements GlycoGraph
{
    public static final int UNKNOWN   = -1;
    
    private GlycoEdge m_linkRepeatLinkage = null;
    private int m_iMinCount = SugarUnitRepeat.UNKNOWN;
    private int m_iMaxCount = SugarUnitRepeat.UNKNOWN;
    private ArrayList<GlycoNode> m_aResidues = new ArrayList<GlycoNode>();
    private ArrayList<UnderdeterminedSubTree> m_aSpezialTrees = new ArrayList<UnderdeterminedSubTree>(); 
    
    
    public SugarUnitRepeat() 
    {
        super();
        this.m_aResidues.clear();
    }
    
    /**
     * 
     * @return minima count for this repeat unit ; -1 for unknown 
     */
    public int getMinRepeatCount() 
    {
        return this.m_iMinCount;
    }
    
    /**
     * 
     * @return maxima count for this repeat unit ; -1 for unknown 
     */
    public int getMaxRepeatCount() 
    {
        return this.m_iMaxCount;
    }
    
    /**
     *  
     */
    public void setMinRepeatCount(int a_iCount) 
    {
        this.m_iMinCount = a_iCount;
    }
    
    /**
     *  
     */
    public void setMaxRepeatCount(int a_iCount) 
    {
        this.m_iMaxCount = a_iCount;
    }

    /**
     * Sets the repeat linkage
     * @param a_objLinkage
     * @param a_objChild 
     * @param a_objParent 
     * @throws GlycoconjugateException 
     */
    public void setRepeatLinkage(GlycoEdge a_objLinkage, GlycoNode a_objParent, GlycoNode a_objChild) throws GlycoconjugateException 
    {
        if ( a_objChild == null || a_objLinkage == null || a_objParent == null )
        {
            throw new GlycoconjugateException("null is not valide for a internal repeat linkage and residue.");
        }
        this.m_linkRepeatLinkage = a_objLinkage;
        this.m_linkRepeatLinkage.setParent(a_objParent);
        this.m_linkRepeatLinkage.setChild(a_objChild);
    }
    
    /** 
     * Delivers the repeat linkage.
     * 
     * @return repeat linkage
     */
    public GlycoEdge getRepeatLinkage() 
    {
        return this.m_linkRepeatLinkage;
    }

    /**
     * @see org.eurocarbdb.util.Visitable#accept(org.eurocarbdb.util.GlycoVisitor)
     */
    public void accept(GlycoVisitor a_objVisitor) throws GlycoVisitorException 
    {
        a_objVisitor.visit(this);        
    }

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
            throw new GlycoconjugateException("Repeat unit seems not to have at least one root residue");
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
        if ( a_objResidue.getClass() != SugarUnitCyclic.class )
        {
            this.searchCyclicForDeleting(a_objResidue);
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
        if ( !this.m_aResidues.contains(a_objResidue) )
        {
            try 
            {
                GlycoVisitorNodeType t_objNodeType = new GlycoVisitorNodeType();
                if ( t_objNodeType.isSugarUnitCyclic(a_objResidue) )
                {
                    throw new GlycoconjugateException("Cyclic unit are not allowed in repeat units.");
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
        this.addNode(a_objChild);
        this.addNode(a_objParent);
        if ( !this.m_aResidues.contains(a_objChild) || !this.m_aResidues.contains(a_objParent) )
        {
            throw new GlycoconjugateException("Could not add residue to repeat unit.");
        }
        // test for indirect cyclic structures
        if ( this.isParent(a_objChild,a_objParent) )
        {
            throw new GlycoconjugateException("You try to create a cyclic sugar, cyclic units are not allowed in repeat units.");
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

    /**
     * @param residue
     * @throws GlycoconjugateException 
     */
    private void searchCyclicForDeleting(GlycoNode a_objResidue) throws GlycoconjugateException
    {
        for (Iterator<GlycoNode> t_iterNodes = this.m_aResidues.iterator(); t_iterNodes.hasNext(); )
        {
            GlycoNode t_objElement = t_iterNodes.next();
            if ( t_objElement.getClass() == SugarUnitCyclic.class )
            {
                SugarUnitCyclic t_objCyclic = (SugarUnitCyclic) t_objElement;
                if ( t_objCyclic.getCyclicStart() == a_objResidue )
                {
                    this.removeNode(t_objElement);
                }
            }
        }        
    }    

    /**
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

    public SugarUnitRepeat copy() throws GlycoconjugateException 
    {
        ArrayList<SugarUnitAlternative> t_aAlternative = new ArrayList<SugarUnitAlternative>();
        HashMap<GlycoNode,GlycoNode> t_hashResidues = new HashMap<GlycoNode,GlycoNode>();
        SugarUnitRepeat t_objCopy = new SugarUnitRepeat();
        GlycoNode t_objNodeOne;
        GlycoNode t_objNodeTwo;
        GlycoEdge t_objLinkOriginal;
        GlycoEdge t_objLinkCopy;
        ArrayList<GlycoEdge> t_aLinkages;
        GlycoVisitorNodeType t_visNodeType = new GlycoVisitorNodeType();
        // copy all nodes
        for (Iterator<GlycoNode> t_iterNode = this.m_aResidues.iterator(); t_iterNode.hasNext();) 
        {
            t_objNodeOne = t_iterNode.next();
            try 
            {
                if ( !t_visNodeType.isSugarUnitCyclic(t_objNodeOne) )
                {
                    if ( t_visNodeType.isSugarUnitAlternative(t_objNodeOne) )
                    {
                        throw new GlycoconjugateException("Unable to copy alternative residues.");
                    }
                    else
                    {
                        t_objNodeTwo = t_objNodeOne.copy();
                        t_hashResidues.put(t_objNodeOne,t_objNodeTwo);
                        t_objCopy.addNode(t_objNodeTwo);
                    }
                }
            } 
            catch (GlycoVisitorException e) 
            {
                throw new GlycoconjugateException(e.getMessage(),e);
            } 
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
                    throw new GlycoconjugateException("Impossible to copy repeat unit. Null values in copy.");
                }
                t_objCopy.addEdge( t_objNodeOne, t_objNodeTwo, t_objLinkCopy);
            }
        }
        // copy repeat linkage
        t_objLinkOriginal = this.m_linkRepeatLinkage;
        t_objLinkCopy = t_objLinkOriginal.copy();
        t_objNodeOne = t_hashResidues.get(t_objLinkOriginal.getParent());
        t_objNodeTwo = t_hashResidues.get(t_objLinkOriginal.getChild());
        if ( t_objNodeOne == null || t_objNodeTwo == null )
        {
            throw new GlycoconjugateException("Impossible to copy repeat unit. Null values in copy.");
        }
        t_objCopy.setRepeatLinkage(t_objLinkCopy,t_objNodeOne,t_objNodeTwo);
        // copie repeat
        t_objCopy.setMinRepeatCount( this.m_iMinCount );
        t_objCopy.setMaxRepeatCount( this.m_iMaxCount );  
        // correkt alternative attache positions
        for (Iterator<SugarUnitAlternative> t_iterAlternatives = t_aAlternative.iterator(); t_iterAlternatives.hasNext();) 
        {
            SugarUnitAlternative t_objAlternative = t_iterAlternatives.next();
            GlycoGraphAlternative t_objAltGraph;
            GlycoNode t_objKeyNode;
            GlycoNode t_objKeyNodeNew;
            HashMap<GlycoNode,GlycoNode> t_objMapNew = new HashMap<GlycoNode,GlycoNode>();
            HashMap<GlycoNode,GlycoNode> t_objMapOld;
            for (Iterator<GlycoGraphAlternative> t_iterAltGraph = t_objAlternative.getAlternatives().iterator(); t_iterAltGraph.hasNext();)
            {
                t_objAltGraph = t_iterAltGraph.next();
                t_objMapNew.clear();
                t_objMapOld = t_objAltGraph.getLeadOutNodeToNode();
                for (Iterator<GlycoNode> t_iterPositions = t_objMapOld.keySet().iterator(); t_iterPositions.hasNext();)
                {
                    t_objKeyNode = t_iterPositions.next();
                    t_objKeyNodeNew = t_hashResidues.get(t_objKeyNode);
                    if ( t_objKeyNodeNew == null )
                    {
                        throw new GlycoconjugateException("Error child attache position of alternative graph was not translated.");                 
                    }
                    t_objMapNew.put(t_objKeyNodeNew, t_objMapOld.get(t_objKeyNode));
                }
                t_objAlternative.setLeadOutNodeToNode(t_objMapNew,t_objAltGraph);
            }
        }
        // copie special units
        UnderdeterminedSubTree t_objTreeOriginal;
        UnderdeterminedSubTree t_objTreeCopy;
        for (Iterator<UnderdeterminedSubTree> t_iterTree = this.m_aSpezialTrees.iterator(); t_iterTree.hasNext();) 
        {
            t_objTreeOriginal = t_iterTree.next();
            t_objTreeCopy = t_objTreeOriginal.copy();
            t_objCopy.addUndeterminedSubTree(t_objTreeCopy);
            // copy parent information
            for (Iterator<GlycoNode> t_iterParents = t_objTreeOriginal.getParents().iterator(); t_iterParents.hasNext();) 
            {
                t_objNodeOne = t_iterParents.next();
                t_objNodeTwo = t_hashResidues.get(t_objNodeOne);
                if ( t_objNodeTwo == null )
                {
                    throw new GlycoconjugateException("Impossible to copy repeat unit. Null values in copy.");                    
                }
                t_objTreeCopy.addParent( t_objNodeTwo );
            }
        }
        return t_objCopy;
    }
    
    public void setUndeterminedSubTrees(ArrayList<UnderdeterminedSubTree> a_aSubTree) throws GlycoconjugateException
    {
        if ( a_aSubTree == null )
        {
            throw new GlycoconjugateException("null is not a valide set of special subtrees.");
        }
        this.m_aSpezialTrees.clear();
        for (Iterator<UnderdeterminedSubTree> t_iterTree = a_aSubTree.iterator(); t_iterTree.hasNext();) 
        {
            this.addUndeterminedSubTree(t_iterTree.next());            
        }
    }
    
    public ArrayList<UnderdeterminedSubTree> getUndeterminedSubTrees()
    {
        return this.m_aSpezialTrees;
    }
    
    public boolean addUndeterminedSubTreeParent(UnderdeterminedSubTree a_objTree, GlycoNode a_objParent ) throws GlycoconjugateException
    {
        if ( !this.m_aResidues.contains(a_objParent) )
        {
            throw new GlycoconjugateException("Parent is not part of the sugar.");
        }
        if ( !this.m_aSpezialTrees.contains(a_objTree) )
        {
            throw new GlycoconjugateException("UnderdeterminedSubTree is not part of the sugar.");
        }
        return a_objTree.addParent(a_objParent);
    }
    
    public boolean addUndeterminedSubTree(UnderdeterminedSubTree a_objTree) throws GlycoconjugateException
    {
        if ( a_objTree == null )
        {
            throw new GlycoconjugateException("null is not valide for special subtree.");
        }
        if ( !this.m_aSpezialTrees.contains(a_objTree) )
        {
            return this.m_aSpezialTrees.add(a_objTree);
        }
        return false;
    }

    /**
     * @param i
     */
    public void setRepeatCount(int a_iCount) 
    {
        this.setMinRepeatCount(a_iCount);
        this.setMaxRepeatCount(a_iCount);
    }
}