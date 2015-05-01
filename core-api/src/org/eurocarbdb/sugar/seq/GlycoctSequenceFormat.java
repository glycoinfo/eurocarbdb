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
*   Last commit: $Rev: 1560 $ by $Author: glycoslave $ on $Date:: 2009-07-21 #$  
*/

package org.eurocarbdb.sugar.seq;

import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;

import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Substituent;
import org.eurocarbdb.sugar.Substituents;
import org.eurocarbdb.sugar.Modification;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.sugar.SequenceFormat;
import org.eurocarbdb.sugar.CommonSubstituent;
import org.eurocarbdb.sugar.SequenceFormatException;

import org.eurocarbdb.sugar.impl.ComplexMonosaccharide;

import org.eurocarbdb.sugar.seq.grammar.GlycoctLexer;
import org.eurocarbdb.sugar.seq.grammar.GlycoctParser;
import org.eurocarbdb.sugar.seq.grammar.ParserAdaptor;
import org.eurocarbdb.sugar.seq.grammar.GlycoctParserAdaptor;

import static org.eurocarbdb.util.StringUtils.join;


/*  class GlycoctSequenceFormat  *//*********************************
*
*   Implements parsing and generation of carbohydrate sequences in 
*   GlycoCT format.
*
*   @author mjh
*   @see GlycoctParser
*   @see GlycoctLexer
*   @see GlycoctParserAdaptor
*   @see SugarSequence
*   @see Sugar
*/
public class GlycoctSequenceFormat implements SequenceFormat
{   
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Logging handle. */
    static final Logger log = Logger.getLogger( GlycoctSequenceFormat.class );
    
    
    public static final String UNKNOWN_RING_POSITION = "x";

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    //~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    //~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** 
    *   Returns the GlycoCT sequence for the passed Monosaccharide. 
    *   @see #getSequence(Monosaccharide) 
    *
    *   @deprecated use {@link #getSequence(Monosaccharide)}
    */
    @Deprecated
    public static String getGlycoCTName( Monosaccharide m )
    {
        return new GlycoctSequenceFormat().getSequence( m );   
    }
    
    
    //  (no constructors)
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    

    /*  getName  *//*************************************************  
    *
    *   Returns "GlycoCT", the name of this format.
    *
    *   @see SequenceFormat#getName()  
    */
    public String getName() {  return "GlycoCT";  }


    /*  getSugar  *//******************************************* 
    *
    *   Parses a GlycoCT sequence string into a Sugar object.
    *
    *   @see SequenceFormat.getSugar(String) 
    */
    public Sugar getSugar( String sequence ) throws SequenceFormatException
    {
        log.debug("note: replacing unknown terminii ('-1', 'x') with '?', and linearising sequence...");
       
        String tmp = sequence;
        tmp = tmp.replace("x:x", "0:0");
        tmp = tmp.replace('\n', ' ');
        tmp = tmp.replace("(-1", "(?");
        tmp = tmp.replace("--1)", "-?)");
        
        // assert sequence.length() == tmp.length();
        sequence = tmp;
        
        GlycoctLexer   lexer = new GlycoctLexer( new StringReader( sequence ) );
        GlycoctParser parser = new GlycoctParser( lexer );

        ParserAdaptor.performParse( parser, sequence );
        
        return parser.getSugar();
    }

    
    /**
    *   Parses {@link Monosaccharide}s sequences in GlycoCT format, ie:
    *   monosaccharide names of the form "a-dman", "b-dglc", "a-dgro-dgal".
    */
    public Monosaccharide getMonosaccharide( String seq ) 
    throws SequenceFormatException
    {
        if ( seq == null || seq.length() == 0 )
            throw new IllegalArgumentException(
                "Monosaccharide name can't be null or zero-length");

        log.warn("this method is currently returning null");
        return null;
    }

    
    public Substituent getSubstituent( String seq ) throws SequenceFormatException
    {
        // throw new UnsupportedOperationException( "NOT YET IMPLEMENTED" );
        // return CommonSubstituent.forName( seq );
        return Substituents.getSubstituent( seq );
    }
    

    /*  getSequence  *//*****************************************
    *
    *   Produces a GlycoCT sequence for the passed Sugar.
    *   @see SequenceFormat.getSugar(Sugar) 
    */
    public String getSequence( Sugar s )
    {
        // TODO: Rene's exporter code would go here!
        throw new UnsupportedOperationException( 
            "NOT YET IMPLEMENTED" ); 
    }
    
    
    /*  getSequence  *//*****************************************
    *
    *   Temporary 'bridging' method to support Rene's Sugar   
    *
    */
    public String getSequence( org.eurocarbdb.MolecularFramework.sugar.Sugar s )
    {
        org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCTCondensed 
            objGlycoCTCon = new org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCTCondensed();
        
        String seq = null;
        try
        {
            objGlycoCTCon.start(s);
            seq = objGlycoCTCon.getHashCode();
        }
        catch (org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException e )
        {
            log.warn( "failed to calculate condensed glyco ct: "
                    + e.getMessage()
                    , e
                    );
            
            seq = null;
        }
        
        return seq;
    }
    
    
    public String getSequence( Monosaccharide ms )    
    {
        ComplexMonosaccharide m = (ms instanceof ComplexMonosaccharide) 
                                ? (ComplexMonosaccharide) ms
                                : new ComplexMonosaccharide( ms );
                                
        String basetype = m.getAnomer().getSymbol()
                        + "-" 
                        + m.getStereoConfig()
                        + m.getBasetype().getName()
                        ;
        
        String ringstart = ( m.getRingStart() == -1 )
                         ? UNKNOWN_RING_POSITION
                         : String.valueOf( m.getRingStart() )
                         ;

        String ringend = ( m.getRingEnd() == -1 )   
                       ? UNKNOWN_RING_POSITION
                       : String.valueOf( m.getRingEnd() )
                       ;
                         
        String modificationList = getSequence( m.getModifications() ); 
        
        String name = join( "-", basetype, 
                                 m.getSuperclass(),
                                 ringstart + ":" + ringend );
        
        return ( modificationList.length() > 0 )
            ?   name + modificationList
            :   name
            ;
    }
    
    
    /** 
    *   Returns a string representing the passed Modification in
    *   GlycoCT format.
    */
    public String getSequence( Modification m )
    {
        if ( m.hasPositionTwo() )
        {
            return m.getPositionOne()
                 + ","
                 + m.getPositionTwo()
                 + ":"
                 + m.getName()
                 ;  
        }
        else 
        {
            return m.getPositionOne()
                 + ":"
                 + m.getName();  
        }
    }
    
    
    /** 
    *   Returns a string representing the passed list of 
    *   Substituents in GlycoCT format.
    */
    String getSequence( List<Modification> modifications )
    {
        if ( modifications == null || modifications.size() == 0 ) return "";
        
        List<String> mod_name_list 
            = new ArrayList<String>( modifications.size() );
        
        for ( Modification m : modifications )
            mod_name_list.add( getSequence( m ) );
        
        //  mjh: terminus position is the first character of the generated 
        //  mod name, therefore we can take advantage of the built-in
        //  comparator for sorting strings instead of writing our own :-)
        Collections.sort( mod_name_list );
        
        return "|" + join( "|", mod_name_list );
    }
    
    
    /** {@inheritDoc}  @see SequenceFormat#getSequence(Residue)  */
    public String getSequence( Residue r )
    {
        return r.getName();
    }


    /**
    *   TODO for GlycoCT
    */
    public String getSequence( Substituent s )
    {
        return s.getName();
    }

    
} // end class


