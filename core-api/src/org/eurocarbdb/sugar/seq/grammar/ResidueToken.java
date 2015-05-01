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

//  3rd party imports
import org.apache.log4j.Logger;

import antlr.Token;
import antlr.SemanticException;

//  eurocarb imports
import org.eurocarbdb.sugar.Anomer;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.CommonSubstituent;
import org.eurocarbdb.sugar.SimpleSubstituent;
import org.eurocarbdb.sugar.SequenceFormatException;
import org.eurocarbdb.sugar.impl.ComplexMonosaccharide;
import org.eurocarbdb.sugar.impl.SimpleMonosaccharide;
import org.eurocarbdb.sugar.Substituents;

//  static imports


/*  class ResidueToken  *//************************************** 
*
*   Sub-interface of {@link antlr.Token} for residues.
*
*   @author mjh 
*/
public class ResidueToken extends Token
{
    //~~~~~~~~~~~~~~~~~~~~~  STATIC FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Logging instance. */
    public static final Logger log = Logger.getLogger( ResidueToken.class );
    

    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** The anomeric configuration of the residue represented by this
    *   token, or null if this is not applicable to this residue type. */
    public Token anomer = null;

    /** The ANTLR token object that contains the text for this residue. */
    protected Token token;
    
    /** The residue created from this token. */
    protected Residue residue;
    
    /** Reference to the string from which the token was parsed. */
    protected String glycanSequence;
    
    /** Reference to the parser from which this {@link Token} was emitted. */
    protected ParserAdaptor parser;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    // /** 
    // *   Constructor. 
    // *   @param parser   The parser generating this token
    // *   @param tok      The ANTLR token being wrapped.
    // */
    // public ResidueToken( ParserAdaptor parser, Token tok )
    // {
    //     this.glycanSequence = parser.getSequence();
    //     this.parser = parser;
    //     this.token = tok;
        
    //     //  init residue at construction time.
    //     getResidue();
    // }
    
    
    /** 
    *   Constructor. 
    *   @param parser   The parser generating this token
    *   @param tok      The ANTLR token being wrapped.
    *   @param r        A pre-constructed {@link Residue}
    */
    public ResidueToken( ParserAdaptor parser, Token tok, Residue r )
    {
        this.glycanSequence = parser.getSequence();
        this.parser = parser;
        this.token = tok;

        this.residue = r;        
    }

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /*  getResidue  *//**********************************************
    * 
    *   Creates a new Residue (Monosaccharide or Substituent, as 
    *   appropriate) from this token, throwing an exception if the 
    *   monosaccharide name is invalid.
    */
    public Residue getResidue() throws SequenceFormatException
    {
        // if ( residue != null ) 
        return residue;
    
        // String residue_name = token.getText();

        // //  note to self: this code sucks, feels like it belongs 
        // //  somewhere more central...
        
        // //  try making a monosaccharide 
        // try 
        // {
        //     residue = SimpleMonosaccharide.forName( residue_name );
        //     if ( residue != null )
        //         return residue;
        // }
        // catch ( SequenceFormatException ex ) {}
            
        // try 
        // {
        //     residue = ComplexMonosaccharide.forName( residue_name );
        //     if ( residue != null )
        //         return residue;
        // }
        // catch ( SequenceFormatException ex ) {}
            
        // //  a substituent perhaps?
        // try 
        // {
        //     //  todo: REPLACE THIS CRAP WITH CALLS TO METHODS ON PARSER SUBCLASSES
        //     //  and maybe generify ResidueToken?
        //     residue = Substituents.getSubstituent( residue_name );
        //     if ( residue != null )
        //         return residue;
        // }
        // catch ( SequenceFormatException ex ) {}
            

        // // try {  residue = parser.getSequenceFormat().parseResidue( residue_name );  }
        // // catch ( Exception ex )
        // // {
        
        // //  shouldn't get here normally, as syntactic errors should have 
        // //  already been thrown by the lexing/parsing.
        // throw new SequenceFormatException(
        //     glycanSequence,
        //     token.getColumn() - 1,
        //     token.getColumn() + residue_name.length() - 2,
        //     "Unrecognised residue name"
        // );
        // // }
    }
    
    
    public void setResidue( Residue r )
    {
        log.debug("residue manually set to: " + r );
        this.residue = r;
    }
    
    
    /** 
    *   Returns the column number of the start of this residue token
    *   in the source text. 
    */
    public int getColumn() {  return token.getColumn();  }

    
    /**
    *   Returns the line number of this residue token in the source text.
    */
    public int getLine()   {  return token.getLine();  }

    
    /**
    *   Returns the filename of the source text from which this 
    *   token was parsed.
    */
    public String getFilename() {  return token.getFilename();  }

    
    /** Returns this token's text. */
    public String getText() {  return token.getText();  }
    
    
    /** Returns this token's text. */
    public String toString() 
    {  
        return 
            getClass().getSimpleName()
            + "="
            + token.getText()
            ;
            
            // "<token: text="
            // + token.getText()
            // + ", residue="
            // + ( residue != null 
            //     ? residue.toString()
            //     : "null" )
            // + ">"
            // ;  
            
            // + "], line="
            // // + token.getText()
            // + token.getLine()
            // + ", col="
            // + token.getColumn()
            // + ">"
            // ;  
    }
        
} // end class ResidueToken
