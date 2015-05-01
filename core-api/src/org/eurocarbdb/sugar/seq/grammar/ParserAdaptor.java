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
*   Last commit: $Rev: 1237 $ by $Author: glycoslave $ on $Date:: 2009-06-21 #$  
*/

package org.eurocarbdb.sugar.seq.grammar;

//  stdlib imports
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;

//  3rd party imports - antlr
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.SemanticException;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.TokenStreamRecognitionException;
import antlr.ParserSharedInputState;

//  3rd party imports - commons logging
import org.apache.log4j.Logger;

//  eurocarb imports - sugar stuff
import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.Anomer;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Linkage;
import org.eurocarbdb.sugar.Substituent;
import org.eurocarbdb.sugar.Substituents;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.SequenceFormat;
import org.eurocarbdb.sugar.SequenceFormatException;

//  eurocarb imports - graphs
import org.eurocarbdb.util.graph.Graph;
import org.eurocarbdb.util.graph.Vertex;
import org.eurocarbdb.util.graph.Edge;
import org.eurocarbdb.util.graph.GraphIterator;
import org.eurocarbdb.util.graph.DepthFirstGraphVisitor;

//  eurocarb imports - string manipulation
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.util.StringUtils.repeat;


/*  class ParserAdaptor  *//*****************************************
*<p>
*   This class is an adaptor/helper class for building an Abstract
*   Syntax Tree (AST) for a carbohydrate sequence using ANTLR. 
*   This class is intended to be used as a base class for generated 
*   sugar parser classes. For ANTLR grammars, this is accomplished by 
*   declaring your grammar to inherit from this class, eg:
*</p>
<pre>
    class GlycoctParser extends Parser("org.eurocarbdb.sugar.seq.grammar.ParserAdaptor");
    
    rest of ANTLR grammar here...
</pre>
*<p>
*   Usage of this class from within an ANTLR grammar is very straightforward 
*   -- just call the {@link #addResidue} & {@link #addLinkage} methods repeatedly (from
*   grammatical actions), and assign a root residue with the {@link #setRootResidue}
*   method. {@link Residue} and {@link Linkage} tokens are produced via the 
*   <tt>create*Token</tt> methods. The AST produced is of type 
*   <tt>Graph&lt;ResidueToken,LinkageToken&gt;</tt>, which can be obtained from
*   the {@link #getAST} method, and the transformed sugar object from
*   the {@link #getSugar} method. {@link SequenceFormatException}s are emitted
*   immediately upon recognition of syntactic errors.
*</p>
*<p>
*   Finally, the static method {@link #performParse} serves as a nice, easy
*   driver for running a built parser:
*</p>
<pre>
    String sequence = ...;
    
    MyLexer lexer = new MyLexer( new StringReader( sequence ) );
    MyParser parser = new MyParser( lexer );
    
    ParserAdaptor.performParse( parser, sequence );
    
    Sugar s = parser.getSugar();
</pre>
*
*   @see ResidueToken
*   @see LinkageToken
*   @author mjh
*   @version $Rev: 1237 $
*/
public abstract class ParserAdaptor extends LLkParser
{
 
    //~~~~~~~~~~~~~~~~~~~~~  STATIC FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Logging instance. */
    static final Logger log = Logger.getLogger( ParserAdaptor.class );
    
    static final boolean debugging = log.isDebugEnabled();
    static final boolean tracing = log.isTraceEnabled();

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Our sugar graph (Abstract Syntax Tree). */   
    protected Graph<LinkageToken,ResidueToken> graph 
        = new Graph<LinkageToken,ResidueToken>();

    /** The sugar sequence currently being parsed. */
    protected String sequence;
        
        
    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /*  pointlessly inherited constructors, stupid java  */
    public ParserAdaptor( int k ) { super( k ); }
    public ParserAdaptor( ParserSharedInputState state, int k ) { super( state, k ); }
    public ParserAdaptor( TokenBuffer buffer, int k ) { super( buffer, k ); }
    public ParserAdaptor( TokenStream stream, int k ) { super( stream, k ); }

    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~//        
    
    /**
    *   Parses the given sequence string using the given parser instance.
    *   @throws SequenceFormatException in response to sequence syntax 
    *           errors.
    */
    public static void performParse( ParserAdaptor parse, String sequence )
    throws SequenceFormatException
    {
        if ( sequence == null || sequence.length() == 0 )
            throw new SequenceFormatException(
                "sequence cannot be null or zero-length" );
        
        parse.setSequence( sequence );
        
        try 
        {  
            parse.sugar();  
        }
        
        //  catch ANTLR exceptions and turn into seq format exceptions
        //  for contextual error messages. it's ugly, but it's very 
        //  effective for diagnosing sequence syntax errors. 
        catch ( RecognitionException e )
        {
            throw new SequenceFormatException(  
                    sequence, 
                    e.column - 1, 
                    e.getMessage() 
                    );
        }
        catch ( TokenStreamRecognitionException e )
        {
            throw new SequenceFormatException(  
                    sequence, 
                    e.recog.column - 1, 
                    e.getMessage()  
                    );
        }
        catch ( TokenStreamException e )
        {
            // we don't really care about this i don't think...
            //e.printStackTrace();
            log.warn("Caught " + e.getClass() + " while parsing sequence: " 
                    + e.getMessage() );
            
            throw new SequenceFormatException( e.getMessage() );
        }

        return;
    }
        
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//        
    
    /*  sugar  *//*************************************************** 
    *
    *   Start rule for parsing a sugar sequence in any format. 
    *   This method is meant to be overriden by a grammar subclass;
    *   this method only serves to identify the start rule for parsing
    *   any given grammar.
    */
    public abstract void sugar()
    throws RecognitionException, TokenStreamException
    ;
    
    
    /*    addResidue  *//**********************************************
    *
    *   Adds a residue vertex to the current sugar graph. Note that the 
    *   added residue will be regarded as *not* being attached to anything 
    *   after this method is called - it needs to be linked to the nascent 
    *   sugar with the addLinkage method.    
    *   
    *     @param r     
    *   A ResidueToken instance representing a residue.
    *     @throws SequenceFormatException
    *   upon encountering invalid syntax
    *
    *   @see #createResidueToken
    */
    public void addResidue( ResidueToken r ) throws SequenceFormatException
    {
        _check_notnull( r );        
        
        if ( tracing )
        {
            log.trace( "adding to graph: residue '" + r + "'" );
            traceParse( r, "A residue" );
        }
        
        assert ! graph.contains( r ): "Graph already contains residue " + r;
        
        graph.addVertex( r );
    }

    
    /*    addLinkage  *//**********************************************
    *
    *   Adds an edge to the object graph between the passed ResidueToken
    *   vertices, with the value of the passed LinkageToken. 
    *
    *     @param child    
    *   The residue on the <b>non-reducing</b> terminal side of the linkage.
    *     @param link
    *   The linkage.
    *     @param parent
    *   The residue on the <b>reducing</b> terminal side of the linkage.
    *     @throws SequenceFormatException
    *   If there's a problem
    */
//    public void addLinkage( ResidueToken child, LinkageToken link, ResidueToken parent ) 
    public void addLinkage( ResidueToken parent, LinkageToken link, ResidueToken child ) 
    throws SequenceFormatException
    {
        _check_notnull( child ); 
        _check_notnull( parent );
        _check_notnull( link ); 
        
        if ( tracing )
        {
            log.trace( 
                "adding to graph: linkage=" 
                + link 
                + " between parent=" 
                + parent
                + ", position="
                + link.getLinkage().getParentTerminus()
                + " and child="
                + child
                + ", position="
                + link.getLinkage().getChildTerminus()
            );
            traceParse( link, "A linkage" );
         
            //  produce a debugging message to STDERR showing
            //  start/end of added linkage. pretty useful IMO.
            
            int left = child.getColumn();
            int right = parent.getColumn();
            if ( left > right ) { int swap = left; left = right; right = swap; }
            int padding = (right - left - 2);
            if ( padding < 0 ) padding = 0;
            
            int link_left  = link.getLeftColumn();
            int link_right = link.getRightColumn();
            int link_len   = link_right - link_left + 1;
            if ( link_len <= 1 ) link_len = 0;
            
            int padding2 = link_left - right - 1;
            if ( padding2 < 0 ) padding2 = 0;
            
            log.trace( sequence );
            log.trace( 
                repeat(" ", left)
                + '\\'
                + repeat("_", padding)
                + '/'
                + repeat(" ", padding2 )
                + repeat("^", link_len )
                + " linkage value = "
                + link
            );
        }

        graph.addEdge( 
            graph.getVertex( parent ),
            graph.getVertex( child ), 
            link 
        );
                     
        return;
    }
    

    /*  checkRepeatBounds  *//***************************************
    *   
    *   Sanity checks the bounds of an internal sugar repeat sequence. 
    *   
    *   @param lowertok
    *   @param uppertok
    *   @throws SemanticException
    */
    protected void checkRepeatBounds( Token lowertok, Token uppertok ) 
    throws SequenceFormatException
    {
        
        int lower, upper;       
        lower = Integer.parseInt( lowertok.getText() );
        
        if ( uppertok != null )
        {
            /* it's a dual-bounded repeat range "XX-YY" */

            if ( tracing )
            {
                log.trace( 
                    "checking dual-bounded repeat range: " 
                    + lowertok.getText() 
                    + "-" 
                    + uppertok.getText() 
                );
            }
            
            upper = Integer.parseInt( uppertok.getText() ); 
            
            if ( lower >= upper )
                throw new SequenceFormatException( 
                        getSequence(),
                        lowertok.getColumn() - 1,
                        uppertok.getColumn() - 1,
                        "First repeat bound in a repeat range must be"
                        + " less than the second repeat bound"
                );
        }
        else
        {
            /* it's a single bounded repeat "XX", not a range */

            if ( tracing )
                log.trace( "checking singly-bounded repeat = " + lowertok.getText() );

            if ( lower <= 1 )
                throw new SequenceFormatException( 
                    getSequence(),
                    lowertok.getColumn() - 1,  
                    "Single repeat bound must be greater than 1"
                );                
        }           
    }
    

    /*  createLinkageToken  *//**************************************
    *
    *   Creates a LinkageToken. Intended to be overriden by subclasses
    *   to return sequence format-specific linkage tokens as appropriate
    *   to the needs of the format.
    *
    *   @param anomer  Token encapsulating anomeric configuration.
    *   @param parent  Token from reducing terminal side.
    *   @param child   Token from non-reducing terminal side.
    */
    public LinkageToken createLinkageToken( Token anomer, Token parent, Token child )
    {
        return new LinkageToken( sequence, anomer, parent, child );
    }
    
    
    /*  createResidueToken  *//**************************************
    *
    *   Creates a ResidueToken representing a {@link Residue}, which
    *   is neither a {@link Monosaccharide} nor {@link Substituent}. 
    *   Intended to be overriden by subclasses to return sequence 
    *   format-specific residue tokens as appropriate to the needs 
    *   of the format.
    *
    *   Since we generally expect {@link Residue}s to be either
    *   {@link Monosaccharide}s or {@link Substituent}s, the default
    *   implementation of this method throws a {@link UnsupportedOperationException}.
    *
    *   @param raw_sequence_token   
    *       A token taken directly from the raw sequence.
    *   @throws SequenceFormatException     
    *       If the token text does not correspond to a valid Residue.
    */
    protected ResidueToken createResidueToken( Token raw_sequence_token )
    throws SequenceFormatException
    {
        throw new UnsupportedOperationException(
            "The default implementation of this method throws this exception"
            + " -- feel free to implement it."
        );
    }
    
    
    /*  createMonosaccharideToken  *//*******************************
    *
    *   Creates a ResidueToken representing a {@link Monosaccharide}. 
    *   Intended to be overriden by subclasses to return sequence 
    *   format-specific residue tokens as appropriate to the needs 
    *   of the format.
    *
    *   @param raw_sequence_token   
    *       A token taken directly from the raw sequence.
    *   @return 
    *       A {@link ResidueToken}, initialised from the token text
    *   @throws SequenceFormatException     
    *       If the token text does not correspond to a valid Monosaccharide.
    */
    protected ResidueToken createMonosaccharideToken( Token raw_sequence_token )
    throws SequenceFormatException
    {
        String name = raw_sequence_token.getText();
        Monosaccharide monosac = getSequenceFormat().getMonosaccharide( name );
        
        if ( monosac == null )
        {
            throw new SequenceFormatException(
                getSequence(),
                raw_sequence_token.getColumn() - 1,
                raw_sequence_token.getColumn() + name.length() - 2,
                "Unrecognised monosaccharide name: " + name
            );
        }
        
        return new ResidueToken( this, raw_sequence_token, monosac );        
    }

    
    /*  createSubstituentToken  *//**********************************
    *
    *   Creates a ResidueToken representing a {@link Substituent}. 
    *   Intended to be overriden by subclasses to return sequence 
    *   format-specific residue tokens as appropriate to the needs 
    *   of the format.
    *
    *   @param raw_sequence_token   
    *       A token taken directly from the raw sequence.
    *   @throws SequenceFormatException     
    *       If the token text does not correspond to a valid Substituent.
    */
    protected ResidueToken createSubstituentToken( Token raw_sequence_token )
    throws SequenceFormatException
    {
        String name = raw_sequence_token.getText();
        Substituent substit = getSequenceFormat().getSubstituent( name );
        
        if ( substit == null )
        {
            /*
            throw new SequenceFormatException(
                getSequence(),
                raw_sequence_token.getColumn() - 1,
                raw_sequence_token.getColumn() + name.length() - 2,
                "Unrecognised substituent name: " + name
            );
            */
            log.warn(
                "Substituent with name '" 
                + name 
                + "' is unknown, returning a generic substituent residue"
            );
            substit = Substituents.createUnknownSubstituent( name );
        }
        
        return new ResidueToken( this, raw_sequence_token, substit );        
    }
    
    
    /**
    *   Returns the {@link SequenceFormat} that this parser implements.
    */
    public abstract SequenceFormat getSequenceFormat()
    ;
    
    
    /*  getGraph  *//************************************************
    *
    *   Returns the graph instance used internally to build the sugar
    *   object whilst parsing a sequence string.
    */
    public Graph<LinkageToken,ResidueToken> getGraph() {  return graph;  }
    
    
    /*  getSugar  *//************************************************
    *
    *   Returns the sugar object for the parsed sequence. Only makes
    *   sense to call this method after parsing, see the sugar() method.
    *
    *   @see #sugar()
    *   @see #createSugar()
    */
    public Sugar getSugar() throws SequenceFormatException
    {
        if ( tracing )
            log.trace("parsed sugar AST:\n" + graph.toString() );
        
        Sugar sugar = createSugar();

        if ( debugging ) 
            log.debug("Translating sugar AST to sugar object");
        
        translateAstToSugar( graph, sugar );
        
        return sugar;
    }
    
    
    /*  createSugar  *//*********************************************
    *
    *   Factory contructor for a new, empty {@link Sugar} object.
    *   Intended to be overridden by subclasses where necessary.
    */
    protected Sugar createSugar()
    {
        return new Sugar( graph.countVertices() );
    }
    
    
    /**
    *   Translates the abstract syntax tree of parsed {@link ResidueToken}s 
    *   and {@link LinkageToken}s to the given empty {@link Sugar}. 
    */
    protected void translateAstToSugar( Graph<LinkageToken,ResidueToken> ast, Sugar sugar )
    {
        //  create an AST walker to populate the new Sugar
        AstTranslatorVisitor ast_visitor = new AstTranslatorVisitor( sugar );
        
        //  walk the AST & add to sugar
        ast_visitor.visit( ast );
    }
    
    
    /*  getSequence  *//*********************************************
    *
    *   Gets the sequence string currently being parsed.
    */
    public String getSequence() {  return sequence;  }
    
    
    /*  setRootResidue  *//******************************************
    *
    *   Sets the "root" residue of the Sugar being parsed, since different 
    *   sequence formats encounter the root residue at different times
    *   during parsing. 
    */
    public void setRootResidue( ResidueToken r ) throws SequenceFormatException
    {
        _check_notnull( r );  
        if ( tracing )
        {
            log.trace( "setting root residue '" + r + "'" );
            traceParse( r, "Root residue" );
        }
        graph.setRootVertex( graph.getVertex(r) );
    }
    
    
    /*  setSequence  *//*********************************************
    *
    *   Sets the sequence being parsed.
    */
    public void setSequence( String seq ) {  sequence = seq;  }


    /*  traceIn  *//*************************************************
    *
    *   Overriden from ANTLR Parser class in order to add additional
    *   context information to the trace messages produced by ANTLR.
    *   Calling of this method is controlled by the 'trace' and 
    *   'traceparser' debugging settings in ANTLR; that is, this
    *   method is implicitly called by ANTLR only if the ANTLR
    *   trace/traceparser setting is true.
    *   @see the Ant "build-grammar" build task in the build.xml
    *   file for this package.
    */
    public void traceIn( String rule_name ) throws TokenStreamException
    {
        super.traceIn( rule_name ); /*
        if ( log.isDebugEnabled() )
        {
            traceParse( LT(1) );
            System.err.println();
        }
        */
    }
    
/* only for hard-core debugging...
    public void traceOut( String rule_name ) throws TokenStreamException
    {
        super.traceOut( rule_name );
        traceParse( LT(1) );
    }
*/

    /**
    *   Factory method for creating {@link SequenceFormatException}s.
    *   @see SequenceFormatException
    */
    protected SequenceFormatException createSyntaxException( Token t, String message )
    {
        return new SequenceFormatException(
            getSequence(),
            t.getColumn() - 1,
            t.getColumn() + t.getText().length() - 2,
            message
        );          
    }


    /**
    *   Factory method for creating {@link SequenceFormatException}s.
    *
    *   @see SequenceFormatException
    *   @param seq_index 
    *   the index of the syntax error in the sequence string returned 
    *   by {@link #getSequence}
    *   @param message the error message to show
    */
    protected SequenceFormatException createSyntaxException( int seq_index, String message )
    {
        return new SequenceFormatException(
            getSequence(),
            seq_index,
            message
        );          
    }

    
    /*  traceParse  *//**********************************************
    *   
    *   Writes a formatted debugging message to STDERR indicating 
    *   the position and content of the passed Token. Returns without
    *   error if Token is null, or if the log level is not set to at least
    *   TRACE level.
    *
    *   @param t    
    *       The token to be traced
    *   @param desc
    *       A text string describing what this token is meant to represent
    */
    public void traceParse( Token t, String description )
    {
        if ( ! tracing ) 
            return;

        if ( t == null ) 
            return;
        
        if ( t.getText() == null ) 
            return;
        
        int col = t.getColumn();
        if ( col < 1 ) return;

        System.err.println( sequence );
        System.err.println( 
            repeat(" ", col - 1 ) 
            + repeat("^", t.getText().length() )
            + " "
            + description
            + " (col "
            + col
            + ")" 
        );

        //  alt format
        //        System.out.println( 
        //                String.format(
        //                        "@index %3d | '%s' -> %s", 
        //                        t.getColumn(), 
        //                        t.getText(),
        //                        this.getTokenName( t.getType() )
        //                )
        //        );
        
        //  alt format 2
        //        System.err.println(
        //                String.format(
        //                        "%s>%s :: index %3d: %s", 
        //                        StringUtils.repeat( "-", t.getColumn() ),
        //                        t.getText(),                        
        //                        t.getColumn(), 
        //                        this.getTokenName( t.getType() )
        //                )
        //        );
    }

    
    public void traceParse( Token t )
    {
        if ( ! tracing ) 
            return;
        
        if ( t == null ) 
            return;
        
        traceParse( t, this.getTokenName( t.getType() ) );
    }
    
    
    
    
    //~~~~~~~~~~~~~~~~~~~~  PRIVATE METHODS  ~~~~~~~~~~~~~~~~~~~~~~~~

    private static final void _check_notnull( ResidueToken rt )
    throws SequenceFormatException
    {
        if ( rt == null ) 
            throw new SequenceFormatException(
                "Expected Residue, but got null");
    }
    

    private static final void _check_notnull( LinkageToken lt )
    throws SequenceFormatException
    {
        if ( lt == null ) 
            throw new SequenceFormatException(
                "Expected Linkage, but got null");
    }


} // end class ParserAdapter
