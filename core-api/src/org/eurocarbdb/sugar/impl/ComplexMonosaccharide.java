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
*   Last commit: $Rev: 1232 $ by $Author: glycoslave $ on $Date:: 2009-06-19 #$  
*/

package org.eurocarbdb.sugar.impl;

//  stdlib imports
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

//  3rd party imports - commons logging
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.sugar.*;

import static java.util.Collections.unmodifiableList;


/**
*   Implements monosaccharides.
*
*   @author mjh
*/
public class ComplexMonosaccharide extends SimpleMonosaccharide 
{
    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//

    public static final int UNKNOWN_RING = -1;
    
    /** Logging instance. */
    static final Logger log = Logger.getLogger( ComplexMonosaccharide.class );
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** List of applicable basetypes for this monosac; majority will be <= 2 basetypes */
    private List<Basetype> basetypes = new ArrayList<Basetype>( 2 );
    
    private Superclass superclass;
    
    /** The (reducing end) carbon atom that is attached to the ring oxygen. 
    *   ringStart is always < ringEnd. ringEnd & ringStart are both set to 
    *   {@link OPEN_CHAIN} if monosac is in open chain form. */
    private int ringStart = UNKNOWN_RING;
    
    /** The (non-reducing end) carbon atom that is attached to the ring oxygen. 
    *   ringEnd is always > ringStart. ringEnd & ringStart are both set to 
    *   {@link OPEN_CHAIN} if monosac is in open chain form. */
    private int ringEnd = UNKNOWN_RING;
    
    /** List of modifications to this monosaccharide. Default to a zero 
    *   size modification list to save memory for the most common case.  */
    private List<Modification> modifications = new ArrayList<Modification>( 0 );
    
    private List<Molecule> attached;

    
    //~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~//

    /**
    *   Creates a new Monosaccharide with the given attributes.
    *   It is expected that monosaccharides will generally be 
    *   looked up, rather than created, per se.
    */
    public ComplexMonosaccharide( Anomer a, Superclass s ) 
    //throws IllegalArgumentException
    {
        this( a, s, 
              UNKNOWN_RING, UNKNOWN_RING, // ring start, end
              null,                       // default basetypes
              null                        // default modifications
        );
    }
    
    /**
    *   Creates a new Monosaccharide with the given attributes.
    *   It is expected that monosaccharides will generally be 
    *   looked up, rather than created, per se.
    */
    public ComplexMonosaccharide( Anomer a, 
                           Superclass s, 
                           int ring_start, 
                           int ring_end,
                           List<Basetype> basetype_list,
                           List<Modification> modification_list ) 
    {
        if ( a == null )
            throw new IllegalArgumentException(
                "Anomer can't be null");

        if ( s == null )
            throw new IllegalArgumentException(
                "Superclass can't be null");
        
         if ( basetype_list == null )
             basetype_list = new ArrayList<Basetype>( 2 );
         
         if ( modification_list == null )
             modification_list = new ArrayList<Modification>( 0 );
         
        setAnomer( a );
        this.superclass    = s;
        this.ringEnd       = ring_end;
        this.ringStart     = ring_start;
        this.basetypes     = basetype_list;
        this.modifications = modification_list;
        
        this.attached = new ArrayList<Molecule>( superclass.size() );
    }
    
    
    public ComplexMonosaccharide( Monosaccharide example )
    {
        //  TODO here...   
        log.warn("zomg implement meeee!");
    }
    
    
    /** temp */
    public ComplexMonosaccharide() {}
    
    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /*-*
    *<p>
    *   Look up a Monosaccharide by a string name/descriptor only.
    *</p>
    *<p>
    *   INTEGRATION OF MONOSAC DB LOOKUP SHOULD GO HERE.
    *</p>
    *<p>
    *   The idea is that various sequence formats will be tried to
    *   parse the given name and the resultant monosaccharide normalised
    *   and returned.
    *</p>
    *-/
    public static Monosaccharide forName( String name )
    {
        Monosaccharide m = new Monosaccharide();
        m.name = name;
        return m;
    }
    */
    
    /**
    *   Parse a Monosaccharide from a string name/descriptor using the 
    *   given {@link SequenceFormat}.
    */
    public static Monosaccharide forName( String name, SequenceFormat format )
    {
        return format.getMonosaccharide( name );
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    public void attach( Molecule entity, int position )
    {
        if ( _position_occupied( position ) )
            throw new PositionOccupiedException( this, position );
        
        _set_position( position, entity );
        
        if ( log.isDebugEnabled() )
        {
            log.debug("attached substituent '" 
                + entity 
                + "'; monosac name is now: '" 
                + getName() 
                + "'"
            );
        }        
    }
    
    
    public String getName() 
    {  
        return null;
    }
    
    
    /**
    *   Adds the given basetype to the current list of basetypes comprising
    *   this monosaccharide.
    */
    public void addBasetype( Basetype b ) 
    {
        if ( b == null )
            throw new IllegalArgumentException(
                "Basetype can't be null");

        assert basetypes != null;
        this.basetypes.add( b );
    }
    
    /**
    *   Adds the given modification to this monosaccharide.
    */
    public void addModification( Modification m ) 
    {
        if ( m == null )
            throw new IllegalArgumentException(
                "Modification can't be null");

        assert modifications != null;
        this.modifications.add( m );
    }
    
        
    /**
    *   mjh2rene: needs clarification
    *
    *   @return Positive Startposition of the ring or -1 if not validated
    */
    public int getRingStart() 
    {
        return this.ringStart;
    }
    
    /**
    *   mjh2rene: needs clarification
    * 
    *   @return Positive endposition of the ring or -1 if not validated
    */
    public int getRingEnd() 
    {
        return this.ringEnd;
    }    
    

    @Override
    public Superclass getSuperclass()
    {
        return superclass;
    }
    
    
    /**
    *   Sets the superclass (ring-size configuration) of this 
    *   monosaccharide.
    */
    public void setSuperclass( Superclass s ) 
    throws IllegalArgumentException
    {
        if ( s == null )
            throw new IllegalArgumentException(
                "Superclass can't be null");

        this.superclass = s;
    }
    
    
    /**
    *   Returns the number of basetypes present in this monosaccharide.
    */
    public int getBasetypeCount()
    {
        return this.basetypes.size();
    }
    
    
    /**
    *   Returns the list of basetypes of this monosaccharide.
    */
    public List<Basetype> getBasetypes() 
    {
        return unmodifiableList( this.basetypes );
    }
        
    
    /**
    *   Returns the list of modifications to this monosaccharide.
    */
    public List<Modification> getModifications()
    {
        return unmodifiableList( this.modifications );
    }
    
    
    /**
    *   Removes the given {@link Basetype} from the list of 
    *   basetypes for this monosaccharide.
    *   @return true if basetype was removed.
    */
    public boolean removeBasetype( Basetype baseType )
    {
        return this.basetypes.remove( baseType );
    }

    
    /**
    *   Removes the given {@link Basetype} from the list of 
    *   modifications for this monosaccharide.
    *   @return true if modification was removed.
    */
    public boolean removeModification( Modification m )
    {
        return this.modifications.remove( m );
    }

    
    /** Sets ring start position. */
    public void setRingStart( int ringStart ) //throws IllegalArgumentException
    {
        _check_ring( ringStart, this.ringEnd );
        this.ringStart = ringStart;
    }
    
        
    /** Sets ring end position. */
    public void setRingEnd( int ringEnd ) //throws IllegalArgumentException
    {
        _check_ring( this.ringStart, ringEnd );
        this.ringEnd = ringEnd;
    }
    

    /** definition to be finalised... */
    public String toString()
    {
        return getName(); //+ "_" + Integer.toString( hashCode() & 0xFF, 16 );
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Validates passed ring start- and end-points. */
    private final void _check_ring( int ring_start, int ring_end )
    {
        if ( ringStart < -1 )
            throw new IllegalArgumentException(
                "Ring start position must be equal or larger than -1" );
            
        if ( ringEnd < -1 )
            throw new IllegalArgumentException(
                "Ring end position must be equal or larger than -1" );
            
        if ( ringStart > ringEnd )
            throw new IllegalArgumentException(
                "Endpoint must be larger than startpoint");
    }
    
    
    private final boolean _position_occupied( int position )
    {
        return attached.get( position ) != null;
    }
    
    
    private final void _set_position( int position, Molecule m )
    {
        attached.set( position, m );
    }
    
} // end class Monosaccharide



