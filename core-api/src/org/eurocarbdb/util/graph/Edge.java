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
*   Last commit: $Rev: 1426 $ by $Author: glycoslave $ on $Date:: 2009-07-05 #$  
*/

package org.eurocarbdb.util.graph;

import java.io.Serializable;
import org.eurocarbdb.util.graph.Vertex;


/*  class Edge  *//********************************************** 
*   
*   Wrapper class for objects acting as <em>edges</em> in a {@link Graph}.
*   Like vertices, this class is <em>parameterised</em> by Edge (E) and 
*   {@link Vertex} (V) type. Unlike Vertexes, the <em>value</em> of an
*   Edge is mutable (can be changed).
*
*   @see Graph
*   @see Vertex
*   @version $Rev: 1426 $
*   @author mjh 
*/
public class Edge<E,V> implements Serializable
{
    /** This is just the string that is used to separate vertices 
    *   and edges in the toString method.  */
    public static String TO_STRING_SPACER = " ===> ";

    //~~~~~~~~~~~~~~~~~~~~~~~~  FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** The <em>value</em> of this edge.  */
    E value = null;
    
    /** The parent vertex of this edge.  */
    public final Vertex<E,V> parent;
    
    /** The child vertex of this edge. */
    public final Vertex<E,V> child;

    
    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Creates a new Edge between the given vertices. */
    protected Edge( Vertex<E,V> parent, Vertex<E,V> child ) 
    { 
        this( parent, child, null ); 
    }
    
    
    /** Creates a new Edge between the given vertices, with the given value. */
    protected Edge( Vertex<E,V> parent, Vertex<E,V> child, E value ) 
    { 
        if ( parent == null )
            throw new RuntimeException( "Argument 'parent' cannot be null"
                                      + "-- other arguments were child="
                                      + child
                                      + ", value="
                                      + value 
                                      );
        if ( child == null )
            throw new RuntimeException("Argument 'child' cannot be null"
                                      + "-- other arguments were parent="
                                      + parent
                                      + ", value="
                                      + value 
                                      );

        this.parent = parent; 
        this.child = child; 
        this.value = value; 
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~  METHODS  ~~~~~~~~~~~~~~~~~~~~~~~~~~//

    
    /** Returns the child vertex forming this edge. */
    public Vertex<E,V> getChild() {  return this.child;  }


    /** Returns the parent vertex forming this edge. */
    public Vertex<E,V> getParent() {  return this.parent;  }
    
    
    /** Returns the current <em>value</em> of this edge. */
    public E getValue() {  return this.value;  }
    
    
    /** Returns the opposing vertex to the given vertex.
    *   Throws an exception if the given vertex is not part of this edge. */
    public Vertex<E,V> getVertexOpposite( Vertex<E,V> vertex )
    {
        if ( vertex == parent ) 
            return child;
        else if ( vertex == child ) 
            return parent;
        else throw new IllegalArgumentException( 
            "Given vertex '" 
            + vertex 
            + "' is not part of this edge; edge parent='" 
            + parent 
            + "', child='" 
            + child 
            + "'"
        );
    }
    
    
    /** Returns an edge with inverted parent-child vertex directionality. */
    @Deprecated // todo - move this to Graphs
    public Edge<E,V> invert()
    {
        return new Edge<E,V>( this.child, this.parent, this.value ); 
    }
    
    
    /** Eliminates/clears/frees this edge, breaking all references to this object 
    *   from the vertices it attaches. */
    protected void free()
    {
        parent.removeEdge( this );
        child.removeEdge( this );
    }
    
    
    /** Sets the current <em>value</em> of this edge. */
    public void setValue( E value ) { this.value = value; }
    

    /** Returns the string value of this edge (ie: <code>value.toString()</code>). */
    public String toString() 
    { 
        // String s = "Edge="
        //     + (value != null 
        //     ?   parent + TO_STRING_SPACER + value + TO_STRING_SPACER + child 
        //     :   parent + TO_STRING_SPACER + child )
        // ;
        String s = "" + value;
        
        return ( parent == child ) 
            ?   s + " (self-referential)" 
            :   s
            ;
    }

} // end of class Edge ------------------------------------------
