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
*   Last commit: $Rev: 1233 $ by $Author: glycoslave $ on $Date:: 2009-06-19 #$  
*/

package org.eurocarbdb.sugar;

//  stdlib imports

//  3rd party imports

//  eurocarb imports
import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.SugarSequence;
// import org.eurocarbdb.sugar.Substituent;
import org.eurocarbdb.sugar.seq.*;

//  static imports


/**
*   An interface for classes implementing different sequence formats.
*   The general procedure for translating between formats is to use one 
*   of the following 3 idioms.<br/>
*</p>
*<ol>
*   Given:
*   <pre>
*       String input_sequence = ...;
*       SequenceFormat format1 = SequenceFormat.Iupac;
*       SequenceFormat format2 = SequenceFormat.Glycoct;
*   </pre>
*   <li>using an intermediary {@link Sugar} object:</li>
*   <pre>
*       Sugar s = format1.getSugar( input_sequence );
*       String output_sequence = format2.getSequence( s );
*   </pre>
*
*   <li>using an intermediary {@link SugarSequence} object:</li>
*   <pre>
*       SugarSequence ss = new SugarSequence( input_sequence, format1 );
*       String output_sequence = ss.toString( format2 );
*   </pre>
*
*   <li>directly converting between {@link String}s:</li>
*   <pre>
*       String output_sequence = SugarSequence.translate( input_sequence, format1, format2 );
*   </pre>
*</ol>
*<p>
*   For all 3 methods, converting between formats involved parsing of one 
*   String format into a Sugar object, and then generating the desired
*   String format from that Sugar.
*</p>
*<p>
*    Created 19-Sep-2005.
*
*   @author mjh
*/
public interface SequenceFormat extends ResidueFormat
{
    /** The GlycoCT sequence format; see also {@link GlycoctSequenceFormat}. */
    public static final SequenceFormat Glycoct = new GlycoctSequenceFormat();
    
    /** The GlycoCT-XML sequence format; see also {@link GlycoctXmlSequenceFormat}. */
    public static final SequenceFormat GlycoctXml = new GlycoctXmlSequenceFormat();
    
    /** The Iupac sequence format; see also {@link IupacSequenceFormat}. */
    public static final SequenceFormat Iupac = new IupacSequenceFormat();
    
    /** The Carbbank sequence format; see also {@link CarbbankSequenceFormat}. */
    public static final SequenceFormat Carbbank = new CarbbankSequenceFormat();
    
    /** 
    *   A special sequence format representing any of the sequence formats 
    *   supported by Eurocarb; currently: Glycoct, Iupac, Carbbank, GlycoctXml.
    *   @see MultiSequenceFormat
    */
    public static final SequenceFormat AnyFormat 
        = new MultiSequenceFormat( Glycoct, Iupac, Carbbank, GlycoctXml );
    
        
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
    /*  getName  *//*************************************************
    *   
    *   Returns a string description of this sequence format.
    */
    public String getName()
    ;
    
    
    /*  getMonosaccharide  *//*************************************
    *   
    *   Produces a {@link Monosaccharide} object from a textual sequence 
    *   string in the current sequence format.
    *   
    *   @throws SequenceFormatException 
    *   If passed a sequence string that contains a syntax error.
    */
    public Monosaccharide getMonosaccharide( String seq ) throws SequenceFormatException
    ;
    

    /*  getSubstituent  *//*************************************
    *   
    *   Produces a {@link Substituent} object from a textual sequence 
    *   string in the current sequence format.
    *   
    *   @throws SequenceFormatException 
    *   If passed a sequence string that contains a syntax error.
    */
    public Substituent getSubstituent( String seq ) throws SequenceFormatException
    ;
    
    
    /*  getSugar  *//*******************************************
    *   
    *   Produces a {@link Sugar} object from a textual sequence string
    *   in the current sequence format.
    *   
    *   @throws SequenceFormatException 
    *   If passed a sequence string that contains a syntax error.
    */
    public Sugar getSugar( String seq ) throws SequenceFormatException
    ;
    

    /*  getSequence  *//*****************************************
    * 
    *   Produces a textual string representation of the passed 
    *   {@link Residue} in the current sequence format.
    */
    public String getSequence( Residue r )
    ;

    
    /*  getSequence  *//*****************************************
    * 
    *   Produces a textual string representation of the passed 
    *   {@link Substituent} in the current sequence format.
    */
    public String getSequence( Substituent s )
    ;
    
    
    /*  getSequence  *//*****************************************
    * 
    *   Produces a textual string representation of the passed 
    *   {@link Sugar} in the current sequence format.
    */
    public String getSequence( Sugar s )
    ;
    

    /*  getSequence  *//*****************************************
    * 
    *   Produces a textual string representation of the passed 
    *   {@link Monosaccharide} in the current sequence format.
    */
    public String getSequence( Monosaccharide m )
    ;

} // end class
