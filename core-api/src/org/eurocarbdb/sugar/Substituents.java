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
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.sugar.SimpleSubstituent;

//  static imports


/**
*
*   @author mjh
*
*/
public final class Substituents
{
    /** Not instantiable. */
    private Substituents() {}
    
    /** logging handle */
    static Logger log = Logger.getLogger( Substituents.class );
    
    
    /** Phosphate */
    public static final Substituent 
        Phosphate = new SimpleSubstituent(
            "Phosphate", "P", null, null, 0.0, 0.0 );
    
    /** Sulfate */
    public static final Substituent 
        Sulfate = new SimpleSubstituent(
            "Sulfate", "S", null, null, 0.0, 0.0 );
        
        
    /** List of pre-defined Substituents */
    private static final List<Substituent> predefinedSubstituents 
        = Arrays.asList( 
            Phosphate,
            Sulfate
        );
    
    /** Reference library Map of known/defined Substituents. */
    static final Map<String,Substituent> knownSubstituents
        = new HashMap<String,Substituent>( 
            ( CommonSubstituent.knownSubstits.size() 
            + predefinedSubstituents.size() ) * 2 );
        
    static 
    {
        knownSubstituents.putAll( CommonSubstituent.knownSubstits );
        
        for ( Substituent s : predefinedSubstituents )
        {
            String name = s.getName().toLowerCase();
            assert ! knownSubstituents.containsKey( name );
            knownSubstituents.put( name, s );

            name = s.getFullName().toLowerCase();
            assert ! knownSubstituents.containsKey( name );
            knownSubstituents.put( name, s );
        }
    }
    
    
    public static Substituent createUnknownSubstituent( String name )
    {
        return new SimpleSubstituent(
            "Unknown or partially defined substituent '" + name + "'",
            name,   
            null, // type
            Composition.UnknownComposition,
            0,
            0
        );   
    }
    
    
    /** */
    public static final Substituent getSubstituent( String name )
    {
        if ( name == null || name.length() == 0 )
            throw new IllegalArgumentException(
                "Name cannot be null or zero-length");
            
        String lc_name = name.toLowerCase();
        
        Substituent s = knownSubstituents.get( lc_name );
        
        if ( s == null )
            return null;
            
        //  instances of Residues need to be unique in order to put
        //  them into Sets/Maps/Graphs, so we need to return a *copy*.
        /*
        if ( s instanceof Cloneable )
        {
            return (Substituent) s.clone();
        }
        */
        if ( predefinedSubstituents.contains( s ) )
        {
            // return new SimpleSubstituent( s );
            return ((SimpleSubstituent) s).clone();
        }
        
        return s;
    }
    
    
    /** 
    *   For pragmatic reasons, Eurocarb defines some {@link Substituent}s
    *   as being <em>part of</em> a {@link Monosaccharide}, as distinct 
    *   from being <em>attached to</em> a Monosaccharide as an independent
    *   residue. For example, all of the {@link Substituent}s defined by 
    *   the enum class {@link CommonSubstituent} fall into this category.
    */
    public static boolean substituentIsPartOfMonosaccharide( Substituent s )
    {
        return (s instanceof CommonSubstituent); 
    }
}
    
