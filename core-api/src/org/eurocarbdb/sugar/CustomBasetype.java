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
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

//  3rd party imports
import org.apache.log4j.Logger;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.ArrayListMultimap;

//  eurocarb imports
import org.eurocarbdb.util.BitSet;
import org.eurocarbdb.sugar.Basetypes;
import org.eurocarbdb.sugar.Monosaccharide;

//  static imports
import static org.eurocarbdb.sugar.Superclass.*;
import static org.eurocarbdb.sugar.StereoConfig.*;
import static org.eurocarbdb.sugar.RingConformation.*;
import static org.eurocarbdb.sugar.CommonSubstituent.*;
import static org.eurocarbdb.sugar.Basetypes.UnknownBasetype;
import static org.eurocarbdb.util.StringUtils.join;


/**
*   Implementation of the {@link Basetype} interface for building
*   mutable, user-defineable basetypes, usually based on the immutable 
*   basetypes in {@link CommonBasetype}.
*
*   @see Basetypes
*   @see CommonBasetype
*   @see Substituent
*
*   @author mjh
*/
public class CustomBasetype implements Basetype
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** logging handle */
    static Logger log = Logger.getLogger( CustomBasetype.class );
    
    static final boolean debugging = log.isDebugEnabled();
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private String name = null;

    /** Array of the basetypes whose stereochemistry defines this 
    *   object's stereochemistry; basetype at index 0 is furthest from C1. */
    private CommonBasetype[] basetypes;
    
    /** Array of the stereoconfigs of each of {@link #basetypes}. 
    *   Since stereoconfig of basetype is derived from stereoconfig of
    *   the chiral position furthest from the reducing carbonyl, the 
    *   stereoconfig at index=0 is also always the stereoconfig for 
    *   the whole basetype. */
    private StereoConfig[] stereoConfigs;
    
    /** Our superclass */
    private Superclass superclass;
    
    /** Stereochemistry of this basetype -- true in the BitSet means pointed 
    *   right in a Fischer projection, index=0 means position=1 */
    private BitSet stereochemistry;
    
    /** BitSet specifying which basetype positions are chiral, true means chiral,
    *   index=0 means position=1. */
    private BitSet chiralPositions;
    
    /** List of the functional groups of this basetype, index=0 means position 1 */
    private List<Substituent> functionalGroups;

    
    //~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~

    CustomBasetype() {}
    
    
    public CustomBasetype( 
        StereoConfig[] stereoConfigs, 
        CommonBasetype[] basetypes, 
        Superclass superclass,
        BitSet stereochem,
        BitSet chiralPos,
        List<Substituent> functionalGroups
    )
    {
        // assert dl == D || dl == L;
        
        this.stereoConfigs    = stereoConfigs;
        this.basetypes        = basetypes;   
        this.superclass       = superclass;
        this.stereochemistry  = stereochem.clone();
        this.chiralPositions  = chiralPos.clone();
        this.functionalGroups = new ArrayList<Substituent>( functionalGroups );
    }
/*        
        int size = 0;
        List<String> names = new ArrayList<String>( basetypes.length );
        
        for ( Basetype b : basetypes )
        {
            assert b.isDefinite();
            assert b.getStereoConfig() == D;
                
            size += b.getSuperclass().size();
            names.add( b.getName() );
        }
        
        BitSet stereochemistry = new BitSet();
        BitSet chiralPositions = new BitSet();
        this.functionalGroups = new ArrayList<Substituent>( size );
        
        for ( Basetype b : basetypes )
        {
            stereochemistry.append( b.getStereochemistry() );   
            chiralPositions.append( b.getChiralPositions() );   
            for ( Substituent s : b.getFunctionalGroups() )
                this.functionalGroups.add( s );
        }
        
        this.superclass = Superclass.forSize( size );
        this.stereochemistry = stereochemistry;
        this.chiralPositions = chiralPositions;
        this.name = join("-", names );
    }
*/    
    
    //~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~

    static CustomBasetype clone( Basetype b )
    {
        if ( b instanceof CommonBasetype )
        {
            return new CustomBasetype(
                new StereoConfig[] { b.getStereoConfig() },
                new CommonBasetype[] { (CommonBasetype) b },
                b.getSuperclass(),
                b.getStereochemistry(),
                b.getChiralPositions(),
                b.getFunctionalGroups() 
            );      
        }
        else if ( b instanceof CustomBasetype )
        {
            CustomBasetype cb = (CustomBasetype) b;
            return new CustomBasetype(
                // Arrays.copyOf( cb.stereoConfigs, cb.stereoConfigs.length ),
                // Arrays.copyOf( cb.basetypes, cb.basetypes.length ), 
                arrayCopyOf( cb.stereoConfigs ),
                arrayCopyOf( cb.basetypes ),
                cb.getSuperclass(),
                cb.getStereochemistry(),
                cb.getChiralPositions(),
                cb.getFunctionalGroups() 
            );
        }
        else throw new UnsupportedOperationException();
    }
    
    
    /** 
    *   Returns a (typed) copy of the passed Array. 
    *   Needed cause we are targeting JDK1.5, and Arrays.copyOf() is JDK1.6. 
    */
    private static final <T> T[] arrayCopyOf( T[] array )
    {
        T[] copy = (T[]) java.lang.reflect.Array.newInstance( 
            array.getClass().getComponentType(), array.length );
        System.arraycopy( array, 0, copy, 0, array.length );
        return copy;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public CommonBasetype[] getComponentBasetypes()
    {
        return basetypes;   
    }

    
    void addChiralPosition( int position, boolean preserveSuperclass )
    {
        int index = position - 1;
        
        if ( preserveSuperclass )
        {
            assert chiralPositions.get( index ) == false;
            chiralPositions.set( index, true );
            return;
        }
        else
        {
            insertNewPosition( index );
            chiralPositions.set( index, true );
            stereochemistry.set( index, true );
        }
        
        name = null;
    }
    
    
    static void normalise( CustomBasetype b )
    {
        BitSet stereochem = b.getStereochemistry();   
        BitSet chiral_pos = b.getChiralPositions();
        
        int nmb_basetypes = (int) (chiral_pos.size() / 4);
        
        CommonBasetype[] basetypes = new CommonBasetype[ nmb_basetypes ];
        /*
        for ( int i = 0; i < nmb_basetypes; i++ )
        {
            int id = 
            CommonBasetype cb =     
        }
        */
        // TODO
    }
    
    
    /**
    *   Assuming given position is a chiral position:
    *   If preserving superclass, just flip a bit;
    *   If not preserving superclass, then in order to preserve 
    *   basetype, have to insert a new non-chiral position.
    */
    void removeChiralPosition( int position, boolean preserveSuperclass )
    {
        int index = position - 1;
        
        if ( preserveSuperclass )
        {
            assert chiralPositions.get( index ) == true;
            chiralPositions.set( index, false );
            return;
        }
        else
        {
            insertNewPosition( index );
            chiralPositions.set( index, false );
            stereochemistry.set( index, false );
        }
        
        name = null;
    }
    
    
    /** 
    *   Insert a whole new position at given index, shifting 
    *   everything up a position, effectively bumping up the 
    *   {@link Superclass} by 1, and setting functional group
    *   at new position to null.
    */
    private final void insertNewPosition( int index )
    {
        BitSet b = new BitSet( 1 ); // defaults to false
        chiralPositions.bitShiftInsert( index, b );
        stereochemistry.bitShiftInsert( index, b );
        functionalGroups.add( index, null );
        
        superclass = Basetypes.determineSuperclass( this );
        name = null;
    }
    
    
    /** 
    *   Equivalent to calling <code>{@link #setFunctionalGroup}(s,position,true)</code>; 
    *   that is, the given {@link Substituent} will be set at the given 
    *   position by preserving the superclass and changing the stereochemistry
    *   for substituents that induce stereochemical changes.
    *
    *   @param s 
    *       the {@link Substituent} to substitute
    *   @param position 
    *       the position to substitute
    *   @return
    *       true if the stereochemistry of this basetype changed
    *   @see #setFunctionalGroup(Substituent,int,boolean)
    */
    public boolean setFunctionalGroup( Substituent s, int position )
    throws IllegalArgumentException, NullPointerException
    {
        return setFunctionalGroup( s, position, true ); 
    }
    
    
    /**
    *<p>
    *   Sets the given {@link Substituent} as the functional group at 
    *   the given position in this basetype. If the given Substituent
    *   causes a loss or gain of stereochemistry as a result of its
    *   introduction then the choice of whether to alter the 
    *   {@link Superclass} or the {@link Basetype} is determined by
    *   the given boolean <code>preserveSuperclass</code> argument.
    *</p>
    *<p>
    *   If preserveSuperclass is false, then the {@link Superclass} 
    *   of this basetype will be increased to accomodate a substituent 
    *   that causes a loss of stereochemistry, and decreased for a 
    *   substituent that introduces a new stereo-genic centre.
    *</p>
    *<p>
    *   If preserveSuperclass is true, then {@link Superclass} will
    *   be preserved, which means that substituents that induce a  
    *   change of stereochemistry will change the {@link Basetype}
    *   identity of this basetype.
    *</p>
    *<p>
    *   If the given {@link Substituent} causes a new stereogenic 
    *   centre to be created where there wasn't one before, then
    *   the stereochemistry of that position defaults to TRUE 
    *   (ie: oriented RIGHT in a Fischer projection). Note that the
    *   method returns true if there was a change in stereochemistry. 
    *</p>
    *    
    *   @param s 
    *       the {@link Substituent} to substitute
    *   @param position 
    *       the position to substitute
    *   @param preserveSuperclass
    *       if false, {@link Superclass} will be changed to accomodate
    *       stereochemistry-changing substituents, thus preserving the
    *       underlying basetype stereochemistry. if true, the Basetype
    *       will be changed and Superclass preserved when introducing
    *       a stereochemistry-changing substituent.
    *   @return
    *       true if the stereochemistry of this basetype changed
    *   @throws IllegalArgumentException
    *       if position is out of bounds for the basetype
    *   @throws NullPointerException
    *       if s is null
    */
    public boolean setFunctionalGroup( Substituent s, int position, boolean preserveSuperclass )
    throws IllegalArgumentException, NullPointerException
    {
        int index = position - 1;
        boolean old_sub_is_chiral = chiralPositions.get( index );
        boolean new_sub_is_chiral = ! s.causesStereoloss();
        
        Substituent current = functionalGroups.get( index );
        assert current != null;
        assert old_sub_is_chiral == ! current.causesStereoloss();
        
        boolean stereochem_changes = old_sub_is_chiral ^ new_sub_is_chiral; 

        name = null;
        
        if ( stereochem_changes )
        {
            if ( new_sub_is_chiral /* then old sub isn't */)
            {
                // old sub position isn't chiral, new sub is 
                if ( debugging )
                {
                    log.debug(
                        "setting " 
                        + s 
                        + " at position=" 
                        + position 
                        + ", replacing " 
                        + current
                        + ", resulting in the gain of a stereogenic centre"
                    );
                }

                addChiralPosition( position, preserveSuperclass );
                functionalGroups.set( index, s );
                
                //  default == fischer right
                stereochemistry.set( index, true );
                
                return true;
            }
            else 
            {
                // old sub position is chiral, new sub isn't 
                if ( debugging )
                {
                    log.debug(
                        "setting " 
                        + s 
                        + " at position=" 
                        + position 
                        + ", replacing " 
                        + current
                        + ", resulting in a loss of a stereogenic centre"
                    );
                }
                
                removeChiralPosition( position, preserveSuperclass );
                functionalGroups.set( index, s );
                return true;
            }
        }
        else
        {
            // no change in stereochem
            if ( debugging )
            {
                log.debug(
                    "setting " 
                    + s 
                    + " at position=" 
                    + position 
                    + ", replacing " 
                    + current
                    + ", resulting in no change of stereochemistry"
                );
            }
     
            functionalGroups.set( index, s );               
            return false;
        }
    }
    
    
    /* implementation of Basetype interface */    
    
    public BitSet getChiralPositions()
    {
        return chiralPositions;
    }
    
    
    public List<Substituent> getFunctionalGroups()
    {
        return functionalGroups;   
    }
    
    
    public StereoConfig getStereoConfig()
    {
        return stereoConfigs[0];
    }
    
    
    public BitSet getStereochemistry()
    {
        return stereochemistry;
    }
    
    
    public Superclass getSuperclass()
    {
        return superclass;
    }
    
    
    /* implementation of Molecule interface */
    
    public String getName()
    {
        if ( this.name != null )
            return this.name;
        
        StringBuilder stem_name = new StringBuilder();
        StringBuilder substits = new StringBuilder();
        
        for ( int i = 0; i < basetypes.length; i++ )
        {
            if ( i != 0 )
                stem_name.append( '-' );
            
            stem_name.append( stereoConfigs[i] );   
            stem_name.append( '-' );   
            stem_name.append( basetypes[i].name() );   
        }

        //  substituents        
        Substituent s;
        ListMultimap<Substituent,Integer> map = ArrayListMultimap.create();
        CommonBasetype basetype_with_carbonyl = basetypes[(basetypes.length - 1)];
        
        for ( int i = 0; i < functionalGroups.size(); i++ )
        {
            s = functionalGroups.get( i );
            
            if ( s == OH )
                continue;
            
            if ( s == basetype_with_carbonyl.getFunctionalGroups().get(i) )
                continue;
            
            map.put( s, i+1 );                
        }
        
        List<Integer> positions;
        for ( int i = 0; i < functionalGroups.size(); i++ )
        {
            s = functionalGroups.get( i );
            positions = map.get( s );
            
            if ( positions == null || positions.size() == 0 )
                continue;
         
            if ( substits.length() > 0 )
                substits.append( '-' );
            
            substits.append( join(",", positions) );
            substits.append( '-' );
            substits.append( s.getName() );
            
            map.removeAll( s );
        }

        //  superclass, if needed         
        if ( substits.length() > 0 || basetypes.length > 1 )
        {
            stem_name.append('-');
            stem_name.append( getSuperclass().getFullName() );
        }
            
        String name = ( substits.length() > 0 
                    ? substits.toString() + "-"
                    : "" )
                    + stem_name.toString()
                    ;
                  
        this.name = name;
        
        return name;
    }


    public String getFullName()
    {
        return getName();   
    }
    
    
    public double getMass()
    {
        return 0;   
    }
    
    
    public double getAvgMass()
    {
        return 0;    
    }
    
    
    /*  implementation of PotentiallyIndefinite interface  */
    
    public boolean isDefinite()
    {
        return true;   
    }
    
    
    public String toString()
    {
        return getName();   
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~
    
}
