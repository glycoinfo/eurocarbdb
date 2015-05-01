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
*   Last commit: $Rev: 1357 $ by $Author: glycoslave $ on $Date:: 2009-06-30 #$  
*/

package org.eurocarbdb.dataaccess.core.seq;

//  stdlib imports
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collections;

import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.util.graph.Graph;
import org.eurocarbdb.util.graph.Edge;
import org.eurocarbdb.util.graph.Vertex;
import org.eurocarbdb.util.graph.DepthFirstGraphVisitor;

import org.eurocarbdb.sugar.Linkage;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.Substituent;
// import org.eurocarbdb.sugar.Molecule;
import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.sugar.GlycosidicLinkage;

import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import org.eurocarbdb.dataaccess.core.GlycanSequence;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
*   A GlycanResidue represents a single {@link Residue} of a single 
*   {@link GlycanSequence}, normally in the form of GlycanResidue
*   sub-classes {@link GlycanMonosaccharide} and {@link GlycanSubstituent}.
*
*   @author mjh
*/
public class GlycanResidue extends BasicEurocarbObject 
implements Residue, Serializable
{
    /** Logging handle. */
    protected static final Logger log = Logger.getLogger( GlycanResidue.class );
    
    /** Unique id */
    private int glycanResidueId;
    
    /** The GlycanSequence to which this GlycanResidue belongs */
    private GlycanSequence glycanSequence;
    
    private String residueName; 
    
    /** The identity of the residue this GlycanResidue represents */
    private Residue residue;
    
    /** This is the linkage of this residue to its parent, if any */
    private Linkage linkage;

    /** The parent of this GlycanResidue, if any */    
    private GlycanResidue parent;
    
    /** The children of this GlycanResidue, if any */    
    private Set<GlycanResidue> children = new HashSet<GlycanResidue>(0);
    
    /** The 'left' value of this GlycanResidue in a nested set traversal 
    *   of the {@link Graph} of GlycanResidues to which this GlycanResidue 
    *   belongs. */
    private int leftIndex;
    
    /** The 'right' value of this GlycanResidue in a nested set traversal 
    *   of the {@link Graph} of GlycanResidues to which this GlycanResidue 
    *   belongs. */
    private int rightIndex;

    
    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /* needed for hibernate */
    GlycanResidue() 
    {
    }
    
    
    public GlycanResidue( Residue r )
    {
        this.glycanSequence = null;   
        this.residue = r;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /**
    *   Translates the {@link SugarSequence} encapsulted in the given 
    *   {@link GlycanSequence} into a {@link Set} of {@link GlycanResidue}s 
    *   representing the tree/graph structure of that sequence.
    */
    public static Set<GlycanResidue> calculateResidueGraph( final GlycanSequence gs )
    {
        log.debug("===== calculating substructure info =====");
            
        try
        {
            NestedSetVisitor visitor = new NestedSetVisitor( gs );   
        
            visitor.visit();
        
            return visitor.getResidues();
        }
        catch ( Exception ex )
        {
            log.warn(
                "Caught exception while getting residue graph for GlycanSequence id="
                + gs.getGlycanSequenceId() 
                , ex 
            );   
            return Collections.emptySet();
        }
        
    }
    

    /** 
    *   Traverses a {@link Sugar} {@link Graph} to (1) generate nested
    *   set left/right values for each {@link GlycanResidue} in the Graph, and
    *   (2) updates and checks each GlycanResidue so that parent/children 
    *   relationships are correct and the {@link GlycanSequence} property
    *   is set.
    */
    public static class NestedSetVisitor
    extends DepthFirstGraphVisitor<Linkage,Residue>     
    {
        int nestedSetCounter = 1;
        
        // private GlycanResidue current = null;
        
        private Map<Residue,GlycanResidue> residues;
        
        // private Set<GlycanResidue> residues;

        private final GlycanSequence gs;
        
        private final Graph<Linkage,Residue> tree;

        
        public NestedSetVisitor( GlycanSequence gs )
        {
            SugarSequence seq = gs.getSugarSequence();
            
            this.tree = seq.getSugar().getGraph();
            this.residues = new HashMap<Residue,GlycanResidue>( tree.size() );
            // this.visited = new HashSet<Residue>( tree.size() );
            this.gs = gs;
        }
        

        /** Graph traversal start method */
        public void visit()
        {
            visit( tree );    
        }
        
        
        public void accept( Monosaccharide m )
        {
            if ( ! residues.containsKey( m ) )
                residues.put( m, new GlycanMonosaccharide( m ) );
        }
        

        public void accept( Substituent s )
        {
            if ( ! residues.containsKey( s ) )
                residues.put( s, new GlycanSubstituent( s ) );
        }

        
        public void accept( Vertex<Linkage,Residue> v )
        {
            Residue r = v.getValue();
            
            //  this populates the residues hash
            visit( r ); 
            
            GlycanResidue gr = residues.get( r );
            assert gr != null;
            
            if ( tree.getRootVertex() == v )
                gr.setParent( null );
            
            gr.setGlycanSequence( gs );

            //  nested set 'left' value
            gr.leftIndex = nestedSetCounter++;
            
            super.accept( v );
            
            //  nested set 'right' value
            gr.rightIndex = nestedSetCounter++;
        }
        
        
        public void accept( Edge<Linkage,Residue> e )
        {
            GlycosidicLinkage ln = (GlycosidicLinkage) e.getValue();
            
            assert e.getParent() != e.getChild();
            
            Residue parent_res = e.getParent().getValue();
            GlycanResidue parent = residues.get( parent_res );
            assert parent != null;
            
            Residue child_res = e.getChild().getValue();
            visit( child_res );
            GlycanResidue child = residues.get( child_res );
            assert child != null;
            assert child != parent;
            
            //  structure must be a tree, not a graph
            //  for this algorithm to apply
            if ( child.getParent() != null && child.getParent() != parent )
                vertexHasMultipleParents( e.getChild() );
            
            // residues.add( child );
            child.setLinkage( ln );
            child.setParent( parent );
            parent.getChildren().add( child );
            
            super.accept( e );
        }
        
        
        public void accept( Graph<Linkage,Residue> g )
        {
            // //  make sure root vertex 
            // //  this prob isn't needed but do it just in case
            // if ( g.size() > 0 )
            // {
            //     GlycanResidue root = (GlycanResidue) g.getRootVertex().getValue();
                
            //     root.setParent( null );
            //     residues.add( root );
            // }       
            
            super.accept( g );

            if ( residues.size() != tree.size() )
            {
                throw new RuntimeException(
                    "Number of residues calculated differs from the number of "
                    + "residues in the sugar graph from which they have been derived: "
                    + tree.size() 
                    + " residue(s) in sugar, "
                    + residues.size() 
                    + " residue(s) visited. Aborting..."
                );
            }
        }
        
            
        private void vertexHasMultipleParents( Vertex<Linkage,Residue> v )
        {
            List<Edge<Linkage,Residue>> inc_edges = v.getIncomingEdges();
            
            //  count_parents > 1
            //  this means structure is a graph, not a tree.
            //  todo: non-trees are not handled (yet)
            String message = 
                "structure is a graph, not a tree: node '"
                + v
                + "' has "
                + inc_edges.size() 
                + " parents, incoming edges are: "
                + inc_edges
                + "; skipping..."
            ;
            
            log.warn( message );
            
            if ( log.isDebugEnabled() )
                log.debug( "graph:\n" + tree.toString() );
            
            throw new UnsupportedOperationException( message );
        }
        
        
        public Set<GlycanResidue> getResidues() 
        {  
            Set<GlycanResidue> set = new HashSet<GlycanResidue>( residues.values() );
            assert set.size() == residues.size();
            return set;  
        }
        
    } // end inner class

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    public int getGlycanResidueId() 
    {
        return this.glycanResidueId;
    }
    
    
    protected void setGlycanResidueId(int glycanResidueId) 
    {
        this.glycanResidueId = glycanResidueId;
    }
    
    
    public GlycanSequence getGlycanSequence() 
    {
        return this.glycanSequence;
    }
    
    
    private void setGlycanSequence( GlycanSequence glycanSequence ) 
    {
        this.glycanSequence = glycanSequence;
    }
    
    
    public Residue getResidue() 
    {
        return this.residue;
    }
    
    
    public void setResidue(Residue residue) 
    {
        this.residue = residue;
    }
    
    
    public String getResidueName()
    {
        return residueName;
    }


    public void setResidueName( String name )
    {
        this.residueName = name;       
    }


    public Linkage getLinkage()
    {
        return linkage;
    }
    
    public void setLinkage( GlycosidicLinkage link )
    {
        this.linkage = link;   
    }
    
    
    public GlycanResidue getParent() 
    {
        return this.parent;
    }
    
    
    void setParent( GlycanResidue parent ) 
    {
        assert parent != this;
        this.parent = parent;
    }
    
    
    public Set<GlycanResidue> getChildren() 
    {
        return this.children;
    }
    
    
    void setChildren(Set<GlycanResidue> children) 
    {
        this.children = children;
    }

    
    /*  implementation (delegation) of Residue interface  */
    
    public double getMass()
    {
        return residue.getMass();
    }

    
    public double getAvgMass()
    {
        return residue.getAvgMass();
    }
    
    
    public String getName()
    {
        return residue.getName();
    }

    
    public String getFullName()
    {
        return residue.getFullName();
    }
    
    
    // /*  implementation of Attachable interface  */
    
    // public void attach( Molecule m, int position )
    // {
    //     log.warn("todo");
    // }
    
    
    // public void unattach( int position )
    // {
    //     log.warn("todo");
    // }
    
    
    // public Set<Integer> getAttachablePositions()
    // {
    //     log.warn("todo");
    //     return null;
    // }
    
    
    // /** 
    // *   Returns the attached object at the given position, or null 
    // *   if that position is free. 
    // */
    // public Molecule getAttached( int position )
    // {
    //     log.warn("todo");
    //     return null;
    // }
    
    
    /*  overridden Object methods  */
    
    public boolean equals( Object other )
    {
        if ( this == other )
            return true;
        
        if ( other == null || !(other instanceof GlycanResidue) )
            return false;
        
        GlycanResidue gr = (GlycanResidue) other;
        
        if (   this.glycanSequence == gr.glycanSequence
            && this.residue == gr.residue 
            && this.linkage == gr.linkage ) 
            return true;
            
        
        if ( this.glycanSequence != null && this.glycanSequence.equals( gr.glycanSequence ) 
            && this.residue != null && this.residue.equals( gr.residue )
            && this.linkage != null && this.linkage.equals( gr.linkage )  )
            return true;
            
        return false;
    }
    
    
    public int hashCode()
    {
        int hash = super.hashCode();
        
        if ( this.glycanSequence != null )
            hash *= this.glycanSequence.hashCode();
        
        if ( this.residue != null )
            hash *= this.residue.hashCode();
        
        if ( this.linkage != null )
            hash *= this.linkage.hashCode();
            
        return hash;   
    }
    

    public String toString()
    {
        return "[" 
            + getClass().getSimpleName().substring( 0, 13 ) 
            + "="
//            + getGlycanResidueId()
//            + "; Res="
            + ( residue != null 
                ? residue.getName()
                : "null" )
//            + "; GS="
//            + ( glycanSequence != null 
//                ? glycanSequence.getGlycanSequenceId() 
//                : "null" )
            + "]"
        ;    
    }

    
    private int getLeftIndex() 
    {
        return this.leftIndex;
    }
    
    
    private int getRightIndex() 
    {
        return this.rightIndex;
    }
    

    
    
    /*  class Generator  *//*****************************************
    *
    *   intended for one-time use onlt to populate new seq.* schema tables.
    *
    *   <em> will probably be removed or moved in near future</em>
    */
    public static final class Generator
    {
        int count_todo = 0;
        int count_success = 0;
        int count_failed = 0;
        int count_remaining = 0;
        
        public static void main( String[] args )
        {
            Generator g = new Generator();
            
            g.generate();
        }
        
        
        public void generate()
        {
            List<Integer> seqs = getSeqsWithNoSubstructInfo();
            count_todo = seqs.size();
            
            for ( int id : seqs )
            {
                getEntityManager().beginUnitOfWork();
                
                GlycanSequence gs = getEntityManager().lookup( GlycanSequence.class, id );
                log.debug("======================= " + gs + " =======================");
                addSubstructInfo( gs );
                
                getEntityManager().endUnitOfWork();

                log.debug("count successful: " + count_success + ", failed: " + count_failed );
            }
            
            seqs = getSeqsWithNoSubstructInfo();
            count_remaining = seqs.size();
            
            System.err.println( 
                "count sequences processed: " 
                + count_todo 
                + ", successful: "
                + count_success
                + ", failed: "
                + count_failed
            );
            System.err.println( "count sequences with no substruct info: " + count_remaining );
            
            //log.debug("fixed: " + count_success + ", skipped: " + count_failed );
        }
        
               
        /** 
        *   Returns {@link List} of {@link GlycanSequence} IDs that don't 
        *   have any substructure info generated.
        */
        List<Integer> getSeqsWithNoSubstructInfo()
        {
            getEntityManager().beginUnitOfWork();
            
            Object result = getEntityManager()
                .getQuery( GlycanSequence.class.getName() 
                    + ".by_missing_substructure_info")
                .list();
                            
            getEntityManager().endUnitOfWork();
            
            if ( result == null )
                return Collections.emptyList();
            
            return (List<Integer>) result;
        }
        
        
        void addSubstructInfo( GlycanSequence gs )
        {
            assert gs != null;
            log.debug( "processing seq " + gs );
            
            try 
            {
                getEntityManager().update( gs );
                gs.calculateSubstructureInfo();
                count_success++;
            }
            catch ( Exception ex )
            {
                log.warn("Caught exception, skipping sequence...", ex );
                count_failed++;
                return;
            }
                
            for ( GlycanResidue gr : gs.getGlycanResidues() )
            {
                getEntityManager().store( gr );
            }
        }
        
    } // end class Generator
    
} // end class GlycanResidue



