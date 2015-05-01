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

import java.util.List;
import java.util.ArrayList;
import antlr.Token;

//  eurocarb imports
import org.eurocarbdb.sugar.Anomer;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Linkage;
import org.eurocarbdb.sugar.GlycosidicLinkage;
import org.eurocarbdb.sugar.SequenceFormatException;


/*  class LinkageToken  *//************************************** 
*
*   Subclass of ANTLR.Token for linkages.
*/
public class LinkageToken extends Token
{
    //~~~~~~~~~~~~~~~~~~~~~  STATIC FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~//

    /** A constant indicating that a terminus position is not known. */
    public static final int UNKNOWN_TERMINUS = 0;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** An anomeric configuration token. Optionally populated - 
    *   depends on whether the sequence format associates anomer 
    *   with linkage or with residue. */
    Token anomerToken;
    
    /** The token for the residue position on the reducing terminus side, ie: the parent. */
    Token parentToken;
    
    /** The token for the residue position on the non-reducing terminus side, ie: the child. */
    Token childToken;
    
    /** Reference to the sequence string we are parsing, in order 
    *   that we can generate informative sequence format errors. */
    final String sequence;
    
    /** The linkage object that will be created from this LinkageToken. */
    private Linkage linkage;
    
/*    
    /-** A list of possible multi-connections, only populated if 
    *   this linkage has multiple connections between the same pair 
    *   of residues (rare).  *-/
    private List<LinkageToken> multilinks = null;
*/  
    
    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Regular constructor. */
    public LinkageToken( String seq, Token anomer_tok, Token parent_tok, Token child_tok )
    {
        anomerToken = anomer_tok;
        parentToken  = parent_tok;
        childToken = child_tok;
        sequence    = seq;
        
        //  parse linkage elements into an actual linkage object.
        Anomer anomer;
        int parent  = UNKNOWN_TERMINUS;
        int child = UNKNOWN_TERMINUS;
        Token t = null;
        
        try
        {
            //  first, the anomer
            t = anomerToken;
            anomer  = ( anomerToken == null ) 
                    ? Anomer.None
                    : Anomer.forName( anomerToken.getText() );

            //  then, reducing terminus
            t = parentToken;
            parent   = ( parentToken == null ) 
                    ? UNKNOWN_TERMINUS
                    : Integer.parseInt( parentToken.getText() );


            //  non-reducing terminus
            t = childToken;
            child  = ( childToken == null ) 
                    ? UNKNOWN_TERMINUS
                    : Integer.parseInt( childToken.getText() );
        }
        catch ( Exception e )
        {
            throw new SequenceFormatException( 
                sequence, t.getColumn(), e.getMessage() );
        }

        linkage = new GlycosidicLinkage( anomer, parent, child ); 
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//        

/*
    /-**
    *   Adds an additional linkage to the linkage(s) already encapsulated
    *   by this LinkageToken, thereby creating a multi-connected linkage
    *   (if not already).
    *-/
    public void addMulticonnectingLinkage( LinkageToken lt )
    {
        if ( multilinks == null )
            multilinks = new ArrayList<LinkageToken>();
            
        multilinks.add( lt );
    }
*/

    /**
    *   Returns the linkage object corresponding to this linkage token.
    */
    public Linkage getLinkage()
    {
        return linkage;
    }
    
    
    private int leftCol = 0;
    private int rightCol = 0;
    
    public void setLeftColumn( int col ) 
    {
        assert col >= 0;
        leftCol = col; 
    }
    
    public int getLeftColumn() 
    {
        return leftCol;
    }
    
    public void setRightColumn( int col ) 
    {
        assert col >= 0;
        rightCol = col; 
    }
    
    public int getRightColumn() 
    {
        return rightCol;
    }

    
    /**
    *   Returns a stringified version of this token, mainly useful
    *   for debugging.
    */
    public String toString() 
    {  
        return 
            getClass().getSimpleName()
            + "="
            + linkage
            ;
        
        
            // "<token linkage="
            // + linkage.toString()
            // + ">"
            // ;
            
             /*
             //"<linkage token: anomer=" 
             (anomerToken != null ? anomerToken.getText() : "")
             //+ " (col="
             //+ anomerToken.getColumn()
             //+ "), parent="
             + (parentToken != null ? parentToken.getText() : "")
             //+ " (col="
             //+ parentToken.getColumn()
             //+ "), child="
             + "-"
             + (childToken != null ? childToken.getText() : "")
             //+ " (col="
             //+ childToken.getColumn()
             //+ ")>"
             ;
             */
    }

} // end class LinkageToken

