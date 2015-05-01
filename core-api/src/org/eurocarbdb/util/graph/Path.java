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
*   Last commit: $Rev: 1500 $ by $Author: glycoslave $ on $Date:: 2009-07-14 #$  
*/

package org.eurocarbdb.util.graph;


import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import java.io.Serializable;

import org.apache.log4j.Logger;

import org.eurocarbdb.util.BitSet;
import org.eurocarbdb.util.graph.Edge;
import org.eurocarbdb.util.graph.Vertex;
import org.eurocarbdb.util.graph.Graph;

import static org.eurocarbdb.util.StringUtils.join;


/*  class Path  *//**************************************************
*<p>                 
*   Abstracts the notion of an ordered traversal through {@link Vertex}es
*   and {@link Edge}s in a {@link Graph}.
*</p>                 
*<p>                 
*   Paths have {@link List}-like semantics, in the sense they have 
*   an implicit order, and may contain (traverse) the same vertex
*   or edge multiple times.
*</p>                 
*<p>
*   {@link Path}s may traverse over just vertexes, or vertexes + edges.
*   No restrictions are placed on the continuity of vertexes in a Path,
*   however edges that are added must be continuous with a previously-
*   added vertex.
*</p>
*<p>                 
*   Like edges, this class is <em>parameterised</em> by {@link Edge} 
*   (E) and Vertex (V) type.
*</p>                 
*
*   @see Graph
*   @see Graphs
*   @see Edge
*   @see Vertex
*
*   @version $Rev: 1500 $
*   @author mjh 
*/
public class Path<E,V> implements Serializable //, Iterable<Edge<E,V>>
{   
    /** logging handle */
    static Logger log = Logger.getLogger( Graph.class );
    
    //~~~~~~~~~~~~~~~~~~~~~~~~  FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** The graph being walked */
    private final Graph<E,V> graph;
    
    /** The path through the graph, may be either {@link Edge} 
    *   or {@link Vertex} instances */
    private List<Object> path;
    
    /** True indexes point to Edge instances, otherwise they are Vertexes. */
    private BitSet edges;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Sole constructor. */
    public Path( Graph<E,V> g ) 
    {  
        graph = g;
        edges = new BitSet();
        path  = new ArrayList<Object>();
    }

    
    //~~~~~~~~~~~~~~~~~~~~~~~~  METHODS  ~~~~~~~~~~~~~~~~~~~~~~~~~~//
        
    /*
    public int compareTo( Vertex<E,V> other )
    {
        return ((Integer) this.index).compareTo( other.index );            
    }
    
    public boolean equals( Vertex<E,V> other )
    {
        return this.index == other.index;    
    }
    */
    
    
    /** Add a {@link Vertex} to this {@link Path}. */
    public void add( Vertex<E,V> v )
    {
        if ( ! graph.contains( v ) )
        {
            throw new IllegalArgumentException(
                "vertex not in graph");
            
            // graph.addVertex( v );
        }
        
        edges.set( path.size(), false );
        path.add( v );
    }
    
    
    /** 
    *   Add an {@link Edge} to this {@link Path}.
    *
    *   @throws NullPointerException 
    *       if Edge argument is null.
    *   @throws IllegalArgumentException 
    *       if Edge is not continuous with last added Edge or Vertex.
    */
    public void add( Edge<E,V> e )
    throws NullPointerException, IllegalArgumentException
    {
        Object x = getLast();
        if ( x != null )
        {
            if ( edges.get( path.size() - 1 ) )
            {
                //  x == Edge
                Edge<E,V> edge = (Edge<E,V>) x;
                if ( 
                    edge.getParent() != e.getParent() 
                    && edge.getParent() != e.getChild() 
                    && edge.getChild() != e.getParent() 
                    && edge.getChild() != e.getChild() 
                )
                    throw new IllegalArgumentException(
                        "Edge not continuous with last added Edge");
            }
            else
            {
                //  x == Vertex
                Vertex<E,V> v = (Vertex<E,V>) x;
                if ( v != e.getParent() && v != e.getChild() )
                    throw new IllegalArgumentException(
                        "Edge not continuous with last added Vertex");
            }
        }
        
        edges.set( path.size(), true );
        path.add( e );
    }
    
    
    /** 
    *   Returns the {@link Edge} or {@link Vertex} at the given index. 
    *
    *   @throws IndexOutOfBoundsException if given index is negative 
    *   or greater than/equal to {@link #size()}.
    */
    public Object get( int i )
    throws IndexOutOfBoundsException
    {
        return path.get(i);    
    }
    
    
    /** 
    *   Returns the <em>value</em> of the {@link Edge} or 
    *   {@link Vertex} at the given index. 
    *
    *   @throws IndexOutOfBoundsException if given index is negative 
    *   or greater than/equal to {@link #size()}.
    */
    public Object getValue( int i )
    {
        return edges.get(i) 
            ?   ((Edge<E,V>) path.get(i)).getValue()
            :   ((Vertex<E,V>) path.get(i)).getValue();
    }
    
    
    /** Returns the {@link Graph} through which this {@link Path} traverses. */
    public Graph<E,V> getGraph()
    {
        return graph;
    }
    
    
    /** 
    *   Returns a {@link List} of all the {@link Edge}s
    *   in this {@link Path}, in order. 
    */
    public List<Edge<E,V>> edges()
    {
        List<Edge<E,V>> edge_list 
            = new ArrayList<Edge<E,V>>( countEdges() );
        
        for ( int i : edges )
        {
            edge_list.add( (Edge<E,V>) path.get(i) );   
        }
        
        return edge_list;
    }
    
    
    /** 
    *   Returns a {@link List} of all the {@link Vertex}es
    *   in this {@link Path}, in order. 
    */
    public List<Vertex<E,V>> vertexes()
    {
        List<Vertex<E,V>> vertexes 
            = new ArrayList<Vertex<E,V>>( countVertexes() );
        
        for ( int i = 0; i < path.size(); i++ )
        {
            if ( edges.get(i) )
                continue;
            
            vertexes.add( (Vertex<E,V>) path.get(i) );   
        }
        
        return vertexes;
    }
    
    
    /** 
    *   Returns an unmodifiable {@link List} of the {@link Edge}s
    *   and {@link Vertex}es in this {@link Path}, in order. 
    */
    public List<?> elements()
    {
        return Collections.unmodifiableList( path );       
    }
    
    
    public <T> List<T> elements( List<T> list )
    {
        for ( int i = 0; i < path.size(); i++ )
            list.add( (T) path.get( i ) );               
        
        return list;
    }
    
    
    /** 
    *   Returns a {@link List} of the <em>values</em> each of the 
    *   {@link Edge}s and {@link Vertex}es in this {@link Path}, in order.
    */
    public List<?> values()
    {
        return values( new ArrayList<Object>( path.size() ) );
    }
    
    
    /** 
    *   Similar to {@link #values()}, in that it returns the {@link List} 
    *   of {@link Edge} and {@link Vertex} values in order, but 
    *   using the passed {@link List} as the recipient and return value.
    *   This allows client code to pass in a specific {@link List}
    *   implementation.
    */
    public <T> List<T> values( List<T> list )
    {
        for ( int i = 0; i < path.size(); i++ )
            list.add( (T) getValue( i ) );               
        
        return list;
    }
    
    
    /** 
    *   Returns the <em>i</em>th {@link Edge} in the {@link Path}, when
    *   Edges are traversed in order. 
    *
    *   @throws IndexOutOfBoundsException
    *       if the given count is less than or equal to zero, 
    *       or greater than {@link #countEdges()}.
    */
    public Edge<E,V> getEdge( int count )
    throws IndexOutOfBoundsException
    {
        int c = 0;
        for ( int i : edges )
            if ( ++c == count )
                return (Edge<E,V>) path.get(i);
     
        throw new IndexOutOfBoundsException(
            "Invalid index: " + count + "; valid indexes are: " + edges );
    }
    
    
    
    /** 
    *   Returns the <em>i</em>th {@link Vertex} in the {@link Path}, when
    *   Vertexes are traversed in order. 
    *
    *   @throws IndexOutOfBoundsException
    *       if the given count is less than or equal to zero, 
    *       or greater than {@link #countEdges()}.
    */
    public Vertex<E,V> getVertex( int count )
    {
        int c = 0;
        for ( int i = 0; i < path.size(); i++ )
            if ( edges.get(i) == false )
                if ( ++c == count )
                    return (Vertex<E,V>) path.get(i);
     
        throw new IndexOutOfBoundsException(
            "Invalid index: " 
            + count 
            + "; valid indexes are: " 
            + edges.bitSlice( 0, path.size() ).bitComplementEquals() 
        );
    }
    
    
    /** 
    *   Returns the last {@link Edge} added, or null if there 
    *   are no Edges or {@link #size} == 0. 
    */
    public Edge<E,V> getLastEdge()
    {
        if ( path.size() == 0 )
            return null;
        
        int i = edges.highestSetBit();
        if ( i == -1 )
            return null;
        
        return (Edge<E,V>) path.get( i ); 
    }
    
    
    /** 
    *   Returns the last {@link Vertex} added, or null if there 
    *   are no Vertexes or {@link #size} == 0. 
    */
    public Vertex<E,V> getLastVertex()
    {
        for ( int i = path.size() - 1; i > -1; i-- )
            if ( ! edges.get(i) )
                return (Vertex<E,V>) path.get( i );
        
        return null; 
    }
    
    
    /** Returns the last-added {@link Edge} or {@link Vertex}. */
    public Object getLast()
    {
        if ( path.size() == 0 )
            return null;
        
        return path.get( path.size() - 1 );
    }
    
    
    /** Reverses the order of this entire {@link Path}. */
    public void reverse()
    {
        Collections.reverse( path );   
        
        //  reverse edges bitmask as well
        int size = path.size();
        for ( 
            int left = 0, mid = size >> 1, right = size - 1; 
            left < mid; 
            left++, right-- )
        {
            boolean b = edges.get( left );
            edges.set( left, edges.get( right ) );
            edges.set( right, b );
        }
    }
    
    
    /** Returns the number of elements in this {@link Path}. */
    public int size()
    {
        return path.size();    
    }
    
    
    /** Returns the number of {@link Edge}s in this {@link Path}. */
    public int countEdges()
    {
        return edges.size();
    }
    
    
    /** Returns the number of {@link Vertex}es in this {@link Path}. */
    public int countVertexes()
    {
        return path.size() - edges.size();
    }
    
    
    /** Returns the {@link String} form of each of the elements of
    *   this {@link Path}, concatenated with the string " -> ". */
    public String toString()
    {
        return join( " -> ", path );   
    }
    
    
    /**
    *   
    */
    public class Iterator implements java.util.Iterator<Object>
    {
        final Path path;
        
        final ListIterator iter;
        
        
        public Iterator( Path p )
        {
            path = p;
            iter = p.path.listIterator();
        }
        
        
        public void add( Object x ) {  NOT_IMPLEMENTED();  }
        public void remove()        {  NOT_IMPLEMENTED();  }
        public void set( Object x ) {  NOT_IMPLEMENTED();  }
        
        
        public boolean hasNext()
        {
            return iter.hasNext();    
        }
        
        
        public boolean hasEdgeNext()
        {
            return ( iter.hasNext() && edges.get( iter.nextIndex() ) );     
        }
        
        
        public boolean hasVertexNext()
        {
            return ! hasEdgeNext();
        }
        
        
        public Object next()
        {
            return iter.next();
        }
        
        
        public int nextIndex()
        {
            return iter.nextIndex();   
        }
        
        
        public Edge<E,V> nextEdge()
        {
            return (Edge<E,V>) next();    
        }
        
        
        public Vertex<E,V> nextVertex()
        {
            return (Vertex<E,V>) next();    
        }
     
        
        public boolean hasPrevious()
        {
            return iter.hasPrevious();   
        }
        
        
        public Object previous()
        {
            return iter.hasPrevious();   
        }
        
        
        private final void NOT_IMPLEMENTED()
        {
            throw new UnsupportedOperationException(
                "method not supported");
        }
        
    } // end inner class
    
    
} // end class Path -------------------------------------------
