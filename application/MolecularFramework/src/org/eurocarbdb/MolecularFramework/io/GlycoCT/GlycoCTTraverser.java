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
package org.eurocarbdb.MolecularFramework.io.GlycoCT;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

public class GlycoCTTraverser extends GlycoTraverser
{    
    public GlycoCTTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException 
    {
        super(a_objVisitor);
    }

    public void traverse(GlycoEdge a_objEdge) throws GlycoVisitorException 
    {
        // callback of the function before subtree 
        this.m_iState = GlycoTraverser.ENTER;
        a_objEdge.accept(this.m_objVisitor);
        // traverse subtree
        this.traverse(a_objEdge.getChild());
    }

    
    /**
     * @see org.glycomedb.MolecularFrameWork.util.traverser.SugarTraverser#traverse(org.eurocarbdb.MolecularFramework.sugar.GlycoNode)
     */
    @Override
    public void traverse(GlycoNode a_objNode) throws GlycoVisitorException 
    {
        // callback before subtree
        this.m_iState = GlycoTraverser.ENTER;
        a_objNode.accept(this.m_objVisitor);
        // traverse subtree in GlycoCT order
        ArrayList <GlycoEdge> t_aEdge = a_objNode.getChildEdges();
        GlycoCTGlycoEdgeComparator t_oNodeComparator = new GlycoCTGlycoEdgeComparator();
        Collections.sort(t_aEdge,t_oNodeComparator); 
        for (GlycoEdge t_oEdge : t_aEdge) 
        {            
            this.traverse(t_oEdge);
        }
    }
    
    /**
     * @see org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser#traverseGraph(org.eurocarbdb.MolecularFramework.sugar.GlycoGraph)
     */
    @Override
    public void traverseGraph(GlycoGraph a_objSugar) throws GlycoVisitorException 
    {
        ArrayList<GlycoNode> t_aRoot;
        try
        {
            // get root nodes of forest of graphs
            t_aRoot = a_objSugar.getRootNodes();              
            //priorize according to GlycoCT all isolated subgraphs and process consecutivly
            GlycoCTGlycoNodeComparator t_oNodeComparator = new GlycoCTGlycoNodeComparator();
            Collections.sort(t_aRoot,t_oNodeComparator);           
            
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

