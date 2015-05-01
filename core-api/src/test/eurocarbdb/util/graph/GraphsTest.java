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
*   Last commit: $Rev: 1394 $ by $Author: glycoslave $ on $Date:: 2009-07-03 #$  
*/

package test.eurocarbdb.util.graph;

import java.util.*;
import org.apache.log4j.Logger; 

import org.testng.annotations.*;

import org.eurocarbdb.util.graph.*;

import static java.lang.System.out;
//import static org.eurocarbdb.util.StringUtils.join;

import static org.eurocarbdb.util.graph.Graphs.isConnected;
import static org.eurocarbdb.util.graph.Graphs.getConnectedSubgraphs;


public class GraphsTest
{

    @Test
    (
        groups={"util.graphs"}   
    )
    public void graphConnectivity()
    {
        out.println( "determining if a graph is connected or not" );
        
        Graph<String,String> g = new Graph<String,String>();
        
        //  0 vertices, 0 edges - unconnected
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 0;
        assert g.countEdges() == 0;
        assert ! isConnected(g);
        out.println(); 
        
        //  1 vertex, 0 edges - connected
        //  graph API defines isConnected to be true when there is a single vertex.
        g.addVertex( "aaa" );
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 1;
        assert g.countEdges() == 0;
        assert isConnected(g);
        out.println();         
        
        //  2 vertices, 0 edges - unconnected
        g.addVertex( "bbb" );
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 2;
        assert g.countEdges() == 0;
        assert ! isConnected(g);
        out.println(); 
        
        //  2 vertices, 1 edge - connected
        g.addEdge( "aaa", "bbb" );
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 2;
        assert g.countEdges() == 1;
        assert isConnected(g);
        out.println(); 
        
        //  3 vertices, 1 edge - unconnected
        g.addVertex( "ccc" );
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 3;
        assert g.countEdges() == 1;
        assert ! isConnected(g);
        out.println(); 

        //  4 vertices, 2 edges - unconnected
        g.addVertex( "ddd" );        
        g.addEdge( "ccc", "ddd" );        
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 4;
        assert g.countEdges() == 2;
        assert ! isConnected(g);
        out.println(); 

        //  4 vertices, 4 edges - unconnected 
        g.addEdge( "ccc", "ddd" );        
        g.addEdge( "ccc", "ddd" );        
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 4;
        assert g.countEdges() == 4;
        assert ! isConnected(g);
        out.println(); 

        //  4 vertices, 5 edges - connected
        g.addEdge( "aaa", "ddd" );        
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 4;
        assert g.countEdges() == 5;
        assert isConnected(g);
        
    }
    
    
    @Test
    (
        groups={"util.graphs"}   
    )
    public void graphConnectivitySubgraphs()
    {
        out.println( "deriving connected subgraphs for a graph" );
        
        Graph<String,String> g = new Graph<String,String>();
        List<Graph<String,String>> subgraphs;
        
        //  0 vertices, 0 edges - unconnected
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 0;
        assert g.countEdges() == 0;
        assert ! isConnected(g);
        
        subgraphs = getConnectedSubgraphs( g );
        reportSubgraphs( subgraphs );
        assert subgraphs.size() == 0;
        
        //  1 vertex, 0 edges - connected
        //  graph API defines isConnected to be true when there is a single vertex.
        g.addVertex( "aaa" );
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 1;
        assert g.countEdges() == 0;
        assert isConnected(g);
        
        subgraphs = getConnectedSubgraphs( g );
        reportSubgraphs( subgraphs );
        assert subgraphs.size() == 1;

        //  2 vertices, 0 edges - unconnected
        g.addVertex( "bbb" );
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 2;
        assert g.countEdges() == 0;
        assert ! isConnected(g);
        out.println(); 
        
        subgraphs = getConnectedSubgraphs( g );
        reportSubgraphs( subgraphs );
        assert subgraphs.size() == 2;

        //  2 vertices, 1 edge - connected
        g.addEdge( "aaa", "bbb" );
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 2;
        assert g.countEdges() == 1;
        assert isConnected(g);
        out.println(); 
        
        subgraphs = getConnectedSubgraphs( g );
        reportSubgraphs( subgraphs );
        assert subgraphs.size() == 1;

        //  3 vertices, 1 edge - unconnected
        g.addVertex( "ccc" );
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 3;
        assert g.countEdges() == 1;
        assert ! isConnected(g);
        out.println(); 

        subgraphs = getConnectedSubgraphs( g );
        reportSubgraphs( subgraphs );
        assert subgraphs.size() == 2;

        //  4 vertices, 1 edges - unconnected
        g.addVertex( "ddd" );        
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 4;
        assert g.countEdges() == 1;
        assert ! isConnected(g);
        out.println(); 

        subgraphs = getConnectedSubgraphs( g );
        reportSubgraphs( subgraphs );
        assert subgraphs.size() == 3;

        //  4 vertices, 2 edges - unconnected
        g.addEdge( "ccc", "ddd" );        
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 4;
        assert g.countEdges() == 2;
        assert ! isConnected(g);
        out.println(); 

        subgraphs = getConnectedSubgraphs( g );
        reportSubgraphs( subgraphs );
        assert subgraphs.size() == 2;

        //  4 vertices, 4 edges - unconnected 
        g.addEdge( "ccc", "ddd" );        
        g.addEdge( "ccc", "ddd" );        
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 4;
        assert g.countEdges() == 4;
        assert ! isConnected(g);
        out.println(); 

        subgraphs = getConnectedSubgraphs( g );
        reportSubgraphs( subgraphs );
        assert subgraphs.size() == 2;

        //  4 vertices, 5 edges - connected
        g.addEdge( "aaa", "ddd" );        
        
        out.println( "graph is now:" ); 
        out.println( g.toString() ); 
        out.println( "isConnected = " + isConnected(g) );
        assert g.countVertices() == 4;
        assert g.countEdges() == 5;
        assert isConnected(g);
        
        subgraphs = getConnectedSubgraphs( g );
        reportSubgraphs( subgraphs );
        assert subgraphs.size() == 1;

    }
    
    
    private <E,V> void reportSubgraphs( List<Graph<E,V>> graphlist )
    {
        out.println( "List of subgraphs:" );   
        
        if ( graphlist.size() == 0 )
        {
            out.println( "(no subgraphs)" );   
            out.println();  
            return;
        }
        
        int i = 1;
        for ( Graph<E,V> g : graphlist )
        {
            out.println( "subgraph " + i + ":" );   
            out.println( g );   
            out.println();  
            i++;
        }
    }
    
} // end class
