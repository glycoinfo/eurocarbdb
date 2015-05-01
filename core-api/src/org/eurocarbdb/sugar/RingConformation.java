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
*   Last commit: $Rev: 1561 $ by $Author: glycoslave $ on $Date:: 2009-07-21 #$  
*/

package org.eurocarbdb.sugar;

import static org.eurocarbdb.util.StringUtils.join;

public enum RingConformation implements PotentiallyIndefinite
{
    /** Indicates ring conformation is unknown */
    UnknownRingConformation('?', -1 ),
    
    /** Indicates ring conformation is not given */
    DefaultRingConformation('-', -1 ),
    
    /** Indicates {@link Monosaccharide} is in linear (open chain) conformation */
    OpenChain('o', 0 ),
    
    /** Indicates {@link Monosaccharide} is in 5-membered ring conformation */
    Furanose('f', 5 ),
    
    /** Indicates {@link Monosaccharide} is in 6-membered ring conformation */
    Pyranose('p', 6 )
    ;
    
    
    char symbol;
    byte ringsize;
    
    
    RingConformation( char symbol, int ringsize )
    {
        this.symbol = symbol;
        this.ringsize = (byte) ringsize;
    }
    
    
    public static final RingConformation forName( char c )
    throws IllegalArgumentException
    {
        switch ( c )
        {
            case 'p':
            case 'P':
                return Pyranose;
            
            case 'f':
            case 'F':
                return Furanose;
                
            case 'o':
            case 'O':
                return OpenChain;
                
            case '?':
            case 'x':
            case 'u':
                return UnknownRingConformation;
                
            default:
                throw new IllegalArgumentException( 
                    "Invalid ring conformation '" 
                    + c 
                    + "'; defined ring conformations are: "
                    + join(", ", values() )
                );
        }
    }
    
    
    public static final RingConformation forRingPositions( int startPos, int endPos )
    throws IllegalArgumentException
    {
        if ( startPos < 1 )
            throw new IllegalArgumentException("start position must be > 0");
            
        if ( endPos < 1 )
            throw new IllegalArgumentException("end position must be > 0");
            
        if ( startPos >= endPos )
            throw new IllegalArgumentException("end position must be > start position");

        return forRingSize( endPos - startPos + 2 );    
    }
    
    
    public static final RingConformation forRingSize( int size )
    throws IllegalArgumentException
    {
        switch ( size )
        {
            case 6:
                return Pyranose;
                
            case 5:
                return Furanose;
                
            case 0:
                return OpenChain;
                
            case -1:
                return UnknownRingConformation;
                
            default:
            {
                throw new IllegalArgumentException(
                    "Undefined ring-size '" 
                    + size 
                    + "'; valid values are: "
                    + join(", ", values() )
                );
            }
        }
    }
    
    
    public byte getRingSize()
    {
        return ringsize;    
    }
    
    
    public final boolean isClosedRing()
    {
        return this == Furanose || this== Pyranose;   
    }
    
    
    public final boolean isDefinite()
    {
        return this != UnknownRingConformation;   
    }
    
    
    public final char toSymbol()
    {
        return symbol;   
    }
    
    public final char toChar()
    {
        return symbol;
    }
    
    
    /** Returns name() + "=" + getRingSize(). */
    public String toString()
    {
        return name() + "=" + getRingSize();
    }
    
} // end class

