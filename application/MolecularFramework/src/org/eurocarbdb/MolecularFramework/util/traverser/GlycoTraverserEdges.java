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

import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
* Traverse linkages only and only one time per linkage. 
* But can start with repeat or statistical.
* Support only STATE ENTER.
* No order.
*  
*/
public class GlycoTraverserEdges extends GlycoTraverser 
{
    public GlycoTraverserEdges(GlycoVisitor a_objVisitor) throws GlycoVisitorException
    {
        super(a_objVisitor);
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoTraverser#traverse(de.glycosciences.MolecularFrameWork.sugar.GlycoNode)
     */
    @Override
    public void traverse(GlycoNode a_objResidue) throws GlycoVisitorException 
    {
        // traverse subtree
        Iterator<GlycoEdge> t_iterLinkages = a_objResidue.getChildEdges().iterator();
        while (t_iterLinkages.hasNext())
        {
            GlycoEdge t_linkChild = t_iterLinkages.next();
            //t_linkChild.accept(this.m_objVisitor);
            this.traverse(t_linkChild);
        }
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoTraverser#traverse(de.glycosciences.MolecularFrameWork.sugar.Linkage)
     */
    @Override
    public void traverse(GlycoEdge a_objLinkage) throws GlycoVisitorException 
    {
        // callback of the function before subtree 
        this.m_iState = GlycoTraverser.ENTER;
        a_objLinkage.accept(this.m_objVisitor);
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoTraverser#traverseGraph(de.glycosciences.MolecularFrameWork.sugar.Sugar)
     */
    @Override
    public void traverseGraph(GlycoGraph a_objSugar) throws GlycoVisitorException 
    {
        Iterator<GlycoNode> t_iterResidues = a_objSugar.getNodeIterator();
        while ( t_iterResidues.hasNext() )
        {
            this.traverse(t_iterResidues.next());
        }
    }
}
