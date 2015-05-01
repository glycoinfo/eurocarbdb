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
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;

import org.eurocarbdb.util.graph.Edge;
                                              

/*  class Vertex  *//************************************************
*                 
*   Wrapper class for <em>vertices</em> in a {@link Graph}.
*   Like edges, this class is <em>parameterised</em> by {@link Edge} 
*   (E) and Vertex (V) type.
*
*   @see Graph
*   @see Edge
*   @version $Rev: 1423 $
*   @author mjh 
*/
public class Vertex<E,V> implements Serializable, Comparable<Vertex<E,V>>
{   
    //~~~~~~~~~~~~~~~~~~~~~~~~  FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Value of this vertex. */
    final V value;
    
    /** */
    int index = -1;
    
    /** List of attached edges, lazily initialised. */
    // private List<Edge<E,V>> attachedEdges = Collections.emptyList();
    private Set<Edge<E,V>> attachedEdges = Collections.emptySet();
    
    
//        /** Whether this vertex is at the periphery of the graph. 
//        *   ATM this is read & modified directly from with the Graph class. */
//        private boolean isLeaf = true;
    
    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Sole constructor. */
    protected Vertex( V value ) {  this.value = value;  }

    
    //~~~~~~~~~~~~~~~~~~~~~~~~  METHODS  ~~~~~~~~~~~~~~~~~~~~~~~~~~//
        
    public int compareTo( Vertex<E,V> other )
    {
        return ((Integer) this.index).compareTo( other.index );            
    }
    
    
    public boolean equals( Vertex<E,V> other )
    {
        return this.index == other.index;    
    }
    
    
    /** Connects this vertex to another vertex with the given Edge. */
    protected void addEdge( Edge<E,V> edge )
    {
        _lazy_init();
        attachedEdges.add( edge );
    }
    
    
    /** Returns the number of edges attached to this Vertex. */
    public int countAttachedEdges() 
    {  
        return attachedEdges.size();
    }
    
    
    /** Returns the number of vertices attached to this Vertex. */
    public int countAttachedVertices() 
    {  
        //return getAttachedVertices().size();  
        int vertices = 0;
        for ( Edge<E,V> e : attachedEdges )
            if ( e.getParent() != e.getChild() )
                vertices++;
                
        return vertices;
    }

    
    /** 
    *   Pre-allocates memory for the specified number of Edges that will
    *   be attached to this vertex. The purpose of this method is to
    *   prevent multiple re-hashes if the number of Edges to add is large,
    *   of if the final number of Edges for this vertex is known in advance.
    */
    public void ensureCapacity( int size )
    {
        if ( size <= attachedEdges.size() )
            return;
        
        Graph.log.debug("setting expected capacity of vertex=" + this + " to " + size );
        
        if ( attachedEdges == Collections.EMPTY_SET )
        {
            //  loadFactor == 1.0 cause we expect the user knows the
            //  exact number of Edges they are expecting to add, and
            //  loadFactor == 1.0 means no rehashing as long as user
            //  adds <= size elements.
            attachedEdges = new HashSet<Edge<E,V>>( size, 1.0f );
            return;
        }
        
        //  if size is more than a trivial number and is decently 
        //  larger than current size, then re-create as a larger Set,
        //  otherwise, let Set dynamically resize itself as per usual.
        if ( size > attachedEdges.size() * 4 && size > 64 )
        {
            Set<Edge<E,V>> resized = new HashSet<Edge<E,V>>( size );
            resized.addAll( attachedEdges );
            attachedEdges = resized;
            return;
        }
    }
    
    
    /** Returns the value of this vertex. */
    public V getValue() {  return this.value;  }

    
    /** Returns the list of the values of the edges associated with this Vertex. */
    public List<E> getAttachedEdgeValues() 
    {  
        List<E> values = new ArrayList<E>( attachedEdges.size() );
        for ( Edge<E,V> v : attachedEdges ) 
            values.add( v.getValue() );
        
        return values;  
    }

    
    /** Returns the (unmodifiable) list of Edges attached to this Vertex. */
    public Set<Edge<E,V>> getAttachedEdges() 
    {  
        return Collections.unmodifiableSet( attachedEdges );  
    }
    
    
    /** 
    *   Returns the Set of Vertices attached to this Vertex. External modification of 
    *   this set does not affect the Graph. 
    */
    public Set<Vertex<E,V>> getAttachedVertices() 
    {  
        Set<Vertex<E,V>> attachedVertices = new HashSet<Vertex<E,V>>( attachedEdges.size() );
        for ( Edge<E,V> e : attachedEdges )
        {
            Vertex<E,V> v = e.getVertexOpposite( this );
            if ( v == this ) continue;
            attachedVertices.add( v );
        }
        
        return attachedVertices;  
    }
    
        
    /** Returns the list of the values of the vertices attached to this Vertex. */
    public Set<V> getAttachedVertexValues() 
    {  
        Set<Vertex<E,V>> attachedVertices = getAttachedVertices();
        Set<V> values = new HashSet<V>( attachedVertices.size() );
        
        for ( Vertex<E,V> v : attachedVertices ) 
            values.add( v.getValue() );
        
        return values;  
    }

    
    /** 
    *   Returns only {@link Edge}s for which this vertex is the child 
    *   vertex of that edge. 
    */
    public List<Edge<E,V>> getIncomingEdges()
    {
        List<Edge<E,V>> incoming = new ArrayList<Edge<E,V>>();
        for ( Edge<E,V> e : attachedEdges )
        {
            if ( e.child == this )
                incoming.add( e );
        }
        
        return incoming;
    }
    

    /** 
    *   Returns only {@link Edge}s for which this vertex is the parent 
    *   vertex of that edge. 
    */
    public List<Edge<E,V>> getOutgoingEdges()
    {
        List<Edge<E,V>> incoming = new ArrayList<Edge<E,V>>();
        for ( Edge<E,V> e : attachedEdges )
        {
            if ( e.parent == this )
                incoming.add( e );
        }
        
        return incoming;
    }
    
    
    /** Returns whether this vertex has attached vertices. */
    public boolean hasAttachedVertices() 
    {
        for ( Edge<E,V> e : attachedEdges )
            if ( e.getParent() != this || e.getChild() != this )
                return true;
            
        return false;
    }

    
    /** 
    *   This method is overriden to return the hashcode of the *value* of
    *   this vertex, not the vertex itself. What this means is that vertex
    *   values must be *unique* per graph. 
    *    
    *   @see java.lang.Object#hashCode() 
    */
    public int hashCode() {  return value.hashCode();  }
    
    
    /** Removes the given edge from this vertex. */
    protected boolean removeEdge( Edge edge ) 
    {  
        return attachedEdges.remove( edge );
    }
    

    /** Returns this Vertex's value as a string. */
    public String toString() {  return "Vertex=" + value.toString();  }

    
    /** Lazily initialises internal lists for attahed vertices & edges. */
    private final void _lazy_init() 
    {
        if ( attachedEdges == Collections.EMPTY_SET )
            attachedEdges = new HashSet<Edge<E,V>>( Graph.Default_Number_Of_Children );
    }
    
} // end class Vertex -------------------------------------------
