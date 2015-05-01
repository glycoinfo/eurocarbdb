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

import java.util.Set;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.eurocarbdb.util.Visitor;

/** 
*   Walks a {@link Graph}, visiting every Vertex and Edge in order.
*   See the {@link accept(Graph)} method for details.
*                       
*   Note that this traversal class is not re-entrant; the default 
*   implementation cannot traverse a Graph that contains nested Graphs.
*
*   @author mjh
*/
public class GraphVisitor<E,V> extends Visitor
{
    /** Inheritable logging handle */
    protected static Logger log = Logger.getLogger( DepthFirstGraphVisitor.class );
    
    /** Set of visited {@link Vertex}es. */
    protected Set<Vertex<E,V>> visitedVertices = new HashSet<Vertex<E,V>>();
    
    /** Set of unvisited {@link Vertex}es. */
    protected Set<Vertex<E,V>> unvisitedVertices = new HashSet<Vertex<E,V>>();
    
    /** Set of visited {@link Edge}s. */
    protected Set<Edge<E,V>> visitedEdges = new HashSet<Edge<E,V>>();

    
    /** 
    *   Resets this visitor to its starting state, including the 
    *   {@link Set} of {@link #visitedVertices visited} vertices. 
    */
    public void clear()
    {
        visitedVertices.clear();
        visitedEdges.clear();
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
        log.debug("visiting graph of size=" + g.size() );
        if ( g.isEmpty() )
        {
            log.debug("graph is empty, returning...");
            return;
        }
        
        this.clear();
        
        Vertex<E,V> cursor = g.getRootVertex();
        unvisitedVertices.addAll( g.getAllVertices() );           
        
        while ( cursor != null && ! unvisitedVertices.isEmpty() )        
        {
            visit( cursor );
            cursor = unvisitedVertices.iterator().next();
        }
        
    }
    
    
    /**
    *   This method is called on visiting a Vertex; the default implementation 
    *   first visits this {@link Vertex}'s {@link Vertex.getValue() value},
    *   then each of this Vertex's {@link Vertex.getAttachedEdges() attached edges}.
    *
    *   @see Vertex.getValue()
    *   @see Vertex.getAttachedEdges()
    */
    public void accept( Vertex<E,V> v )
    {
        //  return if Vertex already visited (ie: add returns false)
        if ( ! visitedVertices.add( v ) )
            return;
        
        unvisitedVertices.remove( v );
        
        log.debug("visiting vertex: " + v );
        
        //  visit Vertex value first
        visit( v.getValue() );
        
        //  then visit attached Edges
        for ( Edge<E,V> e : v.getAttachedEdges() )
            if ( ! visitedEdges.contains( e ) )
                visit( e );
    
        return;
    }
    
    
    /**
    *   This method is called on visiting an Edge; the default implementation 
    *   first visits this {@link Edge}'s {@link Edge.getValue() value},
    *   then the {@link Edge.getParent() parent} and {@link Edge.getChild() child} 
    *   vertexes of this Edge.
    *
    *   @see Edge.getValue()
    *   @see Edge.getParent()
    *   @see Edge.getChild()
    */
    public void accept( Edge<E,V> e )
    {
        //  return if Edge already visited (ie: add returns false)
        if ( ! visitedEdges.add( e ) )
            return;
        
        log.debug("visiting edge: " + e );
        
        //  visit value first
        visit( e.getValue() );
        
        Vertex<E,V> v;
        
        //  visit edge's parent vertex (if not already)
        v = e.getParent(); 
        if ( ! visitedVertices.contains( v ) )
            visit( v );
            
        //  visit edge's child vertex (if not already)
        v = e.getChild(); 
        if ( ! visitedVertices.contains( v ) )
            visit( v );
    }

    
} // end class


