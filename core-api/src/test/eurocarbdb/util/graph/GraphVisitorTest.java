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

/**
*   Test tree used (object member {@link #tree}) in this class is:
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
*<h2>depth-first traversal</h2>
*<p>
*   If the starting vertex was A, the returned list in an unsorted DF 
*   traversal should be one of: 
*<ol>
*   <li>A, B, C, E, F, G, D, H</li>
*   <li>A, B, D, H, C, E, F, G</li>
*   <li>A, B, D, H, C, E, G, F</li>
*   <li>A, B, C, E, G, F, D, H</li>
*</ol>
*</p>
*/
@Test( groups="util.graphs", sequential=true, timeOut=5000 )
public class GraphVisitorTest
{
    protected Graph<Integer,Character> tree = new Graph<Integer,Character>();

    @BeforeTest
    public void setupLogging()
    {
        Logger.getLogger("org.eurocarbdb.util.graph").setLevel( Level.ALL );   
    }
    
    @Test
    public void graphInitialConstruction()
    {
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
    
    
    @Test( dependsOnMethods = {"graphInitialConstruction"} )
    public void graphDepthFirstTraversalSmokeTest()
    {
        DepthFirstGraphVisitor<Integer,Character> v1 
            = new DepthFirstGraphVisitor<Integer,Character>();
            
        v1.visit( tree );
    }

    
    @Test( dependsOnMethods = {"graphInitialConstruction"} )
    public void graphDepthFirstTraversal()
    {
        DepthFirstGraphVisitor<Integer,Character> v2 
            = new DepthFirstGraphVisitor<Integer,Character>() 
            {
                int depth = 0;
            
                public void accept( Vertex<Integer,Character> v )
                {
                    depth++;
                    for ( int i = 1; i < depth; i++ ) out.print("    "); 
                    out.println( "> approaching vertex " + v.getValue() );
                    
                    super.accept( v );
                    
                    for ( int i = 1; i < depth; i++ ) out.print("    "); 
                    out.println( "< leaving vertex " + v.getValue() );
                    depth--;
                }
            };
        
        v2.visit( tree );
    }
    
    
    /** 
    *   This tests that a (unsorted) depth-traversal traversal proceeds
    *   in a correct order.
    */
    @Test( dependsOnMethods = {"graphInitialConstruction"} )
    public void graphDepthFirstTraversalOrderIsCorrect()
    {
        final char[] correct_order1 = {'A', 'B', 'C', 'E', 'F', 'G', 'D', 'H' };
        final char[] correct_order2 = {'A', 'B', 'C', 'E', 'G', 'F', 'D', 'H' };
        final char[] correct_order3 = {'A', 'B', 'D', 'H', 'C', 'E', 'F', 'G' };
        final char[] correct_order4 = {'A', 'B', 'D', 'H', 'C', 'E', 'G', 'F' };
        final char[] observed_order = new char[ correct_order1.length ]; 
        
        DepthFirstGraphVisitor<Integer,Character> v3 
            = new DepthFirstGraphVisitor<Integer,Character>() 
                {
                    int index = 0;
                    
                    public void accept( Vertex<Integer,Character> v )
                    {
                        char value = v.getValue();
                        observed_order[index] = value;
                        index++;
                        
                        super.accept( v );
                    }
                };
        
        v3.visit( tree );        
        
        out.println("expecting one of the following:");
        out.println( correct_order1 );
        out.println( correct_order2 );
        out.println( correct_order3 );
        out.println( correct_order4 );

        out.println();
        out.println("observed:");
        out.println( observed_order );
        
        assert Arrays.equals( correct_order1, observed_order )
            || Arrays.equals( correct_order2, observed_order )
            || Arrays.equals( correct_order3, observed_order )
            || Arrays.equals( correct_order4, observed_order )
            : "order of vertices in depth-first traversal was incorrect"
            ;
        
        out.println();
        out.println("depth-first order is correct");
    }
    
    
    private final void countEdgesOf( char vertex, int expected_nmb_edges )
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
        
} // end class



