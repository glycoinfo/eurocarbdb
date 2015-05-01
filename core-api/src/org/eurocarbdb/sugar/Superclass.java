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
*   Last commit: $Rev: 1263 $ by $Author: glycoslave $ on $Date:: 2009-06-26 #$  
*/
/**
* 
*/
package org.eurocarbdb.sugar;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;

import static org.eurocarbdb.util.StringUtils.join;

/**
*   Superclass is a term that describes the number of Carbon atoms that constitute
*   a {@link Monosaccharide} {@link Basetype}.
*
*   @author mjh
*/
public enum Superclass implements Molecule
{
    /** The canonical "unknown" superclass */
    UnknownSuperclass("Unknown" , 0.0, 0.0, 0 ), 
    
    /** Indicates a known superclass not currently listed. */ 
    OtherSuperclass("Other" , 0.0, 0.0, -1 ),                              
    
    /** Trioseoses (3 carbons), {@link #Gly Glyceraldehyde} and {@link #DHA Dihydroxyacetone}. */
    Triose( "Tri", 0.0, 0.0, 3  ),
    
    /** Trioseoses (4 carbons), {@link Ery Erythrose}, {@link Thr Threose}, and {@link Eru Erythrulose}. */
    Tetrose( "Tet", 0.0, 0.0, 4  ),
    
    /** Pentoseoses (5 carbons), eg: {@link Rib Ribose}, {@link Ara Arabinose}, {@link Xyl Xylose}. */
    Pentose( "Pent", 0.0, 0.0, 5  ),
    
    /** Hexoseoses (6 carbons), eg: {@link Glc Glucose}, {@link Man Mannose}, {@link Gal Galactose}. */
    Hexose( "Hex", 0.0, 0.0, 6  ),
    
    /** Heptoseoses (7 carbons), eg: mannulose. */
    Heptose( "Hept", 0.0, 0.0, 7  ),
    
    /** Octoseoses (8 carbons). */
    Octose( "Oct", 0.0, 0.0, 8 ),
    
    /** Nonoseoses (9 carbons), eg: {@link NeuAc}, {@link NeuGc}. */
    Nonose( "Non", 0.0, 0.0, 9 ),
    
    /** Decoseoses (10 carbons). */
    Decose( "Dec", 0.0, 0.0, 10 ),
    
    /** Un-decoses (11 carbons). */
    Undecose( "Undec", 0.0, 0.0, 11 ),

    /** Do-decoses (12 carbons). */
    Dodecose( "Dodec", 0.0, 0.0, 12 ),
    
    /** Triose-decoses (13 carbons). */
    Tridecose( "Tridec", 0.0, 0.0, 13 ),
    
    /** Tetrosera-decoses (14 carbons). */
    Tetradecose( "Tetradec", 0.0, 0.0, 14 )
    
    ;
    
    
    private static final Map<String,Superclass> namemap 
        = new HashMap<String,Superclass>( values().length );
        
    /** Short string name of the superclass, eg: Pent, Hex, Hept */
    private final String name;
    
    private final double mass;
    
    private final double avgmass;
    
    private final byte size;
    
    static 
    {
        for ( Superclass s : EnumSet.range( Triose, Tetradecose ) )
        {
            namemap.put( "" + s.size(), s );
            namemap.put( s.getName().toLowerCase(), s );
            namemap.put( s.getFullName().toLowerCase(), s );
            namemap.put( s.getGlycoctName().toLowerCase(), s );
        }
        
        for ( Superclass s : EnumSet.range( Undecose, Tetradecose ) )
        {
            String name = "s" + s.size();
            namemap.put( name, s );
        }
        
        namemap.put( "?", UnknownSuperclass );
        namemap.put( "x", UnknownSuperclass );
    }
    
    
    /*  Constructor  *//** 
    *
    *   @param fullname
    *   The full string name of the ringsize fullname, eg: hexose, heptose.
    *   @param prefix
    *   prefix form of the fullname, eg: hex, hep.
    */
    Superclass( String name, double mass, double avgmass, int size )
    {
        this.name = name;
        this.mass = mass;
        this.avgmass = avgmass;
        this.size = (byte) size;
    }
    
    
    /**
    *   Returns a Superclass object whose 3-char prefix or full name matches 
    *   the given string.
    *
    *   @param name
    *   Lookup string that can match either a prefix or the full name.
    */
    public static Superclass forName( String name )
    throws IllegalArgumentException
    {
        if ( name == null )
            return null;
        
        String key = name.toLowerCase();
        Superclass s = namemap.get( key );
        
        if ( s != null )
        {
            return s;
        }
        else 
        {   
            throw new IllegalArgumentException( 
                "Unknown superclass '" 
                + name 
                + "': valid values are: " 
                + join(", ", values() )
            );
        }
    }    
    

    public static final Superclass forSize( byte size )
    throws IllegalArgumentException
    {
        if ( size < 0 || size > values().length )
        {
            throw new IllegalArgumentException( 
                "Invalid superclass size '" 
                + size 
                + "': defined sizes are: "
                + join(", ", values() )
            );
        }
        
        if ( size >= Triose.ordinal() )
            return Superclass.values()[size - 1];
        
        if ( size == 0 )
            return UnknownSuperclass;
        
        return OtherSuperclass;
    }
    
    
    public static final Superclass forSize( int size )
    throws IllegalArgumentException
    {
        return forSize( (byte) size );
    }
    

    public final byte size()
    {
        return this.size;
    }
    
    
    /** Returns the 3-letter prefix for this ringsize. */
    public final String getGlycoctName() 
    {  
        if ( this.size < 3 )
            return "?";
        
        if ( this.size > 10 ) 
            return "S" + this.size;
        
        return name().substring( 0, 3 ).toUpperCase();
    }
    

    /** Returns "[enum name]=[ring size]"; intended for debugging use. */
    public String toString()
    {
        return name() + "=" + size();   
    }
    
    
    /*  implementation of Molecule interface */
    
    /** Returns the short string name of the superclass, eg: Pent, Hex, Hept */
    public final String getName() 
    {  
        return this.name;
    }
    
    
    /** Returns "Triose", "Tetrose", "Pentose", "Hexose", etc. */
    public final String getFullName()
    {
        return name();
    }
    
    
    public final double getAvgMass()
    {
        return mass;
    }
    
    
    public final double getMass()
    {
        return avgmass;
    }
    
}
