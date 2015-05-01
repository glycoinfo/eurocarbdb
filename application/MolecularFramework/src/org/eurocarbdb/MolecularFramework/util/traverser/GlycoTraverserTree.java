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
package org.eurocarbdb.MolecularFramework.util.traverser;

import java.util.ArrayList;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
* Traverser travers all subtrees of a sugar. 
* Traverser supports 
* ENTER,RETURN,LEAVE for residues and ENTER,LEAVE for linkages. Traversing of linkages 
* in no order.
* Internal repeat linkage is not touched. Descent into subunits.  
*   
* @author rene
*
*/
public class GlycoTraverserTree extends GlycoTraverser
{
    public GlycoTraverserTree(GlycoVisitor a_objVisitor) throws GlycoVisitorException
    {
        super(a_objVisitor);
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarTraverser#traverse(de.glycosciences.MolecularFrameWork.sugar.Residue)
     */
    @Override
    public void traverse(GlycoNode a_objResidue) throws GlycoVisitorException
    {
        // callback before subtree
        this.m_iState = GlycoTraverser.ENTER;
        a_objResidue.accept(this.m_objVisitor);
        // traverse subtree
        for (Iterator<GlycoEdge> t_iterLinkages = a_objResidue.getChildEdges().iterator(); t_iterLinkages.hasNext();) 
        {
            GlycoEdge t_linkChild = t_iterLinkages.next();
            //t_linkChild.accept(this.m_objVisitor);
            this.traverse(t_linkChild);
            // callback after return
            this.m_iState = GlycoTraverser.RETURN;
            a_objResidue.accept(this.m_objVisitor);
        }
        // callback after subtree
        this.m_iState = GlycoTraverser.LEAVE;
        a_objResidue.accept(this.m_objVisitor);
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarTraverser#traverse(de.glycosciences.MolecularFrameWork.Linkage)
     */
    @Override
    public void traverse(GlycoEdge a_objLinkage) throws GlycoVisitorException
    {
        // callback of the function before subtree 
        this.m_iState = GlycoTraverser.ENTER;
        a_objLinkage.accept(this.m_objVisitor);
        // traverse subtree
        this.traverse(a_objLinkage.getChild());
        // callback of the function after subtree 
        this.m_iState = GlycoTraverser.LEAVE;
        a_objLinkage.accept(this.m_objVisitor);
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarTraverser#traverseGraph(de.glycosciences.MolecularFrameWork.sugar.Sugar)
     */
    @Override
    public void traverseGraph(GlycoGraph a_objSugar) throws GlycoVisitorException
    {
        ArrayList<GlycoNode> t_aRoot;
        try
        {
            t_aRoot = a_objSugar.getRootNodes();
            Iterator<GlycoNode> t_objIterator = t_aRoot.iterator();
            while ( t_objIterator.hasNext() )
            {
                this.traverse(t_objIterator.next());
            }
        } 
        catch (GlycoconjugateException e)
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }
    }
    
}
