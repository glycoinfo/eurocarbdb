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

package org.eurocarbdb.util.graph;

import java.util.*;
import org.eurocarbdb.util.Visitor;

/** [unfinished!!!] Utility class for walking a graph, breadth-first */
public class BreadthFirstGraphVisitor<E,V> extends Visitor
{
    /** Set of vertices that have not been walked. */
    protected Set<Vertex<E,V>> unvisitedVertices;
    
    /** Set of edges that have been traversed. */
    protected Set<Edge<E,V>> visitedEdges;
    
    /** Set of vertices that have been traversed. */
    protected Set<Vertex<E,V>> visitedVertices;
    
    
    /** Walk all vertices and edges of the passed graph. */
    public void accept( Graph<E,V> g )
    {
        unvisitedVertices = new HashSet<Vertex<E,V>>( g.getAllVertices() );
      
        while ( unvisitedVertices.size() > 0 )
        {
            Vertex<E,V> cursor = unvisitedVertices.iterator().next();
            visit( cursor );            
        }
    }
    
    
    public void accept( Vertex<E,V> v )
    {
        unvisitedVertices.remove( v );
        visitedVertices.add( v );
        
        visit( v.getValue() );
        
        for ( Edge<E,V> e : v.getAttachedEdges() )
            visit( e );
    }
    
    
    public void accept( Edge<E,V> e )
    {
        if ( ! visitedEdges.add( e ) )
            return;
        
        visit( e.getValue() );
        
        if ( ! visitedVertices.contains( e.getParent() ) )
            visit( e.getParent() );
            
        if ( ! visitedVertices.contains( e.getParent() ) )
            visit( e.getChild() );
    }
    
}    

