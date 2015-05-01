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

import java.util.List;
import java.util.ArrayList;

/*  class class Tree  *//********************************************
*
*   Implements a "tree" (directed acyclic graph) data structure.
*/
public class Tree<E,V> extends Graph<E,V>
{

    //~~~~~~~~~~~~~~~~~~~~~~~~  FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~~~~~//



    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//

    /** Generic constructor. */
    public Tree() {}

    //~~~~~~~~~~~~~~~~~~~~~~~~  METHODS  ~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    @Override
    public boolean addVertex( Vertex<E,V> vertex )
    {
        boolean true_if_added = super.addVertex( vertex );
        return true_if_added;
    }


    /*  getLeaves  *//***********************************************
    *   
    *   Returns a list of leaf vertices. Leaf vertices are those vertices 
    *   that have 1 or no attached vertices. Returns null if the graph 
    *   has no vertices.
    */
    public List<Vertex<E,V>> getLeaves() 
    {
        if ( this.isEmpty() ) return null;
        
        List<Vertex<E,V>> leaves = new ArrayList<Vertex<E,V>>( vertices.size() );
        
        for ( Vertex<E,V> v : vertices )
            if ( v.countAttachedVertices() <= 1 ) 
                leaves.add( v );
        
        return leaves;
    }
    
    
    /*  getParentsOf  *//********************************************
    *   
    *   Returns an iterator over the list of all parents of the given
    *   vertex, in order from the given vertex to the root vertex.
    */
    /*
    public GraphIterator<Vertex<E,V>> getParentsOf( final Vertex<E,V> v )
    {
        return new GraphIterator<Vertex<E,V>>() {
            private Vertex<E,V> cursor = v;
            public final boolean hasNext() { return cursor.getParent() != null; }
            public final Vertex<E,V> next() { return ( cursor = cursor.getParent() ); }
            public void remove() { Graph.this.remove( cursor ); }
        };
    }
    */
    

 
//    /*  getSubtreeFrom  *//******************************************
//    *   
//    *   not working atm.
//    */
//    public Graph<E,V> getSubtreeFrom( Vertex<E,V> starting_vertex )
//    {
//        if ( ! this.contains( starting_vertex ) )
//            throw new RuntimeException(
//                    "Vertex '" + starting_vertex + "' not found in tree");
//
//      
//        List<Vertex<E,V>> subtree = this.getAllChildrenOf( starting_vertex );
//        subtree.add( 0, starting_vertex );
//
////        List<Vertex<E,V>> newtree = new ArrayList<Vertex<E,V>>( subtree.size() );
////        for ( Vertex<E,V> v : subtree ) newtree.add( v.clone() ); 
////        newtree.get(0).setParent( null );
//
////        List<Vertex<E,V>> newtree = new ArrayList<Vertex<E,V>>( subtree.size() );
////        
////        for ( Vertex<E,V> v : this.getChildrenOf( starting_vertex ) )
////        {
////            Vertex<E,V> vc = v.clone();
////            
////        }
//        
//        return new Graph<E,V>( subtree );
//    }
//    


//    /*  lastBranchVertex  *//****************************************
//    *   
//    *   Convenience method that returns the vertex nearest to the given
//    *   vertex (on the root side) that has more than one child. 
//    */
//    public Vertex<E,V> lastBranchVertex()
//    {
//        if ( this.isEmpty() ) return null;
//        for ( Vertex<E,V> v : this.getParentsOf( lastVertex() ) )
//            if ( v.hasAttachedVertices() && v.getAttachedVertices().size() > 1 )
//                return v;
//        
//        return null;
//    }


//    /*  prune  *//***************************************************
//    *   
//    *   Prunes (removes) from the current tree the subtree of vertices 
//    *   starting at the given vertex.
//    */
//    public void prune( Vertex<E,V> vertex )
//    {
////        List<Vertex<E,V>> children = this.getAllChildrenOf( vertex );
////        for ( Vertex<E,V> v : children ) v.clear();
////        vertex.clear();
////        
////        vertices.removeAll( children );
//    }


    

}