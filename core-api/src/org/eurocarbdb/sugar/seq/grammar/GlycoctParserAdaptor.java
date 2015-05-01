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
*   Last commit: $Rev: 1559 $ by $Author: glycoslave $ on $Date:: 2009-07-21 #$  
*/

package org.eurocarbdb.sugar.seq.grammar;

//  stdlib imports
import java.util.List;
import java.util.EnumSet;
import java.util.ArrayList;
import java.util.Iterator;

//  3rd party imports 
import org.apache.log4j.Logger;

import antlr.Token;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.SemanticException;
import antlr.ParserSharedInputState;

//  3rd party imports 

//  eurocarb imports
import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.Anomer;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Linkage;
import org.eurocarbdb.sugar.Basetype;
import org.eurocarbdb.sugar.CommonBasetype;
import org.eurocarbdb.sugar.Superclass;
import org.eurocarbdb.sugar.StereoConfig;
import org.eurocarbdb.sugar.Modification;
import org.eurocarbdb.sugar.ModificationType;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.Substituent;
import org.eurocarbdb.sugar.Substituents;
import org.eurocarbdb.sugar.GlycosidicLinkage;
import org.eurocarbdb.sugar.SequenceFormatException;
import org.eurocarbdb.sugar.SequenceFormat;
import org.eurocarbdb.sugar.SugarRepeat;
import org.eurocarbdb.sugar.SugarRepeatAnnotation;
import org.eurocarbdb.sugar.seq.GlycoctSequenceFormat;

import org.eurocarbdb.util.graph.Graph;
import org.eurocarbdb.util.graph.Edge;
import org.eurocarbdb.util.graph.Vertex;

import org.eurocarbdb.sugar.impl.SimpleMonosaccharide;
import org.eurocarbdb.sugar.impl.ComplexMonosaccharide;

//  static imports
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.sugar.Basetypes.getBasetype;
import static org.eurocarbdb.sugar.Substituents.substituentIsPartOfMonosaccharide;


/*  class GlycoctParserAdaptor  *//**********************************
*
*   Utility class to support parsing carbohydrates in GlycoCT 
*   sequence format. This class provides methods for use in 
*   {@link GlycoctParser}, the class that is auto-generated from
*   a grammar file by ANTLR.
*
*   @see GlycoctSequenceFormat
*   @see ResidueToken
*   @see LinkageToken
*/
public abstract class GlycoctParserAdaptor extends ParserAdaptor
{

    //~~~~~~~~~~~~~~~~~~~~~  STATIC FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Logging instance. */
    static final Logger log = Logger.getLogger( GlycoctParserAdaptor.class );
    
    static final boolean debugging = log.isDebugEnabled();
    
    static final boolean tracing = log.isTraceEnabled();

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** List of residues added in order of addition, oldest first. */
    protected List<ResidueToken> residues = new ArrayList<ResidueToken>();
    
    /** List of linkages added in order of addition, oldest first. */
    protected List<LinkageToken> linkages = new ArrayList<LinkageToken>();
        
    /** List of linkages added in order of addition, oldest first. */
    protected List<RepeatResidueToken> repeats = new ArrayList<RepeatResidueToken>( 2 );
    
    protected List<RepeatResidueToken> repeatsStack 
        = new ArrayList<RepeatResidueToken>( 2 );
    
    
    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /*  pointlessly inherited constructors, stupid java  */
    public GlycoctParserAdaptor( int k ) { super( k ); }
    public GlycoctParserAdaptor( ParserSharedInputState state, int k ) { super( state, k ); }
    public GlycoctParserAdaptor( TokenBuffer buffer, int k ) { super( buffer, k ); }
    public GlycoctParserAdaptor( TokenStream stream, int k ) { super( stream, k ); }

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~        
    
    @Override
    public void addResidue( ResidueToken r ) throws SequenceFormatException
    {
        super.addResidue( r );
        residues.add( r );
        
        if ( residues.size() == 1 ) 
            setRootResidue( r );
        
        //  add to repeats if we are inside of a repeat        
        for ( RepeatResidueToken repeat : repeatsStack )
        {
            if ( tracing )
                log.trace( "also adding last residue to repeat sub-graph" );
            repeat.addResidueToken( r );
        }
    }


    /*  addLinkage  *//**********************************************
    *
    *   Adds a new linkage into the sugar object graph.
    *
    *   @param linkage_index            the index of the linkage being referenced in the list of linkages          
    *   @param parent_residue_index     the index of the residue on the reducing (parent) side of the linkage
    *   @param parent_linkage_type      type of bond on parent's side of linkage
    *   @param linkage_parent           terminal position of bond to parent residue 
    *   @param linkage_child            terminal position of bond to parent residue 
    *   @param child_residue_index      the index of the residue on the non-reducing (child) side of the linkage
    *   @param child_linkage_type       type of bond on child's side of linkage
    */
    public void addLinkage( Token linkage_index
                          , Token parent_residue_index 
                          , Token parent_linkage_type 
                          , Token linkage_parent 
                          , Token linkage_child
                          , Token child_residue_index 
                          , Token child_linkage_type   )
    throws SequenceFormatException
    {
        //  retrieve link index, reducing terminal position, non-reducing terminal position
        int link_index   = getLinkageIndexFor( linkage_index );
        int parent_index = getResidueIndexFor( parent_residue_index  );        
        int child_index  = getResidueIndexFor( child_residue_index );
        
        //  get parent & child residues
        ResidueToken parent = residues.get( parent_index );
        ResidueToken child  = residues.get( child_index );
        
        if ( "?".equals( linkage_parent.getText() ) ) 
            linkage_parent.setText( "" + LinkageToken.UNKNOWN_TERMINUS );
        
        if ( "?".equals( linkage_child.getText() ) ) 
            linkage_child.setText( "" + LinkageToken.UNKNOWN_TERMINUS );
        
        //  create the linkage itself
        LinkageToken link = createLinkageToken( child.anomer, linkage_parent, linkage_child );
        
        //  this is so that error messages know where linkages start and finish in the sequence
        link.setLeftColumn( parent_residue_index.getColumn() );
        link.setRightColumn( child_linkage_type.getColumn() );
        
        super.addLinkage( parent, link, child );
        
        linkages.add( link );
        
        /*
        //  add to repeats if we are inside of a repeat        
        for ( RepeatResidueToken repeat : repeatsStack )
        {
            log.debug( "also adding last linkage to repeat sub-graph" );
            repeat.getGraph().addEdge(
                this.graph.getVertex( parent ),
                this.graph.getVertex( child ), 
                link 
            );
        }
        */
    }
    
    
    /*  addSubstituentOrModification  *//**************************************
    *
    *   Attempt to add a {@link Substituent} or {@link Modification} from the 
    *   passed {@link Token} to current {@link Residue} ({@link #lastResidue}) 
    *   at given position(s).
    *
    *   @param mod_or_subst 
    *       the token containing the name of the modification
    *   @param term1 
    *       token containing int position 1
    *   @param term2 
    *       token containing (optional) int position 2, or null if only 1 position.
    */
    public void addSubstituentOrModification( Token mod_or_subst, Token term1, Token term2 )
    throws SequenceFormatException
    {
        assert term1 != null;
        assert mod_or_subst != null;
     
        //  try to get a Substituent for name, if none, then try to add as 
        //  modification
        String name = mod_or_subst.getText();
        Substituent s = Substituents.getSubstituent( name );
        
        //  if not a Substituent, then must be a modification
        if ( s == null )
        {
            addModification( mod_or_subst, term1, term2 );
            return;
        }
            
        //  ok, it's a substituent, make sure it is the right type
        //  to be allowed to be merged into the monosaccharide
        if ( ! substituentIsPartOfMonosaccharide( s ) )
        {
            throw createSyntaxException( 
                mod_or_subst, 
                "Substituent cannot be considered part of the monosaccharide" 
            );
        }

        //  get the monosac (token) we're going to attach to.        
        //  the last residue token added *must* be castable to MonosacResidueToken
        ResidueToken rt = lastResidue();
        assert rt instanceof MonosacResidueToken;
        MonosacResidueToken mrt = (MonosacResidueToken) rt;
        
        //  get terminus position
        int position = getTerminusFor( term1 );        
        
        _set_substituent( mrt, mod_or_subst, s, position );
        
        //  if term2 != null, then it's a dual-point substituent like
        //  alkene, which we handle (simplistically) by adding the same
        //  substituent to both positions. this might need some additional
        //  code/abstraction in the future, such as moving the check for
        //  valid positions into the substituent class/instance.
        if ( term2 != null )
        {
            log.trace("adding second position of dual point substituent");
            int position2 = getTerminusFor( term2 );
            
            if ( position == position2 )
            {   
                throw createSyntaxException( 
                    term2, "position2 cannot be equal to position1" );
            }
            
            //  assert contiguous for now...
            if ( position2 - position != 1 )
            {
                log.warn(
                    "dual point substituent with non-contiguous positions: "
                    + "substituent="
                    + s
                    + ", position1="
                    + position
                    + ", position2="
                    + position2
                );   
            }
            _set_substituent( mrt, mod_or_subst, s, position2 );
        }
            
    }
    
    
    private final void _set_substituent( MonosacResidueToken mrt
                                       , Token mod_or_subst
                                       , Substituent s
                                       , int position )
    throws SequenceFormatException
    {
        if ( tracing )
        {
            log.trace(
                "Adding substituent " 
                + s 
                + " to residue " 
                + mrt 
                + " at position " 
                + position 
            );
            traceParse( mod_or_subst, "A substituent attached to a monosaccharide" );
        }

        try
        {
            mrt.setSubstituent( s, position );
        }
        catch ( IllegalArgumentException ex )
        {
            SequenceFormatException sfex = createSyntaxException( mod_or_subst, ex.getMessage() );
            sfex.initCause( ex ); 
            throw sfex;
        }
    }
    
    
    /*  addModification  *//*****************************************
    *
    *   Attempt to add a {@link Modification} to current {@link Residue} 
    *   ({@link #lastResidue}) at given positions.
    *
    *   @param modification the token containing the name of the modification
    *   @param term1 token containing int position 1
    *   @param term2 token containing (optional) int position 2
    */
    public void addModification( Token modification, Token term1, Token term2 )
    throws SequenceFormatException
    {
        assert term1 != null;
        assert modification != null;
        
        Modification m = null;
        ModificationType type = null;
        ResidueToken r = lastResidue();
        
        int t1 = getTerminusFor( term1 );        
        String modification_name = modification.getText();
        
        //  lookup modification name
        try 
        {  
            type = ModificationType.forName( modification_name );  
        }
        catch ( IllegalArgumentException ex )
        {
            SequenceFormatException sfex 
                = createSyntaxException( modification, ex.getMessage() );
            sfex.initCause( ex ); 
            throw sfex;
        }
        
        //  is it a dual-point modification? 
        if ( term2 != null ) 
        {
            //  dual-point
            int t2 = getTerminusFor( term2 );
            
            try 
            {
                m = new Modification( type, t1, t2 );
            }
            catch ( IllegalArgumentException ex )
            {
                SequenceFormatException sfex 
                    = createSyntaxException( term2, ex.getMessage() );
                sfex.initCause( ex ); 
                throw sfex;
            }
        }
        else
        {
            //  single-point
            try 
            {
                m = new Modification( type, t1 );
            }
            catch ( IllegalArgumentException ex )
            {
                SequenceFormatException sfex 
                    = createSyntaxException( term1, ex.getMessage() );
                sfex.initCause( ex ); 
                throw sfex;
            }
        }
            
        //  add modification to residue
        if ( debugging )
        {
            log.debug("Adding modification " + m + " to residue " + r );
            traceParse( modification, "A modification" );
        }
        
        //  TODO - add modification $m to $r
        //  
        log.warn("TODO modifications...");
        
        // Residue res = r.getResidue();
        
        // if ( ! (res instanceof Monosaccharide) ) 
        // {   
        //     throw new SequenceFormatException( 
        //         getSequence(), 
        //         r.getColumn() - 1, 
        //         "Residue was determined to be a " 
        //             + res.getClass() 
        //             + ", not a Monosaccharide"
        //     );
        // }
        
        // Monosaccharide monosac = (Monosaccharide) res;
        // ComplexMonosaccharide cm = (monosac instanceof ComplexMonosaccharide)
        //     ?   (ComplexMonosaccharide) monosac
        //     :   new ComplexMonosaccharide( monosac );
            
        // cm.addModification( m );
        
        return;
    }
    
    
    /**
    *   Adds a (forward reference to a) repeat sub-structure to the 
    *   current sugar. 
    *   @param token      
    *   token for the index of the repeat being referenced, index starts at 1.
    */  
    public void addRepeatResidue( Token token )
    {
        RepeatResidueToken rt = new RepeatResidueToken( this, token, null );
        
        int i = 0;
        try {  i = Integer.parseInt( token.getText() );  }
        catch ( Exception ex ) 
        {
            SequenceFormatException sfex = createSyntaxException( 
                token
                , "Invalid repeat index: " 
                + ex.getMessage()   
            );
            sfex.initCause( ex ); 
            throw sfex;
        }

        addResidue( rt );
        repeats.add( rt );

        //  assume for now that the index given is the same as the 
        //  the position of the repeat added to the list
        assert repeats.size() == i;

        if ( debugging )
        {
            log.debug("adding repeat " + i );
            traceParse( token, "Forward repeat reference" );
        }
        
        return;
    }
    

    @Override
    protected Sugar createSugar()
    {
        if ( repeats != null && repeats.size() > 0 )
        {
            if ( tracing )
                log.trace("creating repeat sugar...");
            return new SugarRepeat( graph.countVertices() );
        }
        
        else return super.createSugar();
    }
    
    
    /** Returns {@link SequenceFormat#Glycoct}. */
    @Override
    public final SequenceFormat getSequenceFormat()
    {
        return SequenceFormat.Glycoct;   
    }

     
    @Override
    public Sugar getSugar() throws SequenceFormatException
    {
        if ( debugging )
            log.debug("raw parsed sugar AST:\n" + graph.toString() );
        
        boolean has_repeats = ( repeats != null && repeats.size() > 0 );
        
        //  remove virtual RepeatResidueTokens from AST and re-stitch
        //  linkages to the correct places 
        if ( has_repeats )
            inlineRepeats( graph );
        
        //  get rid of pesky NAcs and other common substituents
        inlineSubstituents( graph );
        
        Sugar sugar = super.getSugar();
        
        //  add SugarRepeatAnnotations to sugar
        //  note: that the passed Sugar will be a SugarRepeat if 
        //  repeats were detected during parse.
        if ( has_repeats )
            addRepeatAnnotations( (SugarRepeat) sugar );
        
        return sugar;
    }
    
    
    private final void addRepeatAnnotations( SugarRepeat sugar )
    {
        if ( debugging )
            log.debug("adding " + repeats.size() + " repeat annotation(s)");

        for ( RepeatResidueToken repeat_residue : repeats )
        {
            //  Sugar is a SugarRepeat; see overridden #createSugar()
            SugarRepeatAnnotation a = repeat_residue.getRepeatAnnotation();
            sugar.addRepeatAnnotation( a );
        }
    }
    
    
    /**
    *<p>
    *   In this class, repeats are initially included into the AST 
    *   as a virtual residue, to match the way they are handled in
    *   glycoct. The residues of the repeat themselves are treated as
    *   if attached to this virtual residue. This method removes the 
    *   virtual residue from the graph and stitches its incoming and 
    *   outgoing edges to the root and leaf residues of the repeat.
    *</p>
    *<p>
    *   The 4 possibilities are: 
    *       1) repeat residue is in the middle of the structure
    *       2) repeat residue is at the root of the structure 
    *       3) repeat residue is at a leaf of the structure
    *       4) the entire structure is the repeat
    *</p>
    */
    private final void inlineRepeats( Graph<LinkageToken,ResidueToken> ast )
    {
        if ( debugging )
            log.debug("inlining repeat sub-tree(s)");
        
        for ( RepeatResidueToken repeat_residue : repeats )
        {
            //  need to remove RepeatResidueToken from AST; it is just a 
            //  marker/placeholder residue for the repeat
            Vertex<LinkageToken,ResidueToken> v = ast.getVertex( repeat_residue );
            assert v != null;
            
            List<Edge<LinkageToken,ResidueToken>> elist;
            
            //  incoming edge
            elist = v.getIncomingEdges();
            Edge<LinkageToken,ResidueToken> incoming_edge = null;

            if ( elist.size() > 1 )
            {
                //  glycoct doesn't support this anyway, should never happen
                throw new UnsupportedOperationException(
                    "multi-connections to repeat start residue not handled");
            }
            else if ( elist.size() == 1 )
            {
                //  the normal case...
                incoming_edge = elist.get(0);   
            }
            else  
            { 
                // virtual repeat residue must at the root
                assert v == ast.getRootVertex() 
                    : "expected " + v + " == " + ast.getRootVertex();
                    
                ast.setRootVertex( 
                    ast.getVertex( repeat_residue.getRootResidueToken() ) );
            }
                
            
            //  outgoing edge
            elist = v.getOutgoingEdges();
            Edge<LinkageToken,ResidueToken> outgoing_edge = null;
            if ( elist.size() > 1 )
            {
                //  glycoct doesn't support this anyway, should never happen
                throw new UnsupportedOperationException(
                    "multi-connections from repeat end residue not handled");
            }
            else if ( elist.size() == 1 )
            {
                //  the normal case...
                outgoing_edge = elist.get(0);   
            }
            else  
            { 
                // virtual repeat residue must be a leaf
                assert ast.getLeafVertices().contains( v );
            }

            assert incoming_edge == null 
                || outgoing_edge == null
                || incoming_edge.getChild() == outgoing_edge.getParent();
            
            //  remove v (the virtual repeat token) from the AST 
            //  (this will also remove its edges)
            ast.remove( v );
            
            //  then reconnect edges to the repeat sub-tree:
            if ( incoming_edge != null )
            {
                ast.addEdge( 
                    incoming_edge.getParent().getValue(), 
                    repeat_residue.getRootResidueToken(), 
                    incoming_edge.getValue() 
                );
            }
            
            if ( outgoing_edge != null )
            {
                ast.addEdge( 
                    repeat_residue.getLeafResidueToken(), 
                    outgoing_edge.getChild().getValue(), 
                    outgoing_edge.getValue() 
                );
            }
        }
        
        if ( tracing )
            log.trace("AST after inling repeats:\n" + ast.toString() );
    }
    
    
    private void inlineSubstituents( Graph<LinkageToken,ResidueToken> ast )
    {
        if ( tracing )
            log.trace("before pruning common substituents, AST is:\n" + ast );
        
        List<Vertex<LinkageToken,ResidueToken>> vertices_to_remove = null;
        Vertex<LinkageToken,ResidueToken> substit_vert = null;
        Edge<LinkageToken,ResidueToken> substit_edge;
        
        for ( ResidueToken rt : ast.getAllVertexValues() )
        {
            //  if residue is a type that we consider to be part of the 
            //  sugar, then coalesce it with the Residue it's attached to
            //  and remove it from the graph.
            Residue r = rt.getResidue();
            boolean residue_should_be_merged = (r instanceof Substituent) 
                && substituentIsPartOfMonosaccharide( (Substituent) r );
            
            // if ( "n-acetyl".equals( res_name ) || "n".equals( res_name ) )
            if ( residue_should_be_merged )
            {
                if ( debugging )
                    log.debug("merging common substituent: " + rt );
                substit_vert = ast.getVertex( rt );
                
                //  assume that NAc is always a terminating child residue (ie: leaf)
                assert substit_vert.countAttachedEdges() == 1;
                substit_edge = substit_vert.getAttachedEdges().iterator().next();
                assert substit_vert == substit_edge.getChild();
                
                LinkageToken lt = substit_edge.getValue(); 
                int position = lt.getLinkage().getParentTerminus();
                ResidueToken parent = substit_edge.getParent().getValue();
                
                // if ( position <= 0 )
                // {
                //     log.warn("common substituent NOT merged because position is unknown");
                //     continue;
                // }
                
                mergeCommonSubstituent( rt, parent, position );
                
                // ast.remove( substit_vert );
                if ( vertices_to_remove == null )
                    vertices_to_remove = new ArrayList<Vertex<LinkageToken,ResidueToken>>( 8 );
                
                vertices_to_remove.add( substit_vert );
                // linkages.remove( lt ); // is this even necessary?
                // it.remove();
            }
        }
        
        if ( vertices_to_remove != null )
        {
            if ( tracing )
                log.trace("Removing " + vertices_to_remove.size() + " common substituents:");

            for ( Vertex<LinkageToken,ResidueToken> substituent : vertices_to_remove )
                 ast.remove( substituent );
            
            if ( tracing )
                log.trace("after pruning common substituents, AST is now:\n" + ast );
        }
    }
    
    
    void mergeCommonSubstituent( ResidueToken to_merge, ResidueToken recipient, int position )
    {
        Residue r = recipient.getResidue();
        if ( ! (r instanceof Monosaccharide) )
        {
            throw createSyntaxException(
                recipient
                , "Can't merge residue token " 
                + to_merge
                + " -- recieving residue "
                + recipient
                + "is not a Monosaccharide"
                );
        }
        
        Monosaccharide m = (Monosaccharide) r;
        Substituent s    = (Substituent) to_merge.getResidue();
        
        try
        {
            m.attach( s, position );
        }
        catch ( Exception ex )
        {
            SequenceFormatException sfex = createSyntaxException(
                recipient
                , "Caught exception while trying to merge substituent '"
                + to_merge
                + "' into '"
                + recipient
                + "': "
                + ex.getMessage()
            );   
            sfex.initCause( ex );
            throw sfex;
        }
    }
    
                
    /** Returns the repeat corresponding to the given index */
    public RepeatResidueToken getRepeat( Token repeat_index )
    {
        return repeats.get( getRepeatIndex( repeat_index ) );   
    }
    
    
    /** Returns the index of the given repeat (token) in the {@link #repeats} list. */
    public int getRepeatIndex( Token repeat_index )
    {
        int i = Integer.parseInt( repeat_index.getText() );           
        if ( i > repeats.size() || i < 1 )
        {
            throw createSyntaxException( repeat_index, 
                "Invalid repeat index, index outside bounds");
        } 
        
        return i - 1;
    }
    
    
    /** Returns the value of the given repeat bound token. */
    public int getRepeatBound( Token repeat_bound )
    {
        String token_text = repeat_bound.getText();
        int bound = -1; 
        
        if ( ! (token_text.equals("?") || token_text.equals("-1")) )
            bound = Integer.parseInt( token_text );
        
        if ( bound < -1 )
            bound = -1;
        
        return bound;
    }
    
    
    // final Residue getResidue( Token index )
    // {
    //     return getResidueTokenFor( index ).getResidue();
    // }
    
        
    final ResidueToken getResidueToken( Token index )
    {
        return residues.get( getResidueIndexFor( index ) );    
    }
    
    
    /** 
    *   Called when the start of a repeat sugar has been 
    *   encountered during the parse. 
    */
    public void repeatStarts( Token repeat_index )
    {
        int index = getRepeatIndex( repeat_index );

        if ( tracing )
            log.trace("entering repeat, index=" + index );
        
        RepeatResidueToken r = repeats.get( index );
        repeatsStack.add( r );
    }
    
    
    /** 
    *   Called when the end of a repeat sugar has been 
    *   encountered during the parse. 
    */
    public void repeatEnds( Token repeat_index )
    {
        int index = getRepeatIndex( repeat_index );

        if ( tracing )
            log.trace("exiting repeat, index=" + index );
        
        RepeatResidueToken r = repeatsStack.remove( repeatsStack.size() - 1 );
        assert r == repeats.get( index );
    }
    
   
    /** 
    *   Sets the lower and upper bounds of the range of the repeat with 
    *   the given index. 
    *   @see RepeatResidueToken
    */
    public void setRepeatRange( Token repeat_index, Token lower_bound, Token upper_bound )
    throws SequenceFormatException
    {
        //  get index of repeat
        int index = getRepeatIndex( repeat_index ); 
        
        //  lower repeat range bound        
        int lower = getRepeatBound( lower_bound );
        
        //  upper repeat range bound
        int upper = getRepeatBound( upper_bound );
        
        if ( lower != -1 && upper != -1 && lower > upper )
        {   
            throw createSyntaxException( lower_bound, 
                "Invalid repeat range, left bound must be lower than right bound"); 
        }
        
        RepeatResidueToken rt = repeats.get( index );
        rt.setRepeatRange( lower, upper );
        
        return;
    }
    
    
    /**
    *   Sets the {@link Superclass} of the last added {@link Residue} 
    *   to the value of the given {@link Token}.
    *   @see #lastResidue()
    */
    public void setSuperclass( Token superclass_tok )
    throws SequenceFormatException
    {
        String name  = superclass_tok.getText();
        
        //  superclass name is already checked by the lexer, so no
        //  try/catch or checking required here...
        Superclass s = Superclass.forName( name );
        
        ResidueToken rt = lastResidue();
        
        if ( tracing )
        {
            log.trace("setting superclass " + s + " on monosaccharide " + rt );
            traceParse( superclass_tok, "Superclass descriptor" );
        }
            
        assert rt instanceof MonosacResidueToken;
        MonosacResidueToken mrt = (MonosacResidueToken) rt;
        
        mrt.setSuperclass( s );
    }
    
    
    /** 
    *   Sets the ring closure positions of the last residue added from 
    *   the given terminii tokens. 
    */
    public void setRingClosure( Token term1, Token term2 )
    throws SequenceFormatException
    {
        int t1 = 0, t2 = 0;
        
        if ( term1.getText() == "x" )
            t1 = 0;
        else 
            t1 = Integer.parseInt( term1.getText() ); 
        
        if ( term2.getText() == "x" )
            t2 = 0;
        else 
            t2 = Integer.parseInt( term2.getText() ); 
        
        if ( t1 != 0 && t1 == t2 )
        {
            throw createSyntaxException( term1, 
                "Invalid ring closure positions: terminii cannot be equal");
        }
        
        if ( t1 > t2 )
        {
            throw createSyntaxException( term1, 
                "Invalid ring closure position: second terminus cannot be less than first");
        }
        
        ResidueToken rt = lastResidue();
        if ( tracing )
        {
            log.trace( 
                "setting ring closure positions " 
                + t1 
                + "-" 
                + t2 
                + " for residue " 
                + rt 
            );   
        }

        assert rt instanceof MonosacResidueToken;
        MonosacResidueToken mrt = (MonosacResidueToken) rt;
        
        mrt.setRingStart( t1 );
        mrt.setRingEnd( t2 );
        
        return;
    }
    
    
    /** Returns the last residue token added. */
    public ResidueToken lastResidue()
    {
        return graph.lastVertex().getValue();   
    }
    
    
    /**
    *   Expects a {@link Token} with a valid Glycoct basetype stem, ie: 
    *   text of form "a-dglc", "o-lman", "a-dgro-dgal", etc.
    *
    *   {@inheritDoc}
    */
    @Override
    protected ResidueToken createMonosaccharideToken( Token name_tok )
    throws SequenceFormatException
    {
        int i = 0;
        String name = name_tok.getText();
        
        //  extract anomer
        Anomer a = null;
        try
        {
            a = Anomer.forName( name.charAt( i ) );
            if ( a == null )
                throw new RuntimeException();
        }
        catch ( Exception ex )
        {
            SequenceFormatException sfex = createSyntaxException( 
                name_tok
                , "Invalid anomer '"
                + name.charAt(i)
                + "'; valid values are: "
                + join(", ", Anomer.values() ) 
            );
            sfex.initCause( ex ); 
            throw sfex;
        }
        
        i++;
        
        //  extract basetype(s)
        List<Basetype> basetypes = new ArrayList<Basetype>( 2 );
        while ( i < name.length() )
        {
            //  hyphen
            if ( name.charAt(i) != '-' )
            {
                throw createSyntaxException( 
                    name_tok.getColumn() + i - 1, "Expected a hyphen '-'");
            }            
        
            i = _extract_basetype( name_tok, i + 1, basetypes );
        }
        
        Basetype basetype = null;
        try
        {
            basetype = getBasetype( basetypes );
        }
        catch ( Exception ex )
        {
            SequenceFormatException sfex = createSyntaxException( 
                name_tok, ex.getMessage() );
            sfex.initCause( ex );
            throw sfex;
        }
        
        //  create monosac
        // Monosaccharide monosac = new SimpleMonosaccharide( basetype );
        // monosac.setAnomer( a );
        
        MonosacResidueToken m = new MonosacResidueToken( this, name_tok, null );
        
        m.setAnomer( a );
        m.setBasetype( basetype );
        
        return m;
    }
    
    
    /**
    *   Extracts 1 basetype from the token text, and places into passed List,
    *   returning new cursor position.
    *
    *   @param t Token that contains basetype name text
    *   @param i current index position of parse into Token t's text
    *   @param dest List that accumulates basetypes parsed from t.
    *   @return index position i after extraction of 1 basetype
    */
    private final int _extract_basetype( Token t, int i, List<Basetype> dest_list )
    throws SequenceFormatException
    {
        String name = t.getText();
        int endpos = name.indexOf('-', i );
        
        if ( endpos == -1 )
            endpos = name.length();
            
        StereoConfig stereo = null;
        try
        {
            stereo = StereoConfig.forName( name.charAt(i) );
            if ( stereo == null )
                throw new RuntimeException();
        }
        catch ( Exception ex ) 
        {
            SequenceFormatException sfex = createSyntaxException( 
                t.getColumn() + i - 1
                , "Invalid stereo-configuration for monosaccharide '"
                + name.charAt(i)
                + "'; valid values are: "
                + join(", ", StereoConfig.values() )
            );
            sfex.initCause( ex ); 
            throw sfex;
            
        }
        
        i++;
        String basetype_name = name.substring( i, endpos );
        
        CommonBasetype basetype = CommonBasetype.forName( basetype_name );
        
        if ( basetype == null )
        {
            throw createSyntaxException( 
                t.getColumn() + i - 1
                , "Unknown or invalid monosaccharide basetype '"
                + basetype_name
                + "'; see the list of permissible basetypes in class CommonBasetype"
            );
        }
        
        dest_list.add( getBasetype( stereo, basetype ) );
        
        return endpos;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Returns integer position of a linkage terminus from given {@link Token}. */
    private final int getLinkageIndexFor( Token t )
    throws SequenceFormatException
    {
        int link_index = Integer.parseInt( t.getText() );
        
        //  can't be <= 0
        if ( link_index <= 0 ) 
            throw createSyntaxException( t, "Linkage count cannot be <= 0" );
        
        //  can't be larger than 1 + the number of linkages we have
        if ( link_index > linkages.size() + 1 ) 
        {
            throw createSyntaxException( 
                t
                , "Invalid linkage index '" 
                + link_index
                + "', should be " 
                + (linkages.size() + 1) 
            );
        }
                                               
        return link_index;
    }
    
    
    /** Extract an integer residue index from a {@link Token}. */
    private final int getResidueIndexFor( Token t ) 
    throws SequenceFormatException
    {
        int index = 0;
        try 
        {  
            index = Integer.parseInt( t.getText() );  
        }
        catch ( Exception ex ) 
        {
            SequenceFormatException sfex = createSyntaxException( 
                t, "Invalid residue number: " + ex.getMessage() );
            sfex.initCause( ex ); 
            throw sfex;
        }
        
        if ( index <= 0 )
            throw createSyntaxException( t, "Residue order number cannot be 0" );
        
        if ( index > residues.size() )
        {
            throw createSyntaxException( 
                t
                , "Invalid residue number - there are only "
                + residues.size()
                + " residue(s) in the sequence"  
            );
        }
        
        return index - 1;
    }
    
    
    /** Extract an integer terminus from a {@link Token}. */
    private final int getTerminusFor( Token t ) 
    throws SequenceFormatException
    {
        int terminus = 0;
        String s = t.getText();
        
        if ( s == null || s == "?" || s == "-1" ) 
            return LinkageToken.UNKNOWN_TERMINUS;
        
        try {  terminus = Integer.parseInt( s );  }
        catch ( Exception ex ) 
        {
            SequenceFormatException sfex = createSyntaxException( 
                t, "Invalid terminal position: " + ex.getMessage() );
            sfex.initCause( ex ); 
            throw sfex;
        }
        
        if ( terminus < 1 )
        {
            throw createSyntaxException( 
                t, "Terminal position cannot be < 1" );
        }   
        
        return terminus;
    }
    
    
} // end class GlycoctParserAdapter





