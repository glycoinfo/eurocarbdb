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

package org.eurocarbdb.sugar.impl;

//  stdlib imports
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.util.BitSet;
import org.eurocarbdb.util.StringUtils;

import org.eurocarbdb.sugar.Anomer;
import org.eurocarbdb.sugar.Basetype;
import org.eurocarbdb.sugar.Superclass;
import org.eurocarbdb.sugar.StereoConfig;
import org.eurocarbdb.sugar.RingConformation;
import org.eurocarbdb.sugar.CommonBasetype;
import org.eurocarbdb.sugar.PotentiallyIndefinite;

import org.eurocarbdb.sugar.Basetypes;
// import org.eurocarbdb.sugar.Attachable;
import org.eurocarbdb.sugar.Substituent;

import org.eurocarbdb.sugar.BasicMolecule;
import org.eurocarbdb.sugar.SequenceFormat;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.Substituent;

import org.eurocarbdb.sugar.PositionOccupiedException;
import org.eurocarbdb.sugar.PositionNotOccupiedException;
import org.eurocarbdb.sugar.SequenceFormatException;
import org.eurocarbdb.sugar.SugarChemistryException;

//  static imports
import static java.util.Collections.unmodifiableList;
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.sugar.Basetypes.getBasetype;
import static org.eurocarbdb.sugar.Basetypes.getNormalisedBasetype;
import static org.eurocarbdb.sugar.RingConformation.OpenChain;
import static org.eurocarbdb.sugar.CommonSubstituent.*;
import static org.eurocarbdb.sugar.CarbohydrateChemistry.getCarbohydrateChemistry;

/**
*   Straightforward implementation of the {@link Monosaccharide} interface.
*
*   @author mjh
*/
public class SimpleMonosaccharide extends BasicMolecule implements Monosaccharide
{
    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** Logging instance. */
    static final Logger log = Logger.getLogger( SimpleMonosaccharide.class );
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private Anomer anomer;

    private Basetype basetype;

    private RingConformation conformation;
    
    /** Stereochemical configuration, defaults to the {@link StereoConfig} 
    *   of the {@link Basetype} set. */
    private StereoConfig stereo; 
    
    /** {@link List} of {@link Substituent} substituents, lazily instantiated. */
    private List<Substituent> attached = null; //Collections.emptyList();
    
    private List<Substituent> unknowns = null; //Collections.emptyList();
    
    
    //~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~

    SimpleMonosaccharide() {}
    
    
    /** 
    *   Constructs a new Monosaccharide based on the given {@link Basetype},
    *   whose anomeric configuration, stereochemistry and ring conformation
    *   are all set to their respective defaults.
    *
    *   @see Anomer.DefaultAnomer
    *   @see RingConformation.DefaultRingConformation
    */
    public SimpleMonosaccharide( Basetype bt )
    {
        this( 
            Anomer.DefaultAnomer, 
            bt.getStereoConfig(),
            bt,
            RingConformation.DefaultRingConformation
        );
    }
    

    /** 
    *   Constructs a new Monosaccharide based on the given {@link Basetype},
    *   with the given {@link StereoConfig}, with default anomeric configuration
    *   and ring conformation.
    *
    *   @see Anomer.DefaultAnomer
    *   @see RingConformation.DefaultRingConformation
    */
    public SimpleMonosaccharide( StereoConfig dl, CommonBasetype bt )
    {
        this( 
            Anomer.DefaultAnomer, 
            dl,
            Basetypes.getBasetype( dl, bt ),
            RingConformation.DefaultRingConformation
        );
    }
            
    
    /** 
    *   Constructs a new Monosaccharide based on the given {@link Basetype},
    *   with the given {@link StereoConfig}, {@link Anomer}, and {@link RingConformation}.
    *
    *   @throws SugarChemistryException if the given chemistry does not conform
    *   to fundamental rules of {@link CarbohydrateChemistry}.
    */
    public SimpleMonosaccharide( Anomer a, StereoConfig dl, Basetype bt, RingConformation rc )
    throws SugarChemistryException
    {
        stereo = dl;
        basetype = bt;    

        setAnomer( a );
        setRingConformation( rc );
    }
    
    
    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
    *   Attempts to parse a {@link SimpleMonosaccharide} from the 
    *   given {@link String}. The expected syntax is given by the
    *   {@link Basetypes.getBasetype(String)} method.
    *
    *   @throws SequenceFormatException for syntactic errors
    *   @throws SugarChemistryException for semantic errors
    */
    public static final SimpleMonosaccharide forName( String name )
    throws SequenceFormatException, SugarChemistryException
    {
        log.warn("move this code into ResidueFormat subclass");
        return new SimpleMonosaccharide( Basetypes.getBasetype( name ) );           
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** {@inheritDoc} @see org.eurocarbdb.sugar.BasicMolecule#getName */
    @Override
    public String getName()
    {
        List<String> bits = new ArrayList<String>(); 
        
        if ( this.unknowns != null )
        {
            for ( Substituent s : unknowns )
                bits.add( "?-" + s.getName() );
        }

        if ( this.attached != null )
        {
            List<Substituent> btSubstits = getBasetype().getFunctionalGroups();
            assert btSubstits.size() == attached.size();
            
            for ( int i = 0; i < attached.size(); i++ )
                if ( attached.get(i) != null && btSubstits.get(i) != attached.get(i) )
                    bits.add( "" + (i + 1) + "-" + attached.get(i).getName() );
        }
        
        bits.add( getBasetype().getName() );
        
        return join( "-", bits );
    }
    
    
    /*  implementation of Monosaccharide interface  */
    
    public Anomer getAnomer()
    {
        return anomer;            
    }   
    
    
    public void setAnomer( Anomer a ) 
    throws IllegalArgumentException, SugarChemistryException
    {
        getCarbohydrateChemistry().checkAnomer( a, this );
        anomer = a;   
    }
    

    public Basetype getBasetype() 
    {
        return basetype;
    }
    
    
    public RingConformation getRingConformation()
    {
        return conformation;
    }
    
    
    public void setRingConformation( RingConformation rc )
    throws IllegalArgumentException, SugarChemistryException
    {
        getCarbohydrateChemistry().checkRingConformation( rc, this );
        conformation = rc;
    }
    
    
    int getRingStart()
    {
        if ( ! getRingConformation().isClosedRing() )
            return -1;
        
        for ( int i = 0; i < getSuperclass().size(); i++ )
            if ( _functional_group(i) == Carbonyl )
                return i + 1;
            
        return -1;
    }
    
    
    int getRingEnd()
    {
        if ( ! getRingConformation().isClosedRing() )
            return -1;
        
        int ringStart = getRingStart();
        if ( ringStart == -1 )
            return -1;
        
        return ringStart + getRingConformation().getRingSize() - 1;   
    }
    
    
    public StereoConfig getStereoConfig()
    {
        return stereo;
    }
    
    
    public Superclass getSuperclass()
    {
        return basetype.getSuperclass();
    }
    
    
    public boolean isDefinite()
    {
        if ( anomer != null && ! anomer.isDefinite() )   
            return false;
        
        if ( basetype != null && ! basetype.isDefinite() )
            return false;
        
        if ( stereo != null && ! stereo.isDefinite() )
            return false;
        
        return true;
    }
    
    
    /*  implementation of Attachable interface  */
    
    /**
    *   {@inheritDoc}
    *
    *   @throws PositionOccupiedException
    *       If the given {@link Substituent} and position conflicts with
    *       an existing Substituent at that position
    *   @throws IllegalArgumentException
    *       If position is greater than the number of positions available 
    *       for this monosaccharide (see {@link #countPositions}), or 
    *       the number of substituents exceeds {@link #countPositions}
    *       when substituents with unknown attachment positions are 
    *       considered.
    */
    public void attach( Substituent s, int position )
    throws PositionOccupiedException, IllegalArgumentException
    {
        if ( position < 1 )
        {
            //  it's an unknown
            log.debug("adding substituent '" + s + "' at unknown position");
            if ( unknowns == null )
                unknowns = new ArrayList( 2 );
            
            if ( unknowns.size() >= getAttachablePositions().size() )
            {
                throw new PositionOccupiedException(
                    "Cannot attach substituent '"
                    + s
                    + "': there are no free positions remaining"
                );
            }
                
            unknowns.add( s );
        }
        else if ( position > countPositions() ) 
        {
            throw new IllegalArgumentException(
                "Invalid position '" 
                + position 
                + "' for Substituent '"
                + s
                + "'; valid attachment positions for this monosaccharide are: "
                + StringUtils.join(", ", getAttachablePositions() )
            );
        }
        else
        {
            // if ( _functional_group( position) == s )
            //     return;
                
            if ( _position_occupied( position ) )
                throw new PositionOccupiedException( this, position );
            
            _attach( s, position );
        }
    }
    
    
    public void unattach( int position )
    {
        if ( ! _position_occupied( position ) )
            throw new PositionNotOccupiedException( this, position );
            
        _init_attached();
        
        int i = position - 1;
        Substituent s = getBasetype().getFunctionalGroups().get( i ); 
        attached.set( i, s );
        
        // //  allow detachment from basetype or not?
        // throw new UnsupportedOperationException(
        //     "Detachment of substituents from Basetype not permitted: "
        //     + "basetype="
        //     + getBasetype()
        //     + ", functional groups="
        //     + getBasetype().getFunctionalGroups()
        //     + ", attempted detachment position="
        //     + position
        // );
    }
    

    public Set<Integer> getAttachablePositions()
    {
        int size = getSuperclass().size();
        BitSet free_positions = new BitSet( size );
        
        for ( int i = 1; i <= size; i++ )
            if ( ! _position_occupied( i ) )
                free_positions.set( i );
            
        int ringStart = getRingStart();
        int ringEnd = getRingEnd();
        
        if ( ringStart > 0 && ringEnd > 0 )
        {
            free_positions.clear( ringStart - 1 );
            free_positions.clear( ringEnd - 1 );
        }
        
        return free_positions;
    }
    
    
    public Substituent getAttached( int position )
    {
        return _functional_group( position - 1 );
    }
    
    
    
    public int countPositions()
    {
        return getSuperclass().size();   
    }
    
    
    public String toString()
    {
        return "[" 
            + getClass().getSimpleName()
            + "="
            // + getBasetype()
            // + substitutions
            // + unknowns
            + getName()
            + "]"
        ;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
    *   Lazily instantiate {@link #attached} -- a {@link List} of 
    *   {@link Substituent}s obtained from our {@link Basetype}.   
    */
    private final void _init_attached()
    {
        if ( attached == null )
        {
            List<Substituent> fgs = getBasetype().getFunctionalGroups();
            attached = new ArrayList<Substituent>( fgs.size() );
            for ( Substituent s : fgs )
                attached.add( null );
        }                
        assert attached.size() == getSuperclass().size();
    }

    
    /** 
    *   Hides whether we have local substituents or we're looking at 
    *   Basetype's substituents.
    */
    private final Substituent _functional_group( int index )
    {
        if ( attached == null || attached.get( index ) == null )
            return getBasetype().getFunctionalGroups().get( index );
        else 
            return attached.get( index );
    }
    
    
    /** 
    *   Checks if given position is free in this Monosac or its basetype
    *   (since our Basetypes contain functional groups too)
    */
    private final boolean _position_occupied( int pos )
    {
        // if ( attached != null )
        Substituent s = _functional_group( pos - 1 );
        return s != OH && s != Carbonyl;
    }
    
    
    /** Called by {@link #attach} after we know it's safe to attach something. */
    private final void _attach( Substituent s, int position )
    {
        _init_attached();
        attached.set( position - 1, s );
        _normalise();
    }
    
    
    /** 
    *   Called after attaching or removing a substituent -- need to 
    *   normalise Basetype to see if basetype has changed. 
    */
    private final void _normalise()
    {
        assert attached != null;
        
        Basetype b = getNormalisedBasetype( this.basetype, this.attached );
        if ( b != this.basetype )
        {
            log.debug("basetype changed, basetype is now: " + b );
            this.basetype = b;
        }
    }
    
    
    /** Free all local substituents */
    private void unattach()
    {
        attached = null;
    }
    
    
    /*
    protected static class Terminus implements Comparable<Terminus>
    {
        final byte index;
        final Substituent attached;
        
        public Terminus( int index, Substituent s )
        {
            this.index = (byte) index;
            this.attached = s;
        }
        
        
        public int compareTo( Terminus t )
        {
            return ((Byte) this.index).compareTo( t.index );    
        }
        
        
        public int getPosition()
        {
            return index + 1;   
        }
        
        
        public Substituent getAttachedSubstituent()
        {
            return attached;   
        }
        
        public String toString()
        {
            return getPosition() + "-" + getAttachedSubstituent().getName();   
        }
    }
    */
    

} // end class Monosaccharide



