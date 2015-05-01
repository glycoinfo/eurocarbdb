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
*   Last commit: $Rev: 1561 $ by $Author: glycoslave $ on $Date:: 2009-07-21 #$  
*/

package org.eurocarbdb.sugar;

//  stblib imports
import java.util.Iterator;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.util.graph.Graph;
import org.eurocarbdb.util.graph.Edge;
import org.eurocarbdb.util.graph.Vertex;
import org.eurocarbdb.sugar.SequenceFormat;

//  static imports
import static org.eurocarbdb.util.graph.Graphs.unmodifiableGraph;


/*  class Sugar  *//*************************************************
*<p> 
*   This class provides numerous methods and other facilities for creating
*   and manipulating oligosaccharides. Sugars (oligosaccharides) are 
*   essentially modelled in this class as a directed {@link Graph graph} of 
*   {@link Residue}s and {@link Linkage}s.
*</p>
*<p>
*   Usage normally involves creating a Sugar object with an initial
*   (root) residue, which is then elaborated by the addition of 
*   further residues with the <code>addResidue</code> method.
*</p>
*
*   @see Residue
*   @see Graph
*   @author mjh 
*/
public class Sugar extends BasicMolecule 
implements Cloneable, PotentiallyIndefinite, Iterable<Residue>
{
    /** Logging instance. */
    protected static final Logger log = Logger.getLogger( Sugar.class );

    //~~~~~~~~~~~~~~~~~~~~  OBJECT FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Internal graph of residues in this sugar.  */
    protected Graph<Linkage,Residue> graph;
    
    /** Sequence of this sugar. */
    protected SugarSequence sequence;
    
    /** The root residue */
    protected Residue rootResidue;

    /** The molecule attached to the reducing terminus of this sugar, if any. */
    private Molecule aglyconResidue = null;
    
    /** The linkage by which {@link #aglyconResidue} is attached to this sugar. */
    private Linkage aglyconLinkage = null;
    
    
    //~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /**   
    *   Constructs a 'null' (empty) Sugar. 
    */
    public Sugar() 
    {
        //  assume most sugars will be 8 residues or less
        graph = new Graph<Linkage,Residue>( 8 );
    }

    
    /** 
    *   Constructs an empty Sugar pre-allocated for given, expected
    *   number of residues 
    */
    public Sugar( int initial_size )
    {
        graph = new Graph<Linkage,Residue>( initial_size );           
    }
    
    
    /*  Constructor  *//*********************************************
    *   
    *   Constructs a Sugar with a single (root) residue.
    */
    public Sugar( Residue root )
    {
        this();
        addRootResidue( root );
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~  METHODS  ~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /*  addLinkage  *//**********************************************
    *<p>
    *   Adds a linkage between two residues that are already present
    *   in this sugar. 
    *</p>
    *<p>
    *   As the default names of the residue variables in the argument 
    *   list suggests - the order of residues in the argument list implies 
    *   linkage directionality.  
    *</p>
    *   @param child 
    *   @param link
    *   @param parent
    */
    public void addLinkage( Residue parent, Linkage linkage, Residue child )
    {
        //  no args can be null
        if ( parent == null ) 
            throw new IllegalArgumentException(
                    "Argument 'parent' cannot be null");
        if ( child == null ) 
            throw new IllegalArgumentException(
                    "Argument 'child' cannot be null");
        if ( linkage == null ) 
            throw new IllegalArgumentException(
                    "Argument 'linkage' cannot be null");
        
        if ( graph.isEmpty() )
            throw new IllegalArgumentException(
                "Sugar does not contain any residues -- try adding "
                + "a residue or two before trying to add linkages" );
            
        //  check parent is IN sugar, and that child ISN'T. 
        //  these are not expensive checks, since contains calls 
        //  are hashtable lookups.
        if ( ! graph.contains( parent ) )
            throw new IllegalArgumentException(
                "Parent residue does not exist in this sugar");
        
        if ( ! graph.contains( child ) )
            throw new IllegalArgumentException(
                "Child residue does not exist in this sugar");
               
        graph.addEdge(  
            graph.getVertex( parent ), 
            graph.getVertex( child ), 
            linkage 
        );
        
        return;
    }
    
    
    /*  addResidue  *//**********************************************
    *   
    *   Adds a new, unlinked residue to the current sugar. If this 
    *   sugar has no residues then the residue added will become the
    *   root residue.
    *
    *   @see #addLinkage
    *   @see #addRootResidue
    */
    public void addResidue( Residue r )
    {
        if ( r == null )
            throw new IllegalArgumentException(
                "residue argument cannot be null");
        
        if ( graph.isEmpty() )
        {
            addRootResidue( r );
            return;
        }
            
        if ( log.isDebugEnabled() )
            log.debug( "adding new residue " + r ); 

        graph.addVertex( r );
        
        return;
    }
    
        
    /*  addResidue  *//**********************************************
    *   
    *   Core method for elaborating/extending sugars, which adds both a 
    *   new (child) residue and linkage to an existing (parent) residue
    *   in the sugar. This method is equivalent to calling:
    *   {@link #addResidue(Residue)} and {@link #addLinkage} in succession.
    *
    *   @throws IllegalArgumentException if any of the arguments are null
    */
    public void addResidue( Residue parent, Linkage linkage, Residue child )
    {
        //  make sure none of the arguments are null. nulls are bad.
        if ( parent == null )
            throw new IllegalArgumentException(
                "Invalid argument: parent residue cannot be null");
                            
        if ( graph.isEmpty() || ! graph.contains( parent ) )
            addRootResidue( parent );
        
        if ( parent != child )        
            addResidue( child );
        
        addLinkage( parent, linkage, child );        
        
        return;
    }
    
     
    /*  addRootResidue  *//***************************************
    *
    *   Adds the given residue to the root of this sugar. Only callable 
    *   if no other residues have yet been added to this sugar.
    *   
    *   @param root
    *   The residue that will become the root residue of this sugar.
    */
    public void addRootResidue( Residue root )
    {
        if ( graph.size() > 0 )
            throw new UnsupportedOperationException(
                    "Sugar already has " 
                    + this.countResidues() 
                    + " residue(s), use method "
                    + "addRootResidue( Linkage, Residue ) to "
                    + "add a new root residue to an existing structure"
                    );
        
        if ( log.isDebugEnabled() )
            log.debug( "adding initial root residue " + root );
        
        graph.addVertex( root );
        rootResidue = root;
        return;
    }
    
   
    /*  addRootResidue  *//******************************************
    *   
    *   Adds the given residue to the root of this sugar, displacing the 
    *   this root residue with the passed residue.
    *   
    *   @param link      
    *   The desired linkage to create between the old root residue and 
    *   the newly introduced one. Cannot be null.
    *   @param new_root
    *   The residue that will become the new root residue of this sugar.
    */
    public void addRootResidue( Linkage linkage, Residue new_root )
    {
        assert( ! this.contains( new_root ) );
        
        if ( linkage == null )
                throw new IllegalArgumentException(
                        "Invalid argument: linkage cannot be null");
        
        log.debug( "adding root residue " + new_root );
        
        graph.addVertex( new_root );
        graph.addEdge(  graph.getVertex( rootResidue ), 
                        graph.lastVertex(), 
                        linkage 
                        );

        return;
    }
    

    @Override
    @SuppressWarnings("unchecked")  // <- I hate java... 
    public Object clone()
    {
        Sugar copy = null;
        try
        {
            copy = (Sugar) super.clone();
            copy.graph = (Graph<Linkage,Residue>) this.graph.clone();
        }
        catch ( CloneNotSupportedException e ) {  e.printStackTrace();  } 
        
        return copy;
    }
    
    
    /*  contains  *//************************************************
    *   
    *   Returns true if the given residue is found in this sugar; 
    *   false otherwise. Note that this method tests for the existance
    *   of a *specific* residue instance, not for a residue *type*.
    *   
    *   This method cannot be used, for instance, to test for the 
    *   presence of mannose, only for a <em>specific</em> mannose residue.
    */
    public boolean contains( Residue residue )
    {   
        return graph.contains( residue );
    }
    
    
    /*  countResidues  *//*******************************************
    *   
    *   Returns the number of residues in this Sugar.
    */
    public int countResidues()
    {
        return graph.countVertices();
    }


    /*  getAglycon  *//**********************************************
    *   
    *   Returns the molecule attached to the reducing terminus of this 
    *   sugar, if any.
    */
    public Molecule getAglycon()
    {
        return this.aglyconResidue;
    }
    

    /*  getAglyconLinkage  *//***************************************
    *   
    *   Returns the linkage of the molecule attached to the reducing 
    *   terminus of this sugar, if applicable.
    */
    public Linkage getAglyconLinkage()
    {
        return this.aglyconLinkage;
    }
    
    
    /*  getComposition  *//******************************************
    *
    */
    public Composition<Residue> getComposition()
    {
        return null;
    }
    
    
    /*  getGraph  *//************************************************
    *
    *   Returns an unmodifiable graph of the linkages and residues
    *   comprising this sugar.
    *
    *   @see Graphs#unmodifiableGraph
    */
    public Graph<Linkage,Residue> getGraph()
    {
        return unmodifiableGraph( graph );
    }
    
        
    /*  getRootResidue  *//******************************************
    *
    *   Returns the reducing terminal (root) residue of this sugar.
    *   Returns null if this Sugar is empty (ie: has no residues).
    */
    public Residue getRootResidue()
    {
        return rootResidue; 
    }
    
    
    /*  getSequence  *//*********************************************
    *   
    *   Implicit access to the object encapsulating the
    *   sequence of this sugar.
    */
    public SugarSequence getSequence()
    {
        return this.sequence;
    }

    
    public boolean isDefinite()
    {
        Residue r;
        for ( Vertex<Linkage,Residue> v : graph )        
        {
            r = v.getValue();
            if ( r instanceof PotentiallyIndefinite )
                if ( ! ((PotentiallyIndefinite) r).isDefinite() )
                    return false;
        }
        
        Linkage l;
        for ( Edge<Linkage,Residue> e : graph.getAllEdges() )        
        {
            l = e.getValue();
            if ( l instanceof PotentiallyIndefinite )
                if ( ! ((PotentiallyIndefinite) l).isDefinite() )
                    return false;
        }
        
        return true;
    }
    

    /*  iterator  *//************************************************
    *
    *   Returns an iterator over all residues in this sugar in 
    *   no particular order.
    *   
    *   @see java.lang.Iterable#iterator()
    */
    public Iterator<Residue> iterator()
    {
        return graph.getAllVertexValues().iterator();
    }

    
    /*  lastResidue  *//*********************************************
    *
    *   Returns the most recently added residue. 
    */
    public Residue lastResidue()
    {
        return graph.lastVertex().getValue();
    }
    

    /*  setAglycon  *//**********************************************
    *   
    *   Sets the molecule/linkage attached to the reducing terminus of 
    *   this sugar.
    */
    public void setAglycon( Linkage l, Molecule m ) 
    {
        assert l != null;
        assert m != null;
        this.aglyconLinkage = l;
        this.aglyconResidue = m;
    }
    

    /*  toString  *//************************************************
    *  
    *   Returns the string representation of this sugar's sequence in 
    *   the default {@link SequenceFormat}.
    */
    public String toString()
    {
        return SugarSequence.DEFAULT_SEQUENCE_FORMAT.getSequence( this );
    }

    
    /*  toString  *//************************************************
    *  
    *   Returns the string representation of this sugar's sequence in 
    *   the given {@link SequenceFormat}.
    */
    public String toString( SequenceFormat format )
    {
        assert format != null;
        return format.getSequence( this );
    }
    
    
} // end class 
