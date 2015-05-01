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

package test.eurocarbdb.util.graph;

import java.util.*;
import org.apache.log4j.Logger; 
import org.apache.log4j.Level; 

import org.testng.annotations.*;

import org.eurocarbdb.util.graph.*;

import static java.lang.System.out;
import static org.eurocarbdb.util.StringUtils.join;

@Test( groups="util.graphs", sequential=true, timeOut=2000 )
/**
*   Tests fundamental operation of {@link Graph} class.
*/
public class GraphTest
{
    /**
    *   contains the following graph, once populated:
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
    */
    protected Graph<Integer,Character> tree;

    protected Graph<Float, String> g1;
    
    protected Graph<Float, String> g2;
    
    
    @BeforeTest
    public void setupLogging()
    {
        Logger.getLogger("org.eurocarbdb.util.graph").setLevel( Level.ALL );   
    }
    
    
    @Test
    /** Constructs empty graph, g1 */
    public void graphInitialConstruction()
    {
        g1 = new Graph<Float,String>();
        
        assert g1 != null;  
        
        assert g1.size() == 0;
        
        assert g1.getAllVertices() != null;
        assert g1.getAllVertices().size() == 0;
        
        assert g1.getAllEdges() != null;
        assert g1.getAllEdges().size() == 0;
        
        Collection<String> values = g1.getAllVertexValues();
        assert values != null;
        assert values.size() == 0;
        
        out.println("Constructed zero-size graph:");
        out.println( g1 );
    }
    
    
    @Test( dependsOnMethods = {"graphInitialConstruction"} )
    /**
    *   Populates variable {@link #tree}, the test graph created is:
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
    */
    public void graphAdditionOfVerticesAndEdges()
    {
        tree = new Graph<Integer,Character>();

        tree.addVertex('A');
        tree.addVertex('B');
        tree.addVertex('C');
        tree.addVertex('D');
        tree.addVertex('E');
        tree.addVertex('F');
        tree.addVertex('G');
        tree.addVertex('H');
        tree.addEdge('A', 'B');
        tree.addEdge('B', 'C');
        tree.addEdge('C', 'E');
        tree.addEdge('E', 'F');
        tree.addEdge('E', 'G');
        tree.addEdge('B', 'D');
        tree.addEdge('D', 'H');
        
        out.println("tree:");
        out.println( tree.toString() );
        
        //  sanity check graph size
        assert tree.size() == 8;
        assert tree.countEdges() == 7;
        assert tree.countVertices() == 8;

        //  check #edges for each vertex conforms to expected number         
        countEdgesOf( 'A', 1 );
        countEdgesOf( 'B', 3 );
        countEdgesOf( 'C', 2 );
        countEdgesOf( 'D', 2 );
        countEdgesOf( 'E', 3 );
        countEdgesOf( 'F', 1 );
        countEdgesOf( 'G', 1 );
        countEdgesOf( 'H', 1 );        
    }
    
    
    final void countEdgesOf( char vertex, int expected_nmb_edges )
    {
        out.println( 
            "expected "
            + expected_nmb_edges
            + " edge(s) for vertex '"
            + vertex
            + "', observed: "
            + join(", ", tree.getVertex( vertex ).getAttachedEdges())  
        );
        assert tree.getVertex( vertex ).countAttachedEdges() == expected_nmb_edges;
        assert tree.getVertex( vertex ).countAttachedVertices() == expected_nmb_edges;
    }
    
        
    @Test( dependsOnMethods = {"graphInitialConstruction"} )
    /**
    *   populates graph g1:
    *<pre>
    *       vertex1 --- 0.75 --> vertex2 --- 0.25 ---> vertex3
    *                               |
    *                              0.1 
    *                               |
    *                              \|/
    *                            vertex4
    *</pre>
    *   graph is circularised in method 'graphCircularise'
    */
    public void graphAdditionOfVerticesAndEdgesWithValues()
    {
        //  1 Vertex  
        //
        //      vertex1
        //
        out.println( "adding first vertex: vertex1" );
        g1.addVertex( "vertex1" );
       
        out.println( "last vertex added was " + g1.lastVertex() );
        out.println( "graph is now:" );
        out.println( g1 );
        out.println();
        
        assert g1.lastVertex().getValue() == "vertex1";
        assert g1.size() == 1;
        assert g1.lastVertex().getIncomingEdges().size() == 0;
        assert g1.lastVertex().getOutgoingEdges().size() == 0;
        
        //  add 1 vertex + 1 edge; total 2 vertices, 1 edge.
        //
        //      vertex1 --- 0.75 --> vertex2
        //
        g1.addPath( g1.lastVertex(), 0.75f, "vertex2" );
       
        out.println( "last vertex added was " + g1.lastVertex() );
        out.println( "graph is now:" );
        out.println( g1 );
        out.println();

        assert g1.size() == 2;
        assert g1.countEdges() == 1;
        assert g1.countVertices() == 2;
        assert g1.lastVertex().getValue() == "vertex2";
        assert g1.lastVertex().getIncomingEdges().size() == 1;
        assert g1.lastVertex().getOutgoingEdges().size() == 0;
        
        //  add another vertex + edge; total 3 vertices, 2 edges.
        //
        //      vertex1 --- 0.75 --> vertex2 --- 0.25 ---> vertex3
        //
        Vertex<Float,String> v = g1.lastVertex();
        out.println( "adding vertex: vertex3" );
        g1.addVertex( "vertex3" );

        out.println( "adding edge: vertex2 -> 0.25 -> vertex3" );
        g1.addEdge( v, g1.lastVertex(), 0.25f );
        out.println( "graph is now:" );
        out.println( g1 );
        out.println();
        
        assert g1.lastVertex().getValue() == "vertex3";
        assert g1.size() == 3;
        assert g1.countEdges() == 2;
        assert g1.countVertices() == 3;
        
        //  + 1 vertex + 1 edge
        //
        //      vertex1 --- 0.75 --> vertex2 --- 0.25 ---> vertex3
        //                              |
        //                             0.1 
        //                              |
        //                             \|/
        //                           vertex4
        //
        out.println( "adding path: vertex2 -> 0.1 -> vertex4" );
        g1.addPath( g1.getVertex( "vertex2" ), 0.1f, "vertex4" );
        
        assert g1.countEdges() == 3;
        assert g1.countVertices() == 4;
        
        out.println( "graph is now:" );
        out.println( g1 );
        out.println();
    }
        
    @Test( dependsOnMethods = {"graphAdditionOfVerticesAndEdgesWithValues"} )
    /** Simple listing of vertices/edges. */
    public void graphListVerticesAndEdges()
    {
        assert g1.countEdges() == 3;
        assert g1.countVertices() == 4;
        
        //  list vertices
        Collection<String> vlist = g1.getAllVertexValues();
       
        out.println( "vertices are: " + vlist );
        assert vlist.size() == 4;
        
        Collection<Float> elist = g1.getAllEdgeValues();
       
        System.out.println( "edges are: " + elist );
        assert elist.size() == 3;
        
        //System.out.println( "leaves are: " + g.getLeaves() );
        //System.out.println( "current root vertex is: " + g.getRootVertex() );
    }
    
    
    @Test( dependsOnMethods = {"graphListVerticesAndEdges"} )
    /**
    *   circularises graph g1, to:
    *<pre>
    *                         ___ 0.02 ___ 
    *                       /             \
    *                 vertex6 -- 0.98     vertex5
    *                       \     |       /
    *                      0.02   |    0.01
    *                         \   |   / 
    *       vertex1 -- 0.75 -- vertex2 -- 0.25 -- vertex3
    *                             |    
    *                            0.1   
    *                             |    
    *                          vertex4     
    *</pre>
    */
    public void graphCircularise()
    {
        assert g1.countEdges() == 3;
        assert g1.countVertices() == 4;

        out.println( "adding path: vertex2 -> 0.01 -> vertex5" );
        g1.addPath( g1.getVertex( "vertex2" ), 0.01f, "vertex5" );

        assert g1.countEdges() == 4;
        assert g1.countVertices() == 5;

        out.println( "adding path: vertex5 -> 0.02 -> vertex6" );
        g1.addPath( g1.lastVertex(), 0.02f, "vertex6" );
        
        assert g1.countEdges() == 5;
        assert g1.countVertices() == 6;

        out.println( "adding path: vertex6 -> 0.98 -> vertex2" );
        g1.addEdge( g1.getVertex("vertex6"), g1.getVertex("vertex2"), 0.98f );
        
        assert g1.countEdges() == 6;
        assert g1.countVertices() == 6;

        out.println( "adding loop: vertex6 -> 0.02 -> vertex2" );
        g1.addEdge( g1.getVertex("vertex6"), g1.getVertex("vertex2"), 0.02f );

        assert g1.countEdges() == 7;
        assert g1.countVertices() == 6;

        out.println( "graph is now:" );
        out.println( g1 );
        out.println();
        
        for ( Vertex<Float,String> v : g1 )
        {
            List<Edge<Float,String>> inclist = v.getIncomingEdges();
            out.println( "incoming edges of " 
                + v 
                + " == " 
                + inclist.size() 
                + ": " 
                + inclist 
            );
            
            List<Edge<Float,String>> outlist = v.getOutgoingEdges();
            out.println( "outgoing edges of " 
                + v 
                + " == " 
                + outlist.size() 
                + ": " 
                + outlist 
            );
        }
    }
    
    
    @Test( dependsOnMethods = {"graphCircularise"} )
    /** 
    *   Tests behaviour of graph g1 as a {@link Set}: presence/absence of
    *   vertices in graph. 
    */
    public void graphContains()
    {
        assert g1.countEdges() == 7;
        assert g1.countVertices() == 6;

        boolean b = false;

        out.println( "testing if graph contains each of its own vertices" );
        for ( Vertex<Float,String> v : g1.getAllVertices() )
        {
            b = g1.contains( v );
            out.println( "contains " + v + ": " + b );
            assert b;
        }
        
        assert b : "didn't enter iteration loop!";
        out.println();
        b = false;
        
        out.println( "testing if graph contains each of its own vertex values" );
        for ( String s : g1.getAllVertexValues() )
        {
            b = g1.contains( s );
            out.println( "contains vertex value '" + s + "': " + b );
            assert b;
        }
        
        assert b : "didn't enter iteration loop!";
        out.println();
        b = true;

        out.println( "testing if graph contains some random strings (these should all be false)" );
        for ( String s : new String[] {"sdfsdf", "423234", "dhhdfg", "£$%%%^"} )
        {
            b = g1.contains( s );
            out.println( "contains vertex value '" + s + "': " + b );
            assert ! b;
        }
        assert ! b : "didn't enter iteration loop!";
    }
    
    
    @Test( dependsOnMethods = {"graphContains"} )
    /** Tests removal of vertices. Adds a vertex to g1 then removes it. */
    public void graphSubtraction()
    {
        out.println( "testing the removal of vertices" );
        int ce = g1.countEdges();
        int cv = g1.countVertices();

        boolean b = false;
        out.println();
        out.println( "adding a temporary vertex" );
        assert ! g1.contains( "temp" );
        
        g1.addVertex("temp");
        b = g1.contains( "temp" );
        out.println("contains vertex 'temp'? " + b );
        assert b;
        
        g1.remove( "temp" );
        b = g1.contains( "temp" );
        out.println("contains vertex 'temp' post-removal? " + b );
        assert ! b;

        //  check graph is exactly the same as when we started
        assert g1.countEdges() == ce;
        assert g1.countVertices() == cv;
    }
    
    
    @Test( dependsOnMethods = {"graphCircularise"} )
    /**
    *   Tests depth-first traversal using circularised graph g1:
    *<pre>
    *                         ___ 0.02 ___ 
    *                       /             \
    *                 vertex6 -- 0.98     vertex5
    *                       \     |       /
    *                      0.02   |    0.01
    *                         \   |   / 
    *       vertex1 -- 0.75 -- vertex2 -- 0.25 -- vertex3
    *                             |    
    *                            0.1   
    *                             |    
    *                          vertex4     
    *</pre>
    */
    public void graphTraversal()
    {
        //~~~  graph traversal ~~~        

        out.print( "depth-first order  : " );
        GraphIterator<Float,String> dfs = g1.traverseDepthFirst();
        
        List<String> values = dfs.getValues();
        out.println( join( ", ", values ) );
        assert values.size() == g1.size();
        
        out.print( "breadth-first order: " );
        GraphIterator<Float,String> bfs = g1.traverseBreadthFirst();
        
        values = bfs.getValues();
        out.println( join( ", ", values ) );
       
       
        out.println( "using stepwise iteration:" );
        dfs.reset();
        bfs.reset();
        
        out.println("depth-first - breadth-first");
        while ( dfs.hasNext() && bfs.hasNext() )
        {
            out.println( dfs.nextVertex()
                       + " - "
                       + bfs.nextVertex()
                       );
        }
    }
        
    
    @Test( dependsOnMethods = {"graphCircularise"} )
    /** Adds entire graph g1 to graph 2, and links them through vertices vertex1 & newvertex1. */
    public void graphAddition()
    {
        //~~~  graph + graph operations ~~~
        
        //  record starting state
        int ce = g1.countEdges();
        int cv = g1.countVertices();

        out.println();
        out.println( "~~~ adding a graph to a graph ~~~" );
        
        //  g2:
        //
        //      newvertex1 --- 1.0 --> newvertex2 --- 1.0 ---> newvertex3
        //                                  |
        //                                 1.0
        //                                  |
        //                              newvertex4
        //
        g2 = new Graph<Float,String>();
        g2.addVertex( "newvertex1" );
        g2.addPath( g2.lastVertex(), 1.0f, "newvertex2" );
        g2.addPath( g2.lastVertex(), 1.0f, "newvertex3" );
        g2.addPath( g2.getVertex("newvertex2"), 1.0f, "newvertex4" );
        
        out.println("the graph to be added:");
        out.println( g2 );
        
        assert g2.countEdges() == 3;
        assert g2.countVertices() == 4;
        
        //  add g1 to g2
        g2.addGraph( g1 );
        g2.addEdge( g1.getVertex("vertex1"), g2.getVertex("newvertex1") );    
        
        out.println("the added graph:");
        out.println( g2 );
        
        //  check final state
        assert g2.countEdges() == ce + 4 
            : "expected " + (ce+4) + " edges, found " + g1.countEdges();
        assert g2.countVertices() == cv + 4
            : "expected " + (cv+4) + " vertices, found " + g1.countVertices();
    }


    @Test( dependsOnMethods = {"graphAddition"} )
    /** Clears graph g2 */
    public void graphClear()
    {
        assert g2.countVertices() > 0;
        out.println( "~~~ clearing a graph ~~~" );
        
        g2.clear();
        
        assert g2.countEdges() == 0;
        assert g2.countVertices() == 0;
    }
    
} // end class
