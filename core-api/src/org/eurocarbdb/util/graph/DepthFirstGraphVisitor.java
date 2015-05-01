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

package org.eurocarbdb.util.graph;

import java.util.Set;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.eurocarbdb.util.Visitor;

/** 
*   Utility class to traverse the elements of a {@link Graph} in 
*   depth-first order, following the {@link Visitor} pattern. The normal
*   usage of this class is through subclassing, and adding the desired
*   data and behaviour to the subclass.
*
*<h2>Example</h2> 
*
*   Given the graph:
*<pre>
*        F
*         \
*          E -- C
*         /      \
*        G        B -- A
*                /
*          H -- D
*          
*</pre>
*   The depth-first traversal of this graph (with lower sorting 
*   alternatives traversed first) starting from A would traverse 
*   vertices in the following order: A, B, C, E, F, G, D, H. For example,
*   given the code (minus generic arguments for clarity):                   
*<pre>
*       DepthFirstGraphVisitor visitor = new DepthFirstGraphVisitor() 
*       {
*           int depth = 0;
*
*           public void accept( Vertex v )
*           {
*               depth++;
*               log.debug( "> approaching vertex " + v.getValue() );
*
*               super.accept( v );
*
*               log.debug( "< leaving vertex " + v.getValue() );
*               depth--;
*           }
*       };
*
*</pre>
*   would print (with added indenting for clarity):
*<pre>                                                       
*       > approaching vertex A
*           > approaching vertex B
*               > approaching vertex C
*                   > approaching vertex E
*                       > approaching vertex F
*                       < leaving vertex F
*                       > approaching vertex G
*                       < leaving vertex G
*                   < leaving vertex E
*               < leaving vertex C
*               > approaching vertex D
*                   > approaching vertex H
*                   < leaving vertex H
*               < leaving vertex D
*           < leaving vertex B
*       < leaving vertex A
*
*</pre>
*                       
*   Note that this traversal class is not re-entrant; the default 
*   implementation cannot traverse a Graph that contains nested Graphs.
*
*   @author mjh
*/
public class DepthFirstGraphVisitor<E,V> extends Visitor
{
    /** logging handle */
    protected static Logger log = Logger.getLogger( DepthFirstGraphVisitor.class );
    
    /** Set of visited vertices, initially empty. */
    protected Set<Vertex<E,V>> visitedVertices = new HashSet<Vertex<E,V>>();

    /** Set of unvisited vertices, initially null. */
    protected Set<Vertex<E,V>> unvisitedVertices = null; //new HashSet<Vertex<E,V>>();

    
    /** 
    *   Resets this visitor to its starting state, including the 
    *   {@link Set} of {@link #visitedVertices visited} vertices. 
    */
    public void clear()
    {
        visitedVertices = new HashSet<Vertex<E,V>>();
    }

    
    /**
    *   Called when visiting a {@link Graph}. 
    *
    *   The default implementation visits all {@link Vertex}es and {@link Edge}s 
    *   in depth-first order, starting from the Graph's root vertex, as given
    *   by the method {@link Graph#getRootVertex()}.
    *
    */
    public void accept( Graph<E,V> g ) 
    {
        if ( g.isEmpty() )
        {
            log.debug("graph is empty, returning...");
            return;
        }
            
        log.debug("visiting graph of size=" + g.size() );
        
        Vertex<E,V> v = g.getRootVertex();
        unvisitedVertices = new HashSet<Vertex<E,V>>( g.getAllVertices() );
        
        visit( v );
            
        if ( visitedVertices.size() < g.countVertices() )
        {
            log.info("note: imperfect depth-first traversal - graph is not fully connected");
            
            //  graph isn't fully connected, visit remaing vertices in
            //  semi-arbitrary order
            while ( ! unvisitedVertices.isEmpty() )
            {
                v = unvisitedVertices.iterator().next();
                visit( v );   
            }
        }
    }
    
    
    /**
    *   Called when visiting a {@link Vertex}. 
    *
    *   The default implementation visits this Vertex's value, then all 
    *   outgoing {@link Edge}s of this Vertex, as given by the method 
    *   {@link Vertex#getOutgoingEdges()}.
    *
    *   @see Vertex.getValue()
    *   @see Vertex.getOutgoingEdges()
    */
    public void accept( Vertex<E,V> v )
    {
        log.debug("visiting vertex: " + v );
        
        if ( ! visitedVertices.add( v ) )
            return;
        
        if ( unvisitedVertices != null )
            unvisitedVertices.remove( v );
        
        visit( v.getValue() );
        
        for ( Edge<E,V> e : v.getOutgoingEdges() )
            visit( e );
    
        return;
    }
    
    
    /**
    *   Called when visiting an {@link Edge}. 
    *
    *   The default implementation visits the child {@link Vertex}
    *   of this Edge, as given by the method {@link Edge#getChild()}.
    */
    public void accept( Edge<E,V> e )
    {
        log.debug("visiting edge: " + e );
        
        visit( e.getValue() );
        
        Vertex<E,V> child_vertex = e.getChild(); 
        if ( visitedVertices.contains( child_vertex ) ) 
            return;
            
        visit( child_vertex );
    }

    
} // end class


