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

//  stdlib imports

//  3rd party imports

//  eurocarb imports

//  static imports


/**
*
*   @author mjh
*/
public enum StereoConfig implements PotentiallyIndefinite
{
    D( 'd', "dextro" ),
    L( 'l', "levo" ),
    M( 'm', "meso" ),
    UnknownStereoConfig( '?', "unknown" )
    ;
    
    /** single char representation of this stereo-config */
    private final Character charValue;
    
    private final String name;
    
    
    StereoConfig( char c, String s )
    {
        charValue = c;    
        name = s;
    }
    

    public static final StereoConfig forName( Character letter )
    throws IllegalArgumentException
    {
        char c = Character.toUpperCase( letter );
        switch ( c )
        {
            case 'D':   
                return D;
                
            case 'L':
                return L;                          
                
            case '?':
            case 'X':
            case 'U':
                return UnknownStereoConfig;
                
            case 'M':
                return M;
                
            default:
                throw new IllegalArgumentException(
                    "UnknownStereoConfig or unrecognised stereo configuration: "
                    + "valid values are: " + StereoConfig.values() 
                );
        }
    }

    
    public static final StereoConfig forName( String name )
    {
        if ( name == null || name.length() == 0 )
            throw new IllegalArgumentException(
                "Null or zero-length argument" );
        
        return forName( name.charAt( 0 ) );
    }
    
    
    /** 
    *   Returns a {@link StereoConfig} that results from stereochemical
    *   inversion of the given StereoConfig; ie: {@link #D} becomes {@link #L},
    *   L becomes D, meso stays as meso, unknown stays as unknown.
    */
    public static final StereoConfig invert( StereoConfig sc )
    throws NullPointerException
    {
        switch ( sc )
        {
            case D:
                return L;
                
            case L:
                return D;
                
            default:
                return sc;
        }
    }
    
    
    public final boolean isDefinite()
    {
        return this != UnknownStereoConfig;   
    }
    
    
    public Character toChar()
    {
        return charValue;
    }

} // end enum


