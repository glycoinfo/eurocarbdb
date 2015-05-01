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
package org.eurocarbdb.MolecularFramework.io.Linucs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
* @author Logan
*
*/
public class GlycoTraverserLinucs extends GlycoTraverser 
{
    private GlycoNode m_objRepeatEnd = null;
    private GlycoEdge m_objRepeatLinkage = null;
    private GlycoVisitorExportRepeat m_objRepeatVisitor = null;
    /**
     * @param a_objVisitor
     * @throws GlycoVisitorException
     */
    public GlycoTraverserLinucs(GlycoVisitor a_objVisitor) throws GlycoVisitorException 
    {
        super(a_objVisitor);
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.traverser.GlycoTraverser#traverse(org.glycomedb.MolecularFrameWork.sugar.GlycoNode)
     */
    @Override
    public void traverse(GlycoNode a_objNode) throws GlycoVisitorException 
    {
        this.m_iState = GlycoTraverser.ENTER;
        a_objNode.accept(this.m_objVisitor);
        // sort childpositions
        for (Iterator<GlycoEdge> t_iterEdges = a_objNode.getChildEdges().iterator(); t_iterEdges.hasNext();) 
        {
            GlycoEdge t_objEdge = t_iterEdges.next();
            if ( t_objEdge.getGlycosidicLinkages().size() != 1 )
            {
                 throw new GlycoVisitorException("Linucs does not support multiple connected residues.");
            }
            if ( t_objEdge.getGlycosidicLinkages().get(0).getChildLinkages().size() > 1 )
            {
                Collections.sort( t_objEdge.getGlycosidicLinkages().get(0).getChildLinkages() );
            }
            if ( t_objEdge.getGlycosidicLinkages().get(0).getParentLinkages().size() > 1 )
            {
                Collections.sort( t_objEdge.getGlycosidicLinkages().get(0).getParentLinkages() );
            }            
        }
        // sort childnodes
        LinucsComparatorEdges t_objComperator = new LinucsComparatorEdges();
        if ( a_objNode == this.m_objRepeatEnd )
        {
            ArrayList<GlycoEdge> t_objEdgesOriginal = a_objNode.getChildEdges();
            ArrayList<GlycoEdge> t_objEdges = new ArrayList<GlycoEdge>();
            for (Iterator<GlycoEdge> t_iterEdges = t_objEdgesOriginal.iterator(); t_iterEdges.hasNext();) 
            {
                t_objEdges.add(t_iterEdges.next());                
            }
            t_objEdges.add(this.m_objRepeatLinkage);
            Collections.sort( t_objEdges , t_objComperator );    
            // traverse childnodes
            GlycoEdge t_objEdge;
            for (int t_iPosition = 0; t_iPosition < t_objEdges.size(); t_iPosition++) 
            {
                t_objEdge = t_objEdges.get(t_iPosition);
                if ( t_objEdge != this.m_objRepeatLinkage )
                {
                    this.traverse(t_objEdge);
                }
                else
                {
                    this.m_objRepeatVisitor.switchStrings();
                }
            }
        }
        else
        {
            ArrayList<GlycoEdge> t_objEdges = a_objNode.getChildEdges(); 
            Collections.sort( t_objEdges , t_objComperator );    
            // traverse childnodes
            for (int t_iPosition = 0; t_iPosition < t_objEdges.size(); t_iPosition++) 
            {
                this.traverse(t_objEdges.get(t_iPosition));            
            }
        }
        this.m_iState = GlycoTraverser.LEAVE;
        a_objNode.accept(this.m_objVisitor);
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser#traverse(org.eurocarbdb.MolecularFramework.sugar.GlycoEdge)
     */
    @Override
    public void traverse(GlycoEdge a_objEdge) throws GlycoVisitorException 
    {
        a_objEdge.accept(this.m_objVisitor);
        this.traverse( a_objEdge.getChild() );
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser#traverseGraph(org.eurocarbdb.MolecularFramework.sugar.GlycoGraph)
     */
    @Override
    public void traverseGraph(GlycoGraph a_objSugar) throws GlycoVisitorException 
    {
        try 
        {
            ArrayList<GlycoNode> t_aRoots = a_objSugar.getRootNodes();
            if ( t_aRoots.size() != 1 )
            {
                throw new GlycoVisitorException("LINUCS can only store connected sugars.");
            }
            this.traverse(t_aRoots.get(0));
        } 
        catch (GlycoconjugateException e) 
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }
    }

    /**
     * @param repeat
     * @param out
     */
    public void traverseGraph(SugarUnitRepeat a_objRepeat, GlycoEdge a_objLinkage, GlycoVisitorExportRepeat a_objVisitor) throws GlycoVisitorException
    {
        this.m_objRepeatEnd = a_objRepeat.getRepeatLinkage().getParent();
        this.m_objRepeatLinkage = a_objLinkage;
        this.m_objRepeatVisitor = a_objVisitor;
        this.traverseGraph(a_objRepeat);
    }
}
