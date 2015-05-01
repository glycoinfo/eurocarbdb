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
*   Last commit: $Rev: 1423 $ by $Author: glycoslave $ on $Date:: 2009-07-05 #$  
*/

package org.eurocarbdb.util.graph;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.eurocarbdb.util.Visitor;


/**
*   A collection of mathematical and utility methods for Graphs.
*
*   @author mjh
*/
public final class Graphs
{
    /**
    *   Returns an unmodifiable version of the passed Graph.
    */
    public static final <E,V> Graph<E,V> unmodifiableGraph( Graph<E,V> g )
    {
        //  TODO - override all the modification methods.
        return g;   
    }
    
    
    /*  isConnected  *//*********************************************
    *
    *   Returns true if this graph is fully <em>connected</em>, that is, 
    *   there is at least 1 or more paths connecting every vertex in 
    *   the graph. Boundary cases are when the graph is empty, in which
    *   case this method returns <tt>false</tt>, and when the graph
    *   contains a single vertex, in which case this method is defined 
    *   to return <tt>true</tt>.
    */
    public static final <E,V> boolean isConnected( Graph<E,V> g )
    {
        //  eliminate simple edge cases
        switch ( g.countVertices() ) 
        {
            case 0: return false;
            case 1: return true;
        }
            
        //  optimisation: if the number of edges exceeds the number of vertices
        //  by 1 or more, then there must be *at least* (#vertices - #edges)
        //  unconnected subgraphs.
        if ( g.countVertices() - g.countEdges() > 1 )
            return false;

        //  walk graph to identify any unconnected portion        
        ConnectedGraphVisitor<E,V> visitor = new ConnectedGraphVisitor<E,V>();
        visitor.visit( g );
        
        return visitor.isConnected;
    }
    
    
    /*  getConnectedSubgraphs  *//***********************************
    *
    *   Returns the list of <em>fully connected</em> subgraphs in the 
    *   passed graph, that is, graphs within this graph for which
    *   there is at least 1 or more paths connecting every vertex.
    *
    *   @see #isConnected
    */
    public static final <E,V> List<Graph<E,V>> getConnectedSubgraphs( Graph<E,V> g )
    {
        switch ( g.countVertices() ) 
        {
            case 0: return Collections.emptyList();
            case 1: return Collections.singletonList( g );
        }

        ConnectedSubgraphVisitor<E,V> visitor 
            = new ConnectedSubgraphVisitor<E,V>();
            
        visitor.visit( g );
        
        return visitor.subgraphs;
    }
    
    
    /** 
    *   Returns a {@link List} of all {@link Path}s from the 
    *   {@link Graph#getRootVertex root vertex} to all 
    *   {@link Graph#getLeafVertices leaves} in the given {@link Graph}.
    */
    public static final <E,V> List<Path<E,V>> getPaths( Graph<E,V> g )
    {
        if ( g.size() == 0 )
            return Collections.emptyList();
            
        List<Path<E,V>> paths = new ArrayList<Path<E,V>>();
        Set<Vertex<E,V>> leaves = g.getLeafVertices();
        Vertex<E,V> root = g.getRootVertex();
        List<Edge<E,V>> inc_edges;
        Vertex<E,V> parent;
        
        for ( Vertex<E,V> leaf : leaves )
        {
            Path<E,V> path = new Path<E,V>( g );
            Vertex<E,V> p  = leaf;
            
            while ( true )
            {
                path.add( p );    
                if ( p == root )
                    break;
                
                inc_edges = p.getIncomingEdges();
                
                //  assume only trees for now, therefore only 1 parent.
                if ( inc_edges.size() != 1 )
                    throw new UnsupportedOperationException(
                        "this method only works with connected trees, not graphs for now");
                
                parent = inc_edges.get(0).getParent();
                if ( p == parent )
                    break;
                
                path.add( inc_edges.get(0) );
                p = parent;       
            }
            
            path.reverse();
            paths.add( path );
        }       
        
        return paths;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ INNER CLASSES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
       
    /** Utility class for walking a graph to test for connectivity. */
    private static class ConnectedGraphVisitor<E,V> extends GraphVisitor<E,V>
    {
        /** Populated by accept(Graph). */
        public boolean isConnected;
        
        public void accept( Graph<E,V> g )
        {
            isConnected = true;
            
            if ( g.isEmpty() )
            {
                log.debug("graph is empty, returning...");
                return;
            }
                
            log.debug("visiting graph of size=" + g.size() );
            
            visit( g.getRootVertex() );
            
            if ( visitedVertices.size() < g.countVertices() )
                isConnected = false;
        }
        
    }
    
    
    /** Utility class for walking a graph to build fully connected subgraphs. */
    private static class ConnectedSubgraphVisitor<E,V> extends GraphVisitor<E,V>
    {
        /** List of fully connected subgraphs. */
        private List<Graph<E,V>> subgraphs;
               
        
        /** Walk all vertices and edges of the passed graph. */
        public void accept( Graph<E,V> g )
        {
            unvisitedVertices = new HashSet<Vertex<E,V>>( g.getAllVertices() );
            subgraphs = new ArrayList<Graph<E,V>>();
          
            Vertex<E,V> cursor;
            
            while ( unvisitedVertices.size() > 0 )
            {
                cursor = unvisitedVertices.iterator().next();
                //System.err.println("iterating over " + cursor );
                
                visitedEdges = new HashSet<Edge<E,V>>();
                visitedVertices = new HashSet<Vertex<E,V>>();
                
                visit( cursor );            
                
                Graph<E,V> subgraph = new Graph<E,V>( visitedVertices, visitedEdges );
                subgraphs.add( subgraph );
            }            
        }
        
        
        public List<Graph<E,V>> getConnectedSubgraphs()
        {
            assert subgraphs != null;
            return subgraphs; 
        }
    }    
    
}

