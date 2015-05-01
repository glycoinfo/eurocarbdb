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
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
* @author rene
*
*/
public class GlycoTraverserValdidation extends GlycoTraverser
{

    /**
     * @param a_objVisitor
     * @throws GlycoVisitorException 
     */
    public GlycoTraverserValdidation(GlycoVisitor a_objVisitor) throws GlycoVisitorException
    {
        super(a_objVisitor);
    }

    public void traverse(GlycoNode a_objResidue) throws GlycoVisitorException
    {
        a_objResidue.accept( this.m_objVisitor );
        // get all Linkages
        ArrayList<GlycoEdge> t_aLinkages = a_objResidue.getChildEdges();
        for (Iterator<GlycoEdge> t_iterEdges = t_aLinkages.iterator(); t_iterEdges.hasNext();) 
        {
            this.traverse(t_iterEdges.next());          
        }
        if ( a_objResidue.getParentEdge() != null )
        {
            this.traverse(a_objResidue.getParentEdge());
        }
    }

    public void traverse(GlycoEdge a_objLinkage) throws GlycoVisitorException 
    {
        a_objLinkage.accept( this.m_objVisitor );
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarTraverser#traverse(de.glycosciences.MolecularFrameWork.sugar.Sugar)
     */
    public void traverseGraph(GlycoGraph a_objSugar) throws GlycoVisitorException 
    {
        Iterator<GlycoNode> t_objIterator = a_objSugar.getNodeIterator();
        while ( t_objIterator.hasNext() )
        {
            this.traverse(t_objIterator.next());
        }
    }

}
