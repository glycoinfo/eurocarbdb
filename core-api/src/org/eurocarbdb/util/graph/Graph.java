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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/

package org.eurocarbdb.util.graph;

//  stdlib imports
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections; 

import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

import static org.eurocarbdb.util.StringUtils.join;


/*  class Graph  *//**************************************************
*<p> 
*   This class implements mathematical graphs. Conventional graph theory
*   defines a graph as a set of <em>vertices</em> (or nodes), connected 
*   to each other by <em>edges</em>. For more specific information about 
*   vertices and edges, see the {@link Vertex} and {@link Edge} class 
*   documentation.  
*</p>
*<p>
*   Typical usage involves creating a Graph object, to which Vertex
*   and Edge objects are then added through the various <tt>add</tt> 
*   methods. Vertex and Edge objects are created through the 
*   {@link #createVertex createVertex} and {@link #createEdge createEdge} 
*   methods.
*</p>
*<p>
*   Alternatively, vertices and edges may be rapidly added to the graph
*   through the {@link Graph#addPath addPath} method, which creates 
*   Vertex and Edge objects implicitly.
*</p>
*<p>
*   This is a <em>generic</em> class - it can be parameterised for both
*   Edge and Vertex types; accordingly, the 'E' and 'V' in the class 
*   declaration refer to the parameterised Edge and Vertex types 
*   respectively. While it is possible to access edge and vertex
*   objects directly, this is rarely necessary, since most methods
*   also provide direct access to vertex and edge values.
*</p>
*<p>
*   The associated {@link Graphs} class provides several additional
*   methods for working with {@link Graph}s.
*</p>
*
*<h3>Example usage</h3>
*<pre>
        //  create an empty graph</em>
        Graph&lt;Float,String&gt; g = new Graph&lt;Float,String&gt;();
        
        //  add a vertex
        g.addVertex("vertex1");
        System.out.println("last vertex added was " + g.lastVertex() );
        
        //  add an edge and vertex attached to vertex1
        g.addPath( g.lastVertex(), 0.75f, "vertex2" );
        System.out.println("last vertex added was " + g.lastVertex() );

        //  add a couple more vertices and edges
        g.addPath( g.lastVertex(), 0.25f, "vertex3" );        
        g.addPath( g.getVertex("vertex2"), 0.1f, "vertex4" );
        
        //  list all vertices in the graph
        Set&lt;String> vlist = g.getAllVertexValues();
        System.out.println("vertices are: " + vlist );
        
        //  list all edges
        List&lt;Float&gt; elist = g.getAllEdgeValues();
        System.out.println("edges are: " + elist );

        //  list all vertices that have 0 or 1 vertex attached to them
        System.out.println("leaves are: " + g.getLeaves() );
        
        //  elaborate graph a little more...
        g.addPath( g.getVertex("vertex2"), 0.01f, "vertex5" );
        g.addPath( g.lastVertex(), 0.02f, "vertex6" );
        g.addPath( g.getVertex("vertex5"), 0.98f, "vertex7" );
                
        System.out.println("leaves are: " + g.getLeaves() );
        
        //  this edge introduces a cycle into the graph
        g.addEdge( g.getVertex("vertex1"), g.getVertex("vertex7"), 0.001f );
        
        System.out.println("leaves are: " + g.getLeaves() );
        
        //  traverse graph vertices in depth-first order
        Graph&lt;Float,String&gt;.GraphIterator.DepthFirst dfs = g.traverseDepthFirst();
        while ( dfs.hasNext() )
        {
            String s = dfs.next();
            System.out.println( s );
        }

*</pre>
*
*   @param E The edge type.
*   @param V The vertex type.
*   @see Graphs
*   @see Vertex
*   @see Edge
*   @author mjh
*/
public class Graph<E,V> 
implements Iterable<Vertex<E,V>>, Set<Vertex<E,V>>, Serializable, Cloneable
{
    //~~~~~~~~~~~~~~~~~~~~~  STATIC FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** logging handle */
    static Logger log = Logger.getLogger( Graph.class );
    
    private static final long serialVersionUID = -1;
    
    /** The default number of child vertices to accomodate per vertex. */
    static final int Default_Number_Of_Children = 4;
    
    /** The default number of vertices to accomodate in a tree. */
    static final int Default_Tree_Size = 8;
    
        
    //~~~~~~~~~~~~~~~~~~~~~~~~~  FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~~~~//

    ///**
    //*   
    //*/
    //public boolean directional = true;

    /** Private set of Vertices. By definition, the first vertex in 
    *   the list is the root vertex.  */
    protected Set<Vertex<E,V>> vertices;
    
    /** Private list of Edges. */
    protected Set<Edge<E,V>> edges;
    
    /** Map of value to vertex, to trade some memory for enhanced lookup speed. */
    private Map<V,Vertex<E,V>> values;

    /** A semi-arbitrary root vertex. This will be the vertex that is
    *   used to start any graph traversal in lieu of a user-provided 
    *   vertex. This also serves as the root node in the case that
    *   the current graph is a tree.  */
    private Vertex<E,V> rootVertex = null;

    /** The most recently-added vertex. */
    private Vertex<E,V> lastVertex;
        
    
    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /*  Constructor  *//*********************************************
    *
    *   Constructs an empty Graph with an initial vertex capacity 
    *   of size {@link #Default_Tree_Size}.
    */
    public Graph() 
    { 
        _init( Default_Tree_Size );
    }
    
    
    /*    Constructor  *//*******************************************
    * 
    *   Constructs an empty Graph with an initial vertex capacity 
    *   of the given initial size.
    */
    public Graph( int initial_size )
    {
        _init( initial_size );
    }


    /*    Constructor  *//*******************************************
    * 
    *   Constructs an empty Graph with an initial vertex capacity 
    *   of the given initial vertex size, and an initial edges
    *   size per vertex of the given edges size.
    */
    public Graph( int initial_edges_size, int initial_vertex_size )
    {
        //  TODO!
        _init( initial_vertex_size );
    }
    
    
    /*  Constructor  *//*********************************************
    *   
    *   Constructs a Graph with the given vertices. Package use only.
    */
    Graph( Set<Vertex<E,V>> vertices, Set<Edge<E,V>> edges )
    {
        assert vertices != null;
        assert edges != null;
        
        this.vertices = vertices;
        this.edges = edges;
        this.values = new HashMap<V,Vertex<E,V>>( vertices.size() );
        
        for ( Vertex<E,V> v : vertices )
        {
            values.put( v.getValue(), v );   
        }
        assert values.size() == vertices.size();
    }
    
    
    
    public static <E,V> Graph<E,V> create() 
    {  
        return new Graph<E,V>();  
    }
    
    
    public static <E,V> Graph<E,V> create( int vertices ) 
    {  
        return new Graph<E,V>( vertices );  
    }
    
    
    public static <E,V> Graph<E,V> create( int vertices, int edges ) 
    {  
        return new Graph<E,V>( vertices, edges );  
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~  METHODS  ~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /*  add  *//*****************************************************
    *
    *   Semantically equivalent to addVertex(Vertex).
    */
    public boolean add( Vertex<E,V> v ) {  return addVertex( v );  }
    
    
    /*  addAll  *//************************************************** 
    *
    *   Semantically equivalent to addVertex(Vertex) called for every
    *   vertex in the passed collection. 
    */
    public boolean addAll( Collection<? extends Vertex<E,V>> vertices )
    {
        boolean true_if_added = false;
        for ( Vertex<E,V> v : vertices )
            true_if_added &= addVertex( v );
    
        return true_if_added;
    }
    
    
    /*  addAllValues  *//********************************************
    *
    *   Semantically equivalent to {@link #addVertex(V)} called for every
    *   vertex in the passed collection. 
    */
    public boolean addAllValues( Collection<V> vertex_values )
    {
        boolean true_if_added = false;
        for ( V v : vertex_values )
            true_if_added &= addVertex( v );
    
        return true_if_added;
    }
    
    
    /*  addEdge  *//*************************************************
    *<p>   
    *   Adds an edge (of implicit value null) between the given vertices. 
    *</p>
    *<p>
    *   The order of the given vertex implies directionality - the edge 
    *   created by this method will use the first given vertex as the 
    *   "parent", the second as the "child". In effect this means that 
    *   the edge will be considered to have originated from the parent 
    *   vertex.
    *<p>
    *   
    *   @param parent       The parent/originating/source vertex.
    *   @param child        The child/sink vertex.
    *   @return             True unless an edge between the given vertices 
    *                       already exists in the graph.
    *   @throws IllegalArgumentException if either of the passed 
    *   vertices are not in the current graph.
    */
    public boolean addEdge( Vertex<E,V> parent, Vertex<E,V> child )
    {
        return addEdge( parent, child, null );
    }
    
    
    /*  addEdge  *//*************************************************
    *
    *   Convenience method for adding edges between vertices with the
    *   given vertex values. Subject to all of the caveats described
    *   in {@link #addEdge(Vertex,Vertex)}.
    *
    *   @param parent       The parent/originating/source vertex value.
    *   @param child        The child/sink vertex value.
    *   @throws IllegalArgumentException if either of the passed 
    *   values do not correspond to vertices in the current graph.
    */
    public boolean addEdge( V parent, V child )
    {
        return addEdge( parent, child, null );
    }
    
    
    /*  addEdge  *//*************************************************
    *<p>   
    *   Adds an edge with the given value between the given vertices. 
    *</p>
    *<p>
    *   The order of the given vertex implies directionality - the edge 
    *   created by this method will use the first given vertex as the 
    *   "parent", the second as the "child". In effect this means that 
    *   the edge will be considered to have originated from the parent 
    *   vertex.
    *</p>
    *
    *   @param parent       The parent/originating/source vertex.
    *   @param child        The child/sink vertex.
    *   @param edge_value   Value of this edge.
    *   @return             True unless an edge with the given value 
    *                       between the given vertices already exists 
    *                       in the graph.
    */
    public boolean addEdge( Vertex<E,V> parent, Vertex<E,V> child, E edge_value )
    {
        return addEdge( createEdge( parent, child, edge_value ) );
    }
    
    
    /*  addEdge  *//*************************************************
    *
    *   Convenience method for adding edges between vertices with the
    *   given vertex values. Subject to all of the caveats described
    *   in {@link #addEdge(Vertex,Vertex)}.
    *
    *   @param parent 
    *   The parent/originating/source vertex (value).
    *   @param child        
    *   The child/sink vertex (value).
    *   @throws IllegalArgumentException 
    *   If either of the passed values do not correspond to vertices in the current graph.
    */
    public boolean addEdge( V parent, V child, E edge_value )
    {
        Vertex<E,V> parentv = getVertex( parent );
        Vertex<E,V> childv  = getVertex( child );
        
        if ( parentv == null )
            throw new IllegalArgumentException("Parent vertex value argument '" 
                + parent 
                + "' does not correspond to a vertex in the current graph; try adding it first");
            
        if ( childv == null )
            throw new IllegalArgumentException("Child vertex value argument '" 
                + child 
                + "' does not correspond to a vertex in the current graph; try adding it first");
        
        return addEdge( parentv, childv, edge_value );
    }
    
    
    /*  addEdge  *//*************************************************
    *<p>   
    *   Adds a pre-defined edge to the current graph. 
    *</p>
    *<p>   
    *   The given edge cannot be null, though it may contain vertices
    *   that do not yet exist in the graph.
    *</p> 
    *   @return             True unless the given edge already exists 
    *                       in the graph.
    */
    protected boolean addEdge( Edge<E,V> edge )
    {
        if ( edge == null )
            throw new IllegalArgumentException(
                "edge argument cannot be null");

        if ( log.isDebugEnabled() )
        {
            if ( edge.parent != edge.child )
            {
                //  regular edge between diff vertices
                log.debug( "adding edge of " 
                    + (edge.value == null ? "no value" : ("value=" + edge.value) )
                    + " between parent="
                    + edge.parent
                    + " and child="
                    + edge.child  
                );
            }
            else
            {
                //  self-referential edge from/to the same vertex
                log.debug( "adding self-referential (circular) edge of value " 
                         + edge.value 
                         + " to "
                         + edge.parent
                         );
            }
        }
        
        //  Skip over the question of whether the vertices in the given 
        //  edge are actually in the graph by just adding them anyway. 
        //  Object member 'vertices' is a Set anyway and won't accept duplicates.
        if ( vertices.add( edge.parent ) )
            log.warn("parent vertex '" 
                + edge.parent 
                + "' of edge was not added to graph, adding it...");
        
        if ( vertices.add( edge.child ) )
            log.warn("child vertex '" 
                + edge.child
                + "' of edge was not added to graph, adding it...");
            
        //  add edge references to vertices. 
        edge.parent.addEdge( edge );
        
        //  only need to add edge once if parent & child are the same vertex.
        if ( edge.parent != edge.child )
            edge.child.addEdge( edge );
        
        return edges.add( edge );
    }
    
    
    /*    addGraph  *//************************************************
    *
    *   Adds an existing graph to this graph. Note that this involves 
    *   a shallow copy of references only, so future changes made to
    *   the graph being added will be observed in the present graph.
    *
    *     @param graph
    *   The graph to be added
    *     @return
    *   True if all vertices and edges were added successfully.
    *   
    *   @see java.util.Collection#addAll(java.util.Collection)
    */
    public boolean addGraph( Graph<E,V> graph )
    {
        boolean true_if_all_added = vertices.addAll( graph.vertices );
        true_if_all_added &= edges.addAll( graph.edges );
        return true_if_all_added;
    }
    
    
    /*  addPath  *//*************************************************
    *<p>   
    *   Convenience method for quickly adding edges/vertices. This method
    *   implicity creates a new edge and vertex with the given values,
    *   and associates them with the given vertex. 
    *</p>
    *<p>   
    *   This method can be useful in combination with the <code>lastVertex()</code>
    *   method to rapidly add edges & vertices to a graph.
    *</p>   
    *
    *   @see #lastVertex()
    *   @param vertex       A pre-existing vertex in the current graph.
    *   @param edge_value   The value of the edge between <code>vertex</code> 
    *                       and the new vertex created with <code>vertex_value</code>.
    *   @param vertex_value The desired value of the vertex created. 
    *   @return             True unless the given edge already exists 
    *                       in the graph.
    */
    public boolean addPath( Vertex<E,V> vertex, E edge_value, V vertex_value )
    {
        assert( vertex != null );
        
        if ( ! this.contains( vertex ) )
            throw new IllegalArgumentException("vertex '" 
                + vertex 
                + "' not present in graph");
        
        Vertex<E,V> v = createVertex( vertex_value );
        addVertex( v );
        
        return addEdge( vertex, v, edge_value );
    }
    
    
    /*  addVertex(V)  *//********************************************
    *   
    *   Adds a new vertex into the current graph.
    * 
    *   @return 
    *   True unless a vertex with the given value already exists in the graph.
    */
    public boolean addVertex( V vertex_value )
    {
        return addVertex( createVertex( vertex_value ) );
    }

            
    /*  addVertex(Vertex)  *//***************************************
    *   
    *   Adds a predeclared vertex into the current graph.
    * 
    *   @return 
    *   True unless the given vertex already exists in the graph.
    */
    protected boolean addVertex( Vertex<E,V> vertex )
    {
        log.debug( "adding vertex of value " + vertex.getValue() );        
        if ( rootVertex == null ) 
            rootVertex = vertex;
        
        lastVertex = vertex;
        boolean added = vertices.add( vertex );
        values.put( vertex.getValue(), vertex );
        vertex.index = vertices.size();
        
        if ( values.size() != vertices.size() )
            throw new IllegalArgumentException("Vertex value '" 
                + vertex.getValue() 
                + "' already exists in graph -- vertex values must be unique");
        
        return added;
    }

    
    /*  clear  *//***************************************************
    *   
    *   Empties this graph of all vertices and edges.
    */
    public void clear() {  _init( -1 );  }
    
    
    
    /*  clone  *//***************************************************
    *   
    *   Returns a shallow copy of this graph -- this means that 
    *   modifications made to the original graph will appear in both 
    *   the original and copied graph. 
    */
    @Override
    @SuppressWarnings("unchecked")
    public Object clone()
    {
        Graph<E,V> copy = null;
        try
        {
            copy          = (Graph<E,V>) super.clone();
            copy.edges    = (Set<Edge<E,V>>) ((HashSet) this.edges).clone(); 
            copy.vertices = (Set<Vertex<E,V>>) ((HashSet) this.vertices).clone(); 
            copy.values   = (Map<V,Vertex<E,V>>) ((HashMap) this.values).clone(); 
        }
        catch ( CloneNotSupportedException ex ) 
        {
            log.warn( ex );
        }
        
        return copy;
    }
    
    
    /*  contains  *//************************************************
    *   
    *   Returns whether this Graph contains the given {@link Vertex}, 
    *   {@link Edge}, or a vertex that has a value equal to the given 
    *   value argument.
    */
    public boolean contains( Object obj )
    {
        if ( obj instanceof Vertex )
            return vertices.contains( obj );
        
        else if ( obj instanceof Edge )
            return edges.contains( obj );
        
        else try 
        {   
            // maybe it's a vertex value?
            //  this try-catch block is only necessary because Java's
            //  generics are erased at compile time, so we can't test
            //  the type of obj against V or E in a runtime instanceof 
            //  expression. lame!!!
            V value = (V) obj;
            return (this.getVertex( value ) != null);
        }
        catch ( ClassCastException dont_care ) {  return false;  }
    }

    
    /** Not supported. */
    public boolean containsAll( Collection<?> c )
    throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Feature not supported");
    }


    /*  countEdges  *//**********************************************
    *
    *   Returns the number of edges in the current graph. Note that
    *   the number of edges is unrelated to the number of vertices.
    */
    public int countEdges()
    {
        return this.edges.size();
    }

    
    /*  countVertices  *//*******************************************
    *
    *   Returns the number of vertices in the current graph. Note that
    *   the number of vertices is unrelated to the number of edges.
    */
    public int countVertices()
    {
        return this.vertices.size();
    }


    /*  createEdge  *//**********************************************
    *
    *   Factory style constructor for Edge instances. Note that this
    *   method only creates edges; it does not add them to the current
    *   graph. 
    *   
    *   Subclasses should feel free to reimplement this method if they
    *   wish to create more specialised Edge classes.
    *   
    *   @param parent       The parent/originating/source vertex.
    *   @param child        The child/sink vertex.
    *   @param edge_value   Value of the edge.
    *   @return             A new edge instance.
    *   @throws IllegalArgumentException if either parent or child are null,
    *   or if either parent or child are not in the current Graph.
    */
    protected Edge<E,V> createEdge( Vertex<E,V> parent, Vertex<E,V> child, E edge_value )
    {
        if ( child == null )
            throw new IllegalArgumentException(
                "child vertex of edge cannot be null");
        
        if ( parent == null )
            throw new IllegalArgumentException(
                "parent vertex of edge cannot be null");
        
        if ( ! this.contains( parent ) )
            throw new IllegalArgumentException("Parent vertex '" 
                + parent 
                + "' not present in graph");
            
        if ( ! this.contains( child ) )
            throw new IllegalArgumentException("Child vertex '" 
                + child 
                + "' not present in graph");
            
        return new Edge<E,V>( parent, child, edge_value );
    }
    
    
    //  possible enhancement for later?
    // /** 
    // *   Creates a subgraph of the current Graph -- changes to 
    // *   vertices and edges in the sub-graph will also be reflected 
    // *   in this Graph.
    // */
    // public Graph<E,V> createSubgraph()
    // {
    //        
    // }
    
    
    /*  createVertex  *//********************************************
    *<p>   
    *   Factory style constructor for Vertex instances. Note that this
    *   method only creates vertices; it does not add them to the current
    *   graph.
    *</p>
    *<p>
    *   Subclasses should feel free to reimplement this method if they
    *   wish to create more specialised Vertex classes.
    *</p>
    *   @param value        The value of the created vertex.
    *   @return             A new vertex instance.
    */
    protected Vertex<E,V> createVertex( V value ) {  return new Vertex<E,V>( value );  }
               
    
    /*  getAllEdges  *//*********************************************
    *   
    *   Returns the set of Edges in this graph. Note that the returned
    *   set is a <em>copy</em> of the set of edges in the graph, and so 
    *   can be freely modified.
    */
    public Set<Edge<E,V>> getAllEdges()
    {
        //return new HashSet<Edge<E,V>>( edges );
        return Collections.unmodifiableSet( edges );
    }


    /*  getAllEdgeValues  *//****************************************
    *   
    *   Returns all edge values in this graph as a List.
    */
    public List<E> getAllEdgeValues()
    {
        List<E> values = new ArrayList<E>( edges.size() );
        for ( Edge<E,V> e : edges )
            values.add( e.getValue() );
        
        return values;
    }

    
    /*  getAllVertexValues  *//******************************************
    *   
    *   Returns a (unmodifiable) {@link Set} of all {@link Vertex} 
    *   values in the current graph.
    */
    public Set<V> getAllVertexValues()
    {
        return Collections.unmodifiableSet( values.keySet() );
    }

    
//  TODO only if needed...
//
//    /*  getAllPaths  *//*********************************************
//    *   
//    *   Returns a list of a list of vertex paths through the tree, one
//    *   path per leaf vertex. Each path list comprises all vertices from
//    *   the leaf vertex to the root, inclusive.
//    */
//    public List<List<Vertex<E,V>>> getAllPaths()
//    {
//        List<List<Vertex<E,V>>> paths = new ArrayList<List<Vertex<E,V>>>();
//        List<Vertex<E,V>>      leaves = this.getLeaves();
//        
//        for ( Vertex<E,V> v : leaves )
//        {
//            List<Vertex<E,V>> path = this.getAllParentsOf( v );
//            path.add( 0, v );
//            paths.add( path );
//        }
//        
//        return paths;
//    }
//
    
    /*  getAllVertices  *//******************************************
    *   
    *   Returns the set of vertices in the current graph.
    */
    public Set<Vertex<E,V>> getAllVertices()
    {
        return Collections.unmodifiableSet( vertices );
    }
     
    
    /*  getEdges  *//************************************************
    * 
    *   Returns the list of edges that exist in this graph between the
    *   given 2 vertices, returning an empty list if no edges exist
    *   between these vertices.
    */
    public Set<Edge<E,V>> getEdges( Vertex<E,V> v1, Vertex<E,V> v2 )
    {
        Set<Edge<E,V>> e1 = v1.getAttachedEdges();
        if ( e1.size() == 0 ) 
            return Collections.emptySet();

        Set<Edge<E,V>> e2 = v2.getAttachedEdges();
        if ( e2.size() == 0 ) 
            return Collections.emptySet();
        
        Set<Edge<E,V>> intersection = new HashSet<Edge<E,V>>();
        
        intersection.addAll( e1 );
        intersection.retainAll( e2 );

        if ( intersection.size() == 0 ) 
            return Collections.emptySet(); //Collections.EMPTY_LIST

        // return new ArrayList<Edge<E,V>>( intersection );
        return intersection;
    }
    

    /*  getEdges  *//************************************************
    * 
    *   Returns the list of edge values that exist in this graph between 
    *   the given 2 vertices, returning an empty list if no edges exist
    *   between these vertices. Note that this is a {@link List} whereas
    *   the other {@link getEdges} method returns a {@link Set}. This is 
    *   because this method cannot assume that Edge <em>values</em> 
    *   are always unique.
    */
    public List<E> getEdges( V v1, V v2 )
    {
        Set<Edge<E,V>> edges = getEdges( getVertex(v1), getVertex(v2) );

        if ( edges.size() == 0 ) 
            return Collections.emptyList();
            
        List<E> edge_values = new ArrayList<E>( edges.size() );
        for ( Edge<E,V> e : edges )
            edge_values.add( e.getValue() );
            
        return edge_values;
    }
    
    
    /** 
    *   Returns the {@link Set} of values that correspond to (leaf) vertices; 
    *   ie: vertices that have exactly one incoming {@link Edge} and no 
    *   outgoing {@link Edge}s (ie: no children). 
    */
    public Set<V> getLeafValues()
    {
        Set<V> leaves = new HashSet<V>();
        for ( Vertex<E,V> v : getLeafVertices() )
            leaves.add( v.getValue() );
            
        return leaves;
    }
    

    /** 
    *   Leaf vertices are vertices that have exactly one incoming {@link Edge}
    *   and no outgoing {@link Edge}s. 
    */
    public Set<Vertex<E,V>> getLeafVertices()
    {
        Set<Vertex<E,V>> leaves = new HashSet<Vertex<E,V>>();
        for ( Vertex<E,V> v : vertices )
            if ( v.getIncomingEdges().size() == 1
                && v.getOutgoingEdges().size() == 0 )
                leaves.add( v );
            
        if ( vertices.size() == 1 )
            leaves.addAll( vertices );
            
        return leaves;
    }

    
    /*  getRootVertex  *//*******************************************
    *   
    *   Returns the value of the nominal 'root' vertex of this graph;
    *   see {@link #getRootVertex()}. 
    */
    public V getRootValue() 
    {  
        if ( this.isEmpty() ) return null;
        return this.rootVertex.getValue();  
    }

    
    /*  getRootVertex  *//*******************************************
    *   
    *   Returns the nominal 'root' vertex of this graph. Graphs do not
    *   ordinarily have a "root" vertex, however for the purposes of this
    *   class, the root vertex of a graph is used in various places as the
    *   default "starting vertex" for various types of iteration and graph
    *   representation. It also serves as the canonical root vertex for
    *   graphs that are themselves trees. If not specifically assigned,
    *   the root vertex of a graph instance is the first added vertex.
    *   This method returns null if the graph itself is devoid of vertices
    *   (ie: <tt>isEmpty()</tt> returns <tt>true</tt>).
    */
    public Vertex<E,V> getRootVertex() 
    {  
        if ( this.isEmpty() ) return null;
        return this.rootVertex;  
    }


    /*  getVertex  *//***********************************************
    *   
    *   Returns the vertex with the given value, or null if no such 
    *   vertex exists.
    */
    public Vertex<E,V> getVertex( V vertex_value )
    {
        // for ( Vertex<E,V> v : vertices )
            // if ( v.getValue().equals( vertex_value ) )
                // return v;
                
        return values.get( vertex_value );
    }
 
    
    /*  isEmpty  *//*************************************************
    *   
    *   Returns whether this tree is empty.
    */
    public boolean isEmpty() {  return vertices.isEmpty();  }
    
    
    /*  iterator  *//************************************************
    *
    *   Currently returns an iterator over the vertices in this tree,
    *   in chronological order. This method allows statements of the
    *   form:
    *   <pre>
    *       Graph<E,V> my_graph = ...;
    *       for ( Vertex<E,V> v : my_graph ) { ... } 
    *   </pre>
    */
    public Iterator<Vertex<E,V>> iterator()
    {
        return vertices.iterator();
    }
    
    
    /*  lastVertex  *//**********************************************
    *   
    *   Convenience method that returns the last added vertex.
    */
    public Vertex<E,V> lastVertex()
    {
        return ( this.isEmpty() ) ? null : lastVertex;
    }

    
    /*  remove  *//**************************************************
    *   
    *   Removes the given {@link Edge}, {@link Vertex}, or vertex value 
    *   from the graph, returning true if said edge or vertex was 
    *   removed from the graph. If the passed object corresponds to 
    *   the value of a vertex in the graph, then that vertex is removed.
    *   
    *   @throws IllegalArgumentException if the passed 
    *   object is not an edge, vertex, nor vertex value type.
    */
    public boolean remove( Object obj )
    {
        if ( obj instanceof Vertex )
        {
            return remove( (Vertex<E,V>) obj );
        }
        else if ( obj instanceof Edge )
        {
            return remove( (Edge<E,V>) obj );
        }
        else 
        {
            try
            {
                @SuppressWarnings("unchecked") // fuck you java
                V value = (V) obj;
                return remove( getVertex( value ) );
            }
            catch ( ClassCastException dont_care ) 
            {  
                throw new IllegalArgumentException(
                    "Passed object is neither a Vertex, Edge, nor Vertex value");
            }
        }
    }
    
    
    /*  remove  *//**************************************************
    *   
    *   Removes the given {@link Vertex} from the graph, implicitly
    *   also removing all {@link Edge}s of this vertex.
    *   
    *   @throws IllegalArgumentException if the passed vertex is not in 
    *   the graph.
    */
    public boolean remove( Vertex<E,V> v )
    {
        if ( log.isDebugEnabled() )
            log.debug("removing vertex: " + v );
        
        if ( ! vertices.contains( v ) )
            throw new IllegalArgumentException(
                "Vertex " + v + " not found in graph");
        
        //  need to make copy of edge list cause if not, weird shit happens    
        List<Edge<E,V>> my_edges = new ArrayList( v.getAttachedEdges() );
        for ( Edge<E,V> e : my_edges )
        {
            remove( e );
        }
        // log.debug("after: " + edges.size()  );
        // log.debug("implicitly removed " + my_edges.size() + " edges of vertex " + v );
                
        vertices.remove( v );
        values.remove( v.getValue() );
        assert values.size() == vertices.size();
            
        //  there is a subtle problem with removing the last vertex,
        //  since lastVertex() will return a vertex that is no longer
        //  in the graph. so we null it for just this case in the hope
        //  that this makes it easier to get picked up later.
        if ( v == lastVertex )
            lastVertex = null;
            
        return true;
    }
    
    
    /*  remove  *//**************************************************
    *   
    *   Removes the given {@link Edge} from the graph. 
    *   
    *   @throws IllegalArgumentException 
    *           if the passed edge is not in the graph.
    */
    public boolean remove( Edge<E,V> e )
    {
        if ( log.isDebugEnabled() )
            log.debug("removing edge: '" + e );

        if ( ! edges.contains( e ) )
            throw new IllegalArgumentException(
                "Edge " + e + " not found in graph");

        e.free();
        edges.remove( e );
            
        return true;
    }
    

    /*  removeAll  *//***********************************************
    *   
    *   Removes the given vertices from the tree; any edges attached to 
    *   removed vertices are similarly removed.
    */
    public boolean removeAll( Collection<?> vertices_to_remove )
    {
        if ( vertices.removeAll( vertices_to_remove ) )
        {
            // Remove edges that have vertices that are no longer in the graph. */   
            for ( Edge<E,V> edge : edges )
                if ( ! (vertices.contains(edge.parent) && vertices.contains(edge.child)) )
                    remove( edge );

            return true;
        }
        else return false;
    }
    

    /*  retainAll  *//**************************************************
    *   
    *   Removes all vertices that are not in the passed collection of 
    *   vertices. All edges that are attached to vertices removed are
    *   similarly removed.
    */
    public boolean retainAll( Collection<?> vertices_to_keep )
    {
        if ( vertices.retainAll( vertices_to_keep ) )
        {
            // Remove edges that have vertices that are no longer in the graph. 
            //  is this code even necessary? if retainAll calls remove() then
            //  edge will already be getting removed.
            for ( Edge<E,V> edge : edges )
                if ( ! (vertices.contains(edge.parent) && vertices.contains(edge.child)) )
                    remove( edge );

            return true;
        }
        else return false;
    }
    
    
    /*    setRootVertex  *//*******************************************
    *
    *   Sets the given vertex as the root vertex of this tree.
    */
    public void setRootVertex( Vertex<E,V> v )
    {
        assert v != null;
        assert this.contains( v );
        
        rootVertex = v;
    }


    /*    size  *//****************************************************
    *
    *     Same as countVertices(). Only here to preserve compatibility/familiarity
    *   with java Collection-like semantics.
    */
    public int size()
    {
        return countVertices();
    }
    
    
    /*    traverseBreadthFirst  *//************************************
    *
    *   Returns an iterator over this graph in breadth-first order,
    *   starting at the root vertex of this graph.
    *   
    *   @see Graph.GraphIterator.BreadthFirst
    */
    public GraphIterator<E,V> traverseBreadthFirst()
    {
        return traverseBreadthFirst( getRootVertex() );
    }

    
    /*  traverseBreadthFirst  *//************************************
    *
    *   Returns an iterator over this graph in breadth-first order,
    *   starting at the given vertex within the graph.
    */
    public GraphIterator<E,V> traverseBreadthFirst( Vertex<E,V> starting_vertex )
    {
        return new GraphIterator.BreadthFirst<E,V>( this, starting_vertex );
    }
    
   
    /*    traverseDepthFirst  *//***************************************
    *
    *   Returns an iterator over this graph in depth-first order,
    *   starting at the root vertex of this graph.
    *
    *     @see Graph.GraphIterator.DepthFirst
    */
    public GraphIterator<E,V> traverseDepthFirst()
    {
        return traverseDepthFirst( getRootVertex() );
    }

    
    /*  traverseDepthFirst  *//***************************************
    *
    *   Returns an iterator over this graph in depth-first order,
    *   starting at the given vertex within the graph.
    */
    public GraphIterator<E,V> traverseDepthFirst( Vertex<E,V> starting_vertex )
    {
        return new GraphIterator.DepthFirst<E,V>( this, starting_vertex );
    }


    /*  implementation of java.util.Set#toArray()  */
    public Object[] toArray() {  return getAllVertexValues().toArray();  }


    /*  implementation of java.util.Set#toArray(T[])  */
    public <V> V[] toArray( V[] values )
    {
        return getAllVertexValues().toArray( values );
    }
    
    
    /*  toString  *//************************************************
    *   
    *   Returns a simple text representation of all vertices and edges
    *   in the current graph. This representation is meant to be more
    *   useful for deciphering the graph, as opposed to being visually
    *   aesthetic.
    *   
    *   @see java.lang.Object#toString()
    */
    public String toString_old()
    {        
        StringBuilder sb = new StringBuilder();
        List<Vertex<E,V>> vlist = new ArrayList<Vertex<E,V>>( getAllVertices() );
        
        sb.append( "GRAPH of " 
                 + vertices.size() 
                 + " vertices, " 
                 + edges.size() 
                 + " edges"
                 );
        
        if ( vertices.size() > 0 )
            sb.append( ", with root vertex "
                     + rootVertex
                     + " ("
                     + vlist.indexOf( rootVertex )
                     + ")\n" 
                     );
        
        for ( Vertex<E,V> v : vlist )
        {
            sb.append( "    VERTEX " 
                     + v.getValue() 
                     + " has " 
                     + v.countAttachedEdges()
                     + " edge(s)"
                     );
            
            if ( v.countAttachedEdges() > 0 )
            {
                sb.append( ":\n" 
                         + "        EDGE "
                         + join( "\n        EDGE ", v.getAttachedEdges() ) 
                         + "\n"
                         );
            }
            else
            {
                sb.append( "\n" );
            }
        }
        
        return sb.toString();
    }

    
    public String toString()
    {        
        StringBuilder sb = new StringBuilder();
        
        sb.append( "GRAPH of " 
                 + vertices.size() 
                 + " vertex(es), " 
                 + edges.size() 
                 + " edge(s)"
                 );

        if ( vertices.size() > 0 )
        {
            sb.append("\nVertexes:");
            List<Vertex<E,V>> vlist = new ArrayList<Vertex<E,V>>( vertices );
            Collections.sort( vlist );
            
            for ( Vertex<E,V> v : vlist )    
            {
                sb.append(
                    "\n    "
                    + v.index
                    + ": "
                    + v.getValue()
                );           
            }
            
            if ( edges.size() > 0 )
            {
                sb.append("\nEdges:");
                for ( Vertex<E,V> v : vlist )    
                {
                    for ( Edge<E,V> e : v.getOutgoingEdges() )    
                    {
                        sb.append(
                            "\n    "
                            + e.getParent().index
                            + " --> "
                            + e.getChild().index
                            + ": "
                            + e.getValue()
                        );  
                    }
                }
            }
        }
        
        return sb.toString();
    }    
    
    
    
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~  PRIVATE METHODS  ~~~~~~~~~~~~~~~~~~~//

    /** Initialises/resets the current graph to its starting state. */
    private final void _init( int size )
    {
        if ( vertices == null || edges == null )
        {
            log.debug( "initialising graph of size=" + size );  

            if ( size < 0 ) 
                size = Default_Tree_Size;

            vertices = new HashSet<Vertex<E,V>>( size ); 
            values   = new HashMap<V,Vertex<E,V>>( size ); 
            edges    = new HashSet<Edge<E,V>>( size ); 
        }
        else
        {
            log.debug( "re-initialising graph (clearing vertices/edges)" ); 
            values.clear();
            vertices.clear(); 
            edges.clear(); 
        }
        
        rootVertex = null;
        lastVertex = null;
        
        return;
    }


    /** method to prevent type-cast warnings from java compiler because 
    *   java's generic are so poorly implemented. unavoidable :(  */
    @SuppressWarnings("unchecked")              
    private final Vertex<E,V> _cast_to_Vertex( Object  obj ) {  return (Vertex<E,V>) obj;  }

    /** method to prevent type-cast warnings from java compiler because 
    *   java's generic are so poorly implemented. unavoidable :(  */
    @SuppressWarnings("unchecked")              
    private final V _cast_to_V( Object  obj ) {  return (V) obj;  }

    
} // end of class Graph 

