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
*   Last commit: $Rev: 1552 $ by $Author: glycoslave $ on $Date:: 2009-07-20 #$  
*/

package org.eurocarbdb.sugar;

//  stdlib imports
import java.io.Serializable;
import java.util.Iterator;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.sugar.SequenceFormat;
import org.eurocarbdb.sugar.SequenceFormatException;

//  static imports


/**
*<p>
*   A very lightweight, fast and sequence format-independent 
*   abstraction of carbohydrate sequences, which includes methods to 
*   retrieve the equivalent {@link Sugar} object of this sequence, 
*   as well as methods to interconvert between different carbohydrate 
*   {@link SequenceFormat}s. 
*</p><p>
*   This class is deliberately final and immutable to keep this 
*   implementation as memory & speed efficient as possible.
*</p>
*<p>
*<strong>Usage</strong>
*<ul>
*   <li>To obtain a {@link String} sequence:<pre>
*           SugarSequence seq = ...;
*           String the_seq = seq.toString();
*   </pre>
*   </li>
*   <li>To obtain a {@link String} sequence in a different format:<pre>
*           SugarSequence seq = ...;
*           SequenceFormat format = ...;
*           String the_seq = seq.toString( format );
*   </pre>
*   </li>
*   <li>To convert a {@link String} from 1 {@link SequenceFormat} to another directly:<pre>
*           String seq1 = ...;
*           SequenceFormat from_format = ..., to_format = ...;
*           String seq2 = SugarSequence.translate( seq1, from_format, to_format );
*   </pre>
*   </li>
*</ul>
*</p>
*<p>
*    Created 19-Sep-2005.
*</p>
*
*   @see #translate(String,SequenceFormat,SequenceFormat)
*   @see SequenceFormat
*   @author mjh
*/
public final class SugarSequence implements Serializable
{
    /** logging handle */
    static Logger log = Logger.getLogger( SugarSequence.class );
    
    public static SequenceFormat 
        DEFAULT_SEQUENCE_FORMAT = SequenceFormat.Glycoct;
    
        
    //~~~~~~~~~~~~~~~~~~~~~~~ OBJECT FIELDS ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Self-explanatory, this is the raw sequence as given by 
    *   clients of this class. */
    private final String sequence;
        
    /** This is the {@link SequenceFormat sequence format} of 
    *   the given {@link sequence}. */
    private final SequenceFormat format;
    
    /** Sugar object parsed from this sequence, lazily instantiated. */
    private transient Sugar sugar;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~//
   
    /** 
    *   Constructs a SugarSequence from the given sequence string 
    *   in the currently set {@link #DEFAULT_SEQUENCE_FORMAT default format}.
    *   @see #DEFAULT_SEQUENCE_FORMAT
    *   @see #Sequence_Parser
    */
    public SugarSequence( String raw_sequence )
    throws SequenceFormatException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug(
                "new sugar sequence of " 
                + raw_sequence.length() 
                + " chars, using default format="
                + DEFAULT_SEQUENCE_FORMAT.getName()
            );
        }
        this.sequence = raw_sequence;
        this.format = DEFAULT_SEQUENCE_FORMAT;
    }
    
   
    /**
    *   Constructs a SugarSequence from the given sequence string in 
    *   the given format.
    */
    public SugarSequence( String raw_sequence, SequenceFormat format )
    throws SequenceFormatException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug(
                "new sugar sequence of " 
                + raw_sequence.length() 
                + " chars, using given format="
                + format.getName()
            );
        }
        this.sequence = raw_sequence;
        this.format = format;
    }


    /** Privileged package-access constructor. */
    SugarSequence( Sugar s ) 
    {
        this.sugar    = s;
        this.format   = DEFAULT_SEQUENCE_FORMAT;
        this.sequence = s.toString( format );
    }

    
    //~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~//   
    
    /**
    *   Convenience method for direct conversion of {@link String}
    *   glycan sequence from one {@link SequenceFormat} to another.
    *   eg:
    *<pre>
    *       String seq_carbbank = ...;
    *       SequenceFormat from_format = SequenceFormat.Carbbank;
    *       SequenceFormat to_format = SequenceFormat.Glycoct;
    *       String seq_glycoct = SugarSequence.translate( seq_carbbank, from_format1, to_format );
    *</pre>
    *   @throws SequenceFormatException if passed sequence does not 
    *   syntactically conform to the passed format.
    */  
    public static String translate( String sequence,
                                    SequenceFormat from_format, 
                                    SequenceFormat to_format )
    throws SequenceFormatException
    {
        SugarSequence sseq = new SugarSequence( sequence, from_format );
        return sseq.toString( to_format );
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~//    
    
    /*  @see Sequence#getSequenceFormat()  */
    public SequenceFormat getSequenceFormat()
    {
        return format;   
    }
    
    
    /**
    *   Returns a parsed Sugar object representing this sugar sequence.
    *   This Sugar object is lazily instantiated to keep this sequence 
    *   class implementation as lightweight as possible.
    *
    *   @throws SequenceFormatException if the original raw sequence
    *           passed on instantiation is syntactically incorrect.
    */
    public Sugar getSugar() throws SequenceFormatException
    {
        if ( sugar == null ) 
            sugar = format.getSugar( sequence );
        
        return sugar;
    }
    
    
    /** 
    *   Returns true only if the sequence string given on 
    *   instantiation is syntactically valid.
    */
    public boolean isValid()
    {
        try 
        { 
            getSugar();                     
            return true;   
        }
        catch ( SequenceFormatException e ) 
        {  
            return false;  
        }
    }
    
    
    /*  @see Sequence#iterator()  */
    public Iterator<Residue> iterator()
    {
        return getSugar().iterator();   
    }
    
    
    /*  @see Sequence#toString()  */
    public String toString()
    {
        return this.toString( DEFAULT_SEQUENCE_FORMAT ); 
    }
    
    
    /**
    *   Returns this sequence in the passed {@link SequenceFormat},
    *   interconverting between formats as necessary.
    *
    *   @throws SequenceFormatException if there is a problem
    *           parsing the sequence or during conversion. 
    */
    public String toString( SequenceFormat format )
    throws SequenceFormatException
    {
        if ( format == this.format )
            return sequence;
        
        else return format.getSequence( this.getSugar() ); 
    }
    
} // end class



