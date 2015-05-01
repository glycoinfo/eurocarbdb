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
*   Last commit: $Rev: 1499 $ by $Author: glycoslave $ on $Date:: 2009-07-14 #$  
*/


package org.eurocarbdb.sugar.seq.grammar;


//  stdlib imports
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

//  3rd party imports
import org.apache.log4j.Logger;

import antlr.Token;
import antlr.SemanticException;

//  eurocarb imports
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Substituent;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.SequenceFormatException;

import org.eurocarbdb.sugar.Anomer;
import org.eurocarbdb.sugar.Basetype;
import org.eurocarbdb.sugar.Superclass;
import org.eurocarbdb.sugar.RingConformation;
import org.eurocarbdb.sugar.SugarException;
import org.eurocarbdb.sugar.CommonSubstituent;

import org.eurocarbdb.sugar.impl.SimpleMonosaccharide;

//  static imports
import static org.eurocarbdb.sugar.Basetypes.getNormalisedBasetype;
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.sugar.CommonSubstituent.Deoxy;
import static org.eurocarbdb.sugar.CommonSubstituent.En;


/*  class MonosacResidueToken  *//************************************** 
*
*   Sub-class of {@link ResidueToken} to encapsulate information about
*   a parsed {@link Monosaccharide}.
*
*   @author mjh 
*/
public class MonosacResidueToken extends ResidueToken
{
    //~~~~~~~~~~~~~~~~~~~~~  STATIC FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Logging instance. */
    public static final Logger log = Logger.getLogger( MonosacResidueToken.class );
    

    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    protected Anomer anomer;
    
    protected Basetype basetype;
    
    protected Superclass superclass;
    
    protected RingConformation conformation;
    
    protected List<Substituent> substituents = null;
    
    protected int ringStart = -1;
    
    protected int ringEnd = -1;
    
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /** 
    *   Constructor. 
    *
    *   @param parser   The parser generating this token
    *   @param tok      The ANTLR token being wrapped.
    *   @param r        A pre-constructed {@link Residue}
    */
    public MonosacResidueToken( ParserAdaptor parser, Token tok, Residue r )
    {
        super( parser, tok, r );
    }

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /*  getResidue  *//**********************************************
    * 
    *   Always returns a {@link Monosaccharide}.
    */
    public Residue getResidue() throws SequenceFormatException
    {
        if ( residue != null )
            return residue;
        
        assert basetype != null;
        
        if ( substituents != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug(
                    "normalising basetype " 
                    + basetype 
                    + " against substituents: " 
                    + substituents 
                );
            }
            try
            {
                //  hack for glycoct residues like 'Ara-HEX|2-keto', where C1
                //  could be taken for either a (second) Carbonyl or a hydroxl
                if ( basetype.getSuperclass() != this.superclass 
                    && substituents.contains( CommonSubstituent.Carbonyl ) 
                    && substituents.get(0) == null )
                {
                    //  hint to the normalise routine that the basetypes
                    //  like the example above should be normalised to
                    //  'Fru'.
                    substituents.set( 0, CommonSubstituent.OH );
                }
            
                basetype = getNormalisedBasetype( basetype, substituents );
            }
            catch ( Exception ex )
            {
                SequenceFormatException sfex 
                    = parser.createSyntaxException( this.token, ex.getMessage() );
                sfex.initCause( ex ); 
                throw sfex;
            }
        }
            
        Monosaccharide monosac = null; 
        
        try
        {
            monosac = new SimpleMonosaccharide( basetype );
            
            if ( substituents != null )
                for ( int i = 0; i < substituents.size(); i++ )
                    if ( substituents.get(i) != null )
                        monosac.attach( substituents.get(i), i+1 );
        }
        catch ( Exception ex )
        {
            SequenceFormatException sfex 
                = parser.createSyntaxException( this.token, ex.getMessage() );
            sfex.initCause( ex ); 
            throw sfex;
        }
        
        if ( anomer != null )
            monosac.setAnomer( anomer );
        
        if ( conformation != null )
            monosac.setRingConformation( conformation );
        
        this.residue = monosac;
        
        return monosac;
    }
    
    
    public void setAnomer( Anomer a ) {  anomer = a;  }
    
    
    public void setBasetype( Basetype b ) {  basetype = b;  }
    
    
    public void setSuperclass( Superclass s ) {  superclass = s;  }
    
    
    public void setRingConformation( RingConformation rc ) 
    {  
        conformation = rc;
        if ( residue != null )
            ((Monosaccharide) residue).setRingConformation( rc );
    }
    
    
    public void setSubstituent( Substituent s, int position ) 
    throws IllegalArgumentException
    {   
        if ( substituents == null )
        {
            if ( superclass != null )
                substituents = Arrays.asList( new Substituent[ superclass.size() ] );
            else if ( basetype != null )
                substituents = Arrays.asList( new Substituent[ basetype.getSuperclass().size() ] );
            else 
                assert false;
        }
        
        if ( position < 1 )
            throw new IllegalArgumentException(
                "position cannot be less than 1");
            
        if ( position > substituents.size() )
            throw new IllegalArgumentException(
                "position cannot be greater than monosaccharide size");

        // if ( position == ringStart )
        //     throw new IllegalArgumentException(
        //         "position cannot be equal to ring start position");
        
        if ( position == ringEnd )
            throw new IllegalArgumentException(
                "position cannot be equal to ring end position");
            
        // substituents[position - 1] = s
        int i = position - 1;
        if ( substituents.get( i ) != null )
        {
            if ( s == Deoxy && substituents.get( i ) == En )
                return;
            
            log.warn(
                "displacing existing substituent " 
                + s
                + " at position "
                + position
                + " with substituent "
                + substituents.get( i )
            );
        }
        substituents.set( i, s );
    }
    
    
    public void setRingStart( int pos ) 
    {  
        ringStart = pos;
        _check_ring();
    }
    
    
    public void setRingEnd( int pos ) 
    {  
        ringEnd = pos;  
        _check_ring();
    }
    
    
    private final void _check_ring()
    {
        if ( ringStart > 0 && ringEnd > 0 )
        {
            assert ringEnd > ringStart; 
            setRingConformation( 
                RingConformation.forRingPositions( ringStart, ringEnd ) );   
        }
    }
    
    // /** Returns this token's text. */
    // public String toString() 
    // {  
    //     return 
    //         "<token: text="
    //         + token.getText()
    //         + ", residue="
    //         + ( residue != null 
    //             ? residue.toString()
    //             : "null" )
    //         + ">"
    //         // + "], line="
    //         // // + token.getText()
    //         // + token.getLine()
    //         // + ", col="
    //         // + token.getColumn()
    //         // + ">"
    //         ;  
    // }
        
} // end class MonosacResidueToken
