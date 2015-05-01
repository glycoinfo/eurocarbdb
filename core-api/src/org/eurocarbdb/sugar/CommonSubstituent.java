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

package org.eurocarbdb.sugar;


//  stdlib imports
import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;

//  3rd party imports

//  eurocarb imports
import org.eurocarbdb.sugar.Substituent;

//  static imports


/**
*   Enumerations of common functional groups (effectively, {@link Substituent}s) 
*   of {@link Monosaccharide}s, which Eurocarb deems as <em>part of</em> a 
*   monosaccharide, as distinct from a <em>separate {@link Residue}</em> of  
*   a monosaccharide.
*   
*   @see Substituents
*   @see SimpleSubstituent
*   @author mjh
*/
public enum CommonSubstituent implements Substituent
{
    /** Hydroxyl group. */
    OH( "hydroxy" ),
    
    /** N-acetamido (CH3CONH-) group. */
    NAc( "N-acetyl", "N-acetamide"),
    
    /** Amino (NH2) group. */
    NH2( "amino", "amine", "n"),
    
    /** Acetyl (CH3COO-) group. */
    Acetyl( "acetyl", "Ac" ),
    
    /** Carbonyl (O=) group. */
    Carbonyl( "keto", "ketone" ),
    
    /** Deoxygenation (-O) of hydroxyl, effectively a "negative substituent". */
    Deoxy( "deoxy", "d" ),
    
    /** Alditol (+H2). */
    Alditol( "aldi", "alditol" ),
    
    /** N-Glycolyl (HO-CH2-CONH-) group */
    NGlycolyl( "N-glycolyl", "NGc" ),
    
    /** Glycolyl (HO-CH2-COOH-) group */
    Glycolyl( "glycolyl", "Gc" ),
    
    //  acidic functional groups 
    
    /** Carboxylic acid (HOOC-) functional group. */
    Carboxyl( "carboxy", "a", "carboxylic acid" ),
    
    /** Aldonic acid, in which the aldehyde functional group of an aldose is oxidized */
    Aldonic( "aldonic acid" ),
    
    /** Aldaric acid, in which both ends of an aldose are oxidized */
    Aldaric( "aldaric", "aldiric acid" ),
    
    /** Uronic acid, in which the terminal hydroxyl group of an aldose or ketose is oxidized */
    Uronic( "U", "uronic acid" ),
    
    /** Ulosonic acid, in which the first hydroxyl group of a 2-ketose is oxidised creating an α-ketoacid. */
    Ulosonic( "ulo", "ulosonic acid" ),
    
    /** Sulfhydryl (SH-) group. */
    Thiol( "thio", "thiol", "sulfhydryl" ),
    
    OMethyl( "hydroxymethyl", "OMe" ),
    
    Methyl( "methyl", "Me" ),
    
    Ethyl( "ethyl", "Et" ),
    
    /** Alkene, double bond (=) */
    En( "ene", "enx", "alkene" ),
    
    Lactate( "lactate" )
    ;
    
    
    private String name;
    
    private String[] synonyms;
    
    
    CommonSubstituent( String name, String... synonyms )
    {
        this.name = name;
        this.synonyms = synonyms;
    }
    
    static EnumSet<CommonSubstituent> causeStereoloss 
        = EnumSet.of( Deoxy, Carbonyl, En );
        
    
    static Map<String,CommonSubstituent> knownSubstits;
    
    static 
    {
        knownSubstits = new HashMap<String,CommonSubstituent>( 
            CommonSubstituent.values().length * 4 );
        
        for ( CommonSubstituent s : CommonSubstituent.values() )
        {
            knownSubstits.put( s.name().toLowerCase(), s );   
            knownSubstits.put( s.name.toLowerCase(), s );
            for ( String syn : s.synonyms )
                knownSubstits.put( syn.toLowerCase(), s );
        }
    }
        
    
    public boolean causesStereoloss()
    {
        return causeStereoloss.contains( this );           
    }
    
        
    public static final CommonSubstituent forName( String name )
    {
        return knownSubstits.get( name.toLowerCase() );
    }
    
    
    public String getFullName()
    {
        if ( synonyms != null && synonyms.length > 0 )
            return synonyms[0];
        
        else return getName();    
    }
    
    
    public String getName()
    {
        return name;    
    }
    

    public double getMass() 
    {
        return 0;
    }    
    
    
    public double getAvgMass() 
    {
        return 0;
    }
    
    
    public String toString()
    {
        // return "[" + getClass().getSimpleName() + "=" + name() + "]";
        return name();
    }
}
    
