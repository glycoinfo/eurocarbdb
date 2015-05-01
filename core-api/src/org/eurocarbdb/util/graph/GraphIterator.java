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
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;

import org.eurocarbdb.util.graph.Graph;
import org.eurocarbdb.util.graph.Edge;
import org.eurocarbdb.util.graph.Vertex;

/*  abstract class Iterator  *//*********************************
*
*   Abstract base class for iterators over graphs. 
*   
*   This class provides the regular Iterator semantics, but with 
*   added functionality.
*
*     @author matt
*
*/
public abstract class GraphIterator<E,V> implements java.util.Iterator, Iterable
{
    //~~~~~~~~~~~~~~~~~~~~~~~~  FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** The graph over which we're iterating. */
    protected Graph<E,V> graph;
    
    /** List of unvisited vertices. This list may be used as either a 
    *   stack or queue, depending on the type of iteration. In a 
    *   depth-first algorithm, this list would be a stack, in a 
    *   breadth-first algorithm, this list would be a queue.  */
    protected LinkedList<Vertex<E,V>> unvisited;

    /** Stack of visited vertices. */
    protected Set<Vertex<E,V>> visited;

    /** */
    protected Vertex<E,V> startVertex;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /*  Constructor  *//*****************************************
    *   
    *   Constructs an iterator over the given graph, starting at the given
    *   vertex.
    */
    public GraphIterator( Graph<E,V> g, Vertex<E,V> start_vertex )
    {
        assert g != null;
        assert start_vertex != null;
        
        this.graph = g;
        
        if ( ! graph.contains( start_vertex ) )
            throw new RuntimeException( "Start vertex '" 
                                        + start_vertex
                                        + "' does not exist in given graph" 
                                        );

        _init( start_vertex );
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~  METHODS  ~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /*  getValues  *//*******************************************
    *
    *   Returns a List of the remaining vertex values in the present iteration.
    *   If none of the <tt>next*</tt> methods have been called then this
    *   method will return all vertex values in the graph, in iteration order.
    */
    public List<V> getValues()
    {
        List<V> list = new ArrayList<V>( graph.countVertices() );
        while ( hasNext() ) list.add( next() );
        return list;
    }
    
    
    /*    getVertices  *//*****************************************
    *
    *     Returns a List of the remaining vertices in the present iteration.
    *   If none of the <tt>next*</tt> methods have been called then this
    *   method will return all vertices in the graph, in iteration order.
    */
    public List<Vertex<E,V>> getVertices()
    {
        List<Vertex<E,V>> list = new ArrayList<Vertex<E,V>>( graph.countVertices() );
        while ( this.hasNext() ) list.add( this.nextVertex() );
        return list;
    }
    
    
    /*    getCurrentVertex  *//****************************************
    *
    *   Returns the current vertex in the present iteration. Does not advance
    *   the iteration cursor.
    */
    public Vertex<E,V> getCurrentVertex() {  return unvisited.peek();  }

    
    /** Returns the graph over which we're iterating. */
    public Graph<E,V> getGraph() {  return graph;  }

    
    /*  @see java.util.Iterator#hasNext()  */
    public boolean hasNext()
    {
        return ! ( unvisited.isEmpty() 
            || visited.size() >= graph.countVertices() );
    }

    
    /*  _init  *//***************************************************
    *  
    *   Initialises internal data prior to iteration. Can also be used to 
    *   re_initialise iteration to the starting state.  
    */
    private final void _init( Vertex<E,V> start_vertex )
    {
        this.visited     = new HashSet<Vertex<E,V>>( graph.countVertices() );
        this.unvisited   = new LinkedList<Vertex<E,V>>();
        this.startVertex = start_vertex;
        pushUnvisited( start_vertex );
    }

    /** Returns this iterator, so that these iterators can be used 
    *   in foreach statements.  
    */
    public Iterator iterator() {  return this;  }

    
    /*  @see java.util.Iterator#next()  */
    public V next() {  return nextVertex().getValue();  }

    
    /*  nextVertex  *//**********************************************
    *  
    *   Similar to <tt>next()</tt>, except that this method returns the
    *   current vertex, instead of its value. Like <tt>next()</tt>, this 
    *   method advances the iteration cursor.  
    */
    public Vertex<E,V> nextVertex()
    {
        assert unvisited.size() > 0;

        Vertex<E,V> current = unvisited.remove();

        if ( visited.contains( current ) )
            return nextVertex();

        visited.add( current );

        if ( ! current.hasAttachedVertices() )
            return current;

        for ( Vertex<E,V> child : current.getAttachedVertices() )
            if ( ! visited.contains( child ) )
                pushUnvisited( child );

        return current;
    }
    
    /*  pushUnvisited  *//*******************************************
    *  
    *   Add a vertex to the 'unvisited' list. This method needs to be abstract
    *   so that subclasses implementing depth-first or breadth-first algorithms
    *   can add the passed vertex to the appropriate end of their stack or queue
    *   respectively. This method would not be necessary if Java provided 
    *   Stack and Queue classes with common addition/removal semantics. 
    *   
    *   @see #unvisited
    */
    protected abstract void pushUnvisited( Vertex<E,V> v );

    /*  @see java.util.Iterator#remove()  */
    public void remove() 
    {  
        Vertex<E,V> v = unvisited.remove();
        getGraph().remove( v );  
    }

    /** Resets iteration to when this iterator was first created. */
    public void reset() {  _init( this.startVertex );  }


    //~~~~~~~~~~~~~~~~~~~~~  INNER CLASSES  ~~~~~~~~~~~~~~~~~~~~~~~//
    

    /*  class GraphIterator.BreadthFirst  *//************************
    *<p>
    *   Implements iteration through Graph vertices in breadth-first order. 
    *   This means that all of the vertices immediately attached to the current 
    *   vertex will be returned before any of their children are returned. 
    *</p>
    *<p>
    *   eg, given:
    *<pre>
    *   
    *        F
    *         \
    *          E -- C
    *         /      \
    *        G        B -- A
    *                /
    *          H -- D
    *          
    *</pre>
    *<p>
    *   If the starting vertex was A, the returned list would be: A, B, C, D, E, H, F, G.
    *</p>
    *   Created 19-Oct-2005.
    *   @author matt
    */
    public static class BreadthFirst<E,V> extends GraphIterator<E,V>
    {
        /*  Constructor  */
        public BreadthFirst( Graph<E,V> g, Vertex<E,V> start_vertex ) 
        {  
            super( g, start_vertex );  
        }
    
        /*  @see GraphIterator#pushUnvisited(Vertex)  */
        protected void pushUnvisited( Vertex<E,V> v ) 
        {  
            unvisited.addLast( v );  
        }     
    }

    
    /*  class GraphIterator.DepthFirst  *//**************************
    *<p>
    *   Implements iteration through Graph vertices in depth-first order. 
    *   This means that all of the residues attached to a certain branch 
    *   will be returned before objects comprising other (sibling) branches.
    *</p>
    *<p>
    *   eg, given:
    *<pre>
    *   
    *        F
    *         \
    *          E -- C
    *         /      \
    *        G        B -- A
    *                /
    *          H -- D
    *          
    *</pre>
    *<p>
    *   If the starting vertex was A, the returned list would be: A, B, C, E, F, G, D, H.
    *</p>
    *   Created 19-Oct-2005.
    *   @author matt
    */
    public static class DepthFirst<E,V> extends GraphIterator<E,V>
    {
        /*    Constructor  */
        public DepthFirst( Graph<E,V> g, Vertex<E,V> start_vertex ) 
        {  
            super( g, start_vertex );  
        }
        
        /*     @see GraphIterator#pushUnvisited(Vertex)  */
        protected void pushUnvisited( Vertex<E,V> v ) 
        {  
            unvisited.addFirst( v );  
        }     
    }



} // end of class Iterator 

