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


package org.eurocarbdb.sugar.seq.grammar;

//  stdlib imports
import java.util.Stack;

//  3rd party imports
import antlr.Token;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import org.apache.log4j.Logger;

//  eurocarb imports 
import org.eurocarbdb.sugar.*;
import org.eurocarbdb.util.graph.*;

import org.eurocarbdb.sugar.seq.grammar.ResidueToken;
import org.eurocarbdb.sugar.seq.grammar.LinkageToken;


/*  class IupacParserAdaptor  *//************************************
*<p>
*   This class is a wrapper around the ParserAdaptor class to implement
*   a state machine for parsing the Iupac sequence format from left to
*   right. 
*</p>
*<p>
*   The implicit natural structure of the Iupac format is right
*   (root) to left (leaves), so parsing left to right we build up a kind
*   of state machine, buffering residues and linkages until their attachment 
*   points can be fully determined.
*</p>
*<p>
*   At the most basic level, the parser works by accumulating residue-linkage 
*   pairs (the addLinkedResidue method), and marking branch start- and end-
*   points via the branchStart() and branchEnd() methods. 
*</p>
*<p>
*   Note that all declared methods of this class are final in order to
*   maximise inlineability & parsing speed.
*</p>
*   @see iupac_grammar.g
*   @author mjh <glycoslave@gmail.com>
*/
public abstract class IupacParserAdaptor extends ParserAdaptor
{

    //~~~~~~~~~~~~~~~~~~~~~  STATIC FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~//

    /** Logging instance. */
    static final Logger log = Logger.getLogger( IupacParserAdaptor.class );
       
    static final boolean DEBUGGING = log.isDebugEnabled();
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Branch stack buffer for residue tokens. Each element of this 
    *   stack represents a residue that is an end point of a buffered 
    *   branch.  */
    protected Stack<ResidueToken> residueBuffer = new Stack<ResidueToken>();
    
    /** Branch stack buffer for linkage tokens. Each element of this 
    *   stack represents a linkage that is an end point of a buffered 
    *   branch. Each linkage token in this stack matches a residue in
    *   residueBuffer (ie: it is that residue's reducing terminal linkage).  
    */
    protected Stack<LinkageToken> linkageBuffer = new Stack<LinkageToken>();

    /** Current residue token cursor. */
    protected ResidueToken lastResidue;

    /** Current linkage token cursor. */
    protected LinkageToken lastLinkage;

    /** Indicates that the currently parsed branch has just been closed. */
    private boolean branch_just_ended = false;

    /** The integer maintains a running count of how deeply nested we are
    *   in branches. let i == branch_depth. when i > 0, we are in the i'th 
    *   nested branch; when i == 0 we are parsing along the "main" sugar
    *   branch (ie: not in a nested branch). if i < 0 then that is a bug :-( */
    private int branch_depth = 0;


    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /*  pointlessly inherited constructors, stupid java  */
    public IupacParserAdaptor( int k ) { super( k ); }
    public IupacParserAdaptor( ParserSharedInputState state, int k ) { super( state, k ); }
    public IupacParserAdaptor( TokenBuffer buffer, int k ) { super( buffer, k ); }
    public IupacParserAdaptor( TokenStream stream, int k ) { super( stream, k ); }

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//        
    
    /*  addLinkedResidue  *//****************************************
    *<p>
    *   Add a just-parsed {@link Residue}-{@link Linkage} pair to the 
    *   growing sugar {@link Graph}. Note that this does not necessarily 
    *   mean that the residue/linkage is added immediately, as the parse 
    *   is still in progress.
    *</p><p>
    *   In short, the passed {@link ResidueToken} rt will be added immediately
    *   to the graph if it is not a leaf residue, whereas all passed 
    *   {@link LinkageToken}s will never be added immediately, since we will
    *   always have to continue parsing to get the residue to which 
    *   it's bound.
    *</p>
    */
    protected final void addLinkedResidue( ResidueToken rt, LinkageToken lt ) 
    throws SemanticException
    {
        //  add this incoming residue to the graph immediately; worry
        //  about connecting it later.
        addResidue( rt );

        if ( lastResidue != null && lastLinkage != null )
        {
            //  ok, there is a residue & linkage cached from a previous invocation 
            //  of this method, so we can go ahead and connect rt & lastResidue
            //  by lastLinkage.
            if ( DEBUGGING )
                log.debug("adding linkage " + lastLinkage + " to child=" + lastResidue );
            
            addLinkage( rt, lastLinkage, lastResidue );
        }
        
        //  if a branch just ended then attach associated saved/buffered 
        //  branches, if any.
        if ( branch_just_ended ) 
            attachSavedBranchesTo( rt );

        //  unconditionally update residue/linkage cursors.
        lastResidue = rt;
        lastLinkage = lt;
    }


    /*  addRootResidue  *//******************************************
    *
    *   Adds the penultimate sugar root residue, effectively finalising
    *   the parsing of the current sugar (since in Iupac format the root
    *   monosaccharide is parsed last when parsing from left to right).
    */
    protected final void addRootResidue( ResidueToken rt ) 
    throws SemanticException
    {
        if ( DEBUGGING )
            log.debug("adding final (root) residue " + rt );
        
        addResidue( rt );

        setRootResidue( rt );
        
        if ( lastLinkage != null )
            addLinkage( rt, lastLinkage, lastResidue );
        // addLinkage( lastResidue, lastLinkage, rt );
        
        if ( branch_just_ended ) 
            attachSavedBranchesTo( rt );
    }
    

    /*  branchStarts  *//********************************************
    *
    *   Indicates that we have just encountered the start of a new branch.
    *   Internally, this means the last-encountered residue and linkage
    *   pair are pushed to their respective Stack buffers to be popped
    *   when the end of this branch is encountered.
    */
    protected final void branchStarts() 
    {
        if ( DEBUGGING )
            log.debug( "new branch starting" ); 
        
        branch_depth++;

        //  this is a special case where a branch closes right
        //  next to where a new branch opens.
        if ( branch_just_ended )
            branch_just_ended = false;
    
        if ( DEBUGGING )
        {
            log.debug( "saving the branch ending with residue=" 
                     + lastResidue 
                     + ", linkage="
                     + lastLinkage
                     + ", branch depth = "
                     + branch_depth
                     );
        }
         
        residueBuffer.push( lastResidue );
        linkageBuffer.push( lastLinkage );
        lastResidue = null;
        lastLinkage = null;
    }
    
    
    /*  branchEnds  *//**********************************************
    *
    *   Indicates that we have just encountered the end of a branch.
    *   Internally, this method only marks that the most recent residue/linkage
    *   in our residue/linkage stack need to be added to whatever is 
    *   the next parsed residue.
    */
    protected final void branchEnds() 
    {
        if ( DEBUGGING )
            log.debug( "marking end of branch" );
        
        branch_just_ended = true;
        branch_depth--;
    }

    
    /** Returns {@link SequenceFormat#Iupac}. */
    @Override
    public final SequenceFormat getSequenceFormat()
    {
        return SequenceFormat.Iupac;   
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~//        

    /*  attachSavedBranchesTo  *//****************************************
    *
    *   This method adds saved linkage/residue pairs from the 
    *   residue/linkage stacks and adds them to the passed Residue.
    *   The number of branches to add is equal to the current 
    *   residue/linkage buffer size minus the current branch depth.
    */
    private final void attachSavedBranchesTo( ResidueToken rt ) 
    throws SemanticException
    {
        //  the number of branches that need to be added is equal to 
        //  the current stack size minus (the number of branch opens 
        //  minus the number of branch opens).
        int branches_to_add = residueBuffer.size() - branch_depth;

        //  this value should never be < 0, if it is, it's a bug.
        if ( branches_to_add <= 0 ) 
            throw new RuntimeException("BUG! THIS SHOULDN'T HAPPEN!!!");

        if ( DEBUGGING )
            log.debug("adding " + branches_to_add + " saved branch(es):");
        
        while ( branches_to_add-- > 0 )
        {
            lastResidue = residueBuffer.pop();
            lastLinkage = linkageBuffer.pop(); 
            
            if ( DEBUGGING )
            {
                log.debug( 
                    "connecting the saved branch that ends in residue=" 
                    + lastResidue 
                    + ", linkage="
                    + lastLinkage 
                );
            }
            
            // addLinkage( lastResidue, lastLinkage, rt );
            addLinkage( rt, lastLinkage, lastResidue );

            branch_just_ended = false;
        }
    }

} // end of class


