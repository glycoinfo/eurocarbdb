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

package org.eurocarbdb.sugar.seq;

//  stdlib imports
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Collections;
import java.util.ListIterator;
import java.io.StringReader;

//  3rd party imports
import org.apache.log4j.Logger;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.ArrayListMultimap;

//  eurocarb imports
import org.eurocarbdb.util.Visitor;
import org.eurocarbdb.util.graph.Graph;
import org.eurocarbdb.util.graph.Vertex;
import org.eurocarbdb.util.graph.Edge;
import org.eurocarbdb.util.graph.Path;
// import org.eurocarbdb.util.graph.DepthFirstGraphVisitor;

import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.SugarRepeat;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Linkage;
import org.eurocarbdb.sugar.Anomer;
import org.eurocarbdb.sugar.Basetype;
import org.eurocarbdb.sugar.Substituent;
import org.eurocarbdb.sugar.Substituents;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.sugar.SequenceFormat;
import org.eurocarbdb.sugar.RingConformation;
import org.eurocarbdb.sugar.GlycosidicLinkage;
import org.eurocarbdb.sugar.SequenceFormatException;
import org.eurocarbdb.sugar.impl.SimpleMonosaccharide;

import org.eurocarbdb.sugar.seq.grammar.IupacLexer;
import org.eurocarbdb.sugar.seq.grammar.IupacParser;
import org.eurocarbdb.sugar.seq.grammar.ParserAdaptor;
import org.eurocarbdb.sugar.seq.grammar.IupacParserAdaptor;

//  static imports
import static java.util.Collections.sort;
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.util.graph.Graphs.getPaths;


/*  class IupacSequenceFormat  *//***********************************
* 
<style>
    tt { color: darkgreen; }
</style>
<p>
    Implementation of a parser/generator for the Eurocarb carbohydrate 
    sequence format. This format is largely based on the 
    <a href="http://www.chem.qmul.ac.uk/iupac/2carb/index.html">1996 IUPAC 
    recommendations for carbohydrate nomenclature</a>. As is the norm
    for carbohydrate sequences, structures are read from right-to-left;
    that is, the "root" monosaccharide is always the rightmost 
    monosaccharide. 
</p>
<p>
    Samples of this format are as follows:
    
    <ul>
        <li>The human 'A' blood group antigen:
            <br/>
            <tt>GalNAc(a1-3)[Fuc(a1-2)]Gal</tt>
        </li>
        <li>The human 'B' blood group antigen:
            <br/>
            <tt>Gal(a1-3)[Fuc(a1-2)]Gal</tt>
        </li>
        <li>The human 'O' blood group antigen:
            <br/>
            <tt>Fuc(a1-2)Gal</tt>
        </li>
        <li>
            The N-glycan Man3GlcNAc2 core:
            <br/> 
            <tt>Man(a1-6)[Man(1-3)]Man(b1-4)GlcNAc(b1-4)GlcNAc</tt>
        </li>
        <li>An example tri-antennary, tri-sialylated complex N-glycan:
            <br/>
            <tt>NeuAc(a2-6)Gal(b1-4)GlcNAc(b1-4)[NeuAc(a2-3)Gal(b1-4)GlcNAc(b1-2)]Man(a1-6)[NeuAc(a2-3)Gal(b1-4)GlcNAc(b1-2)Man(1-3)]Man(b1-4)GlcNAc(b1-4)GlcNAc</tt>
        </li>
    </ul>
</p>

<h2>Grammar</h2>

<h3>Residues</h3>
<p>
    Monosaccharide/residues must be between 3 and 6 characters long, 
    and may consist of any alphanumeric or underscore ([A-Za-z0-9_]), 
    upper or lower case, except for the first letter, which must be 
    alphabetic and upper-case. Examples include Man, Glc, GlcNAc, NeuAc,
    Neu2Ac. 
</p>
<p>
    This nomenclature is consistent with the overwhelming majority of 
    common naturally occuring monosaccharide names covered by IUPAC. 
    Names currently cannot contain hyphens ('-'), although these may 
    be added to accomodate the reduced forms of sugars, which are 
    commonly abbreviated to <tt>-ol</tt>, eg: <tt>GlcNAc-ol</tt>.
</p>

<h3>Linkages</h3>
<p>
    Linkages generally take the form 
    '<tt>([anomer][reducing-terminus]-[non-reducing-terminus])</tt>',
    eg: <tt>Gal(b1-4)Glc</tt> refers to an beta 1->4 linkage from the
    1 (reducing) position of the Gal (Galactose), to the 4
    (a non-reducing) position on the Glc (Glucose) to form the
    common disaccharide Lactose. Parentheses '()' are required around
    linkages and the internal delimiter '<tt>-</tt>' is required between reducing
    and non-reducing terminii.
</p>
<p>
    There are several other linkage descriptions that deviate from this,
    for example covalently bound inorganic phosphate ('<tt>P</tt>') 
    and sulfate ('<tt>S</tt>'). 
    These substituents omit the anomer and reducing terminus syntax, 
    as per the following examples: <tt>P(-4)Gal(b1-4)Glc</tt>. 
    <strong>(P & S linkage syntax is NOT yet supported by this parser)</strong> 
</p>

<h3>Branches</h3>
<p>
    Branches are indicated in the text sequence by the delimiters 
    '<tt>[]</tt>', which surround the text from the opening (rightmost) 
    linkage in the branch to the last (leftmost) residue in the branch.
</p>

<p>
    Created 06-Oct-2005.
</p>
*
*   @author mjh
*   @see IupacParser
*   @see IupacLexer
*   @see IupacParserAdaptor
*   @see SugarSequence
*   @see Sugar
*
********************************************************************/
public class IupacSequenceFormat implements SequenceFormat
{   
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Logging handle. */
    static final Logger log = Logger.getLogger( IupacSequenceFormat.class );
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    
    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    //  no constructors...
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** Returns "Iupac" */
    public String getName() {  return "Iupac";  }

    
    /** {@inheritDoc}  @see SequenceFormat#getMonosaccharide(String)  */
    public Monosaccharide getMonosaccharide( String seq ) 
    throws SequenceFormatException
    {
        return SimpleMonosaccharide.forName( seq );   
    }
    

    public Substituent getSubstituent( String seq ) throws SequenceFormatException
    {
        return Substituents.getSubstituent( seq );
    }

    
    /** {@inheritDoc}  @see SequenceFormat#getSugar(String)  */
    public Sugar getSugar( String seq ) throws SequenceFormatException
    {
        IupacLexer   lexer = new IupacLexer( new StringReader( seq ) );
        IupacParser parser = new IupacParser( lexer );

        ParserAdaptor.performParse( parser, seq );
        
        return parser.getSugar();
    }

    
    /** {@inheritDoc}  @see SequenceFormat#getSequence(Sugar)  */
    public String getSequence( Sugar s )
    {
        // throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
        
        // Graph<Linkage,Residue> graph = s.getGraph();
        Generator g = new Generator( s );
        String seq = g.generateSequence();
        
        return seq;
    }

    
    /** {@inheritDoc}  @see SequenceFormat#getSequence(Monosaccharide)  */
    public String getSequence( Monosaccharide m )
    {
        /*
        Basetype b = m.getBasetype();
        Anomer a = m.getAnomer();
        RingConformation rc = m.getRingConformation();
        
        throw new UnsupportedOperationException();
        */
        return m.getName();
    }
    
    
    /** {@inheritDoc}  @see SequenceFormat#getSequence(Residue)  */
    public String getSequence( Residue r )
    {
        return r.getName();
    }


    /** {@inheritDoc}  @see SequenceFormat#getSequence(Substituent)  */
    public String getSequence( Substituent s )
    {
        return s.getName();
    }

    
    
    
    public static class Generator extends Visitor
    {
        private LinkedList<Object> seqList;

        private StringBuilder sequence;
        
        private final Graph<Linkage,Residue> graph;
        
        private final Sugar sugar;
        
        private boolean hasRepeat = false;
        
        static Comparator<Path<Linkage,Residue>> pathComparator 
            = new Comparator<Path<Linkage,Residue>>() 
            {
                int i1, i2, i3;
                
                public final int compare( Path<Linkage,Residue> p1
                                        , Path<Linkage,Residue> p2 )      
                {
                    int i = ((Integer) p2.countVertexes()).compareTo( p1.countVertexes() );                           
                    if ( i != 0 )
                        return i;
                    
                    int size = p1.countEdges();
                    for ( i = 1; i <= size; i++ )
                    {
                        i1 = p1.getEdge(i).getValue().getParentTerminus();
                        i2 = p2.getEdge(i).getValue().getParentTerminus();
                        i3 = ((Integer) i2).compareTo( i1 );
                        // log.info("comparing:\n    p1: " + p1 + "\n    p2: " + p2 + "\n    i=" + i + ", i1=" + i1 + ", i2=" + i2 + ", i3=" + i3 + ", size=" + size);
                        
                        if ( i3 != 0 )
                            return i3;
                    }
                    
                    return 0;
                }
                
                public final boolean equals( Object x ) {  return false;  }
            };
        
            
        public Generator( Sugar s )
        {
            this.sugar = s;
            this.graph = s.getGraph();   
            this.seqList = new LinkedList<Object>();
        }
        
        
        public String generateSequence()
        {
            //  reset sequence list
            seqList.clear();
            
            //  get the list of all paths through the graph from 
            //  root to each leaf
            List<Path<Linkage,Residue>> paths = getPaths( graph );
            
            //  order these paths by length and linkage
            sort( paths, pathComparator );
            
            if ( log.isDebugEnabled() )
            {
                log.debug( 
                    "sorted paths (root -> leaf):\n    "
                    + join("\n    ", paths )
                );                   
            }
            
            //  highest sorted path becomes the 'main' branch, 
            //  the rest become branches
            // paths.get( 0 ).values( seqList );
            paths.get( 0 ).elements( seqList );
            
            //  record which graph elements have been incorporated            
            Set<Object> seen = new HashSet<Object>( graph.size() * 3 );
            seen.addAll( paths.get( 0 ).elements() );

            //  for each branch:
            for ( int i = 1; i < paths.size(); i++ )
            {
                Path<Linkage,Residue> path = paths.get(i); 
                ListIterator<Object>  iter = seqList.listIterator();
                
                //  for each element in the branch, root -> leaf
                for ( int j = 0; j < path.size(); j++ )
                {
                    if ( seen.contains( path.get(j) ) )
                    {
                        iter.next();
                        continue;
                    }
                    
                    //  we've reached the point at which the current branch
                    //  diverges from the main brain, so insert it.
                    iter.add( BRANCH_END );
                    for ( int k = j; k < path.size(); k++ )
                    {
                        // iter.add( path.getValue( k ) );
                        iter.add( path.get( k ) );
                        seen.add( path.get( k ) );
                    }
                    iter.add( BRANCH_START );
                }
            }
                
            // log.debug( "sequence is: " + seqList );
            Collections.reverse( seqList );
            
            this.sequence = new StringBuilder( seqList.size() * 5 );
            
            for ( Object x : seqList )
            {
                visit( x );
            }
                
            return sequence.toString();
        }
        
        static final String BRANCH_START  = "[";
        static final String BRANCH_END    = "]";
        static final String LINKAGE_START = "(";
        static final String LINKAGE_END   = ")";
        static final String LINKAGE_SEP   = "-";
        
        
        public void accept( Monosaccharide x )
        {
            sequence.append( new IupacSequenceFormat().getSequence( x ) );
        }
        

        public void accept( Substituent x )
        {
            sequence.append( new IupacSequenceFormat().getSequence( x ) );
        }
        
        
        public void accept( String x )
        {
            sequence.append( x );   
        }
        
        /*
        public void accept( GlycosidicLinkage x )
        {
            sequence.append( LINKAGE_START );
            sequence.append( x.getAnomer().toChar() );
            sequence.append( x.getChildTerminus() );
            sequence.append( LINKAGE_SEP );
            sequence.append( x.getParentTerminus() );
            sequence.append( LINKAGE_END );
        }
        
        
        public void accept( SugarRepeat s )
        {
            assert s.getGraph() == this.graph;
            hasRepeat = true;
            visit( graph );
        }
        
        
        public void accept( Sugar s )
        {
            assert s.getGraph() == this.graph;
            visit( graph );
        }
        
        
        public void accept( Graph<Linkage,Residue> g )
        {
            assert g == this.graph;
            unvisited = new HashSet<Vertex<Linkage,Residue>>( g.size(), 1.0 );
            
        }
        
        
        */
        public void accept( Vertex<Linkage,Residue> v )
        {
            visit( v.getValue() );                
        }
        
        
        public void accept( Edge<Linkage,Residue> e )
        {
            sequence.append( LINKAGE_START );
            
            GlycosidicLinkage x = (GlycosidicLinkage) e.getValue();
            Residue parent = e.getParent().getValue();
            Residue child = e.getChild().getValue();
            
            if ( ! (child instanceof Substituent) )
            {
                Anomer a = ((Monosaccharide) child).getAnomer(); 
                // if ( x.getChildAnomer() != Anomer.NONE )
                if ( a != Anomer.None )
                    sequence.append( a.toChar() );
                
                sequence.append( x.getChildTerminus() > 0 ? x.getChildTerminus() : '?' );
            }
            
            sequence.append( LINKAGE_SEP );
            
            if ( ! (parent instanceof Substituent) )
            {
                sequence.append( x.getParentTerminus() > 0 ? x.getChildTerminus() : '?' );
            }
            
            sequence.append( LINKAGE_END );
        }
        
        
    } // end class Generator
    
} // end class IupacSequenceFormat


