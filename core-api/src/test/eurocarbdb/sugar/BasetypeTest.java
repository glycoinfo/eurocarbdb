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
*   Last commit: $Rev: 1273 $ by $Author: glycoslave $ on $Date:: 2009-06-26 #$  
*/

package test.eurocarbdb.sugar;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import org.testng.annotations.*;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.ArrayListMultimap;



import org.eurocarbdb.util.BitSet;
import org.eurocarbdb.sugar.Basetype;
import org.eurocarbdb.sugar.CommonBasetype;
import org.eurocarbdb.sugar.CustomBasetype;
import org.eurocarbdb.sugar.Substituent;
import org.eurocarbdb.sugar.CommonSubstituent;

import test.eurocarbdb.dataaccess.CoreApplicationTest;

import static java.lang.System.out;

import static org.eurocarbdb.sugar.CommonSubstituent.*;
import static org.eurocarbdb.sugar.CommonBasetype.*;
import static org.eurocarbdb.sugar.StereoConfig.*;
import static org.eurocarbdb.sugar.Superclass.*;
import static org.eurocarbdb.util.StringUtils.join;

import static org.eurocarbdb.sugar.Basetypes.describe;
import static org.eurocarbdb.sugar.Basetypes.getBasetype;
import static org.eurocarbdb.sugar.Basetypes.getInvertedBasetype;
import static org.eurocarbdb.sugar.Basetypes.getNormalisedBasetype;
import static org.eurocarbdb.sugar.Basetypes.getStereochemicalId;
import static org.eurocarbdb.sugar.Basetypes.getBasetypeId;


// @Test( groups={"sugar.lib.basetype"}, dependsOnGroups={"util.bitset"} )
@Test( groups={"sugar.lib"} )
public class BasetypeTest extends CoreApplicationTest
{
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~ TESTS ~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** 
    *   Dumps the list of {@link CommonBasetype}s.
    */
    @Test
    public void basetypeInit()
    {
        out.println("sanity checking all common basetypes");
        
        for ( Basetype b : CommonBasetype.values() )   
        {
            if ( ! b.isDefinite() )
                continue;
            
            report( b );
        }
    }
    
    
    /** 
    *   Tests creation of {@link Basetype}s using {@link Basetypes#getBasetype}.
    */
    @Test( dependsOnMethods={"basetypeInit", "basetypeId", "basetypeStereochemId"} )
    public void basetypeCreateComplex()
    {
        report( getBasetype( L, Glc ) );
        
        report( getBasetype( D, Gly, D, Gal ) );
        
        report( getBasetype( D, Gly, L, Gal ) );
        
        
        // Basetype b2 = getBasetype( D, Gly, D, Gal, Octose );
        // Basetype b3 = getBasetype("d-glc");
    }    
    
    
    /** 
    *   Tests complex manipulation of {@link CustomBasetype}s using
    *   {@link CustomBasetype#setFunctionalGroup}.
    */
    @Test( dependsOnMethods={"basetypeCreateComplex", "basetypeId", "basetypeStereochemId"} )
    public void basetypeManipulateComplex()
    {
        CustomBasetype b1 = (CustomBasetype) getBasetype( L, All );
        
        out.println("at start: ");
        report( b1 );
        assert b1.getSuperclass() == Hexose;
        
        out.println("add 2-deoxy, preserve basetype: ");
        b1.setFunctionalGroup( Deoxy, 2, false );
        report( b1 );
        assert b1.getSuperclass() == Heptose;
        
        out.println("add 4-deoxy, preserve basetype: ");
        b1.setFunctionalGroup( Deoxy, 4, false );
        report( b1 );
        assert b1.getSuperclass() == Octose;
        
        //  looking up basetype after changing isn't done yet
        /*
        CustomBasetype b2 = (CustomBasetype) getBasetype( L, Glc );
        
        out.println("at start: ");
        report( b2 );
        assert b2.getSuperclass() == Hexose;
        
        out.println("add 2-deoxy, don't preserve basetype: ");
        b2.setFunctionalGroup( Deoxy, 2 );
        report( b2 );
        assert b2.getSuperclass() == Hexose;
        
        out.println("add 4-deoxy, don't preserve basetype: ");
        b2.setFunctionalGroup( Deoxy, 4 );
        report( b2 );
        assert b2.getSuperclass() == Hexose;
        */
    }
    
        
    static final String[] bts = new String[] {
        "d-Glc",
        "D-Glc",
        "l-Glc",
        "L-Glc",
        "d-glc-l-glc",
        "l-ery-l-gal",
        "d-ery-l-gal",
        "l-ery-d-gal",
        "l-all-l-all",
        "d-all-l-all",
        "l-all-d-all",
        "l-glc;2-deoxy",
        // "D-Glc,2-NAc",
        // "d-Gly-l-Glc|2-deoxy-2-keto",
    };

    static final String[] incorrect_bts = new String[] {
        "d-Glcc",
        "l-Glc-d"
    };
    
    
    /** Is going to be moved */
    @Test( enabled=false )
    public void basetypeParse()
    {
        for ( String name : bts )
        {
            out.println();
            out.println("attempting to parse name '" + name + "':" );
            report( getBasetype( name ) );   
        }
    }
    
    
    /** Is going to be moved */
    @Test( enabled=false )
    public void basetypeParse2()
    {
        for ( String name : incorrect_bts )
        {
            try
            {
                out.println();
                out.println("attempting to parse (incorrect) name '" + name + "':" );
                Basetype b = getBasetype( name );
                assert false : "Expected exception, but parsed OK";
            }
            catch ( Exception ex )
            {
                out.println("name correctly threw " + ex );
            }
        }
    }

    
    /** 
    *   Tests/Lists IDs for basetypes that are dependent on basetype 
    *   stereochemistry *and* the positions of chiral centres, but not dependent 
    *   on functiional groups.
    */
    @Test( dependsOnMethods={"basetypeInit"} )
    public void basetypeId()
    {
        out.println("calculating basetype IDs (stereochem + chiral positions + superclass)" );
        out.println();

        // Map<Integer,Basetype> bts  = new HashMap<Integer,Basetype>();
        ListMultimap<Integer,Basetype> bts  = ArrayListMultimap.create();
        
        for ( CommonBasetype b : CommonBasetype.values() )
        {
            if ( b.getStereochemistry() == null )
                continue;
            
            int id = getBasetypeId( b );
            addToMap( bts, id, b );
            
            //  skip unknown basetype
            if ( id == 0 )
                continue;
            
            Basetype inverted = getInvertedBasetype( b );
            id = getBasetypeId( inverted );
            addToMap( bts, id, inverted );
        }
        
        List<Integer> ids = new ArrayList<Integer>( bts.keySet() );
        java.util.Collections.sort( ids );
        
        for ( Integer id : ids )
        {
            // Basetype b = bts.get( id );
            List<Basetype> list = bts.get( id );
            // out.println( "name=" + b + ", id=" + id );
            out.println( "id=" + id + ", basetypes=" + list );
        }
    }
    
    
    /** 
    *   Tests/Lists IDs for basetypes that are dependent solely on basetype 
    *   stereochemistry independent of *where* those stereochemical positions 
    *   are, and what functional groups are involved.  
    */
    @Test( dependsOnMethods={"basetypeInit"} ) 
    public void basetypeStereochemId()
    {
        out.println("calculating basetype stereochem IDs (stereochem only)" );
        out.println();
        
        // Map<Integer,Basetype> bts  = new HashMap<Integer,Basetype>();
        ListMultimap<Integer,Basetype> bts  = ArrayListMultimap.create();
        
        for ( CommonBasetype b : CommonBasetype.values() )
        {
            if ( b.getStereochemistry() == null )
                continue;
            
            // out.println("processing: " + b );
            int id = getStereochemicalId( b );
            addToMap( bts, id, b );
            
            //  skip unknown basetype
            if ( id == 0 )
                continue;
            
            Basetype inverted = getInvertedBasetype( b );
            id = getStereochemicalId( inverted );
            addToMap( bts, id, inverted );
        }
        
        List<Integer> ids = new ArrayList<Integer>( bts.keySet() );
        java.util.Collections.sort( ids );
        
        for ( Integer id : ids )
        {
            // Basetype b = bts.get( id );
            List<Basetype> list = bts.get( id );
            // out.println( "name=" + b + ", id=" + id );
            out.println( "id=" + id + ", basetypes=" + list );
        }
    }
    
    
    /** 
    *   Tests {@link Basetypes#getNormalisedBasetype}: checks that  
    *   basetypes + substituents that cause loss or gain of stereogenic 
    *   positions are handled correctly, in this case, by preserving basetype
    *   stereochemistry and changing the {@link Superclass}.
    */
    @Test( dependsOnMethods={"basetypeManipulateComplex"} )
    public void basetypeNormalise_checkSuperclassNormalisation()
    {
        Basetype basetype;
        List<CommonSubstituent> substits;

        //  1-Deoxy-Glc 
        out.println("::::::::: test 1 :::::::::");
        substits = Arrays.asList( Deoxy, null, null, null, null, null );
        basetype = normaliseAndReport( Glc, substits );
        assert basetype == Glc;

        
        //  2-Deoxy-Glc
        out.println("::::::::: test 2 :::::::::");
        substits = Arrays.asList( null, Deoxy, null, null, null, null );
        basetype = normaliseAndReport( Glc, substits );
        assert basetype != Glc;
        assert basetype.getSuperclass() == Heptose;
        
        //  6-Deoxy-Glc
        out.println("::::::::: test 3 :::::::::");   
        substits = Arrays.asList( null, null, null, null, null, Deoxy );
        basetype = normaliseAndReport( Glc, substits );
        assert basetype == Glc;
        
        //  6x Deoxy to Glc
        out.println("::::::::: test 4 :::::::::");
        substits = Arrays.asList( Deoxy, Deoxy, Deoxy, Deoxy, Deoxy, Deoxy );
        basetype = normaliseAndReport( Glc, substits );
        assert basetype != Glc;
        assert basetype.getSuperclass() == Undecose;
        
        //  2-Deoxy-Fru
        out.println("::::::::: test 5 :::::::::");
        substits = Arrays.asList( null, Deoxy, null, null, null, null );
        basetype = normaliseAndReport( Fru, substits );
        
        //  2-keto-Glc
        out.println("::::::::: test 6 :::::::::");
        substits = Arrays.asList( null, Carbonyl, null, null, null, null );
        basetype = normaliseAndReport( Glc, substits );
        assert basetype != Glc;
        assert basetype.getSuperclass() == Heptose;
        
        //  1-deoxy-2-keto-Glc
        out.println("::::::::: test 7 :::::::::");
        substits = Arrays.asList( Deoxy, Carbonyl, null, null, null, null );
        basetype = normaliseAndReport( Glc, substits );
        assert basetype != Glc;
        assert basetype.getSuperclass() == Heptose;
        
    }
    
    
    /** 
    *   Tests {@link Basetypes#getNormalisedBasetype}: checks that  
    *   basetypes + substituents that match a pre-defined {@link CommonBasetype}
    *   will return that CommonBasetype.
    */
    @Test( dependsOnMethods={"basetypeManipulateComplex"} )
    public void basetypeNormalise_checkNormalisationToCommonBasetypes()
    {
        Basetype basetype;
        List<CommonSubstituent> substits;

        //  Glc -> GlcNAc
        out.println("::::::::: test 1 :::::::::");
        substits = Arrays.asList( null, NAc, null, null, null, null );
        basetype = normaliseAndReport( Glc, substits );
        assert basetype == GlcNAc;
        
        //  Glc -> GlcN
        out.println("::::::::: test 2 :::::::::");
        substits = Arrays.asList( null, NH2, null, null, null, null );
        basetype = normaliseAndReport( Glc, substits );
        assert basetype == GlcN;
        

        //  Gal -> GalNAc
        out.println("::::::::: test 3 :::::::::");
        substits = Arrays.asList( null, NAc, null, null, null, null );
        basetype = normaliseAndReport( Gal, substits );
        assert basetype == GalNAc;
        
        //  NeuAc-related tests
        Basetype neuac = getBasetype( D, Gly, D, Gal ); 

        //  not quite NeuAc
        out.println("::::::::: test 4 :::::::::");
        substits = Arrays.asList( Carboxyl, Carbonyl, Deoxy, null, null, null );
        basetype = normaliseAndReport( neuac, substits );
        assert basetype != NeuAc;

        //  really NeuAc
        out.println("::::::::: test 5 :::::::::");
        substits = Arrays.asList( Carboxyl, Carbonyl, Deoxy, null, NAc, null );
        basetype = normaliseAndReport( neuac, substits );
        assert basetype == NeuAc;

        //  Man -> Rha
        out.println("::::::::: test 6 :::::::::");
        substits = Arrays.asList( null, null, null, null, null, Deoxy );
        basetype = normaliseAndReport( Man, substits );
        assert basetype == Rha;

        //  Gal -> Fuc
        out.println("::::::::: test 6 :::::::::");
        substits = Arrays.asList( null, null, null, null, null, Deoxy );
        basetype = normaliseAndReport( getInvertedBasetype( Gal ), substits );
        assert basetype == Fuc;

    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
    final Basetype normaliseAndReport( Basetype basetype, List<CommonSubstituent> substits )
    {
        out.print( "basetype before normalisation: " );
        report( basetype );
        out.print( "substits to consider: " );
        out.println( substits );
        out.println();
        
        Basetype normalised = getNormalisedBasetype( 
            basetype, new ArrayList<Substituent>( substits ) );
        
        out.println();
        out.print( "basetype after normalisation: " );
        report( normalised );
        out.print( "substits remaining: " );
        out.println( substits );
        out.println();
        
        return normalised;
    }
    
    
    final void addToMap( ListMultimap<Integer,Basetype> bts, int id, Basetype b )
    {
        bts.put( id, b );
    }
    
    
    final void report( Basetype b )
    {
        out.println( describe( b ) );   
    }
    
}

