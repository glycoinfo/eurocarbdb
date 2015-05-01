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

package org.eurocarbdb.sugar;

//  stdlib imports
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;

//  3rd party imports
import org.apache.log4j.Logger;
import com.google.common.collect.Multimap;
import com.google.common.collect.ArrayListMultimap;

//  eurocarb imports
import org.eurocarbdb.util.BitSet;
import org.eurocarbdb.sugar.Monosaccharide;

//  static imports
import static org.eurocarbdb.sugar.CommonBasetype.*;
import static org.eurocarbdb.sugar.Superclass.*;
import static org.eurocarbdb.sugar.StereoConfig.*;
import static org.eurocarbdb.sugar.RingConformation.*;
import static org.eurocarbdb.sugar.CommonSubstituent.*;

import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.util.StringUtils.split;
import static org.eurocarbdb.util.StringUtils.CR;

/**
*   Provides various functions for creating, manipulating, and
*   querying {@link Basetype}s.
*
*   @see CommonBasetype
*   @see CustomBasetype
*   @author mjh
*/
public final class Basetypes
{
    /** Constant indicating an unknown basetype. */
    public static final Basetype UnknownBasetype = new CustomBasetype() 
        {
            public final String getName()
            {
                return "UNKNOWN";  
            }
            
        };

    /** logging handle */
    static Logger log = Logger.getLogger( Basetypes.class );
    
    /** Uninstantiable */    
    private Basetypes() {}

    
    static final Multimap<Integer,Basetype> basetypesById 
        = ArrayListMultimap.create( CommonBasetype.values().length * 2, 1 ); 
    
    static final Multimap<Integer,Basetype> basetypesByStereochemId 
        = ArrayListMultimap.create( CommonBasetype.values().length * 2, 2 ); 
    
    static
    {
        for ( CommonBasetype b : CommonBasetype.values() )
        {
            if ( b.getStereochemistry() == null )
                continue;
            
            int basetype_id = getBasetypeId( b );
            basetypesById.put( basetype_id, b );   
            
            int stereochem_id = getStereochemicalId( b );
            basetypesByStereochemId.put( stereochem_id, b );   

            Basetype inverse = getInvertedBasetype( b );
            
            basetype_id = getBasetypeId( inverse );
            basetypesById.put( basetype_id, inverse );   
            
            stereochem_id = getStereochemicalId( inverse );
            basetypesByStereochemId.put( stereochem_id, inverse );   
        }
    }
    
    
    /**
    *   Returns a {@link Basetype} corresponding to the given String name.
    */
    public static Basetype getBasetype( String name )
    {
        if ( name == null || name.length() == 0 )
            throw new IllegalArgumentException(
                "Basetype name can't be null or zero-length");
            
        Basetype b = CommonBasetype.forName( name );    
        
        if ( b != null )
            return b;
        
        log.warn("**** still need to move this code ****");
        
        BasetypeParser parser = new BasetypeParser( name );
        
        parser.parse();
        b = parser.getBasetype(); 
        List<Substituent> subs = parser.getSubstituents();
        
        return b;
    }
     
    
    static class BasetypeParser
    {
        int pos = 0;
        
        final String input;
        final String basetypeText;
        final String substituentText;
        
        Basetype basetype = null;
        List<Substituent> substituents = null;
        
        BasetypeParser( String text )
        {
            this.input = text;
            int i = text.indexOf(';');
            if ( i != -1 )
            {
                this.basetypeText = text.substring( 0, i );
                this.substituentText = text.substring( i+1 );
            }
            else
            {
                this.basetypeText = text;
                this.substituentText = null;
            }
        }
        
        
        Basetype getBasetype() {  return basetype;  }
        
        
        List<Substituent> getSubstituents() {  return substituents;  }
        
        
        void parse()
        {
            this.basetype = parseBasetype();
            
            if ( substituentText == null )
                return;
            
            substituents = new ArrayList( basetype.getFunctionalGroups() );
            List<String> substits = split(';', substituentText );
            
            pos = basetypeText.length() + 1;
            
            int position;
            List<String> bits;
            
            for ( String substit : substits )
            {
                if ( substit.length() < 3 )
                    throw new SequenceFormatException(
                        input, pos + 2, "Expected a Substituent name");
                
                bits = split( '-', substit );
                if ( bits.size() > 2 )
                    throw new SequenceFormatException(
                        input, pos, pos + substit.length() - 1, 
                        "Invalid Substituent: expected '<position>-<substituent-name>");
                
                try {  position = Integer.valueOf( bits.get(0) );  }
                catch ( NumberFormatException ex )
                {
                    throw new SequenceFormatException(
                        input, pos, pos + bits.get(0).length(), 
                        "Expected a numeric substituent position" );
                }
                
                Substituent s = CommonSubstituent.forName( bits.get(1) );
                substitute( position, s ); 
                
                pos += substit.length() + 1; 
            }
        }
        
        
        private void substitute( int position, Substituent s )
        {
            if ( position < 0 || position >= basetype.getStereochemistry().length() )
                throw new SequenceFormatException( 
                    input, pos, "Invalid substituent position");

            Substituent existing = basetype.getFunctionalGroups().get( position );
            // log.debug("existing = ");
            
            if ( existing != OH )
            {
                throw new UnsupportedOperationException(
                    "Substitution of non-hydroxy positions not implemented; position="
                    + position
                    + ", existing substituent="
                    + existing
                    + ", desired substituent="
                    + s
                );
            }   
            
                
            boolean multiple_basetypes 
                = ( (basetype instanceof CustomBasetype) 
                    && ((CustomBasetype) basetype).getComponentBasetypes().length > 1 );
            
            if ( multiple_basetypes && s.causesStereoloss() )    
            {
                ((CustomBasetype) basetype).removeChiralPosition( position, false );
            }
            
            substituents.add( position, s );   
        }
        
        
        // private final List<Substituent> parseSubstituents()
        // {
        //     return substituents;
        // }
        
        
        private final Basetype parseBasetype()
        {
            List<StereoConfig> scs = new ArrayList<StereoConfig>( 4 );
            List<CommonBasetype> bts = new ArrayList<CommonBasetype>( 4 );
            
            while ( pos < basetypeText.length() )
            {
                StereoConfig sc = null;
                if ( pos + 1 < basetypeText.length() && basetypeText.charAt( pos + 1 ) == '-' )
                {
                    sc = parseStereoConfig();
                    pos += 2;
                }
                
                CommonBasetype cb = parseSingleBasetype();
                if ( sc == null )
                    sc = cb.getStereoConfig();
                
                scs.add( sc );
                bts.add( cb );
            }
            
            if ( bts.size() == 1 )
            {
                return Basetypes.getBasetype( scs.get(0), bts.get(0) );
            }
            else if ( bts.size() == 2 )
            {
                return Basetypes.getBasetype( 
                    scs.get(0), bts.get(0), 
                    scs.get(1), bts.get(1) 
                );
            }
            else 
            {
                throw new UnsupportedOperationException(
                    "compound basetypes with > 2 bts not yet supported");
            }
        }    
        
        
        private final StereoConfig parseStereoConfig()
        {
            try
            {
                return StereoConfig.forName( basetypeText.charAt(pos) );   
            }
            catch ( IllegalArgumentException ex )
            {
                throw new SequenceFormatException(
                    input, pos, "Expected a StereoConfig letter" );
            }
        }
        
        
        private final CommonBasetype parseSingleBasetype()
        {
            String name;
            int pos2 = basetypeText.indexOf( '-', pos );
            if ( pos2 == -1 )
            {
                pos2 = basetypeText.length() - 1;
                name = basetypeText.substring( pos );
            }
            else
            {
                name = basetypeText.substring( pos, pos2 ); 
            }
                
            if ( name.length() < 3 || name.length() > 8 )
                throw new SequenceFormatException(
                    input, pos, pos2, "Invalid basetype name" );
                
            // log.debug( "looking up name: " + name );
            CommonBasetype cb = CommonBasetype.forName( name );
            
            if ( cb == null )
                throw new SequenceFormatException(
                    input, pos, pos2, "Unknown basetype name" );
                
            pos += cb.name().length() + 1; 
            return cb;
        }
        
    } // end class    
    
    
    /**
    *   Returns a {@link Basetype} corresponding to the given {@link CommonBasetype} 
    *   with the specified {@link StereoConfig}.
    */
    public static Basetype getBasetype( StereoConfig sc, CommonBasetype bt )
    {
        assert sc != null; 
        assert bt != null; 

        //  if the Basetype given is defined in terms of the given StereoConfig, 
        //  then we can just return the enum constant Basetype.        
        if ( sc == bt.getStereoConfig() )                    
            return bt;

        //  otherwise, return a basetype with inverted stereochemistry
        return new CustomBasetype(
            new StereoConfig[] { sc },
            new CommonBasetype[] { bt },
            bt.getSuperclass(),
            bt.getStereochemistry().bitComplementEquals().bitwiseAndEquals( bt.getChiralPositions() ),
            bt.getChiralPositions(),
            bt.getFunctionalGroups()
        );
    }


    /**
    *   Returns a {@link Basetype} with a hydroxyl configuration corresponding
    *   to the given conjoined {@link CommonBasetype}s with the given 
    *   {@link StereoConfig}urations, as specified by 
    *   <a href="http://www.chem.qmul.ac.uk/iupac/2carb/08n09.html#083">
    *   IUPAC conventions</a>, with the first CommonBasetype being closest 
    *   to the carbonyl end.
    */
    public static Basetype getBasetype( 
        StereoConfig sc1, CommonBasetype bt1, 
        StereoConfig sc2, CommonBasetype bt2
    )
    {
        assert sc1 != null;
        assert bt1 != null;
        assert sc2 != null;
        assert bt2 != null;
        assert bt1.isAldose();
        assert bt2.isAldose();
        
        Basetype b1 = bt1;
        if ( sc1 != b1.getStereoConfig() )
            b1 = getBasetype( sc1, bt1 );
        
        Basetype b2 = bt2;
        if ( sc2 != b2.getStereoConfig() )
            b2 = getBasetype( sc2, bt2 );
        
        //  resolve stereochemistry of b1
        int lo_index, hi_index;
        
        lo_index = b1.getChiralPositions().lowestSetBit();
        hi_index = b1.getChiralPositions().highestSetBit() + 1;
        assert hi_index - lo_index == b1.getChiralPositions().size();
        
        BitSet stereochem1 = b1.getStereochemistry().bitSlice( lo_index, hi_index );
        BitSet chiral_posc1 = b1.getChiralPositions().bitSlice( lo_index, hi_index );
        
        //  resolve stereochemistry of b2
        lo_index = b2.getChiralPositions().lowestSetBit();
        hi_index = b2.getChiralPositions().highestSetBit() + 1;
        assert hi_index - lo_index == b2.getChiralPositions().size();
        
        BitSet stereochem2 = b2.getStereochemistry().bitSlice( lo_index, hi_index );
        BitSet chiral_posc2 = b2.getChiralPositions().bitSlice( lo_index, hi_index );
        
        //  append stereochems: b1 goes into the highest bits. 
        //  chiral positions start from the second carbon, as per IUPAC, 
        //  see http://www.chem.qmul.ac.uk/iupac/2carb/08n09.html#083 
        BitSet stereochem = new BitSet( 1 ); 
        stereochem.append( stereochem2, stereochem1, new BitSet( 1 ) );
        
        BitSet chiral_pos = new BitSet( stereochem.length() );
        chiral_pos.set( 1, chiral_pos.length() - 1 );
        
        int size = stereochem.length();
        List<Substituent> func_groups = new ArrayList<Substituent>( size );
        for ( int i = 0; i < size; i++ )
            func_groups.add( OH );
        func_groups.set( 0, Carbonyl );
        
        return new CustomBasetype(
            new StereoConfig[] { sc1, sc2 },
            new CommonBasetype[] { bt1, bt2 },
            Superclass.forSize( size ),
            stereochem,
            chiral_pos,
            func_groups
        );
    }
    
    
    public static final Basetype getBasetype( List<Basetype> basetypes )
    {
        if ( basetypes.size() == 1 )
            return basetypes.get( 0 );
        
        if ( basetypes.size() == 2 )
        {
            //  bit of a hack here, casting to CommonBasetype
            //  isn't necessary except to satisfy javac. what should really
            //  happen here is to write a new method that will take
            //  any arbitrary list of basetypes.
            return getBasetype( 
                basetypes.get( 0 ).getStereoConfig(),
                (CommonBasetype) basetypes.get( 0 ),
                basetypes.get( 1 ).getStereoConfig(),
                (CommonBasetype) basetypes.get( 1 )
            );
        }
        
        if ( basetypes.size() == 0 )
            return UnknownBasetype;
            
        throw new UnsupportedOperationException(
            "more than 2 conjoined basetype temporarily not supported");
    }
    
    
    /**
    *   Returns a {@link Basetype} with inverted {@link StereoConfig}.
    */
    public static final Basetype getInvertedBasetype( CommonBasetype b )
    {
        StereoConfig sc = StereoConfig.invert( b.getStereoConfig() );
        return getBasetype( sc, b );
    }
    
    
    /**
    */
    static final BitSet getInvertedStereochemistry( BitSet stereochem, BitSet chiralPos )
    {
        return stereochem.bitwiseXorEquals( chiralPos );
    }


    static final boolean tracing = log.isTraceEnabled();   
    static final boolean debugging = log.isDebugEnabled();   
    
    /**
    *<p>
    *   Normalises an input {@link Basetype} and array of 
    *   {@link Substituent}s, preserving Basetype stereochemistry
    *   at the expense of altering the {@link Superclass} (if
    *   substituents in the passed array would cause a loss of
    *   stereochemistry).
    *</p>
    *<p>
    *   The returned Basetype may also be different than the given 
    *   Basetype if: 
    *<ol>
    *   <li>Substituents in the parameter list cause a gain or loss
    *   of stereocentres, in which case the Basetype stereochemistry 
    *   is preserved and the {@link Superclass} adjusted accordingly</li> 
    *
    *   <li>Substituents extracted from the parameter list 
    *   would result in a Basetype that better matches the list of 
    *   known basetypes (ie: the enum of {@link CommonBasetype}s).
    *   If such a match occurs, Substituents that match the new, 
    *   returned Basetype will be removed from the passed list.</li> 
    *</ol>
    *</p>
    *<p>
    *   For example:
    *<pre>
    *       import org.eurocarbdb.sugar.CommonBasetype.Glc;
    *       import org.eurocarbdb.sugar.CommonSubstituent.NAc;
    *       import org.eurocarbdb.sugar.Basetypes.getNormalisedBasetype;
    *
    *       List&lt;Substituent&gt; subs = Arrays.asList( null, NAc, null, null, null, null );
    *       Basetype b = getNormalisedBasetype( Glc, subs );
    *
    *       System.out.println( b ); // prints "GlcNAc"
    *       System.out.println( subs ); // prints "[null, null, null, null, null, null]"
    *</pre>
    *</p>
    */
    public static final Basetype 
    getNormalisedBasetype( Basetype b, List<Substituent> substits )//, boolean preserveSuperclass )
    {
        if ( substits == null || substits.size() == 0 )
            return b;
        
        if ( debugging )
        {
            log.debug(
                "normalising basetype " + b + " to substituents: " 
                + substits 
            );
            
            if ( tracing )
            {
                log.trace( 
                    "before considering substituents, basetype is:\n" 
                    + describe( b ) 
                );
            }
        }
        List<Substituent> fgs = b.getFunctionalGroups();
        CustomBasetype cb = CustomBasetype.clone( b );
        
        boolean basetype_changed = false;
        boolean substits_is_all_nulls = true;
        
        for ( int i = 0; i < substits.size(); i++ )
        {
            if ( substits.get(i) == null )
                continue;
             
            substits_is_all_nulls = false;
            
            boolean need_to_alter_superclass 
                = ( substits.get(i).causesStereoloss() 
                    && cb.getChiralPositions().get(i) );
                
            if ( need_to_alter_superclass )
            {    
                cb.removeChiralPosition( i+1, false );
                cb.getFunctionalGroups().set( i, substits.get(i) );
                basetype_changed = true;

                //  this next line doesn't really affect this method, 
                //  but has important implications for the caller. 
                //  if not nulled out, then the caller sees all the 
                //  substituents that have been effectively added to
                //  the basetype still in their substituent array.
                substits.set( i, null );
            }
        }
     
        //  if the passed substituents array has no substituents in it,
        //  just return the originally passed basetype.
        if ( substits_is_all_nulls )
        {
            if ( tracing )
                log.trace("no substituents defined in array");
            
            return b;
        }
            
        if ( tracing && ! substits_is_all_nulls )
        {
            log.trace(
                "after considering stereoloss substits, derived basetype is:\n" 
                + describe( cb ) 
            );
        }
        
        //  else examine the current working basetype to see if there is
        //  an existing CommonBasetype that matches the stereochemistry
        //  of the passed basetype plus passed substituents.
        int id = getBasetypeId( cb );
        Collection<Basetype> equivalent_bts = basetypesById.get( id );

        if ( debugging )
        {
            log.debug(
                "CommonBasetypes with matching stereochemistry: " 
                + equivalent_bts
            );   
        }
        
        if ( equivalent_bts.size() == 0 )
            return basetype_changed ? cb : b;
        
        // CommonBasetype best_match = null;
        Basetype best_match = null;
        
        //  record which basetypes hit and use the one that matches the
        //  most number of substituents in the passed array.
        int max_hits = 0;
        fgs = cb.getFunctionalGroups();
        BitSet best_match_indexes = null;
        BitSet matched = new BitSet( substits.size() );
        
        basetypes: for ( Basetype known_bt : equivalent_bts )
        {
            matched.clear();
            
            // for ( int i : not_null_indexes )
            func_groups: for ( int i = 0; i < fgs.size(); i++ )
            {
                if ( i < substits.size() && substits.get(i) != null )
                {
                    // if ( fgs.get(i) == substits[i] )
                    if ( known_bt.getFunctionalGroups().get(i) == substits.get(i) )
                        matched.set(i);
                }
                else
                {
                    if ( fgs.get(i) != known_bt.getFunctionalGroups().get(i) )
                        // break;
                        continue basetypes;
                }
            }
            
            if ( matched.size() > max_hits )
            {
                best_match = known_bt;
                max_hits = matched.size();
                best_match_indexes = matched.clone();
            }
        }
          
        //  if no better match, return the Basetype we have been working with
        if ( best_match == null )
        {
            if ( tracing )
                log.trace("no CommonBasetypes with matching stereochemistry "
                    + "as well as functional groups, returning derived basetype");
            
                return basetype_changed ? cb : b;
        }
            
        if ( debugging )
        {
            log.debug(
                "derived basetype matches "
                + best_match
                + ", returning it..."
            );
        }
        
        
        //  else there is a better match: remove the substituents from the 
        //  passed array that match the matched basetype, and return the 
        //  matched basetype.
        for ( int i : best_match_indexes )
            substits.set( i, null );
        
        return best_match;
    }
    
    
    /**
    *   Returns an ID for a {@link Basetype} that is unique for that 
    *   Basetype's stereochemistry and chiral positions - basetypes 
    *   with different substitutions that do not affect the stereochemistry
    *   still return the same ID.
    *   @see #getEquivalentBasetypes
    */
    public static final int getBasetypeId( Basetype b )
    {
        BitSet sc = b.getStereochemistry();
        
        if ( sc == null ) // some CBs have null values (temporary)
            return 0;
        
        BitSet cp = b.getChiralPositions();
        
        /*BitSet joined = sc.clone();
        joined.append( cp );
        
        return joined.intValue();*/
        return getBasetypeId( sc, cp );
    }

    
    /** Derives basetype ID from the given arguments. */
    static final int getBasetypeId( BitSet stereochem, BitSet chiralPositions )
    {
        BitSet joined = stereochem.clone();
        joined.append( chiralPositions );
        
        return joined.intValue();
    }
    
    
    /**
    *   Returns an ID that uniquely identifies the stereochemistry of the
    *   passed {@link Basetype}, irrespective of functional groups and 
    *   the location of chiral positions.
    */
    public static final int getStereochemicalId( Basetype b )
    {
        // BitSet bs = b.getStereochemistry().bitSlice( b.getChiralPositions() );     
        // return bs.intValue() | (1 << bs.length());
        return getStereochemicalId( 
            b.getStereochemistry(), b.getChiralPositions() );
    }
    

    /** Derives stereochemical ID from the given arguments. */
    static final int getStereochemicalId( BitSet stereochem, BitSet chiralPositions )
    {
        BitSet bs = stereochem.bitSlice( chiralPositions );     
        return bs.intValue() | (1 << bs.length());
    }

    
    /**
    *   Returns a {@link Set} of {@link CommonBasetype}s that have similar
    *   stereochemistry and chiral position configuration to the passed 
    *   {@link Basetype}. Note that this method does not consider functional 
    *   groups at all, it only considers (1) which positions are chiral, and 
    *   (2) the stereochemistry of those positions. Accordingly, the {@link #D} 
    *   forms of {@link #Glc}, {@link #GlcNAc}, and {@link #GlcN} are equivalent 
    *   according to this method, but D-{@link #Ara}, D-{@link #Fru} are not,
    *   despite the latter having identical stereochemistry.
    */
    public static final Set<Basetype> getEquivalentBasetypes( Basetype b )
    {
        return new HashSet<Basetype>( 
            basetypesById.get( getBasetypeId( b )));           
    }
    
    
    /**
    *   Returns a {@link Set} of pre-defined {@link Basetype}s that have identical
    *   stereochemistry, irrespective of actual chiral positions and functional
    *   groups. So  {@link #Glc}, {@link #GlcNAc}, and {@link #GlcN} are 
    *   stereochemically identical, as are D-{@link #Ara}, D-{@link #Fru}.
    */
    public static final Set<Basetype> getStereochemicallyEqualBasetypes( Basetype b )
    {
        return new HashSet<Basetype>( 
            basetypesByStereochemId.get( getBasetypeId( b )));           
    }
    
    
    static final StereoConfig determineStereoConfig( Basetype b ) 
    {
        // BitSet chiralHydroxylPositions =  b.getStereochemistry().bitwiseAndEquals( b.getChiralPositions() );
        // int dl_position = chiralHydroxylPositions.highestSetBit();
        // return b.getStereochemistry().get( dl_position ) ? D : L;
        return b.getStereoConfig();
    }
    

    static final Superclass determineSuperclass( Basetype b )
    {
        return Superclass.forSize( b.getStereochemistry().length() );           
    }
 
 
    /**
    *   Returns a {@link String} which lists the salient features of the
    *   passed {@link Basetype}, mainly useful for debugging purposes.
    *   The String returned has the following form:
    *<pre>
    *       CommonBasetype=D-GlcNAc
    *       name: D-GlcNAc, fullname: N-acetylglucosamine, superclass: Hexose=6
    *       stereochemistry   : [false, true, false, true, true, false]
    *       chiral indexes    : [false, true, true, true, true, false]
    *       functional groups : [Carbonyl, NAc, OH, OH, OH, OH]
    *       fischer: 
    *       
    *               1: Carbonyl
    *               |
    *               +---- 2
    *               |
    *         3 ----+ 
    *               |
    *               +---- 4
    *               |
    *               +---- 5
    *               |
    *               6
    *       
    *</pre>
    *   Some notes: a stereochemical value of 'true' means projected RIGHT
    *   in a Fischer projection (same side as C2 of {@link Gly Glyceraldhyde}).
    */
    public static final String describe( Basetype b )
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append( b.getClass().getSimpleName() + "=" + b );
        sb.append( CR );

        sb.append("name: " + b.getName() );
        sb.append(", fullname: " + b.getFullName() );
        sb.append(", superclass: " + b.getSuperclass() );
        sb.append( CR );
        
        boolean[] stereo = b.getStereochemistry().toBitArray();
        boolean[] chiral = b.getChiralPositions().toBitArray();
        List<Substituent> chem = b.getFunctionalGroups();
        
        sb.append("stereochemistry   : " + Arrays.toString(stereo) + CR );
        sb.append("chiral indexes    : " + Arrays.toString(chiral) + CR );
        sb.append("functional groups : " + chem + CR );
        sb.append("fischer: " + CR + CR );
        
        assert stereo.length == chiral.length;
        assert stereo.length == chem.size();
        
        for ( int i = 0; i < stereo.length; i++ )
        {
            int pos = i + 1;
            if ( i != 0 )
            {
                sb.append("        |");
                sb.append( CR );
            }
            
            if ( chiral[i] )
            {
                if ( stereo[i] )
                {
                    //  fischer right
                    sb.append("        +---- " + pos + CR );
                }
                else
                {
                    //  fischer left
                    sb.append( "  " + pos + " ----+ " + CR );
                }
            }
            else
            {
                sb.append("        " + pos );
                if ( chem.get(i) != OH )  
                    sb.append(": " + chem.get(i) );
                sb.append( CR );
            }
        }
        
        sb.append( CR );
        
        return sb.toString();
    }
    
} // end class


