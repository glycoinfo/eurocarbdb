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
*   Last commit: $Rev: 1231 $ by $Author: glycoslave $ on $Date:: 2009-06-19 #$  
*/

package org.eurocarbdb.sugar.seq;
    
//  stdlib imports
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.sugar.SequenceFormat;
import org.eurocarbdb.sugar.SequenceFormatException;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.Substituent;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Sugar;

//  static imports
import static org.eurocarbdb.util.StringUtils.join;


/**
*<p>
*   Convenience class for combining multiple {@link SequenceFormat}s
*   into a single, virtual SequenceFormat that will trial all
*   {@link String} sequences against an internal {@link List} of
*   SequenceFormats. 
*</p>
*<p>
*   When asked to parse a String sequence, this parser will try
*   to parse the given sequence using each of the sequence formats
*   given by {@link #getSupportedFormats()} in turn until a parse
*   is successful (ie: returns a non-null {@link Sugar} object),
*   or until the list of formats is exhausted, in which case it
*   throws a {@link SequenceFormatException}.
*</p>
*<p>
*   The production of String sequences from {@link Residue}s, 
*   {@link Monosaccharide}s, {@link Substituent}s and {@link Sugar}s
*   will use the SequenceFormat returned by the method 
*   {@link #getOutputFormat}.
*</p>
*
*   @author mjh
*/
public class MultiSequenceFormat implements SequenceFormat
{
    /** logging handle */
    static Logger log = Logger.getLogger( MultiSequenceFormat.class );

    /** The output SequenceFormat to use, default to first format in 
    *   list of given formats. */
    private SequenceFormat outputFormat;
    
    /** List of formats this instance will use to try to parse sequences. */
    private List<SequenceFormat> knownFormats = new ArrayList<SequenceFormat>( 8 ); 

    
    /** 
    *   Constructor in which all given formats will be added to the 
    *   {@link #getSupportedFormats list of supported formats}, with the
    *   default output format defaulting to the first format in the list.
    */
    public MultiSequenceFormat( SequenceFormat... formats )
    {
        for ( SequenceFormat f : formats )
            if ( f != null )
                knownFormats.add( f );
            
        outputFormat = knownFormats.get( 0 );
    }
    
    
    /** 
    *   Constructor in which all given formats will be added to the 
    *   {@link #getSupportedFormats list of supported formats}. 
    */
    public MultiSequenceFormat( Collection<SequenceFormat> formats )
    {
        knownFormats.addAll( formats );
        outputFormat = knownFormats.get( 0 );
    }
    
    
    /*  see SequenceFormat.getName  */
    public String getName() 
    {  
        return "[SequenceFormat: any of "
            + knownFormats.toString()
            + "]"; 
    }
    
    
    /** 
    *   Returns the {@link SequenceFormat} that will be used to generate 
    *   any sequences via the getSequence( Xxx ) methods. 
    */
    public SequenceFormat getOutputFormat()
    {
        return this.outputFormat;   
    }
    
    
    /** 
    *   Sets the {@link SequenceFormat} that will be used to generate 
    *   any sequences via the getSequence( Xxx ) methods. 
    */
    public void setOutputFormat( SequenceFormat f )
    {
        this.outputFormat = f;   
    }
    
    
    /** 
    *   Returns a modifiable list of sequence formats that are supported
    *   by this multi-parser. 
    */
    public List<SequenceFormat> getSupportedFormats() {  return knownFormats;  }
    
    
    /** 
    *   Attempts to parse {@link String} sequence using any of this 
    *   instances {@link #getSupportedFormats supported sequence formats}.
    *   
    *   @see SequenceFormat.getSugar(String)  
    */
    public Sugar getSugar( String seq ) throws SequenceFormatException
    {   
        Sugar sugar = null;
        for ( SequenceFormat format : knownFormats )
        {
            try 
            {  
                sugar = format.getSugar( seq );  
                if ( sugar != null ) 
                    return sugar;
            }
            catch ( SequenceFormatException sfe )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug("attempt to parse sequence with format " 
                        + format.getName()
                        + " failed: "
                        + sfe.getMessage() 
                    );
                }
                continue;
            }
        }
        
        if ( sugar == null )
        {
            throw new SequenceFormatException(
                "Sequence could not be parsed by any known "
                + "sequence format parser ("
                + this.toString()
                + ")" 
            );
        }
        
        return sugar;
    }
    
    
    /** Not yet implemented @throws UnsupportedOperationException */
    public Monosaccharide getMonosaccharide( String seq ) 
    throws SequenceFormatException
    {
        throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
    }

    
    /** Not yet implemented @throws UnsupportedOperationException */
    public Substituent getSubstituent( String seq ) throws SequenceFormatException
    {
        throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
    }
    
    
    /** Returns the sequence produced by {@link #getOutputFormat}. */
    public String getSequence( Sugar s ) 
    {
        return getOutputFormat().getSequence( s );
    }


    /** Returns the sequence produced by {@link #getOutputFormat}. */
    public String getSequence( Monosaccharide m ) 
    {
        return getOutputFormat().getSequence( m );
    }
    
    
    /** Returns the sequence produced by {@link #getOutputFormat}. */
    public String getSequence( Substituent s )
    {
        return getOutputFormat().getSequence( s );
    }
    
    
    /** Returns the sequence produced by {@link #getOutputFormat}. */
    public String getSequence( Residue r )
    {
        return getOutputFormat().getSequence( r );
    }

    
    /** 
    *   Returns a string representation of this multi-format sequence parser,
    *   including a list of all supported format. 
    */
    public String toString()
    {
        return getName();
    }
    
} // end class

