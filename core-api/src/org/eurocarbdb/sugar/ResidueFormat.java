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
*   Last commit: $Rev: 1259 $ by $Author: glycoslave $ on $Date:: 2009-06-26 #$  
*/

package org.eurocarbdb.sugar;


//  stdlib imports

//  3rd party imports

//  eurocarb imports
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.Substituent;

//  static imports


/**
*   An interface for classes implementing different {@link Residue} formats.
*
*   @author mjh
*/
public interface ResidueFormat
{
    
    // /** The Carbbank sequence format; see also {@link CarbbankSequenceFormat}. */
    // public static final SequenceFormat Carbbank = new CarbbankSequenceFormat();
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
    /*  getName  *//*************************************************
    *   
    *   Returns a string description of this residue format.
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
    public Monosaccharide getMonosaccharide( String name ) throws SequenceFormatException
    ;
    
    
    /*  getSubstituent  *//******************************************
    *
    *   Produces a {@link Substituent} object from a textual sequence string
    *   in the current sequence format.
    *
    *   @throws SequenceFormatException 
    *   If passed a sequence string that contains a syntax error.
    */
    public Substituent getSubstituent( String name ) throws SequenceFormatException
    ;    
    
    
    /*  getSequence  *//*****************************************
    * 
    *   Produces a textual string representation of the passed 
    *   {@link Monosaccharide} in the current sequence format.
    */
    public String getSequence( Monosaccharide m )
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
    *   Residue in the current sequence format.
    */
    public String getSequence( Residue r )
    ;    
    
} // end class
